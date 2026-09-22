package dev.askov.vipet.common;

import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.NetworkNodeModel;

public final class ValidatorUtil {

  private ValidatorUtil() {}

  public static Double validateProbability(final String text) {
    if (text == null || text.isEmpty()) {
      return null;
    }

    var processedText =
        text.replace(NumericFormatUtil.getDecimalSeparator(), '.').replaceAll("\\b\\s*", "");

    final var isInPercentageForm = processedText.endsWith("%");

    if (isInPercentageForm) {
      processedText = processedText.substring(0, processedText.length() - 1);
    }

    final var fractionParts = processedText.split("/");

    try {
      if (fractionParts.length == 2) {
        final var numerator = Double.parseDouble(fractionParts[0]);
        final var denominator = Double.parseDouble(fractionParts[1]);
        final var result = (numerator / denominator) / (isInPercentageForm ? 100 : 1);

        return result >= 0 && result <= 1 ? result : null;
      } else {
        final var result = Double.parseDouble(processedText) / (isInPercentageForm ? 100 : 1);

        return result >= 0 && result <= 1 ? result : null;
      }
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static boolean validateNameFormat(final String text) {
    // Non-empty string with no leading or trailing whitespace, and no more than one space character
    // between words. Arbitrary length. Unicode is allowed. From the set of special characters, only
    // '.' is allowed.
    return text != null
        && !text.isEmpty()
        && text.matches(
            "^(?=\\S)(?!.*\\p{Z}{2,})[\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}.]*[\\p{L}\\p{M}\\p{N}.]$");
  }

  public static boolean validateNameUniqueness(
      final String name, final NetworkModel networkModel, final NetworkNodeModel excludeNode) {
    if (name == null || name.isEmpty() || networkModel == null) {
      return false;
    }

    return networkModel.getNodeModels().stream()
        .filter(node -> !node.equals(excludeNode))
        .noneMatch(node -> name.equals(node.getName()));
  }
}
