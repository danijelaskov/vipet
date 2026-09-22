package dev.askov.vipet.configuration;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import dev.askov.vipet.localization.LocalizationManager;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class VIPETCommandLineArgumentsReader extends VIPETConfigurationProvider {

  private static class Parameters {

    public static class LanguageValidator implements IParameterValidator {

      @Override
      public void validate(String name, String value) throws ParameterException {
        if (!LocalizationManager.isLocaleSupported(Locale.forLanguageTag(value))) {
          throw new ParameterException(
              "[Error] Language must be one of: "
                  + Arrays.toString(LocalizationManager.SUPPORTED_LOCALES)
                  + ". Got: "
                  + value);
        }
      }
    }

    public static class StyleValidator implements IParameterValidator {

      @Override
      public void validate(String name, String value) throws ParameterException {
        value = value.toUpperCase();
        if (!THEME_NAMES.contains(value)) {
          throw new ParameterException(
              "[Error] Style must be one of: " + THEME_NAMES + ". Got: " + value);
        }
      }
    }

    @Parameter(
        names = {"--style", "-s"},
        description = "Application style",
        validateWith = StyleValidator.class)
    @SuppressWarnings("FieldMayBeFinal")
    private String style = DEFAULT_THEME_NAME;

    @Parameter(
        names = {"--maximized", "-m"},
        description = "Start application maximized")
    @SuppressWarnings("FieldMayBeFinal")
    private boolean maximized = DEFAULT_IS_WINDOW_MAXIMIZED;

    @Parameter(
        names = {"--width", "-w"},
        description = "Application width")
    @SuppressWarnings("FieldMayBeFinal")
    private double width = DEFAULT_WINDOW_WIDTH;

    @Parameter(
        names = {"--height", "-h"},
        description = "Application height")
    @SuppressWarnings("FieldMayBeFinal")
    private double height = DEFAULT_WINDOW_HEIGHT;

    @Parameter(
        names = {"--lang", "-l"},
        description = "Application language",
        validateWith = LanguageValidator.class)
    @SuppressWarnings("FieldMayBeFinal")
    private String lang = DEFAULT_LOCALE.getLanguage().toLowerCase();

    @Parameter(
        names = {"--networks", "-n"},
        description = "Network file(s) to load on startup")
    @SuppressWarnings("FieldMayBeFinal")
    private List<String> networks = List.of();
  }

  private static final Parameters PARAMETERS = new Parameters();

  public VIPETCommandLineArgumentsReader(final String[] args) {
    JCommander.newBuilder().addObject(PARAMETERS).build().parse(args);
  }

  @Override
  public String getThemeName() {
    return PARAMETERS.style;
  }

  @Override
  public double getWindowWidth() {
    return PARAMETERS.width;
  }

  @Override
  public double getWindowHeight() {
    return PARAMETERS.height;
  }

  @Override
  public boolean isWindowMaximized() {
    return PARAMETERS.maximized;
  }

  @Override
  public List<String> getNetworksToLoad() {
    return PARAMETERS.networks;
  }

  @Override
  public Locale getLocale() {
    final var languageParts = PARAMETERS.lang.split("_");

    if (languageParts.length == 2) {
      final var language = languageParts[0];
      final var country = languageParts[1];

      return Locale.of(language, country);
    } else if ((languageParts.length == 1)) {
      final var language = languageParts[0];

      return Locale.of(language);
    } else {
      return Locale.ENGLISH;
    }
  }
}
