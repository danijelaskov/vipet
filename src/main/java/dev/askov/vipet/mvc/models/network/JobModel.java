package dev.askov.vipet.mvc.models.network;

public final class JobModel {

  private static long lastId = 0;

  private final long id;

  private double networkEnterTime;
  private double networkExitTime;
  private double lastArrivalTime;
  private double lastDepartureTime;

  public JobModel() {
    id = lastId++;
  }

  public long getId() {
    return id;
  }

  public void setNetworkEnterTime(final double networkEnterTime) {
    this.networkEnterTime = networkEnterTime;
  }

  public double getNetworkEnterTime() {
    return networkEnterTime;
  }

  public void setNetworkExitTime(final double networkExitTime) {
    this.networkExitTime = networkExitTime;
  }

  public double getNetworkExitTime() {
    return networkExitTime;
  }

  public void setLastArrivalTime(final double lastArrivalTime) {
    this.lastArrivalTime = lastArrivalTime;
  }

  public double getLastArrivalTime() {
    return lastArrivalTime;
  }

  public void setLastDepartureTime(final double lastDepartureTime) {
    this.lastDepartureTime = lastDepartureTime;
  }

  public double getLastDepartureTime() {
    return lastDepartureTime;
  }

  @Override
  public String toString() {
    return "Job [ID=%d, networkEnterTime=%f, networkExitTime=%f]"
        .formatted(id, networkEnterTime, networkExitTime);
  }
}
