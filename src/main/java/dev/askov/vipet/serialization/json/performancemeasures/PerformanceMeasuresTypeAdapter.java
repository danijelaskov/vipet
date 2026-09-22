package dev.askov.vipet.serialization.json.performancemeasures;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import java.io.IOException;
import java.util.Objects;

public final class PerformanceMeasuresTypeAdapter
    extends TypeAdapter<PerformanceMeasureCollection> {

  private final NetworkModel system;

  public PerformanceMeasuresTypeAdapter(final NetworkModel system) {
    this.system = system;
  }

  @Override
  public void write(JsonWriter out, PerformanceMeasureCollection performanceMeasureCollection)
      throws IOException {
    out.beginObject();
    {
      out.name("network").value(performanceMeasureCollection.getSystem().getName());
      out.name("performanceMeasures");
      out.beginArray();
      {
        for (final var serviceCenterModel :
            performanceMeasureCollection.getSystem().getServiceCenterModels()) {
          out.beginObject();
          {
            out.name("id").value(serviceCenterModel.getId());
            out.name("values");
            out.beginArray();
            for (final var performanceMeasureType : PerformanceMeasureType.values()) {
              if (performanceMeasureCollection.hasPerformanceMeasure(
                  serviceCenterModel, performanceMeasureType)) {
                out.beginObject();
                {
                  out.name("name").value(performanceMeasureType.name());
                  out.name("value").beginObject();
                  {
                    out.name("center")
                        .value(
                            performanceMeasureCollection
                                .getPerformanceMeasure(serviceCenterModel, performanceMeasureType)
                                .center());
                    out.name("halfWidth")
                        .value(
                            performanceMeasureCollection
                                .getPerformanceMeasure(serviceCenterModel, performanceMeasureType)
                                .halfWidth());
                  }
                  out.endObject();
                }
                out.endObject();
              }
            }
            out.endArray();
          }
          out.endObject();
        }
        out.beginObject();
        {
          out.name("values").beginArray();
          {
            for (final var performanceMeasureType : PerformanceMeasureType.values()) {
              if (performanceMeasureCollection.hasSystemPerformanceMeasure(
                  performanceMeasureType)) {
                out.beginObject();
                {
                  out.name("name").value(performanceMeasureType.name());
                  out.name("value").beginObject();
                  {
                    out.name("center")
                        .value(
                            performanceMeasureCollection
                                .getSystemPerformanceMeasure(performanceMeasureType)
                                .center());
                    out.name("halfWidth")
                        .value(
                            performanceMeasureCollection
                                .getSystemPerformanceMeasure(performanceMeasureType)
                                .halfWidth());
                  }
                  out.endObject();
                }
                out.endObject();
              }
            }
          }
          out.endArray();
        }
        out.endObject();
      }
      out.endArray();
    }
    out.endObject();
  }

  @Override
  public PerformanceMeasureCollection read(JsonReader in) throws IOException {
    final var performanceMeasureCollection = new PerformanceMeasureCollection(null);

    in.beginObject();
    {
      while (in.hasNext()) {
        var key = in.nextName();
        if (key.equals("network")) {
          final var networkName = in.nextString();
          if (!networkName.equals(system.getName())) {
            throw new RuntimeException(
                "Performance measures are for network \"%s\", but network \"%s\" is expected"
                    .formatted(networkName, system.getName()));
          }
        } else if (key.equals("performanceMeasures")) {
          in.beginArray();
          {
            while (in.hasNext()) {
              in.beginObject();
              {
                key = in.nextName();

                Integer entityId = null;

                if (key.equals("id")) {
                  entityId = in.nextInt();
                  key = in.nextName();
                }

                if (key.equals("values")) {
                  in.beginArray();
                  {
                    while (in.hasNext()) {
                      in.beginObject();
                      {
                        key = in.nextName();
                        if (key.equals("name")) {
                          final var performanceMeasureType =
                              PerformanceMeasureType.valueOf(in.nextString());
                          in.nextName();
                          in.beginObject();
                          {
                            Double center = null;
                            Double halfWidth = null;

                            key = in.nextName();
                            if (key.equals("center")) {
                              center = in.nextDouble();
                            }

                            key = in.nextName();
                            if (key.equals("halfWidth")) {
                              halfWidth = in.nextDouble();
                            }

                            Objects.requireNonNull(
                                center, "Performance measure center is not specified");
                            Objects.requireNonNull(
                                halfWidth, "Performance measure half-width is not specified");

                            final var performanceMeasure =
                                new PerformanceMeasure(center, halfWidth);

                            if (entityId != null) {
                              performanceMeasureCollection.setPerformanceMeasure(
                                  system.getServiceCenterModel(entityId),
                                  performanceMeasureType,
                                  performanceMeasure);
                            } else {
                              performanceMeasureCollection.setSystemPerformanceMeasure(
                                  performanceMeasureType, performanceMeasure);
                            }
                          }
                          in.endObject();
                        }
                      }
                      in.endObject();
                    }
                  }
                  in.endArray();
                } else {
                  throw new RuntimeException("Expected values array but got " + key);
                }
              }
              in.endObject();
            }
          }
          in.endArray();
        }
      }
    }
    in.endObject();

    return performanceMeasureCollection;
  }
}
