package dev.askov.vipet.mvc.views.network.connection;

import dev.askov.vipet.mvc.common.ObservablePoint2D;
import dev.askov.vipet.mvc.models.network.ConnectionModel;
import dev.askov.vipet.mvc.models.simulation.GuidedDiscreteEventDrivenSimulationModel;
import dev.askov.vipet.mvc.views.network.NetworkElement;
import javafx.animation.PathTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Connection extends NetworkElement {

  public static final double TRAVELLING_JOB_CIRCLE_RADIUS = 5.0; // In pixels
  public static final double TRAVELLING_JOB_ANIMATION_DURATION = 1.0; // In seconds

  protected final ObservablePoint2D startPoint = new ObservablePoint2D();
  protected final ObservablePoint2D endPoint = new ObservablePoint2D();
  protected final ObjectProperty<ConnectionModel.Type> typeProperty =
      new SimpleObjectProperty<>(this, "type", ConnectionModel.Type.MULTI_SEGMENT);

  private final ReadOnlyObjectWrapper<AbstractPath> pathPropertyWrapper =
      new ReadOnlyObjectWrapper<>(this, "path");

  public Connection(final ConnectionModel connectionModel) {
    typeProperty.addListener((type, oldType, newType) -> recreatePath(connectionModel));
    recreatePath(connectionModel);
    stateProperty.addListener(
        (state, oldState, newState) -> {
          if (newState == State.HIGHLIGHTED
              || newState == State.SELECTED
              || newState == State.MOVING) {
            toFront();
          }
        });
  }

  private void recreatePath(final ConnectionModel connectionModel) {
    final var path =
        switch (connectionModel.getType()) {
          case SHORTEST -> new StraightLinePath();
          case BEZIER -> new BezierLinePath();
          case MULTI_SEGMENT ->
              new MultiSegmentPathWithArcs(
                  connectionModel.getSource().maxYProperty(),
                  connectionModel.getSource().minYProperty(),
                  connectionModel.getDestination().maxYProperty(),
                  connectionModel.getDestination().minYProperty());
        };

    path.getStartPoint().xProperty().bind(startPoint.xProperty());
    path.getStartPoint().yProperty().bind(startPoint.yProperty());
    path.startNodeForwardOrientedProperty()
        .bind(connectionModel.getSource().forwardOrientedProperty());
    path.getEndPoint().xProperty().bind(endPoint.xProperty());
    path.getEndPoint().yProperty().bind(endPoint.yProperty());
    path.endNodeForwardOrientedProperty()
        .bind(connectionModel.getDestination().forwardOrientedProperty());

    normalStateStrokeColorProperty.set(Color.BLACK);
    strokeColorProperty.bind(
        Bindings.when(stateProperty.isNotEqualTo(State.NORMAL))
            .then(Bindings.createObjectBinding(() -> stateProperty.get().getColor(), stateProperty))
            .otherwise(normalStateStrokeColorProperty));

    path.strokeProperty().bind(strokeColorProperty);
    path.visibleProperty().bind(Bindings.not(stateProperty.isEqualTo(State.OVERLAPPING)));

    getChildren().clear();
    getChildren().add(path);

    pathPropertyWrapper.set(path);
  }

  public ObservablePoint2D getStartPoint() {
    return startPoint;
  }

  public ObservablePoint2D getEndPoint() {
    return endPoint;
  }

  public ObjectProperty<ConnectionModel.Type> typeProperty() {
    return typeProperty;
  }

  public ConnectionModel.Type getType() {
    return typeProperty.get();
  }

  public void setType(final ConnectionModel.Type type) {
    typeProperty.set(type);
  }

  public AbstractPath getPath() {
    return pathPropertyWrapper.get();
  }

  public PathTransition createTravellingJobAnimation(
      final Color fillColor,
      final Color strokeColor,
      final GuidedDiscreteEventDrivenSimulationModel guidedDiscreteEventDrivenSimulationModel) {
    final var circle = new Circle(TRAVELLING_JOB_CIRCLE_RADIUS);
    final var parent = (Group) getParent();

    circle.setFill(fillColor);
    circle.setStroke(strokeColor);
    parent.getChildren().add(circle);
    circle.toFront();

    final var transition = new PathTransition();

    transition.setNode(circle);
    transition.setPath(getPath());
    transition.setDuration(Duration.seconds(TRAVELLING_JOB_ANIMATION_DURATION));
    transition.setAutoReverse(false);
    transition.setOnFinished(
        event -> {
          parent.getChildren().remove(circle);
          guidedDiscreteEventDrivenSimulationModel.setAnimatedConnection(null);
        });

    return transition;
  }
}
