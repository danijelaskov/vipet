package dev.askov.vipet.mvc.models.network;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class NetworkElementModel {

  public enum State {
    CREATING,
    MOVING,
    OVERLAPPING,
    SELECTED,
    HIGHLIGHTED,
    FADED,
    SOURCE,
    FADED_SOURCE,
    NORMAL,
    SIMULATING,
  }

  protected final ObjectProperty<State> stateProperty =
      new SimpleObjectProperty<>(this, "state", State.NORMAL);

  public ObjectProperty<State> stateProperty() {
    return stateProperty;
  }

  public State getState() {
    return stateProperty.get();
  }

  public void setState(final State state) {
    stateProperty.set(state);
  }
}
