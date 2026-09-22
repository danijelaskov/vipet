package dev.askov.vipet.mvc.models.network;

import dev.askov.vipet.mvc.common.ObservablePoint2D;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public abstract class NetworkNodeModel extends NetworkElementModel implements NamedModel {

  private static final Color FILL_COLOR = Color.WHITE;
  private static final Color STROKE_COLOR = Color.BLACK;

  private long id;

  private final StringProperty nameProperty;
  private final StringProperty descriptionProperty;
  private final DoubleProperty xProperty;
  private final DoubleProperty yProperty;
  private final BooleanProperty forwardOrientedProperty;
  private final ObjectProperty<Color> fillColorProperty;
  private final ObjectProperty<Color> strokeColorProperty;
  private final ReadOnlyObjectWrapper<ObservablePoint2D> inputPortPositionProperty;
  private final ReadOnlyObjectWrapper<ObservablePoint2D> outputPortPositionProperty;

  protected final DoubleProperty heightProperty;
  protected final DoubleProperty widthProperty;
  protected final DoubleProperty minYProperty;
  protected final DoubleProperty maxYProperty;
  protected final ReadOnlyBooleanWrapper validProperty;

  public NetworkNodeModel(final long id, final String name, final double x, final double y) {
    this.id = id;

    nameProperty = new SimpleStringProperty(this, "name", name);
    xProperty = new SimpleDoubleProperty(this, "x", x);
    yProperty = new SimpleDoubleProperty(this, "y", y);
    forwardOrientedProperty = new SimpleBooleanProperty(this, "forwardOriented", true);
    descriptionProperty = new SimpleStringProperty(this, "description", "");
    fillColorProperty = new SimpleObjectProperty<>(this, "fillColor", FILL_COLOR);
    strokeColorProperty = new SimpleObjectProperty<>(this, "strokeColor", STROKE_COLOR);
    heightProperty = new SimpleDoubleProperty(this, "height");
    widthProperty = new SimpleDoubleProperty(this, "width");
    minYProperty = new SimpleDoubleProperty(this, "minY");
    maxYProperty = new SimpleDoubleProperty(this, "maxY");
    inputPortPositionProperty =
        new ReadOnlyObjectWrapper<>(this, "inputPortPosition", new ObservablePoint2D());
    outputPortPositionProperty =
        new ReadOnlyObjectWrapper<>(this, "outputPortPosition", new ObservablePoint2D());
    validProperty = new ReadOnlyBooleanWrapper(this, "valid", false);

    getInputPortPosition()
        .xProperty()
        .bind(
            Bindings.when(forwardOrientedProperty)
                .then(xProperty)
                .otherwise(xProperty.add(widthProperty)));
    getInputPortPosition().yProperty().bind(yProperty.add(heightProperty.divide(2.0)));

    getOutputPortPosition()
        .xProperty()
        .bind(
            Bindings.when(forwardOrientedProperty)
                .then(xProperty.add(widthProperty))
                .otherwise(xProperty));
    getOutputPortPosition().yProperty().bind(yProperty.add(heightProperty.divide(2.0)));
  }

  public NetworkNodeModel(final int id) {
    this(id, "", 0.f, 0.f);
  }

  public NetworkNodeModel(final NetworkNodeModel networkNode) {
    id = networkNode.id;

    nameProperty = networkNode.nameProperty;
    descriptionProperty = networkNode.descriptionProperty;
    xProperty = networkNode.xProperty;
    yProperty = networkNode.yProperty;
    forwardOrientedProperty = networkNode.forwardOrientedProperty;
    fillColorProperty = networkNode.fillColorProperty;
    strokeColorProperty = networkNode.strokeColorProperty;
    heightProperty = networkNode.heightProperty;
    widthProperty = networkNode.widthProperty;
    minYProperty = networkNode.minYProperty;
    maxYProperty = networkNode.maxYProperty;
    inputPortPositionProperty = networkNode.inputPortPositionProperty;
    outputPortPositionProperty = networkNode.outputPortPositionProperty;
    validProperty = networkNode.validProperty;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public StringProperty nameProperty() {
    return nameProperty;
  }

  public StringProperty descriptionProperty() {
    return descriptionProperty;
  }

  public String getDescription() {
    return descriptionProperty.get();
  }

  public void setDescription(String description) {
    descriptionProperty.set(description);
  }

  public DoubleProperty xProperty() {
    return xProperty;
  }

  public double getX() {
    return xProperty.get();
  }

  public void setX(double x) {
    xProperty.set(x);
  }

  public DoubleProperty yProperty() {
    return yProperty;
  }

  public double getY() {
    return yProperty.get();
  }

  public void setY(double y) {
    yProperty.set(y);
  }

  public BooleanProperty forwardOrientedProperty() {
    return forwardOrientedProperty;
  }

  public boolean isForwardOriented() {
    return forwardOrientedProperty.get();
  }

  public void setForwardOriented(boolean forwardOriented) {
    forwardOrientedProperty.set(forwardOriented);
  }

  public ObjectProperty<Color> fillColorProperty() {
    return fillColorProperty;
  }

  public Color getFillColor() {
    return fillColorProperty.get();
  }

  public void setFillColor(Color color) {
    fillColorProperty.set(color);
  }

  public ObjectProperty<Color> strokeColorProperty() {
    return strokeColorProperty;
  }

  public Color getStrokeColor() {
    return strokeColorProperty.get();
  }

  public void setStrokeColor(Color color) {
    strokeColorProperty.set(color);
  }

  public DoubleProperty heightProperty() {
    return heightProperty;
  }

  public double getHeightWithoutText() {
    return heightProperty.get();
  }

  public DoubleProperty widthProperty() {
    return widthProperty;
  }

  public double getWidth() {
    return widthProperty.get();
  }

  public DoubleProperty minYProperty() {
    return minYProperty;
  }

  public double getMinY() {
    return minYProperty.get();
  }

  public DoubleProperty maxYProperty() {
    return maxYProperty;
  }

  public double getMaxY() {
    return maxYProperty.get();
  }

  public ObservablePoint2D getInputPortPosition() {
    return inputPortPositionProperty.get();
  }

  public ObservablePoint2D getOutputPortPosition() {
    return outputPortPositionProperty.get();
  }

  public ReadOnlyBooleanProperty validProperty() {
    return validProperty.getReadOnlyProperty();
  }

  public boolean isValid() {
    return validProperty.get();
  }

  public boolean isNotValid() {
    return !validProperty.get();
  }

  public abstract String getType();

  @Override
  public String getName() {
    return nameProperty.get();
  }

  @Override
  public void setName(String name) {
    nameProperty.set(name);
  }

  @Override
  public String toString() {
    return getName();
  }
}
