package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.analyzer.Analyzer;
import dev.askov.vipet.core.analyzer.GordonNewellAnalyzer;
import dev.askov.vipet.core.analyzer.JacksonNetworkAnalyzer;
import dev.askov.vipet.core.analyzer.exceptions.UnstableNetworkException;
import dev.askov.vipet.core.simulator.Simulator;
import dev.askov.vipet.mvc.ControllerInjector;
import dev.askov.vipet.mvc.models.distributions.ExponentialTimeDistributionModel;
import java.io.IOException;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalyzerController<SomeAnalyzer extends Analyzer<?>>
    extends AbstractController<SomeAnalyzer> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerController.class);

  @FXML private VBox rootVBox;
  @FXML private VBox loadingVBox;
  @FXML private FontIcon loadingFontIcon;

  private final EventHandler<WorkerStateEvent> successHandler = this::onSuccess;
  private final EventHandler<WorkerStateEvent> failureHandler = this::onFailure;

  public AnalyzerController(final SomeAnalyzer someAnalyzer) {
    super(someAnalyzer);

    ControllerInjector.registerControllerFactory(
        PerformanceMeasuresProviderController.class,
        () -> new PerformanceMeasuresProviderController(someAnalyzer));
  }

  @Override
  protected void initialize() {
    final var rotateTransition = new RotateTransition(Duration.seconds(1), loadingFontIcon);
    rotateTransition.setByAngle(360);
    rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
    rotateTransition.setInterpolator(Interpolator.LINEAR);
    rotateTransition.play();

    model.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, successHandler);
    model.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, failureHandler);

    model.reset();
    model.start();
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    removeEventHandlers();
  }

  private void removeEventHandlers() {
    model.removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, successHandler);
    model.removeEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, failureHandler);
  }

  private void onSuccess(final WorkerStateEvent event) {
    rootVBox.getChildren().remove(loadingVBox);

    AnchorPane anchorPane = null;
    try {
      if (model instanceof JacksonNetworkAnalyzer) {
        anchorPane = UIUtil.loadAnchorPane("JacksonAnalysis");
      } else if (model instanceof GordonNewellAnalyzer) {
        anchorPane = UIUtil.loadAnchorPane("GordonNewellAnalysis");
      }
      if (anchorPane != null) {
        rootVBox.getChildren().add(anchorPane);
        VBox.setVgrow(anchorPane, Priority.ALWAYS);
        rootVBox.layout();
      }
    } catch (IOException e) {
      LOGGER.error("Failed to load analysis window");
    }
  }

  private void onFailure(final WorkerStateEvent event) {
    final var throwable = event.getSource().getException();

    if (throwable instanceof UnstableNetworkException unstableNetworkException) {
      final var nonErgodicServiceCenterModel =
          unstableNetworkException.getNonErgodicServiceCenterModel();
      final var networkCannotBeAnalyzedAlert =
          UIUtil.createAlert(
              Alert.AlertType.WARNING,
              rootVBox.getScene().getWindow(),
              "analysis.dialogs.networkCannotBeAnalyzed.title",
              "analysis.dialogs.networkCannotBeAnalyzed.header");

      networkCannotBeAnalyzedAlert.setContentText(
          resources
              .getString("analysis.dialogs.networkCannotBeAnalyzed.content.unstableNetwork")
              .formatted(
                  nonErgodicServiceCenterModel.getName(),
                  NumericFormatUtil.formatNumber(unstableNetworkException.getArrivalRate()),
                  NumericFormatUtil.formatNumber(
                      ((ExponentialTimeDistributionModel)
                              nonErgodicServiceCenterModel.getSelectedTimeDistribution())
                          .getRate())));
      networkCannotBeAnalyzedAlert.setOnCloseRequest(
          closeRequest -> {
            removeEventHandlers();

            final var window = (Stage) rootVBox.getScene().getWindow();

            window.close();
            window.getOwner().requestFocus();
          });
      networkCannotBeAnalyzedAlert.setWidth(500);
      networkCannotBeAnalyzedAlert.show();
    } else {
      LOGGER.error(
          "{} unexpectedly failed: ",
          event.getSource() instanceof Simulator ? "Simulator" : "Analyzer",
          throwable);

      final var message = event.getSource().getException().getMessage();
      final var stringBuilder = new StringBuilder(message != null ? message + "\n\n" : "");

      for (final var stackTraceElement : event.getSource().getException().getStackTrace()) {
        stringBuilder.append(stackTraceElement.toString()).append("\n");
      }

      final var analysisFailedAlert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              rootVBox.getScene().getWindow(),
              "analysis.dialogs.analysisFailed.title",
              "analysis.dialogs.analysisFailed.header");

      analysisFailedAlert.setContentText(stringBuilder.toString());
      analysisFailedAlert.setResizable(true);
      analysisFailedAlert.showAndWait();
    }
  }
}
