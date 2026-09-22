package dev.askov.vipet.mvc.models.network.routing;

import dev.askov.vipet.mvc.models.network.ConnectionModel;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;
import org.apache.commons.math3.random.RandomGenerator;

public abstract class RoutingStrategyModel {

  protected NonTerminalNetworkNodeModel networkNode;
  protected RandomGenerator randomGenerator;

  public RoutingStrategyModel(final NonTerminalNetworkNodeModel networkNode) {
    this.networkNode = networkNode;
  }

  public NonTerminalNetworkNodeModel getNonTerminalNetworkNodeModel() {
    return networkNode;
  }

  public void setNetworkNode(final NonTerminalNetworkNodeModel networkNode) {
    this.networkNode = networkNode;
  }

  public abstract ConnectionModel getConnection();

  public abstract String getName();

  public RandomGenerator getRandomGenerator() {
    return randomGenerator;
  }

  public void setRandomGenerator(final RandomGenerator randomGenerator) {
    this.randomGenerator = randomGenerator;
  }
}
