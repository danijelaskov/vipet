package dev.askov.vipet.mvc.controllers;

import static dev.askov.vipet.common.TimeFormatUtil.longTimeString;

import dev.askov.vipet.core.simulator.discrete.Event;
import dev.askov.vipet.mvc.common.IntegerSpinnerValueFactory;
import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.mvc.models.simulation.GuidedDiscreteEventDrivenSimulationModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GuidedDiscreteEventDrivenSimulationController
    extends AbstractController<GuidedDiscreteEventDrivenSimulationModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GuidedDiscreteEventDrivenSimulationController.class);
  private static final int DEFAULT_NUMBER_OF_EVENTS_TO_SKIP = 10;

  private static String currentEventDescription;

  private final NetworkModel networkModel;

  @FXML private Label currentTimeLabel;
  @FXML private Label currentEventLabel;
  @FXML private HBox simulationControlsHBox;
  @FXML private Button nextEventButton;
  @FXML private Button skipEventsButton;
  @FXML private Spinner<Integer> numberOfEventsToSkipSpinner;
  @FXML private Button endButton;

  public GuidedDiscreteEventDrivenSimulationController(
      final GuidedDiscreteEventDrivenSimulationModel guidedDiscreteEventDrivenSimulationModel,
      final NetworkModel networkModel) {
    super(guidedDiscreteEventDrivenSimulationModel);

    this.networkModel = networkModel;
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    currentEventDescription =
        resources.getString("guidedDiscreteEventDrivenSimulation.currentEventDescription");

    currentTimeLabel.setText(longTimeString(model.getCurrentEvent().getTime()));
    currentTimeLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> longTimeString(model.getCurrentEvent().getTime()),
                model.currentEventProperty()));

    currentEventLabel.setText(createEventDescription(model.getCurrentEvent()));
    currentEventLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> createEventDescription(model.getCurrentEvent()),
                model.currentEventProperty()));

    updateVisualizationParameters();
    model
        .currentEventProperty()
        .addListener(
            (event, oldEvent, newEvent) -> {
              if (newEvent != null) {
                updateVisualizationParameters();
              }
            });

    simulationControlsHBox.disableProperty().bind(model.animatedConnection().isNotNull());
    nextEventButton.disableProperty().bind(model.getSimulation().runningProperty().not());
    skipEventsButton.disableProperty().bind(model.getSimulation().runningProperty().not());
    numberOfEventsToSkipSpinner
        .disableProperty()
        .bind(model.getSimulation().runningProperty().not());
    numberOfEventsToSkipSpinner.setValueFactory(
        new IntegerSpinnerValueFactory(2, Integer.MAX_VALUE, DEFAULT_NUMBER_OF_EVENTS_TO_SKIP));
    endButton.disableProperty().bind(model.getSimulation().runningProperty().not());
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    LOGGER.debug("Window closed, skipping all remaining events");

    model.skipAllRemainingEvents();
  }

  private String createEventDescription(final Event event) {
    final var nodeName = event.getNetworkNodeModel().getName();
    final var eventType = event.getType().getName();

    return String.format(currentEventDescription, nodeName, eventType);
  }

  @FXML
  private void onNextEvent() {
    LOGGER.debug("Next event button clicked");

    model.nextEvent();

    LOGGER.debug("Current event: {}", model.getCurrentEvent());
  }

  @FXML
  private void onSkipEvents() {
    LOGGER.debug("Skip events button clicked");

    model.skipEvents(numberOfEventsToSkipSpinner.getValue());

    LOGGER.debug(
        "Skipped {} events. Current event: {}",
        numberOfEventsToSkipSpinner.getValue(),
        model.getCurrentEvent());
  }

  @FXML
  private void onSkipAllRemainingEvents() {
    LOGGER.debug("Skip all button clicked");

    model.skipAllRemainingEvents();
  }

  private void updateVisualizationParameters() {
    for (final var simulationServiceCenterModel :
        model.getSimulation().getNetwork().getServiceCenterModels()) {
      final var serviceCenterModel =
          networkModel.getServiceCenterModel(simulationServiceCenterModel.getName());
      final var currentNumberOfJobsInQueue =
          simulationServiceCenterModel.getCurrentNumberOfJobsInQueue();

      if (currentNumberOfJobsInQueue != 1 || !simulationServiceCenterModel.hasFreeServer()) {
        // Because of the way the simulation is implemented, a job may be enqueued
        // even when the queue is empty and at least one server is free —
        // this happens when the source and destination nodes are the same.
        // We omit this case from visualization to avoid misleading the viewer
        // into thinking the job waited in the queue when it never actually did.
        serviceCenterModel.getQueueModel().setJobVisualizationParameter(currentNumberOfJobsInQueue);
      }
      for (var serverIndex = 0;
          serverIndex < serviceCenterModel.getNumberOfServers();
          serverIndex++) {
        final var serverModel = serviceCenterModel.getServerModel(serverIndex);
        final var simulationServerModel = simulationServiceCenterModel.getServerModel(serverIndex);

        serverModel.setVisualizationParameter(
            simulationServerModel.getCurrentState() == ServerModel.State.BUSY ? 1.0 : 0.0);
      }
    }
  }
}
