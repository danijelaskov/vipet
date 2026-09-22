package dev.askov.vipet.serialization.csv;

import com.opencsv.CSVWriter;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.serialization.PerformanceMeasuresExporter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CSVPerformanceMeasuresExporter extends PerformanceMeasuresExporter {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CSVPerformanceMeasuresExporter.class);

  public CSVPerformanceMeasuresExporter(
      File file,
      PerformanceMeasureCollection somePerformanceMeasureCollection,
      final ResourceBundle localizationBundle) {
    super(file, somePerformanceMeasureCollection, localizationBundle);
  }

  @Override
  public boolean export() {
    try (final var fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
      try (final var csvWriter = new CSVWriter(fileWriter)) {
        List<String> header = new LinkedList<>();

        header.add(localizationBundle.getString("performanceMeasures.name"));

        final List<PerformanceMeasureType> performanceMeasureTypes = new LinkedList<>();

        for (final var performanceMeasureType : PerformanceMeasureType.values()) {
          if (somePerformanceMeasureCollection.hasPerformanceMeasure(performanceMeasureType)) {
            header.add(performanceMeasureType.getName());
            performanceMeasureTypes.add(performanceMeasureType);
          }
        }

        csvWriter.writeNext(header.toArray(String[]::new));

        for (final var serviceCenterModel : somePerformanceMeasureCollection.getServiceCenters()) {
          final List<String> row = new LinkedList<>();

          row.add(serviceCenterModel.getName());

          for (final var performanceMeasureType : performanceMeasureTypes) {
            if (somePerformanceMeasureCollection.hasPerformanceMeasure(performanceMeasureType)) {
              row.add(
                  String.valueOf(
                      somePerformanceMeasureCollection.getPerformanceMeasure(
                          serviceCenterModel, performanceMeasureType)));
            }
          }

          csvWriter.writeNext(row.toArray(String[]::new));
        }

        final List<String> row = new LinkedList<>();

        row.add(localizationBundle.getString("performanceMeasures.system"));
        for (final var performanceMeasureType : performanceMeasureTypes) {
          if (somePerformanceMeasureCollection.hasSystemPerformanceMeasure(
              performanceMeasureType)) {
            row.add(
                String.valueOf(
                    somePerformanceMeasureCollection.getSystemPerformanceMeasure(
                        performanceMeasureType)));
          } else {
            row.add("");
          }
        }

        csvWriter.writeNext(row.toArray(String[]::new));
      }
    } catch (Exception e) {
      LOGGER.error("Error during performance measures export to CSV file: {}", e.getMessage());

      return false;
    }

    return true;
  }
}
