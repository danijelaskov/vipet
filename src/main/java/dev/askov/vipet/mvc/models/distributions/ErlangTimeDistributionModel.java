package dev.askov.vipet.mvc.models.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.commons.math3.distribution.GammaDistribution;

public final class ErlangTimeDistributionModel extends TimeDistributionModel {

  public static final double DEFAULT_RATE = 1.0;
  public static final int DEFAULT_SHAPE = 1;

  private final DoubleProperty rateProperty = new SimpleDoubleProperty(this, "rate");
  private final IntegerProperty shapeProperty = new SimpleIntegerProperty(this, "shape");

  public ErlangTimeDistributionModel(final double rate, final int shape) {
    super("Erlang", new GammaDistribution(shape, 1.0 / rate));

    rateProperty.set(rate);
    rateProperty.addListener(
        (rateValue, oldRateValue, newRateValue) -> {
          if (newRateValue.doubleValue() > 0.0) {
            setRealDistribution(
                new GammaDistribution(
                    getRandomGenerator(), getShape(), 1.0 / newRateValue.doubleValue()));
          }
        });

    shapeProperty.set(shape);
    shapeProperty.addListener(
        (shapeValue, oldShapeValue, newShapeValue) -> {
          if (newShapeValue.intValue() > 0.0) {
            setRealDistribution(
                new GammaDistribution(
                    getRandomGenerator(), newShapeValue.intValue(), 1.0 / getRate()));
          }
        });

    randomGeneratorObjectProperty.addListener(
        (randomGenerator, oldRandomGenerator, newRandomGenerator) ->
            setRealDistribution(
                new GammaDistribution(newRandomGenerator, getShape(), 1.0 / getRate())));

    descriptionPropertyWrapper.bind(
        Bindings.createStringBinding(
            () -> "Erl(%s, %s)".formatted(NumericFormatUtil.formatNumber(getRate()), getShape()),
            rateProperty,
            shapeProperty));
  }

  public ErlangTimeDistributionModel() {
    this(DEFAULT_RATE, DEFAULT_SHAPE);
  }

  public ErlangTimeDistributionModel(final ErlangTimeDistributionModel erlangTimeDistribution) {
    this(erlangTimeDistribution.getRate(), erlangTimeDistribution.getShape());
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

  public IntegerProperty shapeProperty() {
    return shapeProperty;
  }

  public int getShape() {
    return shapeProperty.get();
  }

  public void setShape(final int shape) {
    shapeProperty.set(shape);
  }

  public double getScale() {
    return 1.0 / rateProperty.get();
  }

  public void setScale(final double scale) {
    rateProperty.set(1.0 / scale);
  }

  @Override
  public String getPDFLaTeXFormula() {
    return """
            f\\left(x\\right)=
            \\begin{cases}
              \\dfrac{\\lambda^k x^{k-1}}{\\left(k-1\\right)!}e^{-\\lambda x}\\text{, } & x\\geq 0; \\\\
              0\\text{, } & x<0.
            \\end{cases}
           """;
  }

  @Override
  public String getCDFLaTeXFormula() {
    return """
            F\\left(x\\right)=
            \\begin{cases}
              1-\\sum_{i=0}^{k-1}\\dfrac{\\left(\\lambda x\\right)^i}{i!}e^{-\\lambda x}\\text{, } & x\\geq 0; \\\\
              0\\text{, } & x<0.
            \\end{cases}
           """;
  }

  @Override
  public TimeDistributionModel interpolate(
      final TimeDistributionModel other, final double interpolationFactor) {
    if (other instanceof ErlangTimeDistributionModel otherErlangTimeDistribution) {
      return new ErlangTimeDistributionModel(
          getRate() + interpolationFactor * (otherErlangTimeDistribution.getRate() - getRate()),
          getShape()
              + (int)
                  (interpolationFactor * (otherErlangTimeDistribution.getShape() - getShape())));
    } else {
      throw new IllegalArgumentException("Cannot interpolate with a different distribution type.");
    }
  }
}
