package dev.askov.vipet.mvc.views.network.connection;

import dev.askov.vipet.mvc.common.ObservablePoint2D;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.*;

public final class MultiSegmentPathWithArcs extends AbstractPath {

  public static final double NODE_TO_CONNECTION_DISTANCE = 22.0;
  public static final double INTERNODE_DISTANCE = 2 * NODE_TO_CONNECTION_DISTANCE;

  private static final double ARC_RADIUS = 0.4 * NODE_TO_CONNECTION_DISTANCE;

  private final Path threeSegmentPath = new Path();
  private final Path fiveSegmentPath = new Path();

  private boolean isThreeSegmentPathVisible = true;

  private final DoubleProperty sourceMaxYProperty;
  private final DoubleProperty sourceMinYProperty;
  private final DoubleProperty destinationMaxYProperty;
  private final DoubleProperty destinationMinYProperty;

  public MultiSegmentPathWithArcs(
      final DoubleProperty sourceMaxYProperty,
      final DoubleProperty sourceMinYProperty,
      final DoubleProperty destinationMaxYProperty,
      final DoubleProperty destinationMinYProperty) {
    this.sourceMaxYProperty = sourceMaxYProperty;
    this.sourceMinYProperty = sourceMinYProperty;
    this.destinationMaxYProperty = destinationMaxYProperty;
    this.destinationMinYProperty = destinationMinYProperty;

    createPaths();
    startPoint.addListener((value, oldValue, newValue) -> switchPath());
    endPoint.addListener((value, oldValue, newValue) -> switchPath());
    startNodeForwardOrientedProperty().addListener((value, oldValue, newValue) -> switchPath());
    endNodeForwardOrientedProperty().addListener((value, oldValue, newValue) -> switchPath());
  }

  public DoubleProperty sourceMaxYProperty() {
    return sourceMaxYProperty;
  }

  public double getSourceMaxY() {
    return sourceMaxYProperty.get();
  }

  public DoubleProperty sourceMinYProperty() {
    return sourceMinYProperty;
  }

  public double getSourceMinY() {
    return sourceMinYProperty.get();
  }

  public DoubleProperty destinationMaxYProperty() {
    return destinationMaxYProperty;
  }

  public double getDestinationMaxY() {
    return destinationMaxYProperty.get();
  }

  public DoubleProperty destinationMinYProperty() {
    return destinationMinYProperty;
  }

  public double getDestinationMinY() {
    return destinationMinYProperty.get();
  }

  private boolean shouldUseThreeSegmentPath() {
    final var bothForward = isStartNodeForwardOriented() && isEndNodeForwardOriented();
    final var bothBackward = !isStartNodeForwardOriented() && !isEndNodeForwardOriented();
    final var oppositeDirections = isStartNodeForwardOriented() != isEndNodeForwardOriented();
    final var horizontalDistance = Math.abs(endPoint.getX() - startPoint.getX());
    final var verticalDistance =
        endPoint.getY() > startPoint.getY()
            ? getDestinationMinY() - getSourceMaxY()
            : getSourceMinY() - getDestinationMaxY();

    return (bothForward
            && (endPoint.getX() > startPoint.getX())
            && horizontalDistance >= INTERNODE_DISTANCE)
        || (bothBackward
            && (startPoint.getX() > endPoint.getX())
            && horizontalDistance >= INTERNODE_DISTANCE)
        || (oppositeDirections && verticalDistance >= INTERNODE_DISTANCE);
  }

  private void switchPath() {
    final var shouldUseThreeSegmentPath = shouldUseThreeSegmentPath();

    if (shouldUseThreeSegmentPath && (!isThreeSegmentPathVisible || getElements().isEmpty())) {
      getElements().clear();
      getElements().addAll(threeSegmentPath.getElements());

      isThreeSegmentPathVisible = true;
    } else if (!shouldUseThreeSegmentPath && isThreeSegmentPathVisible) {
      getElements().clear();
      getElements().addAll(fiveSegmentPath.getElements());

      isThreeSegmentPathVisible = false;
    }
  }

