package dev.askov.vipet.common;

@SuppressWarnings("DuplicatedCode")
public final class TimeFormatUtil {

  private TimeFormatUtil() {}

  public static String formatNanoseconds(final long nanoSeconds) {
    return shortTimeString(nanoSeconds * 1e-9);
  }

  public static String shortTimeString(final double value) {
    final var hours = (int) (value / TimeUnit.HOUR.convertToSeconds());
    final var minutes =
        (int) (value / TimeUnit.MINUTE.convertToSeconds())
            % (int) TimeUnit.HOUR.convertTo(TimeUnit.MINUTE);
    final var seconds = (int) (value % TimeUnit.MINUTE.convertToSeconds());
    final var milliseconds =
        (int) ((value - (int) value) * TimeUnit.SECOND.convertTo(TimeUnit.MILLISECOND));

    return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds);
  }

  public static String longTimeString(final double value) {
    final var weeks = (int) (value / TimeUnit.WEEK.convertToSeconds());
    final var days =
        (int) (value / TimeUnit.DAY.convertToSeconds())
            % (int) TimeUnit.WEEK.convertTo(TimeUnit.DAY);
    final var hours =
        (int) (value / TimeUnit.HOUR.convertToSeconds())
            % (int) TimeUnit.DAY.convertTo(TimeUnit.HOUR);
    final var minutes =
        (int) (value / TimeUnit.MINUTE.convertToSeconds())
            % (int) TimeUnit.HOUR.convertTo(TimeUnit.MINUTE);
    final var seconds = (int) (value % TimeUnit.MINUTE.convertToSeconds());
    final var milliseconds =
        (int) ((value - (int) value) * TimeUnit.SECOND.convertTo(TimeUnit.MILLISECOND));
    final var microseconds =
        (int)
            (((value - (int) value) * TimeUnit.SECOND.convertTo(TimeUnit.MICROSECOND))
                % TimeUnit.MILLISECOND.convertTo(TimeUnit.MICROSECOND));

    if (weeks > 0) {
      return String.format(
          "%02d %s %02d %s %02d %s %02d %s %02d %s %03d %s %03d %s",
          weeks,
          TimeUnit.WEEK.getLabel(),
          days,
          TimeUnit.DAY.getLabel(),
          hours,
          TimeUnit.HOUR.getLabel(),
          minutes,
          TimeUnit.MINUTE.getLabel(),
          seconds,
          TimeUnit.SECOND.getLabel(),
          milliseconds,
          TimeUnit.MILLISECOND.getLabel(),
          microseconds,
          TimeUnit.MICROSECOND.getLabel());
    } else if (days > 0) {
      return String.format(
          "%02d %s %02d %s %02d %s %02d %s %03d %s %03d %s",
          days,
          TimeUnit.DAY.getLabel(),
          hours,
          TimeUnit.HOUR.getLabel(),
          minutes,
          TimeUnit.MINUTE.getLabel(),
          seconds,
          TimeUnit.SECOND.getLabel(),
          milliseconds,
          TimeUnit.MILLISECOND.getLabel(),
          microseconds,
          TimeUnit.MICROSECOND.getLabel());
    } else if (hours > 0) {
      return String.format(
          "%02d %s %02d %s %02d %s %03d %s %03d %s",
          hours,
          TimeUnit.HOUR.getLabel(),
          minutes,
          TimeUnit.MINUTE.getLabel(),
          seconds,
          TimeUnit.SECOND.getLabel(),
          milliseconds,
          TimeUnit.MILLISECOND.getLabel(),
          microseconds,
          TimeUnit.MICROSECOND.getLabel());
    } else if (minutes > 0) {
      return String.format(
          "%02d %s %02d %s %03d %s %03d %s",
          minutes,
          TimeUnit.MINUTE.getLabel(),
          seconds,
          TimeUnit.SECOND.getLabel(),
          milliseconds,
          TimeUnit.MILLISECOND.getLabel(),
          microseconds,
          TimeUnit.MICROSECOND.getLabel());
    } else if (seconds > 0) {
      return String.format(
          "%02d %s %03d %s %03d %s",
          seconds,
          TimeUnit.SECOND.getLabel(),
          milliseconds,
          TimeUnit.MILLISECOND.getLabel(),
          microseconds,
          TimeUnit.MICROSECOND.getLabel());
    } else if (milliseconds > 0) {
      return String.format(
          "%03d %s %03d %s",
          milliseconds,
          TimeUnit.MILLISECOND.getLabel(),
          microseconds,
          TimeUnit.MICROSECOND.getLabel());
    } else if (microseconds > 0) {
      return String.format("%03d %s", microseconds, TimeUnit.MICROSECOND.getLabel());
    } else {
      return "0";
    }
  }
}
