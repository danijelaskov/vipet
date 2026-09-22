package dev.askov.vipet.mvc.controllers.networkeditor.states;

import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import javafx.scene.input.ScrollEvent;

public final class AddingServiceCenter extends AddingNode<ServiceCenterModel> {

  @Override
  public void setNetworkEditorController(final NetworkEditorController networkEditorController) {
    super.setNetworkEditorController(networkEditorController);

    networkNodeModelSupplier =
        () -> new ServiceCenterModel(networkEditorController.getModel().getNextNodeId());
  }

  @Override
  public void enter() {
    super.enter();

    networkEditorController.getScrollPane().setOnScroll(this::onScroll);
  }

  @Override
  public void exit() {
    super.exit();

    networkEditorController.getScrollPane().setOnScroll(null);
  }

  @Override
  protected String getName() {
    return "Adding Service Center";
  }

  private void onScroll(final ScrollEvent event) {
    final var currentNumberOfServers = networkNodeModel.getNumberOfServers();

    if (event.getDeltaY() > 0) {
      networkNodeModel.setNumberOfServers(currentNumberOfServers + 1);
    } else {
      networkNodeModel.setNumberOfServers(
          currentNumberOfServers > 1 ? currentNumberOfServers - 1 : 1);
    }

    moveNetworkNode(event.getX(), event.getY());
  }
}
