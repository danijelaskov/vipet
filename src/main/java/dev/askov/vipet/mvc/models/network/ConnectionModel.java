package dev.askov.vipet.mvc.models.network;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class ConnectionModel extends NetworkElementModel {

  public enum Type {
    SHORTEST,
    BEZIER,
    MULTI_SEGMENT
  }

  private final ObjectProperty<NonTerminalNetworkNodeModel> sourceProperty =
      new SimpleObjectProperty<>(this, "source");
  private final ObjectProperty<NetworkNodeModel> destinationProperty =
      new SimpleObjectProperty<>(this, "destination");
  private final ObjectProperty<Type> typeProperty =
      new SimpleObjectProperty<>(this, "type", Type.MULTI_SEGMENT);
  private final ObjectProperty<Type> preferredType =
      new SimpleObjectProperty<>(this, "preferredType", getType());

  public ConnectionModel(
      final NonTerminalNetworkNodeModel source, final NetworkNodeModel destination) {
    setSource(source);
    setDestination(destination);

    if (destination instanceof ServiceCenterModel sourceServiceCenterModel
        && source != destination) {
      sourceServiceCenterModel.setIncomingConnections(true);
    } else if (destination instanceof SinkModel sinkModel) {
      sinkModel.setIncomingConnections(true);
    }

    typeProperty.set(getPreferredType());
    typeProperty.bind(
        Bindings.createObjectBinding(
            () -> {
              final var isSourceForwardOriented = getSource().isForwardOriented();

              if (isSourceForwardOriented != getDestination().isForwardOriented()) {
                return Type.MULTI_SEGMENT;
              }

              final var sourceX = getSource().getOutputPortPosition().getX();
              final var destinationX = getDestination().getInputPortPosition().getX();

              if (isSourceForwardOriented) {
                if (sourceX >= destinationX) {
                  return Type.MULTI_SEGMENT;
                } else {
                  return getPreferredType();
                }
              } else {
                if (sourceX <= destinationX) {
                  return Type.MULTI_SEGMENT;
                } else {
                  return getPreferredType();
                }
              }
            },
            getSource().getOutputPortPosition().xProperty(),
            getDestination().getInputPortPosition().xProperty(),
            getSource().forwardOrientedProperty(),
            getDestination().forwardOrientedProperty(),
            preferredType));
  }

  public ConnectionModel(
      final NonTerminalNetworkNodeModel source,
      final NetworkNodeModel destination,
      final Type type) {
    this(source, destination);

    setPreferredType(type);
  }

  public ConnectionModel() {}

  public NonTerminalNetworkNodeModel getSource() {
    return sourceProperty.get();
  }

  public void setSource(NonTerminalNetworkNodeModel source) {
    sourceProperty.set(source);
  }

  public NetworkNodeModel getDestination() {
    return destinationProperty.get();
  }

  public void setDestination(NetworkNodeModel destination) {
    destinationProperty.set(destination);

    if (destination instanceof ServiceCenterModel serviceCenterModel
        && sourceProperty.get() != serviceCenterModel) {
      serviceCenterModel.setIncomingConnections(true);
    } else if (destination instanceof SinkModel sinkModel) {
      sinkModel.setIncomingConnections(true);
    }
  }

  public ObjectProperty<Type> typeProperty() {
    return typeProperty;
  }

  public Type getType() {
    return typeProperty.get();
  }

  public void setType(Type type) {
    preferredType.set(type);
  }

  private void setPreferredType(Type type) {
    preferredType.set(type);
  }

  private Type getPreferredType() {
    return preferredType.get();
  }

  public boolean isLoop() {
    return getSource() == getDestination();
  }
}
