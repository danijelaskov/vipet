package dev.askov.vipet.core.performancemeasures;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;

public abstract class PerformanceMeasuresProvider extends Service<PerformanceMeasureCollection> {

  private long startTime;
  private long endTime;

  protected final ObjectProperty<NetworkModel> networkProperty =
      new SimpleObjectProperty<>(this, "network");

  public PerformanceMeasuresProvider(final NetworkModel networkModel) {
    networkProperty.set(networkModel);
  }

  public NetworkModel getNetworkModel() {
    return networkProperty.get();
  }

  public void setNetwork(NetworkModel networkModel) {
    networkProperty.set(networkModel);
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(final long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(final long endTime) {
    this.endTime = endTime;
  }

  public long getElapsedTime() {
    return endTime - startTime;
  }
}
