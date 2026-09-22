package dev.askov.vipet.core.simulator;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import javafx.concurrent.Task;

public abstract class Simulation extends Task<Void> {

  protected final long id;
  protected final NetworkModel networkModel;
  protected final double maxDuration; // In seconds
  protected final double maxSimulatedTime; // In seconds
  protected final int maxSampleSize;

  public Simulation(
      final int id,
      final NetworkModel networkModel,
      final double maxDuration,
      final double maxSimulatedTime,
      final int maxSampleSize) {
    this.id = id;
    this.networkModel = networkModel;
    this.maxDuration = maxDuration;
    this.maxSimulatedTime = maxSimulatedTime;
    this.maxSampleSize = maxSampleSize;
  }

  public long getID() {
    return id;
  }

  public NetworkModel getNetwork() {
    return networkModel;
  }

  public int getMaxSampleSize() {
    return maxSampleSize;
  }
}
