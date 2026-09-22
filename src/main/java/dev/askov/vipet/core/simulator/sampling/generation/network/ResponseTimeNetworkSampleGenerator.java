package dev.askov.vipet.core.simulator.sampling.generation.network;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.JobModel;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import java.util.HashMap;
import java.util.Map;

public final class ResponseTimeNetworkSampleGenerator extends NetworkSampleGenerator {

  private int numberOfDepartedJobs;
  private final Map<JobModel, Double> arrivalTimeMap = new HashMap<>();

  public ResponseTimeNetworkSampleGenerator(
      final int maxNumberOfSamples, final NetworkModel networkModel) {
    super(maxNumberOfSamples, networkModel);
  }

  @Override
  public PerformanceMeasureType getPerformanceMeasureType() {
    return PerformanceMeasureType.RESPONSE_TIME;
  }

  @Override
  protected boolean shouldTakeMeasurement() {
    if (namedModel.isOpen()) {
      final var lastNumberOfDepartedJobs = this.numberOfDepartedJobs;

      this.numberOfDepartedJobs = namedModel.getNumberOfDepartedJobs();

      return namedModel.getNumberOfDepartedJobs() > lastNumberOfDepartedJobs;
    } else {
      return namedModel.getCentralServiceCenterModel().getLastServicedJob() != null;
    }
  }

  @Override
  public double getValue() {
    if (namedModel.isOpen()) {
      final var lastDepartedJobModel = namedModel.getLastDepartedJobModel();

      return lastDepartedJobModel.getNetworkExitTime() - lastDepartedJobModel.getNetworkEnterTime();
    } else {
      final var centralServiceCenterModel = namedModel.getCentralServiceCenterModel();
      final var lastJobModel = centralServiceCenterModel.getLastServicedJob();
      final double lastArrivalTime =
          arrivalTimeMap.getOrDefault(lastJobModel, lastJobModel.getLastArrivalTime());
      var result = lastJobModel.getLastDepartureTime() - lastArrivalTime;

      arrivalTimeMap.put(lastJobModel, lastJobModel.getLastArrivalTime());
      centralServiceCenterModel.removeLastServicedJob();

      return result;
    }
  }

  @Override
  public double getWeight() {
    return 1.0;
  }
}
