package dev.askov.vipet.mvc.controllers.networkeditor.states;

import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import dev.askov.vipet.mvc.models.network.SinkModel;

public final class AddingSink extends AddingNode<SinkModel> {

  @Override
  public void setNetworkEditorController(final NetworkEditorController networkEditorController) {
    super.setNetworkEditorController(networkEditorController);

    networkNodeModelSupplier =
        () -> new SinkModel(networkEditorController.getModel().getNextNodeId());
  }

  @Override
  protected String getName() {
    return "Adding Sink";
  }
}
