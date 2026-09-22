package dev.askov.vipet.serialization.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.NetworkExporter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSONNetworkExporter extends NetworkExporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JSONNetworkExporter.class);

  private final Gson gson;

  public JSONNetworkExporter(OutputStream outputStream, NetworkModel networkModel) {
    super(outputStream, networkModel);

    gson =
        new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new VPETTypeAdapterFactory())
            .create();
  }

  @Override
  public boolean export() {
    try (var outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      outputStreamWriter.write(gson.toJson(networkModel));
      LOGGER.info("Exported network {}", networkModel.getName());
    } catch (IOException exception) {
      LOGGER.error("Failed to export network", exception);
      return false;
    }

    return true;
  }
}
