package dev.askov.vipet.serialization.json.distributions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.ErlangTimeDistributionModel;
import java.io.IOException;

public final class ErlangTimeDistributionTypeAdapter
    extends TimeDistributionAdapter<ErlangTimeDistributionModel> {

  public static final String TYPE_NAME = "ERLANG";

  private static final ErlangTimeDistributionTypeAdapter INSTANCE =
      new ErlangTimeDistributionTypeAdapter();
  private static final String SHAPE = "shape";
  private static final String SCALE = "scale";

  private ErlangTimeDistributionTypeAdapter() {}

  public static TypeAdapter<ErlangTimeDistributionModel> getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public void writeParameters(
      JsonWriter out, final ErlangTimeDistributionModel erlangTimeDistribution) throws IOException {
    out.name(SHAPE).value(erlangTimeDistribution.getShape());
    out.name(SCALE).value(erlangTimeDistribution.getScale());
  }

  @Override
  public ErlangTimeDistributionModel createDistribution(final JsonReader in) throws IOException {
    final var erlangTimeDistribution = new ErlangTimeDistributionModel();

    while (in.hasNext()) {
      final var parameterName = in.nextName();

      if (parameterName.equals(SHAPE)) {
        erlangTimeDistribution.setShape(in.nextInt());
      } else if (parameterName.equals(SCALE)) {
        erlangTimeDistribution.setScale(in.nextDouble());
      } else {
        throw new IOException(
            String.format(
                "Network specification error: expected \"%s\" or \"%s\" but found \"%s\"",
                SHAPE, SCALE, parameterName));
      }
    }

    return erlangTimeDistribution;
  }
}
