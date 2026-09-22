package dev.askov.vipet.core.simulator.sampling.generation.servicecenter;

import dev.askov.vipet.core.simulator.sampling.generation.SampleGenerator;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public abstract class ServiceCenterSampleGenerator extends SampleGenerator<ServiceCenterModel> {

  public ServiceCenterSampleGenerator(
      final int maxNumberOfSamples,
      final ServiceCenterModel serviceCenterModel,
      final boolean invertMean) {
    super(maxNumberOfSamples, serviceCenterModel, invertMean);
  }

  public ServiceCenterSampleGenerator(
      final int maxNumberOfSamples, final ServiceCenterModel serviceCenterModel) {
    super(maxNumberOfSamples, serviceCenterModel);
  }
}
