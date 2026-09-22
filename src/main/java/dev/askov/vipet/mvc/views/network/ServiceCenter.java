package dev.askov.vipet.mvc.views.network;

import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.mvc.views.network.connection.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ServiceCenter extends NetworkNode {

  public static final double SERVICE_CENTER_WIDTH = 140.0;
  public static final double SINGLE_SERVER_HEIGHT = 40.0;

  private static final double SERVER_RADIUS = 0.5 * SINGLE_SERVER_HEIGHT;
  private static final double INTER_SERVER_DISTANCE = 0.2 * SERVER_RADIUS;
  private static final double QUEUE_SERVERS_DISTANCE = 3.0 * INTER_SERVER_DISTANCE;

  private static final double QUEUE_VISUALIZATION_RECTANGLE_PADDING = 2.0;
  private static final double SERVER_VISUALIZATION_ARC_PADDING = 2.0;

  private static final int CENTRAL_SERVICE_CENTER_ICON_SIZE = 16;
  private static final double CENTRAL_SERVICE_CENTER_HORIZONTAL_DISTANCE = 18.0;
  private static final double CENTRAL_SERVICE_CENTER_VERTICAL_DISTANCE = 14.0;

  private final ObjectProperty<Paint> visualisationPaintProperty =
      new SimpleObjectProperty<>(this, "visualisationPaint");
  private final IntegerProperty numberOfServersProperty =
      new SimpleIntegerProperty(this, "numberOfServers");
  private final BooleanProperty infiniteQueueCapacityProperty =
      new SimpleBooleanProperty(this, "infiniteQueueCapacity", true);
  private final IntegerProperty queueCapacityProperty =
      new SimpleIntegerProperty(this, "queueCapacity", 0);
  private final DoubleProperty queueVisualisationParameterProperty =
      new SimpleDoubleProperty(this, "queueVisualisationParameter", 0.0);
  private final IntegerProperty jobVisualisationParameterProperty =
      new SimpleIntegerProperty(this, "jobVisualisationParameter", 0);
  private final List<DoubleProperty> serverVisualisationParameters = new ArrayList<>();

  private final Rectangle queue = new Rectangle();
  private final List<Rectangle> jobs = new ArrayList<>();
  private final List<javafx.scene.shape.Path> leftSidePaths = new ArrayList<>();
  private final Group serverGroup = new Group();
  private final List<javafx.scene.shape.Path> rightSidePaths = new ArrayList<>();
  private final FontIcon centralServiceCenterIcon =
      new FontIcon("mdi2c-crown:%s".formatted(CENTRAL_SERVICE_CENTER_ICON_SIZE));

  private boolean isFirstInfiniteQueueAnimation = true;
  private boolean isFirstFiniteQueueAnimation = true;

  private final ChangeListener<Number> infiniteQueueJobVisualisationParameterListener =
      (jobVisualisationParameter, oldJobVisualisationParameter, newJobVisualisationParameter) -> {
        final var delay =
            Duration.seconds(
                (isFirstInfiniteQueueAnimation && getState() != State.SIMULATING)
                        || newJobVisualisationParameter.intValue() == 0
                    ? 0
                    : (newJobVisualisationParameter.intValue()
                            > oldJobVisualisationParameter.intValue()
                        ? Connection.TRAVELLING_JOB_ANIMATION_DURATION
                        : 0));
        isFirstInfiniteQueueAnimation = false;
        runAfterDelay(
            () -> {
              if (getState() != State.SIMULATING) {
                return;
              }

              getChildren().removeAll(jobs);
              jobs.clear();

              for (var position = 0;
                  position < newJobVisualisationParameter.intValue();
                  position++) {
                final var job = createJob(newJobVisualisationParameter, position);

                job.fillProperty().bind(visualisationPaintProperty);

                getChildren().add(job);
                jobs.add(job);
              }
            },
            delay);
      };
  private final ChangeListener<Number> finiteQueueJobVisualisationParameterListener =
      (visualisationParameter, oldJobVisualisationParameter, newJobVisualisationParameter) -> {
        final var delay =
            Duration.seconds(
                (isFirstFiniteQueueAnimation && getState() != State.SIMULATING)
                        || newJobVisualisationParameter.intValue() == 0
                    ? 0
                    : (newJobVisualisationParameter.intValue()
                            > oldJobVisualisationParameter.intValue()
                        ? Connection.TRAVELLING_JOB_ANIMATION_DURATION
                        : 0));
        isFirstFiniteQueueAnimation = false;
        runAfterDelay(
            () -> {
              if (getState() != State.SIMULATING) {
                return;
              }

              for (var i = 0; i < jobs.size(); i++) {
                final var job = jobs.get(i);

                if (i < newJobVisualisationParameter.intValue()) {
                  job.setFill(visualisationPaintProperty.get());
                } else {
                  job.setFill(Color.TRANSPARENT);
                }
              }
            },
            delay);
      };
  private final ChangeListener<Number> queueCapacityListener =
      (queueCapacity, oldQueueCapacity, newQueueCapacity) -> {
        getChildren().removeAll(jobs);
        jobs.clear();

        for (var position = 0; position < newQueueCapacity.intValue(); position++) {
          var job = createJob(newQueueCapacity, position);

          job.setFill(Color.TRANSPARENT);

          getChildren().add(job);
          jobs.add(job);
        }

        jobVisualisationParameterProperty.removeListener(
            finiteQueueJobVisualisationParameterListener);
        jobVisualisationParameterProperty.addListener(finiteQueueJobVisualisationParameterListener);
      };

  public ServiceCenter() {
    stateProperty.addListener(
        (state, oldState, newState) -> {
          if (newState != State.SIMULATING) {
            isFirstInfiniteQueueAnimation = true;
            isFirstFiniteQueueAnimation = true;

            getChildren().removeAll(jobs);
            jobs.clear();
          }
        });

    widthProperty.set(SERVICE_CENTER_WIDTH);
    heightProperty.bind(
        numberOfServersProperty
            .multiply(SINGLE_SERVER_HEIGHT)
            .add(numberOfServersProperty.subtract(1).multiply(INTER_SERVER_DISTANCE)));

    visualisationPaintProperty.bind(
        Bindings.createObjectBinding(
            () -> {
              final var color = UIUtil.getForegroundColor(fillColorProperty.get());

              return new LinearGradient(
                  0.0,
                  0.0,
                  0.0,
                  1.0,
                  true,
                  CycleMethod.NO_CYCLE,
                  new Stop(0.0, color.brighter()),
                  new Stop(1.0, color.darker()));
            },
            fillColorProperty));

    queue.fillProperty().bind(gradientProperty);
    queue.strokeProperty().bind(strokeColorProperty);

    queue.setHeight(SINGLE_SERVER_HEIGHT);
    queue
        .widthProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> {
                  if (getNumberOfServers() > 1) {
                    return SERVICE_CENTER_WIDTH
                        - SINGLE_SERVER_HEIGHT
                        - 1.5 * QUEUE_SERVERS_DISTANCE;
                  } else {
                    return SERVICE_CENTER_WIDTH - SINGLE_SERVER_HEIGHT - QUEUE_SERVERS_DISTANCE;
                  }
                },
                numberOfServersProperty));

    queue
        .translateXProperty()
        .bind(
            Bindings.when(forwardOrientedProperty)
                .then(0.0)
                .otherwise(
                    Bindings.when(numberOfServersProperty.isEqualTo(1))
                        .then(2 * SERVER_RADIUS + QUEUE_SERVERS_DISTANCE)
                        .otherwise(2 * SERVER_RADIUS + 1.5 * QUEUE_SERVERS_DISTANCE)));
    queue
        .translateYProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> 0.5 * getTotalServerGroupHeight() - 0.5 * queue.getHeight(),
                numberOfServersProperty));

    serverGroup
        .translateXProperty()
        .bind(
            Bindings.when(forwardOrientedProperty)
                .then(queue.widthProperty().add(QUEUE_SERVERS_DISTANCE + SERVER_RADIUS))
                .otherwise(
                    Bindings.when(numberOfServersProperty.isEqualTo(1))
                        .then(SERVER_RADIUS)
                        .otherwise(0.5 * QUEUE_SERVERS_DISTANCE + SERVER_RADIUS)));
    serverGroup.setTranslateY(0.5 * SINGLE_SERVER_HEIGHT);

    queue
        .effectProperty()
        .bind(Bindings.createObjectBinding(() -> stateProperty.get().getEffect(), stateProperty));
    serverGroup
        .effectProperty()
        .bind(Bindings.createObjectBinding(() -> stateProperty.get().getEffect(), stateProperty));

    final var queueVisualisationRectangle = new Rectangle();

    queueVisualisationRectangle
        .widthProperty()
        .bind(
            queueVisualisationParameterProperty.multiply(
                queue.widthProperty().subtract(2 * QUEUE_VISUALIZATION_RECTANGLE_PADDING)));
    queueVisualisationRectangle
        .heightProperty()
        .bind(queue.heightProperty().subtract(2 * QUEUE_VISUALIZATION_RECTANGLE_PADDING));

    queueVisualisationRectangle
        .translateXProperty()
        .bind(
            Bindings.when(forwardOrientedProperty)
                .then(
                    queue
                        .translateXProperty()
                        .add(queue.widthProperty())
                        .subtract(queueVisualisationRectangle.widthProperty())
                        .subtract(QUEUE_VISUALIZATION_RECTANGLE_PADDING))
                .otherwise(queue.translateXProperty().add(QUEUE_VISUALIZATION_RECTANGLE_PADDING)));
    queueVisualisationRectangle
        .translateYProperty()
        .bind(queue.translateYProperty().add(QUEUE_VISUALIZATION_RECTANGLE_PADDING));

    queueVisualisationRectangle.fillProperty().bind(visualisationPaintProperty);

    infiniteQueueCapacityProperty.addListener(
        (infiniteQueueCapacity, hadInfiniteQueueCapacity, hasInfiniteQueueCapacity) ->
            updateJobVisualisationParameterPropertyListener(hasInfiniteQueueCapacity));
    updateJobVisualisationParameterPropertyListener(getInfiniteQueueCapacity());

    getChildren().addAll(queue, queueVisualisationRectangle, serverGroup);

    numberOfServersProperty.addListener(
        (numberOfServers, oldNumberOfServers, newNumberOfServers) ->
            updateServersAndConnectionsToQueue());
    forwardOrientedProperty.addListener(
        (forwardOriented, wasForwardOriented, isForwardOriented) ->
            updateServersAndConnectionsToQueue());
    updateServersAndConnectionsToQueue();

    centralServiceCenterIcon.iconColorProperty().bind(fillColorProperty());
    centralServiceCenterIcon.strokeProperty().bind(strokeColorProperty());
    centralServiceCenterIcon
        .translateXProperty()
        .bind(
            getNameLabel()
                .translateXProperty()
                .subtract(CENTRAL_SERVICE_CENTER_HORIZONTAL_DISTANCE));
    centralServiceCenterIcon
        .translateYProperty()
        .bind(getNameLabel().translateYProperty().add(CENTRAL_SERVICE_CENTER_VERTICAL_DISTANCE));
    getChildren().add(centralServiceCenterIcon);
  }

  public IntegerProperty numberOfServersProperty() {
    return numberOfServersProperty;
  }

  public int getNumberOfServers() {
    return numberOfServersProperty.get();
  }

  public void setNumberOfServers(int numberOfServers) {
    numberOfServersProperty.set(numberOfServers);
  }

  public boolean getInfiniteQueueCapacity() {
    return infiniteQueueCapacityProperty.get();
  }

  public DoubleProperty queueVisualisationParameterProperty() {
    return queueVisualisationParameterProperty;
  }

  public IntegerProperty jobVisualisationParameterProperty() {
    return jobVisualisationParameterProperty;
  }

  private double getTotalServerGroupHeight() {
    return getNumberOfServers() * SINGLE_SERVER_HEIGHT
        + (getNumberOfServers() - 1) * INTER_SERVER_DISTANCE;
  }

  private void updateServersAndConnectionsToQueue() {
    serverGroup.getChildren().clear();

    var oldNumberOfServers = serverVisualisationParameters.size();
    for (var i = 0; i < getNumberOfServers(); i++) {
      final var server = new Circle();

      server.setRadius(SERVER_RADIUS);
      server.setCenterX(0.0);
      server.setCenterY(i * (2 * SERVER_RADIUS + INTER_SERVER_DISTANCE));
      server.fillProperty().bind(fillColorProperty());
      server.strokeProperty().bind(strokeColorProperty());
      server.fillProperty().bind(gradientProperty);

      final var arc = new Arc();

      arc.radiusXProperty()
          .bind(server.radiusProperty().subtract(SERVER_VISUALIZATION_ARC_PADDING));
      arc.radiusYProperty()
          .bind(server.radiusProperty().subtract(SERVER_VISUALIZATION_ARC_PADDING));
      arc.centerXProperty().bind(server.centerXProperty());
      arc.centerYProperty().bind(server.centerYProperty());
      arc.setStartAngle(90.0);
      arc.typeProperty().set(ArcType.ROUND);
      arc.fillProperty().bind(visualisationPaintProperty);

      if (i >= oldNumberOfServers) {
        serverVisualisationParameters.add(
            new SimpleDoubleProperty(this, "serverVisualisationParameter", 0.0));
      }

      arc.lengthProperty().bind(serverVisualisationParameters.get(i).multiply(-360.0));

      serverGroup.getChildren().addAll(server, arc);
    }

    if (oldNumberOfServers > getNumberOfServers()) {
      serverVisualisationParameters.subList(getNumberOfServers(), oldNumberOfServers).clear();
    }

    getChildren().removeAll(leftSidePaths);
    leftSidePaths.forEach(getChildren()::remove);

    getChildren().removeAll(rightSidePaths);
    rightSidePaths.forEach(getChildren()::remove);

    final var aX = isForwardOriented() ? queue.getWidth() : 0.0;
    final var aY = 0.5 * getTotalServerGroupHeight();

    final var bX = aX + (isForwardOriented() ? 0.5 * QUEUE_SERVERS_DISTANCE : 0.0);

    var cY = serverGroup.getTranslateY();

    final var dX =
        isForwardOriented() || getNumberOfServers() > 1 ? bX + 0.5 * QUEUE_SERVERS_DISTANCE : 0.0;
    var dY = cY;

    final var eX = dX + 2 * SERVER_RADIUS;
    var eY = dY;

    final var fX = eX + 0.5 * QUEUE_SERVERS_DISTANCE;
    var fY = eY;

    var leftSidePath = new javafx.scene.shape.Path();
    if (isForwardOriented()) {
      leftSidePath.getElements().add(new MoveTo(aX, aY));
      leftSidePath.getElements().add(new LineTo(bX, aY));
    }

    if (getNumberOfServers() > 1) {
      leftSidePath.getElements().add(new MoveTo(bX, cY));
      leftSidePath
          .getElements()
          .add(
              new LineTo(
                  bX,
                  cY
                      + (getNumberOfServers() - 1)
                          * (SINGLE_SERVER_HEIGHT + INTER_SERVER_DISTANCE)));
    }

    leftSidePath.strokeProperty().bind(strokeColorProperty());
    getChildren().add(leftSidePath);
    leftSidePaths.add(leftSidePath);

    var rightSidePath = new javafx.scene.shape.Path();
    if (!isForwardOriented()) {
      rightSidePath.getElements().add(new MoveTo(fX, aY));
      rightSidePath.getElements().add(new LineTo(fX + 0.5 * QUEUE_SERVERS_DISTANCE, aY));
    }
    if (getNumberOfServers() > 1) {
      rightSidePath.getElements().add(new MoveTo(fX, fY));
      rightSidePath
          .getElements()
          .add(
              new LineTo(
                  fX,
                  fY
                      + (getNumberOfServers() - 1)
                          * (SINGLE_SERVER_HEIGHT + INTER_SERVER_DISTANCE)));
    }

    rightSidePath.strokeProperty().bind(strokeColorProperty());
    getChildren().add(rightSidePath);
    rightSidePaths.add(rightSidePath);

    if (isForwardOriented()) {
      for (var i = 0; i < getNumberOfServers(); i++) {
        leftSidePath = new javafx.scene.shape.Path();

        leftSidePath.getElements().add(new MoveTo(bX, cY));
        leftSidePath.getElements().add(new LineTo(dX, dY));

        leftSidePath.strokeProperty().bind(strokeColorProperty());
        getChildren().add(leftSidePath);
        leftSidePaths.add(leftSidePath);

        if (getNumberOfServers() > 1) {
          rightSidePath = new javafx.scene.shape.Path();

          rightSidePath.getElements().add(new MoveTo(eX, eY));
          rightSidePath.getElements().add(new LineTo(fX, fY));

          rightSidePath.strokeProperty().bind(strokeColorProperty());
          getChildren().add(rightSidePath);
          rightSidePaths.add(rightSidePath);
        }

        cY += SINGLE_SERVER_HEIGHT + INTER_SERVER_DISTANCE;
        dY = cY;
        eY = cY;
        fY = cY;
      }
    } else {
      for (var i = 0; i < getNumberOfServers(); i++) {
        rightSidePath = new javafx.scene.shape.Path();

        rightSidePath.getElements().add(new MoveTo(eX, eY));
        rightSidePath.getElements().add(new LineTo(fX, fY));

        rightSidePath.strokeProperty().bind(strokeColorProperty());
        getChildren().add(rightSidePath);
        rightSidePaths.add(rightSidePath);

        if (getNumberOfServers() > 1) {
          leftSidePath = new javafx.scene.shape.Path();

          leftSidePath.getElements().add(new MoveTo(bX, cY));
          leftSidePath.getElements().add(new LineTo(dX, dY));

          leftSidePath.strokeProperty().bind(strokeColorProperty());
          getChildren().add(leftSidePath);
          leftSidePaths.add(leftSidePath);
        }

        cY += SINGLE_SERVER_HEIGHT + INTER_SERVER_DISTANCE;
        dY = cY;
        eY = cY;
        fY = cY;
      }
    }
  }

  public int getServerVisualisationParametersSize() {
    return serverVisualisationParameters.size();
  }

  public DoubleProperty getServerVisualisationParameterProperty(final int serverIndex) {
    return serverVisualisationParameters.get(serverIndex);
  }

  public void updateJobVisualisationParameterPropertyListener(final boolean hasInfiniteCapacity) {
    if (hasInfiniteCapacity) {
      jobVisualisationParameterProperty.removeListener(
          infiniteQueueJobVisualisationParameterListener);
      jobVisualisationParameterProperty.addListener(infiniteQueueJobVisualisationParameterListener);
    } else {
      queueCapacityProperty.removeListener(queueCapacityListener);
      queueCapacityProperty.addListener(queueCapacityListener);
    }
  }

  private void runAfterDelay(final Runnable action, final Duration duration) {
    final var pauseTransition = new PauseTransition(duration);

    pauseTransition.setOnFinished(event -> action.run());
    pauseTransition.play();
  }

  private Rectangle createJob(final Number newQueueCapacity, final int position) {
    final var job = new Rectangle();

    job.widthProperty()
        .bind(
            queue
                .widthProperty()
                .subtract(2 * QUEUE_VISUALIZATION_RECTANGLE_PADDING)
                .subtract((newQueueCapacity.intValue() - 1) * QUEUE_VISUALIZATION_RECTANGLE_PADDING)
                .divide(newQueueCapacity.intValue()));
    job.heightProperty()
        .bind(queue.heightProperty().subtract(2 * QUEUE_VISUALIZATION_RECTANGLE_PADDING));

    job.translateXProperty()
        .bind(
            queue
                .translateXProperty()
                .add(
                    job.widthProperty()
                        .add(QUEUE_VISUALIZATION_RECTANGLE_PADDING)
                        .multiply(position)
                        .add(QUEUE_VISUALIZATION_RECTANGLE_PADDING)));
    job.translateYProperty()
        .bind(queue.translateYProperty().add(QUEUE_VISUALIZATION_RECTANGLE_PADDING));

    return job;
  }

  public FontIcon getCentralServiceCenterIcon() {
    return centralServiceCenterIcon;
  }
}
