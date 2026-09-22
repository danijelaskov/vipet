package dev.askov.vipet.configuration;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import dev.askov.vipet.localization.LocalizationManager;
import dev.askov.vipet.mvc.ControllerInjector;
import dev.askov.vipet.mvc.controllers.MainController;
import dev.askov.vipet.mvc.models.network.NetworkCollectionModel;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.NetworkImporter;
import dev.askov.vipet.serialization.json.JSONNetworkImporter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VPETConfigurator {

  private static final Logger LOGGER = LoggerFactory.getLogger(VPETConfigurator.class);
  private static final String CASPIAN = "css/caspian.css";
  private static final NetworkCollectionModel NETWORKS = new NetworkCollectionModel();

  private final VIPETConfigurationProvider[] configurationProviders;
  private final List<String> networksToLoad = new ArrayList<>();

  private String stylesheetName;
  private boolean isWindowMaximized = VIPETConfigurationProvider.DEFAULT_IS_WINDOW_MAXIMIZED;
  private double windowWidth = VIPETConfigurationProvider.DEFAULT_WINDOW_WIDTH;
  private double windowHeight = VIPETConfigurationProvider.DEFAULT_WINDOW_HEIGHT;

  private MainController mainController;

  public VPETConfigurator(VIPETConfigurationProvider... configurationProviders) {
    this.configurationProviders = configurationProviders;
  }

  public void configure(final Runnable mainWindowLoader) {
    configureBeforeLoad();
    mainWindowLoader.run();
    configureAfterLoad();
  }

  private void configureBeforeLoad() {
    var windowIcon = VIPETConfigurationProvider.DEFAULT_WINDOW_ICON;
    var locale = VIPETConfigurationProvider.DEFAULT_LOCALE;

    for (final var configurationProvider : configurationProviders) {
      windowIcon = configurationProvider.getWindowIcon();
      isWindowMaximized = configurationProvider.isWindowMaximized();
      windowWidth = configurationProvider.getWindowWidth();
      windowHeight = configurationProvider.getWindowHeight();
      stylesheetName =
          configurationProvider.getThemeName() != null ? configurationProvider.getThemeName() : "";
      locale = configurationProvider.getLocale();
      networksToLoad.addAll(configurationProvider.getNetworksToLoad());

      DiscreteEventDrivenSimulator.setDefaultRandomGeneratorType(
          configurationProvider.getRandomGeneratorType());
      DiscreteEventDrivenSimulator.setDefaultSeed(configurationProvider.getSeed());
    }

    VIPET.getPrimaryStage().getIcons().add(windowIcon);

    LocalizationManager.getInstance().setLocale(locale);

    mainController = new MainController(NETWORKS);
    ControllerInjector.registerControllerFactory(MainController.class, () -> mainController);

    if (!isWindowMaximized) {
      VIPET.getPrimaryStage().setWidth(windowWidth);
      VIPET.getPrimaryStage().setHeight(windowHeight);

      LOGGER.debug("Set window size to {}x{}", windowWidth, windowHeight);
    } else {
      VIPET.getPrimaryStage().setMaximized(true);
    }
  }

  private void configureAfterLoad() {
    if (VIPETConfigurationProvider.THEME_NAMES.contains(stylesheetName.toUpperCase())) {
      VIPET.setUserAgentStylesheet(stylesheetName.toUpperCase());
    } else {
      LOGGER.warn("Invalid stylesheet name: {}", stylesheetName);
    }

    if ("caspian".equals(stylesheetName)) {
      final var caspianStylesheetURL = VIPET.class.getResource(CASPIAN);

      if (caspianStylesheetURL != null) {
        VIPET
            .getPrimaryStage()
            .getScene()
            .getStylesheets()
            .add(caspianStylesheetURL.toExternalForm());
      } else {
        LOGGER.warn("Failed to load {}", CASPIAN);
      }
    }

    for (final var networkToLoad : networksToLoad) {
      final var networkInputStream = VIPET.class.getResourceAsStream(networkToLoad);

      if (networkInputStream != null) {
        final NetworkImporter networkImporter = new JSONNetworkImporter(networkInputStream);

        if (networkImporter.importNetwork()) {
          NETWORKS.add(networkImporter.getNetwork());

          LOGGER.debug("Successfully imported network from {}", networkToLoad);
        } else {
          LOGGER.error("Failed to import network from {}", networkToLoad);
        }
      } else {
        LOGGER.warn("Failed to load network from {}", networkToLoad);
      }
    }

    if (NETWORKS.isEmpty()) {
      NETWORKS.add(new NetworkModel(mainController.getNextDefaultNetworkName()));
    }
  }
}
