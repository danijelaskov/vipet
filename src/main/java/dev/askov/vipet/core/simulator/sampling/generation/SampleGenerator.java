package dev.askov.vipet.core.simulator.sampling.generation;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.core.simulator.sampling.Measurement;
import dev.askov.vipet.mvc.models.network.NamedModel;
import java.util.ArrayList;
import java.util.List;

public abstract class SampleGenerator<SomeNamedModel extends NamedModel> {

  private final int maxSampleSize;
  private final boolean inverted;

  protected final SomeNamedModel namedModel;

  private final List<Measurement> sample = new ArrayList<>();
  protected double currentTime;

  public SampleGenerator(
      final int maxSampleSize, final SomeNamedModel namedModel, final boolean inverted) {
    this.maxSampleSize = maxSampleSize;
    this.inverted = inverted;

    this.namedModel = namedModel;
  }

  public SampleGenerator(final int maxSampleSize, final SomeNamedModel namedModel) {
    this(maxSampleSize, namedModel, false);
  }

  public SomeNamedModel getNamedModel() {
    return namedModel;
  }

  public List<Measurement> getSample() {
    return sample;
  }

  public boolean getInverted() {
    return inverted;
  }

  public void takeMeasurement(final double time) {
    currentTime = time;

    if (sample.size() < maxSampleSize && shouldTakeMeasurement()) {
      sample.add(new Measurement(currentTime, getValue(), getWeight()));
    }
  }

  public int getSampleSize() {
    return sample.size();
  }

  public int getMaxSampleSize() {
    return maxSampleSize;
  }

  public boolean isFull() {
    return sample.size() == maxSampleSize;
  }

  public abstract PerformanceMeasureType getPerformanceMeasureType();

  protected abstract boolean shouldTakeMeasurement();

  protected abstract double getValue();

  protected abstract double getWeight();

  @Override
  public String toString() {
    final var sb =
        new StringBuilder(
            "SampleGenerator{performanceIndicator=%s, samples=[\n"
                .formatted(getPerformanceMeasureType().toString()));

    if (sample.isEmpty()) {
      return "\t{No samples!}";
    }

    int i;
    for (i = 0; i < Math.min(30, sample.size()); i++) {
      sb.append("\t{%05d: %s}\n".formatted(i, sample.get(i).toString()));
    }
    if (i < sample.size()) {
      sb.append("\t...\n");
      sb.append("\t{%05d: %s}".formatted(sample.size() - 1, sample.getLast().toString()));
    }

    sb.append("\n]}");

    return sb.toString();
  }
}
