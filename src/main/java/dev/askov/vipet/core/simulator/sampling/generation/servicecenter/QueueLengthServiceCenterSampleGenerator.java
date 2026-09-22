package dev.askov.vipet.core.simulator.sampling.generation.servicecenter;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public final class QueueLengthServiceCenterSampleGenerator extends ServiceCenterSampleGenerator {

  private long lastQueueLength;
  private long currentQueueLength;
  private double time;

  public QueueLengthServiceCenterSampleGenerator(
      final int maxNumberOfSamples, final ServiceCenterModel serviceCenterModel) {
    super(maxNumberOfSamples, serviceCenterModel);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.QUEUE_LENGTH;
  }

  @Override
  public boolean shouldTakeMeasurement() {
    currentQueueLength = namedModel.getCurrentNumberOfJobsInQueue();

    return currentQueueLength != lastQueueLength;
  }

  @Override
  protected double getValue() {
    double value = lastQueueLength;
    lastQueueLength = currentQueueLength;

    return value;
  }

  @Override
  protected double getWeight() {
    var weight = currentTime - time;
    time = currentTime;

    return weight;
  }
}
