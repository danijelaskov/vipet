package dev.askov.vipet.core.simulator;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasuresProvider;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public abstract class Simulator extends PerformanceMeasuresProvider {

  public static final double DEFAULT_MAX_DURATION = 5 * 60.0; // 5 minutes
  public static final double DEFAULT_MAX_SIMULATED_TIME = 24 * 60 * 60.0; // 24 hours
  public static final int DEFAULT_MAX_SAMPLE_SIZE = 10_000;

  private final DoubleProperty maxDurationProperty =
      new SimpleDoubleProperty(this, "maxDuration", DEFAULT_MAX_DURATION);
  private final DoubleProperty maxSimulatedTimeProperty =
      new SimpleDoubleProperty(this, "maxSimulatedTime", DEFAULT_MAX_SIMULATED_TIME);
  private final IntegerProperty maxSampleSizeProperty =
      new SimpleIntegerProperty(this, "maxSampleSize", DEFAULT_MAX_SAMPLE_SIZE);

  public Simulator(final NetworkModel networkModel) {
    super(networkModel);
  }

  public double getMaxDuration() {
    return maxDurationProperty.get();
  }

  public void setMaxDuration(final double maxDuration) {
    maxDurationProperty.set(maxDuration);
  }

  public double getMaxSimulatedTime() {
    return maxSimulatedTimeProperty.get();
  }

  public void setMaxSimulatedTime(final double maxSimulatedTime) {
    maxSimulatedTimeProperty.set(maxSimulatedTime);
  }

  public int getMaxSampleSize() {
    return maxSampleSizeProperty.get();
  }

  public void setMaxSampleSize(final int maxSampleSize) {
    maxSampleSizeProperty.set(maxSampleSize);
  }
}
