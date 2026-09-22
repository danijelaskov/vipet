package dev.askov.vipet.serialization.json.distributions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.ParetoTimeDistributionModel;
import java.io.IOException;

public final class ParetoTimeDistributionTypeAdapter
    extends TimeDistributionAdapter<ParetoTimeDistributionModel> {

  public static final String TYPE_NAME = "PARETO";

  private static final ParetoTimeDistributionTypeAdapter INSTANCE =
      new ParetoTimeDistributionTypeAdapter();
  private static final String SHAPE = "shape";
  private static final String SCALE = "scale";

  private ParetoTimeDistributionTypeAdapter() {}

  public static TypeAdapter<ParetoTimeDistributionModel> getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public void writeParameters(
      JsonWriter out, final ParetoTimeDistributionModel paretoTimeDistribution) throws IOException {
    out.name(SHAPE).value(paretoTimeDistribution.getShape());
    out.name(SCALE).value(paretoTimeDistribution.getScale());
  }

  @Override
  public ParetoTimeDistributionModel createDistribution(JsonReader in) throws IOException {
    final var paretoTimeDistribution = new ParetoTimeDistributionModel();

    while (in.hasNext()) {
      final var parameterName = in.nextName();

      if (parameterName.equals(SHAPE)) {
        paretoTimeDistribution.setShape(in.nextDouble());
      } else if (parameterName.equals(SCALE)) {
        paretoTimeDistribution.setScale(in.nextDouble());
      } else {
        throw new IOException(
            String.format(
                "Network specification error: expected \"%s\" or \"%s\" but found \"%s\"",
                SHAPE, SCALE, parameterName));
      }
    }

    return paretoTimeDistribution;
  }
}
