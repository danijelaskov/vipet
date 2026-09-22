package dev.askov.vipet.mvc.models.network;

import javafx.scene.paint.Color;

public final class SourceModel extends NonTerminalNetworkNodeModel {

  private static final Color DEFAULT_FILL_COLOR = Color.rgb(204, 255, 204);
  private static final Color DEFAULT_STROKE_COLOR = Color.rgb(51, 102, 51);

  public SourceModel(final long id, final String name, final double x, final double y) {
    super(id, name, x, y);

    setFillColor(DEFAULT_FILL_COLOR);
    setStrokeColor(DEFAULT_STROKE_COLOR);

    validProperty.bind(connectionsProperty.emptyProperty().not());
  }

  public SourceModel(final long id) {
    this(id, "", 0.f, 0.f);
  }

  public SourceModel(final SourceModel sourceModel) {
    super(sourceModel);
  }

  @Override
  public String getType() {
    return "Source";
  }
}
