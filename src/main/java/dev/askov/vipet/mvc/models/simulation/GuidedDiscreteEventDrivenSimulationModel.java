package dev.askov.vipet.mvc.models.simulation;

import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulation;
import dev.askov.vipet.core.simulator.discrete.Event;
import dev.askov.vipet.mvc.models.network.ConnectionModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.WorkerStateEvent;

public class GuidedDiscreteEventDrivenSimulationModel {

  private final DiscreteEventDrivenSimulation simulation;

  private final ReadOnlyObjectWrapper<Event> currentEventPropertyWrapper =
      new ReadOnlyObjectWrapper<>(this, "currentEvent");
  private final ReadOnlyObjectWrapper<ConnectionModel> animatedConnectionPropertyWrapper =
      new ReadOnlyObjectWrapper<>(this, "animatedConnection");

  public GuidedDiscreteEventDrivenSimulationModel(final DiscreteEventDrivenSimulation simulation) {
    this.simulation = simulation;

    currentEventPropertyWrapper.set(simulation.getCurrentEvent());
    simulation.setOnNextEvent(
        event -> {
          if (event != null) {
            Platform.runLater(() -> currentEventPropertyWrapper.set(event));
          }
        });
    simulation.setOnNextJobTransition(
        connectionModel ->
            Platform.runLater(() -> animatedConnectionPropertyWrapper.set(connectionModel)));
    simulation.addEventHandler(
        WorkerStateEvent.WORKER_STATE_SUCCEEDED,
        event ->
            Platform.runLater(() -> currentEventPropertyWrapper.set(simulation.getCurrentEvent())));
  }

  public DiscreteEventDrivenSimulation getSimulation() {
    return simulation;
  }

  public ReadOnlyObjectWrapper<Event> currentEventProperty() {
    return currentEventPropertyWrapper;
  }

  public Event getCurrentEvent() {
    return currentEventPropertyWrapper.get();
  }

  public ReadOnlyObjectProperty<ConnectionModel> animatedConnection() {
    return animatedConnectionPropertyWrapper.getReadOnlyProperty();
  }

  public ConnectionModel getAnimatedConnection() {
    return animatedConnectionPropertyWrapper.get();
  }

  public void setAnimatedConnection(final ConnectionModel connectionModel) {
    animatedConnectionPropertyWrapper.set(connectionModel);
  }

  public void nextEvent() {
    simulation.nextEvent();
  }

  public void skipEvents(final int numberOfEvents) {
    final var onNextJobTransition = simulation.getOnNextJobTransition();
    simulation.setOnNextJobTransition(null);

    for (var i = 0; i < numberOfEvents; i++) {
      simulation.nextEvent();
    }

    simulation.setOnNextJobTransition(onNextJobTransition);
  }

  public void skipAllRemainingEvents() {
    simulation.setGuided(false);
    simulation.nextEvent(true);
  }
}
