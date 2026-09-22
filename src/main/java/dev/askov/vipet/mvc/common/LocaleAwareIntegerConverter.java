package dev.askov.vipet.mvc.common;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.util.StringConverter;

public final class LocaleAwareIntegerConverter extends StringConverter<Integer> {

  @Override
  public String toString(Integer number) {
    return NumericFormatUtil.useLocalizedGroupingSeparator(number);
  }

  @Override
  public Integer fromString(String string) {
    string = NumericFormatUtil.useDefaultGroupingSeparator(string);
    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
