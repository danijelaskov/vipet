package dev.askov.vipet.mvc.models.network.routing;

import dev.askov.vipet.mvc.models.network.ConnectionModel;
import dev.askov.vipet.mvc.models.network.NetworkNodeModel;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;

public final class ProbabilityRoutingStrategyModel extends RoutingStrategyModel {

  private final MapProperty<NetworkNodeModel, DoubleProperty> probabilityMap =
      new SimpleMapProperty<>(
          this, "probabilityMap", FXCollections.observableMap(new LinkedHashMap<>()));
  private final List<Double> probabilities = new ArrayList<>();

  public ProbabilityRoutingStrategyModel() {
    super(null);
  }

  public ProbabilityRoutingStrategyModel(final NonTerminalNetworkNodeModel networkNode) {
    super(networkNode);
  }

  public ProbabilityRoutingStrategyModel(
      final ProbabilityRoutingStrategyModel probabilityRoutingStrategy,
      final NonTerminalNetworkNodeModel networkNode) {
    super(networkNode);

    getProbabilityMap().putAll(probabilityRoutingStrategy.getProbabilityMap());
  }

  public MapProperty<NetworkNodeModel, DoubleProperty> probabilityMap() {
    return probabilityMap;
  }

  public Map<NetworkNodeModel, DoubleProperty> getProbabilityMap() {
    return probabilityMap.get();
  }

  public DoubleProperty probability(final NetworkNodeModel networkNode) {
    return getProbabilityMap().get(networkNode);
  }

  public double getProbability(final NetworkNodeModel networkNode) {
    return getProbabilityMap().get(networkNode).get();
  }

  public void setProbability(final NetworkNodeModel networkNode, double probability) {
    getProbabilityMap()
        .put(
            networkNode,
            new SimpleDoubleProperty(this, networkNode.getName() + "Probability", probability));
  }

  public List<Double> getProbabilities() {
    return probabilities;
  }

  public void setNextProbability(final double probability) {
    probabilities.add(probability);
  }

  public void assignNextProbabilityToRouteDestination(final NetworkNodeModel destination) {
    getProbabilityMap()
        .put(
            destination,
            new SimpleDoubleProperty(
                this, destination.getName() + "Probability", probabilities.removeFirst()));
  }

  public static ProbabilityRoutingStrategyModel createFrom(
      final RandomRoutingStrategyModel randomRoutingStrategy) {
    final var networkNode = randomRoutingStrategy.getNonTerminalNetworkNodeModel();
    final var probabilityRoutingStrategy = new ProbabilityRoutingStrategyModel(networkNode);

    for (final var connectionModel : networkNode.getConnectionModels()) {
      probabilityRoutingStrategy.setProbability(
          connectionModel.getDestination(), 1.0 / networkNode.getConnectionModels().size());
    }

    return probabilityRoutingStrategy;
  }

  @Override
  public ConnectionModel getConnection() {
    if (networkNode.getConnectionModels().size() > 1) {
      final var randomValue = getRandomGenerator().nextDouble();
      var sum = 0.0;
      for (final var connectionModel : networkNode.getConnectionModels()) {
        sum += getProbabilityMap().get(connectionModel.getDestination()).get();
        if (randomValue < sum) {
          return connectionModel;
        }
      }
    } else if (networkNode.getConnectionModels().size() == 1) {
      return networkNode.getConnectionModels().getFirst();
    }

    return null;
  }

  @Override
  public String getName() {
    return "probabilities";
  }
}
