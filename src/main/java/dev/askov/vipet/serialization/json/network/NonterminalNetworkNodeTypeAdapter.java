package dev.askov.vipet.serialization.json.network;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.*;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;
import dev.askov.vipet.mvc.models.network.routing.ProbabilityRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RandomRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RoundRobinRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RoutingStrategyModel;
import dev.askov.vipet.serialization.NetworkSpecificationException;
import dev.askov.vipet.serialization.json.distributions.*;
import dev.askov.vipet.serialization.json.network.routing.ProbabilityRoutingStrategyAdapter;
import dev.askov.vipet.serialization.json.network.routing.RandomRoutingStrategyTypeAdapter;
import dev.askov.vipet.serialization.json.network.routing.RoundRobinRoutingStrategyTypeAdapter;
import dev.askov.vipet.serialization.json.network.routing.RoutingStrategyAdapter;
import java.io.IOException;
import java.util.function.Supplier;

public abstract class NonterminalNetworkNodeTypeAdapter<
        SomeNonTerminalNetworkNodeModel extends NonTerminalNetworkNodeModel>
    extends NetworkNodeTypeAdapter<SomeNonTerminalNetworkNodeModel> {

  private static final String TIME_DISTRIBUTION = "timeDistribution";
  private static final String ROUTING_STRATEGY = "routingStrategy";

  protected final Gson gson;

  public NonterminalNetworkNodeTypeAdapter(
      final Gson gson, final Supplier<? extends SomeNonTerminalNetworkNodeModel> supplier) {
    super(supplier);
    this.gson = gson;
  }

  @Override
  public void write(JsonWriter out, SomeNonTerminalNetworkNodeModel nonTerminalNetworkNodeModel)
      throws IOException {
    super.write(out, nonTerminalNetworkNodeModel);

    out.name(TIME_DISTRIBUTION).beginObject();
    gson.toJson(
        nonTerminalNetworkNodeModel.getSelectedTimeDistribution(),
        nonTerminalNetworkNodeModel.getSelectedTimeDistribution().getClass(),
        out);
    out.endObject();

    out.name(ROUTING_STRATEGY).beginObject();
    gson.toJson(
        nonTerminalNetworkNodeModel.getSelectedRoutingStrategy(),
        nonTerminalNetworkNodeModel.getSelectedRoutingStrategy().getClass(),
        out);
    out.endObject();
  }

  @Override
  public SomeNonTerminalNetworkNodeModel read(JsonReader in) throws IOException {
    final var nonTerminalNetworkNode = super.read(in);

    var foundTimeDistribution = false;
    var foundRoutingStrategy = false;

    while (!(foundTimeDistribution && foundRoutingStrategy) && in.hasNext()) {
      switch (in.nextName()) {
        case TIME_DISTRIBUTION -> {
          in.beginObject();
          {
            if (in.hasNext() && in.nextName().equals(TimeDistributionAdapter.TYPE)) {
              switch (in.nextString()) {
                case ExponentialTimeDistributionTypeAdapter.TYPE_NAME ->
                    readTimeDistribution(
                        nonTerminalNetworkNode, in, ExponentialTimeDistributionModel.class);
                case UniformTimeDistributionTypeAdapter.TYPE_NAME ->
                    readTimeDistribution(
                        nonTerminalNetworkNode, in, UniformTimeDistributionModel.class);
                case DeterministicTimeDistributionTypeAdapter.TYPE_NAME ->
                    readTimeDistribution(
                        nonTerminalNetworkNode, in, DeterministicTimeDistributionModel.class);
                case ErlangTimeDistributionTypeAdapter.TYPE_NAME ->
                    readTimeDistribution(
                        nonTerminalNetworkNode, in, ErlangTimeDistributionModel.class);
                case ParetoTimeDistributionTypeAdapter.TYPE_NAME ->
                    readTimeDistribution(
                        nonTerminalNetworkNode, in, ParetoTimeDistributionModel.class);
                case HyperexponentialTimeDistributionTypeAdapter.TYPE_NAME ->
                    readTimeDistribution(
                        nonTerminalNetworkNode, in, HyperexponentialTimeDistributionModel.class);
                default ->
                    throw new NetworkSpecificationException("Unknown time distribution type");
              }
              foundTimeDistribution = true;
            } else {
              throw new NetworkSpecificationException("Unknown time distribution property");
            }
          }
          in.endObject();
        }
        case ROUTING_STRATEGY -> {
          in.beginObject();
          if (in.hasNext() && in.nextName().equals(RoutingStrategyAdapter.TYPE)) {
            switch (in.nextString()) {
              case ProbabilityRoutingStrategyAdapter.TYPE_NAME ->
                  readRoutingStrategy(
                      nonTerminalNetworkNode, in, ProbabilityRoutingStrategyModel.class);
              case RandomRoutingStrategyTypeAdapter.TYPE_NAME ->
                  readRoutingStrategy(nonTerminalNetworkNode, in, RandomRoutingStrategyModel.class);
              case RoundRobinRoutingStrategyTypeAdapter.TYPE_NAME ->
                  readRoutingStrategy(
                      nonTerminalNetworkNode, in, RoundRobinRoutingStrategyModel.class);
              default -> throw new NetworkSpecificationException("Unknown routing strategy type");
            }
            foundRoutingStrategy = true;
          } else {
            throw new NetworkSpecificationException("Unknown routing strategy property");
          }
          in.endObject();
        }
      }
    }

    return nonTerminalNetworkNode;
  }

  private <SomeTimeDistribution extends TimeDistributionModel> void readTimeDistribution(
      final SomeNonTerminalNetworkNodeModel nonTerminalNetworkNode,
      final JsonReader in,
      final Class<SomeTimeDistribution> clazz) {
    final SomeTimeDistribution someTimeDistribution = gson.fromJson(in, clazz);
    final int index =
        nonTerminalNetworkNode.getTimeDistributions().stream()
            .filter(timeDistribution -> clazz.isAssignableFrom(timeDistribution.getClass()))
            .findFirst()
            .map(nonTerminalNetworkNode.getTimeDistributions()::indexOf)
            .orElse(-1);

    if (index != -1) {
      nonTerminalNetworkNode.getTimeDistributions().set(index, someTimeDistribution);
    } else {
      nonTerminalNetworkNode.addTimeDistribution(someTimeDistribution);
    }
    nonTerminalNetworkNode.setSelectedTimeDistribution(someTimeDistribution);
  }

  private <SomeRoutingStrategy extends RoutingStrategyModel> void readRoutingStrategy(
      final SomeNonTerminalNetworkNodeModel nonTerminalNetworkNode,
      final JsonReader in,
      final Class<SomeRoutingStrategy> clazz) {
    final SomeRoutingStrategy someRoutingStrategy = gson.fromJson(in, clazz);
    final int index =
        nonTerminalNetworkNode.getRoutingStrategies().stream()
            .filter(routingStrategy -> clazz.isAssignableFrom(routingStrategy.getClass()))
            .findFirst()
            .map(nonTerminalNetworkNode.getRoutingStrategies()::indexOf)
            .orElse(-1);

    someRoutingStrategy.setNetworkNode(nonTerminalNetworkNode);
    if (index != -1) {
      nonTerminalNetworkNode.getRoutingStrategies().set(index, someRoutingStrategy);
    } else {
      nonTerminalNetworkNode.addRoutingStrategy(someRoutingStrategy);
    }
    nonTerminalNetworkNode.setSelectedRoutingStrategy(someRoutingStrategy);
  }
}
