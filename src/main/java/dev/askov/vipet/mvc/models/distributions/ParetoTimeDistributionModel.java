package dev.askov.vipet.mvc.models.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.math3.distribution.ParetoDistribution;

public final class ParetoTimeDistributionModel extends TimeDistributionModel {

  public static final double DEFAULT_SCALE = 1.0;
  public static final double DEFAULT_SHAPE = 2.0;

  private final DoubleProperty scaleProperty = new SimpleDoubleProperty(this, "scale");
  private final DoubleProperty shapeProperty = new SimpleDoubleProperty(this, "shape");

  public ParetoTimeDistributionModel(final double scale, final double shape) {
    super("Pareto", new ParetoDistribution(scale, shape));

    scaleProperty.set(scale);
    scaleProperty.addListener(
        (scaleValue, oldScaleValue, newScaleValue) -> {
          if (newScaleValue.doubleValue() > 0.0) {
            setRealDistribution(
                new ParetoDistribution(
                    getRandomGenerator(), newScaleValue.doubleValue(), getShape()));
          }
        });

    shapeProperty.set(shape);
    shapeProperty.addListener(
        (shapeValue, oldShapeValue, newShapeValue) -> {
          if (newShapeValue.doubleValue() > 0.0) {
            setRealDistribution(
                new ParetoDistribution(
                    getRandomGenerator(), getScale(), newShapeValue.doubleValue()));
          }
        });

    randomGeneratorObjectProperty.addListener(
        (randomGenerator, oldRandomGenerator, newRandomGenerator) ->
            setRealDistribution(
                new ParetoDistribution(newRandomGenerator, getScale(), getShape())));

    descriptionPropertyWrapper.bind(
        Bindings.createStringBinding(
            () ->
                "Par(%s, %s)"
                    .formatted(
                        NumericFormatUtil.formatNumber(getScale()),
                        NumericFormatUtil.formatNumber(getShape())),
            scaleProperty,
            shapeProperty));
  }

  public ParetoTimeDistributionModel() {
    this(DEFAULT_SCALE, DEFAULT_SHAPE);
  }

  public ParetoTimeDistributionModel(final ParetoTimeDistributionModel paretoTimeDistribution) {
    this(paretoTimeDistribution.getScale(), paretoTimeDistribution.getShape());
  }

  public DoubleProperty scaleProperty() {
    return scaleProperty;
  }

  public double getScale() {
    return scaleProperty.get();
  }

  public void setScale(final double scale) {
    scaleProperty.set(scale);
  }

  public DoubleProperty shapeProperty() {
    return shapeProperty;
  }

  public double getShape() {
    return shapeProperty.get();
  }

  public void setShape(final double shape) {
    shapeProperty.set(shape);
  }

  @Override
  public String getPDFLaTeXFormula() {
    return """
            f\\left(x\\right)=
            \\begin{cases}
                \\dfrac{\\alpha x_\\text{m}^\\alpha}{x^{\\alpha+1}}\\text{, } & x\\geq x_\\text{m}; \\\\
                0\\text{, } & x<x_\\text{m}.
            \\end{cases}
            """;
  }

  @Override
  public String getCDFLaTeXFormula() {
    return """
            F\\left(x\\right)=
            \\begin{cases}
              1-\\left(\\dfrac{x_\\text{m}}{x}\\right)^\\alpha\\text{, } & x\\geq x_\\text{m}; \\\\
              0\\text{, } & x<x_\\text{m}.
            \\end{cases}
            """;
  }

  @Override
  public TimeDistributionModel interpolate(
      final TimeDistributionModel other, final double interpolationFactor) {
    if (other instanceof ParetoTimeDistributionModel paretoTimeDistribution) {
      return new ParetoTimeDistributionModel(
          getScale() * (1.0 - interpolationFactor)
              + paretoTimeDistribution.getScale() * interpolationFactor,
          getShape() * (1.0 - interpolationFactor)
              + paretoTimeDistribution.getShape() * interpolationFactor);
    } else {
      throw new IllegalArgumentException("The other distribution is not a Pareto distribution.");
    }
  }
}
