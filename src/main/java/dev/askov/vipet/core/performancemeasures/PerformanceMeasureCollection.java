package dev.askov.vipet.core.performancemeasures;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class PerformanceMeasureCollection {

  public class Metadata {

    private final ServiceCenterModel serviceCenterModel;

    public Metadata(final ServiceCenterModel serviceCenterModel) {
      this.serviceCenterModel = serviceCenterModel;
    }

    public Metadata() {
      this.serviceCenterModel = null;
    }

    public String getName() {
      return serviceCenterModel != null ? serviceCenterModel.getName() : system.getName();
    }

    public PerformanceMeasure getPerformanceMeasure(
        final PerformanceMeasureType performanceMeasureType) {
      return serviceCenterModel != null
          ? performanceMeasures.get(serviceCenterModel).get(performanceMeasureType)
          : systemPerformanceMeasures.get(performanceMeasureType);
    }

    public boolean isSystemPerformanceMeasureCollection() {
      return serviceCenterModel == null;
    }

    public ServiceCenterModel getServiceCenter() {
      return serviceCenterModel;
    }
  }

  private final NetworkModel system;
  private final LinkedHashMap<
          ServiceCenterModel, LinkedHashMap<PerformanceMeasureType, PerformanceMeasure>>
      performanceMeasures = new LinkedHashMap<>();
  private final LinkedHashMap<PerformanceMeasureType, PerformanceMeasure>
      systemPerformanceMeasures = new LinkedHashMap<>();

  public PerformanceMeasureCollection(final NetworkModel system) {
    this.system = system;
  }

  public List<ServiceCenterModel> getServiceCenters() {
    return performanceMeasures.keySet().stream().toList();
  }

  public boolean hasPerformanceMeasure(
      final ServiceCenterModel serviceCenterModel,
      final PerformanceMeasureType performanceMeasureType) {
    return performanceMeasures.containsKey(serviceCenterModel)
        && performanceMeasures.get(serviceCenterModel).containsKey(performanceMeasureType);
  }

  public boolean hasPerformanceMeasure(final PerformanceMeasureType performanceMeasureType) {
    return performanceMeasures.values().stream()
        .allMatch(map -> map.containsKey(performanceMeasureType));
  }

  public PerformanceMeasure getPerformanceMeasure(
      final ServiceCenterModel serviceCenterModel,
      final PerformanceMeasureType performanceMeasureType) {
    return performanceMeasures.get(serviceCenterModel).get(performanceMeasureType);
  }

  public boolean hasSystemPerformanceMeasure(final PerformanceMeasureType performanceMeasureType) {
    return systemPerformanceMeasures.containsKey(performanceMeasureType);
  }

  public PerformanceMeasure getSystemPerformanceMeasure(
      final PerformanceMeasureType performanceMeasureType) {
    return systemPerformanceMeasures.get(performanceMeasureType);
  }

  public void setPerformanceMeasure(
      final ServiceCenterModel serviceCenterModel,
      final PerformanceMeasureType performanceMeasureType,
      final PerformanceMeasure performanceMeasure) {
    if (!performanceMeasures.containsKey(serviceCenterModel)) {
      performanceMeasures.put(serviceCenterModel, new LinkedHashMap<>());
    }
    performanceMeasures.get(serviceCenterModel).put(performanceMeasureType, performanceMeasure);
  }

  public void setSystemPerformanceMeasure(
      final PerformanceMeasureType performanceMeasureType,
      final PerformanceMeasure performanceMeasure) {
    systemPerformanceMeasures.put(performanceMeasureType, performanceMeasure);
  }

  public ObservableList<Metadata> getPerformanceMeasureCollectionMetadataList() {
    final var list =
        new ArrayList<>(performanceMeasures.keySet().stream().map(Metadata::new).toList());

    list.add(new Metadata()); // Adding system performance measure metadata

    return FXCollections.observableArrayList(list);
  }

  public NetworkModel getSystem() {
    return system;
  }
}
