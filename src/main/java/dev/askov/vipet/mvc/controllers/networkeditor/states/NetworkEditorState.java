package dev.askov.vipet.mvc.controllers.networkeditor.states;

import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NetworkEditorState {

  protected static final Logger LOGGER = LoggerFactory.getLogger(NetworkEditorState.class);

  protected NetworkEditorController networkEditorController;

  public void setNetworkEditorController(final NetworkEditorController networkEditorController) {
    this.networkEditorController = networkEditorController;
  }

  public void enter() {
    if (networkEditorController == null) {
      throw new IllegalStateException("NetworkEditorController is null");
    }
    LOGGER.debug(
        "Network \"{}\", entering state: {}",
        networkEditorController.getModel().getName(),
        getName());
  }

  public void exit() {
    if (networkEditorController == null) {
      throw new IllegalStateException("NetworkEditorController is null");
    }
    LOGGER.debug(
        "Network \"{}\", exiting state: {}",
        networkEditorController.getModel().getName(),
        getName());
  }

  protected abstract String getName();
}
