package dev.askov.vipet.serialization.json.distributions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.ExponentialTimeDistributionModel;
import java.io.IOException;

public final class ExponentialTimeDistributionTypeAdapter
    extends TimeDistributionAdapter<ExponentialTimeDistributionModel> {

  public static final String TYPE_NAME = "EXPONENTIAL";

  private static final ExponentialTimeDistributionTypeAdapter INSTANCE =
      new ExponentialTimeDistributionTypeAdapter();
  private static final String RATE = "rate";

  private ExponentialTimeDistributionTypeAdapter() {}

  public static TypeAdapter<ExponentialTimeDistributionModel> getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  protected void writeParameters(
      JsonWriter out, final ExponentialTimeDistributionModel exponentialTimeDistribution)
      throws IOException {
    out.name(RATE).value(exponentialTimeDistribution.getRate());
  }

  @Override
  protected ExponentialTimeDistributionModel createDistribution(JsonReader in) throws IOException {
    final var exponentialTimeDistribution = new ExponentialTimeDistributionModel();

    while (in.hasNext()) {
      final var parameterName = in.nextName();

      if (parameterName.equals(RATE)) {
        exponentialTimeDistribution.setRate(in.nextDouble());
      } else {
        throw new IOException(
            String.format(
                "Network specification error: expected \"%s\" but found \"%s\"",
                RATE, parameterName));
      }
    }

    return exponentialTimeDistribution;
  }
}
