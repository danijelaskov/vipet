package dev.askov.vipet.serialization.json.distributions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.DeterministicTimeDistributionModel;
import java.io.IOException;

public final class DeterministicTimeDistributionTypeAdapter
    extends TimeDistributionAdapter<DeterministicTimeDistributionModel> {

  public static final String TYPE_NAME = "DETERMINISTIC";

  private static final DeterministicTimeDistributionTypeAdapter INSTANCE =
      new DeterministicTimeDistributionTypeAdapter();
  private static final String VALUE = "value";

  private DeterministicTimeDistributionTypeAdapter() {}

  public static TypeAdapter<DeterministicTimeDistributionModel> getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  protected void writeParameters(
      JsonWriter out, final DeterministicTimeDistributionModel deterministicTimeDistribution)
      throws IOException {
    out.name(VALUE).value(deterministicTimeDistribution.getValue());
  }

  @Override
  protected DeterministicTimeDistributionModel createDistribution(JsonReader in)
      throws IOException {
    final var deterministicTimeDistribution = new DeterministicTimeDistributionModel();

    while (in.hasNext()) {
      final var parameterName = in.nextName();

      if (parameterName.equals(VALUE)) {
        deterministicTimeDistribution.setValue(in.nextDouble());
      } else {
        throw new IOException(
            String.format(
                "Network specification error: expected \"%s\" but found \"%s\"",
                VALUE, parameterName));
      }
    }

    return deterministicTimeDistribution;
  }
}
