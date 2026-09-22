package dev.askov.vipet.serialization.json.distributions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.UniformTimeDistributionModel;
import java.io.IOException;

public final class UniformTimeDistributionTypeAdapter
    extends TimeDistributionAdapter<UniformTimeDistributionModel> {

  public static final String TYPE_NAME = "UNIFORM";

  private static final UniformTimeDistributionTypeAdapter INSTANCE =
      new UniformTimeDistributionTypeAdapter();
  private static final String MIN = "min";
  private static final String MAX = "max";

  private UniformTimeDistributionTypeAdapter() {}

  public static TypeAdapter<UniformTimeDistributionModel> getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  protected void writeParameters(
      JsonWriter out, final UniformTimeDistributionModel uniformTimeDistribution)
      throws IOException {
    out.name(MIN).value(uniformTimeDistribution.getMin());
    out.name(MAX).value(uniformTimeDistribution.getMax());
  }

  @Override
  protected UniformTimeDistributionModel createDistribution(JsonReader in) throws IOException {
    final var uniformTimeDistribution = new UniformTimeDistributionModel();

    while (in.hasNext()) {
      final var parameterName = in.nextName();

      if (parameterName.equals(MIN)) {
        uniformTimeDistribution.setMin(in.nextDouble());
      } else if (parameterName.equals(MAX)) {
        uniformTimeDistribution.setMax(in.nextDouble());
      } else {
        throw new IOException(
            String.format(
                "Network specification error: expected \"%s\" or \"%s\" but found \"%s\"",
                MIN, MAX, parameterName));
      }
    }

    return uniformTimeDistribution;
  }
}
