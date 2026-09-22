package dev.askov.vipet.mvc.models.network;

import dev.askov.vipet.mvc.models.distributions.ExponentialTimeDistributionModel;
import dev.askov.vipet.mvc.models.network.queue.FIFOQueueDisciplineModel;
import dev.askov.vipet.mvc.models.network.routing.ProbabilityRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RandomRoutingStrategyModel;
import java.io.File;
import java.util.*;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkModel implements NamedModel {

  public static final Color DEFAULT_COLOR = Color.LIGHTGRAY;
  public static final int INVALID_NODE_ID = 0;

  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkModel.class);
  private static final int DEFAULT_NUMBER_OF_JOBS = 1; // Relevant only for closed networks
  private static final double DEFAULT_SCALE = 1.0;
  private static final long FIRST_NODE_ID = 1;

  private final StringProperty nameProperty;
  private final ObjectProperty<File> fileProperty;
  private final ReadOnlyBooleanWrapper closedPropertyWrapper;
  private final ReadOnlyBooleanWrapper openPropertyWrapper;
  private final IntegerProperty numberOfJobsProperty;
  private final StringProperty descriptionProperty;
  private final DoubleProperty scaleProperty;
  private final ListProperty<NetworkNodeModel> nodeModelsProperty =
      new SimpleListProperty<>(this, "nodeModels", FXCollections.observableArrayList());
  private final ObjectProperty<ServiceCenterModel> centralServiceCenterModelProperty =
      new SimpleObjectProperty<>(
          this, "centralServiceCenterModel"); // Relevant only for closed networks
  private final ObjectProperty<NetworkElementModel> selectedNetworkElementModelProperty =
      new SimpleObjectProperty<>(this, "selectedNetworkElementModel");
  private final ReadOnlyBooleanWrapper validProperty =
      new ReadOnlyBooleanWrapper(this, "valid", false);
  private final ReadOnlyBooleanWrapper solvableProperty =
      new ReadOnlyBooleanWrapper(this, "solvable", false);
  private final BooleanProperty modifiedProperty =
      new SimpleBooleanProperty(this, "modified", false);
  private final ObjectProperty<RandomGenerator> randomGeneratorObjectProperty =
      new SimpleObjectProperty<>(this, "randomGenerator", null);

  private final ChangeListener<RealDistribution> distributionChangeListener =
      (distribution, oldDistribution, newDistribution) -> setModified(true);

  private long currentNumberOfJobs;
  private final List<JobModel> departedJobModels = new ArrayList<>();

  private long nextNodeId = FIRST_NODE_ID;

  public NetworkModel(final String name, final List<NetworkNodeModel> nodes) {
    nameProperty = new SimpleStringProperty(this, "name", name);
    fileProperty = new SimpleObjectProperty<>(this, "file", null);
    closedPropertyWrapper = new ReadOnlyBooleanWrapper(this, "closed", true);
    openPropertyWrapper = new ReadOnlyBooleanWrapper(this, "open", false);
    numberOfJobsProperty = new SimpleIntegerProperty(this, "numberOfJobs", DEFAULT_NUMBER_OF_JOBS);
    descriptionProperty = new SimpleStringProperty(this, "description", "");
    scaleProperty = new SimpleDoubleProperty(this, "scale", DEFAULT_SCALE);

    addListeners();

    if (nodes != null) {
      nodeModelsProperty.addAll(nodes);
      for (final var node : nodes) {
        if (node instanceof SourceModel || node instanceof SinkModel) {
          setClosed(false);
          break;
        }
      }
    }
  }

  public NetworkModel(final String name) {
    this(name, null);
  }

  public NetworkModel() {
    this("");
  }

  @SuppressWarnings("CopyConstructorMissesField")
  public NetworkModel(final NetworkModel networkModel) {
    nameProperty = networkModel.nameProperty;
    fileProperty = networkModel.fileProperty;
    closedPropertyWrapper = networkModel.closedPropertyWrapper;
    openPropertyWrapper = networkModel.openPropertyWrapper;
    numberOfJobsProperty =
        new SimpleIntegerProperty(this, "numberOfJobs", networkModel.getNumberOfJobs());
    descriptionProperty = networkModel.descriptionProperty;
    scaleProperty = networkModel.scaleProperty;

    addNumberOfJobsChangedListener();

    for (final var networkNode : networkModel.nodeModelsProperty) {
      NetworkNodeModel networkNodeCopy = null;

      if (networkNode instanceof SourceModel sourceModel) {
        networkNodeCopy = new SourceModel(sourceModel);
      } else if (networkNode instanceof SinkModel sinkModel) {
        networkNodeCopy = new SinkModel(sinkModel);
      } else if (networkNode instanceof ServiceCenterModel serviceCenterModel) {
        networkNodeCopy = new ServiceCenterModel(serviceCenterModel);
      }

      nodeModelsProperty.add(networkNodeCopy);
    }

    if (networkModel.getCentralServiceCenterModel() != null) {
      centralServiceCenterModelProperty.set(
          getServiceCenterModel(networkModel.getCentralServiceCenterModel().getName()));
    }

    for (final var networkNode : networkModel.nodeModelsProperty) {
      if (networkNode instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
        final var sourceCopy =
            (NonTerminalNetworkNodeModel) getNodeModel(nonTerminalNetworkNode.getName());

        for (final var connectionModel : nonTerminalNetworkNode.getConnectionModels()) {
          final var destination = connectionModel.getDestination();
          final var destinationCopy = getNodeModel(destination.getName());
          final var connectionModelCopy = new ConnectionModel(sourceCopy, destinationCopy);

          sourceCopy.addConnection(connectionModelCopy);
        }

        final var routingStrategyModel = nonTerminalNetworkNode.getSelectedRoutingStrategy();
        if (routingStrategyModel
            instanceof ProbabilityRoutingStrategyModel probabilityRoutingStrategy) {
          final var probabilityRoutingStrategyCopy =
              new ProbabilityRoutingStrategyModel(sourceCopy);

          for (final var entry : probabilityRoutingStrategy.getProbabilityMap().entrySet()) {
            probabilityRoutingStrategyCopy.setProbability(
                getNodeModel(entry.getKey().getName()), entry.getValue().get());
          }

          sourceCopy.setRoutingStrategies(List.of(probabilityRoutingStrategyCopy));
          sourceCopy.setSelectedRoutingStrategy(probabilityRoutingStrategyCopy);
        }
      }
    }

    addRandomGeneratorListener();
  }

  private void addListeners() {
    openPropertyWrapper.bind(closedPropertyWrapper.not());

    addNumberOfJobsChangedListener();

    nodeModelsProperty.addListener(
        (ListChangeListener<NetworkNodeModel>)
            nodeModelsChange -> {
              while (nodeModelsChange.next()) {
                if (nodeModelsChange.wasAdded()) {
                  setModified(true);

                  for (final var networkNode : nodeModelsChange.getAddedSubList()) {
                    if (networkNode instanceof SourceModel || networkNode instanceof SinkModel) {
                      setClosed(false);
                    }

                    networkNode
                        .validProperty()
                        .addListener(
                            (valid, wasValid, isValid) -> {
                              if (isValid) {
                                for (final var node : nodeModelsProperty) {
                                  if (node != networkNode && node.isNotValid()) {
                                    validProperty.set(false);
                                    return;
                                  }
                                }
                                validProperty.set(true);
                              } else {
                                validProperty.set(false);
                              }
                            });

                    if (networkNode instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
                      nonTerminalNetworkNode
                          .connectionsProperty()
                          .addListener(
                              (ListChangeListener<ConnectionModel>)
                                  connectionChange -> {
                                    while (connectionChange.next()) {
                                      if (connectionChange.wasRemoved()) {
                                        for (final var connectionModel :
                                            connectionChange.getRemoved()) {
                                          if (connectionModel.equals(
                                              getSelectedNetworkElementModel())) {
                                            setSelectedNetworkElementModel(null);
                                            break;
                                          }
                                        }
                                      }
                                    }

                                    checkNodeReachability();
                                    setModified(true);
                                  });
                      if (nodeModelsProperty.size() == 1
                          && nonTerminalNetworkNode
                              instanceof ServiceCenterModel serviceCenterModel) {
                        serviceCenterModel.setInitialNumberOfJobs(getNumberOfJobs());
                      }
                      nonTerminalNetworkNode
                          .selectedRoutingStrategyProperty()
                          .addListener(
                              (selectedRoutingStrategy,
                                  oldSelectedRoutingStrategy,
                                  newSelectedRoutingStrategy) -> {
                                if (newSelectedRoutingStrategy
                                        instanceof ProbabilityRoutingStrategyModel
                                    || newSelectedRoutingStrategy
                                        instanceof RandomRoutingStrategyModel) {
                                  updateSolvable();

                                  if (newSelectedRoutingStrategy
                                      instanceof
                                      ProbabilityRoutingStrategyModel probabilityRoutingStrategy) {
                                    probabilityRoutingStrategy
                                        .probabilityMap()
                                        .addListener(
                                            (probabilityMap,
                                                oldProbabilityMap,
                                                newProbabilityMap) -> setModified(true));
                                  }
                                } else {
                                  setSolvable(false);
                                }

                                setModified(true);
                              });
                      nonTerminalNetworkNode.getRoutingStrategies().stream()
                          .filter(
                              routingStrategy ->
                                  routingStrategy instanceof ProbabilityRoutingStrategyModel)
                          .findFirst()
                          .ifPresent(
                              routingStrategy -> {
                                final var probabilityRoutingStrategy =
                                    (ProbabilityRoutingStrategyModel) routingStrategy;

                                probabilityRoutingStrategy
                                    .probabilityMap()
                                    .addListener(
                                        (MapChangeListener<
                                                ? super NetworkNodeModel, ? super DoubleProperty>)
                                            probabilityMapChange -> {
                                              if (probabilityMapChange.wasAdded()) {
                                                final var probability =
                                                    probabilityMapChange.getValueAdded();

                                                probability.addListener(
                                                    (probabilityValue,
                                                        oldProbabilityValue,
                                                        newProbabilityValue) -> setModified(true));
                                              }
                                            });
                              });
                      nonTerminalNetworkNode
                          .selectedTimeDistributionProperty()
                          .addListener(
                              (timeDistribution, oldTimeDistribution, newTimeDistribution) -> {
                                if (newTimeDistribution
                                    instanceof ExponentialTimeDistributionModel) {
                                  updateSolvable();
                                } else {
                                  setSolvable(false);
                                }

                                newTimeDistribution
                                    .distributionProperty()
                                    .addListener(distributionChangeListener);
                                setModified(true);

                                oldTimeDistribution
                                    .distributionProperty()
                                    .removeListener(distributionChangeListener);
                              });
                      if (nonTerminalNetworkNode instanceof ServiceCenterModel serviceCenterModel) {
                        serviceCenterModel
                            .numberOfServersProperty()
                            .addListener(
                                (numberOfServers, oldNumberOfServers, newNumberOfServers) -> {
                                  updateSolvable();

                                  if (newNumberOfServers.intValue()
                                      < oldNumberOfServers.intValue()) {
                                    redistributeInitialJobs(serviceCenterModel);
                                  }

                                  setModified(true);
                                });
                        serviceCenterModel
                            .getQueueModel()
                            .infiniteCapacityProperty()
                            .addListener(
                                (infiniteCapacity, wasInfiniteCapacity, isInfiniteCapacity) -> {
                                  updateSolvable();

                                  if (!isInfiniteCapacity) {
                                    redistributeInitialJobs(serviceCenterModel);
                                  }

                                  setModified(true);
                                });
                        serviceCenterModel
                            .getQueueModel()
                            .capacityProperty()
                            .addListener(
                                (queueCapacity, oldQueueCapacity, newQueueCapacity) -> {
                                  if (newQueueCapacity.intValue() < oldQueueCapacity.intValue()) {
                                    redistributeInitialJobs(serviceCenterModel);
                                  }

                                  updateSolvable();
                                  setModified(true);
                                });
                        serviceCenterModel
                            .getQueueModel()
                            .selectedQueueDisciplineProperty()
                            .addListener(
                                (selectedQueueDiscipline,
                                    oldSelectedQueueDiscipline,
                                    newSelectedQueueDiscipline) -> {
                                  if (newSelectedQueueDiscipline
                                      instanceof FIFOQueueDisciplineModel) {
                                    updateSolvable();
                                  } else {
                                    setSolvable(false);
                                  }

                                  setModified(true);
                                });
                      }
                    }
                  }
                }

                if (nodeModelsChange.wasRemoved()) {
                  var isClosed = true;
                  for (final var networkNode : nodeModelsChange.getList()) {
                    if (networkNode instanceof SourceModel || networkNode instanceof SinkModel) {
                      isClosed = false;
                      break;
                    }
                  }
                  setClosed(isClosed);

                  validProperty.set(!nodeModelsProperty.isEmpty());
                  for (final var networkNode : nodeModelsChange.getList()) {
                    if (networkNode.isNotValid()) {
                      validProperty.set(false);
                      break;
                    }
                  }

                  for (final var networkNode : nodeModelsChange.getRemoved()) {
                    if (networkNode.equals(getSelectedNetworkElementModel())) {
                      setSelectedNetworkElementModel(null);
                      break;
                    }
                  }

                  setModified(true);
                }

                checkNodeReachability();
              }
            });

    centralServiceCenterModelProperty.addListener(
        (centralServiceCenterModel, oldCentralServiceCenterModel, newCentralServiceCenterModel) ->
            setModified(true));

    validProperty.addListener((valid, wasValid, isValid) -> updateSolvable());
    closedPropertyWrapper.addListener((closed, wasClosed, isClosed) -> updateSolvable());

    modifiedProperty.addListener(
        (modified, wasModified, isModified) -> {
          if (isModified) {
            LOGGER.debug("Network has been modified");

            for (final var networkNodeModel : nodeModelsProperty) {
              if (networkNodeModel instanceof ServiceCenterModel serviceCenterModel) {
                serviceCenterModel.getQueueModel().setJobVisualizationParameter(0);
                serviceCenterModel.getQueueModel().setVisualizationParameter(0.0);
                serviceCenterModel
                    .getServerModels()
                    .forEach(serverModel -> serverModel.setVisualizationParameter(0.0));
              }
            }
          }
        });

    addRandomGeneratorListener();
  }

  public StringProperty nameProperty() {
    return nameProperty;
  }

  public ObjectProperty<File> fileProperty() {
    return fileProperty;
  }

  public File getFile() {
    return fileProperty.get();
  }

  public void setFile(final File file) {
    fileProperty.set(file);
  }

  public ReadOnlyBooleanProperty closedProperty() {
    return closedPropertyWrapper.getReadOnlyProperty();
  }

  public boolean isClosed() {
    return closedPropertyWrapper.get();
  }

  private void setClosed(final boolean closed) {
    closedPropertyWrapper.set(closed);
  }

  public ReadOnlyBooleanProperty openProperty() {
    return openPropertyWrapper.getReadOnlyProperty();
  }

  public boolean isOpen() {
    return openPropertyWrapper.get();
  }

  public IntegerProperty numberOfJobsProperty() {
    return numberOfJobsProperty;
  }

  public int getNumberOfJobs() {
    return numberOfJobsProperty.get();
  }

  public void setNumberOfJobs(final int numberOfJobs) {
    numberOfJobsProperty.set(numberOfJobs);
  }

  public StringProperty descriptionProperty() {
    return descriptionProperty;
  }

  public String getDescription() {
    return descriptionProperty.get();
  }

  public void setDescription(final String description) {
    descriptionProperty.set(description);
  }

  public ListProperty<NetworkNodeModel> nodesProperty() {
    return nodeModelsProperty;
  }

  public List<NetworkNodeModel> getNodeModels() {
    return nodeModelsProperty.get();
  }

  public ObjectProperty<ServiceCenterModel> centralServiceCenterModelProperty() {
    return centralServiceCenterModelProperty;
  }

  public ServiceCenterModel getCentralServiceCenterModel() {
    return centralServiceCenterModelProperty.get();
  }

  public void setCentralServiceCenterModel(final ServiceCenterModel centralServiceCenterModel) {
    centralServiceCenterModelProperty.set(centralServiceCenterModel);
  }

  public ObjectProperty<NetworkElementModel> selectedNetworkElementModelProperty() {
    return selectedNetworkElementModelProperty;
  }

  public NetworkElementModel getSelectedNetworkElementModel() {
    return selectedNetworkElementModelProperty.get();
  }

  public void setSelectedNetworkElementModel(final NetworkElementModel networkElementModel) {
    final var selectedNetworkElementModel = selectedNetworkElementModelProperty.get();

    if (selectedNetworkElementModel != null) {
      selectedNetworkElementModel.setState(NetworkElementModel.State.NORMAL);
    }
    selectedNetworkElementModelProperty.set(networkElementModel);
    if (networkElementModel != null) {
      networkElementModel.setState(NetworkElementModel.State.SELECTED);
    }
  }

  public void addNodeModel(final NetworkNodeModel networkNodeModel) {
    nodeModelsProperty.add(networkNodeModel);

    if (getCentralServiceCenterModel() == null) {
      if (networkNodeModel instanceof ServiceCenterModel serviceCenterModel) {
        centralServiceCenterModelProperty.unbind();
        setCentralServiceCenterModel(serviceCenterModel);
      }
    }
  }

  public void removeNodeModel(final NetworkNodeModel networkNodeModel) {
    nodeModelsProperty.remove(networkNodeModel);

    if (getCentralServiceCenterModel() == networkNodeModel) {
      centralServiceCenterModelProperty.unbind();
      if (!getServiceCenterModels().isEmpty()) {
        setCentralServiceCenterModel(getServiceCenterModels().getFirst());
      } else {
        setCentralServiceCenterModel(null);
      }
    }
  }

  public void removeNetworkElementModel(final NetworkElementModel networkElementModel) {
    if (networkElementModel instanceof NetworkNodeModel networkNode) {
      nodeModelsProperty.forEach(
          node -> {
            if (node instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
              nonTerminalNetworkNode
                  .getConnectionModels()
                  .removeIf(connection -> connection.getDestination() == networkElementModel);
            }
          });
      if (networkNode instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
        nonTerminalNetworkNode.getConnectionModels().clear();
      }

      removeNodeModel(networkNode);
    } else if (networkElementModel instanceof ConnectionModel connectionModel) {
      final var nonTerminalNetworkNodeModel = connectionModel.getSource();

      nonTerminalNetworkNodeModel.removeConnection(connectionModel);

      final var destinationNetworkNodeModel = connectionModel.getDestination();
      if (destinationNetworkNodeModel instanceof ServiceCenterModel serviceCenterModel) {
        var hasIncomingConnections = false;

        for (final var currentNonTerminalNetworkNodeModel : getNonTerminalNetworkNodeModels()) {
          if (currentNonTerminalNetworkNodeModel != destinationNetworkNodeModel
              && currentNonTerminalNetworkNodeModel.getConnectionModels().stream()
                  .anyMatch(c -> c.getDestination() == serviceCenterModel)) {
            hasIncomingConnections = true;
            break;
          }
        }

        serviceCenterModel.setIncomingConnections(hasIncomingConnections);
      } else if (destinationNetworkNodeModel instanceof SinkModel sinkModel) {
        var hasIncomingConnections = false;

        for (final var currentNonTerminalNetworkNodeModel : getNonTerminalNetworkNodeModels()) {
          if (currentNonTerminalNetworkNodeModel.getConnectionModels().stream()
              .anyMatch(c -> c.getDestination() == sinkModel)) {
            hasIncomingConnections = true;
            break;
          }
        }

        sinkModel.setIncomingConnections(hasIncomingConnections);
      }

      if (nonTerminalNetworkNodeModel.getSelectedRoutingStrategy()
          instanceof ProbabilityRoutingStrategyModel probabilityRoutingStrategy) {
        final var totalProbability =
            nonTerminalNetworkNodeModel.getConnectionModels().stream()
                .mapToDouble(
                    currentConnectionModel ->
                        probabilityRoutingStrategy.getProbability(
                            currentConnectionModel.getDestination()))
                .sum();

        nonTerminalNetworkNodeModel
            .getConnectionModels()
            .forEach(
                currentConnectionModel ->
                    probabilityRoutingStrategy.setProbability(
                        currentConnectionModel.getDestination(),
                        probabilityRoutingStrategy.getProbability(
                                currentConnectionModel.getDestination())
                            / totalProbability));
      }
    }
  }

  public NetworkNodeModel getNodeModel(final String name) {
    for (var node : nodeModelsProperty) {
      if (node.getName().equals(name)) {
        return node;
      }
    }
    return null;
  }

  public ServiceCenterModel getServiceCenterModel(final String name) {
    for (final var node : nodeModelsProperty) {
      if (node instanceof ServiceCenterModel serviceCenterModel
          && serviceCenterModel.getName().equals(name)) {
        return serviceCenterModel;
      }
    }
    return null;
  }

  public ServiceCenterModel getServiceCenterModel(final int id) {
    for (final var node : nodeModelsProperty) {
      if (node instanceof ServiceCenterModel serviceCenterModel
          && serviceCenterModel.getId() == id) {
        return serviceCenterModel;
      }
    }
    return null;
  }

  public List<SourceModel> getSourceModels() {
    final List<SourceModel> result = new ArrayList<>();

    for (final var node : nodeModelsProperty) {
      if (node instanceof SourceModel sourceModel) {
        result.add(sourceModel);
      }
    }

    return result;
  }

  public ObservableList<ServiceCenterModel> getServiceCenterModels() {
    final List<ServiceCenterModel> result = new ArrayList<>();

    for (final var node : nodeModelsProperty) {
      if (node instanceof ServiceCenterModel serviceCenterModel) {
        result.add(serviceCenterModel);
      }
    }

    return FXCollections.observableArrayList(result);
  }

  public ObservableList<NonTerminalNetworkNodeModel> getNonTerminalNetworkNodeModels() {
    final List<NonTerminalNetworkNodeModel> result = new ArrayList<>();

    for (final var node : nodeModelsProperty) {
      if (node instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNodeModel) {
        result.add(nonTerminalNetworkNodeModel);
      }
    }

    return FXCollections.observableArrayList(result);
  }

  public ReadOnlyBooleanProperty validProperty() {
    return validProperty.getReadOnlyProperty();
  }

  public boolean isValid() {
    return validProperty.get();
  }

  public ReadOnlyBooleanProperty solvableProperty() {
    return solvableProperty.getReadOnlyProperty();
  }

  public boolean isSolvable() {
    return solvableProperty.get();
  }

  private void setSolvable(final boolean solvable) {
    solvableProperty.set(solvable);
  }

  public BooleanProperty modifiedProperty() {
    return modifiedProperty;
  }

  public void setModified(final boolean modified) {
    modifiedProperty.set(modified);
  }

  public long getCurrentNumberOfJobs() {
    return currentNumberOfJobs;
  }

  public void incrementCurrentNumberOfJobs() {
    currentNumberOfJobs++;
  }

  public void incrementCurrentNumberOfJobs(final int numberOfJobs) {
    currentNumberOfJobs += numberOfJobs;
  }

  public int getNumberOfDepartedJobs() {
    return departedJobModels.size();
  }

  public JobModel getLastDepartedJobModel() {
    return departedJobModels.getLast();
  }

  public void addDepartedJobModel(final JobModel jobModel, final double time) {
    jobModel.setNetworkExitTime(time);
    currentNumberOfJobs--;
    departedJobModels.add(jobModel);
  }

  public boolean isEmpty() {
    return nodeModelsProperty.isEmpty();
  }

  @Override
  public String getName() {
    return nameProperty.get();
  }

  @Override
  public void setName(String name) {
    nameProperty.set(name);
  }

  @Override
  public String toString() {
    return getName();
  }

  public long peekNextNodeId() {
    return nextNodeId;
  }

  public long getNextNodeId() {
    return nextNodeId++;
  }

  public void setNextNodeId(final long nextNodeId) {
    this.nextNodeId = nextNodeId;
  }

  public RandomGenerator getRandomGenerator() {
    return randomGeneratorObjectProperty.get();
  }

  public void setRandomGenerator(final RandomGenerator randomGenerator) {
    randomGeneratorObjectProperty.set(randomGenerator);
  }

  private void addRandomGeneratorListener() {
    randomGeneratorObjectProperty.addListener(
        (observableRng, oldRng, newRng) ->
            getNonTerminalNetworkNodeModels()
                .forEach(
                    nonTerminalNetworkNodeModel ->
                        nonTerminalNetworkNodeModel.setRandomGenerator(newRng)));
  }

  private void addNumberOfJobsChangedListener() {
    numberOfJobsProperty.addListener(
        (numberOfJobs, oldNumberOfJobs, newNumberOfJobs) -> {
          var totalNumberOfJobsDelta =
              Math.abs(newNumberOfJobs.intValue() - oldNumberOfJobs.intValue());
          final var shouldAddJobs = newNumberOfJobs.intValue() > oldNumberOfJobs.intValue();

          for (final var currentServiceCenterModel : getServiceCenterModels()) {
            final var initialJobs = currentServiceCenterModel.getInitialNumberOfJobs();
            final var currentCapacity = currentServiceCenterModel.getCapacity();

            if (currentCapacity > initialJobs) {
              final var numberOfJobsDelta =
                  shouldAddJobs
                      ? Math.min(totalNumberOfJobsDelta, currentCapacity - initialJobs)
                      : Math.min(totalNumberOfJobsDelta, initialJobs);

              if (shouldAddJobs) {
                currentServiceCenterModel.setInitialNumberOfJobs(initialJobs + numberOfJobsDelta);
              } else {
                currentServiceCenterModel.setInitialNumberOfJobs(initialJobs - numberOfJobsDelta);
              }
              totalNumberOfJobsDelta -= numberOfJobsDelta;

              if (totalNumberOfJobsDelta <= 0) {
                break;
              }
            }
          }

          updateSolvable();
          setModified(true);
        });
  }

  private void updateSolvable() {
    if (isValid()) {
      setSolvable(true);

      for (final var networkNode : nodeModelsProperty) {
        if (networkNode instanceof ServiceCenterModel serviceCenterModel) {
          if (!(serviceCenterModel.getSelectedTimeDistribution()
              instanceof ExponentialTimeDistributionModel)) {
            setSolvable(false);
            LOGGER.debug(
                "Network became unsolvable. Reason: time distribution of service center \"{}\"",
                serviceCenterModel.getName());

            return;
          } else if (!(serviceCenterModel.getSelectedRoutingStrategy()
                  instanceof ProbabilityRoutingStrategyModel)
              && !(serviceCenterModel.getSelectedRoutingStrategy()
                  instanceof RandomRoutingStrategyModel)) {
            setSolvable(false);
            LOGGER.debug(
                "Network became unsolvable. Reason: routing strategy of service center \"{}\"",
                serviceCenterModel.getName());

            return;
          } else if (!serviceCenterModel.getQueueModel().isInfinite()) {
            if (isOpen()) {
              setSolvable(false);
            } else if (isSolvable()) {
              setSolvable(serviceCenterModel.getCapacity() >= getNumberOfJobs());
            }

            if (!isSolvable()) {
              LOGGER.debug(
                  "Network became unsolvable. Reason: capacity of service center \"{}\"",
                  serviceCenterModel.getName());

              return;
            }
          } else if (!(serviceCenterModel.getQueueModel().getSelectedQueueDiscipline()
              instanceof FIFOQueueDisciplineModel)) {
            setSolvable(false);
            LOGGER.debug(
                "Network became unsolvable. Reason: queue discipline of service center \"{}\"",
                serviceCenterModel.getName());

            return;
          }
        } else if (networkNode instanceof SourceModel sourceModel) {
          if (!(sourceModel.getSelectedTimeDistribution()
              instanceof ExponentialTimeDistributionModel)) {
            setSolvable(false);
            LOGGER.debug(
                "Network became unsolvable. Reason: time distribution of source \"{}\"",
                sourceModel.getName());

            return;
          }
        }
      }
    } else {
      setSolvable(false);
    }
  }

  private void checkNodeReachability() {
    // Initialize lists for reachable and unreachable nodes
    final List<NetworkNodeModel> reachableNodeModels = new ArrayList<>();
    final List<NetworkNodeModel> unreachableNodeModels = new ArrayList<>(nodeModelsProperty);

    // Determine initial set of reachable nodes based on network type
    if (isClosed()) {
      var centralServiceCenterModel = getCentralServiceCenterModel();
      // It may happen that centralServiceCenterModel contains a reference to a node that has been
      // removed from the network
      try {
        if (centralServiceCenterModel != null
            && !nodeModelsProperty.contains(centralServiceCenterModel)) {
          centralServiceCenterModel = getServiceCenterModels().getFirst();
        }
      } catch (final NoSuchElementException exception) {
        // If there are no service centers, centralServiceCenterModel will be null
        centralServiceCenterModel = null;
      }

      if (centralServiceCenterModel != null) {
        reachableNodeModels.add(centralServiceCenterModel);
        unreachableNodeModels.remove(centralServiceCenterModel);
        centralServiceCenterModel.setReachable(true);
      }
    } else {
      final var sourceModels = getSourceModels();
      if (sourceModels != null) {
        reachableNodeModels.addAll(sourceModels);
        unreachableNodeModels.removeAll(sourceModels);
      }
    }

    // Traverse and mark reachable nodes
    while (!reachableNodeModels.isEmpty()) {
      final var networkNodeModel = reachableNodeModels.removeLast();

      // Process only NonTerminalNetworkNodeModel nodes
      if (networkNodeModel instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
        final var connections = nonTerminalNetworkNode.getConnectionModels();
        if (connections != null) {
          for (final var connection : connections) {
            final var destination = connection.getDestination();
            if (destination != null && unreachableNodeModels.contains(destination)) {
              reachableNodeModels.add(destination);
              unreachableNodeModels.remove(destination);

              if (destination instanceof ServiceCenterModel serviceCenterModel) {
                serviceCenterModel.setReachable(true);
              } else if (destination instanceof SinkModel sinkModel) {
                sinkModel.setReachable(true);
              }
            }
          }
        }
      }
    }

    // Mark remaining unreachable nodes
    for (var networkNodeModel : unreachableNodeModels) {
      if (networkNodeModel instanceof ServiceCenterModel serviceCenterModel) {
        serviceCenterModel.setReachable(false);
      } else if (networkNodeModel instanceof SinkModel sinkModel) {
        sinkModel.setReachable(false);
      }
    }
  }

  private void redistributeInitialJobs(final ServiceCenterModel serviceCenterModel) {
    final var initialNumberOfJobs = serviceCenterModel.getInitialNumberOfJobs();
    final var capacity = serviceCenterModel.getCapacity();

    if (initialNumberOfJobs > capacity) {
      serviceCenterModel.setInitialNumberOfJobs(capacity);

      var jobsToDistribute = initialNumberOfJobs - capacity;
      final var atLeastOneInfiniteCapacityServiceCenter =
          getServiceCenterModels().stream().anyMatch(sc -> sc.getQueueModel().isInfinite());

      if (atLeastOneInfiniteCapacityServiceCenter) {
        final var infiniteCapacityServiceCenterModel =
            getServiceCenterModels().stream()
                .filter(sc -> sc.getQueueModel().isInfinite())
                .findFirst()
                .orElse(null);

        if (infiniteCapacityServiceCenterModel != null) {
          infiniteCapacityServiceCenterModel.setInitialNumberOfJobs(
              infiniteCapacityServiceCenterModel.getInitialNumberOfJobs() + jobsToDistribute);
        } else {
          LOGGER.error(
              "No service center with infinite queue capacity found to redistribute jobs.");
        }
      } else {
        final var networkCapacity =
            getServiceCenterModels().stream().mapToInt(ServiceCenterModel::getCapacity).sum();
        final var totalNumberOfJobs =
            getServiceCenterModels().stream()
                .mapToInt(ServiceCenterModel::getInitialNumberOfJobs)
                .sum();

        if (totalNumberOfJobs + jobsToDistribute > networkCapacity) {
          LOGGER.warn(
              "Network {} has {} jobs, but the total capacity of service centers is only {}. Number of jobs will be reduced by {}.",
              getName(),
              totalNumberOfJobs + jobsToDistribute,
              networkCapacity,
              totalNumberOfJobs + jobsToDistribute - networkCapacity);

          setNumberOfJobs(networkCapacity);
        } else {
          for (final var currentServiceCenterModel : getServiceCenterModels()) {
            final var initialJobs = currentServiceCenterModel.getInitialNumberOfJobs();
            final var currentCapacity = currentServiceCenterModel.getCapacity();

            if (currentCapacity > initialJobs) {
              final var numberOfJobsDelta =
                  Math.min(jobsToDistribute, currentCapacity - initialJobs);

              currentServiceCenterModel.setInitialNumberOfJobs(initialJobs + numberOfJobsDelta);
              jobsToDistribute -= numberOfJobsDelta;

              if (jobsToDistribute <= 0) {
                break;
              }
            }
          }
        }
      }
    }
  }
}
