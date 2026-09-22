package dev.askov.vipet.serialization.excel;

import dev.askov.vipet.core.simulator.sampling.generation.SampleGenerator;
import dev.askov.vipet.mvc.models.network.NamedModel;
import dev.askov.vipet.serialization.SampleExporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XLSXSampleExporter extends SampleExporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(XLSXSampleExporter.class);
  private static final String FILE_EXTENSION = ".xlsx";

  private final List<SampleGenerator<?>> sampleGenerators;

  public XLSXSampleExporter(
      final File parentDirectory,
      final List<SampleGenerator<?>> sampleGenerators,
      final NamedModel namedModel,
      final ResourceBundle resourceBundle) {
    super(new File(parentDirectory, namedModel.getName() + FILE_EXTENSION), resourceBundle);
    this.sampleGenerators = sampleGenerators;

    LOGGER.debug(
        "Created XLSX sample exporter for {} with {} sample generators",
        namedModel.getName(),
        sampleGenerators.size());
  }

  @Override
  public boolean export() {
    try (final OutputStream outputStream = new FileOutputStream(getFile());
        final Workbook workbook = new XSSFWorkbook()) {

      for (final var sampleGenerator : sampleGenerators) {
        LOGGER.debug(
            "Exporting {} samples for {} and performance measure {} to XLSX file {}",
            sampleGenerator.getSampleSize(),
            sampleGenerator.getNamedModel().getName(),
            sampleGenerator.getPerformanceMeasureType().getName(),
            getFile().getPath());

        final var sheet =
            workbook.createSheet(sampleGenerator.getPerformanceMeasureType().getName());

        final var centeredCellStyle = workbook.createCellStyle();
        centeredCellStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        sheet.createRow(0);

        final var firstHeaderCell = sheet.getRow(0).createCell(0, CellType.STRING);
        firstHeaderCell.setCellValue(getLocalizedString("sampleExport.headerRow.time"));
        firstHeaderCell.setCellStyle(centeredCellStyle);

        final var secondHeaderCell = sheet.getRow(0).createCell(1, CellType.STRING);
        secondHeaderCell.setCellValue(getLocalizedString("sampleExport.headerRow.value"));
        secondHeaderCell.setCellStyle(centeredCellStyle);

        final var thirdHeaderCell = sheet.getRow(0).createCell(2, CellType.STRING);
        thirdHeaderCell.setCellValue(getLocalizedString("sampleExport.headerRow.weight"));
        thirdHeaderCell.setCellStyle(centeredCellStyle);

        for (var i = 1; i <= sampleGenerator.getSampleSize(); i++) {
          sheet.createRow(i);

          final var firstCell = sheet.getRow(i).createCell(0, CellType.NUMERIC);
          firstCell.setCellValue(sampleGenerator.getSample().get(i - 1).time());
          firstCell.setCellStyle(centeredCellStyle);

          final var secondCell = sheet.getRow(i).createCell(1, CellType.NUMERIC);
          secondCell.setCellValue(sampleGenerator.getSample().get(i - 1).value());
          secondCell.setCellStyle(centeredCellStyle);

          final var thirdCell = sheet.getRow(i).createCell(2, CellType.NUMERIC);
          thirdCell.setCellValue(sampleGenerator.getSample().get(i - 1).weight());
          thirdCell.setCellStyle(centeredCellStyle);
        }
      }

      workbook.write(outputStream);
      LOGGER.debug("Successfully exported samples to XLSX file {}", getFile().getPath());
    } catch (final Exception e) {
      LOGGER.error(
          "Error while exporting samples to {} file: {}", getFile().getPath(), e.getMessage(), e);

      return false;
    }

    return true;
  }
}
