package dev.askov.vipet.serialization.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.PerformanceMeasuresExporter;
import dev.askov.vipet.serialization.json.performancemeasures.PerformanceMeasuresTypeAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSONPerformanceMeasuresExporter extends PerformanceMeasuresExporter {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JSONPerformanceMeasuresExporter.class);

  private final Gson gson;

  public JSONPerformanceMeasuresExporter(
      final File file,
      final PerformanceMeasureCollection somePerformanceMeasureCollection,
      final ResourceBundle resourceBundle,
      final NetworkModel networkModel) {
    super(file, somePerformanceMeasureCollection, resourceBundle);

    gson =
        new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(
                PerformanceMeasureCollection.class,
                new PerformanceMeasuresTypeAdapter(networkModel))
            .create();
  }

  @Override
  public boolean export() {
    try (final var fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
      fileWriter.write(gson.toJson(somePerformanceMeasureCollection));
    } catch (IOException exception) {
      LOGGER.error("Failed to export performance measures to JSON", exception);

      return false;
    }

    return true;
  }
}
