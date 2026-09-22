package dev.askov.vipet.mvc.controllers.networkeditor;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.controllers.networkeditor.states.NetworkEditorState;
import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.mvc.views.network.*;
import dev.askov.vipet.mvc.views.network.connection.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

public final class NetworkEditorController extends AbstractController<NetworkModel> {

  private final Network network = new Network();
  private final ObjectProperty<NetworkEditorState> stateProperty =
      new SimpleObjectProperty<>("stateProperty", null);
  private final Map<NetworkNode, NetworkNodeModel> networkNodesMap = new HashMap<>();
  private final Map<Connection, ConnectionModel> connectionMap = new HashMap<>();

  @FXML private ScrollPane scrollPane;
  @FXML private Pane pane;

  public NetworkEditorController(final NetworkModel networkModel) {
    super(networkModel);
  }

  @Override
  protected void initialize() {
    network
        .getStylesheets()
        .add(Objects.requireNonNull(VIPET.class.getResource("css/main.css")).toExternalForm());

    model
        .getNodeModels()
        .forEach(
            networkNodeModel -> {
              createNetworkNode(networkNodeModel);
              if (networkNodeModel
                  instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNodeModel) {
                nonTerminalNetworkNodeModel
                    .getConnectionModels()
                    .forEach(
                        connectionModel -> createConnection(networkNodeModel, connectionModel));
              }
              addConnectionsChangeListener(networkNodeModel);
            });

    model
        .nodesProperty()
        .addListener(
            (ListChangeListener<NetworkNodeModel>)
                change -> {
                  while (change.next()) {
                    if (change.wasAdded()) {
                      change
                          .getAddedSubList()
                          .forEach(
                              networkNodeModel -> {
                                createNetworkNode(networkNodeModel);
                                addConnectionsChangeListener(networkNodeModel);
                              });
                    }
                    if (change.wasRemoved()) {
                      change
                          .getRemoved()
                          .forEach(
                              networkNodeModel -> {
                                final var networkNode = getNetworkNode(networkNodeModel);

                                network.removeNetworkNode(networkNode);
                                networkNodesMap.remove(networkNode);
                              });
                    }
                  }
                });

    pane.getChildren().add(network);
  }

  private void addConnectionsChangeListener(final NetworkNodeModel networkNodeModel) {
    if (networkNodeModel instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
      nonTerminalNetworkNode
          .connectionsProperty()
          .addListener(
              (ListChangeListener<ConnectionModel>)
                  change -> {
                    while (change.next()) {
                      if (change.wasAdded()) {
                        change
                            .getAddedSubList()
                            .forEach(
                                connectionModel ->
                                    createConnection(networkNodeModel, connectionModel));
                      }
                      if (change.wasRemoved()) {
                        change
                            .getRemoved()
                            .forEach(
                                connectionModel ->
                                    removeConnection(getConnection(connectionModel)));
                      }
                    }
                  });
    }
  }

