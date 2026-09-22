package dev.askov.vipet.mvc.controllers.networkeditor.states;

import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.mvc.views.network.NetworkNode;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public final class AddingConnection extends NetworkEditorState {

  private NonTerminalNetworkNodeModel sourceNodeModel;
  private NetworkNodeModel destinationNodeModel;

  @Override
  public void enter() {
    super.enter();

    sourceNodeModel = null;
    destinationNodeModel = null;

    networkEditorController
        .getNetworkNodes()
        .forEach(
            networkNode -> {
              final var networkNodeModel = networkEditorController.getNetworkNodeModel(networkNode);

              if (networkNodeModel instanceof SinkModel) {
                networkNodeModel.setState(NetworkElementModel.State.FADED);
                networkNode.setCursor(Cursor.DEFAULT);
              } else {
                networkNodeModel.setState(NetworkElementModel.State.NORMAL);
                networkNode.setCursor(Cursor.HAND);
              }

              networkNode.setOnMousePressed(event -> onMousePressed(event, networkNode));
            });
    networkEditorController
        .getModel()
        .getNodeModels()
        .forEach(
            nodeModel -> {
              if (nodeModel instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNodeModel) {
                for (var connectionModel : nonTerminalNetworkNodeModel.getConnectionModels()) {
                  connectionModel.setState(NetworkElementModel.State.FADED);
                }
              }
            });
    networkEditorController
        .getPane()
        .setOnMousePressed(event -> networkEditorController.setState(new Idle()));
  }

  @Override
  public void exit() {
    super.exit();

    networkEditorController
        .getNetworkNodes()
        .forEach(
            networkNode -> {
              final var networkNodeModel = networkEditorController.getNetworkNodeModel(networkNode);
              networkNodeModel.setState(NetworkElementModel.State.NORMAL);
              networkNode.setOnMousePressed(null);
            });
    networkEditorController
        .getModel()
        .getNodeModels()
        .forEach(
            nodeModel -> {
              if (nodeModel instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNodeModel) {
                for (final var connectionModel :
                    nonTerminalNetworkNodeModel.getConnectionModels()) {
                  connectionModel.setState(NetworkElementModel.State.NORMAL);
                }
              }
            });
    networkEditorController.getPane().setOnMousePressed(null);
  }

  @Override
  protected String getName() {
    return "Adding Connection";
  }

  private void onMousePressed(final MouseEvent event, final NetworkNode networkNode) {
    if (event.getButton() == MouseButton.PRIMARY) {
      if (sourceNodeModel == null) {
        if (networkEditorController.getNetworkNodeModel(networkNode)
            instanceof NonTerminalNetworkNodeModel potentialSourceNodeModel) {
          sourceNodeModel = potentialSourceNodeModel;
          networkEditorController
              .getNetworkNodeModel(networkNode)
              .setState(NetworkElementModel.State.SOURCE);

          networkEditorController
              .getNetworkNodes()
              .forEach(
                  currentNetworkNode -> {
                    final var currentNetworkNodeModel =
                        networkEditorController.getNetworkNodeModel(currentNetworkNode);

                    if (currentNetworkNodeModel instanceof SourceModel
                        || potentialSourceNodeModel.isConnectedTo(currentNetworkNodeModel)) {
                      if (currentNetworkNodeModel != sourceNodeModel) {
                        currentNetworkNodeModel.setState(NetworkElementModel.State.FADED);
                      } else {
                        currentNetworkNodeModel.setState(NetworkElementModel.State.FADED_SOURCE);
                      }
                      currentNetworkNode.setCursor(Cursor.DEFAULT);
                    } else if (currentNetworkNodeModel instanceof SinkModel
                        && !(potentialSourceNodeModel instanceof SourceModel)) {
                      currentNetworkNodeModel.setState(NetworkElementModel.State.NORMAL);
                      currentNetworkNode.setCursor(Cursor.HAND);
                    }
                  });
        } else {
          networkEditorController.setState(new Idle());

          return;
        }
      } else {
        destinationNodeModel = networkEditorController.getNetworkNodeModel(networkNode);

        if (sourceNodeModel.getConnectionModels().stream()
                .anyMatch(connection -> connection.getDestination() == destinationNodeModel)
            || destinationNodeModel instanceof SourceModel
            || (destinationNodeModel instanceof SinkModel
                && sourceNodeModel instanceof SourceModel)) {
          networkEditorController.setState(new Idle());

          return;
        } else {
          sourceNodeModel.addConnectionModel(
              new ConnectionModel(sourceNodeModel, destinationNodeModel));
          networkEditorController.setState(this);
        }
      }
    } else {
      networkEditorController.setState(new Idle());
    }
    event.consume();
  }
}
