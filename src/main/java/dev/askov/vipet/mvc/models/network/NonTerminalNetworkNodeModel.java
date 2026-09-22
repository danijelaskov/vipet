package dev.askov.vipet.mvc.models.network;

import dev.askov.vipet.mvc.models.distributions.*;
import dev.askov.vipet.mvc.models.network.routing.ProbabilityRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RandomRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RoundRobinRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RoutingStrategyModel;
import java.util.List;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.math3.random.RandomGenerator;

public abstract class NonTerminalNetworkNodeModel extends NetworkNodeModel {

  private final ListProperty<TimeDistributionModel> timeDistributionsProperty =
      new SimpleListProperty<>(this, "timeDistributions", null);
  private final ObjectProperty<TimeDistributionModel> selectedTimeDistributionProperty =
      new SimpleObjectProperty<>(this, "selectedTimeDistribution", null);
  private final ListProperty<RoutingStrategyModel> routingStrategiesProperty =
      new SimpleListProperty<>(this, "routingStrategies", null);
  private final ObjectProperty<RoutingStrategyModel> selectedRoutingStrategyProperty =
      new SimpleObjectProperty<>(this, "selectedRoutingStrategy", null);

  protected final ListProperty<ConnectionModel> connectionsProperty =
      new SimpleListProperty<>(this, "connections", FXCollections.observableArrayList());

  protected final ObjectProperty<RandomGenerator> randomGeneratorObjectProperty =
      new SimpleObjectProperty<>(this, "randomGenerator", null);

  public NonTerminalNetworkNodeModel(
      final long id, final String name, final double x, final double y) {
    super(id, name, x, y);

    timeDistributionsProperty.set(creteTimeDistributions());
    selectedTimeDistributionProperty.set(timeDistributionsProperty.getFirst());
    routingStrategiesProperty.set(createRoutingStrategies());
    selectedRoutingStrategyProperty.set(routingStrategiesProperty.getFirst());
    connectionsProperty.addListener(
        (ListChangeListener<ConnectionModel>)
            change -> {
              while (change.next()) {
                if (change.wasAdded()) {
                  for (final var connectionModel : change.getAddedSubList()) {
                    final var probabilityRoutingStrategy =
                        getRoutingStrategies().stream()
                            .filter(
                                routingStrategy ->
                                    routingStrategy instanceof ProbabilityRoutingStrategyModel)
                            .map(
                                routingStrategy ->
                                    (ProbabilityRoutingStrategyModel) routingStrategy)
                            .findFirst()
                            .orElse(null);

                    if (probabilityRoutingStrategy != null) {
                      if (probabilityRoutingStrategy.getProbabilities().isEmpty()) {
                        final var totalProbability =
                            probabilityRoutingStrategy.getProbabilityMap().values().stream()
                                .mapToDouble(DoubleProperty::getValue)
                                .sum();

                        if (totalProbability < 1.0) {
                          probabilityRoutingStrategy
                              .getProbabilityMap()
                              .put(
                                  connectionModel.getDestination(),
                                  new SimpleDoubleProperty(
                                      probabilityRoutingStrategy,
                                      connectionModel.getDestination().getName() + "Probability",
                                      1.0 - totalProbability));
                        } else {
                          final var probability = 1.0 / change.getList().size();

                          for (final var otherConnectionModel : getConnectionModels()) {
                            probabilityRoutingStrategy.setProbability(
                                otherConnectionModel.getDestination(), probability);
                          }
                        }
                      } else {
                        probabilityRoutingStrategy.assignNextProbabilityToRouteDestination(
                            connectionModel.getDestination());
                      }
                    }
                  }
                } else if (change.wasRemoved()) {
                  for (final var connectionModel : change.getRemoved()) {
                    if (getSelectedRoutingStrategy()
                        instanceof ProbabilityRoutingStrategyModel probabilityRoutingStrategy) {
                      probabilityRoutingStrategy
                          .getProbabilityMap()
                          .remove(connectionModel.getDestination());
                    }
                  }
                }
              }
            });

    addRandomGeneratorListener();
  }

  public NonTerminalNetworkNodeModel(final long id) {
    this(id, "", 0.f, 0.f);
  }

  public NonTerminalNetworkNodeModel(final NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
    super(nonTerminalNetworkNode);

    setSelectedTimeDistribution(copySelectedTimeDistribution(nonTerminalNetworkNode));
    setSelectedRoutingStrategy(copySelectedRoutingStrategy(nonTerminalNetworkNode));

    addRandomGeneratorListener();
  }

  public ListProperty<ConnectionModel> connectionsProperty() {
    return connectionsProperty;
  }

  public List<ConnectionModel> getConnectionModels() {
    return connectionsProperty.get();
  }

  public void addConnectionModel(final ConnectionModel connectionModel) {
    connectionsProperty.add(connectionModel);
  }

  public ObservableList<RoutingStrategyModel> getRoutingStrategies() {
    return routingStrategiesProperty.get();
  }

  public void setRoutingStrategies(final List<RoutingStrategyModel> routingStrategies) {
    routingStrategiesProperty.set(FXCollections.observableArrayList(routingStrategies));
  }

  public void addRoutingStrategy(final RoutingStrategyModel routingStrategyModel) {
    routingStrategiesProperty.get().add(routingStrategyModel);
  }

  public ObjectProperty<RoutingStrategyModel> selectedRoutingStrategyProperty() {
    return selectedRoutingStrategyProperty;
  }

  public RoutingStrategyModel getSelectedRoutingStrategy() {
    return selectedRoutingStrategyProperty.get();
  }

