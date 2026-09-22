package dev.askov.vipet.mvc.controllers.converters;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;
import javafx.util.StringConverter;

public final class NonTerminalNetworkNodeConverter
    extends StringConverter<NonTerminalNetworkNodeModel> {

  final NetworkModel networkModel;

  public NonTerminalNetworkNodeConverter(final NetworkModel networkModel) {
    this.networkModel = networkModel;
  }

  @Override
  public String toString(final NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
    if (nonTerminalNetworkNode == null) {
      return null;
    }
    return nonTerminalNetworkNode.getName();
  }

  @Override
  public NonTerminalNetworkNodeModel fromString(final String string) {
    return networkModel.getNonTerminalNetworkNodeModels().stream()
        .filter(nonTerminalNetworkNode -> nonTerminalNetworkNode.getName().equals(string))
        .findFirst()
        .orElseThrow();
  }
}
