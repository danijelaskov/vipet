package dev.askov.vipet.mvc.models.network.routing;

import dev.askov.vipet.mvc.models.network.ConnectionModel;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;

public final class RandomRoutingStrategyModel extends RoutingStrategyModel {

  public RandomRoutingStrategyModel() {
    super(null);
  }

  public RandomRoutingStrategyModel(final NonTerminalNetworkNodeModel networkNode) {
    super(networkNode);
  }

  @Override
  public ConnectionModel getConnection() {
    if (networkNode.getConnectionModels().size() > 1) {
      final var randomValue =
          (int) (getRandomGenerator().nextDouble() * networkNode.getConnectionModels().size());
      return networkNode.getConnectionModels().get(randomValue);
    } else if (networkNode.getConnectionModels().size() == 1) {
      return networkNode.getConnectionModels().getFirst();
    }

    return null;
  }

  @Override
  public String getName() {
    return "random";
  }
}
