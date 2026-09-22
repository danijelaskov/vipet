package dev.askov.vipet.core.simulator.sampling.generation.network;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServerModel;

public final class ThroughputNetworkSampleGenerator extends NetworkSampleGenerator {

  private long value;
  private double time;

  public ThroughputNetworkSampleGenerator(
      final int maxNumberOfSamples, final NetworkModel networkModel) {
    super(maxNumberOfSamples, networkModel, true);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.THROUGHPUT;
  }

  @Override
  public boolean shouldTakeMeasurement() {
    if (namedModel.isOpen()) {
      final var oldNumberOfJobs = value;
      value = namedModel.getCurrentNumberOfJobs();

      return value > oldNumberOfJobs;
    } else {
      final var oldNumberOfIdleServers = value;
      value =
          namedModel.getCentralServiceCenterModel().getServerModels().stream()
              .mapToInt(server -> server.getCurrentState() == ServerModel.State.IDLE ? 1 : 0)
              .sum();

      return value > oldNumberOfIdleServers;
    }
  }

  @Override
  protected double getValue() {
    final var lastTime = this.time;
    this.time = currentTime;

    return currentTime - lastTime;
  }

  @Override
  protected double getWeight() {
    return 1.0;
  }
}
