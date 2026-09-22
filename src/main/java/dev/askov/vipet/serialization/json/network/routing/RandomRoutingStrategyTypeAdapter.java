package dev.askov.vipet.serialization.json.network.routing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.routing.RandomRoutingStrategyModel;
import java.io.IOException;

public final class RandomRoutingStrategyTypeAdapter
    extends RoutingStrategyAdapter<RandomRoutingStrategyModel> {

  public static final String TYPE_NAME = "RANDOM";

  private static final RandomRoutingStrategyTypeAdapter INSTANCE =
      new RandomRoutingStrategyTypeAdapter();

  public RandomRoutingStrategyTypeAdapter() {
    super(RandomRoutingStrategyModel::new);
  }

  public static TypeAdapter<RandomRoutingStrategyModel> getInstance() {
    return INSTANCE;
  }

  @Override
  public void write(JsonWriter out, RandomRoutingStrategyModel randomRoutingStrategy)
      throws IOException {
    super.write(out, randomRoutingStrategy);
  }

  @Override
  public RandomRoutingStrategyModel read(JsonReader in) throws IOException {
    return super.read(in);
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }
}
