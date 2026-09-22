package dev.askov.vipet.mvc.models.network;

import java.util.List;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

public final class NetworkCollectionModel {

  private final ListProperty<NetworkModel> networksProperty =
      new SimpleListProperty<>(this, "networks", FXCollections.observableArrayList());
  private final IntegerProperty currentNetworkIndexProperty =
      new SimpleIntegerProperty(this, "currentNetworkIndex", -1);
  private final ReadOnlyObjectWrapper<NetworkModel> currentNetworkPropertyWrapper =
      new ReadOnlyObjectWrapper<>(this, "currentNetwork", null);

  public NetworkCollectionModel() {
    currentNetworkIndexProperty.addListener(
        (currentNetworkIndex, oldIndex, newIndex) -> {
          if (newIndex.intValue() >= 0 && newIndex.intValue() < networksProperty.size()) {
            currentNetworkPropertyWrapper.set(networksProperty.get(newIndex.intValue()));
          } else {
            currentNetworkPropertyWrapper.set(null);
          }
        });
  }

  public ListProperty<NetworkModel> networksProperty() {
    return networksProperty;
  }

  public List<NetworkModel> getNetworks() {
    return networksProperty.get();
  }

  public IntegerProperty currentNetworkIndexProperty() {
    return currentNetworkIndexProperty;
  }

  public int getCurrentNetworkIndex() {
    return currentNetworkIndexProperty.get();
  }

  public ReadOnlyObjectProperty<NetworkModel> currentNetworkProperty() {
    return currentNetworkPropertyWrapper.getReadOnlyProperty();
  }

  public NetworkModel getCurrentNetwork() {
    return currentNetworkPropertyWrapper.get();
  }

  public void add(final NetworkModel networkModel) {
    networksProperty.add(networkModel);
  }

  public void remove(final NetworkModel networkModel) {
    final var index = networksProperty.indexOf(networkModel);

    networksProperty.remove(networkModel);

    if (!networksProperty.isEmpty()) {
      if (networksProperty.size() == index) {
        currentNetworkPropertyWrapper.set(networksProperty.get(index - 1));
      } else {
        currentNetworkPropertyWrapper.set(networksProperty.get(index));
      }
    } else {
      currentNetworkPropertyWrapper.set(null);
    }
  }

  public void remove(final int index) {
    networksProperty.remove(index);
  }

  public void set(final int index, final NetworkModel networkModel) {
    networksProperty.set(index, networkModel);
  }

  public boolean isEmpty() {
    return networksProperty.isEmpty();
  }

  public int indexOf(final NetworkModel networkModel) {
    return networksProperty.indexOf(networkModel);
  }

  public int getNumberOfNetworks() {
    return networksProperty.size();
  }
}
