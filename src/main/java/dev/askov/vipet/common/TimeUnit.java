package dev.askov.vipet.common;

public enum TimeUnit {
  MICROSECOND(1e-6, "μs"),
  MILLISECOND(1e-3, "ms"),
  SECOND(1., "s"),
  MINUTE(60., "min"),
  HOUR(3_600., "h"),
  DAY(86_400., "d"),
  WEEK(604_800., "w");

  private final double seconds;
  private final String label;

  TimeUnit(final double seconds, final String label) {
    this.seconds = seconds;
    this.label = label;
  }

  public double convertTo(final TimeUnit timeUnit) {
    return seconds / timeUnit.seconds;
  }

  public double convertToSeconds() {
    return seconds;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
