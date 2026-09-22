package dev.askov.vipet.serialization.json.network;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.serialization.NetworkSpecificationException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkTypeAdapter extends TypeAdapter<NetworkModel> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkTypeAdapter.class);
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String NODES = "nodes";
  private static final String NODE_TYPE = "type";
  private static final String NODE_TYPE_SERVICE_CENTER = "SERVICE_CENTER";
  private static final String NODE_TYPE_SOURCE = "SOURCE";
  private static final String NODE_TYPE_SINK = "SINK";
  private static final String CENTRAL_SERVICE_CENTER_ID = "centralServiceCenterId";
  private static final String NUMBER_OF_JOBS = "numberOfJobs";
  private static final String CONNECTIONS = "connections";
  private static final String CONNECTION_SOURCE_ID = "sourceId";
  private static final String CONNECTION_DESTINATION_ID = "destinationId";
  private static final String CONNECTION_TYPE = "type";

  private final Gson gson;

  public NetworkTypeAdapter(final Gson gson) {
    this.gson = gson;
  }

  public static TypeAdapter<NetworkModel> getInstance(Gson gson) {
    return new NetworkTypeAdapter(gson);
  }

  @Override
  public void write(JsonWriter out, NetworkModel networkModel) throws IOException {
    out.beginObject();
    {
      out.name(NAME).value(networkModel.getName());
      out.name(DESCRIPTION).value(networkModel.getDescription());

      out.name(NODES).beginArray();
      {
        networkModel.getNodeModels().forEach(node -> gson.toJson(node, node.getClass(), out));
      }
      out.endArray();

      if (networkModel.getCentralServiceCenterModel() != null) {
        out.name(CENTRAL_SERVICE_CENTER_ID)
            .value(networkModel.getCentralServiceCenterModel().getId());
      }
      if (networkModel.isClosed()) {
        out.name(NUMBER_OF_JOBS).value(networkModel.getNumberOfJobs());
      }

      out.name(CONNECTIONS).beginArray();
      {
        networkModel
            .getNodeModels()
            .forEach(
                node -> {
                  if (node instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
                    nonTerminalNetworkNode
                        .getConnectionModels()
                        .forEach(
                            connection -> {
                              try {
                                out.beginObject();
                                {
                                  out.name(CONNECTION_SOURCE_ID)
                                      .value(connection.getSource().getId());
                                  out.name(CONNECTION_DESTINATION_ID)
                                      .value(connection.getDestination().getId());
                                  out.name(CONNECTION_TYPE).value(connection.getType().name());
                                }
                                out.endObject();
                              } catch (IOException exception) {
                                LOGGER.error(
                                    "Failed to write connection from {} to {}.",
                                    connection.getSource().getName(),
                                    connection.getDestination().getName(),
                                    exception);
                              }
                            });
                  }
                });
      }
      out.endArray();
    }
    out.endObject();
  }

  @Override
  public NetworkModel read(JsonReader in) throws IOException {
    final var networkModel = new NetworkModel();

    in.beginObject();
    {
      while (in.hasNext()) {
        switch (in.nextName()) {
          case NAME -> networkModel.setName(in.nextString());
          case DESCRIPTION -> networkModel.setDescription(in.nextString());
          case NODES -> {
            in.beginArray();
            {
              while (in.hasNext()) {
                in.beginObject();
                {
                  NetworkNodeModel networkNodeModel = null;
                  if (in.hasNext() && in.nextName().equals(NODE_TYPE)) {
                    switch (in.nextString()) {
                      case NODE_TYPE_SERVICE_CENTER ->
                          networkModel.addNodeModel(
                              networkNodeModel = gson.fromJson(in, ServiceCenterModel.class));
                      case NODE_TYPE_SOURCE ->
                          networkModel.addNodeModel(
                              networkNodeModel = gson.fromJson(in, SourceModel.class));
                      case NODE_TYPE_SINK ->
                          networkModel.addNodeModel(
                              networkNodeModel = gson.fromJson(in, SinkModel.class));
                    }
                    if (networkNodeModel != null) {
                      networkModel.setNextNodeId(
                          Math.max(networkModel.peekNextNodeId(), networkNodeModel.getId() + 1));
                    }
                  } else {
                    throw new NetworkSpecificationException(
                        "Unexpected property. The first node property must be \"%s\""
                            .formatted(NODE_TYPE));
                  }
                }
                in.endObject();
              }
            }
            in.endArray();
          }
          case CENTRAL_SERVICE_CENTER_ID -> {
            final var centralServiceCenterId = in.nextInt();
            final var centralServiceCenterModel =
                networkModel.getServiceCenterModels().stream()
                    .filter(serviceCenter -> serviceCenter.getId() == centralServiceCenterId)
                    .findFirst()
                    .orElse(null);

            if (centralServiceCenterModel != null) {
              networkModel.setCentralServiceCenterModel(centralServiceCenterModel);
            } else {
              throw new NetworkSpecificationException("Central service center not found");
            }
          }
          case NUMBER_OF_JOBS -> networkModel.setNumberOfJobs(in.nextInt());
          case CONNECTIONS -> {
            in.beginArray();
            {
              while (in.hasNext()) {
                in.beginObject();
                {
                  NonTerminalNetworkNodeModel source = null;
                  NetworkNodeModel destination = null;
                  var type = ConnectionModel.Type.MULTI_SEGMENT;

                  while (in.hasNext()) {
                    switch (in.nextName()) {
                      case CONNECTION_SOURCE_ID -> {
                        final var sourceId = in.nextInt();
                        final var potentialSource =
                            networkModel.getNodeModels().stream()
                                .filter(node -> node.getId() == sourceId)
                                .findFirst()
                                .orElse(null);

                        if (potentialSource != null) {
                          if (potentialSource
                              instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
                            source = nonTerminalNetworkNode;
                          } else {
                            throw new NetworkSpecificationException(
                                "Source node cannot be a terminal node");
                          }
                        } else {
                          throw new NetworkSpecificationException("Source node not found");
                        }
                      }
                      case CONNECTION_DESTINATION_ID -> {
                        final var destinationId = in.nextInt();
                        destination =
                            networkModel.getNodeModels().stream()
                                .filter(node -> node.getId() == destinationId)
                                .findFirst()
                                .orElse(null);

                        if (destination == null) {
                          throw new NetworkSpecificationException("Destination node not found");
                        }
                      }
                      case CONNECTION_TYPE -> {
                        final var connectionType = in.nextString();
                        switch (connectionType) {
                          case "SHORTEST" -> type = ConnectionModel.Type.SHORTEST;
                          case "BEZIER" -> type = ConnectionModel.Type.BEZIER;
                          case "MULTI_SEGMENT" -> type = ConnectionModel.Type.MULTI_SEGMENT;
                          default ->
                              throw new NetworkSpecificationException("Unknown connection type");
                        }
                      }
                    }
                  }
                  if (source != null && destination != null) {
                    source.addConnection(new ConnectionModel(source, destination, type));
                  } else {
                    throw new NetworkSpecificationException(
                        "Missing source or destination in connection specification");
                  }
                }
                in.endObject();
              }
            }
            in.endArray();
          }
          default -> throw new NetworkSpecificationException("Unknown network property");
        }
      }
    }
    in.endObject();

    return networkModel;
  }
}
