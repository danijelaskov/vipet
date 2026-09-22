package dev.askov.vipet.mvc.views.network;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;

public abstract class NetworkNode extends NetworkElement {

  protected static final double DEFAULT_TEXT_NODE_DISTANCE = 10.0;

  private static final double DEFAULT_NODE_POSITION_X = 0.0;
  private static final double DEFAULT_NODE_POSITION_Y = 0.0;

  protected final BooleanProperty forwardOrientedProperty =
      new SimpleBooleanProperty(this, "forwardOriented", true);
  protected final ObjectProperty<LinearGradient> gradientProperty =
      new SimpleObjectProperty<>(this, "gradient", null);
  protected final DoubleProperty heightProperty = new SimpleDoubleProperty(this, "height", 0.0);
  protected final DoubleProperty widthProperty = new SimpleDoubleProperty(this, "width", 0.0);

  private final Label nameLabel = new Label();

  public NetworkNode() {
    setTranslateX(DEFAULT_NODE_POSITION_X);
    setTranslateY(DEFAULT_NODE_POSITION_Y);

    strokeColorProperty.bind(
        Bindings.when(
                stateProperty
                    .isNotEqualTo(State.OVERLAPPING)
                    .and(stateProperty.isNotEqualTo(State.MOVING)))
            .then(normalStateStrokeColorProperty)
            .otherwise(
                Bindings.createObjectBinding(() -> stateProperty.get().getColor(), stateProperty)));
    fillColorProperty.bind(normalStateFillColorProperty);
    opacityProperty()
        .bind(
            Bindings.when(
                    stateProperty
                        .isEqualTo(State.FADED)
                        .or(stateProperty.isEqualTo(State.FADED_SOURCE)))
                .then(0.5)
                .otherwise(1.0));

    gradientProperty.bind(
        Bindings.createObjectBinding(
            () ->
                new LinearGradient(
                    0.0,
                    0.0,
                    0.0,
                    1.0,
                    true,
                    null,
                    new Stop(0.0, fillColorProperty.getValue().brighter()),
                    new Stop(1.0, fillColorProperty.getValue().darker())),
            fillColorProperty));

    nameLabel
        .translateXProperty()
        .bind(widthProperty.divide(2).subtract(nameLabel.widthProperty().divide(2)));
    nameLabel.translateYProperty().bind(heightProperty.add(DEFAULT_TEXT_NODE_DISTANCE));
    nameLabel.setTextAlignment(TextAlignment.CENTER);
    nameLabel.textFillProperty().bind(strokeColorProperty);

    getChildren().addAll(nameLabel);

    stateProperty.addListener(
        (state, oldState, newState) -> {
          if (newState == State.HIGHLIGHTED
              || newState == State.SELECTED
              || newState == State.MOVING) {
            toFront();
          }
        });

    errorIcon
        .xProperty()
        .bind(nameLabel.translateXProperty().add(nameLabel.widthProperty().add(5)));
    errorIcon.yProperty().bind(nameLabel.translateYProperty().add(nameLabel.heightProperty()));
    getChildren().add(errorIcon);
  }

  public BooleanProperty forwardOrientedProperty() {
    return forwardOrientedProperty;
  }

  public boolean isForwardOriented() {
    return forwardOrientedProperty.get();
  }

  public DoubleProperty heightProperty() {
    return heightProperty;
  }

  public double getHeight() {
    return heightProperty.get();
  }

  public DoubleProperty widthProperty() {
    return widthProperty;
  }

  public double getWidth() {
    return widthProperty.get();
  }

  public Property<Color> fillColorProperty() {
    return fillColorProperty;
  }

  public Color getFillColor() {
    return fillColorProperty.getValue();
  }

  public Label getNameLabel() {
    return nameLabel;
  }
}
