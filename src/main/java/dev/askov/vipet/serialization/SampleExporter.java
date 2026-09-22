package dev.askov.vipet.serialization;

import java.io.File;
import java.util.ResourceBundle;

public abstract class SampleExporter implements Exporter {

  private final File file;
  private final ResourceBundle resourceBundle;

  public SampleExporter(final File file, final ResourceBundle resourceBundle) {
    this.file = file;
    this.resourceBundle = resourceBundle;
  }

  public File getFile() {
    return file;
  }

  protected String getLocalizedString(final String key) {
    return resourceBundle.getString(key);
  }
}
