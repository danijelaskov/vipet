package dev.askov.vipet.core.simulator.sampling.analysis;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.core.simulator.sampling.generation.SampleGenerator;
import java.util.ArrayList;
import java.util.List;

public abstract class SampleAnalyzer<Entity> {

  private final Entity entity;
  public final PerformanceMeasureType performanceMeasureType;
  private final List<SampleGenerator<?>> sampleGenerators = new ArrayList<>();
  private final double sampleDiscardRatio;
  private final double confidenceLevel;

  public SampleAnalyzer(
      final List<SampleGenerator<?>> sampleGenerators,
      final double sampleDiscardRatio,
      final double confidenceLevel,
      final Entity entity,
      final PerformanceMeasureType performanceMeasureType) {
    if (sampleGenerators.isEmpty()) {
      throw new IllegalArgumentException("At least one sample generator must be provided");
    }

    this.entity = entity;
    this.performanceMeasureType = performanceMeasureType;
    this.sampleGenerators.addAll(sampleGenerators);
    this.sampleDiscardRatio = sampleDiscardRatio;
    this.confidenceLevel = confidenceLevel;
  }

  public Entity getEntity() {
    return entity;
  }

  public PerformanceMeasureType getPerformanceIndicatorType() {
    return performanceMeasureType;
  }

  public List<SampleGenerator<?>> getSampleGenerators() {
    return sampleGenerators;
  }

  public abstract PerformanceMeasure calculatePerformanceMeasure();

  protected double getSampleDiscardRatio() {
    return sampleDiscardRatio;
  }

  protected double getConfidenceLevel() {
    return confidenceLevel;
  }
}
