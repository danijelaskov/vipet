package dev.askov.vipet.mvc.controllers.networkeditor.states;

import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import dev.askov.vipet.mvc.models.network.SourceModel;

public final class AddingSource extends AddingNode<SourceModel> {

  @Override
  public void setNetworkEditorController(final NetworkEditorController networkEditorController) {
    super.setNetworkEditorController(networkEditorController);

    networkNodeModelSupplier =
        () -> new SourceModel(networkEditorController.getModel().getNextNodeId());
  }

  @Override
  protected String getName() {
    return "Adding Source";
  }
}
