package dev.askov.vipet.mvc.common;

import dev.askov.vipet.common.NumericFormatUtil;
import java.text.ParseException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

public final class IntegerSpinnerValueFactory extends SpinnerValueFactory<Integer> {

  private final IntegerProperty minProperty;
  private final IntegerProperty maxProperty;
  private final IntegerProperty stepProperty;

  public IntegerSpinnerValueFactory(int min, int max) {
    this(min, max, min);
  }

  public IntegerSpinnerValueFactory(int min, int max, int initialValue) {
    this(min, max, initialValue, 1);
  }

  public IntegerSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy) {
    if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
    if (initialValue < min || initialValue > max)
      throw new IllegalArgumentException("Initial value out of bounds");

    minProperty = new SimpleIntegerProperty(this, "min", min);
    maxProperty = new SimpleIntegerProperty(this, "max", max);
    stepProperty = new SimpleIntegerProperty(this, "amountToStepBy", amountToStepBy);

    setConverter(
        new StringConverter<>() {
          @Override
          public String toString(Integer value) {
            return value != null ? NumericFormatUtil.formatNumber(value) : "";
          }

          @Override
          public Integer fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
              return getValue();
            }

            try {
              return NumericFormatUtil.parseInteger(string.trim());
            } catch (ParseException e) {
              return getValue();
            }
          }
        });

    setValue(initialValue);
  }

  @Override
  public void decrement(int steps) {
    final var newValue = getValue() - steps * getAmountToStepBy();
    setValue(Math.max(newValue, getMin()));
  }

  @Override
  public void increment(int steps) {
    final var newValue = getValue() + steps * getAmountToStepBy();
    setValue(Math.min(newValue, getMax()));
  }

  public int getMin() {
    return minProperty.get();
  }

  public void setMin(int value) {
    minProperty.set(value);
  }

  public IntegerProperty minProperty() {
    return minProperty;
  }

  public int getMax() {
    return maxProperty.get();
  }

  public void setMax(int value) {
    maxProperty.set(value);
  }

  public IntegerProperty maxProperty() {
    return maxProperty;
  }

  public int getAmountToStepBy() {
    return stepProperty.get();
  }

  public void setAmountToStepBy(int value) {
    stepProperty.set(value);
  }

  public IntegerProperty amountToStepByProperty() {
    return stepProperty;
  }
}
