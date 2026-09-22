package dev.askov.vipet.mvc.views.components;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public final class ErrorsVBox extends VBox {

  public ErrorsVBox() {}

  public void addError(final String message) {
    final var errorHBox = new ErrorHBox(message);

    errorHBox.setAlignment(Pos.CENTER_LEFT);

    getChildren().add(errorHBox);
  }
}
