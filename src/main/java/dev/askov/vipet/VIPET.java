package dev.askov.vipet;

import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.configuration.VIPETCommandLineArgumentsReader;
import dev.askov.vipet.configuration.VIPETPropertiesFileReader;
import dev.askov.vipet.configuration.VPETConfigurator;
import java.io.IOException;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VIPET extends Application {

  private static final Logger LOGGER = LoggerFactory.getLogger(VIPET.class);

  private static String[] args;
  private static Stage primaryStage;

  public static void main(String[] args) {
    VIPET.args = args;
    launch();
  }

  @Override
  public void start(Stage primaryStage) {
    VIPET.primaryStage = primaryStage;

    final var configurator =
        new VPETConfigurator(
            new VIPETCommandLineArgumentsReader(args),
            new VIPETPropertiesFileReader("application.properties"));

    configurator.configure(
        () -> {
          try {
            UIUtil.loadMainWindow("Main", primaryStage, System.getProperty("app.name"));
          } catch (IOException e) {
            LOGGER.error("Failed to load main application window", e);
          }
        });

    primaryStage.show();
  }

  public static Stage getPrimaryStage() {
    return primaryStage;
  }
}
