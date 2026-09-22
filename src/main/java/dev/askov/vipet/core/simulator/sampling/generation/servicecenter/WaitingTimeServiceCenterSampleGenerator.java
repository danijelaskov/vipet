package dev.askov.vipet.core.simulator.sampling.generation.servicecenter;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public final class WaitingTimeServiceCenterSampleGenerator extends ServiceCenterSampleGenerator {

  public WaitingTimeServiceCenterSampleGenerator(
      final int maxNumberOfSamples, final ServiceCenterModel serviceCenterModel) {
    super(maxNumberOfSamples, serviceCenterModel);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.WAITING_TIME;
  }

  @Override
  public boolean shouldTakeMeasurement() {
    return namedModel.isServiceStarted();
  }

  @Override
  protected double getValue() {
    namedModel.resetServiceStarted();

    return namedModel.getLastWaitingTime();
  }

  @Override
  protected double getWeight() {
    return 1.0;
  }
}
