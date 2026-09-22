package dev.askov.vipet.mvc.models.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.math3.distribution.ExponentialDistribution;

public final class ExponentialTimeDistributionModel extends TimeDistributionModel {

  public static final double DEFAULT_RATE = 1.0;

  private final DoubleProperty rateProperty = new SimpleDoubleProperty(this, "rate");

  public ExponentialTimeDistributionModel(final double rate) {
    super("Exponential", new ExponentialDistribution(1.0 / rate));

    rateProperty.set(rate);
    rateProperty.addListener(
        (rateValue, oldRateValue, newRateValue) -> {
          if (newRateValue.doubleValue() > 0.0) {
            setRealDistribution(
                new ExponentialDistribution(
                    getRandomGenerator(), 1.0 / newRateValue.doubleValue()));
          }
        });

    randomGeneratorObjectProperty.addListener(
        (randomGenerator, oldRandomGenerator, newRandomGenerator) ->
            setRealDistribution(new ExponentialDistribution(newRandomGenerator, 1.0 / getRate())));

    descriptionPropertyWrapper.bind(
        Bindings.createStringBinding(
            () -> "Exp(%s)".formatted(NumericFormatUtil.formatNumber(getRate())), rateProperty));
  }

  public ExponentialTimeDistributionModel() {
    this(DEFAULT_RATE);
  }

  public ExponentialTimeDistributionModel(
      final ExponentialTimeDistributionModel exponentialTimeDistribution) {
    this(exponentialTimeDistribution.getRate());
  }

  public DoubleProperty rateProperty() {
    return rateProperty;
  }

  public double getRate() {
    return rateProperty.get();
  }

  public void setRate(final double rate) {
    rateProperty.set(rate);
  }

  public double getScale() {
    return 1.0 / rateProperty.get();
  }

  @Override
  public String getPDFLaTeXFormula() {
    return """
            f\\left(x\\right)=
            \\begin{cases}
              \\lambda e^{-\\lambda x}\\text{, } & x\\geq 0; \\\\
              0\\text{, } & x<0.
            \\end{cases}
            """;
  }

  @Override
  public String getCDFLaTeXFormula() {
    return """
            F\\left(x\\right)=
            \\begin{cases}
              1-e^{-\\lambda x}\\text{, } & x\\geq 0; \\\\
              0\\text{, } & x<0.
            \\end{cases}
            """;
  }

  @Override
  public TimeDistributionModel interpolate(
      final TimeDistributionModel other, final double interpolationFactor) {
    if (other instanceof ExponentialTimeDistributionModel otherExponentialTimeDistribution) {
      return new ExponentialTimeDistributionModel(
          getRate()
              + interpolationFactor * (otherExponentialTimeDistribution.getRate() - getRate()));
    } else {
      throw new IllegalArgumentException("Cannot interpolate with a different distribution type.");
    }
  }
}
