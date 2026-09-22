package dev.askov.vipet.mvc.models.network;

import dev.askov.vipet.mvc.models.network.queue.QueueModel;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceCenterModel extends NonTerminalNetworkNodeModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterModel.class);
  private static final Color DEFAULT_FILL_COLOR = Color.rgb(204, 230, 255);
  private static final Color DEFAULT_STROKE_COLOR = Color.rgb(51, 77, 179);
  private static final int MIN_NUMBER_OF_SERVERS = 1;
  private static final int DEFAULT_NUMBER_OF_SERVERS = MIN_NUMBER_OF_SERVERS;

  private final BooleanProperty incomingConnectionsProperty;
  private final BooleanProperty reachableProperty;
  private final IntegerProperty numberOfServersProperty;
  private final List<ServerModel> serverModels = new ArrayList<>();
  private final IntegerProperty initialNumberOfJobsProperty;
  private final IntegerProperty initialNumberOfJobsOnServersProperty =
      new SimpleIntegerProperty(this, "initialNumberOfJobsOnServers", 0);

  private QueueModel queueModel;
  private double lastServiceTime;
  private double lastWaitingTime;
  private JobModel lastJobModel;
  private boolean serviceStarted;
  private boolean serviceCompleted;

  public ServiceCenterModel(
      final long id,
      final String name,
      final double x,
      final double y,
      final boolean infiniteQueueCapacity,
      final int queueCapacity,
      final int numberOfServers) {
    super(id, name, x, y);

    if (numberOfServers < MIN_NUMBER_OF_SERVERS) {
      throw new IllegalArgumentException("Number of servers must be greater than 0");
    }

    incomingConnectionsProperty = new SimpleBooleanProperty(this, "incomingConnections", false);
    reachableProperty = new SimpleBooleanProperty(this, "reachable", false);
    numberOfServersProperty = new SimpleIntegerProperty(this, "numberOfServers", numberOfServers);

    queueModel = new QueueModel(infiniteQueueCapacity, queueCapacity);

    setFillColor(DEFAULT_FILL_COLOR);
    setStrokeColor(DEFAULT_STROKE_COLOR);

    stateProperty.addListener(
        (state, oldState, newState) -> {
          if (newState == State.SIMULATING) {
            queueModel.setVisualizationParameter(0.0);
            serverModels.forEach(serverModel -> serverModel.setVisualizationParameter(0.0));
          }
        });

    numberOfServersProperty.addListener(
        (value, oldValue, newValue) -> {
          LOGGER.debug(
              "Number of servers at service center \"{}\" changed from {} to {}",
              getName(),
              oldValue,
              newValue);
          if (newValue.intValue() < oldValue.intValue()) {
            serverModels.subList(newValue.intValue(), oldValue.intValue()).clear();
          } else {
            for (var i = oldValue.intValue(); i < newValue.intValue(); i++) {
              serverModels.add(new ServerModel(this));
            }
          }
        });
    for (var i = 0; i < numberOfServers; i++) {
      serverModels.add(new ServerModel(this));
    }

    initialNumberOfJobsProperty = new SimpleIntegerProperty(this, "initialNumberOfJobs", 0);
    initialNumberOfJobsProperty.addListener(
        (initialNumberOfJobs, oldInitialNumberOfJobs, newInitialNumberOfJobs) -> {
          if (newInitialNumberOfJobs.intValue() < 0) {
            throw new IllegalArgumentException(
                "Initial number of jobs must be non-negative but got %d"
                    .formatted(newInitialNumberOfJobs.intValue()));
          }
          if (!queueModel.isInfinite() && newInitialNumberOfJobs.intValue() > getCapacity()) {
            throw new IllegalArgumentException(
                "Initial number of jobs exceeds the capacity of the service center");
          }
          queueModel.setInitialNumberOfJobs(
              newInitialNumberOfJobs.intValue() >= getNumberOfServers()
                  ? newInitialNumberOfJobs.intValue() - getNumberOfServers()
                  : 0);
          initialNumberOfJobsOnServersProperty.set(
              newInitialNumberOfJobs.intValue() - queueModel.getInitialNumberOfJobs());
        });

    validProperty.bind(
        Bindings.createBooleanBinding(
            () -> {
              if (hasIncomingConnections() && !getConnectionModels().isEmpty()) {
                return (getConnectionModels().size() > 1
                        || !getConnectionModels().getFirst().getDestination().equals(this))
                    && isReachable();
              } else {
                return false;
              }
            },
            connectionsProperty(),
            incomingConnectionsProperty,
            reachableProperty));

    addRngListener();
  }

  public ServiceCenterModel(final long id) {
    this(id, "", 0.f, 0.f, true, QueueModel.DEFAULT_QUEUE_CAPACITY, DEFAULT_NUMBER_OF_SERVERS);
  }

  @SuppressWarnings("CopyConstructorMissesField")
  public ServiceCenterModel(final ServiceCenterModel serviceCenterModel) {
    super(serviceCenterModel);

    incomingConnectionsProperty =
        new SimpleBooleanProperty(
            this, "incomingConnections", serviceCenterModel.incomingConnectionsProperty.get());
    reachableProperty =
        new SimpleBooleanProperty(this, "reachable", serviceCenterModel.reachableProperty.get());
    numberOfServersProperty =
        new SimpleIntegerProperty(this, "numberOfServers", serviceCenterModel.getNumberOfServers());
    queueModel = new QueueModel(serviceCenterModel.getQueueModel());

    for (var i = 0; i < numberOfServersProperty.get(); i++) {
      serverModels.add(new ServerModel(this));
    }

    initialNumberOfJobsProperty =
        new SimpleIntegerProperty(
            this, "initialNumberOfJobs", serviceCenterModel.initialNumberOfJobsProperty.get());

    addRngListener();
  }

  public QueueModel getQueueModel() {
    return queueModel;
  }

  public void setQueueModel(final QueueModel queueModel) {
    this.queueModel = queueModel;
  }

  public boolean hasIncomingConnections() {
    return incomingConnectionsProperty.get();
  }

  public void setIncomingConnections(boolean incomingConnections) {
    incomingConnectionsProperty.set(incomingConnections);
  }

  public boolean isReachable() {
    return reachableProperty.get();
  }

  public void setReachable(boolean reachable) {
    reachableProperty.set(reachable);
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

  public boolean hasFreeServer() {
    return serverModels.stream()
        .anyMatch(serverModel -> serverModel.getCurrentState() == ServerModel.State.IDLE);
  }

  public ServerModel getFreeServerModel() {
    return serverModels.stream()
        .filter(serverModel -> serverModel.getCurrentState() == ServerModel.State.IDLE)
        .findFirst()
        .orElse(null);
  }

  public boolean hasFreeQueueSlot() {
    return queueModel.isInfinite()
        || queueModel.getCurrentNumberOfJobs() < queueModel.getCapacity();
  }

  public ServerModel getServerModel(final int index) {
    return serverModels.get(index);
  }

  public List<ServerModel> getServerModels() {
    return serverModels;
  }

  public void startService(
      final JobModel jobModel, final ServerModel serverModel, final double time) {
    serverModel.startService(jobModel);
    lastWaitingTime = time - jobModel.getLastArrivalTime();
    serviceStarted = true;
  }

  public void finishService(final ServerModel serverModel, final double time) {
    final var lastJobModel = serverModel.finishService(time);
    lastServiceTime = lastJobModel.getLastDepartureTime() - lastJobModel.getLastArrivalTime();
    serviceCompleted = true;
    this.lastJobModel = lastJobModel;
  }

  public double getLastWaitingTime() {
    return lastWaitingTime;
  }

  public double getLastServiceTime() {
    return lastServiceTime;
  }

  public boolean isServiceStarted() {
    return serviceStarted;
  }

  public void resetServiceStarted() {
    serviceStarted = false;
  }

  public boolean isServiceCompleted() {
    return serviceCompleted;
  }

  public void resetServiceCompleted() {
    serviceCompleted = false;
  }

  public JobModel getLastServicedJob() {
    return lastJobModel;
  }

  public void removeLastServicedJob() {
    lastJobModel = null;
  }

  public void addToQueue(final JobModel jobModel, final double time) {
    jobModel.setLastArrivalTime(time);
    queueModel.addJob(jobModel);
  }

  public IntegerProperty initialNumberOfJobsProperty() {
    return initialNumberOfJobsProperty;
  }

  public int getInitialNumberOfJobs() {
    return initialNumberOfJobsProperty.get();
  }

  public void setInitialNumberOfJobs(final int initialNumberOfJobs) {
    initialNumberOfJobsProperty.set(initialNumberOfJobs);
  }

  public long getCurrentNumberOfJobsInQueue() {
    return queueModel.getCurrentNumberOfJobs();
  }

  public long getCurrentNumberOfJobs() {
    return getCurrentNumberOfJobsInQueue()
        + serverModels.stream()
            .filter(serverModel -> serverModel.getCurrentState() == ServerModel.State.BUSY)
            .count();
  }

  public int getCapacity() {
    return getQueueModel().isInfinite()
        ? Integer.MAX_VALUE
        : queueModel.getCapacity() + getNumberOfServers();
  }

  @Override
  public String getType() {
    return "ServiceCenter";
  }

  private void addRngListener() {
    randomGeneratorObjectProperty.addListener(
        (observableRng, oldRng, newRng) ->
            queueModel.getSelectedQueueDiscipline().setRandomGenerator(newRng));
  }
}
