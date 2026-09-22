package dev.askov.vipet.mvc.views.network;

import dev.askov.vipet.mvc.views.network.connection.Connection;
import dev.askov.vipet.mvc.views.network.connection.MultiSegmentPathWithArcs;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;

public final class Network extends Group {

  private static final double MIN_INTERNODE_DISTANCE =
      1.2 * MultiSegmentPathWithArcs.INTERNODE_DISTANCE;

  private final ListProperty<NetworkNode> networkNodesProperty =
      new SimpleListProperty<>(this, "networkNodes", FXCollections.observableArrayList());
  private final ListProperty<Connection> connectionsProperty =
      new SimpleListProperty<>(this, "connections", FXCollections.observableArrayList());

  public boolean hasNodeThatOverlapsWith(final NetworkNode networkNode) {
    if (networkNode == null) {
      return false;
    }

    var overlapping = false;

    final var nodeX = networkNode.getTranslateX();
    final var nodeY = networkNode.getTranslateY();
    final var nodeMaxX = networkNode.getBoundsInParent().getMaxX();
    final var nodeMaxY = networkNode.getBoundsInParent().getMaxY();

    for (var currentNetworkNode : networkNodesProperty) {
      if (currentNetworkNode != networkNode) {
        final var currentNodeX = currentNetworkNode.getTranslateX();
        final var currentNodeY = currentNetworkNode.getTranslateY();

        if (nodeX <= currentNetworkNode.getBoundsInParent().getMaxX() + MIN_INTERNODE_DISTANCE
            && nodeMaxX >= currentNodeX - MIN_INTERNODE_DISTANCE
            && nodeMaxY >= currentNodeY - MIN_INTERNODE_DISTANCE
            && nodeY <= currentNetworkNode.getBoundsInParent().getMaxY() + MIN_INTERNODE_DISTANCE) {
          overlapping = true;
          break;
        }
      }
    }

    return overlapping;
  }

  public void addNetworkNode(final NetworkNode networkNode) {
    networkNodesProperty.add(networkNode);
    getChildren().add(networkNode);
  }

  public void removeNetworkNode(final NetworkNode networkNode) {
    networkNodesProperty.remove(networkNode);
    getChildren().remove(networkNode);
  }

  public void addConnection(final Connection connection) {
    connectionsProperty.add(connection);
    getChildren().add(connection);

    networkNodesProperty.forEach(Node::toFront);
  }

  public void removeConnection(final Connection connection) {
    connectionsProperty.remove(connection);
    getChildren().remove(connection);
  }
}
