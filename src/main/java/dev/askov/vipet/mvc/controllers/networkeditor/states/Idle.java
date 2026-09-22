package dev.askov.vipet.mvc.controllers.networkeditor.states;

import dev.askov.vipet.mvc.common.SelectedNetworkElementModelChangeListener;
import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.mvc.views.network.NetworkElement;
import dev.askov.vipet.mvc.views.network.NetworkNode;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;

public final class Idle extends NetworkEditorState {

  private final ContextMenu networkNodeContextMenu = new ContextMenu();
  private ChangeListener<NetworkElementModel> selectedNetworkElementModelChangeListener;

  @Override
  public void setNetworkEditorController(final NetworkEditorController networkEditorController) {
    super.setNetworkEditorController(networkEditorController);

    selectedNetworkElementModelChangeListener =
        new SelectedNetworkElementModelChangeListener(
            networkEditorController, networkNodeContextMenu.getItems());
  }

  @Override
  public void enter() {
    super.enter();

    networkEditorController
        .getModel()
        .selectedNetworkElementModelProperty()
        .addListener(selectedNetworkElementModelChangeListener);

    networkEditorController.getNetworkElements().forEach(this::setMouseListeners);

    for (final var networkElement : networkEditorController.getNetworkElements()) {
      setMouseListeners(networkElement);
    }
  }

  @Override
  public void exit() {
    super.exit();

    networkEditorController
        .getModel()
        .selectedNetworkElementModelProperty()
        .removeListener(selectedNetworkElementModelChangeListener);

    networkEditorController.getNetworkElements().forEach(this::resetMouseListeners);
  }

  @Override
  protected String getName() {
    return "Idle";
  }

  private void setMouseListeners(final NetworkElement networkElement) {
    final var networkElementModel = networkEditorController.getNetworkElementModel(networkElement);

    networkElement.setOnMouseEntered(
        event -> networkElementModel.setState(NetworkElementModel.State.HIGHLIGHTED));
    networkElement.setOnMouseExited(
        event -> {
          if (networkElementModel
              != networkEditorController.getModel().getSelectedNetworkElementModel()) {
            networkElementModel.setState(NetworkElementModel.State.NORMAL);
          }
        });
    networkElement.setOnMouseClicked(
        event -> {
          networkEditorController.getModel().setSelectedNetworkElementModel(networkElementModel);
          if (event.getButton() == MouseButton.SECONDARY) {
            networkNodeContextMenu.show(networkElement, event.getScreenX(), event.getScreenY());
          }
        });
    networkElement.setOnMousePressed(
        event -> {
          if (event.isPrimaryButtonDown() && networkElement instanceof NetworkNode networkNode) {
            networkNodeContextMenu.hide();
            networkEditorController.setState(
                new MovingNode(networkNode, event.getX(), event.getY()));
          }
        });
  }

  private void resetMouseListeners(final NetworkElement networkElement) {
    networkElement.setOnMouseEntered(null);
    networkElement.setOnMouseExited(null);
    networkElement.setOnMouseClicked(null);
    networkElement.setOnMousePressed(null);
  }
}
