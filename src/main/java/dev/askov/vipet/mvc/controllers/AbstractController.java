package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.localization.LocalizationManager;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

public abstract class AbstractController<Model> {

  protected Model model;
  protected final LocalizationManager localizationManager = LocalizationManager.getInstance();
  @FXML protected ResourceBundle resources;

  public AbstractController(final Model model) {
    this.model = model;
  }

  public Model getModel() {
    return model;
  }

  public void setModel(Model model) {
    this.model = model;
  }

  public void onWindowClosed(final WindowEvent event) {}

  @FXML
  protected abstract void initialize();
}
