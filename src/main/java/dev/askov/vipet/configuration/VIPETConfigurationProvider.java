package dev.askov.vipet.configuration;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.image.Image;
import org.apache.commons.math3.random.RandomGenerator;

public abstract class VIPETConfigurationProvider {

  protected static final List<String> THEME_NAMES =
      List.of(Application.STYLESHEET_MODENA, Application.STYLESHEET_CASPIAN);
  protected static final String DEFAULT_THEME_NAME = THEME_NAMES.getFirst();
  protected static final Image DEFAULT_WINDOW_ICON =
      new Image(Objects.requireNonNull(VIPET.class.getResourceAsStream("img/app-icon.png")));
  protected static final boolean DEFAULT_IS_WINDOW_MAXIMIZED = false;
  protected static final double DEFAULT_WINDOW_WIDTH = 800.0;
  protected static final double DEFAULT_WINDOW_HEIGHT = 600.0;
  protected static final List<Locale> SUPPORTED_LOCALE_NAMES =
      List.of(Locale.ENGLISH, Locale.of("sr", "RS"));
  protected static final Locale DEFAULT_LOCALE =
      SUPPORTED_LOCALE_NAMES.stream()
          .filter(locale -> locale.equals(Locale.getDefault()))
          .findFirst()
          .orElse(SUPPORTED_LOCALE_NAMES.getFirst());
  protected static final Class<? extends RandomGenerator> DEFAULT_RANDOM_GENERATOR_TYPE =
      DiscreteEventDrivenSimulator.getDefaultRandomGeneratorType();
  protected static final long DEFAULT_SEED = DiscreteEventDrivenSimulator.getDefaultSeed();

  String getThemeName() {
    return THEME_NAMES.getFirst();
  }

  Image getWindowIcon() {
    return DEFAULT_WINDOW_ICON;
  }

  boolean isWindowMaximized() {
    return DEFAULT_IS_WINDOW_MAXIMIZED;
  }

  double getWindowWidth() {
    return DEFAULT_WINDOW_WIDTH;
  }

  double getWindowHeight() {
    return DEFAULT_WINDOW_HEIGHT;
  }

  List<String> getNetworksToLoad() {
    return List.of();
  }

  Locale getLocale() {
    return DEFAULT_LOCALE;
  }

  Class<? extends RandomGenerator> getRandomGeneratorType() {
    return DEFAULT_RANDOM_GENERATOR_TYPE;
  }

  long getSeed() {
    return DEFAULT_SEED;
  }
}
