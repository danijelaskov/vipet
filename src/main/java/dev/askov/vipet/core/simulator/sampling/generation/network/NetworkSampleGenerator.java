package dev.askov.vipet.core.simulator.sampling.generation.network;

import dev.askov.vipet.core.simulator.sampling.generation.SampleGenerator;
import dev.askov.vipet.mvc.models.network.NetworkModel;

public abstract class NetworkSampleGenerator extends SampleGenerator<NetworkModel> {

  public NetworkSampleGenerator(
      int maxNumberOfSamples, NetworkModel networkModel, boolean invertMean) {
    super(maxNumberOfSamples, networkModel, invertMean);
  }

  public NetworkSampleGenerator(int maxNumberOfSamples, NetworkModel networkModel) {
    super(maxNumberOfSamples, networkModel);
  }
}
