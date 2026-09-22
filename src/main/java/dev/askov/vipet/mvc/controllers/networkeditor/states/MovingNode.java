package dev.askov.vipet.mvc.controllers.networkeditor.states;

import static dev.askov.vipet.mvc.views.network.connection.MultiSegmentPathWithArcs.NODE_TO_CONNECTION_DISTANCE;

import dev.askov.vipet.mvc.models.network.ConnectionModel;
import dev.askov.vipet.mvc.models.network.NetworkElementModel;
import dev.askov.vipet.mvc.models.network.NetworkNodeModel;
import dev.askov.vipet.mvc.models.network.NonTerminalNetworkNodeModel;
import dev.askov.vipet.mvc.views.network.NetworkNode;
import dev.askov.vipet.mvc.views.network.connection.Connection;
import java.util.ArrayList;
import java.util.List;

public final class MovingNode extends NetworkEditorState {

  public static final double EXTRA_PADDING = 5.0; // In pixels

  private final NetworkNode networkNode;
  private final double dragStartX;
  private final double dragStartY;
  private final List<ConnectionModel> connectionModels = new ArrayList<>();
  private final List<NetworkElementModel> networkElementModels = new ArrayList<>();

  private double lastValidPositionX;
  private double lastValidPositionY;

  NetworkNodeModel networkNodeModel;

  public MovingNode(
      final NetworkNode networkNode, final double dragStartX, final double dragStartY) {
    this.networkNode = networkNode;
    this.dragStartX = dragStartX;
    this.dragStartY = dragStartY;
    lastValidPositionX = networkNode.getTranslateX();
    lastValidPositionY = networkNode.getTranslateY();
  }

  @Override
  public void enter() {
    super.enter();

    networkNodeModel = networkEditorController.getNetworkNodeModel(networkNode);
    if (networkNodeModel instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
      connectionModels.addAll(nonTerminalNetworkNode.getConnectionModels());
    }
    networkEditorController
        .getModel()
        .getNodeModels()
        .forEach(
            currentNode -> {
              if (currentNode instanceof NonTerminalNetworkNodeModel nonTerminalNode
                  && currentNode != networkNodeModel) {
                nonTerminalNode
                    .getConnectionModels()
                    .forEach(
                        connection -> {
                          if (connection.getDestination() == networkNodeModel) {
                            connectionModels.add(connection);
                            if (!networkElementModels.contains(nonTerminalNode)) {
                              networkElementModels.add(nonTerminalNode);
                            }
                          }
                        });
              }
              if (networkNodeModel instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode
                  && !networkElementModels.contains(currentNode)) {
                for (var connectionModel : nonTerminalNetworkNode.getConnectionModels()) {
                  if (connectionModel.getDestination() == currentNode) {
                    networkElementModels.add(currentNode);
                    break;
                  }
                }
              }
            });
    networkEditorController
        .getConnectionModels()
        .forEach(
            connectionModel -> {
              if (!connectionModels.contains(connectionModel)) {
                connectionModel.setState(NetworkElementModel.State.FADED);
              }
            });
    networkEditorController
        .getNetworkNodeModels()
        .forEach(
            networkNodeModel -> {
              if (!networkElementModels.contains(networkNodeModel)) {
                networkNodeModel.setState(NetworkNodeModel.State.FADED);
              }
            });

    networkNodeModel.setState(NetworkElementModel.State.MOVING);
    networkNode.toFront();

    networkEditorController
        .getNetwork()
        .setOnMouseDragged(
            mouseDraggedEvent -> {
              final var newX = mouseDraggedEvent.getX() - dragStartX;
              final var newY = mouseDraggedEvent.getY() - dragStartY;

              networkNodeModel.setX(
                  Math.max(
                      newX,
                      NODE_TO_CONNECTION_DISTANCE
                          + Connection.TRAVELLING_JOB_CIRCLE_RADIUS
                          + EXTRA_PADDING));
              networkNodeModel.setY(
                  Math.max(
                      newY,
                      NODE_TO_CONNECTION_DISTANCE
                          + Connection.TRAVELLING_JOB_CIRCLE_RADIUS
                          + EXTRA_PADDING));

              if (networkEditorController.getNetwork().hasNodeThatOverlapsWith(networkNode)) {
                networkNodeModel.setState(NetworkNodeModel.State.OVERLAPPING);
                connectionModels.forEach(
                    connection -> connection.setState(NetworkElementModel.State.OVERLAPPING));
                networkElementModels.forEach(
                    networkElementModel -> {
                      if (networkElementModel != networkNodeModel) {
                        networkElementModel.setState(NetworkElementModel.State.FADED);
                      }
                    });
              } else {
                lastValidPositionX = networkNodeModel.getX();
                lastValidPositionY = networkNodeModel.getY();

                networkNodeModel.setState(NetworkElementModel.State.MOVING);
                networkNode.toFront();
                connectionModels.forEach(
                    connection -> connection.setState(NetworkElementModel.State.MOVING));
                networkElementModels.forEach(
                    networkElementModel ->
                        networkElementModel.setState(NetworkElementModel.State.NORMAL));
              }

              mouseDraggedEvent.consume();
            });

    networkEditorController
        .getNetwork()
        .setOnMouseReleased(event -> networkEditorController.setState(new Idle()));
  }

  @Override
  public void exit() {
    super.exit();

    networkNodeModel.setState(NetworkElementModel.State.NORMAL);

    networkNode.setTranslateX(lastValidPositionX);
    networkNode.setTranslateY(lastValidPositionY);

    connectionModels.forEach(connection -> connection.setState(NetworkElementModel.State.NORMAL));
    networkEditorController
        .getConnectionModels()
        .forEach(
            connectionModel -> {
              if (!connectionModels.contains(connectionModel)) {
                connectionModel.setState(NetworkElementModel.State.NORMAL);
              }
            });
    networkEditorController
        .getNetworkNodeModels()
        .forEach(networkNodeModel -> networkNodeModel.setState(NetworkNodeModel.State.NORMAL));

    networkEditorController.getNetwork().setOnMouseDragged(null);
    networkEditorController.getNetwork().setOnMouseReleased(null);
  }

  @Override
  protected String getName() {
    return "Moving Node";
  }
}
