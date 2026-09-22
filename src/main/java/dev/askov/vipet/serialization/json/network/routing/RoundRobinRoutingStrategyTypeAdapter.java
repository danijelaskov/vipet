package dev.askov.vipet.serialization.json.network.routing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.routing.RoundRobinRoutingStrategyModel;
import java.io.IOException;

public final class RoundRobinRoutingStrategyTypeAdapter
    extends RoutingStrategyAdapter<RoundRobinRoutingStrategyModel> {

  public static final String TYPE_NAME = "ROUND_ROBIN";

  private static final RoundRobinRoutingStrategyTypeAdapter INSTANCE =
      new RoundRobinRoutingStrategyTypeAdapter();

  public RoundRobinRoutingStrategyTypeAdapter() {
    super(RoundRobinRoutingStrategyModel::new);
  }

  public static TypeAdapter<RoundRobinRoutingStrategyModel> getInstance() {
    return INSTANCE;
  }

  @Override
  public void write(JsonWriter out, RoundRobinRoutingStrategyModel roundRobinRoutingStrategy)
      throws IOException {
    super.write(out, roundRobinRoutingStrategy);
  }

  @Override
  public RoundRobinRoutingStrategyModel read(JsonReader in) throws IOException {
    return super.read(in);
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }
}