  private void createPaths() {
    final var initialMoveTo = new MoveTo();
    initialMoveTo.xProperty().bind(startPoint.xProperty());
    initialMoveTo.yProperty().bind(startPoint.yProperty());

    final var finalLineTo = new LineTo();
    finalLineTo.xProperty().bind(endPoint.xProperty());
    finalLineTo.yProperty().bind(endPoint.yProperty());

    final var connectionDistance = new ObservablePoint2D(NODE_TO_CONNECTION_DISTANCE, 0);

    {
      final var arcTo1 = new ArcTo();

      final var lineTo1 = new LineTo();
      lineTo1
          .xProperty()
          .bind(
              Bindings.when(endNodeForwardOrientedProperty)
                  .then(
                      Bindings.when(startNodeForwardOrientedProperty)
                          .then(
                              startPoint
                                  .add(connectionDistance)
                                  .xProperty()
                                  .subtract(arcTo1.radiusXProperty()))
                          .otherwise(
                              Bindings.when(startPoint.xProperty().lessThan(endPoint.xProperty()))
                                  .then(
                                      startPoint
                                          .subtract(connectionDistance)
                                          .xProperty()
                                          .add(arcTo1.radiusXProperty()))
                                  .otherwise(
                                      endPoint
                                          .subtract(connectionDistance)
                                          .xProperty()
                                          .add(arcTo1.radiusXProperty()))))
                  .otherwise(
                      Bindings.when(startNodeForwardOrientedProperty)
                          .then(
                              Bindings.when(
                                      startPoint.xProperty().greaterThan(endPoint.xProperty()))
                                  .then(
                                      startPoint
                                          .add(connectionDistance)
                                          .xProperty()
                                          .subtract(arcTo1.radiusXProperty()))
                                  .otherwise(
                                      endPoint
                                          .add(connectionDistance)
                                          .xProperty()
                                          .subtract(arcTo1.radiusXProperty())))
                          .otherwise(
                              startPoint
                                  .subtract(connectionDistance)
                                  .xProperty()
                                  .add(arcTo1.radiusXProperty()))));
      lineTo1.yProperty().bind(startPoint.yProperty());

      arcTo1
          .radiusXProperty()
          .bind(Bindings.min(endPoint.verticalDistance(startPoint).divide(2.0), ARC_RADIUS));
      arcTo1.radiusYProperty().bind(arcTo1.radiusXProperty());
      arcTo1
          .xProperty()
          .bind(
              Bindings.when(endNodeForwardOrientedProperty)
                  .then(
                      Bindings.when(startNodeForwardOrientedProperty)
                          .then(startPoint.add(connectionDistance).xProperty())
                          .otherwise(
                              Bindings.when(startPoint.xProperty().lessThan(endPoint.xProperty()))
                                  .then(startPoint.subtract(connectionDistance).xProperty())
                                  .otherwise(endPoint.subtract(connectionDistance).xProperty())))
                  .otherwise(
                      Bindings.when(startNodeForwardOrientedProperty)
                          .then(
                              Bindings.when(
                                      startPoint.xProperty().greaterThan(endPoint.xProperty()))
                                  .then(startPoint.add(connectionDistance).xProperty())
                                  .otherwise(endPoint.add(connectionDistance).xProperty()))
                          .otherwise(startPoint.subtract(connectionDistance).xProperty())));
      arcTo1
          .yProperty()
          .bind(
              startPoint
                  .yProperty()
                  .add(
                      arcTo1
                          .radiusXProperty()
                          .multiply(
                              Bindings.when(
                                      startPoint.yProperty().greaterThan(endPoint.yProperty()))
                                  .then(-1.0)
                                  .otherwise(1.0))));
      arcTo1
          .sweepFlagProperty()
          .bind(
              startNodeForwardOrientedProperty
                  .and(endPoint.yProperty().greaterThan(startPoint.yProperty()))
                  .or(
                      startNodeForwardOrientedProperty
                          .not()
                          .and(endPoint.yProperty().lessThan(startPoint.yProperty()))));

      final var arcTo2 = new ArcTo();

      final var lineTo2 = new LineTo();
      lineTo2.xProperty().bind(arcTo1.xProperty());
      lineTo2
          .yProperty()
          .bind(
              endPoint
                  .yProperty()
                  .add(
                      arcTo2
                          .radiusXProperty()
                          .multiply(
                              Bindings.when(
                                      startPoint.yProperty().greaterThan(endPoint.yProperty()))
                                  .then(1.0)
                                  .otherwise(-1.0))));

      arcTo2.radiusXProperty().bind(arcTo1.radiusXProperty());
      arcTo2.radiusYProperty().bind(arcTo2.radiusXProperty());
      arcTo2
          .xProperty()
          .bind(
              Bindings.when(endNodeForwardOrientedProperty)
                  .then(lineTo2.xProperty().add(arcTo2.radiusXProperty()))
                  .otherwise(lineTo2.xProperty().subtract(arcTo2.radiusXProperty())));
      arcTo2.yProperty().bind(endPoint.yProperty());
      arcTo2
          .sweepFlagProperty()
          .bind(
              endNodeForwardOrientedProperty
                  .and(startPoint.yProperty().greaterThan(endPoint.yProperty()))
                  .or(
                      endNodeForwardOrientedProperty
                          .not()
                          .and(startPoint.yProperty().lessThan(endPoint.yProperty()))));

      threeSegmentPath
          .getElements()
          .addAll(initialMoveTo, lineTo1, arcTo1, lineTo2, arcTo2, finalLineTo);
    }

    {
      final var arcTo1 = new ArcTo();

      final var lineTo1 = new LineTo();
      lineTo1
          .xProperty()
          .bind(
              Bindings.when(startNodeForwardOrientedProperty)
                  .then(
                      startPoint
                          .xProperty()
                          .add(NODE_TO_CONNECTION_DISTANCE)
                          .subtract(arcTo1.radiusXProperty()))
                  .otherwise(
                      startPoint
                          .xProperty()
                          .subtract(NODE_TO_CONNECTION_DISTANCE)
                          .add(arcTo1.radiusXProperty())));
      lineTo1.yProperty().bind(startPoint.yProperty());

      arcTo1.setRadiusX(ARC_RADIUS);
      arcTo1.radiusYProperty().bind(arcTo1.radiusXProperty());
      arcTo1
          .xProperty()
          .bind(
              Bindings.when(startNodeForwardOrientedProperty)
                  .then(startPoint.xProperty().add(NODE_TO_CONNECTION_DISTANCE))
                  .otherwise(startPoint.xProperty().subtract(NODE_TO_CONNECTION_DISTANCE)));
      arcTo1
          .yProperty()
          .bind(
              Bindings.when(startPoint.yProperty().isNotEqualTo(endPoint.yProperty()))
                  .then(
                      Bindings.when(startPoint.yProperty().lessThan(endPoint.yProperty()))
                          .then(
                              Bindings.when(
                                      destinationMinYProperty
                                          .subtract(sourceMaxYProperty)
                                          .greaterThanOrEqualTo(INTERNODE_DISTANCE))
                                  .then(startPoint.yProperty().add(ARC_RADIUS))
                                  .otherwise(startPoint.yProperty().subtract(ARC_RADIUS)))
                          .otherwise(startPoint.yProperty().subtract(ARC_RADIUS)))
                  .otherwise(startPoint.yProperty().subtract(ARC_RADIUS)));
      arcTo1
          .sweepFlagProperty()
          .bind(
              startNodeForwardOrientedProperty
                  .and(arcTo1.yProperty().greaterThan(startPoint.yProperty()))
                  .or(
                      startNodeForwardOrientedProperty
                          .not()
                          .and(arcTo1.yProperty().lessThan(startPoint.yProperty()))));

      final var arcTo2 = new ArcTo();

      final var lineTo2 = new LineTo();
      lineTo2.xProperty().bind(arcTo1.xProperty());
      lineTo2
          .yProperty()
          .bind(
              Bindings.when(startPoint.yProperty().lessThanOrEqualTo(endPoint.yProperty()))
                  .then(
                      Bindings.when(
                              destinationMinYProperty
                                  .subtract(sourceMaxYProperty)
                                  .greaterThanOrEqualTo(INTERNODE_DISTANCE))
                          .then(
                              sourceMaxYProperty
                                  .add(NODE_TO_CONNECTION_DISTANCE)
                                  .subtract(arcTo2.radiusXProperty()))
                          .otherwise(
                              Bindings.min(
                                  destinationMinYProperty
                                      .subtract(NODE_TO_CONNECTION_DISTANCE)
                                      .add(arcTo1.radiusXProperty()),
                                  sourceMinYProperty
                                      .subtract(NODE_TO_CONNECTION_DISTANCE)
                                      .add(arcTo1.radiusXProperty()))))
                  .otherwise(
                      Bindings.when(
                              sourceMinYProperty
                                  .subtract(destinationMaxYProperty)
                                  .greaterThanOrEqualTo(INTERNODE_DISTANCE))
                          .then(
                              sourceMinYProperty
                                  .subtract(NODE_TO_CONNECTION_DISTANCE)
                                  .add(arcTo2.radiusXProperty()))
                          .otherwise(
                              Bindings.min(
                                  destinationMinYProperty
                                      .subtract(NODE_TO_CONNECTION_DISTANCE)
                                      .add(arcTo2.radiusXProperty()),
                                  sourceMinYProperty
                                      .subtract(NODE_TO_CONNECTION_DISTANCE)
                                      .add(arcTo2.radiusXProperty())))));

      arcTo2
          .radiusXProperty()
          .bind(
              Bindings.when(startNodeForwardOrientedProperty.and(endNodeForwardOrientedProperty))
                  .then(
                      Bindings.min(
                          endPoint
                              .subtract(connectionDistance)
                              .horizontalDistance(startPoint.add(connectionDistance))
                              .divide(2.0),
                          ARC_RADIUS))
                  .otherwise(
                      Bindings.min(
                          endPoint
                              .add(connectionDistance)
                              .horizontalDistance(startPoint.subtract(connectionDistance))
                              .divide(2.0),
                          ARC_RADIUS)));
      arcTo2.radiusYProperty().bind(arcTo2.radiusXProperty());
      arcTo2
          .xProperty()
          .bind(
              Bindings.when(
                      startNodeForwardOrientedProperty.isEqualTo(endNodeForwardOrientedProperty))
                  .then(
                      Bindings.when(startNodeForwardOrientedProperty)
                          .then(lineTo2.xProperty().subtract(arcTo2.radiusXProperty()))
                          .otherwise(lineTo2.xProperty().add(arcTo2.radiusXProperty())))
                  .otherwise(
                      Bindings.when(startPoint.xProperty().lessThan(endPoint.xProperty()))
                          .then(lineTo2.xProperty().add(arcTo2.radiusXProperty()))
                          .otherwise(lineTo2.xProperty().subtract(arcTo2.radiusXProperty()))));
      arcTo2
          .yProperty()
          .bind(
              lineTo2
                  .yProperty()
                  .add(
                      Bindings.when(startNodeForwardOrientedProperty)
                          .then(
                              Bindings.when(arcTo1.sweepFlagProperty())
                                  .then(arcTo2.radiusXProperty())
                                  .otherwise(arcTo2.radiusXProperty().multiply(-1.0)))
                          .otherwise(
                              Bindings.when(arcTo1.sweepFlagProperty())
                                  .then(arcTo2.radiusXProperty().multiply(-1.0))
                                  .otherwise(arcTo2.radiusXProperty()))));
      arcTo2
          .sweepFlagProperty()
          .bind(
              startNodeForwardOrientedProperty
                  .isNotEqualTo(endNodeForwardOrientedProperty)
                  .and(endPoint.xProperty().greaterThan(startPoint.xProperty()))
                  .or(
                      startNodeForwardOrientedProperty
                          .and(endNodeForwardOrientedProperty)
                          .and(arcTo2.yProperty().greaterThan(lineTo2.yProperty())))
                  .or(
                      startNodeForwardOrientedProperty
                          .not()
                          .and(endNodeForwardOrientedProperty.not())
                          .and(arcTo2.yProperty().lessThan(lineTo2.yProperty()))));

      final var arcTo3 = new ArcTo();
      final var lineTo3 = new LineTo();
      lineTo3
          .xProperty()
          .bind(
              Bindings.when(
                      endNodeForwardOrientedProperty.isEqualTo(startNodeForwardOrientedProperty))
                  .then(
                      Bindings.when(endNodeForwardOrientedProperty)
                          .then(
                              endPoint
                                  .xProperty()
                                  .subtract(NODE_TO_CONNECTION_DISTANCE)
                                  .add(arcTo3.radiusXProperty()))
                          .otherwise(
                              endPoint
                                  .xProperty()
                                  .add(NODE_TO_CONNECTION_DISTANCE)
                                  .subtract(arcTo3.radiusXProperty())))
                  .otherwise(
                      Bindings.when(
                              startNodeForwardOrientedProperty
                                  .not()
                                  .and(startPoint.xProperty().lessThan(endPoint.xProperty())))
                          .then(
                              endPoint
                                  .xProperty()
                                  .subtract(NODE_TO_CONNECTION_DISTANCE)
                                  .subtract(arcTo3.radiusXProperty()))
                          .otherwise(
                              Bindings.when(
                                      startNodeForwardOrientedProperty.and(
                                          startPoint.xProperty().greaterThan(endPoint.xProperty())))
                                  .then(
                                      endPoint
                                          .xProperty()
                                          .add(NODE_TO_CONNECTION_DISTANCE)
                                          .add(arcTo3.radiusXProperty()))
                                  .otherwise(
                                      Bindings.when(endNodeForwardOrientedProperty)
                                          .then(
                                              endPoint
                                                  .xProperty()
                                                  .subtract(NODE_TO_CONNECTION_DISTANCE)
                                                  .add(arcTo3.radiusXProperty()))
                                          .otherwise(
                                              endPoint
                                                  .xProperty()
                                                  .add(NODE_TO_CONNECTION_DISTANCE)
                                                  .subtract(arcTo3.radiusXProperty()))))));
      lineTo3.yProperty().bind(arcTo2.yProperty());

      arcTo3.radiusXProperty().bind(arcTo2.radiusXProperty());
      arcTo3.radiusYProperty().bind(arcTo3.radiusXProperty());
      arcTo3
          .xProperty()
          .bind(
              Bindings.when(
                      startNodeForwardOrientedProperty.isEqualTo(endNodeForwardOrientedProperty))
                  .then(
                      Bindings.when(endNodeForwardOrientedProperty)
                          .then(lineTo3.xProperty().subtract(arcTo3.radiusXProperty()))
                          .otherwise(lineTo3.xProperty().add(arcTo3.radiusXProperty())))
                  .otherwise(
                      Bindings.when(startPoint.xProperty().lessThan(endPoint.xProperty()))
                          .then(lineTo3.xProperty().add(arcTo3.radiusXProperty()))
                          .otherwise(lineTo3.xProperty().subtract(arcTo3.radiusXProperty()))));
      arcTo3
          .yProperty()
          .bind(
              lineTo3
                  .yProperty()
                  .add(
                      Bindings.when(
                              startNodeForwardOrientedProperty.isEqualTo(
                                  endNodeForwardOrientedProperty))
                          .then(
                              Bindings.when(endNodeForwardOrientedProperty)
                                  .then(
                                      Bindings.when(arcTo3.sweepFlagProperty())
                                          .then(arcTo3.radiusXProperty().multiply(-1.0))
                                          .otherwise(arcTo3.radiusXProperty()))
                                  .otherwise(
                                      Bindings.when(arcTo3.sweepFlagProperty())
                                          .then(arcTo3.radiusXProperty())
                                          .otherwise(arcTo3.radiusXProperty().multiply(-1.0))))
                          .otherwise(arcTo3.radiusXProperty())));
      arcTo3
          .sweepFlagProperty()
          .bind(
              startNodeForwardOrientedProperty
                  .isNotEqualTo(endNodeForwardOrientedProperty)
                  .and(endPoint.xProperty().greaterThan(startPoint.xProperty()))
                  .or(
                      startNodeForwardOrientedProperty
                          .and(endNodeForwardOrientedProperty)
                          .and(lineTo3.yProperty().greaterThan(endPoint.yProperty())))
                  .or(
                      startNodeForwardOrientedProperty
                          .not()
                          .and(endNodeForwardOrientedProperty.not())
                          .and(lineTo3.yProperty().lessThan(endPoint.yProperty()))));

      final var arcTo4 = new ArcTo();

      final var lineTo4 = new LineTo();
      lineTo4
          .xProperty()
          .bind(
              Bindings.when(endNodeForwardOrientedProperty)
                  .then(endPoint.subtract(connectionDistance).xProperty())
                  .otherwise(endPoint.add(connectionDistance).xProperty()));
      lineTo4
          .yProperty()
          .bind(
              endPoint
                  .yProperty()
                  .add(
                      Bindings.when(
                              startNodeForwardOrientedProperty.isEqualTo(
                                  endNodeForwardOrientedProperty))
                          .then(
                              Bindings.when(startNodeForwardOrientedProperty)
                                  .then(
                                      Bindings.when(arcTo4.sweepFlagProperty())
                                          .then(arcTo4.radiusXProperty())
                                          .otherwise(arcTo4.radiusXProperty().multiply(-1.0)))
                                  .otherwise(
                                      Bindings.when(arcTo4.sweepFlagProperty())
                                          .then(arcTo4.radiusXProperty().multiply(-1.0))
                                          .otherwise(arcTo4.radiusXProperty())))
                          .otherwise(arcTo4.radiusXProperty().multiply(-1.0))));

      arcTo4.setRadiusX(ARC_RADIUS);
      arcTo4.radiusYProperty().bind(arcTo4.radiusXProperty());
      arcTo4
          .xProperty()
          .bind(
              Bindings.when(endNodeForwardOrientedProperty)
                  .then(lineTo4.xProperty().add(ARC_RADIUS))
                  .otherwise(lineTo4.xProperty().subtract(ARC_RADIUS)));
      arcTo4.yProperty().bind(endPoint.yProperty());
      arcTo4
          .sweepFlagProperty()
          .bind(
              Bindings.when(
                      startNodeForwardOrientedProperty
                          .isEqualTo(endNodeForwardOrientedProperty)
                          .and(
                              lineTo2
                                  .yProperty()
                                  .lessThan(startPoint.yProperty())
                                  .and(lineTo2.yProperty().lessThan(endPoint.yProperty()))))
                  .then(arcTo3.sweepFlagProperty())
                  .otherwise(arcTo1.sweepFlagProperty().not()));

      fiveSegmentPath
          .getElements()
          .addAll(
              initialMoveTo,
              lineTo1,
              arcTo1,
              lineTo2,
              arcTo2,
              lineTo3,
              arcTo3,
              lineTo4,
              arcTo4,
              finalLineTo);
    }
  }
}
