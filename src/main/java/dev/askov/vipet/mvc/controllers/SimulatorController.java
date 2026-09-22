package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import dev.askov.vipet.mvc.ControllerInjector;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimulatorController extends AbstractController<DiscreteEventDrivenSimulator> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorController.class);

  public SimulatorController(final DiscreteEventDrivenSimulator discreteEventDrivenSimulator) {
    super(discreteEventDrivenSimulator);

    ControllerInjector.registerControllerFactory(
        PerformanceMeasuresProviderController.class,
        () -> new PerformanceMeasuresProviderController(discreteEventDrivenSimulator));
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

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
