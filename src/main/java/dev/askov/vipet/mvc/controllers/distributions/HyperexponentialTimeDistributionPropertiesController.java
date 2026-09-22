package dev.askov.vipet.mvc.controllers.distributions;

import dev.askov.vipet.common.FilterUtil;
import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeUnit;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.mvc.common.LocaleAwareDoubleConverter;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.models.distributions.HyperexponentialTimeDistributionModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HyperexponentialTimeDistributionPropertiesController
    extends AbstractController<HyperexponentialTimeDistributionModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(HyperexponentialTimeDistributionPropertiesController.class);
  private static final FontIcon REMOVE_ICON = new FontIcon("codicon-trash:24");

  @FXML private TextField probabilityTextField;
  @FXML private TextField rateTextField;
  @FXML private ChoiceBox<TimeUnit> rateTimeUnitChoiceBox;
  @FXML private TextField scaleTextField;
  @FXML private ChoiceBox<TimeUnit> scaleTimeUnitChoiceBox;

  @FXML
  private TableView<HyperexponentialTimeDistributionModel.ProbabilityRatePair> parametersTableView;

  @FXML
  private TableColumn<HyperexponentialTimeDistributionModel.ProbabilityRatePair, Number>
      indexTableColumn;

  @FXML
  private TableColumn<HyperexponentialTimeDistributionModel.ProbabilityRatePair, Number>
      probabilityTableColumn;

  @FXML
  private TableColumn<HyperexponentialTimeDistributionModel.ProbabilityRatePair, Number>
      rateTableColumn;

  private final DoubleProperty rateProperty =
      new SimpleDoubleProperty(
          this, "rate", HyperexponentialTimeDistributionModel.ProbabilityRatePair.DEFAULT_RATE);

  public HyperexponentialTimeDistributionPropertiesController(
      final HyperexponentialTimeDistributionModel model) {
    super(model);
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    probabilityTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareDoubleConverter(),
            HyperexponentialTimeDistributionModel.ProbabilityRatePair.DEFAULT_PROBABILITY,
            FilterUtil.NON_NEGATIVE_VALUES));

    rateTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    rateTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    rateTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedRateTimeUnit, oldRateTimeUnit, newRateTimeUnit) ->
                rateTextField.setText(
                    NumericFormatUtil.useLocalizedDecimalSeparator(
                        rateProperty.get() * newRateTimeUnit.convertToSeconds())));

    rateTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareDoubleConverter(), rateProperty.get(), FilterUtil.NON_NEGATIVE_VALUES));
    Bindings.bindBidirectional(
        rateTextField.textProperty(),
        rateProperty,
        new StringConverter<>() {

          @Override
          public String toString(Number modelRate) {
            return NumericFormatUtil.useLocalizedDecimalSeparator(
                modelRate.doubleValue() * rateTimeUnitChoiceBox.getValue().convertToSeconds());
          }

          @Override
          public Number fromString(String rateTextFieldValue) {
            rateTextFieldValue = NumericFormatUtil.useDefaultDecimalSeparator(rateTextFieldValue);
            try {
              return Double.parseDouble(rateTextFieldValue)
                  / rateTimeUnitChoiceBox.getValue().convertToSeconds();
            } catch (NumberFormatException e) {
              return null;
            }
          }
        });

    scaleTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    scaleTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    scaleTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedScaleTimeUnit, oldScaleTimeUnit, newScaleTimeUnit) -> {
              try {
                final var oldScale = 1.0 / rateProperty.get();

                scaleTextField.setText(
                    NumericFormatUtil.useLocalizedDecimalSeparator(
                        oldScale / newScaleTimeUnit.convertToSeconds()));
              } catch (final NumberFormatException e) {
                scaleTextField.setText("0.0");
              }
            });

    try {
      scaleTextField.setTextFormatter(
          new TextFormatter<>(
              new LocaleAwareDoubleConverter(),
              1.0
                  / Double.parseDouble(rateTextField.getText())
                  / scaleTimeUnitChoiceBox.getValue().convertToSeconds(),
              FilterUtil.NON_NEGATIVE_VALUES));
    } catch (NumberFormatException e) {
      scaleTextField.setTextFormatter(
          new TextFormatter<>(
              new LocaleAwareDoubleConverter(),
              1.0
                  / HyperexponentialTimeDistributionModel.ProbabilityRatePair.DEFAULT_RATE
                  / scaleTimeUnitChoiceBox.getValue().convertToSeconds(),
              FilterUtil.NON_NEGATIVE_VALUES));
    }
    Bindings.bindBidirectional(
        scaleTextField.textProperty(),
        rateProperty,
        new StringConverter<>() {

          @Override
          public String toString(Number modelRate) {
            return NumericFormatUtil.useLocalizedDecimalSeparator(
                1.0
                    / modelRate.doubleValue()
                    / scaleTimeUnitChoiceBox.getValue().convertToSeconds());
          }

          @Override
          public Number fromString(String scaleTextFieldValue) {
            scaleTextFieldValue = NumericFormatUtil.useDefaultDecimalSeparator(scaleTextFieldValue);
            try {
              return 1.0
                  / Double.parseDouble(scaleTextFieldValue)
                  / scaleTimeUnitChoiceBox.getValue().convertToSeconds();
            } catch (NumberFormatException e) {
              return null;
            }
          }
        });

    indexTableColumn.setSortable(false);
    indexTableColumn.setCellValueFactory(
        cellData ->
            new ReadOnlyObjectWrapper<>(
                parametersTableView.getItems().indexOf(cellData.getValue()) + 1));
    indexTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));

    probabilityTableColumn.setCellValueFactory(
        cellData -> cellData.getValue().probabilityProperty());
    probabilityTableColumn.setCellFactory(
        TextFieldTableCell.forTableColumn(new NumberStringConverter()));
    probabilityTableColumn.setOnEditCommit(
        event -> {
          if (event.getNewValue().doubleValue() <= 0.0 || event.getNewValue().doubleValue() > 1.0) {
            final var invalidProbabilityValueAlert =
                UIUtil.createAlert(
                    Alert.AlertType.ERROR,
                    parametersTableView.getScene().getWindow(),
                    "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.title",
                    "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.header",
                    "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.content");

            invalidProbabilityValueAlert.show();

            parametersTableView.refresh();
          } else {
            event.getRowValue().setProbability(event.getNewValue().doubleValue());
          }
          parametersTableView.requestFocus();
        });

    rateTableColumn.setCellValueFactory(cellData -> cellData.getValue().rateProperty());
    rateTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
    rateTableColumn.setOnEditCommit(
        event -> {
          if (event.getNewValue().doubleValue() <= 0.0) {
            final var invalidRateValueAlert =
                UIUtil.createAlert(
                    Alert.AlertType.ERROR,
                    parametersTableView.getScene().getWindow(),
                    "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.title",
                    "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.header",
                    "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.content");

            invalidRateValueAlert.show();

            parametersTableView.refresh();
          } else {
            event.getRowValue().setRate(event.getNewValue().doubleValue());
          }
          parametersTableView.requestFocus();
        });

    parametersTableView.setItems(model.probabilityRatePairsProperty());

    final var contextMenu = new ContextMenu();
    final var removeMenuItem =
        new MenuItem(
            resources.getString("hyperexponentialTimeDistributionProperties.removeMenuItem"));

    removeMenuItem.setOnAction(
        event -> {
          final var selectedItem = parametersTableView.getSelectionModel().getSelectedItem();

          model.probabilityRatePairsProperty().remove(selectedItem);
        });
    removeMenuItem.setGraphic(REMOVE_ICON);

    contextMenu.getItems().add(removeMenuItem);
    parametersTableView.setContextMenu(contextMenu);

    parametersTableView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (value, oldValue, newValue) ->
                removeMenuItem.setDisable(parametersTableView.getItems().size() <= 1));
  }

  @FXML
  private void onAdd() {
    double probability;
    double rate;

    try {
      probability =
          Double.parseDouble(
              NumericFormatUtil.useDefaultDecimalSeparator(probabilityTextField.getText()));

      if (probability <= 0.0 || probability > 1.0) {
        final var invalidProbabilityValueAlert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                parametersTableView.getScene().getWindow(),
                "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.title",
                "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.header",
                "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.content");

        invalidProbabilityValueAlert.show();

        return;
      }
    } catch (final NumberFormatException e) {
      final var invalidProbabilityValueAlert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              parametersTableView.getScene().getWindow(),
              "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.title",
              "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.header",
              "hyperexponentialTimeDistributionProperties.invalidProbabilityValueAlert.content");

      invalidProbabilityValueAlert.show();

      return;
    }

    try {
      rate =
          Double.parseDouble(NumericFormatUtil.useDefaultDecimalSeparator(rateTextField.getText()))
              / rateTimeUnitChoiceBox.getValue().convertToSeconds();

      if (rate <= 0.0) {
        final var invalidRateValueAlert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                parametersTableView.getScene().getWindow(),
                "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.title",
                "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.header",
                "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.content");

        invalidRateValueAlert.show();

        return;
      }
    } catch (final NumberFormatException e) {
      final var invalidRateValueAlert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              parametersTableView.getScene().getWindow(),
              "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.title",
              "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.header",
              "hyperexponentialTimeDistributionProperties.invalidRateValueAlert.content");

      invalidRateValueAlert.show();

      return;
    }

    model
        .probabilityRatePairsProperty()
        .add(new HyperexponentialTimeDistributionModel.ProbabilityRatePair(probability, rate));
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    var sum = 0.0;

    for (final var probabilityRatePair : model.probabilityRatePairsProperty()) {
      sum += probabilityRatePair.probability();
    }

    if (sum != 1.0) {
      final var invalidProbabilitySumAlert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              parametersTableView.getScene().getWindow(),
              "hyperexponentialTimeDistributionProperties.invalidProbabilitySumAlert.title",
              "hyperexponentialTimeDistributionProperties.invalidProbabilitySumAlert.header",
              "hyperexponentialTimeDistributionProperties.invalidProbabilitySumAlert.content");

      invalidProbabilitySumAlert.show();

      event.consume();
    }
  }
}
