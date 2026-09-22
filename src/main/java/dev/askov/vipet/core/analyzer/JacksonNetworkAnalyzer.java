package dev.askov.vipet.core.analyzer;

import dev.askov.vipet.common.MathUtil;
import dev.askov.vipet.core.analyzer.exceptions.UnstableNetworkException;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.analyzer.JacksonNetworkAnalysisModel;
import dev.askov.vipet.mvc.models.distributions.ExponentialTimeDistributionModel;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.apache.commons.math3.linear.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JacksonNetworkAnalyzer extends Analyzer<JacksonNetworkAnalysisModel> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JacksonNetworkAnalyzer.class);
  private static final int MAX_NUM_JOBS = 10;

  private RealMatrix probabilities;
  private RealMatrix k1Matrix;
  private RealMatrix k2Matrix;
  private RealVector externalArrivalRates;
  private RealVector rhoVector;
  private RealVector visitRatios;
  private RealVector arrivalRates;

  public JacksonNetworkAnalyzer(
      final NetworkModel networkModel, final boolean generateAnalysisData) {
    super(networkModel, generateAnalysisData);
  }

  public JacksonNetworkAnalyzer(final NetworkModel networkModel) {
    super(networkModel, false);
  }

  @Override
  protected Task<PerformanceMeasureCollection> createTask() {

    return new Task<>() {

      @Override
      protected PerformanceMeasureCollection call() throws UnstableNetworkException {
        updateProgress(0.0, 1.0);

        LOGGER.debug("Analyzing network {}", getNetworkModel().getName());

        setStartTime(System.nanoTime());

        final List<ServiceCenterModel> serviceCenterModels =
            getNetworkModel().getServiceCenterModels();
        final var numberOfServiceCenters = serviceCenterModels.size();

        probabilities = new Array2DRowRealMatrix(numberOfServiceCenters, numberOfServiceCenters);

        setProbabilities(serviceCenterModels, probabilities);

        LOGGER.debug("Probabilities: {}", probabilities);

        final var sourceModels = getNetworkModel().getSourceModels();
        final var numberOfSources = sourceModels.size();

        externalArrivalRates = new ArrayRealVector(numberOfServiceCenters);
        var externalArrivalRatesSum = 0.0;

        for (var i = 0; i < numberOfServiceCenters; i++) {
          final var serviceCenterModel = serviceCenterModels.get(i);
          for (var j = 0; j < numberOfSources; j++) {
            final var sourceModel = sourceModels.get(j);

            if (sourceModel.getConnectionModels().stream()
                .anyMatch(connection -> connection.getDestination().equals(serviceCenterModel))) {
              var routeProbability =
                  getProbabilityRoutingStrategy(sourceModel).getProbability(serviceCenterModel);
              var arrivalRate =
                  routeProbability
                      * ((ExponentialTimeDistributionModel)
                              sourceModel.getSelectedTimeDistribution())
                          .getRate();

              externalArrivalRates.addToEntry(i, arrivalRate);
              externalArrivalRatesSum += arrivalRate;
            }
          }
        }

        LOGGER.debug("External arrival rates: {}", externalArrivalRates);

        k1Matrix =
            MatrixUtils.createRealIdentityMatrix(numberOfServiceCenters)
                .subtract(probabilities.transpose());

        k2Matrix = MatrixUtils.inverse(k1Matrix);

        arrivalRates = new QRDecomposition(k1Matrix).getSolver().solve(externalArrivalRates);

        LOGGER.debug("Arrival rate ratio vector: {}", arrivalRates);

        final var performanceMeasureCollection =
            new PerformanceMeasureCollection(getNetworkModel());

        final Map<ServiceCenterModel, List<Pair<Integer, Double>>> stateProbabilities =
            new HashMap<>();

        rhoVector = new ArrayRealVector(numberOfServiceCenters);

        // Applying Jackson's theorem: each service center can be treated independently of the
        // others
        for (var i = 0; i < numberOfServiceCenters; i++) {
          final var serviceCenterModel = serviceCenterModels.get(i);

          final var arrivalRate = arrivalRates.getEntry(i);
          final double numberOfServers = serviceCenterModel.getNumberOfServers();
          final var serviceRate =
              ((ExponentialTimeDistributionModel) serviceCenterModel.getSelectedTimeDistribution())
                  .getRate();
          // ρ = λ / (m * μ)
          final var rho = arrivalRate / (numberOfServers * serviceRate);

          rhoVector.setEntry(i, rho);

          LOGGER.debug(
              "rho({}) = {} / ({} * {}) = {}",
              serviceCenterModel.getName(),
              arrivalRate,
              numberOfServers,
              serviceRate,
              rho);

          if (rho >= 1) {
            LOGGER.warn(
                "The network is not stable: rho({}) = {} >= 1", serviceCenterModel.getName(), rho);

            throw new UnstableNetworkException(serviceCenterModel, arrivalRate);
          }

          // Throughput is equal to the arrival rate
          final var throughput = arrivalRates.getEntry(i);
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.THROUGHPUT,
              new PerformanceMeasure(throughput));

          // Utilization is ρ
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel, PerformanceMeasureType.UTILIZATION, new PerformanceMeasure(rho));
          // Calculate the utilization in a different way, using the probabilities of the states in
          // which at least one server is idle
          LOGGER.debug("Utilization of {}: {}", serviceCenterModel.getName(), rho);

          // Calculate average number of jobs in the queue, Jq
          final var numberOfJobsInQueue = calculateNumberOfJobsInQueue(i);
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.QUEUE_LENGTH,
              new PerformanceMeasure(numberOfJobsInQueue));

          // Tq = Jq / X
          final var waitingTimeInQueue = numberOfJobsInQueue / arrivalRates.getEntry(i);
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.WAITING_TIME,
              new PerformanceMeasure(waitingTimeInQueue));

          // T = Tq + s = Tq + 1 / μ
          final var responseTime = waitingTimeInQueue + 1 / serviceRate;
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.RESPONSE_TIME,
              new PerformanceMeasure(responseTime));

          // Applying Little's law: T = J / X => J = X * T
          final var averageNumberOfJobs = throughput * responseTime;
          performanceMeasureCollection.setPerformanceMeasure(
              serviceCenterModel,
              PerformanceMeasureType.NUM_JOBS,
              new PerformanceMeasure(averageNumberOfJobs));

          if (generateAnalysisData) {
            // Calculating the probability of having different numbers of jobs in the service center
            stateProbabilities.put(serviceCenterModel, new ArrayList<>());
            final var minNumJobs =
                Math.max((int) Math.floor(averageNumberOfJobs) - MAX_NUM_JOBS / 2, 0);
            if (minNumJobs != 0) {
              stateProbabilities
                  .get(serviceCenterModel)
                  .add(new Pair<>(0, marginalProbability0(i)));
              for (var j = 1; j < Math.min(minNumJobs, MAX_NUM_JOBS); j++) {
                stateProbabilities
                    .get(serviceCenterModel)
                    .add(new Pair<>(j, marginalProbability(i, j)));
              }
            }
            for (var j = minNumJobs; j < minNumJobs + MAX_NUM_JOBS; j++) {
              stateProbabilities
                  .get(serviceCenterModel)
                  .add(new Pair<>(j, marginalProbability(i, j)));
            }
          }
        }

        visitRatios = arrivalRates.mapDivide(externalArrivalRatesSum);

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

        // λ = sum(1, N, α_i)
        var systemThroughput = externalArrivalRates.getL1Norm();
        performanceMeasureCollection.setSystemPerformanceMeasure(
            PerformanceMeasureType.THROUGHPUT, new PerformanceMeasure(systemThroughput));

        double systemNumberOfJobs = 0;
        for (var i = 0; i < numberOfServiceCenters; i++) {
          final var serviceCenterModel = serviceCenterModels.get(i);

          systemNumberOfJobs +=
              performanceMeasureCollection
                  .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.NUM_JOBS)
                  .center();
        }
        performanceMeasureCollection.setSystemPerformanceMeasure(
            PerformanceMeasureType.NUM_JOBS, new PerformanceMeasure(systemNumberOfJobs));

        setEndTime(System.nanoTime());

        if (generateAnalysisData) {
          analysisData =
              new JacksonNetworkAnalysisModel(
                  probabilities,
                  k1Matrix,
                  k2Matrix,
                  externalArrivalRates,
                  arrivalRates,
                  rhoVector,
                  performanceMeasureCollection,
                  stateProbabilities);
        }

        LOGGER.debug("Analysis finished");
        updateProgress(1.0, 1.0);

        return performanceMeasureCollection;
      }
    };
  }

  private double marginalProbability0(final int serviceCenterIndex) {
    final var numberOfServers =
        getNetworkModel().getServiceCenterModels().get(serviceCenterIndex).getNumberOfServers();
    final var rho = rhoVector.getEntry(serviceCenterIndex);

    return MathUtil.inv(
        1.0
            + MathUtil.sum(
                1, numberOfServers - 1, i -> Math.pow(numberOfServers * rho, i) / MathUtil.fact(i))
            + Math.pow(numberOfServers * rho, numberOfServers)
                / (MathUtil.fact(numberOfServers) * (1.0 - rho)));
  }

  private double marginalProbability(final int serviceCenterIndex, final int numberOfJobs) {
    if (numberOfJobs == 0) {
      return marginalProbability0(serviceCenterIndex);
    }

    final var numberOfServers =
        getNetworkModel().getServiceCenterModels().get(serviceCenterIndex).getNumberOfServers();
    final var rho = rhoVector.getEntry(serviceCenterIndex);

    if (numberOfJobs < numberOfServers) {
      return Math.pow(numberOfServers * rho, numberOfJobs)
          / MathUtil.fact(numberOfJobs)
          * marginalProbability0(serviceCenterIndex);
    } else {
      return Math.pow(rho, numberOfJobs)
          * Math.pow(numberOfServers, numberOfServers)
          / MathUtil.fact(numberOfServers)
          * marginalProbability0(serviceCenterIndex);
    }
  }

  private double calculateNumberOfJobsInQueue(final int serviceCenterIndex) {
    final var rho = rhoVector.getEntry(serviceCenterIndex);
    final var numberOfServers =
        getNetworkModel().getServiceCenterModels().get(serviceCenterIndex).getNumberOfServers();

    return Math.pow(numberOfServers * rho, numberOfServers)
        * rho
        / (MathUtil.fact(numberOfServers) * Math.pow(1 - rho, 2))
        * marginalProbability0(serviceCenterIndex);
  }
}
