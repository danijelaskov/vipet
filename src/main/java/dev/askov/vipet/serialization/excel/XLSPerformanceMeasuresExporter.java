package dev.askov.vipet.serialization.excel;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import java.io.File;
import java.util.ResourceBundle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

public final class XLSPerformanceMeasuresExporter extends ExcelPerformanceMeasuresExporter {

  public XLSPerformanceMeasuresExporter(
      final File file,
      final PerformanceMeasureCollection somePerformanceMeasureCollection,
      final ResourceBundle resourceBundle) {
    super(file, somePerformanceMeasureCollection, resourceBundle);
  }

  @Override
  protected Workbook createWorkbook() {
    return new HSSFWorkbook();
  }
}
