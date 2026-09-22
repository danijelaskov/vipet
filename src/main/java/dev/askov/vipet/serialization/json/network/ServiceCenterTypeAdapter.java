package dev.askov.vipet.serialization.json.network;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import dev.askov.vipet.serialization.NetworkSpecificationException;
import java.io.IOException;

public final class ServiceCenterTypeAdapter
    extends NonterminalNetworkNodeTypeAdapter<ServiceCenterModel> {

  private static final String SERVICE = "service";
  private static final String NUMBER_OF_SERVERS = "numberOfServers";
  private static final String QUEUE = "queue";

  public ServiceCenterTypeAdapter(final Gson gson) {
    super(gson, () -> new ServiceCenterModel(NetworkModel.INVALID_NODE_ID));
  }

  public static TypeAdapter<ServiceCenterModel> getInstance(Gson gson) {
    return new ServiceCenterTypeAdapter(gson);
  }

  @Override
  public void write(JsonWriter out, ServiceCenterModel serviceCenterModel) throws IOException {
    out.beginObject();
    {
      super.write(out, serviceCenterModel);
      out.name(SERVICE).beginObject();
      {
        out.name(NUMBER_OF_SERVERS).value(serviceCenterModel.getNumberOfServers());
      }
      out.endObject();
      out.name(QUEUE);
      gson.toJson(
          serviceCenterModel.getQueueModel(), serviceCenterModel.getQueueModel().getClass(), out);
    }
    out.endObject();
  }

  @Override
  public ServiceCenterModel read(JsonReader in) throws IOException {
    final var serviceCenterModel = super.read(in);

    while (in.hasNext()) {
      switch (in.nextName()) {
        case SERVICE -> {
          in.beginObject();
          while (in.hasNext()) {
            if (in.nextName().equals(NUMBER_OF_SERVERS)) {
              serviceCenterModel.setNumberOfServers(in.nextInt());
            } else {
              throw new NetworkSpecificationException("Unknown service property");
            }
          }
          in.endObject();
        }
        case QUEUE ->
            serviceCenterModel.setQueueModel(
                gson.fromJson(in, serviceCenterModel.getQueueModel().getClass()));
        default -> throw new NetworkSpecificationException("Unknown queue property");
      }
    }

    return serviceCenterModel;
  }

  @Override
  public String getTypeName() {
    return "SERVICE_CENTER";
  }
}
