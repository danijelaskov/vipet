package dev.askov.vipet.mvc.models.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.mvc.models.distributions.custom.HyperexponentialDistribution;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HyperexponentialTimeDistributionModel extends TimeDistributionModel {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(HyperexponentialTimeDistributionModel.class);

  @Override
  public String getPDFLaTeXFormula() {
    return "f\\left(x\\right)=\\sum_{i=1}^n p_i \\lambda_i e^{-\\lambda_i x}.";
  }

  @Override
  public String getCDFLaTeXFormula() {
    return "F\\left(x\\right)=\\sum_{i=1}^n p_i \\left(1-e^{-\\lambda_i x}\\right).";
  }

  public static class ProbabilityRatePair {
    public static final double DEFAULT_PROBABILITY = 1.0;
    public static final double DEFAULT_RATE = ExponentialTimeDistributionModel.DEFAULT_RATE;

    private final DoubleProperty probability = new SimpleDoubleProperty(this, "probability");
    private final DoubleProperty rate = new SimpleDoubleProperty(this, "rate");

    public ProbabilityRatePair(final double probability, final double rate) {
      if (probability < 0.0 || probability > 1.0) {
        throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
      }
      this.probability.set(probability);

      if (rate <= 0.0) {
        throw new IllegalArgumentException("Rate must be greater than 0.0");
      }
      this.rate.set(rate);
    }

    public ProbabilityRatePair() {
      this(DEFAULT_PROBABILITY, DEFAULT_RATE);
    }

    public DoubleProperty probabilityProperty() {
      return probability;
    }

    public double probability() {
      return probability.get();
    }

    public void setProbability(final double probability) {
      this.probability.set(probability);
    }

    public DoubleProperty rateProperty() {
      return rate;
    }

    public double rate() {
      return rate.get();
    }

    public void setRate(final double rate) {
      this.rate.set(rate);
    }
  }

  private final ObservableList<ProbabilityRatePair> probabilityRatePairs =
      new SimpleListProperty<>(this, "probabilityRatePairs", FXCollections.observableArrayList());

  public HyperexponentialTimeDistributionModel(
      final List<ProbabilityRatePair> probabilityRatePairs) {
    super(
        "Hyperexponential",
        new HyperexponentialDistribution(
            probabilityRatePairs.stream().mapToDouble(ProbabilityRatePair::probability).toArray(),
            probabilityRatePairs.stream().mapToDouble(ProbabilityRatePair::rate).toArray()));

    this.probabilityRatePairs.addListener(
        (ListChangeListener<ProbabilityRatePair>)
            change -> {
              final var probabilities =
                  this.probabilityRatePairs.stream()
                      .mapToDouble(ProbabilityRatePair::probability)
                      .toArray();
              final var rates =
                  this.probabilityRatePairs.stream()
                      .mapToDouble(ProbabilityRatePair::rate)
                      .toArray();

              while (change.next()) {
                if (change.wasAdded()) {
                  change
                      .getAddedSubList()
                      .forEach(
                          pair -> {
                            pair.probabilityProperty()
                                .addListener(
                                    (value, oldValue, newValue) -> {
                                      final var newProbabilities =
                                          this.probabilityRatePairs.stream()
                                              .mapToDouble(ProbabilityRatePair::probability)
                                              .toArray();
                                      final var newRates =
                                          this.probabilityRatePairs.stream()
                                              .mapToDouble(ProbabilityRatePair::rate)
                                              .toArray();

                                      try {
                                        setRealDistribution(
                                            new HyperexponentialDistribution(
                                                getRandomGenerator(), newProbabilities, newRates));
                                      } catch (IllegalArgumentException e) {
                                        LOGGER.warn(
                                            "Invalid hyperexponential distribution parameters: {}",
                                            e.getMessage());
                                      }
                                    });
                            pair.rateProperty()
                                .addListener(
                                    (value, oldValue, newValue) -> {
                                      final var newProbabilities =
                                          this.probabilityRatePairs.stream()
                                              .mapToDouble(ProbabilityRatePair::probability)
                                              .toArray();
                                      final var newRates =
                                          this.probabilityRatePairs.stream()
                                              .mapToDouble(ProbabilityRatePair::rate)
                                              .toArray();

                                      try {
                                        setRealDistribution(
                                            new HyperexponentialDistribution(
                                                getRandomGenerator(), newProbabilities, newRates));
                                      } catch (IllegalArgumentException e) {
                                        LOGGER.warn(
                                            "Invalid hyperexponential distribution parameters: {}",
                                            e.getMessage());
                                      }
                                    });
                          });
                }
              }

              try {
                setRealDistribution(
                    new HyperexponentialDistribution(getRandomGenerator(), probabilities, rates));
              } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid hyperexponential distribution parameters: {}", e.getMessage());
              }
            });

    this.probabilityRatePairs.addAll(probabilityRatePairs);

    randomGeneratorObjectProperty.addListener(
        (randomGenerator, oldRandomGenerator, newRandomGenerator) -> {
          try {
            setRealDistribution(
                new HyperexponentialDistribution(
                    newRandomGenerator,
                    probabilityRatePairs.stream()
                        .mapToDouble(ProbabilityRatePair::probability)
                        .toArray(),
                    probabilityRatePairs.stream()
                        .mapToDouble(ProbabilityRatePair::rate)
                        .toArray()));
          } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid hyperexponential distribution parameters: {}", e.getMessage());
          }
        });

    descriptionPropertyWrapper.bind(
        Bindings.createStringBinding(
            () -> {
              final var stringBuilder = new StringBuilder();

              stringBuilder.append("Hyperexp(");
              for (var i = 0; i < probabilityRatePairs.size(); i++) {
                final var probabilityRatePair = probabilityRatePairs.get(i);
                stringBuilder.append(
                    "%s, %s"
                        .formatted(
                            NumericFormatUtil.formatNumber(probabilityRatePair.probability()),
                            NumericFormatUtil.formatNumber(probabilityRatePair.rate())));

                if (i < probabilityRatePairs.size() - 1) {
                  stringBuilder.append("; ");
                }
              }
              stringBuilder.append(")");

              return stringBuilder.toString();
            },
            this.probabilityRatePairs));
  }

  public HyperexponentialTimeDistributionModel(
      final HyperexponentialTimeDistributionModel hyperexponentialTimeDistribution) {
    this(hyperexponentialTimeDistribution.probabilityRatePairs);
  }

  public HyperexponentialTimeDistributionModel() {
    this(List.of(new ProbabilityRatePair()));
  }

  public ObservableList<ProbabilityRatePair> probabilityRatePairsProperty() {
    return probabilityRatePairs;
  }

  public List<ProbabilityRatePair> getProbabilityRatePairs() {
    return probabilityRatePairs;
  }

  @Override
  public TimeDistributionModel interpolate(
      final TimeDistributionModel other, final double interpolationFactor) {
    if (other instanceof HyperexponentialTimeDistributionModel hyperexponentialTimeDistribution) {
      final var interpolatedProbabilityRatePairs =
          interpolateProbabilityRatePairs(
              probabilityRatePairs,
              hyperexponentialTimeDistribution.probabilityRatePairs,
              interpolationFactor);

      return new HyperexponentialTimeDistributionModel(interpolatedProbabilityRatePairs);
    } else {
      throw new IllegalArgumentException("Cannot interpolate with a different distribution type");
    }
  }

  private static List<ProbabilityRatePair> interpolateProbabilityRatePairs(
      final List<ProbabilityRatePair> probabilityRatePairs1,
      final List<ProbabilityRatePair> probabilityRatePairs2,
      final double interpolationFactor) {
    if (probabilityRatePairs1.size() != probabilityRatePairs2.size()) {
      throw new IllegalArgumentException("Probability rate pairs must have the same size");
    }

    final List<ProbabilityRatePair> interpolatedProbabilityRatePairs = new ArrayList<>();

    for (var i = 0; i < probabilityRatePairs1.size(); i++) {
      final var probabilityRatePair1 = probabilityRatePairs1.get(i);
      final var probabilityRatePair2 = probabilityRatePairs2.get(i);

      final var interpolatedProbability =
          probabilityRatePair1.probability()
              + interpolationFactor
                  * (probabilityRatePair2.probability() - probabilityRatePair1.probability());
      final var interpolatedRate =
          probabilityRatePair1.rate()
              + interpolationFactor * (probabilityRatePair2.rate() - probabilityRatePair1.rate());

      interpolatedProbabilityRatePairs.add(
          new ProbabilityRatePair(interpolatedProbability, interpolatedRate));
    }

    return interpolatedProbabilityRatePairs;
  }
}
