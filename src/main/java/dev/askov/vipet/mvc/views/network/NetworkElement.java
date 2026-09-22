package dev.askov.vipet.mvc.views.network;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public abstract class NetworkElement extends Group {

  public enum State {
    CREATING(Color.GREEN, Cursor.CLOSED_HAND),
    MOVING(Color.BLUE, Cursor.MOVE),
    OVERLAPPING(Color.RED),
    SELECTED(
        Color.BLUE,
        new DropShadow(BlurType.GAUSSIAN, Color.BLUE, 5.0, 0.1, 4.0, 4.0),
        Cursor.DEFAULT),
    HIGHLIGHTED(
        Color.BLUE, new DropShadow(BlurType.GAUSSIAN, Color.BLUE, 5.0, 0.1, 4.0, 4.0), Cursor.HAND),
    FADED(Color.GRAY, new DropShadow(BlurType.GAUSSIAN, Color.GRAY, 5.0, 0.1, 4.0, 4.0)),
    SOURCE(Color.GREEN, new DropShadow(BlurType.GAUSSIAN, Color.GREEN, 5.0, 0.1, 4.0, 4.0)),
    FADED_SOURCE(Color.GRAY, new DropShadow(BlurType.GAUSSIAN, Color.GREEN, 5.0, 0.1, 4.0, 4.0)),
    NORMAL(Color.GRAY, new DropShadow(BlurType.GAUSSIAN, Color.GRAY, 5.0, 0.1, 4.0, 4.0)),
    SIMULATING(Color.GRAY, new DropShadow(BlurType.GAUSSIAN, Color.GRAY, 5.0, 0.1, 4.0, 4.0));

    private final Color color;
    private final Effect effect;
    private final Cursor cursor;

    State(final Color color, final Effect effect, final Cursor cursor) {
      this.color = color;
      this.effect = effect;
      this.cursor = cursor;
    }

    State(final Color color, final Cursor cursor) {
      this(color, null, cursor);
    }

    State(final Color color, final Effect effect) {
      this(color, effect, null);
    }

    State(final Color color) {
      this(color, null, null);
    }

    public Color getColor() {
      return color;
    }

    public Effect getEffect() {
      return effect;
    }

    public Cursor getCursor() {
      return cursor;
    }
  }

  protected static final Color ERROR_ICON_COLOR = new Color(1.0, 0.0, 0.0, 1.0);
  protected static final String ERROR_ICON_CODE = "fltfmz-warning-24";
  private static final int ERROR_ICON_SIZE = 20;

  protected final ObjectProperty<State> stateProperty =
      new SimpleObjectProperty<>(this, "state", State.NORMAL);
  protected final ObjectProperty<Color> normalStateFillColorProperty =
      new SimpleObjectProperty<>(this, "normalStateFillColor", Color.WHITE);
  protected final ObjectProperty<Color> fillColorProperty =
      new SimpleObjectProperty<>(this, "fillColor", Color.WHITE);
  protected final ObjectProperty<Color> normalStateStrokeColorProperty =
      new SimpleObjectProperty<>(this, "normalStateStrokeColor", Color.BLACK);
  protected final ObjectProperty<Color> strokeColorProperty =
      new SimpleObjectProperty<>(this, "strokeColor", Color.BLACK);
  protected final BooleanProperty showTooltipProperty =
      new SimpleBooleanProperty(this, "showTooltip", false);
  protected final BooleanProperty validProperty = new SimpleBooleanProperty(this, "valid", false);
  protected final FontIcon errorIcon = new FontIcon(ERROR_ICON_CODE);

  private final Tooltip tooltip = new Tooltip();

  public NetworkElement() {
    stateProperty.addListener(
        (state, oldState, newState) -> {
          if (newState.getCursor() != null) {
            setCursor(newState.getCursor());
          } else if (oldState != null && oldState.getCursor() != null) {
            setCursor(oldState.getCursor());
          } else {
            setCursor(Cursor.CLOSED_HAND);
          }
        });

    showTooltipProperty.addListener(
        (showTooltip, wasShown, isShown) -> {
          if (isShown) {
            Tooltip.install(this, tooltip);
          } else {
            Tooltip.uninstall(this, tooltip);
          }
        });

    opacityProperty()
        .bind(Bindings.when(stateProperty.isEqualTo(State.FADED)).then(0.5).otherwise(1.0));

    showTooltipProperty.bind(
        Bindings.when(
                stateProperty.isEqualTo(State.HIGHLIGHTED).and(tooltip.textProperty().isNotEmpty()))
            .then(true)
            .otherwise(false));

    errorIcon.setIconColor(ERROR_ICON_COLOR);
    errorIcon.setIconSize(ERROR_ICON_SIZE);
    errorIcon
        .visibleProperty()
        .bind(
            stateProperty
                .isNotEqualTo(State.CREATING)
                .and(stateProperty.isNotEqualTo(State.OVERLAPPING))
                .and(validProperty.not()));
  }

  public ObjectProperty<State> stateProperty() {
    return stateProperty;
  }

  public State getState() {
    return stateProperty.get();
  }

  public void setState(final State state) {
    stateProperty.set(state);
  }

  public final Tooltip getTooltip() {
    return tooltip;
  }

  public final BooleanProperty validProperty() {
    return validProperty;
  }

  public final Property<Color> normalStateFillColorProperty() {
    return normalStateFillColorProperty;
  }

  public final Property<Color> normalStateStrokeColorProperty() {
    return normalStateStrokeColorProperty;
  }

  public final Property<Color> strokeColorProperty() {
    return strokeColorProperty;
  }

  public Color getStrokeColor() {
    return strokeColorProperty.getValue();
  }
}