  private void createNetworkNode(final NetworkNodeModel networkNodeModel) {
    NetworkNode networkNode;

    if (networkNodeModel instanceof ServiceCenterModel serviceCenterModel) {
      final var serviceCenter = new ServiceCenter();

      bindServiceCenterTooltip(serviceCenterModel, serviceCenter);

      serviceCenter
          .queueVisualisationParameterProperty()
          .bind(serviceCenterModel.getQueueModel().visualizationParameterProperty());
      serviceCenter
          .jobVisualisationParameterProperty()
          .bind(serviceCenterModel.getQueueModel().jobVisualizationParameterProperty());

      serviceCenter.numberOfServersProperty().bind(serviceCenterModel.numberOfServersProperty());
      serviceCenterModel
          .numberOfServersProperty()
          .addListener(
              (numberOfServers, oldNumberOfServers, newNumberOfServers) -> {
                if (serviceCenter.getServerVisualisationParametersSize()
                        == newNumberOfServers.intValue()
                    && newNumberOfServers.intValue() > oldNumberOfServers.intValue()) {
                  for (var i = oldNumberOfServers.intValue();
                      i < newNumberOfServers.intValue();
                      i++) {
                    serviceCenter
                        .getServerVisualisationParameterProperty(i)
                        .bind(
                            serviceCenterModel.getServerModel(i).visualizationParameterProperty());
                  }
                }
              });
      for (var i = 0; i < serviceCenterModel.getNumberOfServers(); i++) {
        serviceCenter
            .getServerVisualisationParameterProperty(i)
            .bind(serviceCenterModel.getServerModel(i).visualizationParameterProperty());
      }
      serviceCenter
          .getCentralServiceCenterIcon()
          .visibleProperty()
          .bind(
              getModel()
                  .closedProperty()
                  .and(
                      getModel()
                          .centralServiceCenterModelProperty()
                          .isEqualTo(serviceCenterModel)));

      networkNode = serviceCenter;
    } else if (networkNodeModel instanceof SourceModel sourceModel) {
      networkNode = new Source();

      networkNode
          .getTooltip()
          .textProperty()
          .bind(
              Bindings.createStringBinding(
                  () -> {
                    var tooltipText = localizationManager.getString("networkEditor.source.tooltip");

                    if (sourceModel.getDescription().isEmpty()) {
                      tooltipText = tooltipText.substring(tooltipText.indexOf("\n") + 1);

                      return tooltipText.formatted(
                          sourceModel.getSelectedTimeDistribution().getDescription());
                    } else {
                      return tooltipText.formatted(
                          sourceModel.getDescription(),
                          sourceModel.getSelectedTimeDistribution().getDescription());
                    }
                  },
                  sourceModel.descriptionProperty(),
                  sourceModel.selectedTimeDistributionProperty(),
                  sourceModel.getSelectedTimeDistribution().descriptionProperty(),
                  localizationManager.localeProperty()));
    } else {
      networkNode = new Sink();

      networkNode
          .getTooltip()
          .textProperty()
          .bind(
              Bindings.createStringBinding(
                  () -> {
                    var tooltipText = localizationManager.getString("networkEditor.sink.tooltip");

                    if (networkNodeModel.getDescription().isEmpty()) {
                      return null;
                    } else {
                      return tooltipText.formatted(networkNodeModel.getDescription());
                    }
                  },
                  networkNodeModel.descriptionProperty(),
                  localizationManager.localeProperty()));
    }

    bindState(networkNode, networkNodeModel);
    networkNode.forwardOrientedProperty().bind(networkNodeModel.forwardOrientedProperty());
    networkNode.getNameLabel().textProperty().bind(networkNodeModel.nameProperty());
    networkNode.normalStateStrokeColorProperty().bind(networkNodeModel.strokeColorProperty());
    networkNode.normalStateFillColorProperty().bind(networkNodeModel.fillColorProperty());
    networkNode.translateXProperty().bindBidirectional(networkNodeModel.xProperty());
    networkNode.translateYProperty().bindBidirectional(networkNodeModel.yProperty());
    networkNode.validProperty().bind(networkNodeModel.validProperty());

    networkNodeModel.widthProperty().bind(networkNode.widthProperty());
    networkNodeModel
        .minYProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> networkNode.getBoundsInParent().getMinY(),
                networkNode.boundsInParentProperty()));
    networkNodeModel
        .maxYProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> networkNode.getBoundsInParent().getMaxY(),
                networkNode.boundsInParentProperty()));
    networkNodeModel.heightProperty().bind(networkNode.heightProperty());

    networkNodesMap.put(networkNode, networkNodeModel);

    network.addNetworkNode(networkNode);
  }

  private void bindServiceCenterTooltip(
      final ServiceCenterModel serviceCenterModel, final ServiceCenter serviceCenterGroup) {
    serviceCenterGroup.getTooltip().textProperty().unbind();
    serviceCenterGroup
        .getTooltip()
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  var tooltipText =
                      localizationManager.getString("networkEditor.serviceCenter.tooltip");

                  if (serviceCenterModel.getDescription().isEmpty()) {
                    tooltipText = tooltipText.substring(tooltipText.indexOf("\n") + 1);

                    return tooltipText.formatted(
                        serviceCenterModel.getQueueModel().isInfinite()
                            ? "∞"
                            : serviceCenterModel.getQueueModel().getCapacity(),
                        serviceCenterModel.getSelectedTimeDistribution().getDescription());
                  } else {
                    return tooltipText.formatted(
                        serviceCenterModel.getDescription(),
                        serviceCenterModel.getQueueModel().isInfinite()
                            ? "∞"
                            : serviceCenterModel.getQueueModel().getCapacity(),
                        serviceCenterModel.getSelectedTimeDistribution().getDescription());
                  }
                },
                serviceCenterModel.descriptionProperty(),
                serviceCenterModel.getQueueModel().infiniteCapacityProperty(),
                serviceCenterModel.getQueueModel().capacityProperty(),
                serviceCenterModel.selectedTimeDistributionProperty(),
                serviceCenterModel.getSelectedTimeDistribution().descriptionProperty(),
                localizationManager.localeProperty()));
  }

  private void createConnection(
      final NetworkNodeModel sourceNode, final ConnectionModel connectionModel) {
    final var destinationNode = connectionModel.getDestination();
    final var connection = new Connection(connectionModel);

    connection.typeProperty().bind(connectionModel.typeProperty());
    connection
        .getTooltip()
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    localizationManager.getString(
                        "networkEditor.connection.tooltip",
                        connectionModel.getSource().getName(),
                        connectionModel.getDestination().getName()),
                connectionModel.getSource().nameProperty(),
                connectionModel.getDestination().nameProperty(),
                localizationManager.localeProperty()));

    bindState(connection, connectionModel);

    connection
        .getStartPoint()
        .xProperty()
        .bindBidirectional(sourceNode.getOutputPortPosition().xProperty());
    connection
        .getStartPoint()
        .yProperty()
        .bindBidirectional(sourceNode.getOutputPortPosition().yProperty());
    connection
        .getEndPoint()
        .xProperty()
        .bindBidirectional(destinationNode.getInputPortPosition().xProperty());
    connection
        .getEndPoint()
        .yProperty()
        .bindBidirectional(destinationNode.getInputPortPosition().yProperty());

    connectionMap.put(connection, connectionModel);

    network.addConnection(connection);
  }

  public void removeConnection(final Connection connection) {
    connectionMap.remove(connection);
    network.removeConnection(connection);
  }

  public Network getNetwork() {
    return network;
  }

  public ObjectProperty<NetworkEditorState> stateProperty() {
    return stateProperty;
  }

  public NetworkEditorState getState() {
    return stateProperty.get();
  }

  public void setState(final NetworkEditorState state) {
    if (getState() != null) {
      getState().exit();
    }
    state.setNetworkEditorController(this);
    stateProperty.set(state);
    getState().enter();
  }

  public List<NetworkNode> getNetworkNodes() {
    return network.getChildren().filtered(node -> node instanceof NetworkNode).stream()
        .map(node -> (NetworkNode) node)
        .toList();
  }

  public List<Connection> getConnections() {
    return network.getChildren().filtered(node -> node instanceof Connection).stream()
        .map(node -> (Connection) node)
        .toList();
  }

  public NetworkElementModel getNetworkElementModel(final NetworkElement networkElement) {
    if (networkElement instanceof NetworkNode networkNode) {
      return getNetworkNodeModel(networkNode);
    } else if (networkElement instanceof Connection connection) {
      return getConnectionModel(connection);
    } else {
      throw new RuntimeException("NetworkElement could not be found");
    }
  }

  public List<NetworkElement> getNetworkElements() {
    return network.getChildren().filtered(node -> node instanceof NetworkElement).stream()
        .map(node -> (NetworkElement) node)
        .toList();
  }

  public NetworkNodeModel getNetworkNodeModel(final NetworkNode networkNode) {
    final var networkNodeModel = networkNodesMap.get(networkNode);
    if (networkNodeModel == null) {
      throw new RuntimeException(
          "NetworkNode corresponding to \"%s\" could not be found"
              .formatted(networkNode.getNameLabel().getText()));
    }
    return networkNodeModel;
  }

  public List<NetworkNodeModel> getNetworkNodeModels() {
    return networkNodesMap.values().stream().toList();
  }

  public NetworkNode getNetworkNode(final NetworkNodeModel networkNode) {
    return networkNodesMap.entrySet().stream()
        .filter(entry -> entry.getValue().equals(networkNode))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  public ConnectionModel getConnectionModel(final Connection connection) {
    return connectionMap.get(connection);
  }

  public Connection getConnection(final ConnectionModel connectionModel) {
    return connectionMap.entrySet().stream()
        .filter(entry -> entry.getValue().equals(connectionModel))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  public List<ConnectionModel> getConnectionModels() {
    return connectionMap.values().stream().toList();
  }

  public WritableImage getWritableImage() {
    return network.snapshot(null, null);
  }

  public ScrollPane getScrollPane() {
    return scrollPane;
  }

  private static void bindState(
      final NetworkElement networkElementGroup, final NetworkElementModel networkElementModel) {
    networkElementGroup
        .stateProperty()
        .bind(
            Bindings.createObjectBinding(
                () ->
                    switch (networkElementModel.getState()) {
                      case CREATING -> NetworkElement.State.CREATING;
                      case MOVING -> NetworkElement.State.MOVING;
                      case OVERLAPPING -> NetworkElement.State.OVERLAPPING;
                      case SELECTED -> NetworkElement.State.SELECTED;
                      case HIGHLIGHTED -> NetworkElement.State.HIGHLIGHTED;
                      case FADED -> NetworkElement.State.FADED;
                      case SOURCE -> NetworkElement.State.SOURCE;
                      case FADED_SOURCE -> NetworkElement.State.FADED_SOURCE;
                      case NORMAL -> NetworkElement.State.NORMAL;
                      case SIMULATING -> NetworkElement.State.SIMULATING;
                    },
                networkElementModel.stateProperty()));
  }

  public Pane getPane() {
    return pane;
  }
}
