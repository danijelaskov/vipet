package dev.askov.vipet.serialization;

import java.io.IOException;

public class NetworkSpecificationException extends IOException {

  private static final String GENERAL_MESSAGE = "Network specification error: ";

  public NetworkSpecificationException(final String message) {
    super(GENERAL_MESSAGE + message);
  }
}
