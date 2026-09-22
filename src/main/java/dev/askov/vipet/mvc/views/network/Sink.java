package dev.askov.vipet.mvc.views.network;

import java.util.List;

public final class Sink extends PolygonBasedNetworkNode {

  public static final double SINK_WIDTH = 0.5 * ServiceCenter.SERVICE_CENTER_WIDTH;
  public static final double SINK_HEIGHT = ServiceCenter.SINGLE_SERVER_HEIGHT;

  public Sink() {
    widthProperty.set(SINK_WIDTH);
    heightProperty.set(SINK_HEIGHT);

    polygon.getPoints().addAll(getPoints(isForwardOriented()));
  }

  @Override
  protected List<Double> getPoints(boolean isForwardOriented) {
    if (isForwardOriented) {
      return List.of(
          0.0,
          0.0,
          getWidth(),
          0.0,
          getWidth(),
          getHeight(),
          0.0,
          getHeight(),
          0.15 * getWidth(),
          0.5 * getHeight());
    } else {
      return List.of(
          0.0,
          0.0,
          getWidth(),
          0.0,
          0.85 * getWidth(),
          0.5 * getHeight(),
          getWidth(),
          getHeight(),
          0.0,
          getHeight());
    }
  }
}
