package dev.askov.vipet.mvc.models.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.math3.distribution.ConstantRealDistribution;

public final class DeterministicTimeDistributionModel extends TimeDistributionModel {

  public static final double DEFAULT_VALUE = 1.0;

  private final DoubleProperty valueProperty = new SimpleDoubleProperty(this, "value");

  public DeterministicTimeDistributionModel(final double value) {
    super("Deterministic", new ConstantRealDistribution(value));

    valueProperty.addListener(
        (number, oldNumber, newNumber) -> {
          if (newNumber.doubleValue() > 0.0) {
            setRealDistribution(new ConstantRealDistribution(newNumber.doubleValue()));
          }
        });

    valueProperty.set(value);

    descriptionPropertyWrapper.bind(
        Bindings.createStringBinding(
            () -> "Det(%s)".formatted(NumericFormatUtil.formatNumber(getValue())), valueProperty));
  }

  public DeterministicTimeDistributionModel() {
    this(DEFAULT_VALUE);
  }

  public DeterministicTimeDistributionModel(
      final DeterministicTimeDistributionModel deterministicTimeDistribution) {
    this(deterministicTimeDistribution.getValue());
  }

  public DoubleProperty valueProperty() {
    return valueProperty;
  }

  public double getValue() {
    return valueProperty.get();
  }

  public void setValue(final double value) {
    valueProperty.set(value);
  }

  @Override
  public String getPDFLaTeXFormula() {
    return "f\\left(x\\right)=\\delta\\left(x-k\\right)";
  }

  @Override
  public String getCDFLaTeXFormula() {
    return """
            F\\left(x\\right)=
            \\begin{cases}
              0\\text{, } & x<k; \\\\
              1\\text{, } & x\\geq k.
            \\end{cases}
            """;
  }

  @Override
  public TimeDistributionModel interpolate(
      final TimeDistributionModel other, final double interpolationFactor) {
    if (other instanceof DeterministicTimeDistributionModel deterministicTimeDistribution) {
      final var value = getValue();
      final var otherValue = deterministicTimeDistribution.getValue();
      final var interpolatedValue = value + interpolationFactor * (otherValue - value);

      return new DeterministicTimeDistributionModel(interpolatedValue);
    } else {
      throw new IllegalArgumentException("Cannot interpolate with a different distribution type.");
    }
  }
}
