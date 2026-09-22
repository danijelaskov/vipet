package dev.askov.vipet.serialization.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.Importer;
import dev.askov.vipet.serialization.json.performancemeasures.PerformanceMeasuresTypeAdapter;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSONPerformanceMeasuresImporter
    implements Importer<PerformanceMeasureCollection> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JSONPerformanceMeasuresImporter.class);

  private final File file;
  private final Gson gson;

  public JSONPerformanceMeasuresImporter(final File file, final NetworkModel networkModel) {
    this.file = file;

    gson =
        new GsonBuilder()
            .registerTypeAdapter(
                PerformanceMeasureCollection.class,
                new PerformanceMeasuresTypeAdapter(networkModel))
            .create();
  }

  @Override
  public PerformanceMeasureCollection importData() {
    try (final var fileReader = new FileReader(file, StandardCharsets.UTF_8)) {
      return gson.fromJson(fileReader, PerformanceMeasureCollection.class);
    } catch (Exception exception) {
      LOGGER.error("Failed to import performance measures JSON", exception);
      return null;
    }
  }
}
