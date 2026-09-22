package dev.askov.vipet.core.performancemeasures;

import static dev.askov.vipet.common.MathUtil.EPSILON;

import dev.askov.vipet.common.NumericFormatUtil;

public record PerformanceMeasure(double center, double halfWidth) {

  public PerformanceMeasure(final double center) {
    this(center, 0.0);
  }

  public boolean isUncertain() {
    return halfWidth > EPSILON;
  }

  public boolean contains(final double value) {
    final double low = center - halfWidth, high = center + halfWidth;
    final var ulpTolerance = Math.max(Math.ulp(low), Math.ulp(high)) * 8;
    final var tolerance = Math.max(EPSILON, ulpTolerance);

    return value >= low - tolerance && value <= high + tolerance;
  }

  @Override
  public String toString() {
    return NumericFormatUtil.formatPerformanceMeasure(this);
  }
}
