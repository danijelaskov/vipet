package dev.askov.vipet.core.simulator.sampling.analysis;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.core.simulator.sampling.Measurement;
import dev.askov.vipet.core.simulator.sampling.generation.SampleGenerator;
import java.util.List;
import org.apache.commons.math3.distribution.TDistribution;

public final class StandardSampleAnalyzer<Entity> extends SampleAnalyzer<Entity> {

  private final TDistribution tDistribution;

  public StandardSampleAnalyzer(
      final List<SampleGenerator<?>> sampleGenerators,
      final double sampleDiscardRatio,
      final double confidenceLevel,
      final Entity entity,
      final PerformanceMeasureType performanceMeasureType) {
    super(sampleGenerators, sampleDiscardRatio, confidenceLevel, entity, performanceMeasureType);

    if (sampleGenerators.size() < 2) {
      throw new IllegalArgumentException(
          "StandardSampleAnalyzer requires at least two sample generators");
    }

    tDistribution = new TDistribution(sampleGenerators.size() - 1);
  }

  private double calculateMean(final SampleGenerator<?> sampleGenerator) {
    if (sampleGenerator.getSample().isEmpty()) {
      return 0.0;
    }

    var discardCount = (int) (sampleGenerator.getSample().size() * getSampleDiscardRatio());

    final var mean =
        sampleGenerator.getSample().stream()
                .skip(discardCount)
                .mapToDouble(measurement -> measurement.value() * measurement.weight())
                .sum()
            / sampleGenerator.getSample().stream()
                .skip(discardCount)
                .mapToDouble(Measurement::weight)
                .sum();

    return sampleGenerator.getInverted() ? (mean > 0.0 ? 1.0 / mean : 0.0) : mean;
  }

  public double calculateMean() {
    return getSampleGenerators().stream().mapToDouble(this::calculateMean).average().orElse(0.0);
  }

  private double calculateStandardDeviation() {
    final var mean = calculateMean();

    var sum = 0.0;
    for (final var sampleGenerator : getSampleGenerators()) {
      final var sampleGeneratorMean = calculateMean(sampleGenerator);

      sum += Math.pow(sampleGeneratorMean - mean, 2);
    }

    return Math.sqrt(sum / (getSampleGenerators().size() - 1));
  }

  @Override
  public PerformanceMeasure calculatePerformanceMeasure() {
    final var mean = calculateMean();

    final var standardDeviation = calculateStandardDeviation();
    final var alpha = 1.0 - getConfidenceLevel();
    final var cumulativeProbability = 1.0 - alpha / 2.0;
    final var tScore = tDistribution.inverseCumulativeProbability(cumulativeProbability);
    final var standardError = standardDeviation / Math.sqrt(getSampleGenerators().size());
    final var marginOfError = tScore * standardError;

    return new PerformanceMeasure(mean, marginOfError);
  }
}