  public void setSelectedRoutingStrategy(final RoutingStrategyModel selectedRoutingStrategyModel) {
    selectedRoutingStrategyProperty.set(selectedRoutingStrategyModel);
  }

  public ListProperty<TimeDistributionModel> timeDistributionsProperty() {
    return timeDistributionsProperty;
  }

  public ObservableList<TimeDistributionModel> getTimeDistributions() {
    return timeDistributionsProperty.get();
  }

  public void addTimeDistribution(final TimeDistributionModel timeDistributionModel) {
    timeDistributionsProperty.get().add(timeDistributionModel);
  }

  public ObjectProperty<TimeDistributionModel> selectedTimeDistributionProperty() {
    return selectedTimeDistributionProperty;
  }

  public TimeDistributionModel getSelectedTimeDistribution() {
    return selectedTimeDistributionProperty.get();
  }

  public void setSelectedTimeDistribution(
      final TimeDistributionModel selectedTimeDistributionModel) {
    selectedTimeDistributionProperty.set(selectedTimeDistributionModel);
  }

  public void addConnection(final ConnectionModel connectionModel) {
    connectionsProperty.add(connectionModel);
  }

  public void removeConnection(final ConnectionModel connectionModel) {
    connectionsProperty.remove(connectionModel);

    if (getSelectedRoutingStrategy()
        instanceof ProbabilityRoutingStrategyModel probabilityRoutingStrategy) {
      probabilityRoutingStrategy.getProbabilityMap().remove(connectionModel.getDestination());
    }
  }

  public boolean isConnectedTo(final NetworkNodeModel node) {
    return getConnectionModels().stream()
        .anyMatch(connection -> connection.getDestination().equals(node));
  }

  public ObjectProperty<RandomGenerator> randomGeneratorObjectProperty() {
    return randomGeneratorObjectProperty;
  }

  public RandomGenerator getRandomGenerator() {
    return randomGeneratorObjectProperty.get();
  }

  public void setRandomGenerator(final RandomGenerator randomGenerator) {
    randomGeneratorObjectProperty.set(randomGenerator);
  }

  private void addRandomGeneratorListener() {
    randomGeneratorObjectProperty.addListener(
        (observableRng, oldRng, newRng) -> {
          getSelectedTimeDistribution().setRandomGenerator(newRng);
          getSelectedRoutingStrategy().setRandomGenerator(newRng);
        });
  }

  private ObservableList<RoutingStrategyModel> createRoutingStrategies() {
    return FXCollections.observableArrayList(
        new ProbabilityRoutingStrategyModel(this),
        new RandomRoutingStrategyModel(this),
        new RoundRobinRoutingStrategyModel(this));
  }

  private static ObservableList<TimeDistributionModel> creteTimeDistributions() {
    return FXCollections.observableArrayList(
        new ExponentialTimeDistributionModel(),
        new ErlangTimeDistributionModel(),
        new ParetoTimeDistributionModel(),
        new UniformTimeDistributionModel(),
        new DeterministicTimeDistributionModel(),
        new HyperexponentialTimeDistributionModel());
  }

  private static TimeDistributionModel copySelectedTimeDistribution(
      NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
    final var timeDistributionModel = nonTerminalNetworkNode.getSelectedTimeDistribution();
    TimeDistributionModel timeDistributionModelCopy = null;

    if (timeDistributionModel
        instanceof ExponentialTimeDistributionModel exponentialTimeDistribution) {
      timeDistributionModelCopy = new ExponentialTimeDistributionModel(exponentialTimeDistribution);
    } else if (timeDistributionModel
        instanceof UniformTimeDistributionModel uniformTimeDistribution) {
      timeDistributionModelCopy = new UniformTimeDistributionModel(uniformTimeDistribution);
    } else if (timeDistributionModel
        instanceof DeterministicTimeDistributionModel deterministicTimeDistribution) {
      timeDistributionModelCopy =
          new DeterministicTimeDistributionModel(deterministicTimeDistribution);
    } else if (timeDistributionModel
        instanceof ErlangTimeDistributionModel erlangTimeDistribution) {
      timeDistributionModelCopy = new ErlangTimeDistributionModel(erlangTimeDistribution);
    } else if (timeDistributionModel
        instanceof ParetoTimeDistributionModel paretoTimeDistribution) {
      timeDistributionModelCopy = new ParetoTimeDistributionModel(paretoTimeDistribution);
    } else if (timeDistributionModel
        instanceof HyperexponentialTimeDistributionModel hyperexponentialTimeDistributionModel) {
      timeDistributionModelCopy =
          new HyperexponentialTimeDistributionModel(hyperexponentialTimeDistributionModel);
    }

    return timeDistributionModelCopy;
  }

  private RoutingStrategyModel copySelectedRoutingStrategy(
      NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
    final var routingStrategyModel = nonTerminalNetworkNode.getSelectedRoutingStrategy();
    RoutingStrategyModel routingStrategyModelCopy = null;

    if (routingStrategyModel
        instanceof ProbabilityRoutingStrategyModel probabilityRoutingStrategy) {
      routingStrategyModelCopy =
          new ProbabilityRoutingStrategyModel(probabilityRoutingStrategy, this);
    } else if (routingStrategyModel instanceof RandomRoutingStrategyModel) {
      routingStrategyModelCopy = new RandomRoutingStrategyModel(this);
    } else if (routingStrategyModel instanceof RoundRobinRoutingStrategyModel) {
      routingStrategyModelCopy = new RoundRobinRoutingStrategyModel(this);
    }

    return routingStrategyModelCopy;
  }
}
