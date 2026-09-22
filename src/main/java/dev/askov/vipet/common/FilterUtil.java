package dev.askov.vipet.common;

import java.text.ParseException;
import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

public final class FilterUtil {

  public static final UnaryOperator<TextFormatter.Change> NON_NEGATIVE_VALUES =
      change -> {
        try {
          var newValue = NumericFormatUtil.parseDouble(change.getControlNewText());
          if (newValue > 0.0) {
            return change;
          }
        } catch (ParseException e) {
          return null;
        }
        return null;
      };

  private FilterUtil() {}
}
