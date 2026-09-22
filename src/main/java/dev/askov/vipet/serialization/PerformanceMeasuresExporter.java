package dev.askov.vipet.serialization;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import java.io.File;
import java.util.ResourceBundle;

public abstract class PerformanceMeasuresExporter implements Exporter {

  protected final File file;
  protected final PerformanceMeasureCollection somePerformanceMeasureCollection;
  protected final ResourceBundle localizationBundle;

  public PerformanceMeasuresExporter(
      final File file,
      PerformanceMeasureCollection somePerformanceMeasureCollection,
      final ResourceBundle resourceBundle) {
    this.file = file;
    this.somePerformanceMeasureCollection = somePerformanceMeasureCollection;
    this.localizationBundle = resourceBundle;
  }
}
