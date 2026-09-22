package dev.askov.vipet.serialization;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import java.io.InputStream;

public abstract class NetworkImporter {

  protected final InputStream inputStream;
  protected NetworkModel networkModel;

  public NetworkImporter(final InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public abstract boolean importNetwork();

  public NetworkModel getNetwork() {
    return networkModel;
  }
}
