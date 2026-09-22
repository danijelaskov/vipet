package dev.askov.vipet.mvc.views.network.connection;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;

public final class StraightLinePath extends AbstractPath {

  public StraightLinePath() {
    super();

    final var moveTo = new MoveTo();

    moveTo.xProperty().bind(startPoint.xProperty());
    moveTo.yProperty().bind(startPoint.yProperty());

    final var lineTo = new LineTo();

    lineTo.xProperty().bind(endPoint.xProperty());
    lineTo.yProperty().bind(endPoint.yProperty());

    getElements().addAll(moveTo, lineTo);
  }
}
