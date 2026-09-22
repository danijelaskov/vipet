package dev.askov.vipet.mvc.views.network;

import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.scene.shape.Polygon;

public abstract class PolygonBasedNetworkNode extends NetworkNode {

  protected final Polygon polygon = new Polygon();

  public PolygonBasedNetworkNode() {
    polygon.fillProperty().bind(gradientProperty);
    polygon.strokeProperty().bind(strokeColorProperty);
    polygon
        .effectProperty()
        .bind(Bindings.createObjectBinding(() -> stateProperty.get().getEffect(), stateProperty));

    forwardOrientedProperty.addListener(
        (forwardOriented, wasForwardOriented, isForwardOriented) -> {
          polygon.getPoints().clear();
          polygon.getPoints().addAll(getPoints(isForwardOriented));
        });

    getChildren().add(polygon);
  }

  protected abstract List<Double> getPoints(boolean isForwardOriented);
}
