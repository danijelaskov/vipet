package dev.askov.vipet.mvc.views.network;

import java.util.List;

public final class Source extends PolygonBasedNetworkNode {

  public static final double SOURCE_WIDTH = 0.5 * ServiceCenter.SERVICE_CENTER_WIDTH;
  public static final double SOURCE_HEIGHT = ServiceCenter.SINGLE_SERVER_HEIGHT;

  public Source() {
    widthProperty.set(SOURCE_WIDTH);
    heightProperty.set(SOURCE_HEIGHT);

    polygon.getPoints().addAll(getPoints(isForwardOriented()));
  }

  @Override
  protected List<Double> getPoints(final boolean isForwardOriented) {
    if (isForwardOriented) {
      return List.of(
          0.0,
          0.0,
          0.85 * getWidth(),
          0.0,
          getWidth(),
          0.5 * getHeight(),
          0.85 * getWidth(),
          getHeight(),
          0.0,
          getHeight());
    } else {
      return List.of(
          0.0,
          0.5 * getHeight(),
          0.15 * getWidth(),
          0.0,
          getWidth(),
          0.0,
          getWidth(),
          getHeight(),
          0.15 * getWidth(),
          getHeight());
    }
  }
}
