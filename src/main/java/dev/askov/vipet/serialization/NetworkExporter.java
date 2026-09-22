package dev.askov.vipet.serialization;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import java.io.OutputStream;

public abstract class NetworkExporter implements Exporter {

  protected final OutputStream outputStream;
  protected final NetworkModel networkModel;

  public NetworkExporter(final OutputStream outputStream, final NetworkModel networkModel) {
    this.outputStream = outputStream;
    this.networkModel = networkModel;
  }
}
