package dev.askov.vipet.mvc.common;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeUnit;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

public class TimeUnitAwareNumberConverter extends StringConverter<Number> {

  private final ChoiceBox<TimeUnit> timeUnitChoiceBox;

  public TimeUnitAwareNumberConverter(ChoiceBox<TimeUnit> timeUnitChoiceBox) {
    this.timeUnitChoiceBox = timeUnitChoiceBox;
  }

  @Override
  public String toString(Number modelMin) {
    return NumericFormatUtil.useLocalizedDecimalSeparator(
        modelMin.doubleValue() / timeUnitChoiceBox.getValue().convertToSeconds());
  }

  @Override
  public Number fromString(String minTextFieldValue) {
    minTextFieldValue = NumericFormatUtil.useDefaultDecimalSeparator(minTextFieldValue);
    try {
      return Double.parseDouble(minTextFieldValue)
          * timeUnitChoiceBox.getValue().convertToSeconds();
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
