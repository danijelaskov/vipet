package dev.askov.vipet.serialization.json.network;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.SourceModel;
import java.io.IOException;

public final class SourceTypeAdapter extends NonterminalNetworkNodeTypeAdapter<SourceModel> {

  public SourceTypeAdapter(Gson gson) {
    super(gson, () -> new SourceModel(NetworkModel.INVALID_NODE_ID));
  }

  public static TypeAdapter<SourceModel> getInstance(Gson gson) {
    return new SourceTypeAdapter(gson);
  }

  @Override
  public void write(JsonWriter out, SourceModel sourceModel) throws IOException {
    out.beginObject();
    {
      super.write(out, sourceModel);
    }
    out.endObject();
  }

  @Override
  public SourceModel read(JsonReader in) throws IOException {
    return super.read(in);
  }

  @Override
  public String getTypeName() {
    return "SOURCE";
  }
}
