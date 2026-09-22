package dev.askov.vipet.mvc.common;

import dev.askov.vipet.common.NumericFormatUtil;
import java.text.ParseException;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

public final class LongSpinnerValueFactory extends SpinnerValueFactory<Long> {

  private final LongProperty minProperty;
  private final LongProperty maxProperty;
  private final LongProperty stepProperty;

  public LongSpinnerValueFactory(long min, long max) {
    this(min, max, min);
  }

  public LongSpinnerValueFactory(long min, long max, long initialValue) {
    this(min, max, initialValue, 1);
  }

  public LongSpinnerValueFactory(long min, long max, long initialValue, long amountToStepBy) {
    if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
    if (initialValue < min || initialValue > max)
      throw new IllegalArgumentException("Initial value out of bounds");

    minProperty = new SimpleLongProperty(this, "min", min);
    maxProperty = new SimpleLongProperty(this, "max", max);
    stepProperty = new SimpleLongProperty(this, "amountToStepBy", amountToStepBy);

    setConverter(
        new StringConverter<>() {
          @Override
          public String toString(Long value) {
            return value != null ? NumericFormatUtil.formatNumber(value) : "";
          }

          @Override
          public Long fromString(String string) {
            try {
              return NumericFormatUtil.parseLong(string.trim());
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

  public long getMin() {
    return minProperty.get();
  }

  public void setMin(long value) {
    minProperty.set(value);
  }

  public LongProperty minProperty() {
    return minProperty;
  }

  public long getMax() {
    return maxProperty.get();
  }

  public void setMax(long value) {
    maxProperty.set(value);
  }

  public LongProperty maxProperty() {
    return maxProperty;
  }

  public long getAmountToStepBy() {
    return stepProperty.get();
  }

  public void setAmountToStepBy(long value) {
    stepProperty.set(value);
  }

  public LongProperty amountToStepByProperty() {
    return stepProperty;
  }
}
