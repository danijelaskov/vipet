package dev.askov.vipet.mvc.views.network.connection;

import dev.askov.vipet.mvc.common.ObservablePoint2D;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;

public final class BezierLinePath extends AbstractPath {

  public BezierLinePath() {
    super();

    final var midPoint = startPoint.midpoint(endPoint);

    final var controlPoint1 = new ObservablePoint2D();
    controlPoint1.xProperty().bind(midPoint.xProperty());
    controlPoint1.yProperty().bind(startPoint.midpoint(midPoint).yProperty());

    final var controlPoint2 = midPoint.midpoint(endPoint);
    controlPoint2.xProperty().bind(midPoint.xProperty());
    controlPoint2.yProperty().bind(endPoint.midpoint(midPoint).yProperty());

    final var moveTo = new MoveTo();

    moveTo.xProperty().bind(startPoint.xProperty());
    moveTo.yProperty().bind(startPoint.yProperty());

    final var cubicCurveTo = new CubicCurveTo();

    cubicCurveTo.controlX1Property().bind(controlPoint1.xProperty());
    cubicCurveTo.controlY1Property().bind(controlPoint1.yProperty());

    cubicCurveTo.controlX2Property().bind(controlPoint2.xProperty());
    cubicCurveTo.controlY2Property().bind(controlPoint2.yProperty());

    cubicCurveTo.xProperty().bind(endPoint.xProperty());
    cubicCurveTo.yProperty().bind(endPoint.yProperty());

    getElements().addAll(moveTo, cubicCurveTo);
    setFill(null);
  }
}
