package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeFormatUtil;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasuresProvider;
import javafx.beans.binding.Bindings;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;

public final class PerformanceMeasuresProviderController
    extends AbstractController<PerformanceMeasuresProvider> {

  private static final Logger LOGGER =
      org.slf4j.LoggerFactory.getLogger(PerformanceMeasuresProviderController.class);
  private static final double MIN_PROGRESS = 1e-4;

  @FXML private ProgressBar progressBar;
  @FXML private Label progressLabel;
  @FXML private Label elapsedTimeLabel;

  private final EventHandler<WorkerStateEvent> successHandler = this::onSuccess;

  public PerformanceMeasuresProviderController(
      final PerformanceMeasuresProvider performanceMeasuresProvider) {
    super(performanceMeasuresProvider);
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    progressBar.progressProperty().bind(model.progressProperty());
    model
        .progressProperty()
        .addListener(
            (progress, oldProgress, newProgress) -> {
              if (newProgress.doubleValue() < MIN_PROGRESS) {
                return;
              }

              progressLabel.setText(NumericFormatUtil.formatPercentage(newProgress.doubleValue()));
            });
    progressLabel.setText(NumericFormatUtil.formatPercentage(0));
    elapsedTimeLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    resources
                        .getString("performanceMeasuresProvider.elapsedTime")
                        .formatted(
                            TimeFormatUtil.formatNanoseconds(
                                System.nanoTime() - model.getStartTime())),
                model.progressProperty()));

    model.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, successHandler);
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    model.removeEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, successHandler);
  }

  private void onSuccess(final WorkerStateEvent event) {
    elapsedTimeLabel.textProperty().unbind();
    elapsedTimeLabel.setText(
        resources
            .getString("performanceMeasuresProvider.elapsedTime")
            .formatted(TimeFormatUtil.formatNanoseconds(model.getElapsedTime())));
  }
}
