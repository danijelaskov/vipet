package dev.askov.vipet.mvc.controllers;

import static dev.askov.vipet.core.simulator.Simulator.DEFAULT_MAX_DURATION;
import static dev.askov.vipet.core.simulator.Simulator.DEFAULT_MAX_SIMULATED_TIME;
import static dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator.DEFAULT_CONFIDENCE_LEVEL;
import static dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator.DEFAULT_DISCARD_RATIO;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeUnit;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import dev.askov.vipet.mvc.common.IntegerSpinnerValueFactory;
import dev.askov.vipet.mvc.common.LongSpinnerValueFactory;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import dev.askov.vipet.mvc.views.components.NonNegativeIntegerTableCell;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Random;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimulatorParametersController
    extends AbstractController<DiscreteEventDrivenSimulator> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorParametersController.class);

  @FXML private Spinner<Integer> numberOfRunsSpinner;
  @FXML private TextField maxDurationTextField;
  @FXML private ChoiceBox<TimeUnit> maxDurationUnitChoiceBox;
  @FXML private TextField maxSimulatedTimeTextField;
  @FXML private ChoiceBox<TimeUnit> maxSimulatedTimeUnitChoiceBox;
  @FXML private Spinner<Integer> maxSampleSizeSpinner;
  @FXML private Spinner<Integer> maxNumberOfEventsSpinner;
  @FXML private ChoiceBox<Class<? extends RandomGenerator>> randomGeneratorChoiceBox;
  @FXML private Spinner<Long> seedSpinner;
  @FXML private TextField sampleDiscardRatioTextField;
  @FXML private TextField confidenceLevelTextField;
  @FXML private Separator additionalParametersSeparator;
  @FXML private Label initialNumberOfJobsExplanationLabel;
  @FXML private TableView<ServiceCenterModel> jobDistributionTableView;
  @FXML private TableColumn<ServiceCenterModel, String> resourceNameTableColumn;
  @FXML private TableColumn<ServiceCenterModel, Integer> initialNumberOfJobsTableColumn;

  private final Random randomGenerator = new Random();

  public SimulatorParametersController(final DiscreteEventDrivenSimulator model) {
    super(model);
  }

  @Override
  protected void initialize() {
    numberOfRunsSpinner.setValueFactory(new IntegerSpinnerValueFactory(2, Integer.MAX_VALUE));
    numberOfRunsSpinner.getValueFactory().setValue(model.getNumberOfRuns());
    numberOfRunsSpinner
        .valueProperty()
        .addListener(
            (numberOfRuns, oldNumberOfRuns, newNumberOfRuns) ->
                model.setNumberOfRuns(newNumberOfRuns));

    maxDurationUnitChoiceBox.getItems().addAll(TimeUnit.values());
    maxDurationUnitChoiceBox.getSelectionModel().select(TimeUnit.MINUTE);

    maxDurationTextField
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    String.valueOf(
                        model.getMaxDuration()
                            / maxDurationUnitChoiceBox
                                .getSelectionModel()
                                .getSelectedItem()
                                .convertToSeconds()),
                maxDurationUnitChoiceBox.getSelectionModel().selectedItemProperty()));
    maxDurationTextField
        .focusedProperty()
        .addListener(
            (focused, wasFocused, isFocused) -> {
              if (isFocused) {
                maxDurationTextField.textProperty().unbind();
                maxDurationTextField
                    .textProperty()
                    .addListener(
                        (durationText, oldText, newText) -> {
                          try {
                            model.setMaxDuration(
                                NumericFormatUtil.parseDouble(newText)
                                    * maxDurationUnitChoiceBox
                                        .getSelectionModel()
                                        .getSelectedItem()
                                        .convertToSeconds());
                          } catch (ParseException e) {
                            model.setMaxDuration(
                                DEFAULT_MAX_DURATION
                                    * maxDurationUnitChoiceBox
                                        .getSelectionModel()
                                        .getSelectedItem()
                                        .convertToSeconds());
                          }
                        });
              } else {
                maxDurationTextField
                    .textProperty()
                    .bind(
                        Bindings.createStringBinding(
                            () ->
                                String.valueOf(
                                    model.getMaxDuration()
                                        / maxDurationUnitChoiceBox
                                            .getSelectionModel()
                                            .getSelectedItem()
                                            .convertToSeconds()),
                            maxDurationUnitChoiceBox.getSelectionModel().selectedItemProperty()));
              }
            });

    maxSimulatedTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    maxSimulatedTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.HOUR);

    maxSimulatedTimeTextField
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    String.valueOf(
                        model.getMaxSimulatedTime()
                            / maxSimulatedTimeUnitChoiceBox
                                .getSelectionModel()
                                .getSelectedItem()
                                .convertToSeconds()),
                maxSimulatedTimeUnitChoiceBox.getSelectionModel().selectedItemProperty()));
    maxSimulatedTimeTextField
        .focusedProperty()
        .addListener(
            (focused, wasFocused, isFocused) -> {
              if (isFocused) {
                maxSimulatedTimeTextField.textProperty().unbind();
                maxSimulatedTimeTextField
                    .textProperty()
                    .addListener(
                        (durationText, oldText, newText) -> {
                          try {
                            model.setMaxSimulatedTime(
                                NumericFormatUtil.parseDouble(newText)
                                    * maxSimulatedTimeUnitChoiceBox
                                        .getSelectionModel()
                                        .getSelectedItem()
                                        .convertToSeconds());
                          } catch (ParseException e) {
                            model.setMaxSimulatedTime(
                                DEFAULT_MAX_SIMULATED_TIME
                                    * maxSimulatedTimeUnitChoiceBox
                                        .getSelectionModel()
                                        .getSelectedItem()
                                        .convertToSeconds());
                          }
                        });
              } else {
                maxSimulatedTimeTextField
                    .textProperty()
                    .bind(
                        Bindings.createStringBinding(
                            () ->
                                String.valueOf(
                                    model.getMaxSimulatedTime()
                                        / maxSimulatedTimeUnitChoiceBox
                                            .getSelectionModel()
                                            .getSelectedItem()
                                            .convertToSeconds()),
                            maxSimulatedTimeUnitChoiceBox
                                .getSelectionModel()
                                .selectedItemProperty()));
              }
            });

    maxSampleSizeSpinner.setValueFactory(new IntegerSpinnerValueFactory(2, Integer.MAX_VALUE));
    maxSampleSizeSpinner.getValueFactory().setValue(model.getMaxSampleSize());
    maxSampleSizeSpinner
        .valueProperty()
        .addListener(
            (maxSampleSize, oldMaxSampleSize, newMaxSampleSize) ->
                model.setMaxSampleSize(newMaxSampleSize));

    maxNumberOfEventsSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
    maxNumberOfEventsSpinner.getValueFactory().setValue(model.getMaxNumberOfEvents());
    maxNumberOfEventsSpinner
        .valueProperty()
        .addListener(
            (numberOfEvents, oldNumberOfEvents, newNumberOfEvents) ->
                model.setMaxNumberOfEvents(newNumberOfEvents));

    randomGeneratorChoiceBox.setConverter(
        new javafx.util.StringConverter<>() {
          @Override
          public String toString(final Class<? extends RandomGenerator> randomGeneratorType) {
            if (randomGeneratorType == null) {
              return null;
            }

            return randomGeneratorType.getSimpleName();
          }

          @Override
          public Class<? extends RandomGenerator> fromString(final String string) {
            if (string == null || string.isEmpty()) {
              return null;
            }

            return DiscreteEventDrivenSimulator.AVAILABLE_RANDOM_GENERATORS.stream()
                .filter(randomGeneratorType -> randomGeneratorType.getSimpleName().equals(string))
                .findFirst()
                .orElse(null);
          }
        });
    randomGeneratorChoiceBox
        .getItems()
        .addAll(DiscreteEventDrivenSimulator.AVAILABLE_RANDOM_GENERATORS);
    randomGeneratorChoiceBox.getSelectionModel().select(model.getRandomGeneratorType());
    model
        .randomGeneratorTypeProperty()
        .bind(randomGeneratorChoiceBox.getSelectionModel().selectedItemProperty());

    seedSpinner.setValueFactory(new LongSpinnerValueFactory(0, Long.MAX_VALUE, model.getSeed()));
    model.seedProperty().bind(seedSpinner.valueProperty());

    sampleDiscardRatioTextField.setText(
        NumericFormatUtil.formatPercentage(model.getSampleDiscardRatio()));
    sampleDiscardRatioTextField
        .textProperty()
        .addListener(
            (warmupDiscardRatioText, oldWarmupDiscardRatioText, newWarmupDiscardRatioText) -> {
              double newWarmupDiscardRatio;
              try {
                newWarmupDiscardRatio = NumericFormatUtil.parseDouble(newWarmupDiscardRatioText);
              } catch (ParseException e) {
                newWarmupDiscardRatio = DEFAULT_DISCARD_RATIO;
              }
              model.setSampleDiscardRatio(newWarmupDiscardRatio);
            });

    confidenceLevelTextField.setText(
        NumericFormatUtil.formatPercentage(model.getConfidenceLevel()));
    confidenceLevelTextField
        .textProperty()
        .addListener(
            (confidenceLevelText, oldConfidenceLevelText, newConfidenceLevelText) -> {
              double newConfidenceLevel;
              try {
                newConfidenceLevel = NumericFormatUtil.parseDouble(newConfidenceLevelText);
              } catch (ParseException e) {
                newConfidenceLevel = DEFAULT_CONFIDENCE_LEVEL;
              }
              model.setConfidenceLevel(newConfidenceLevel);
            });

    additionalParametersSeparator.disableProperty().bind(model.getNetworkModel().openProperty());

    if (model.getNetworkModel().isClosed()) {
      initialNumberOfJobsExplanationLabel.setText(
          resources
              .getString("simulatorParameters.initialNumberOfJobsExplanation.closedNetwork")
              .formatted(model.getNetworkModel().getNumberOfJobs()));
    } else {
      initialNumberOfJobsExplanationLabel.setText(
          resources.getString("simulatorParameters.initialNumberOfJobsExplanation.openNetwork"));
    }

    resourceNameTableColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    resourceNameTableColumn
        .prefWidthProperty()
        .bind(jobDistributionTableView.widthProperty().multiply(0.49));

    initialNumberOfJobsTableColumn.setCellValueFactory(
        cellData -> cellData.getValue().initialNumberOfJobsProperty().asObject());
    initialNumberOfJobsTableColumn.setCellFactory(
        column ->
            new NonNegativeIntegerTableCell<>(
                ServiceCenterModel::getCapacity, ServiceCenterModel::setInitialNumberOfJobs));
    initialNumberOfJobsTableColumn
        .prefWidthProperty()
        .bind(jobDistributionTableView.widthProperty().multiply(0.49));

    jobDistributionTableView.setItems(model.getNetworkModel().getServiceCenterModels());
  }

  @FXML
  private void onRandomSeed() {
    seedSpinner.getValueFactory().setValue(randomGenerator.nextLong() & Long.MAX_VALUE);
  }

  @FXML
  private void onRandomGeneratorInfo() {
    final var selectedGenerator = randomGeneratorChoiceBox.getSelectionModel().getSelectedItem();
    final var generatorName = selectedGenerator.getSimpleName();

    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop()
            .browse(
                new URI(
                    resources.getString("simulatorParameters.info.url").formatted(generatorName)));
      } catch (IOException | URISyntaxException e) {
        LOGGER.warn("Could not open URL for random generator: {}", generatorName, e);
      }
    } else {
      LOGGER.warn("Desktop is not supported");
    }
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    if (model.getNetworkModel().isClosed()) {
      var sum = 0;
      for (final var serviceCenterModel : jobDistributionTableView.getItems()) {
        sum += serviceCenterModel.getInitialNumberOfJobs();
      }

      if (sum != model.getNetworkModel().getNumberOfJobs()) {
        final var alert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                jobDistributionTableView.getScene().getWindow(),
                "simulatorParameters.errorAlert.title",
                "simulatorParameters.errorAlert.header",
                "simulatorParameters.errorAlert.content.invalidSumOfInitialNumberOfJobs");

        alert.showAndWait();
        event.consume();
      }
    }
  }
}
