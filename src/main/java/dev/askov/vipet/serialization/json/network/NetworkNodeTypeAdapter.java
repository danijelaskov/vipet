package dev.askov.vipet.serialization.json.network;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.NetworkNodeModel;
import dev.askov.vipet.serialization.NetworkSpecificationException;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;
import javafx.scene.paint.Color;

public abstract class NetworkNodeTypeAdapter<SomeNetworkNodeModel extends NetworkNodeModel>
    extends TypeAdapter<SomeNetworkNodeModel> {

  private static final String ID = "id";
  private static final String TYPE = "type";
  private static final String GENERAL = "general";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String POSITION = "position";
  private static final String X = "x";
  private static final String Y = "y";
  private static final String FORWARD_ORIENTATION = "forwardOrientation";
  private static final String FILL_COLOR = "fillColor";
  private static final String STROKE_COLOR = "strokeColor";

  private final Supplier<? extends SomeNetworkNodeModel> networkNodeModelSupplier;

  public NetworkNodeTypeAdapter(
      final Supplier<? extends SomeNetworkNodeModel> networkNodeModelSupplier) {
    this.networkNodeModelSupplier = Objects.requireNonNull(networkNodeModelSupplier);
  }

  @Override
  public void write(JsonWriter out, SomeNetworkNodeModel networkNodeModel) throws IOException {
    out.name(TYPE).value(getTypeName());
    out.name(GENERAL).beginObject();
    {
      out.name(ID).value(networkNodeModel.getId());
      out.name(NAME).value(networkNodeModel.getName());
      out.name(DESCRIPTION).value(networkNodeModel.getDescription());

      out.name(POSITION).beginObject();
      {
        out.name(X).value(networkNodeModel.getX());
        out.name(Y).value(networkNodeModel.getY());
      }
      out.endObject();

      out.name(FORWARD_ORIENTATION).value(networkNodeModel.isForwardOriented());

      out.name(FILL_COLOR).value(networkNodeModel.getFillColor().toString());
      out.name(STROKE_COLOR).value(networkNodeModel.getStrokeColor().toString());
    }
    out.endObject();
  }

  @Override
  public SomeNetworkNodeModel read(JsonReader in) throws IOException {
    final var someNetworkNode = networkNodeModelSupplier.get();

    if (in.hasNext() && in.nextName().equals(GENERAL)) {
      in.beginObject();
      {
        while (in.hasNext()) {
          switch (in.nextName()) {
            case ID -> someNetworkNode.setId(in.nextLong());
            case NAME -> someNetworkNode.setName(in.nextString());
            case DESCRIPTION -> someNetworkNode.setDescription(in.nextString());
            case POSITION -> {
              in.beginObject();
              while (in.hasNext()) {
                switch (in.nextName()) {
                  case X -> someNetworkNode.setX(in.nextDouble());
                  case Y -> someNetworkNode.setY(in.nextDouble());
                  default -> throw new NetworkSpecificationException("Unknown position property");
                }
              }
              in.endObject();
            }
            case FORWARD_ORIENTATION -> someNetworkNode.setForwardOriented(in.nextBoolean());
            case FILL_COLOR -> someNetworkNode.setFillColor(Color.web(in.nextString()));
            case STROKE_COLOR -> someNetworkNode.setStrokeColor(Color.web(in.nextString()));
          }
        }
      }
      in.endObject();
    }

    return someNetworkNode;
  }

  public abstract String getTypeName();
}
