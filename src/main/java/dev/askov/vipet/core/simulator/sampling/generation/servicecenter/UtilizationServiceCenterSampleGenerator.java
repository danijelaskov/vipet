package dev.askov.vipet.core.simulator.sampling.generation.servicecenter;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.ServerModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public final class UtilizationServiceCenterSampleGenerator extends ServiceCenterSampleGenerator {

  private double lastSample;
  private double currentSample;
  private double time;

  public UtilizationServiceCenterSampleGenerator(
      final int maxNumberOfSamples, final ServiceCenterModel serviceCenterModel) {
    super(maxNumberOfSamples, serviceCenterModel);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.UTILIZATION;
  }

  @Override
  public boolean shouldTakeMeasurement() {
    currentSample =
        namedModel.getServerModels().stream()
                .filter(serverModel -> serverModel.getCurrentState() == ServerModel.State.BUSY)
                .count()
            / (double) namedModel.getServerModels().size();

    return currentSample != lastSample;
  }

  @Override
  protected double getValue() {
    var value = lastSample;
    lastSample = currentSample;

    return value;
  }

  @Override
  protected double getWeight() {
    var weight = currentTime - time;
    time = currentTime;

    return weight;
  }
}
