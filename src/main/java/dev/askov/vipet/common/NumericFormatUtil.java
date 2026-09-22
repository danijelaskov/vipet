package dev.askov.vipet.common;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.localization.LocalizationManager;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Properties;

public final class NumericFormatUtil {

  private static final String DEFAULT_DECIMAL_DIGITS = "4";
  private static final String DEFAULT_GROUPING_USED = "true";
  private static final String DEFAULT_ROUNDING_MODE = "HALF_UP";

  private static DecimalFormatSymbols decimalFormatSymbols =
      new DecimalFormatSymbols(LocalizationManager.getInstance().getLocale());
  private static DecimalFormat doubleFormat;
  private static DecimalFormat scientificDoubleFormat;
  private static DecimalFormat integerFormat;
  private static int decimalDigits;

  private static String doubleFormatPattern;
  private static String scientificDoubleFormatPattern;
  private static String integerFormatPattern;

  static {
    LocalizationManager.getInstance()
        .localeProperty()
        .addListener(
            (locale, oldLocale, newLocale) -> {
              decimalFormatSymbols = new DecimalFormatSymbols(newLocale);
              doubleFormat = new DecimalFormat(doubleFormatPattern, decimalFormatSymbols);
              scientificDoubleFormat =
                  new DecimalFormat(scientificDoubleFormatPattern, decimalFormatSymbols);
              integerFormat = new DecimalFormat(integerFormatPattern, decimalFormatSymbols);
            });
  }

  static {
    configureFromProperties(new Properties());
  }

  private NumericFormatUtil() {}

  public static void configureFromProperties(final Properties properties) {
    configureDoubleFormatFromProperties(properties);
    configureIntegerFormatFromProperties(properties);
  }

  private static void configureDoubleFormatFromProperties(final Properties properties) {
    decimalDigits =
        Integer.parseInt(properties.getProperty("math.decimalDigits", DEFAULT_DECIMAL_DIGITS));
    final var groupingUsed =
        Boolean.parseBoolean(properties.getProperty("math.groupingUsed", DEFAULT_GROUPING_USED));
    final var roundingMode =
        RoundingMode.valueOf(properties.getProperty("math.roundingMode", DEFAULT_ROUNDING_MODE));

    doubleFormatPattern = groupingUsed ? "#,##0" : "0";
    if (decimalDigits > 0) {
      doubleFormatPattern += "." + "0".repeat(decimalDigits);
    }

    doubleFormat = new DecimalFormat(doubleFormatPattern, decimalFormatSymbols);
    doubleFormat.setRoundingMode(roundingMode);

    scientificDoubleFormatPattern = "0.";
    if (decimalDigits > 0) {
      scientificDoubleFormatPattern += "0".repeat(decimalDigits);
    }
    scientificDoubleFormatPattern += "E0";
    scientificDoubleFormat = new DecimalFormat(scientificDoubleFormatPattern, decimalFormatSymbols);
    scientificDoubleFormat.setRoundingMode(roundingMode);
  }

  private static void configureIntegerFormatFromProperties(final Properties properties) {
    final var groupingUsed =
        Boolean.parseBoolean(properties.getProperty("math.groupingUsed", DEFAULT_GROUPING_USED));

    integerFormatPattern = groupingUsed ? "#,##0" : "0";
    integerFormat = new DecimalFormat(integerFormatPattern, decimalFormatSymbols);
  }

  public static String formatNumber(final double number) {
    return formatNumber(number, false, true);
  }

  public static String formatNumber(final double number, final boolean laTeX) {
    return formatNumber(number, laTeX, true);
  }

  public static String formatNumber(
      final double number, final boolean laTeX, final boolean scientificNotation) {
    if (Math.abs(number) < MathUtil.MIN_DOUBLE_VALUE) {
      return "0";
    }
    if (hasDecimalDigits(number)) {
      if (laTeX) {
        if (scientificNotation && Math.abs(number) < 0.1) {
          final var scientificNotationFormat = "%." + decimalDigits + "e";
          final var scientificNotationForm = String.format(scientificNotationFormat, number);

          final var parts = scientificNotationForm.split("e");

          final var mantissa =
              parts[0].replace('.', doubleFormat.getDecimalFormatSymbols().getDecimalSeparator());
          final var exponent = Integer.parseInt(parts[1]);

          return String.format("{%s}\\cdot 10^{%d}", mantissa, exponent);
        } else {
          return doubleFormat.format(number);
        }
      } else {
        if (Math.abs(number) >= Math.pow(10, -0.5 * decimalDigits)
            && Math.abs(number) < Math.pow(10, decimalDigits)) {
          return doubleFormat.format(number);
        }
        return scientificDoubleFormat.format(number);
      }
    } else {
      return integerFormat.format(number);
    }
  }

  private static boolean hasDecimalDigits(double value) {
    return Math.abs(value - Math.round(value)) > MathUtil.MIN_DOUBLE_VALUE;
  }

  public static String formatPerformanceMeasure(final PerformanceMeasure performanceMeasure) {
    if (performanceMeasure.isUncertain()) {
      return String.format(
          "%s±%s",
          NumericFormatUtil.formatNumber(performanceMeasure.center()),
          NumericFormatUtil.formatNumber(performanceMeasure.halfWidth()));
    } else {
      return NumericFormatUtil.formatNumber(performanceMeasure.center());
    }
  }

  public static String formatNumber(final int number) {
    return integerFormat.format(number);
  }

  public static String formatNumber(final long number) {
    return integerFormat.format(number);
  }

  public static String formatPercentage(final double percentage) {
    return String.format("%s%%", NumericFormatUtil.formatNumber(percentage * 100.0));
  }

  public static String useLocalizedDecimalSeparator(final Number number) {
    return number.toString().replace('.', decimalFormatSymbols.getDecimalSeparator());
  }

  public static String useDefaultDecimalSeparator(final String number) {
    return number.replace(decimalFormatSymbols.getDecimalSeparator(), '.');
  }

  public static String useLocalizedGroupingSeparator(final Number number) {
    return number.toString().replace(',', decimalFormatSymbols.getGroupingSeparator());
  }

  public static String useDefaultGroupingSeparator(final String number) {
    return number.replace(decimalFormatSymbols.getGroupingSeparator(), ',');
  }

  public static char getDecimalSeparator() {
    return decimalFormatSymbols.getDecimalSeparator();
  }

  public static double parseDouble(String numberString) throws ParseException {
    final var isInPercentageForm = numberString.endsWith("%");

    if (isInPercentageForm) {
      numberString = numberString.substring(0, numberString.length() - 1);
    }

    var parsedValue = doubleFormat.parse(numberString).doubleValue();

    return isInPercentageForm ? parsedValue / 100.0 : parsedValue;
  }

  public static int parseInteger(String numberString) throws ParseException {
    return integerFormat.parse(numberString).intValue();
  }

  public static long parseLong(String numberString) throws ParseException {
    return integerFormat.parse(numberString).longValue();
  }
}
