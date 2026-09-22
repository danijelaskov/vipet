package dev.askov.vipet.mvc.common;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.util.StringConverter;

public final class LocaleAwareDoubleConverter extends StringConverter<Double> {

  @Override
  public String toString(Double number) {
    return NumericFormatUtil.useLocalizedDecimalSeparator(number);
  }

  @Override
  public Double fromString(String string) {
    string = NumericFormatUtil.useDefaultDecimalSeparator(string);
    try {
      return Double.parseDouble(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
