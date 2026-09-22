package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.analyzer.GordonNewellAnalyzer;
import dev.askov.vipet.core.analyzer.JacksonNetworkAnalyzer;
import dev.askov.vipet.core.analyzer.exceptions.UnstableNetworkException;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasuresProvider;
import dev.askov.vipet.core.simulator.Simulator;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import dev.askov.vipet.localization.LocalizationManager;
import dev.askov.vipet.mvc.ControllerInjector;
import dev.askov.vipet.mvc.common.IntegerSpinnerValueFactory;
import dev.askov.vipet.mvc.controllers.converters.NonTerminalNetworkNodeConverter;
import dev.askov.vipet.mvc.controllers.distributions.*;
import dev.askov.vipet.mvc.models.WhatIfModel;
import dev.askov.vipet.mvc.models.distributions.*;
import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.mvc.views.components.ErrorsVBox;
import dev.askov.vipet.mvc.views.formatters.LocaleAwareNumberAxisFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WhatIfController extends AbstractController<WhatIfModel> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WhatIfController.class);

  private static final int MIN_NUMBER_OF_STEPS = 2;
  private static final int MAX_NUMBER_OF_STEPS = 50;
  private static final int INITIAL_NUMBER_OF_STEPS = MIN_NUMBER_OF_STEPS;
  private static final int DEFAULT_END_VALUE_FACTOR = 2;

  public enum VariableType {
    SERVICE_DISTRIBUTION("serviceDistribution"),
    JOB_GENERATION_DISTRIBUTION("jobGenerationDistribution"),
    NUM_SERVERS("numServers"),
    NUM_JOBS_IN_NETWORK("numJobsInNetwork");

    private static final LocalizationManager LOCALIZATION_MANAGER =
        LocalizationManager.getInstance();

    private final String key;

    VariableType(final String key) {
      this.key = key;
    }

    public String getName() {
      return LOCALIZATION_MANAGER.getString("whatIf.variableType." + key + ".name");
    }

    public String getNumberAxisLabel() {
      return LOCALIZATION_MANAGER.getString("whatIf.variableType." + key + ".numberAxisLabel");
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  @FXML private SplitPane rootSplitPane;
  @FXML private VBox settingsVBox;
  @FXML private RadioButton nodeRadioButton;
  @FXML private ChoiceBox<NonTerminalNetworkNodeModel> nodeChoiceBox;
  @FXML private RadioButton systemRadioButton;
  @FXML private ChoiceBox<VariableType> variableTypeChoiceBox;
  @FXML private TextField startValueTextField;
  @FXML private HBox startValueHBox;
  @FXML private Label startValueLabel;
  @FXML private TextField endValueTextField;
  @FXML private HBox endValueHBox;
  @FXML private Label endValueLabel;
  @FXML private Spinner<Integer> numberOfStepsSpinner;
  @FXML private VBox startVBox;
  @FXML private ProgressBar progressBar;
  @FXML private ScrollPane errorsScrollPane;
  @FXML private ErrorsVBox errorsVBox;
  @FXML private TabPane resultsTabPane;

  private final ObjectProperty<TimeDistributionModel> startTimeDistributionProperty =
      new SimpleObjectProperty<>();
  private final ObjectProperty<TimeDistributionModel> endTimeDistributionProperty =
      new SimpleObjectProperty<>();

  private final List<PerformanceMeasuresProvider> performanceMeasuresProviders = new ArrayList<>();
  private final List<Object> variableValues = new ArrayList<>();

  public WhatIfController(final WhatIfModel model) {
    super(model);
  }

  @Override
  protected void initialize() {
    rootSplitPane.getItems().remove(errorsScrollPane);

    numberOfStepsSpinner.setValueFactory(
        new IntegerSpinnerValueFactory(
            MIN_NUMBER_OF_STEPS, MAX_NUMBER_OF_STEPS, INITIAL_NUMBER_OF_STEPS));
    numberOfStepsSpinner.setEditable(true);
    numberOfStepsSpinner.disableProperty().bind(startValueTextField.visibleProperty());

    endValueHBox.visibleProperty().bind(endValueTextField.visibleProperty().not());
    startValueHBox.visibleProperty().bind(startValueTextField.visibleProperty().not());

    endTimeDistributionProperty.addListener(
        (value, oldValue, newValue) -> {
          if (newValue != null) {
            endValueLabel.textProperty().unbind();
            endValueLabel.textProperty().bind(newValue.descriptionProperty());
          }
        });
    startTimeDistributionProperty.addListener(
        (value, oldValue, newValue) -> {
          if (newValue != null) {
            startValueLabel.textProperty().unbind();
            startValueLabel.textProperty().bind(newValue.descriptionProperty());
          }
        });

    endValueTextField.visibleProperty().bind(startValueTextField.visibleProperty());
    startValueTextField
        .visibleProperty()
        .bind(
            variableTypeChoiceBox
                .getSelectionModel()
                .selectedItemProperty()
                .isEqualTo(VariableType.NUM_SERVERS)
                .or(
                    variableTypeChoiceBox
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isEqualTo(VariableType.NUM_JOBS_IN_NETWORK)));

    variableTypeChoiceBox.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(VariableType variableType) {
            if (variableType == null) {
              return null;
            }
            return variableType.getName();
          }

          @Override
          public VariableType fromString(String s) {
            return null;
          }
        });
    variableTypeChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (value, oldValue, newValue) -> {
              if (newValue == VariableType.NUM_SERVERS) {
                startValueTextField.setText(
                    String.valueOf(
                        ((ServiceCenterModel) nodeChoiceBox.getSelectionModel().getSelectedItem())
                            .getNumberOfServers()));
                endValueTextField.setText(
                    String.valueOf(
                        DEFAULT_END_VALUE_FACTOR
                            * Integer.parseInt(startValueTextField.getText())));
              } else if (newValue == VariableType.NUM_JOBS_IN_NETWORK) {
                startValueTextField.setText(String.valueOf(model.networkModel().getNumberOfJobs()));
                endValueTextField.setText(
                    String.valueOf(
                        DEFAULT_END_VALUE_FACTOR
                            * Integer.parseInt(startValueTextField.getText())));
              }
            });

    nodeChoiceBox.getItems().addAll(model.networkModel().getNonTerminalNetworkNodeModels());
    nodeChoiceBox.setConverter(new NonTerminalNetworkNodeConverter(model.networkModel()));
    nodeChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (value, oldValue, newValue) -> {
              if (newValue instanceof ServiceCenterModel) {
                variableTypeChoiceBox
                    .getItems()
                    .setAll(VariableType.SERVICE_DISTRIBUTION, VariableType.NUM_SERVERS);
              } else {
                variableTypeChoiceBox.getItems().setAll(VariableType.JOB_GENERATION_DISTRIBUTION);
              }
              variableTypeChoiceBox.getSelectionModel().selectFirst();
            });
    nodeChoiceBox.getSelectionModel().selectFirst();

    final var toggleGroup = new ToggleGroup();
    toggleGroup.getToggles().addAll(nodeRadioButton, systemRadioButton);

    nodeRadioButton
        .selectedProperty()
        .addListener(
            (value, oldValue, newValue) -> {
              if (newValue) {
                final var selectedNode = nodeChoiceBox.getSelectionModel().getSelectedItem();
                if (selectedNode != null) {
                  if (selectedNode instanceof ServiceCenterModel) {
                    variableTypeChoiceBox
                        .getItems()
                        .setAll(VariableType.SERVICE_DISTRIBUTION, VariableType.NUM_SERVERS);
                  } else {
                    variableTypeChoiceBox
                        .getItems()
                        .setAll(VariableType.JOB_GENERATION_DISTRIBUTION);
                  }
                  variableTypeChoiceBox.getSelectionModel().selectFirst();
                }
              }
            });
    nodeRadioButton.setSelected(true);

    systemRadioButton
        .selectedProperty()
        .addListener(
            (value, oldValue, newValue) -> {
              if (newValue) {
                variableTypeChoiceBox.getItems().setAll(VariableType.NUM_JOBS_IN_NETWORK);
                variableTypeChoiceBox.getSelectionModel().selectFirst();
              }
            });
    systemRadioButton.setDisable(model.networkModel().isOpen());

    toggleGroup
        .selectedToggleProperty()
        .addListener(
            (value, oldValue, newValue) -> nodeChoiceBox.setDisable(newValue != nodeRadioButton));

    startTimeDistributionProperty.bind(
        Bindings.createObjectBinding(
            () -> {
              final var timeDistributionModel =
                  nodeChoiceBox.getSelectionModel().getSelectedItem().getSelectedTimeDistribution();
              return timeDistributionModel.interpolate(timeDistributionModel, 0.0);
            },
            nodeChoiceBox.getSelectionModel().selectedItemProperty()));
    endTimeDistributionProperty.bind(
        Bindings.createObjectBinding(
            () -> {
              final var timeDistributionModel =
                  nodeChoiceBox.getSelectionModel().getSelectedItem().getSelectedTimeDistribution();
              return timeDistributionModel.interpolate(timeDistributionModel, 0.0);
            },
            nodeChoiceBox.getSelectionModel().selectedItemProperty()));
  }

  private int getNumberOfSteps() {
    return switch (variableTypeChoiceBox.getSelectionModel().getSelectedItem()) {
      case SERVICE_DISTRIBUTION, JOB_GENERATION_DISTRIBUTION -> numberOfStepsSpinner.getValue();
      case NUM_SERVERS, NUM_JOBS_IN_NETWORK -> {
        final var startValue = Integer.parseInt(startValueTextField.getText());
        final var endValue = Integer.parseInt(endValueTextField.getText());

        yield Math.max(startValue, endValue) - Math.min(startValue, endValue) + 1;
      }
    };
  }

  @FXML
  private void onStart() {
    settingsVBox.setDisable(true);
    performanceMeasuresProviders.clear();

    final var numberOfCompletedProviders = new AtomicInteger(0);
    final var numberOfSteps = getNumberOfSteps();

    for (var i = 0; i < numberOfSteps; i++) {
      final var networkModel = new NetworkModel(model.networkModel());

      switch (variableTypeChoiceBox.getSelectionModel().getSelectedItem()) {
        case SERVICE_DISTRIBUTION, JOB_GENERATION_DISTRIBUTION -> {
          final var nodeName = nodeChoiceBox.getSelectionModel().getSelectedItem().getName();
          final var nodeModel = networkModel.getNodeModel(nodeName);

          final var interpolationFactor = (double) i / (numberOfSteps - 1);
          final var timeDistributionModel =
              getStartTimeDistribution().interpolate(getEndTimeDistribution(), interpolationFactor);

          if (variableTypeChoiceBox.getSelectionModel().getSelectedItem()
              == VariableType.SERVICE_DISTRIBUTION) {
            ((ServiceCenterModel) nodeModel).setSelectedTimeDistribution(timeDistributionModel);
          } else {
            ((SourceModel) nodeModel).setSelectedTimeDistribution(timeDistributionModel);
          }
          variableValues.add(timeDistributionModel);
        }
        case NUM_SERVERS -> {
          final var serviceCenterModelName =
              nodeChoiceBox.getSelectionModel().getSelectedItem().getName();
          final var serviceCenterModel = networkModel.getServiceCenterModel(serviceCenterModelName);
          final var startValue = Integer.parseInt(startValueTextField.getText());
          final var endValue = Integer.parseInt(endValueTextField.getText());
          final var stepFactor = startValue < endValue ? 1 : -1;
          final var numberOfServers =
              Integer.parseInt(startValueTextField.getText()) + stepFactor * i;

          serviceCenterModel.setNumberOfServers(numberOfServers);
          variableValues.add(numberOfServers);
        }
        case NUM_JOBS_IN_NETWORK -> {
          final var numberOfJobs = Integer.parseInt(startValueTextField.getText()) + i;

          networkModel.setNumberOfJobs(numberOfJobs);
          variableValues.add(numberOfJobs);
        }
      }

      performanceMeasuresProviders.add(
          getPerformanceMeasuresProvider(networkModel, numberOfCompletedProviders));
    }

    progressBar
        .progressProperty()
        .bind(
            new DoubleBinding() {
              {
                performanceMeasuresProviders.forEach(
                    performanceMeasuresProvider ->
                        bind(performanceMeasuresProvider.progressProperty()));
              }

              @Override
              protected double computeValue() {
                return performanceMeasuresProviders.stream()
                    .mapToDouble(PerformanceMeasuresProvider::getProgress)
                    .average()
                    .orElse(0);
              }
            });

    for (var performanceMeasuresProvider : performanceMeasuresProviders) {
      performanceMeasuresProvider.start();
    }
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    for (final var performanceMeasuresProvider : performanceMeasuresProviders) {
      if (performanceMeasuresProvider.isRunning()) {
        performanceMeasuresProvider.cancel();
      }
    }
  }

  private PerformanceMeasuresProvider getPerformanceMeasuresProvider(
      NetworkModel networkModel, final AtomicInteger numberOfCompletedProviders) {
    PerformanceMeasuresProvider performanceMeasuresProvider;

    if (model.type() == WhatIfModel.PerformanceMeasuresProviderType.SIMULATOR) {
      performanceMeasuresProvider = new DiscreteEventDrivenSimulator(networkModel);
    } else {
      if (model.networkModel().isOpen()) {
        performanceMeasuresProvider = new JacksonNetworkAnalyzer(networkModel);
      } else {
        performanceMeasuresProvider = new GordonNewellAnalyzer(networkModel);
      }
    }

    performanceMeasuresProvider.setOnSucceeded(
        workerStateEvent -> {
          if (numberOfCompletedProviders.incrementAndGet() == performanceMeasuresProviders.size()) {
            resultsTabPane.setVisible(true);
            startVBox.setVisible(false);

            updateResultsTabPane();
          }
        });
    performanceMeasuresProvider.setOnFailed(
        workerStateEvent -> {
          final var throwable = workerStateEvent.getSource().getException();
          final var index = performanceMeasuresProviders.indexOf(performanceMeasuresProvider);
          final var variableType =
              variableTypeChoiceBox.getSelectionModel().getSelectedItem().getName();
          final var variableValue = variableValues.get(index).toString();

          if (throwable instanceof UnstableNetworkException) {
            errorsVBox.addError(
                resources
                    .getString("whatIf.specificError")
                    .formatted(
                        index + 1,
                        variableType,
                        variableValue,
                        resources.getString("whatIf.unstableNetwork")));
          } else {
            errorsVBox.addError(
                resources
                    .getString("whatIf.genericError")
                    .formatted(index + 1, variableType, variableValue));
            LOGGER.error(
                "{} unexpectedly failed: ",
                workerStateEvent.getSource() instanceof Simulator ? "Simulation" : "Analysis",
                throwable);
          }

          if (!rootSplitPane.getItems().contains(errorsScrollPane)) {
            rootSplitPane.getItems().add(errorsScrollPane);
            errorsScrollPane.setVisible(true);
          }

          if (numberOfCompletedProviders.incrementAndGet() == performanceMeasuresProviders.size()) {
            resultsTabPane.setVisible(true);
            startVBox.setVisible(false);

            updateResultsTabPane();
          }
        });

    return performanceMeasuresProvider;
  }

  @FXML
  private void onStartValue() throws IOException {
    registerControllerFactories(getStartTimeDistribution());
    UIUtil.loadModalWindow(
        String.format(
            "distributions/%sTimeDistributionProperties", getStartTimeDistribution().getName()),
        (Stage) rootSplitPane.getScene().getWindow(),
        nodeChoiceBox.getValue().getName());
  }

  @FXML
  private void onEndValue() throws IOException {
    registerControllerFactories(getEndTimeDistribution());
    UIUtil.loadModalWindow(
        String.format(
            "distributions/%sTimeDistributionProperties", getEndTimeDistribution().getName()),
        (Stage) rootSplitPane.getScene().getWindow(),
        nodeChoiceBox.getValue().getName());
  }

  private void updateResultsTabPane() {
    resultsTabPane.getTabs().clear();

    for (final var originalServiceCenterModel : model.networkModel().getServiceCenterModels()) {
      final var serviceCenterModelTabPane = new TabPane();
      final var serviceCenterModelTab =
          new Tab(originalServiceCenterModel.getName(), serviceCenterModelTabPane);

      for (final var performanceMeasureType : PerformanceMeasureType.values()) {
        final var performanceMeasureAnchorPane = new AnchorPane();
        final var performanceMeasureTab =
            new Tab(performanceMeasureType.getName(), performanceMeasureAnchorPane);
        final var xAxis = new CategoryAxis();
        final var yAxis = new NumberAxis();
        final var lineChart = new LineChart<>(xAxis, yAxis);
        final var series = new XYChart.Series<String, Number>();

        var i = 0;
        for (final var performanceMeasuresProvider : performanceMeasuresProviders) {
          final var performanceMeasureCollection = performanceMeasuresProvider.getValue();
          final var serviceCenterModel =
              performanceMeasuresProvider
                  .getNetworkModel()
                  .getServiceCenterModel(originalServiceCenterModel.getName());

          if (performanceMeasureCollection != null) {
            if (performanceMeasureCollection.hasPerformanceMeasure(
                serviceCenterModel, performanceMeasureType)) {
              final var performanceMeasure =
                  performanceMeasureCollection.getPerformanceMeasure(
                      serviceCenterModel, performanceMeasureType);
              final var dataPoint =
                  new XYChart.Data<String, Number>(
                      String.valueOf(variableValues.get(i)), performanceMeasure.center());

              dataPoint.setExtraValue(performanceMeasure);
              series.getData().add(dataPoint);
            }
          }

          i++;
        }

        configureLineChart(performanceMeasureType, lineChart);
        lineChart.getData().add(series);

        performanceMeasureAnchorPane.getChildren().add(lineChart);
        AnchorPane.setTopAnchor(lineChart, 5.0);
        AnchorPane.setBottomAnchor(lineChart, 5.0);
        AnchorPane.setLeftAnchor(lineChart, 5.0);
        AnchorPane.setRightAnchor(lineChart, 5.0);

        serviceCenterModelTabPane.getTabs().add(performanceMeasureTab);

        series
            .getNode()
            .setStyle(
                "-fx-stroke: %s;"
                    .formatted(UIUtil.toRGB(originalServiceCenterModel.getStrokeColor())));
        for (final var data : series.getData()) {
          final var node = data.getNode();

          node.setStyle(
              "-fx-background-color: %s;"
                  .formatted(UIUtil.toRGB(originalServiceCenterModel.getStrokeColor().brighter())));

          final var performanceMeasure = (PerformanceMeasure) data.getExtraValue();

          if (performanceMeasure != null) {
            UIUtil.installTooltip(node, performanceMeasure.toString());
          } else {
            UIUtil.installTooltip(node, data.getYValue().doubleValue());
          }
        }
      }

      serviceCenterModelTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
      serviceCenterModelTabPane.setSide(Side.TOP);

      resultsTabPane.getTabs().add(serviceCenterModelTab);
    }

    addNetworkModelTab(resultsTabPane);
  }

  private void addNetworkModelTab(final TabPane resultsTabPane) {
    final var networkModelTabPane = new TabPane();
    final var networkModelTab =
        new Tab(resources.getString("performanceMeasures.system"), networkModelTabPane);

    for (final var performanceMeasureType : PerformanceMeasureType.values()) {
      final var performanceMeasureAnchorPane = new AnchorPane();
      final var performanceMeasureTab =
          new Tab(performanceMeasureType.getName(), performanceMeasureAnchorPane);
      final var xAxis = new CategoryAxis();
      final var yAxis = new NumberAxis();
      final var lineChart = new LineChart<>(xAxis, yAxis);
      final var series = new XYChart.Series<String, Number>();

      var i = 0;
      var skip = false;
      for (final var performanceMeasuresProvider : performanceMeasuresProviders) {
        final var performanceMeasureCollection = performanceMeasuresProvider.getValue();

        if (performanceMeasureCollection != null) {
          if (performanceMeasureCollection.hasSystemPerformanceMeasure(performanceMeasureType)) {
            final var performanceMeasure =
                performanceMeasureCollection.getSystemPerformanceMeasure(performanceMeasureType);
            final var dataPoint =
                new XYChart.Data<String, Number>(
                    String.valueOf(variableValues.get(i)), performanceMeasure.center());

            dataPoint.setExtraValue(performanceMeasure);
            series.getData().add(dataPoint);
          } else {
            skip = true;
            break;
          }
        }

        i++;
      }

      if (skip) {
        continue;
      }

      configureLineChart(performanceMeasureType, lineChart);
      lineChart.getData().add(series);

      performanceMeasureAnchorPane.getChildren().add(lineChart);
      AnchorPane.setTopAnchor(lineChart, 5.0);
      AnchorPane.setBottomAnchor(lineChart, 5.0);
      AnchorPane.setLeftAnchor(lineChart, 5.0);
      AnchorPane.setRightAnchor(lineChart, 5.0);

      networkModelTabPane.getTabs().add(performanceMeasureTab);

      series
          .getNode()
          .setStyle("-fx-stroke: %s;".formatted(UIUtil.toRGB(NetworkModel.DEFAULT_COLOR)));
      for (final var data : series.getData()) {
        final var node = data.getNode();

        node.setStyle(
            "-fx-background-color: %s;"
                .formatted(UIUtil.toRGB(NetworkModel.DEFAULT_COLOR.darker())));

        final var performanceMeasure = (PerformanceMeasure) data.getExtraValue();
        if (performanceMeasure != null) {
          UIUtil.installTooltip(node, performanceMeasure.toString());
        } else {
          UIUtil.installTooltip(node, data.getYValue().doubleValue());
        }
      }
    }

    networkModelTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    networkModelTabPane.setSide(Side.TOP);

    resultsTabPane.getTabs().add(networkModelTab);
  }

  private TimeDistributionModel getStartTimeDistribution() {
    return startTimeDistributionProperty.get();
  }

  private TimeDistributionModel getEndTimeDistribution() {
    return endTimeDistributionProperty.get();
  }

  private void configureLineChart(
      final PerformanceMeasureType performanceMeasureType,
      final LineChart<String, Number> lineChart) {
    final var xAxis = (CategoryAxis) lineChart.getXAxis();
    final var yAxis = (NumberAxis) lineChart.getYAxis();

    if (systemRadioButton.isSelected()) {
      xAxis.setLabel(
          variableTypeChoiceBox.getSelectionModel().getSelectedItem().getNumberAxisLabel());
    } else {
      xAxis.setLabel(
          variableTypeChoiceBox
              .getSelectionModel()
              .getSelectedItem()
              .getNumberAxisLabel()
              .formatted(nodeChoiceBox.getSelectionModel().getSelectedItem().getName()));
    }
    yAxis.setLabel(performanceMeasureType.getUnit());
    yAxis.setTickLabelFormatter(new LocaleAwareNumberAxisFormatter(yAxis));

    lineChart.setLegendVisible(false);
  }

  private void registerControllerFactories(final TimeDistributionModel timeDistributionModel) {
    ControllerInjector.registerControllerFactory(
        TimeDistributionDetailsController.class,
        () -> new TimeDistributionDetailsController(timeDistributionModel));
    ControllerInjector.registerControllerFactory(
        ExponentialTimeDistributionPropertiesController.class,
        () ->
            new ExponentialTimeDistributionPropertiesController(
                (ExponentialTimeDistributionModel) timeDistributionModel));
    ControllerInjector.registerControllerFactory(
        UniformTimeDistributionPropertiesController.class,
        () ->
            new UniformTimeDistributionPropertiesController(
                (UniformTimeDistributionModel) timeDistributionModel));
    ControllerInjector.registerControllerFactory(
        ErlangTimeDistributionPropertiesController.class,
        () ->
            new ErlangTimeDistributionPropertiesController(
                (ErlangTimeDistributionModel) timeDistributionModel));
    ControllerInjector.registerControllerFactory(
        DeterministicTimeDistributionPropertiesController.class,
        () ->
            new DeterministicTimeDistributionPropertiesController(
                (DeterministicTimeDistributionModel) timeDistributionModel));
    ControllerInjector.registerControllerFactory(
        ParetoTimeDistributionPropertiesController.class,
        () ->
            new ParetoTimeDistributionPropertiesController(
                (ParetoTimeDistributionModel) timeDistributionModel));
    ControllerInjector.registerControllerFactory(
        HyperexponentialTimeDistributionPropertiesController.class,
        () ->
            new HyperexponentialTimeDistributionPropertiesController(
                (HyperexponentialTimeDistributionModel) timeDistributionModel));
  }
}
