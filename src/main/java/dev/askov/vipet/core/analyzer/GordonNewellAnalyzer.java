package dev.askov.vipet.core.analyzer;

import dev.askov.vipet.common.MathUtil;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.analyzer.GordonNewellAnalysisModel;
import dev.askov.vipet.mvc.models.distributions.ExponentialTimeDistributionModel;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.apache.commons.math3.linear.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GordonNewellAnalyzer extends Analyzer<GordonNewellAnalysisModel> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GordonNewellAnalyzer.class);

  private RealMatrix k1Matrix;
  private RealMatrix k2Matrix;
  private RealVector visitRatios;
  private RealMatrix fMatrix;
  private RealMatrix gMatrix;
  private RealVector gVector;

  private final Map<ServiceCenterModel, RealVector> gVectorForExcludedServiceCenter =
      new HashMap<>();

  public GordonNewellAnalyzer(final NetworkModel networkModel, final boolean generateAnalysisData) {
    super(networkModel, generateAnalysisData);
  }

  public GordonNewellAnalyzer(final NetworkModel networkModel) {
    super(networkModel, false);
  }

  @Override
  protected Task<PerformanceMeasureCollection> createTask() {

    return new Task<>() {

      @Override
      protected PerformanceMeasureCollection call() {
        LOGGER.debug("Starting Gordon-Newell analysis");

        updateProgress(0.0, 1.0);
        setStartTime(System.nanoTime());

        final var centralServiceCenterModel = getNetworkModel().getCentralServiceCenterModel();
        final List<ServiceCenterModel> serviceCenterModels =
            getNetworkModel().getServiceCenterModels();
        final var numberOfServiceCenters = serviceCenterModels.size();
        final var centralServiceCenterIndex =
            getNetworkModel()
                .getNodeModels()
                .indexOf(getNetworkModel().getCentralServiceCenterModel());

        final RealMatrix identityMatrix =
            new Array2DRowRealMatrix(numberOfServiceCenters, numberOfServiceCenters);
        for (var i = 0; i < numberOfServiceCenters; i++) {
          identityMatrix.setEntry(i, i, 1.0);
        }

        final RealMatrix probabilityMatrix =
            new Array2DRowRealMatrix(numberOfServiceCenters, numberOfServiceCenters);

        setProbabilities(serviceCenterModels, probabilityMatrix);

        final var identitySubMatrix =
            identityMatrix.getSubMatrix(
                0, numberOfServiceCenters - 2, 0, numberOfServiceCenters - 2);

        LOGGER.debug("Identity sub-matrix: {}", identitySubMatrix);

        final var nonCentralServiceCenterIndices = new int[numberOfServiceCenters - 1];
        for (var i = 0; i < numberOfServiceCenters - 1; i++) {
          nonCentralServiceCenterIndices[i] = i < centralServiceCenterIndex ? i : i + 1;
        }
        final var probabilitySubMatrix =
            probabilityMatrix.getSubMatrix(
                nonCentralServiceCenterIndices, nonCentralServiceCenterIndices);

        LOGGER.debug("Probability sub-matrix: {}", probabilitySubMatrix);

        final RealVector probabilitySubVector = new ArrayRealVector(numberOfServiceCenters - 1);
        for (var i = 0; i < numberOfServiceCenters - 1; i++) {
          probabilitySubVector.setEntry(
              i,
              probabilityMatrix.getEntry(
                  centralServiceCenterIndex, i < centralServiceCenterIndex ? i : i + 1));
        }

        LOGGER.debug("Probability sub-vector: {}", probabilitySubVector);

        k1Matrix = identitySubMatrix.subtract(probabilitySubMatrix.transpose());

        k2Matrix = MatrixUtils.inverse(k1Matrix);

        final var visitRatiosSubVector =
            new QRDecomposition(k1Matrix).getSolver().solve(probabilitySubVector);

        visitRatios = new ArrayRealVector(numberOfServiceCenters);
        visitRatios.setEntry(centralServiceCenterIndex, 1.0);
        for (var i = 0; i < numberOfServiceCenters - 1; i++) {
          visitRatios.setEntry(nonCentralServiceCenterIndices[i], visitRatiosSubVector.getEntry(i));
        }

        LOGGER.debug("Visit ratios: {}", visitRatios);

        final var totalNumberOfJobs = getNetworkModel().getNumberOfJobs();

        if (generateAnalysisData) {
          calculateGMatrix(totalNumberOfJobs);
          gVector = gMatrix.getColumnVector(numberOfServiceCenters - 1);
          LOGGER.debug("G matrix: {}", gMatrix);
        } else {
          calculateGVector(totalNumberOfJobs);
        }
        LOGGER.debug("G vector: {}", gVector);

        LOGGER.debug("Total number of jobs: {}", totalNumberOfJobs);
        LOGGER.debug("Normalization constants: {}", gVector);

        final RealVector rateVector = new ArrayRealVector(numberOfServiceCenters);
        for (var i = 0; i < numberOfServiceCenters; i++) {
          final var rate =
              ((ExponentialTimeDistributionModel)
                      serviceCenterModels.get(i).getSelectedTimeDistribution())
                  .getRate();

          rateVector.setEntry(i, rate);
        }

        LOGGER.debug("Rate vector: {}", rateVector);

        final var relativeUtilizationVector = visitRatios.ebeDivide(rateVector);

        LOGGER.debug("Relative utilization vector: {}", relativeUtilizationVector);

        final var performanceMeasureCollection =
            new PerformanceMeasureCollection(getNetworkModel());

        var systemNumberOfJobs = 0.0;
        for (final var serviceCenterModel : serviceCenterModels) {
          final var serviceCenterIndex = serviceCenterModels.indexOf(serviceCenterModel);
          final var visitRatio = visitRatios.getEntry(serviceCenterIndex);
          final var gValue = gVector.getEntry(totalNumberOfJobs - 1);
          final var normalizationConstant = gVector.getEntry(totalNumberOfJobs);

          // X = e * G(K-1) / G(K)
          final var throughput = visitRatio * gValue / normalizationConstant;
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.THROUGHPUT,
              new PerformanceMeasure(throughput));

          if (serviceCenterModel == centralServiceCenterModel) {
            // Throughput for the central service center
            performanceMeasureCollection.setSystemPerformanceMeasure(
                PerformanceMeasureType.THROUGHPUT, new PerformanceMeasure(throughput));
          }

          final var numberOfServers = serviceCenterModel.getNumberOfServers();
          final var serviceRate =
              ((ExponentialTimeDistributionModel) serviceCenterModel.getSelectedTimeDistribution())
                  .getRate();

          // U = X / (m * mu)
          final var utilization = throughput / (numberOfServers * serviceRate);
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.UTILIZATION,
              new PerformanceMeasure(utilization));

          var queueLength = 0.0;
          if (numberOfServers == 1) {
            // Q = 1 / G(K) * sum(2, K, x^i * G(K-i))
            for (var j = numberOfServers + 1; j <= totalNumberOfJobs; j++) {
              queueLength +=
                  Math.pow(visitRatio / serviceRate, j) * gVector.getEntry(totalNumberOfJobs - j);
            }
            queueLength = queueLength / gVector.getEntry(totalNumberOfJobs);
          } else {
            // Q = sum(m + 1, K, (k - m) * π(i))
            for (var k = numberOfServers + 1; k <= totalNumberOfJobs; k++) {
              queueLength += (k - numberOfServers) * marginalProbability(serviceCenterModel, k);
            }
          }
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.QUEUE_LENGTH,
              new PerformanceMeasure(queueLength));

          // Applying Little's law: W = Q / X
          final var waitingTime = queueLength / throughput;
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.WAITING_TIME,
              new PerformanceMeasure(waitingTime));

          // R = W + 1 / mu
          final var responseTime = waitingTime + 1.0 / serviceRate;
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.RESPONSE_TIME,
              new PerformanceMeasure(responseTime));

          // Applying Little's law: J = X * R
          var numberOfJobs = throughput * responseTime;
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.NUM_JOBS,
              new PerformanceMeasure(numberOfJobs));

          systemNumberOfJobs += numberOfJobs;

          updateProgress(
              0.5 + 0.5 * ((double) (serviceCenterIndex + 1) / serviceCenterModels.size()), 1.0);
        }

        performanceMeasureCollection.setSystemPerformanceMeasure(
            PerformanceMeasureType.NUM_JOBS, new PerformanceMeasure(systemNumberOfJobs));

        var systemResponseTime = 0.0;
        for (var i = 0; i < numberOfServiceCenters; i++) {
          systemResponseTime +=
              visitRatios.getEntry(i)
                  * performanceMeasureCollection
                      .getPerformanceMeasure(
                          serviceCenterModels.get(i), PerformanceMeasureType.RESPONSE_TIME)
                      .center();
        }
        performanceMeasureCollection.setSystemPerformanceMeasure(
            PerformanceMeasureType.RESPONSE_TIME, new PerformanceMeasure(systemResponseTime));

        setEndTime(System.nanoTime());

        LOGGER.debug("Gordon-Newell analysis completed in {} ms", getElapsedTime() / 1_000_000.0);

        if (generateAnalysisData) {
          Map<ServiceCenterModel, List<Pair<Integer, Double>>> stateProbabilities = new HashMap<>();

          for (final var serviceCenterModel : serviceCenterModels) {
            final List<Pair<Integer, Double>> stateProbabilitiesList = new java.util.ArrayList<>();

            for (var i = 0; i <= totalNumberOfJobs; i++) {
              final var stateProbability = marginalProbability(serviceCenterModel, i);
              stateProbabilitiesList.add(new Pair<>(i, stateProbability));
            }

            stateProbabilities.put(serviceCenterModel, stateProbabilitiesList);
          }

          analysisData =
              new GordonNewellAnalysisModel(
                  probabilitySubMatrix,
                  k1Matrix,
                  k2Matrix,
                  probabilitySubVector,
                  visitRatiosSubVector,
                  visitRatios,
                  fMatrix,
                  gMatrix,
                  numberOfServiceCenters,
                  totalNumberOfJobs,
                  performanceMeasureCollection,
                  stateProbabilities);
        }

        LOGGER.debug("Analysis finished");
        updateProgress(1.0, 1.0);

        return performanceMeasureCollection;
      }
    };
  }

  private double F(final ServiceCenterModel serviceCenterModel, final int numberOfJobs) {
    final var serviceCenterIndex =
        getNetworkModel().getServiceCenterModels().indexOf(serviceCenterModel);

    return Math.pow(visitRatios.getEntry(serviceCenterIndex), numberOfJobs)
        / A(serviceCenterModel, numberOfJobs);
  }

  private static double A(final ServiceCenterModel serviceCenterModel, final int numberOfJobs) {
    double serviceRateProduct = 1;

    for (var k = 1; k <= numberOfJobs; k++) {
      serviceRateProduct *= serviceRate(serviceCenterModel, k);
    }

    return serviceRateProduct;
  }

  private static double serviceRate(
      final ServiceCenterModel serviceCenterModel, final int numberOfJobs) {
    final var numberOfServers = serviceCenterModel.getNumberOfServers();
    final var serviceRate =
        ((ExponentialTimeDistributionModel) serviceCenterModel.getSelectedTimeDistribution())
            .getRate();

    return numberOfJobs <= numberOfServers
        ? numberOfJobs * serviceRate
        : numberOfServers * serviceRate;
  }

  private void calculateGVector(final int numberOfJobs) {
    final var numberOfServiceCenters = getNetworkModel().getServiceCenterModels().size();
    final var initialFVector =
        calculateFVector(getNetworkModel().getServiceCenterModels().getFirst(), numberOfJobs);

    fMatrix = new Array2DRowRealMatrix(numberOfJobs + 1, numberOfServiceCenters);
    fMatrix.setColumnVector(0, initialFVector);

    gVector = new ArrayRealVector(numberOfJobs + 1);
    for (var i = 0; i <= numberOfJobs; i++) {
      gVector.setEntry(i, initialFVector.getEntry(i));
    }

    gVector = calculateFVector(getNetworkModel().getServiceCenterModels().getFirst(), numberOfJobs);
    final RealVector nextGVector = new ArrayRealVector(numberOfJobs + 1);

    for (var n = 1; n < numberOfServiceCenters; n++) {
      final var currentServiceCenterModel = getNetworkModel().getServiceCenterModels().get(n);
      final var fVector = calculateFVector(currentServiceCenterModel, numberOfJobs);

      fMatrix.setColumnVector(n, fVector);

      for (var k = 0; k <= numberOfJobs; k++) {
        double sum = 0;
        for (var j = 0; j <= k; j++) {
          sum += gVector.getEntry(k - j) * fVector.getEntry(j);
        }

        nextGVector.setEntry(k, sum);
      }

      gVector.setSubVector(0, nextGVector);
    }
  }

  private void calculateGMatrix(final int numberOfJobs) {
    final var numberOfServiceCenters = getNetworkModel().getServiceCenterModels().size();
    final var initialFVector =
        calculateFVector(getNetworkModel().getServiceCenterModels().getFirst(), numberOfJobs);

    fMatrix = new Array2DRowRealMatrix(numberOfJobs + 1, numberOfServiceCenters);
    fMatrix.setColumnVector(0, initialFVector);
    gMatrix = new Array2DRowRealMatrix(numberOfJobs + 1, numberOfServiceCenters);
    gMatrix.setColumnVector(0, initialFVector);

    for (var n = 1; n < numberOfServiceCenters; n++) {
      final var serviceCenterModel = getNetworkModel().getServiceCenterModels().get(n);
      final var fVector = calculateFVector(serviceCenterModel, numberOfJobs);

      fMatrix.setColumnVector(n, fVector);

      for (var k = 0; k <= numberOfJobs; k++) {
        double sum = 0;
        for (var j = 0; j <= k; j++) {
          sum += gMatrix.getEntry(k - j, n - 1) * fVector.getEntry(j);
        }

        gMatrix.setEntry(k, n, sum);
      }
    }
  }

  private RealVector calculateFVector(
      final ServiceCenterModel serviceCenterModel, final int numberOfJobs) {
    final RealVector fVector = new ArrayRealVector(numberOfJobs + 1);

    fVector.setEntry(0, 1.0);
    for (var k = 1; k <= numberOfJobs; k++) {
      fVector.setEntry(k, F(serviceCenterModel, k));
    }

    return fVector;
  }

  private RealVector getGVectorForExcludedServiceCenter(
      final ServiceCenterModel serviceCenterModel) {
    if (gVectorForExcludedServiceCenter.containsKey(serviceCenterModel)) {
      return gVectorForExcludedServiceCenter.get(serviceCenterModel);
    }

    final var serviceCenterIndex =
        getNetworkModel().getServiceCenterModels().indexOf(serviceCenterModel);
    final var fVector = fMatrix.getColumnVector(serviceCenterIndex);
    final var totalNumberOfJobs = getNetworkModel().getNumberOfJobs();

    final RealVector gVector = new ArrayRealVector(totalNumberOfJobs + 1);
    gVector.setEntry(0, 1.0);
    for (var k = 1; k <= totalNumberOfJobs; k++) {
      final var finalK = k;

      gVector.setEntry(
          k,
          this.gVector.getEntry(k)
              - MathUtil.sum(1, k, j -> fVector.getEntry(j) * gVector.getEntry(finalK - j)));
    }

    gVectorForExcludedServiceCenter.put(serviceCenterModel, gVector);

    return gVector;
  }

  public double marginalProbability(
      final ServiceCenterModel serviceCenterModel, final int numberOfJobs) {
    final var serviceCenterIndex =
        getNetworkModel().getServiceCenterModels().indexOf(serviceCenterModel);
    final var numberOfServers = serviceCenterModel.getNumberOfServers();
    final var visitRatio = visitRatios.getEntry(serviceCenterIndex);
    final var serviceRate =
        ((ExponentialTimeDistributionModel) serviceCenterModel.getSelectedTimeDistribution())
            .getRate();
    final var totalNumberOfJobs = getNetworkModel().getNumberOfJobs();
    final var normalizationConstant = gVector.getEntry(totalNumberOfJobs);
    final var gValue =
        numberOfJobs < totalNumberOfJobs
            ? gVector.getEntry(totalNumberOfJobs - numberOfJobs - 1)
            : 0;

    if (numberOfServers == 1) {
      return Math.pow(visitRatio / serviceRate, numberOfJobs)
          / normalizationConstant
          * (gVector.getEntry(totalNumberOfJobs - numberOfJobs)
              - visitRatio / serviceRate * gValue);
    } else {
      final var fVector = fMatrix.getColumnVector(serviceCenterIndex);
      final var gVectorForExcludedServiceCenter =
          getGVectorForExcludedServiceCenter(serviceCenterModel);

      return fVector.getEntry(numberOfJobs)
          / normalizationConstant
          * gVectorForExcludedServiceCenter.getEntry(totalNumberOfJobs - numberOfJobs);
    }
  }
}
