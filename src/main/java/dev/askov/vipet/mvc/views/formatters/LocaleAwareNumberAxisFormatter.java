package dev.askov.vipet.mvc.views.formatters;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.scene.chart.NumberAxis;

public final class LocaleAwareNumberAxisFormatter extends NumberAxis.DefaultFormatter {

  public LocaleAwareNumberAxisFormatter(NumberAxis axis) {
    super(axis);
  }

  @Override
  public String toString(final Number number) {
    return NumericFormatUtil.formatNumber(number.doubleValue());
  }
}
