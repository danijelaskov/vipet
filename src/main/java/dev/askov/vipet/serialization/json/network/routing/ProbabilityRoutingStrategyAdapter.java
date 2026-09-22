package dev.askov.vipet.serialization.json.network.routing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.routing.ProbabilityRoutingStrategyModel;
import dev.askov.vipet.serialization.NetworkSpecificationException;
import java.io.IOException;

public final class ProbabilityRoutingStrategyAdapter
    extends RoutingStrategyAdapter<ProbabilityRoutingStrategyModel> {

  public static final String TYPE_NAME = "PROBABILITIES";

  private static final ProbabilityRoutingStrategyAdapter INSTANCE =
      new ProbabilityRoutingStrategyAdapter();
  private static final String PROBABILITIES = "probabilities";

  public ProbabilityRoutingStrategyAdapter() {
    super(ProbabilityRoutingStrategyModel::new);
  }

  public static TypeAdapter<ProbabilityRoutingStrategyModel> getInstance() {
    return INSTANCE;
  }

  @Override
  public void write(JsonWriter out, ProbabilityRoutingStrategyModel probabilityRoutingStrategy)
      throws IOException {
    super.write(out, probabilityRoutingStrategy);

    out.name(PROBABILITIES).beginArray();
    {
      for (final var probability : probabilityRoutingStrategy.getProbabilityMap().values()) {
        out.value(probability.get());
      }
    }
    out.endArray();
  }

  @Override
  public ProbabilityRoutingStrategyModel read(JsonReader in) throws IOException {
    final var probabilityRoutingStrategy = super.read(in);

    if (in.hasNext() && in.nextName().equals(PROBABILITIES)) {
      in.beginArray();
      {
        while (in.hasNext()) {
          probabilityRoutingStrategy.setNextProbability(in.nextDouble());
        }
      }
      in.endArray();
    } else {
      throw new NetworkSpecificationException(
          String.format(
              "Network specification error: expected \"%s\" but found \"%s\"",
              PROBABILITIES, in.nextName()));
    }

    return probabilityRoutingStrategy;
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }
}
