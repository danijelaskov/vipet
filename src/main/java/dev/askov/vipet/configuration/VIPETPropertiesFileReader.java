package dev.askov.vipet.configuration;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import javafx.scene.image.Image;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VIPETPropertiesFileReader extends VIPETConfigurationProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(VIPETPropertiesFileReader.class);

  private final Properties properties = new Properties();

  public VIPETPropertiesFileReader(final String propertiesFilePath) {
    try (final var inputStream =
        getClass().getClassLoader().getResourceAsStream(propertiesFilePath)) {
      properties.load(inputStream);
      NumericFormatUtil.configureFromProperties(properties);
    } catch (Exception e) {
      LOGGER.error("Failed to load application.properties", e);
    }
  }

  @Override
  public String getThemeName() {
    return getProperty("window.theme");
  }

  @Override
  public Image getWindowIcon() {
    final var windowIconPath = getProperty("window.icon");

    return windowIconPath == null
        ? DEFAULT_WINDOW_ICON
        : new Image(Objects.requireNonNull(VIPET.class.getResourceAsStream(windowIconPath)));
  }

  @Override
  public boolean isWindowMaximized() {
    final var isWindowMaximized = getProperty("window.maximized");

    return isWindowMaximized == null
        ? super.isWindowMaximized()
        : Boolean.parseBoolean(isWindowMaximized);
  }

  @Override
  public double getWindowWidth() {
    final var windowWidth = getProperty("window.size.width");

    return windowWidth == null ? super.getWindowWidth() : Double.parseDouble(windowWidth);
  }

  @Override
  public double getWindowHeight() {
    final var windowHeight = getProperty("window.size.height");

    return windowHeight == null ? super.getWindowHeight() : Double.parseDouble(windowHeight);
  }

  @Override
  public List<String> getNetworksToLoad() {
    final var networksToLoad = getProperty("networks");

    return networksToLoad == null ? super.getNetworksToLoad() : List.of(networksToLoad.split(","));
  }

  @Override
  public Locale getLocale() {
    final var language = getProperty("language");

    return language == null ? super.getLocale() : Locale.forLanguageTag(language);
  }

  @Override
  public Class<? extends RandomGenerator> getRandomGeneratorType() {
    final var randomGeneratorType = getProperty("rng.type");

    return DiscreteEventDrivenSimulator.AVAILABLE_RANDOM_GENERATORS.stream()
        .filter(
            currentRandomGeneratorType ->
                currentRandomGeneratorType.getSimpleName().equals(randomGeneratorType))
        .findFirst()
        .orElse(super.getRandomGeneratorType());
  }

  @Override
  public long getSeed() {
    final var seed = getProperty("rng.seed");

    return seed == null ? super.getSeed() : Long.parseLong(seed);
  }

  private String getProperty(final String key) {
    return properties.getProperty(key);
  }
}
