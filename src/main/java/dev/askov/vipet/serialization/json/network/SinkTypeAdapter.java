package dev.askov.vipet.serialization.json.network;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.SinkModel;
import java.io.IOException;

public final class SinkTypeAdapter extends NetworkNodeTypeAdapter<SinkModel> {

  private static final SinkTypeAdapter INSTANCE = new SinkTypeAdapter();

  public SinkTypeAdapter() {
    super(() -> new SinkModel(NetworkModel.INVALID_NODE_ID));
  }

  public static TypeAdapter<SinkModel> getInstance() {
    return INSTANCE;
  }

  @Override
  public void write(JsonWriter out, SinkModel sinkModel) throws IOException {
    out.beginObject();
    {
      super.write(out, sinkModel);
    }
    out.endObject();
  }

  @Override
  public SinkModel read(JsonReader in) throws IOException {
    return super.read(in);
  }

  @Override
  public String getTypeName() {
    return "SINK";
  }
}
