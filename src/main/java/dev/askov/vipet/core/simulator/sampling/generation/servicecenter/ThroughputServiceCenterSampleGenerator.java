package dev.askov.vipet.core.simulator.sampling.generation.servicecenter;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.ServerModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public final class ThroughputServiceCenterSampleGenerator extends ServiceCenterSampleGenerator {

  private int numberOfIdleServers;
  private double time;

  public ThroughputServiceCenterSampleGenerator(
      final int maxNumberOfSamples, final ServiceCenterModel serviceCenterModel) {
    super(maxNumberOfSamples, serviceCenterModel, true);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.THROUGHPUT;
  }

  @Override
  public boolean shouldTakeMeasurement() {
    final var oldNumberOfIdleServers = numberOfIdleServers;
    numberOfIdleServers =
        namedModel.getServerModels().stream()
            .mapToInt(server -> server.getCurrentState() == ServerModel.State.IDLE ? 1 : 0)
            .sum();

    return numberOfIdleServers > oldNumberOfIdleServers;
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
