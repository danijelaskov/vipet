package dev.askov.vipet.mvc.views.components;

import dev.askov.vipet.common.LaTeXUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

public final class LaTeXEquation extends ImageView {

  private final StringProperty textProperty = new SimpleStringProperty(this, "text", "");

  public LaTeXEquation(final String laTeXString) {
    super();

    textProperty.addListener(
        (observable, oldLaTeXString, newLaTeXString) -> {
          if (newLaTeXString != null && !newLaTeXString.isEmpty()) {
            final var image = LaTeXUtil.createImageFromLaTeX(newLaTeXString);
            if (image != null) {
              setImage(image);

              setFitWidth(image.getWidth());
              setFitHeight(image.getHeight());
            }
          }
        });

    setText(laTeXString);
  }

  @SuppressWarnings("unused")
  public LaTeXEquation() {
    this("");
  }

  public String getText() {
    return textProperty.get();
  }

  public void setText(final String latex) {
    textProperty.set(latex);
  }

  public StringProperty textProperty() {
    return textProperty;
  }
}
