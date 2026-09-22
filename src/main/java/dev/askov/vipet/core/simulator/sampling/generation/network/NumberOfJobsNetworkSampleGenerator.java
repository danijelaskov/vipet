package dev.askov.vipet.core.simulator.sampling.generation.network;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.NetworkModel;

public final class NumberOfJobsNetworkSampleGenerator extends NetworkSampleGenerator {

  private long lastNumberOfJobs;
  private double lastTime;

  public NumberOfJobsNetworkSampleGenerator(
      final int maxNumberOfSamples, final NetworkModel networkModel) {
    super(maxNumberOfSamples, networkModel);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.NUM_JOBS;
  }

  @Override
  public boolean shouldTakeMeasurement() {
    return namedModel.getCurrentNumberOfJobs() != lastNumberOfJobs;
  }

  @Override
  protected double getValue() {
    final double value = lastNumberOfJobs;
    lastNumberOfJobs = namedModel.getCurrentNumberOfJobs();

    return namedModel.isOpen() ? value : lastNumberOfJobs;
  }

  @Override
  protected double getWeight() {
    final var weight = namedModel.isOpen() ? currentTime - lastTime : 1;

    lastTime = currentTime;

    return weight;
  }
}
