package dev.askov.vipet.core.simulator.sampling;

public record Measurement(double time, double value, double weight) {

  @Override
  public String toString() {
    return "Measurement[time=%.10f, value=%.10f, weight=%.10f]".formatted(time, value, weight);
  }
}
