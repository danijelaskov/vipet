package dev.askov.vipet.core.analyzer;

import dev.askov.vipet.core.analyzer.exceptions.AnalysisDataNotAvailableException;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasuresProvider;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import dev.askov.vipet.mvc.models.network.routing.ProbabilityRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RandomRoutingStrategyModel;
import java.util.List;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Analyzer<AnalysisData> extends PerformanceMeasuresProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(Analyzer.class);

  protected final boolean generateAnalysisData;
  protected AnalysisData analysisData;

  public Analyzer(final NetworkModel networkModel, final boolean generateAnalysisData) {
    super(networkModel);
    this.generateAnalysisData = generateAnalysisData;
  }

  public AnalysisData getAnalysisData() {
    if (!generateAnalysisData) {
      throw new AnalysisDataNotAvailableException();
    }

    return analysisData;
  }

  protected static ProbabilityRoutingStrategyModel getProbabilityRoutingStrategy(
      final NonTerminalNetworkNodeModel nonTerminalNetworkNodeModel) {
    final ProbabilityRoutingStrategyModel probabilityRoutingStrategyModel;

    if (nonTerminalNetworkNodeModel.getSelectedRoutingStrategy()
        instanceof ProbabilityRoutingStrategyModel sourceProbabilityRoutingStrategy) {
      probabilityRoutingStrategyModel = sourceProbabilityRoutingStrategy;
    } else if (nonTerminalNetworkNodeModel.getSelectedRoutingStrategy()
        instanceof RandomRoutingStrategyModel sourceRandomRoutingStrategy) {
      probabilityRoutingStrategyModel =
          ProbabilityRoutingStrategyModel.createFrom(sourceRandomRoutingStrategy);
    } else {
      LOGGER.error(
          "Trying to analyze network containing a node with {} routing strategy",
          nonTerminalNetworkNodeModel.getSelectedRoutingStrategy().getClass().getSimpleName());

      throw new RuntimeException("Unsupported routing strategy");
    }

    return probabilityRoutingStrategyModel;
  }

  protected static void setProbabilities(
      final List<ServiceCenterModel> serviceCenterModels, final RealMatrix probabilityMatrix) {
    final var numberOfServiceCenters = serviceCenterModels.size();

    for (var i = 0; i < numberOfServiceCenters; i++) {
      final var source = serviceCenterModels.get(i);
      final var probabilityRoutingStrategy = getProbabilityRoutingStrategy(source);

      for (var j = 0; j < numberOfServiceCenters; j++) {
        final var destination = serviceCenterModels.get(j);

        if (source.isConnectedTo(destination)) {
          probabilityMatrix.setEntry(i, j, probabilityRoutingStrategy.getProbability(destination));
        }
      }
    }
  }
}
