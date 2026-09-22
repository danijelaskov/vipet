package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import dev.askov.vipet.mvc.ControllerInjector;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GuidedSimulatorController
    extends AbstractController<DiscreteEventDrivenSimulator> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GuidedSimulatorController.class);

  @FXML private TabPane simulationsTabPane;

  public GuidedSimulatorController(
      final DiscreteEventDrivenSimulator discreteEventDrivenSimulator) {
    super(discreteEventDrivenSimulator);

    ControllerInjector.registerControllerFactory(
        PerformanceMeasuresProviderController.class,
        () -> new PerformanceMeasuresProviderController(discreteEventDrivenSimulator));
  }

  @Override
  protected void initialize() {
    model.setOnNextGuidedSimulation(
        () ->
            Platform.runLater(
                () -> {
                  final var guidedDiscreteEventDrivenSimulationModel =
                      model.createNextSimulationModel();

                  LOGGER.debug(
                      "Registering controller factory for GuidedDiscreteEventDrivenSimulationController");
                  ControllerInjector.registerControllerFactory(
                      GuidedDiscreteEventDrivenSimulationController.class,
                      () ->
                          new GuidedDiscreteEventDrivenSimulationController(
                              guidedDiscreteEventDrivenSimulationModel, model.getNetworkModel()));

                  try {
                    final var tab =
                        UIUtil.loadTab("GuidedDiscreteEventDrivenSimulation", simulationsTabPane);

                    tab.setClosable(false);
                    tab.setText(
                        resources
                            .getString("guidedSimulator.simulation")
                            .formatted(
                                guidedDiscreteEventDrivenSimulationModel.getSimulation().getID()
                                    + 1));
                    simulationsTabPane.getSelectionModel().select(tab);
                  } catch (IOException e) {
                    LOGGER.error("Failed to load GuidedDiscreteEventDrivenSimulation window", e);
                  }
                }));

    model.reset();
    model.start();
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    if (model.isRunning()) {
      model.cancel();
    }
  }
}
