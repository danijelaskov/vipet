package dev.askov.vipet.common;

import java.util.function.Function;

public final class MathUtil {

  private static final int DEFAULT_DOUBLE_PRECISION = 12;

  public static final double MIN_DOUBLE_VALUE = Double.MIN_VALUE;
  public static final double EPSILON =
      Double.parseDouble(String.format("1E-%d", DEFAULT_DOUBLE_PRECISION));

  private MathUtil() {}

  public static double sum(final int from, final int to, final Function<Integer, Double> function) {
    double sum = 0;
    for (var i = from; i <= to; i++) {
      sum += function.apply(i);
    }
    return sum;
  }

  public static double fact(final int number) {
    double factorial = 1;
    for (var i = 1; i <= number; i++) {
      factorial *= i;
    }
    return factorial;
  }

  public static double inv(final double number) {
    return 1.0 / number;
  }
}
