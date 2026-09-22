package dev.askov.vipet.core.simulator.sampling.generation.servicecenter;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public final class NumberOfJobsServiceCenterSampleGenerator extends ServiceCenterSampleGenerator {

  private long lastNumberOfJobs;
  private double time;

  public NumberOfJobsServiceCenterSampleGenerator(
      final int maxNumberOfSamples, final ServiceCenterModel serviceCenterModel) {
    super(maxNumberOfSamples, serviceCenterModel);
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

    return value;
  }

  @Override
  protected double getWeight() {
    final var weight = currentTime - time;
    time = currentTime;

    return weight;
  }
}
