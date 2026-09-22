package dev.askov.vipet.serialization.json.distributions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.HyperexponentialTimeDistributionModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HyperexponentialTimeDistributionTypeAdapter
    extends TimeDistributionAdapter<HyperexponentialTimeDistributionModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(HyperexponentialTimeDistributionTypeAdapter.class);
  public static final String TYPE_NAME = "HYPEREXPONENTIAL";
  private static final HyperexponentialTimeDistributionTypeAdapter INSTANCE =
      new HyperexponentialTimeDistributionTypeAdapter();
  private static final String RATES = "rates";
  private static final String PROBABILITIES = "probabilities";

  private HyperexponentialTimeDistributionTypeAdapter() {}

  public static TypeAdapter<HyperexponentialTimeDistributionModel> getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  protected void writeParameters(
      JsonWriter out, final HyperexponentialTimeDistributionModel hyperexponentialTimeDistribution)
      throws IOException {
    out.name(PROBABILITIES);
    out.beginArray();
    {
      for (final var probabilityRatePair :
          hyperexponentialTimeDistribution.getProbabilityRatePairs()) {
        out.value(probabilityRatePair.probability());
      }
    }
    out.endArray();

    out.name(RATES);
    out.beginArray();
    {
      for (final var probabilityRatePair :
          hyperexponentialTimeDistribution.getProbabilityRatePairs()) {
        out.value(probabilityRatePair.rate());
      }
    }
    out.endArray();
  }

  @Override
  protected HyperexponentialTimeDistributionModel createDistribution(JsonReader in)
      throws IOException {
    final List<HyperexponentialTimeDistributionModel.ProbabilityRatePair> probabilityRatePairs =
        new ArrayList<>();

    while (in.hasNext()) {
      final var parameterName = in.nextName();

      switch (parameterName) {
        case PROBABILITIES -> {
          in.beginArray();
          {
            while (in.hasNext()) {
              probabilityRatePairs.add(
                  new HyperexponentialTimeDistributionModel.ProbabilityRatePair(
                      in.nextDouble(),
                      HyperexponentialTimeDistributionModel.ProbabilityRatePair.DEFAULT_RATE));
            }
          }
          in.endArray();
        }
        case RATES -> {
          var i = 0;
          in.beginArray();
          {
            while (in.hasNext()) {
              probabilityRatePairs.get(i).setRate(in.nextDouble());
              i++;
            }
          }
          in.endArray();
        }
        default ->
            throw new IOException(
                String.format(
                    "Network specification error: expected \"%s\" or \"%s\" but found \"%s\"",
                    PROBABILITIES, RATES, parameterName));
      }
    }

    final var stringBuilder = new StringBuilder();
    probabilityRatePairs.forEach(
        probabilityRatePair ->
            stringBuilder
                .append(probabilityRatePair.probability())
                .append(" ")
                .append(probabilityRatePair.rate())
                .append(", "));
    LOGGER.debug("Read hyperexponential time distribution: {}", stringBuilder);

    return new HyperexponentialTimeDistributionModel(probabilityRatePairs);
  }
}
