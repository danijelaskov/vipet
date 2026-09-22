package dev.askov.vipet.mvc.views.network.connection;

import dev.askov.vipet.mvc.common.ObservablePoint2D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.shape.Path;

public abstract class AbstractPath extends Path {

  protected final ObservablePoint2D startPoint = new ObservablePoint2D();
  protected final BooleanProperty startNodeForwardOrientedProperty =
      new SimpleBooleanProperty(this, "startNodeForwardOriented", true);
  protected final ObservablePoint2D endPoint = new ObservablePoint2D();
  protected final BooleanProperty endNodeForwardOrientedProperty =
      new SimpleBooleanProperty(this, "endNodeForwardOriented", true);

  public ObservablePoint2D getStartPoint() {
    return startPoint;
  }

  public BooleanProperty startNodeForwardOrientedProperty() {
    return startNodeForwardOrientedProperty;
  }

  public boolean isStartNodeForwardOriented() {
    return startNodeForwardOrientedProperty.get();
  }

  public void setStartNodeForwardOriented(final boolean startNodeForwardOriented) {
    this.startNodeForwardOrientedProperty.set(startNodeForwardOriented);
  }

  public ObservablePoint2D getEndPoint() {
    return endPoint;
  }

  public BooleanProperty endNodeForwardOrientedProperty() {
    return endNodeForwardOrientedProperty;
  }

  public boolean isEndNodeForwardOriented() {
    return endNodeForwardOrientedProperty.get();
  }

  public void setEndNodeForwardOrientedProperty(final boolean endNodeForwardOrientedProperty) {
    this.endNodeForwardOrientedProperty.set(endNodeForwardOrientedProperty);
  }
}
