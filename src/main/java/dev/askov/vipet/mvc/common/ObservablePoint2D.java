package dev.askov.vipet.mvc.common;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;

public final class ObservablePoint2D {

  private final DoubleProperty xProperty = new SimpleDoubleProperty(this, "x", 0.0);
  private final DoubleProperty yProperty = new SimpleDoubleProperty(this, "y", 0.0);

  public ObservablePoint2D(final double x, final double y) {
    xProperty.set(x);
    yProperty.set(y);
  }

  public ObservablePoint2D() {
    this(0.0, 0.0);
  }

  public DoubleProperty xProperty() {
    return xProperty;
  }

  public double getX() {
    return xProperty.get();
  }

  public void setX(final double x) {
    xProperty.set(x);
  }

  public DoubleProperty yProperty() {
    return yProperty;
  }

  public double getY() {
    return yProperty.get();
  }

  public void setY(final double y) {
    yProperty.set(y);
  }

  public ObservablePoint2D midpoint(final ObservablePoint2D otherPoint) {
    final var midpoint = new ObservablePoint2D();

    midpoint.xProperty().bind(xProperty.add(otherPoint.xProperty()).divide(2.0));
    midpoint.yProperty().bind(yProperty.add(otherPoint.yProperty()).divide(2.0));

    return midpoint;
  }

  public DoubleBinding verticalDistance(final ObservablePoint2D otherPoint) {
    return new DoubleBinding() {
      {
        super.bind(yProperty, otherPoint.yProperty());
      }

      @Override
      protected double computeValue() {
        return Math.abs(yProperty.get() - otherPoint.yProperty().get());
      }
    };
  }

  public DoubleBinding horizontalDistance(final ObservablePoint2D otherPoint) {
    return new DoubleBinding() {
      {
        super.bind(xProperty, otherPoint.xProperty());
      }

      @Override
      protected double computeValue() {
        return Math.abs(xProperty.get() - otherPoint.xProperty().get());
      }
    };
  }

  public ObservablePoint2D add(final ObservablePoint2D otherPoint) {
    final var sum = new ObservablePoint2D();

    sum.xProperty().bind(xProperty.add(otherPoint.xProperty()));
    sum.yProperty().bind(yProperty.add(otherPoint.yProperty()));

    return sum;
  }

  public ObservablePoint2D subtract(final ObservablePoint2D otherPoint) {
    final var difference = new ObservablePoint2D();

    difference.xProperty().bind(xProperty.subtract(otherPoint.xProperty()));
    difference.yProperty().bind(yProperty.subtract(otherPoint.yProperty()));

    return difference;
  }

  public DoubleProperty length() {
    return new SimpleDoubleProperty(
        Math.sqrt(Math.pow(xProperty.get(), 2.0) + Math.pow(yProperty.get(), 2.0)));
  }

  public void addListener(final ChangeListener<? super Number> listener) {
    xProperty.addListener(listener);
    yProperty.addListener(listener);
  }
}
