package dev.askov.vipet.mvc.models.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.math3.distribution.UniformRealDistribution;

public final class UniformTimeDistributionModel extends TimeDistributionModel {

  public static final double DEFAULT_MIN = 0.0;
  public static final double DEFAULT_MAX = 1.0;

  private final DoubleProperty minProperty = new SimpleDoubleProperty(this, "min");
  private final DoubleProperty maxProperty = new SimpleDoubleProperty(this, "max");

  public UniformTimeDistributionModel(final double min, final double max) {
    super("Uniform", new UniformRealDistribution(min, max));

    minProperty.set(min);
    minProperty.addListener(
        (minValue, oldMinValue, newMinValue) -> {
          if (newMinValue.doubleValue() < getMax()) {
            setRealDistribution(
                new UniformRealDistribution(
                    getRandomGenerator(), newMinValue.doubleValue(), getMax()));
          }
        });

    maxProperty.set(max);
    maxProperty.addListener(
        (maxValue, oldMaxValue, newMaxValue) -> {
          if (newMaxValue.doubleValue() > getMin()) {
            setRealDistribution(
                new UniformRealDistribution(
                    getRandomGenerator(), getMin(), newMaxValue.doubleValue()));
          }
        });

    randomGeneratorObjectProperty.addListener(
        (randomGenerator, oldRandomGenerator, newRandomGenerator) ->
            setRealDistribution(
                new UniformRealDistribution(newRandomGenerator, getMin(), getMax())));

    descriptionPropertyWrapper.bind(
        Bindings.createStringBinding(
            () ->
                "Unif(%s, %s)"
                    .formatted(
                        NumericFormatUtil.formatNumber(getMin()),
                        NumericFormatUtil.formatNumber(getMax())),
            minProperty,
            maxProperty));
  }

  public UniformTimeDistributionModel() {
    this(DEFAULT_MIN, DEFAULT_MAX);
  }

  public UniformTimeDistributionModel(final UniformTimeDistributionModel uniformTimeDistribution) {
    this(uniformTimeDistribution.getMin(), uniformTimeDistribution.getMax());
  }

  public DoubleProperty minProperty() {
    return minProperty;
  }

  public double getMin() {
    return minProperty.get();
  }

  public void setMin(final double min) {
    this.minProperty.set(min);
  }

  public DoubleProperty maxProperty() {
    return maxProperty;
  }

  public double getMax() {
    return maxProperty.get();
  }

  public void setMax(final double max) {
    this.maxProperty.set(max);
  }

  @Override
  public String getPDFLaTeXFormula() {
    return """
            f\\left(x\\right)=
            \\begin{cases}
              \\dfrac{1}{b-a}\\text{, } & a\\leq x\\leq b; \\\\
              0\\text{, } & x<a \\lor x>b.
            \\end{cases}
            """;
  }

  @Override
  public String getCDFLaTeXFormula() {
    return """
            F\\left(x\\right)=
            \\begin{cases}
              0\\text{, } & x<a; \\\\
              \\dfrac{x-a}{b-a}\\text{, } & a\\leq x\\leq b; \\\\
              1\\text{, } & x>b.
            \\end{cases}
            """;
  }

  @Override
  public TimeDistributionModel interpolate(
      final TimeDistributionModel other, final double interpolationFactor) {
    if (other instanceof UniformTimeDistributionModel uniformTimeDistribution) {
      return new UniformTimeDistributionModel(
          getMin() * (1.0 - interpolationFactor)
              + uniformTimeDistribution.getMin() * interpolationFactor,
          getMax() * (1.0 - interpolationFactor)
              + uniformTimeDistribution.getMax() * interpolationFactor);
    } else {
      throw new IllegalArgumentException(
          "The other distribution is not a UniformTimeDistributionModel.");
    }
  }
}
