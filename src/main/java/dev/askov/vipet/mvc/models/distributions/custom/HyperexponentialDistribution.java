package dev.askov.vipet.mvc.models.distributions.custom;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

public final class HyperexponentialDistribution extends AbstractRealDistribution {

  private final double[] probabilities;
  private final double[] rates;
  private final double[] cumulativeProbabilities;

  public HyperexponentialDistribution(
      final RandomGenerator randomGenerator, double[] probabilities, final double[] rates) {
    super(randomGenerator);

    if (probabilities.length != rates.length) {
      throw new IllegalArgumentException(
          "Rates and probabilities arrays must have the same length.");
    }

    this.probabilities = probabilities;
    this.rates = rates;

    cumulativeProbabilities = new double[probabilities.length];
    double sum = 0;
    for (var i = 0; i < probabilities.length; i++) {
      sum += probabilities[i];
      cumulativeProbabilities[i] = sum;
    }

    if (Math.abs(sum - 1.0) > 1e-9) {
      throw new IllegalArgumentException(
          "Probabilities must sum to 1. Instead, they sum to " + sum);
    }
  }

  public HyperexponentialDistribution(final double[] probabilities, final double[] rates) {
    this(null, probabilities, rates);
  }

  @Override
  public double density(double x) {
    if (x < 0) {
      return 0;
    }

    double density = 0;
    for (var i = 0; i < rates.length; i++) {
      density += probabilities[i] * rates[i] * Math.exp(-rates[i] * x);
    }

    return density;
  }

  @Override
  public double cumulativeProbability(double x) {
    if (x < 0) {
      return 0;
    }

    double cdf = 0;
    for (var i = 0; i < rates.length; i++) {
      cdf += probabilities[i] * (1 - Math.exp(-rates[i] * x));
    }

    return cdf;
  }

  @Override
  public double getNumericalMean() {
    double mean = 0;

    for (var i = 0; i < rates.length; i++) {
      mean += probabilities[i] / rates[i];
    }

    return mean;
  }

  @Override
  public double getNumericalVariance() {
    double variance = 0;

    for (var i = 0; i < rates.length; i++) {
      variance += probabilities[i] / (rates[i] * rates[i]);
    }

    return variance;
  }

  @Override
  public double getSupportLowerBound() {
    return 0;
  }

  @Override
  public double getSupportUpperBound() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  @Deprecated
  public boolean isSupportLowerBoundInclusive() {
    return true;
  }

  @Override
  @Deprecated
  public boolean isSupportUpperBoundInclusive() {
    return false;
  }

  @Override
  public boolean isSupportConnected() {
    return true;
  }

  @Override
  public double sample() {
    var randomValue = random.nextDouble();
    var componentIndex = 0;

    while (componentIndex < cumulativeProbabilities.length - 1
        && randomValue > cumulativeProbabilities[componentIndex]) {
      componentIndex++;
    }

    return -Math.log(1.0 - random.nextDouble()) / rates[componentIndex];
  }
}
