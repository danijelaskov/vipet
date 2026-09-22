package dev.askov.vipet.serialization.json.distributions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.distributions.TimeDistributionModel;
import java.io.IOException;

public abstract class TimeDistributionAdapter<T extends TimeDistributionModel>
    extends TypeAdapter<T> {

  public static final String TYPE = "type";

  private static final String PARAMETERS = "parameters";

  @Override
  public void write(JsonWriter out, final T timeDistribution) throws IOException {
    out.name(TYPE).value(getTypeName());

    out.name(PARAMETERS).beginObject();
    {
      writeParameters(out, timeDistribution);
    }
    out.endObject();
  }

  protected abstract String getTypeName();

  protected abstract void writeParameters(JsonWriter out, final T timeDistribution)
      throws IOException;

  @Override
  public T read(JsonReader in) throws IOException {
    final T timeDistribution;

    if (in.hasNext()) {
      final var name = in.nextName();

      if (name.equals(PARAMETERS)) {
        in.beginObject();
        {
          timeDistribution = createDistribution(in);
        }
        in.endObject();
      } else {
        throw new IOException(
            String.format(
                "Network specification error: expected \"%s\" object but found \"%s\"",
                PARAMETERS, name));
      }
    } else {
      throw new IOException(
          String.format(
              "Network specification error: expected \"%s\" object but found nothing", PARAMETERS));
    }

    return timeDistribution;
  }

  protected abstract T createDistribution(JsonReader in) throws IOException;
}
