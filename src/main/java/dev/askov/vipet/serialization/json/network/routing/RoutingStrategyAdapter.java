package dev.askov.vipet.serialization.json.network.routing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.routing.RoutingStrategyModel;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class RoutingStrategyAdapter<SomeRoutingStrategyModel extends RoutingStrategyModel>
    extends TypeAdapter<SomeRoutingStrategyModel> {

  public static final String TYPE = "type";

  private final Supplier<? extends SomeRoutingStrategyModel> supplier;

  public RoutingStrategyAdapter(final Supplier<? extends SomeRoutingStrategyModel> supplier) {
    this.supplier = Objects.requireNonNull(supplier);
  }

  @Override
  public void write(JsonWriter out, SomeRoutingStrategyModel routingStrategy) throws IOException {
    out.name(TYPE).value(getTypeName());
  }

  @Override
  public SomeRoutingStrategyModel read(JsonReader in) throws IOException {
    return supplier.get();
  }

  protected abstract String getTypeName();
}
