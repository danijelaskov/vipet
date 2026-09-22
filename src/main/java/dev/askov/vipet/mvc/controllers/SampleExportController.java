package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.common.ConcurrentUtil;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import dev.askov.vipet.serialization.excel.XLSXSampleExporter;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SampleExportController extends TrivialAbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleExportController.class);

  private final DiscreteEventDrivenSimulator simulator;
  private final File directory;
  private Task<Void> task;

  @FXML private VBox mainVBox;
  @FXML private ProgressBar progressBar;

  public SampleExportController(
      final DiscreteEventDrivenSimulator simulator, final File directory) {
    this.simulator = simulator;
    this.directory = directory;
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Exporting samples to directory {}", directory);

    if (directory != null) {
      task =
          new Task<>() {

            @Override
            protected Void call() {
              try {
                final var executorService =
                    ConcurrentUtil.getExecutorService(ConcurrentUtil.TaskType.IO_BOUND);
                final var totalRuns = simulator.getNumberOfRuns();

                final var numFilesToGenerate =
                    totalRuns * (simulator.getNetworkModel().getServiceCenterModels().size() + 1);
                final var generatedFiles = new AtomicInteger();
                final var countDownLatch =
                    new CountDownLatch(
                        totalRuns
                            * (simulator.getNetworkModel().getServiceCenterModels().size() + 1));

                for (var runIndex = 0; runIndex < totalRuns; runIndex++) {
                  final var runDirectory =
                      new File(
                          directory,
                          String.format(
                              resources.getString("sampleExport.simulation"), runIndex + 1));

                  if (!runDirectory.exists() && !runDirectory.mkdir()) {
                    throw new Exception(
                        "Failed to create directory %s".formatted(runDirectory.getName()));
                  }

                  final var sampleGenerators = simulator.getSampleGenerators(runIndex);

                  for (final var serviceCenterModel :
                      simulator.getNetworkModel().getServiceCenterModels()) {
                    executorService.submit(
                        () -> {
                          final var serviceCenterModelSampleGenerators =
                              sampleGenerators.stream()
                                  .filter(
                                      sampleGenerator ->
                                          Objects.equals(
                                              sampleGenerator.getNamedModel().getName(),
                                              serviceCenterModel.getName()))
                                  .toList();

                          new XLSXSampleExporter(
                                  runDirectory,
                                  serviceCenterModelSampleGenerators,
                                  serviceCenterModel,
                                  resources)
                              .export();

                          updateProgress(generatedFiles.incrementAndGet(), numFilesToGenerate);
                          countDownLatch.countDown();
                        });
                  }

                  executorService.submit(
                      () -> {
                        final var networkModelSampleGenerators =
                            sampleGenerators.stream()
                                .filter(
                                    sampleGenerator ->
                                        Objects.equals(
                                            sampleGenerator.getNamedModel().getName(),
                                            simulator.getNetworkModel().getName()))
                                .toList();

                        new XLSXSampleExporter(
                                runDirectory,
                                networkModelSampleGenerators,
                                simulator.getNetworkModel(),
                                resources)
                            .export();

                        updateProgress(generatedFiles.incrementAndGet(), numFilesToGenerate);
                        countDownLatch.countDown();
                      });
                }

                executorService.shutdown();
                countDownLatch.await();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Sample export was interrupted: {}", e.getMessage());
              } catch (Exception e) {
                LOGGER.error("Exception occurred during sample export: {}", e.getMessage());

                final var exportFailedAlert =
                    UIUtil.createAlert(
                        Alert.AlertType.ERROR,
                        mainVBox.getScene().getWindow(),
                        "sampleExport.exportFailedAlert.title",
                        "sampleExport.exportFailedAlert.header",
                        "sampleExport.exportFailedAlert.content");

                exportFailedAlert.show();

                mainVBox.getScene().getWindow().hide();
              }
              return null;
            }
          };

      progressBar.progressProperty().bind(task.progressProperty());
      progressBar
          .progressProperty()
          .addListener(
              (value, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1.0) {
                  final var exportSucceededAlert =
                      UIUtil.createAlert(
                          Alert.AlertType.INFORMATION,
                          mainVBox.getScene().getWindow(),
                          "sampleExport.exportSucceededAlert.title",
                          "sampleExport.exportSucceededAlert.header",
                          "sampleExport.exportSucceededAlert.content");

                  exportSucceededAlert.show();

                  mainVBox.getScene().getWindow().hide();
                }
              });

      // Start the task in a background thread
      final var thread = new Thread(task);

      thread.setDaemon(true);
      thread.start();
    } else {
      LOGGER.warn("Directory is null, cannot export samples");
    }
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    if (task.isRunning() && progressBar.getProgress() != 1.0) {
      event.consume();
    }
  }
}
