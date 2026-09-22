package dev.askov.vipet.mvc.controllers.converters;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import javafx.util.StringConverter;

public final class ServiceCenterConverter extends StringConverter<ServiceCenterModel> {

  final NetworkModel networkModel;

  public ServiceCenterConverter(final NetworkModel networkModel) {
    this.networkModel = networkModel;
  }

  @Override
  public String toString(final ServiceCenterModel serviceCenterModel) {
    return serviceCenterModel.getName();
  }

  @Override
  public ServiceCenterModel fromString(final String string) {
    return networkModel.getServiceCenterModels().stream()
        .filter(serviceCenter -> serviceCenter.getName().equals(string))
        .findFirst()
        .orElseThrow();
  }
}
