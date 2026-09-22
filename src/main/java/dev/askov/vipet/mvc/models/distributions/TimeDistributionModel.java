package dev.askov.vipet.mvc.models.distributions;

import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

public abstract class TimeDistributionModel {

  private final ReadOnlyStringWrapper namePropertyWrapper = new ReadOnlyStringWrapper(this, "name");
  private final ReadOnlyObjectWrapper<RealDistribution> distributionProperty =
      new ReadOnlyObjectWrapper<>(this, "distribution");
  private final ReadOnlyDoubleWrapper meanPropertyWrapper =
      new ReadOnlyDoubleWrapper(this, "value");
  private final ReadOnlyDoubleWrapper variancePropertyWrapper =
      new ReadOnlyDoubleWrapper(this, "variance");
  private final ReadOnlyDoubleWrapper standardDeviationPropertyWrapper =
      new ReadOnlyDoubleWrapper(this, "standardDeviation");
  private final ReadOnlyObjectWrapper<Function<Double, Double>>
      probabilityDensityFunctionPropertyWrapper =
          new ReadOnlyObjectWrapper<>(this, "probabilityDensityFunction");
  private final ReadOnlyObjectWrapper<Function<Double, Double>>
      cumulativeDistributionFunctionPropertyWrapper =
          new ReadOnlyObjectWrapper<>(this, "cumulativeDistributionFunction");

  protected final ReadOnlyStringWrapper descriptionPropertyWrapper =
      new ReadOnlyStringWrapper(this, "description");

  protected final ObjectProperty<RandomGenerator> randomGeneratorObjectProperty =
      new SimpleObjectProperty<>(this, "randomGenerator", null);

  public TimeDistributionModel(final String name, final RealDistribution realDistribution) {
    namePropertyWrapper.set(name);
    distributionProperty.set(realDistribution);

    meanPropertyWrapper.bind(
        Bindings.createDoubleBinding(
            () -> distributionProperty.get().getNumericalMean(), distributionProperty));
    variancePropertyWrapper.bind(
        Bindings.createDoubleBinding(
            () -> distributionProperty.get().getNumericalVariance(), distributionProperty));
    standardDeviationPropertyWrapper.bind(
        Bindings.createDoubleBinding(
            () -> Math.sqrt(distributionProperty.get().getNumericalVariance()),
            distributionProperty));
    probabilityDensityFunctionPropertyWrapper.bind(
        Bindings.createObjectBinding(
            () -> x -> distributionProperty.get().density(x), distributionProperty));
    cumulativeDistributionFunctionPropertyWrapper.bind(
        Bindings.createObjectBinding(
            () -> x -> distributionProperty.get().cumulativeProbability(x), distributionProperty));
  }

  public ReadOnlyStringProperty nameProperty() {
    return namePropertyWrapper.getReadOnlyProperty();
  }

  public String getName() {
    return namePropertyWrapper.get();
  }

  public ReadOnlyDoubleProperty meanProperty() {
    return meanPropertyWrapper.getReadOnlyProperty();
  }

  public double getMean() {
    return meanPropertyWrapper.get();
  }

  public ReadOnlyDoubleProperty varianceProperty() {
    return variancePropertyWrapper.getReadOnlyProperty();
  }

  public double getVariance() {
    return variancePropertyWrapper.get();
  }

  public ReadOnlyDoubleProperty standardDeviationProperty() {
    return standardDeviationPropertyWrapper.getReadOnlyProperty();
  }

  public double getStandardDeviation() {
    return standardDeviationPropertyWrapper.get();
  }

  public ObjectProperty<Function<Double, Double>> probabilityDensityFunctionProperty() {
    return probabilityDensityFunctionPropertyWrapper;
  }

  public Function<Double, Double> getProbabilityDensityFunction() {
    return probabilityDensityFunctionPropertyWrapper.get();
  }

  public void setProbabilityDensityFunction(Function<Double, Double> probabilityDensityFunction) {
    this.probabilityDensityFunctionPropertyWrapper.set(probabilityDensityFunction);
  }

  public ObjectProperty<Function<Double, Double>> cumulativeDistributionFunctionProperty() {
    return cumulativeDistributionFunctionPropertyWrapper;
  }

  public Function<Double, Double> getCumulativeDistributionFunction() {
    return cumulativeDistributionFunctionPropertyWrapper.get();
  }

  public void setCumulativeDistributionFunction(
      Function<Double, Double> cumulativeDistributionFunction) {
    this.cumulativeDistributionFunctionPropertyWrapper.set(cumulativeDistributionFunction);
  }

  public double getNextRandomValue() {
    return distributionProperty.get().sample();
  }

  public ReadOnlyStringProperty descriptionProperty() {
    return descriptionPropertyWrapper.getReadOnlyProperty();
  }

  public String getDescription() {
    return descriptionPropertyWrapper.get();
  }

  public ReadOnlyObjectProperty<RealDistribution> distributionProperty() {
    return distributionProperty.getReadOnlyProperty();
  }

  protected void setRealDistribution(final RealDistribution realDistribution) {
    distributionProperty.set(realDistribution);
  }

  public ObjectProperty<RandomGenerator> randomGeneratorProperty() {
    return randomGeneratorObjectProperty;
  }

  public RandomGenerator getRandomGenerator() {
    return randomGeneratorObjectProperty.get();
  }

  public void setRandomGenerator(final RandomGenerator randomGenerator) {
    this.randomGeneratorObjectProperty.set(randomGenerator);
  }

  @Override
  public String toString() {
    return getDescription();
  }

  public abstract String getPDFLaTeXFormula();

  public abstract String getCDFLaTeXFormula();

  public abstract TimeDistributionModel interpolate(
      final TimeDistributionModel other, final double interpolationFactor);
}
