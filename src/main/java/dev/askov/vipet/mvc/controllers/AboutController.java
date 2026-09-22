package dev.askov.vipet.mvc.controllers;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AboutController extends TrivialAbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AboutController.class);
  private static final String LICENSE = "/LICENSE";
  private static final String LICENSES_DIRECTORY = "/licenses/";
  private static final String LICENSES_FILE_NAME_PREFIX = "LICENSE ";
  private static final String[] LIBRARY_NAMES = {
    "Apache Commons Math",
    "Gradle",
    "Gson",
    "Ikonli",
    "JCommander",
    "JLaTeXMath",
    "Log4j",
    "SLF4J",
    "Spotless"
  };

  @FXML private Label headerLabel;
  @FXML private Label descriptionLabel;
  @FXML private TextArea licenceTextArea;
  @FXML public TabPane licencesTabPane;

  @Override
  protected void initialize() {
    headerLabel.setText(
        headerLabel
            .getText()
            .formatted(System.getProperty("app.name"), System.getProperty("app.version")));

    VBox.setVgrow(descriptionLabel, Priority.ALWAYS);

    try (final var inputStream = getClass().getResourceAsStream(LICENSE)) {
      if (inputStream != null) {
        licenceTextArea.setText(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
      } else {
        LOGGER.warn("Could not find resource: {}", LICENSE);
      }
    } catch (IOException e) {
      LOGGER.error("Could not read single license resource", e);
    }

    for (final var libraryName : LIBRARY_NAMES) {
      final var licencePath = LICENSES_DIRECTORY + LICENSES_FILE_NAME_PREFIX + libraryName;

      try (final var inputStream = getClass().getResourceAsStream(licencePath)) {
        if (inputStream != null) {
          final var textArea =
              new TextArea(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
          textArea.setEditable(false);

          final var anchorPane = new AnchorPane(textArea);
          AnchorPane.setTopAnchor(textArea, 5.0);
          AnchorPane.setBottomAnchor(textArea, 5.0);
          AnchorPane.setLeftAnchor(textArea, 5.0);
          AnchorPane.setRightAnchor(textArea, 5.0);

          licencesTabPane.getTabs().add(new Tab(libraryName, anchorPane));
        } else {
          LOGGER.warn("Could not find resource: {}", licencePath);
        }
      } catch (IOException e) {
        LOGGER.warn("Could not read license resource: {}", licencePath, e);
      }
    }
  }

  @FXML
  private void onHyperlinkAction() {
    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(new URI(resources.getString("about.repositoryPath")));
      } catch (IOException | URISyntaxException e) {
        LOGGER.warn("Could not open license URL", e);
      }
    } else {
      LOGGER.warn("Desktop is not supported");
    }
  }
}
