package dev.askov.vipet.serialization.excel;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.serialization.PerformanceMeasuresExporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExcelPerformanceMeasuresExporter extends PerformanceMeasuresExporter {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExcelPerformanceMeasuresExporter.class);
  private static final int DEFAULT_NUMBER_OF_CHARACTERS = 30;
  private static final int COLUMN_WIDTH = DEFAULT_NUMBER_OF_CHARACTERS * 256;

  public ExcelPerformanceMeasuresExporter(
      final File file,
      final PerformanceMeasureCollection somePerformanceMeasureCollection,
      final ResourceBundle resourceBundle) {
    super(file, somePerformanceMeasureCollection, resourceBundle);
  }

  @Override
  public boolean export() {
    try (final var workbook = createWorkbook();
        final OutputStream outputStream = new FileOutputStream(file)) {
      final var sheet = workbook.createSheet(localizationBundle.getString("excel.sheetTitle"));

      final List<PerformanceMeasureType> performanceMeasureTypes = new LinkedList<>();

      for (final var performanceMeasureType : PerformanceMeasureType.values()) {
        if (somePerformanceMeasureCollection.hasPerformanceMeasure(performanceMeasureType)) {
          performanceMeasureTypes.add(performanceMeasureType);
        }
      }

      final var headerRow = sheet.createRow(0);

      final var centeredCellStyle = workbook.createCellStyle();
      centeredCellStyle.setAlignment(HorizontalAlignment.CENTER);
      centeredCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

      final var firstHeaderCell = headerRow.createCell(0, CellType.STRING);

      firstHeaderCell.setCellValue(localizationBundle.getString("name"));
      firstHeaderCell.setCellStyle(centeredCellStyle);

      sheet.setColumnWidth(0, COLUMN_WIDTH);

      var columnIndex = 1;
      for (final var performanceMeasureType : performanceMeasureTypes) {
        final var headerCell = headerRow.createCell(columnIndex, CellType.STRING);

        headerCell.setCellValue(performanceMeasureType.getName());
        headerCell.setCellStyle(centeredCellStyle);

        sheet.setColumnWidth(columnIndex++, COLUMN_WIDTH);
      }

      var rowIndex = 1;
      for (final var serviceCenterModel : somePerformanceMeasureCollection.getServiceCenters()) {
        final var row = sheet.createRow(rowIndex);
        final var firstCell = row.createCell(0, CellType.STRING);

        firstCell.setCellValue(serviceCenterModel.getName());
        firstCell.setCellStyle(centeredCellStyle);

        columnIndex = 1;
        for (final var performanceMeasureType : performanceMeasureTypes) {
          final var performanceMeasure =
              somePerformanceMeasureCollection.getPerformanceMeasure(
                  serviceCenterModel, performanceMeasureType);
          final var cell =
              row.createCell(
                  columnIndex,
                  performanceMeasure.isUncertain() ? CellType.STRING : CellType.NUMERIC);

          if (performanceMeasure.isUncertain()) {
            cell.setCellValue(performanceMeasure.toString());
          } else {
            cell.setCellValue(performanceMeasure.center());
          }
          cell.setCellStyle(centeredCellStyle);

          columnIndex++;
        }

        rowIndex++;
      }

      final var row = sheet.createRow(rowIndex);
      final var firstCell = row.createCell(0, CellType.STRING);

      firstCell.setCellValue(localizationBundle.getString("system"));
      firstCell.setCellStyle(centeredCellStyle);

      columnIndex = 1;
      for (final var performanceMeasureType : performanceMeasureTypes) {
        final var performanceMeasure =
            somePerformanceMeasureCollection.getSystemPerformanceMeasure(performanceMeasureType);
        if (performanceMeasure != null) {
          final var cell =
              row.createCell(
                  columnIndex,
                  performanceMeasure.isUncertain() ? CellType.STRING : CellType.NUMERIC);

          if (somePerformanceMeasureCollection.hasSystemPerformanceMeasure(
              performanceMeasureType)) {
            if (performanceMeasure.isUncertain()) {
              cell.setCellValue(performanceMeasure.toString());
            } else {
              cell.setCellValue(performanceMeasure.center());
            }
          }
          cell.setCellStyle(centeredCellStyle);
        }
        columnIndex++;
      }

      workbook.write(outputStream);

      return true;
    } catch (final Exception e) {
      LOGGER.error("Error while exporting performance measures to Excel", e);
    }

    return false;
  }

  protected abstract Workbook createWorkbook();
}
