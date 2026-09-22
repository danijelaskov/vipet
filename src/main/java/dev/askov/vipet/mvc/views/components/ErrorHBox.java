package dev.askov.vipet.mvc.views.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ErrorHBox extends HBox {

  public ErrorHBox(final String message) {
    super();

    final var warningIcon = new FontIcon("codicon-warning");
    warningIcon.setIconSize(24);
    warningIcon.setIconColor(Color.RED);

    final var warningLabel = new Label(message);
    warningLabel.setTextFill(Color.RED);
    warningLabel.setWrapText(true);
    HBox.setHgrow(warningLabel, Priority.ALWAYS);

    getChildren().addAll(warningIcon, warningLabel);
    setAlignment(Pos.CENTER);
    setSpacing(5.0);
    setPadding(new Insets(5.0, 5.0, 5.0, 5.0));

    getStyleClass().add("error-hbox");
  }
}
