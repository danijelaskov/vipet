package dev.askov.vipet.serialization.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.NetworkImporter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSONNetworkImporter extends NetworkImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JSONNetworkImporter.class);

  private final Gson gson;

  public JSONNetworkImporter(final InputStream inputStream) {
    super(inputStream);

    gson =
        new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new VPETTypeAdapterFactory())
            .create();
  }

  @Override
  public boolean importNetwork() {
    try (final var inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      networkModel = gson.fromJson(inputStreamReader, NetworkModel.class);
    } catch (Exception exception) {
      LOGGER.error("Failed to import network JSON", exception);
      return false;
    }

    return true;
  }
}
