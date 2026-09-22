package dev.askov.vipet.mvc.models.network;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public final class ServerModel {

  public enum State {
    IDLE,
    BUSY
  }

  private final DoubleProperty visualizationParameterProperty =
      new SimpleDoubleProperty(this, "visualizationParameter", 0.0);

  private final ServiceCenterModel serviceCenterModel;
  private State currentState;
  private JobModel currentJobModel;

  public ServerModel(final ServiceCenterModel serviceCenterModel) {
    this.serviceCenterModel = serviceCenterModel;
    currentState = State.IDLE;
  }

  public DoubleProperty visualizationParameterProperty() {
    return visualizationParameterProperty;
  }

  public void setVisualizationParameter(final double serverUtilization) {
    visualizationParameterProperty.set(serverUtilization);
  }

  public ServiceCenterModel getServiceCenter() {
    return serviceCenterModel;
  }

  public State getCurrentState() {
    return currentState;
  }

  public void startService(final JobModel jobModel) {
    currentJobModel = jobModel;
    currentState = State.BUSY;
  }

  public JobModel finishService(final double time) {
    currentJobModel.setLastDepartureTime(time);
    currentState = State.IDLE;

    return currentJobModel;
  }

  @Override
  public String toString() {
    return (serviceCenterModel.getServerModels().indexOf(this) + 1)
        + "@"
        + serviceCenterModel.getName();
  }
}
