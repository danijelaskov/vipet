package dev.askov.vipet.core.analyzer.exceptions;

import dev.askov.vipet.mvc.models.network.ServiceCenterModel;

public class UnstableNetworkException extends Exception {

  private final double arrivalRate;
  private final ServiceCenterModel nonErgodicServiceCenterModel;

  public UnstableNetworkException(
      final ServiceCenterModel nonErgodicServiceCenterModel, final double arrivalRate) {
    super("The network is unstable.");

    this.arrivalRate = arrivalRate;
    this.nonErgodicServiceCenterModel = nonErgodicServiceCenterModel;
  }

  public ServiceCenterModel getNonErgodicServiceCenterModel() {
    return nonErgodicServiceCenterModel;
  }

  public double getArrivalRate() {
    return arrivalRate;
  }
}
