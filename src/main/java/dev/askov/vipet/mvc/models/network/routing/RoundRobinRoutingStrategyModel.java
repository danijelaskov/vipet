package dev.askov.vipet.mvc.models.network.routing;

import dev.askov.vipet.mvc.models.network.ConnectionModel;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;

public final class RoundRobinRoutingStrategyModel extends RoutingStrategyModel {

  private int index = 0;

  public RoundRobinRoutingStrategyModel() {
    super(null);
  }

  public RoundRobinRoutingStrategyModel(final NonTerminalNetworkNodeModel networkNode) {
    super(networkNode);
  }

  @Override
  public ConnectionModel getConnection() {
    if (!networkNode.getConnectionModels().isEmpty()) {
      if (index >= networkNode.getConnectionModels().size()) {
        index = 0;
      }
      return networkNode.getConnectionModels().get(index++);
    }

    return null;
  }

  @Override
  public String getName() {
    return "roundRobin";
  }
}
