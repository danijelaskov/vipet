package dev.askov.vipet.common;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.localization.LocalizationManager;
import dev.askov.vipet.mvc.ControllerInjector;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.models.network.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UIUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(UIUtil.class);
  private static final LocalizationManager LOCALIZATION_MANAGER = LocalizationManager.getInstance();

  private static final FontIcon ROTATE_ICON = new FontIcon("ci-rotate-180:24");
  private static final FontIcon PROPERTIES_ICON = new FontIcon("codicon-symbol-property:24");
  private static final FontIcon REMOVE_ICON = new FontIcon("codicon-trash:24");

  static {
    ROTATE_ICON.getStyleClass().add("menu-item-icon");
    PROPERTIES_ICON.getStyleClass().add("menu-item-icon");
    REMOVE_ICON.getStyleClass().add("menu-item-icon");
  }

  private UIUtil() {}

  public static void loadMainWindow(
      final String viewName, final Stage stage, final Object... windowTitleParameters)
      throws IOException {
    final var loader = ControllerInjector.getLoader(viewName);

    setWindowTitle(viewName, windowTitleParameters, loader, stage);
    stage.setScene(new Scene(loader.load()));

    final AbstractController<?> controller = loader.getController();
    if (controller != null) {
      stage.setOnCloseRequest(controller::onWindowClosed);
    } else {
      LOGGER.error("Controller for main window {} is null", viewName);
    }
  }

  public static void loadWindow(
      final String viewName,
      final Stage ownerStage,
      final boolean resizable,
      final boolean modal,
      final Object... windowTitleParameters)
      throws IOException {
    final var loader = ControllerInjector.getLoader(viewName);
    final var stage = new Stage();

    stage.setScene(new Scene(loader.load()));
    stage.initOwner(ownerStage);
    stage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);

    final AbstractController<?> controller = loader.getController();
    if (controller != null) {
      stage.setOnCloseRequest(controller::onWindowClosed);
    } else {
      LOGGER.error("Controller for window {} is null", viewName);
    }
    stage.setResizable(resizable);
    stage.getIcons().addAll(ownerStage.getIcons());

    setWindowTitle(viewName, windowTitleParameters, loader, stage);

    stage.showAndWait();
  }

  private static void setWindowTitle(
      String viewName, Object[] windowTitleParameters, FXMLLoader loader, Stage stage) {
    final var bundle = loader.getResources();

    if (bundle != null) {
      final var viewNameParts = viewName.split("/");
      final var fileName = viewNameParts[viewNameParts.length - 1];
      final var windowTitleKey =
          Character.toLowerCase(fileName.charAt(0)) + fileName.substring(1) + ".windowTitle";
      if (windowTitleParameters.length > 0) {
        stage.setTitle(String.format(bundle.getString(windowTitleKey), windowTitleParameters));
      } else {
        stage.setTitle(bundle.getString(windowTitleKey));
      }
    } else {
      LOGGER.warn("Cannot set title for window {}. No localization bundle found", viewName);
    }
  }

  public static void loadModalWindow(final String viewName, final Stage ownerStage)
      throws IOException {
    loadWindow(viewName, ownerStage, false, true);
  }

  public static void loadModalWindow(
      final String viewName, final Stage ownerStage, final Object... windowTitleParameters)
      throws IOException {
    loadWindow(viewName, ownerStage, false, true, windowTitleParameters);
  }

  public static Tab loadTab(final String viewName, final TabPane pane) throws IOException {
    final var loader = ControllerInjector.getLoader(viewName);
    final var tab = new Tab();

    tab.setContent(loader.load());
    pane.getTabs().add(tab);

    return tab;
  }

  public static Tab loadTabAtIndex(final String viewName, final TabPane pane, final int index)
      throws IOException {
    final var loader = ControllerInjector.getLoader(viewName);
    final var tab = new Tab();

    tab.setContent(loader.load());
    pane.getTabs().set(index, tab);

    return tab;
  }

  public static AnchorPane loadAnchorPane(final String viewName) throws IOException {
    final var loader = ControllerInjector.getLoader(viewName);
    return loader.load();
  }

  public static void installTooltip(final Node node, final String stringToShow) {
    final var tooltip = new Tooltip(stringToShow);

    tooltip.setShowDelay(Duration.ZERO);
    tooltip.setShowDuration(Duration.INDEFINITE);
    tooltip.setHideDelay(Duration.ZERO);

    Tooltip.install(node, tooltip);
    node.setOnMouseEntered(
        event -> tooltip.show(node, event.getScreenX() + 5.0, event.getScreenY() + 5.0));
    node.setOnMouseExited(event -> tooltip.hide());
  }

  public static void installTooltip(final Node node, final double valueToShow) {
    installTooltip(node, NumericFormatUtil.formatNumber(valueToShow));
  }

  public static Alert createAlert(
      final Alert.AlertType alertType,
      final Window owner,
      final String title,
      final String header,
      final String content) {
    final var alert = new Alert(alertType);

    alert.initOwner(owner);
    alert.initModality(Modality.APPLICATION_MODAL);

    ((Stage) alert.getDialogPane().getScene().getWindow())
        .getIcons()
        .add(VIPET.getPrimaryStage().getIcons().getFirst());
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

    if (title != null) {
      alert.titleProperty().bind(LOCALIZATION_MANAGER.getStringBinding(title));
    }
    if (header != null) {
      alert.headerTextProperty().bind(LOCALIZATION_MANAGER.getStringBinding(header));
    }
    if (content != null) {
      alert.contentTextProperty().bind(LOCALIZATION_MANAGER.getStringBinding(content));
    }

    switch (alert.getAlertType()) {
      case INFORMATION:
      case WARNING:
      case ERROR:
        {
          alert
              .getButtonTypes()
              .setAll(
                  new ButtonType(
                      LOCALIZATION_MANAGER.getString("alert.ok"), ButtonBar.ButtonData.OK_DONE));
          break;
        }
      case CONFIRMATION:
        {
          alert
              .getButtonTypes()
              .setAll(
                  new ButtonType(
                      LOCALIZATION_MANAGER.getString("alert.ok"), ButtonBar.ButtonData.OK_DONE),
                  new ButtonType(
                      LOCALIZATION_MANAGER.getString("alert.cancel"),
                      ButtonBar.ButtonData.CANCEL_CLOSE));
          break;
        }
      case NONE:
        {
          alert
              .getButtonTypes()
              .setAll(
                  new ButtonType(
                      LOCALIZATION_MANAGER.getString("alert.close"),
                      ButtonBar.ButtonData.CANCEL_CLOSE));
          break;
        }
    }

    return alert;
  }

  public static Alert createAlert(
      final Alert.AlertType alertType,
      final Window owner,
      final String title,
      final String header) {
    return createAlert(alertType, owner, title, header, null);
  }

  public static String toRGB(final Color color) {
    return String.format(
        "#%02X%02X%02X",
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
  }

  public static Color getForegroundColor(final Color backgroundColor) {
    final var luminance =
        0.2126 * backgroundColor.getRed()
            + 0.7152 * backgroundColor.getGreen()
            + 0.0722 * backgroundColor.getBlue();

    return luminance < 0.5
        ? backgroundColor.deriveColor(0.0, 1.0, 2, 1.0)
        : backgroundColor.deriveColor(0.0, 1.0, 0.5, 1.0);
  }

  public static void renderLaTeXFormula(
      final String formula, final ImageView imageView, final java.awt.Color foregroundColor) {
    final var image = LaTeXUtil.createImageFromLaTeX(formula, foregroundColor);

    if (image != null) {
      imageView.setImage(image);

      imageView.setFitWidth(image.getWidth());
      imageView.setFitHeight(image.getHeight());
    }
  }

  public static void renderLaTeXFormula(final String formula, final ImageView imageView) {
    renderLaTeXFormula(formula, imageView, null);
  }

  public static List<MenuItem> createNetworkNodePropertiesMenuItems(
      final NetworkElementModel networkElementModel) {
    final List<MenuItem> menuItems = new ArrayList<>();

    if (networkElementModel instanceof NetworkNodeModel networkNodeModel) {
      final var rotateMenuItem = new MenuItem();
      rotateMenuItem.setAccelerator(
          new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
      rotateMenuItem
          .textProperty()
          .bind(LOCALIZATION_MANAGER.getStringBinding("edit.networkNode.rotate"));
      ROTATE_ICON.scaleXProperty().unbind();
      ROTATE_ICON
          .scaleXProperty()
          .bind(
              networkNodeModel
                  .forwardOrientedProperty()
                  .map(forwardOriented -> forwardOriented ? -1 : 1));
      rotateMenuItem.setGraphic(ROTATE_ICON);

      rotateMenuItem.setOnAction(
          event -> networkNodeModel.setForwardOriented(!networkNodeModel.isForwardOriented()));

      menuItems.add(rotateMenuItem);

      final var propertiesMenuItem = new MenuItem();
      propertiesMenuItem.setAccelerator(
          new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN));
      propertiesMenuItem
          .textProperty()
          .bind(LOCALIZATION_MANAGER.getStringBinding("edit.networkNode.properties"));
      propertiesMenuItem.setGraphic(PROPERTIES_ICON);
      propertiesMenuItem.setId("propertiesMenuItem");

      menuItems.add(propertiesMenuItem);
    }

    if (networkElementModel instanceof ConnectionModel connectionModel) {
      final var typeMenu = new Menu();
      typeMenu.textProperty().bind(LOCALIZATION_MANAGER.getStringBinding("edit.connection.type"));

      final var typeToggleGroup = new ToggleGroup();

      final var shortestMenuItem = new RadioMenuItem();
      shortestMenuItem
          .textProperty()
          .bind(LOCALIZATION_MANAGER.getStringBinding("edit.connection.type.shortest"));
      shortestMenuItem.setToggleGroup(typeToggleGroup);
      shortestMenuItem.setOnAction(
          actionEvent -> connectionModel.setType(ConnectionModel.Type.SHORTEST));
      shortestMenuItem.setSelected(connectionModel.getType() == ConnectionModel.Type.SHORTEST);
      shortestMenuItem
          .disableProperty()
          .bind(
              Bindings.when(
                      connectionModel
                          .getSource()
                          .forwardOrientedProperty()
                          .isEqualTo(connectionModel.getDestination().forwardOrientedProperty()))
                  .then(
                      Bindings.when(connectionModel.getSource().forwardOrientedProperty())
                          .then(
                              connectionModel
                                  .getDestination()
                                  .getInputPortPosition()
                                  .xProperty()
                                  .subtract(
                                      connectionModel
                                          .getSource()
                                          .getOutputPortPosition()
                                          .xProperty())
                                  .lessThanOrEqualTo(0))
                          .otherwise(
                              connectionModel
                                  .getDestination()
                                  .getInputPortPosition()
                                  .xProperty()
                                  .subtract(
                                      connectionModel
                                          .getSource()
                                          .getOutputPortPosition()
                                          .xProperty())
                                  .greaterThanOrEqualTo(0)))
                  .otherwise(true));

      final var bezierMenuItem = new RadioMenuItem();
      bezierMenuItem
          .textProperty()
          .bind(LOCALIZATION_MANAGER.getStringBinding("edit.connection.type.bezier"));
      bezierMenuItem.setToggleGroup(typeToggleGroup);
      bezierMenuItem.setOnAction(
          actionEvent -> connectionModel.setType(ConnectionModel.Type.BEZIER));
      bezierMenuItem.setSelected(connectionModel.getType() == ConnectionModel.Type.BEZIER);
      bezierMenuItem.disableProperty().bind(shortestMenuItem.disableProperty());

      final var complexMenuItem = new RadioMenuItem();
      complexMenuItem
          .textProperty()
          .bind(LOCALIZATION_MANAGER.getStringBinding("edit.connection.type.complex"));
      complexMenuItem.setToggleGroup(typeToggleGroup);
      complexMenuItem.setOnAction(
          actionEvent -> connectionModel.setType(ConnectionModel.Type.MULTI_SEGMENT));
      complexMenuItem.setSelected(connectionModel.getType() == ConnectionModel.Type.MULTI_SEGMENT);

      typeMenu.getItems().addAll(shortestMenuItem, bezierMenuItem, complexMenuItem);
      menuItems.add(typeMenu);
    }

    final var removeMenuItem = new MenuItem();
    removeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
    removeMenuItem
        .textProperty()
        .bind(LOCALIZATION_MANAGER.getStringBinding("edit.networkElement.remove"));
    removeMenuItem.setGraphic(REMOVE_ICON);
    removeMenuItem.setId("removeMenuItem");

    menuItems.add(removeMenuItem);

    return menuItems;
  }
}
