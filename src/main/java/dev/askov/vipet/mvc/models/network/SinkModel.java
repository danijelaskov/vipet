package dev.askov.vipet.mvc.models.network;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

public final class SinkModel extends NetworkNodeModel {

  private static final Color DEFAULT_FILL_COLOR = Color.rgb(255, 204, 204);
  private static final Color DEFAULT_STROKE_COLOR = Color.rgb(204, 51, 51);

  private final BooleanProperty incomingConnectionsProperty =
      new SimpleBooleanProperty(this, "incomingConnections", false);
  private final BooleanProperty reachableProperty =
      new SimpleBooleanProperty(this, "reachable", false);

  public SinkModel(final long id, final String name, final double x, final double y) {
    super(id, name, x, y);

    setFillColor(DEFAULT_FILL_COLOR);
    setStrokeColor(DEFAULT_STROKE_COLOR);

    validProperty.bind(incomingConnectionsProperty.and(reachableProperty));
  }

  public SinkModel(final long id) {
    this(id, "", 0.f, 0.f);
  }

  public SinkModel(final SinkModel sinkModel) {
    super(sinkModel);
  }

  public void setIncomingConnections(final boolean incomingConnections) {
    incomingConnectionsProperty.set(incomingConnections);
  }

  public boolean hasIncomingConnections() {
    return incomingConnectionsProperty.get();
  }

  public BooleanProperty incomingConnectionsProperty() {
    return incomingConnectionsProperty;
  }

  public void setReachable(final boolean reachable) {
    reachableProperty.set(reachable);
  }

  public boolean isReachable() {
    return reachableProperty.get();
  }

  public BooleanProperty reachableProperty() {
    return reachableProperty;
  }

  @Override
  public String getType() {
    return "Sink";
  }
}
