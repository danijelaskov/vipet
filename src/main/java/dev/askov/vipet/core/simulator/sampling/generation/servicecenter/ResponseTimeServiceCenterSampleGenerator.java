package dev.askov.vipet.core.simulator.sampling.generation.servicecenter;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public final class ResponseTimeServiceCenterSampleGenerator extends ServiceCenterSampleGenerator {

  public ResponseTimeServiceCenterSampleGenerator(
      final int maxNumberOfSamples, final ServiceCenterModel serviceCenterModel) {
    super(maxNumberOfSamples, serviceCenterModel);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.RESPONSE_TIME;
  }

  @Override
  protected boolean shouldTakeMeasurement() {
    return namedModel.isServiceCompleted();
  }

  @Override
  public double getValue() {
    namedModel.resetServiceCompleted();

    return namedModel.getLastServiceTime();
  }

  @Override
  public double getWeight() {
    return 1.0;
  }
}
