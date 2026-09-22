package dev.askov.vipet.serialization.excel;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import java.io.File;
import java.util.ResourceBundle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class XLSXPerformanceMeasuresExporter extends ExcelPerformanceMeasuresExporter {

  public XLSXPerformanceMeasuresExporter(
      final File file,
      final PerformanceMeasureCollection somePerformanceMeasureCollection,
      final ResourceBundle resourceBundle) {
    super(file, somePerformanceMeasureCollection, resourceBundle);
  }

  @Override
  protected Workbook createWorkbook() {
    return new XSSFWorkbook();
  }
}
