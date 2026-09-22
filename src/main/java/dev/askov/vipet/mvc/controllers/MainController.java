package dev.askov.vipet.mvc.controllers;

import static dev.askov.vipet.mvc.models.WhatIfModel.PerformanceMeasuresProviderType.ANALYZER;
import static dev.askov.vipet.mvc.models.WhatIfModel.PerformanceMeasuresProviderType.SIMULATOR;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.analyzer.Analyzer;
import dev.askov.vipet.core.analyzer.GordonNewellAnalyzer;
import dev.askov.vipet.core.analyzer.JacksonNetworkAnalyzer;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasuresProvider;
import dev.askov.vipet.core.simulator.Simulator;
import dev.askov.vipet.core.simulator.discrete.DiscreteEventDrivenSimulator;
import dev.askov.vipet.localization.LocalizationManager;
import dev.askov.vipet.mvc.ControllerInjector;
import dev.askov.vipet.mvc.common.SelectedNetworkElementModelChangeListener;
import dev.askov.vipet.mvc.controllers.analysis.GordonNewellAnalysisController;
import dev.askov.vipet.mvc.controllers.analysis.JacksonNetworkAnalysisController;
import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import dev.askov.vipet.mvc.controllers.networkeditor.states.*;
import dev.askov.vipet.mvc.models.WhatIfModel;
import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.serialization.NetworkExporter;
import dev.askov.vipet.serialization.NetworkImporter;
import dev.askov.vipet.serialization.json.JSONNetworkExporter;
import dev.askov.vipet.serialization.json.JSONNetworkImporter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MainController extends AbstractController<NetworkCollectionModel> {

  private enum SimulatorType {
    GUIDED,
    AUTOMATIC
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

  private final ObjectProperty<NetworkEditorController> currentNetworkEditorControllerProperty =
      new SimpleObjectProperty<>(this, "currentNetworkEditorController");

  private final List<NetworkEditorController> networkEditorControllers = new ArrayList<>();

  private final List<File> recentFiles = new ArrayList<>();
  private final List<DiscreteEventDrivenSimulator> simulators = new ArrayList<>();
  private final List<Analyzer<?>> analyzers = new ArrayList<>();

  private final ObjectProperty<Analyzer<?>> currentAnalyzerProperty =
      new SimpleObjectProperty<>(this, "currentAnalyzer");
  private final ObjectProperty<DiscreteEventDrivenSimulator> currentSimulatorProperty =
      new SimpleObjectProperty<>(this, "currentSimulator");
  private final ObjectProperty<SimulatorType> currentSimulatorTypeProperty =
      new SimpleObjectProperty<>(this, "currentSimulatorType", SimulatorType.AUTOMATIC);

  @FXML private VBox rootVBox;
  @FXML private Menu fileMenu;
  @FXML private MenuItem newMenuItem;
  @FXML private MenuItem openMenuItem;
  @FXML private Menu recentNetworksMenu;
  @FXML private MenuItem saveMenuItem;
  @FXML private MenuItem saveAsMenuItem;
  @FXML private MenuItem revertToSavedMenuItem;
  @FXML private MenuItem saveScreenshotAsMenuItem;
  @FXML private MenuItem exitMenuItem;
  @FXML private Menu editMenu;
  @FXML private MenuItem networkMenuItem;
  @FXML private Menu selectedNetworkElementMenu;
  @FXML private Menu settingsMenu;
  @FXML private Menu languageMenu;
  @FXML private Menu helpMenu;
  @FXML private MenuItem aboutMenuItem;
  @FXML private ToggleButton newSourceToggleButton;
  @FXML private ToggleButton newServiceCenterToggleButton;
  @FXML private ToggleButton newSinkToggleButton;
  @FXML private ToggleButton newConnectionToggleButton;
  @FXML private Label networkLabel;
  @FXML private ToolBar toolBar;
  @FXML private Button simulatorParametersButton;
  @FXML private SplitMenuButton startSimulatorSplitMenuButton;
  @FXML private MenuItem automaticSimulatorMenuItem;
  @FXML private MenuItem guidedSimulatorMenuItem;
  @FXML private FontIcon startSimulatorFontIcon;
  @FXML private FontIcon startAutomaticSimulatorFontIcon;
  @FXML private FontIcon startGuidedSimulatorFontIcon;
  @FXML private Button exportSamplesButton;
  @FXML private Button simulationResultsButton;
  @FXML private Button whatIfSimulationButton;
  @FXML private Label simulationLabel;
  @FXML private Button analyzeButton;
  @FXML private Button analysisResultsButton;
  @FXML private Button whatIfAnalysisButton;
  @FXML private Label analysisLabel;
  @FXML private TabPane networkEditorTabPane;

  private ChangeListener<NetworkElementModel> selectedNetworkElementModelChangeListener;

  private enum PerformanceMeasuresProviderType {
    SIMULATOR,
    ANALYZER
  }

  private final class PerformanceMeasuresControllerFactory
      implements Callable<PerformanceMeasuresController> {

    private PerformanceMeasuresProviderType performanceMeasuresProviderType;

    public PerformanceMeasuresControllerFactory(
        final PerformanceMeasuresProviderType performanceMeasuresProviderType) {
      this.performanceMeasuresProviderType = performanceMeasuresProviderType;
    }

    public PerformanceMeasuresControllerFactory() {
      this(null);
    }

    public void setPerformanceMeasuresProviderType(
        final PerformanceMeasuresProviderType performanceMeasuresProviderType) {
      this.performanceMeasuresProviderType = performanceMeasuresProviderType;
    }

    @Override
    public PerformanceMeasuresController call() {
      final PerformanceMeasuresProvider performanceMeasuresProvider;
      switch (performanceMeasuresProviderType) {
        case SIMULATOR:
          performanceMeasuresProvider = getCurrentSimulator();
          break;
        case ANALYZER:
          performanceMeasuresProvider = getCurrentAnalyzer();
          break;
        default:
          throw new RuntimeException("Unknown performance measures provider type");
      }

      if (performanceMeasuresProvider == null) {
        throw new RuntimeException("Performance measures provider is null");
      }

      return new PerformanceMeasuresController(performanceMeasuresProvider);
    }
  }

  private final class WhatIfControllerFactory implements Callable<WhatIfController> {

    private PerformanceMeasuresProviderType performanceMeasuresProviderType;

    public WhatIfControllerFactory(
        final PerformanceMeasuresProviderType performanceMeasuresProviderType) {
      this.performanceMeasuresProviderType = performanceMeasuresProviderType;
    }

    public WhatIfControllerFactory() {
      this(null);
    }

    public void setProviderType(
        final PerformanceMeasuresProviderType performanceMeasuresProviderType) {
      this.performanceMeasuresProviderType = performanceMeasuresProviderType;
    }

    @Override
    public WhatIfController call() {
      switch (performanceMeasuresProviderType) {
        case SIMULATOR -> {
          return new WhatIfController(new WhatIfModel(model.getCurrentNetwork(), SIMULATOR));
        }
        case ANALYZER -> {
          return new WhatIfController(new WhatIfModel(model.getCurrentNetwork(), ANALYZER));
        }
      }

      LOGGER.warn("Unknown performance measures provider type");
      return null;
    }
  }

  private final PerformanceMeasuresControllerFactory performanceMeasuresControllerFactory =
      new PerformanceMeasuresControllerFactory();
  private final WhatIfControllerFactory whatIfControllerFactory = new WhatIfControllerFactory();

  public MainController(final NetworkCollectionModel networks) {
    super(networks);

    ControllerInjector.registerControllerFactory(
        NetworkPropertiesController.class,
        () -> new NetworkPropertiesController(model.getCurrentNetwork()));
    ControllerInjector.registerControllerFactory(
        NetworkNodePropertiesController.class,
        () -> {
          final var selectedNetworkElementModel =
              getCurrentNetworkEditorController().getModel().getSelectedNetworkElementModel();

          if (selectedNetworkElementModel instanceof NetworkNodeModel selectedNetworkNode) {
            return new NetworkNodePropertiesController(
                selectedNetworkNode, model.getCurrentNetwork());
          } else {
            throw new RuntimeException("Selected network element is not a NetworkNode");
          }
        });
    ControllerInjector.registerControllerFactory(
        SimulatorParametersController.class,
        () -> new SimulatorParametersController(getCurrentSimulator()));
    ControllerInjector.registerControllerFactory(
        PerformanceMeasuresController.class, performanceMeasuresControllerFactory);
    ControllerInjector.registerControllerFactory(WhatIfController.class, whatIfControllerFactory);
    ControllerInjector.registerControllerFactory(WhatIfController.class, whatIfControllerFactory);
    ControllerInjector.registerControllerFactory(
        JacksonNetworkAnalysisController.class,
        () -> {
          final var currentAnalyzer = getCurrentAnalyzer();

          if (currentAnalyzer instanceof JacksonNetworkAnalyzer jacksonNetworkAnalyzer) {
            return new JacksonNetworkAnalysisController(
                jacksonNetworkAnalyzer.getAnalysisData(), model.getCurrentNetwork());
          } else {
            throw new RuntimeException("Current analyzer is not an JacksonNetworkAnalyzer");
          }
        });
    ControllerInjector.registerControllerFactory(
        GordonNewellAnalysisController.class,
        () -> {
          final var currentAnalyzer = getCurrentAnalyzer();

          if (currentAnalyzer instanceof GordonNewellAnalyzer gordonNewellAnalyzer) {
            return new GordonNewellAnalysisController(
                gordonNewellAnalyzer.getAnalysisData(), model.getCurrentNetwork());
          } else {
            throw new RuntimeException("Current analyzer is not a gordonNewellAnalyzer");
          }
        });
  }

  public String getNextDefaultNetworkName() {
    var i = 1;
    String name;
    boolean isTaken;

    do {
      final var potentialName =
          String.format(localizationManager.getString("main.defaultNetworkName"), i++);

      isTaken =
          networkEditorControllers.stream()
              .anyMatch(controller -> controller.getModel().getName().equals(potentialName));
      name = potentialName;
    } while (isTaken);

    return name;
  }

  private NetworkEditorController getCurrentNetworkEditorController() {
    return currentNetworkEditorControllerProperty.get();
  }

  private DiscreteEventDrivenSimulator getCurrentSimulator() {
    return simulators.get(model.getCurrentNetworkIndex());
  }

  private SimulatorType getCurrentSimulatorType() {
    return currentSimulatorTypeProperty.get();
  }

  private Analyzer<?> getCurrentAnalyzer() {
    return analyzers.get(model.getCurrentNetworkIndex());
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    fileMenu.textProperty().bind(localizationManager.getStringBinding("main.menuBar.file"));
    newMenuItem.textProperty().bind(localizationManager.getStringBinding("main.menuBar.file.new"));
    openMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.file.open"));
    recentNetworksMenu
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.file.recentNetworks"));
    recentNetworksMenu.disableProperty().bind(Bindings.isEmpty(recentNetworksMenu.getItems()));
    saveMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.file.save"));
    saveAsMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.file.saveAs"));
    revertToSavedMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.file.revertToSaved"));
    saveScreenshotAsMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.file.saveScreenshotAs"));
    exitMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.file.exit"));

    editMenu.textProperty().bind(localizationManager.getStringBinding("main.menuBar.edit"));
    networkMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.edit.network"));
    selectedNetworkElementMenu
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.edit.selectedNetworkElement"));

    settingsMenu.textProperty().bind(localizationManager.getStringBinding("main.menuBar.settings"));
    languageMenu
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.settings.language"));
    final var toggleGroup = new ToggleGroup();
    for (final var locale : LocalizationManager.SUPPORTED_LOCALES) {
      final var radioMenuItem = new RadioMenuItem();

      radioMenuItem.setText(localizationManager.getString("locale." + locale));
      radioMenuItem.setToggleGroup(toggleGroup);
      radioMenuItem.setSelected(locale.equals(LocalizationManager.getInstance().getLocale()));
      radioMenuItem.setOnAction(event -> LocalizationManager.getInstance().setLocale(locale));

      languageMenu.getItems().add(radioMenuItem);
    }

    helpMenu.textProperty().bind(localizationManager.getStringBinding("main.menuBar.help"));
    aboutMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.menuBar.help.about"));

    model
        .currentNetworkIndexProperty()
        .bind(networkEditorTabPane.getSelectionModel().selectedIndexProperty());
    model
        .currentNetworkIndexProperty()
        .addListener(
            (currentNetworkIndex, oldIndex, newIndex) ->
                LOGGER.debug("Current network index changed from {} to {}", oldIndex, newIndex));
    networkEditorTabPane
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (selectedIndex, oldSelectedIndex, newSelectedIndex) -> {
              final var newSelectedIndexValue = newSelectedIndex.intValue();

              if (newSelectedIndexValue >= 0) {
                final var networkEditorController =
                    networkEditorControllers.get(newSelectedIndexValue);

                currentNetworkEditorControllerProperty.set(networkEditorController);
                currentAnalyzerProperty.set(
                    newSelectedIndexValue < analyzers.size()
                        ? analyzers.get(newSelectedIndexValue)
                        : null);
                currentSimulatorProperty.set(
                    newSelectedIndexValue < simulators.size()
                        ? simulators.get(newSelectedIndexValue)
                        : null);

                networkEditorController.setState(new Idle());
              }
            });
    model
        .networksProperty()
        .addListener(
            (ListChangeListener<NetworkModel>)
                change -> {
                  while (change.next()) {
                    if (change.wasReplaced()) {
                      final var replacedIndex = change.getFrom();
                      final var newNetworkModel = change.getAddedSubList().getFirst();

                      try {
                        replaceTabAtIndex(replacedIndex, newNetworkModel);
                      } catch (IOException e) {
                        LOGGER.error("Failed to replace tab at index {}", replacedIndex);
                      }

                      networkEditorControllers.get(replacedIndex).setState(new Idle());
                      simulators.get(replacedIndex).setNetwork(newNetworkModel);
                      analyzers.get(replacedIndex).setNetwork(newNetworkModel);
                    } else if (change.wasAdded()) {
                      for (final var networkModel : change.getAddedSubList()) {
                        try {
                          LOGGER.debug("Adding network {}", networkModel.getName());

                          addNewTab(networkModel);
                          addToRecentNetworks(networkModel);

                          getCurrentNetworkEditorController().setState(new Idle());

                          final var simulator = new DiscreteEventDrivenSimulator(networkModel);

                          simulators.add(simulator);
                          simulator
                              .guidedSimulationProperty()
                              .bind(currentSimulatorTypeProperty.isEqualTo(SimulatorType.GUIDED));
                          currentSimulatorProperty.set(
                              simulators.get(model.getCurrentNetworkIndex()));

                          analyzers.add(
                              networkModel.isOpen()
                                  ? new JacksonNetworkAnalyzer(networkModel, true)
                                  : new GordonNewellAnalyzer(networkModel, true));
                          currentAnalyzerProperty.set(
                              analyzers.get(model.getCurrentNetworkIndex()));
                        } catch (IOException e) {
                          throw new RuntimeException(e);
                        }
                      }
                    } else if (change.wasRemoved()) {
                      final var removedIndex = change.getFrom();

                      networkEditorControllers.remove(removedIndex);
                      simulators.remove(removedIndex);
                      analyzers.remove(removedIndex);
                      if (networkEditorTabPane.getTabs().size()
                          != model.getNumberOfNetworks()) { // I.e. the removal was not initiated
                        // through the UI
                        networkEditorTabPane.getTabs().remove(removedIndex);
                      }

                      if (removedIndex == 0 && networkEditorControllers.size() == 1) {
                        final var networkEditorController =
                            networkEditorControllers.get(removedIndex);
                        if (networkEditorController != null) {
                          currentNetworkEditorControllerProperty.set(networkEditorController);
                        }
                      }
                    }
                  }
                });

    final var networkButtonsToggleGroup = new ToggleGroup();

    newSourceToggleButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.network.newSource"));
    newSourceToggleButton.setOnAction(
        event -> {
          final var networkEditorController = getCurrentNetworkEditorController();

          if (networkEditorController.getState() instanceof AddingSource) {
            networkEditorController.setState(new Idle());
          } else {
            networkEditorController.setState(new AddingSource());
          }
        });
    newSourceToggleButton.disableProperty().bind(model.currentNetworkProperty().isNull());
    newSourceToggleButton.setToggleGroup(networkButtonsToggleGroup);

    newServiceCenterToggleButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.network.newServiceCenter"));
    newServiceCenterToggleButton.setOnAction(
        event -> {
          final var networkEditorController = getCurrentNetworkEditorController();

          if (networkEditorController.getState() instanceof AddingServiceCenter) {
            networkEditorController.setState(new Idle());
          } else {
            networkEditorController.setState(new AddingServiceCenter());
          }
        });
    newServiceCenterToggleButton.disableProperty().bind(model.currentNetworkProperty().isNull());
    newServiceCenterToggleButton.setToggleGroup(networkButtonsToggleGroup);

    newSinkToggleButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.network.newSink"));
    newSinkToggleButton.setOnAction(
        event -> {
          final var networkEditorController = getCurrentNetworkEditorController();

          if (networkEditorController.getState() instanceof AddingSink) {
            networkEditorController.setState(new Idle());
          } else {
            networkEditorController.setState(new AddingSink());
          }
        });
    newSinkToggleButton.disableProperty().bind(model.currentNetworkProperty().isNull());
    newSinkToggleButton.setToggleGroup(networkButtonsToggleGroup);

    newConnectionToggleButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.network.newConnection"));
    newConnectionToggleButton.setOnAction(
        event -> {
          final var networkEditorController = getCurrentNetworkEditorController();

          if (networkEditorController.getState() instanceof AddingConnection) {
            networkEditorController.setState(new Idle());
          } else {
            networkEditorController.setState(new AddingConnection());
          }
        });
    newConnectionToggleButton.disableProperty().bind(model.currentNetworkProperty().isNull());
    newConnectionToggleButton.setToggleGroup(networkButtonsToggleGroup);

    networkLabel.textProperty().bind(localizationManager.getStringBinding("main.toolBar.network"));
    networkLabel
        .disableProperty()
        .bind(
            newSourceToggleButton
                .disableProperty()
                .and(newServiceCenterToggleButton.disableProperty())
                .and(newSinkToggleButton.disableProperty())
                .and(newConnectionToggleButton.disableProperty()));

    simulationLabel
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.simulation"));
    simulationLabel
        .disableProperty()
        .bind(
            simulatorParametersButton
                .disableProperty()
                .and(startSimulatorSplitMenuButton.disableProperty())
                .and(simulationResultsButton.disableProperty()));
    simulatorParametersButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.simulation.parameters"));
    startSimulatorFontIcon
        .iconCodeProperty()
        .bind(
            currentSimulatorTypeProperty.map(
                (type) ->
                    switch (type) {
                      case AUTOMATIC -> startAutomaticSimulatorFontIcon.getIconCode();
                      case GUIDED -> startGuidedSimulatorFontIcon.getIconCode();
                    }));
    startSimulatorSplitMenuButton
        .getTooltip()
        .textProperty()
        .bind(
            Bindings.when(currentSimulatorTypeProperty.isEqualTo(SimulatorType.AUTOMATIC))
                .then(
                    localizationManager.getStringBinding(
                        "main.toolBar.simulation.start.automatic.tooltip"))
                .otherwise(
                    localizationManager.getStringBinding(
                        "main.toolBar.simulation.start.guided.tooltip")));
    automaticSimulatorMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.simulation.start.automatic"));
    guidedSimulatorMenuItem
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.simulation.start.guided"));
    exportSamplesButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.simulation.exportSamples"));
    simulationResultsButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.simulation.results"));
    whatIfSimulationButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.simulation.whatIf"));

    analysisLabel
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.analysis"));
    analysisLabel
        .disableProperty()
        .bind(
            analyzeButton
                .disableProperty()
                .and(analysisResultsButton.disableProperty())
                .and(whatIfAnalysisButton.disableProperty()));
    analyzeButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.analysis.start"));
    analysisResultsButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.analysis.results"));
    whatIfAnalysisButton
        .textProperty()
        .bind(localizationManager.getStringBinding("main.toolBar.analysis.whatIf"));

    toolBar.disableProperty().bind(model.currentNetworkProperty().isNull());

    currentNetworkEditorControllerProperty.addListener(
        (networkEditorController, oldNetworkEditorController, newNetworkEditorController) -> {
          if (oldNetworkEditorController != null) {
            oldNetworkEditorController
                .getModel()
                .selectedNetworkElementModelProperty()
                .removeListener(selectedNetworkElementModelChangeListener);
          }
          if (newNetworkEditorController != null) {
            newNetworkEditorController
                .stateProperty()
                .addListener(
                    (state, oldState, newState) -> {
                      if (newState instanceof Idle) {
                        networkButtonsToggleGroup.selectToggle(null);
                      }
                    });
            rebindControls(newNetworkEditorController);

            selectedNetworkElementModelChangeListener =
                new SelectedNetworkElementModelChangeListener(
                    newNetworkEditorController, selectedNetworkElementMenu.getItems());
            newNetworkEditorController
                .getModel()
                .selectedNetworkElementModelProperty()
                .addListener(selectedNetworkElementModelChangeListener);
          }
        });
    model
        .currentNetworkProperty()
        .addListener(
            (currentNetwork, oldNetwork, newNetwork) -> {
              if (newNetwork != null) {
                saveMenuItem.disableProperty().unbind();
                saveMenuItem.disableProperty().bind(newNetwork.fileProperty().isNull());

                revertToSavedMenuItem.disableProperty().unbind();
                revertToSavedMenuItem.disableProperty().bind(newNetwork.fileProperty().isNull());
              }
            });

    currentSimulatorProperty.addListener(
        (simulator, oldSimulator, newSimulator) -> {
          if (newSimulator != null) {
            newSimulator
                .guidedSimulationModelProperty()
                .addListener(
                    (guidedSimulationModel, oldGuidedSimulationModel, newGuidedSimulationModel) -> {
                      newGuidedSimulationModel
                          .animatedConnection()
                          .addListener(
                              (connectionModel, oldConnectionModel, newConnectionModel) -> {
                                if (newConnectionModel == null) {
                                  return;
                                }

                                final var correspondingConnectionModel =
                                    getCurrentNetworkEditorController()
                                        .getConnectionModels()
                                        .stream()
                                        .filter(
                                            currentConnectionModel ->
                                                currentConnectionModel
                                                        .getSource()
                                                        .getName()
                                                        .equals(
                                                            newConnectionModel
                                                                .getSource()
                                                                .getName())
                                                    && currentConnectionModel
                                                        .getDestination()
                                                        .getName()
                                                        .equals(
                                                            newConnectionModel
                                                                .getDestination()
                                                                .getName()))
                                        .findFirst()
                                        .orElse(null);

                                if (correspondingConnectionModel != null) {
                                  final var connection =
                                      getCurrentNetworkEditorController()
                                          .getConnection(correspondingConnectionModel);

                                  if (connection != null) {
                                    connection
                                        .createTravellingJobAnimation(
                                            correspondingConnectionModel.getSource().getFillColor(),
                                            correspondingConnectionModel
                                                .getSource()
                                                .getStrokeColor(),
                                            getCurrentSimulator().getGuidedSimulationModel())
                                        .play();
                                  }
                                }
                              });
                      updateNetworkNodeStates(NetworkElementModel.State.SIMULATING);
                      newGuidedSimulationModel
                          .getSimulation()
                          .addEventHandler(
                              WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                              event -> updateNetworkNodeStates(NetworkElementModel.State.NORMAL));
                      newGuidedSimulationModel
                          .getSimulation()
                          .addEventHandler(
                              WorkerStateEvent.WORKER_STATE_FAILED,
                              event -> updateNetworkNodeStates(NetworkElementModel.State.NORMAL));
                      newGuidedSimulationModel
                          .getSimulation()
                          .addEventHandler(
                              WorkerStateEvent.WORKER_STATE_CANCELLED,
                              event -> updateNetworkNodeStates(NetworkElementModel.State.NORMAL));
                    });
          }
        });
  }

  @FXML
  private void onMenuShowing() {
    final var currentController = getCurrentNetworkEditorController();

    if (currentController != null) {
      currentController.setState(new Idle());
    }
  }

  @FXML
  private void onNew() {
    model.add(new NetworkModel(getNextDefaultNetworkName()));
  }

  @FXML
  private void onOpen() {
    final var fileChooser = new FileChooser();
    fileChooser.setTitle(localizationManager.getString("main.dialogs.open.title"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

    final var file = fileChooser.showOpenDialog(VIPET.getPrimaryStage());

    if (file != null) {
      importNetwork(file);
    }
  }

  @FXML
  private void onSave() {
    final var file = model.getCurrentNetwork().getFile();
    Alert saveNetworkAlert;

    try (final var fileOutputStream = new FileOutputStream(file)) {
      final NetworkExporter networkExporter =
          new JSONNetworkExporter(fileOutputStream, model.getCurrentNetwork());

      if (networkExporter.export()) {
        saveNetworkAlert =
            UIUtil.createAlert(
                Alert.AlertType.INFORMATION,
                rootVBox.getScene().getWindow(),
                "main.dialogs.saveAs.title",
                "main.dialogs.saveAs.success.header");
        saveNetworkAlert.setContentText(
            localizationManager.getString("main.dialogs.saveAs.success.content", file.getName()));
      } else {
        saveNetworkAlert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                rootVBox.getScene().getWindow(),
                "main.dialogs.saveAs.title",
                "main.dialogs.saveAs.error.header");
        saveNetworkAlert.setContentText(
            localizationManager.getString("main.dialogs.saveAs.error.content", file.getName()));

        LOGGER.error(
            "Failed to export network {} to file {}",
            model.getCurrentNetwork().getName(),
            file.getPath());
      }
    } catch (IOException e) {
      saveNetworkAlert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              rootVBox.getScene().getWindow(),
              "main.dialogs.saveAs.title",
              "main.dialogs.saveAs.error.header");
      saveNetworkAlert.setContentText(
          localizationManager.getString(
              "main.dialogs.saveAs.error.content.writing", file.getName()));

      LOGGER.error("Failed to open file {} for writing", file.getPath());
    }

    saveNetworkAlert.showAndWait();
  }

  @FXML
  private void onSaveAs() {
    final var fileChooser = new FileChooser();
    fileChooser.setTitle(localizationManager.getString("main.dialogs.saveAs.title"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
    fileChooser.setInitialFileName(model.getCurrentNetwork().getName());

    final var file = fileChooser.showSaveDialog(VIPET.getPrimaryStage());

    if (file != null) {
      Alert saveNetworkAlert;

      try (final var fileOutputStream = new FileOutputStream(file)) {
        final NetworkExporter networkExporter =
            new JSONNetworkExporter(fileOutputStream, model.getCurrentNetwork());

        LOGGER.debug(
            "Exporting network {} (tab index {}) to file {}",
            model.getCurrentNetwork().getName(),
            networkEditorTabPane.getSelectionModel().getSelectedIndex(),
            file.getName());

        if (networkExporter.export()) {
          model.getCurrentNetwork().setFile(file);

          saveNetworkAlert =
              UIUtil.createAlert(
                  Alert.AlertType.INFORMATION,
                  rootVBox.getScene().getWindow(),
                  "main.dialogs.saveAs.title",
                  "main.dialogs.saveAs.success.header");
          saveNetworkAlert.setContentText(
              localizationManager.getString("main.dialogs.saveAs.success.content", file.getName()));
        } else {
          saveNetworkAlert =
              UIUtil.createAlert(
                  Alert.AlertType.ERROR,
                  rootVBox.getScene().getWindow(),
                  "main.dialogs.saveAs.title",
                  "main.dialogs.saveAs.error.header");
          saveNetworkAlert.setContentText(
              localizationManager.getString("main.dialogs.saveAs.error.content", file.getName()));

          LOGGER.error("Failed to export network to file {}", file.getName());
        }
      } catch (IOException e) {
        saveNetworkAlert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                rootVBox.getScene().getWindow(),
                "main.dialogs.saveAs.title",
                "main.dialogs.saveAs.error.header");
        saveNetworkAlert.setContentText(
            localizationManager.getString(
                "main.dialogs.saveAs.error.content.writing", file.getName()));

        LOGGER.error("Failed to open file {} for writing", file.getName());
      }

      saveNetworkAlert.showAndWait();
    }
  }

  @FXML
  private void onRevertToSaved() {
    final var confirmationAlert =
        UIUtil.createAlert(
            Alert.AlertType.CONFIRMATION,
            rootVBox.getScene().getWindow(),
            "main.dialogs.revertToSaved.title",
            "main.dialogs.revertToSaved.confirmation.header",
            "main.dialogs.revertToSaved.confirmation.content");
    final var result = confirmationAlert.showAndWait();

    if (result.isEmpty() || result.get().getButtonData() != ButtonType.OK.getButtonData()) {
      return;
    }

    Alert revertToSavedAlert;

    try (final InputStream inputStream = new FileInputStream(model.getCurrentNetwork().getFile())) {
      final NetworkImporter networkImporter = new JSONNetworkImporter(inputStream);
      if (networkImporter.importNetwork()) {
        final var currentNetworkIndex = model.getCurrentNetworkIndex();
        final var oldNetwork = networkImporter.getNetwork();

        oldNetwork.setFile(model.getCurrentNetwork().getFile());

        model.set(currentNetworkIndex, oldNetwork);

        revertToSavedAlert =
            UIUtil.createAlert(
                Alert.AlertType.INFORMATION,
                rootVBox.getScene().getWindow(),
                "main.dialogs.revertToSaved.title",
                "main.dialogs.revertToSaved.success.header",
                "main.dialogs.revertToSaved.success.content");
      } else {
        revertToSavedAlert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                rootVBox.getScene().getWindow(),
                "main.dialogs.revertToSaved.title",
                "main.dialogs.revertToSaved.error.header",
                "main.dialogs.revertToSaved.error.content");
      }
    } catch (IOException e) {
      revertToSavedAlert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              rootVBox.getScene().getWindow(),
              "main.dialogs.revertToSaved.title",
              "main.dialogs.revertToSaved.error.header",
              "main.dialogs.revertToSaved.error.content");
    }

    revertToSavedAlert.showAndWait();
  }

  @FXML
  private void onSaveScreenshotAs() {
    final var fileChooser = new FileChooser();
    fileChooser.setTitle(localizationManager.getString("main.dialogs.saveScreenshotAs.title"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));

    final var file = fileChooser.showSaveDialog(VIPET.getPrimaryStage());

    if (file != null) {
      final var snapshot = getCurrentNetworkEditorController().getWritableImage();
      Alert saveScreenshotAlert;

      try {
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);

        saveScreenshotAlert =
            UIUtil.createAlert(
                Alert.AlertType.INFORMATION,
                rootVBox.getScene().getWindow(),
                "main.dialogs.saveScreenshotAs.title",
                "main.dialogs.saveScreenshotAs.success.header");
        saveScreenshotAlert.setContentText(
            localizationManager.getString(
                "main.dialogs.saveScreenshotAs.success.content", file.getName()));
      } catch (IOException e) {
        saveScreenshotAlert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                rootVBox.getScene().getWindow(),
                "main.dialogs.saveScreenshotAs.title",
                "main.dialogs.saveScreenshotAs.error.header");
        saveScreenshotAlert.setContentText(
            localizationManager.getString(
                "main.dialogs.saveScreenshotAs.error.content", file.getName()));
      }

      saveScreenshotAlert.showAndWait();
    }
  }

  @FXML
  private void onExit() {
    final var exitAlert =
        UIUtil.createAlert(
            Alert.AlertType.CONFIRMATION,
            rootVBox.getScene().getWindow(),
            "main.dialogs.exit.title",
            "main.dialogs.exit.header",
            "main.dialogs.exit.content");
    final var result = exitAlert.showAndWait();

    if (result.isPresent() && result.get().getButtonData() == ButtonType.OK.getButtonData()) {
      Platform.exit();
    }
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    final var exitAlert =
        UIUtil.createAlert(
            Alert.AlertType.CONFIRMATION,
            rootVBox.getScene().getWindow(),
            "main.dialogs.exit.title",
            "main.dialogs.exit.header",
            "main.dialogs.exit.content");
    final var result = exitAlert.showAndWait();

    if (result.isEmpty() || result.get().getButtonData() != ButtonType.OK.getButtonData()) {
      event.consume();
    }
  }

  @FXML
  private void onEdit() throws IOException {
    UIUtil.loadModalWindow(
        "NetworkProperties", VIPET.getPrimaryStage(), model.getCurrentNetwork().getName());
  }

  @FXML
  private void onAbout() throws IOException {
    UIUtil.loadWindow("About", VIPET.getPrimaryStage(), true, true);
  }

  @FXML
  private void onExportSamples() throws IOException {
    final var directoryChooser = new DirectoryChooser();
    final var directory = directoryChooser.showDialog(VIPET.getPrimaryStage());

    ControllerInjector.registerControllerFactory(
        SampleExportController.class,
        () -> new SampleExportController(getCurrentSimulator(), directory));
    if (directory != null) {
      UIUtil.loadModalWindow("SampleExport", VIPET.getPrimaryStage());
    }
  }

  @FXML
  private void onNewSource() {
    getCurrentNetworkEditorController()
        .setState(newSourceToggleButton.isSelected() ? new AddingSource() : new Idle());
  }

  @FXML
  private void onNewServiceCenter() {
    getCurrentNetworkEditorController()
        .setState(
            newServiceCenterToggleButton.isSelected() ? new AddingServiceCenter() : new Idle());
  }

  @FXML
  private void onNewSink() {
    getCurrentNetworkEditorController()
        .setState(newSinkToggleButton.isSelected() ? new AddingSink() : new Idle());
  }

  @FXML
  private void onNewConnection() {
    getCurrentNetworkEditorController()
        .setState(newConnectionToggleButton.isSelected() ? new AddingConnection() : new Idle());
  }

  @FXML
  private void onSimulatorParameters() throws IOException {
    UIUtil.loadModalWindow(
        "SimulatorParameters", VIPET.getPrimaryStage(), model.getCurrentNetwork().getName());
  }

  @FXML
  private void onSimulatorStart() throws IOException {
    getCurrentNetworkEditorController().setState(new Idle());

    final var simulator = getCurrentSimulator();

    if (simulator.isRunning()) {
      simulator.cancel();

      return;
    } else if (simulator.getState() != Simulator.State.READY) {
      simulator.reset();
    }

    simulatorParametersButton
        .disableProperty()
        .bind(simulator.stateProperty().isEqualTo(Simulator.State.RUNNING));
    startSimulatorSplitMenuButton.disableProperty().unbind();
    startSimulatorSplitMenuButton
        .disableProperty()
        .bind(simulator.stateProperty().isEqualTo(Simulator.State.RUNNING));

    exportSamplesButton.disableProperty().bind(simulationResultsButton.disableProperty());
    simulationResultsButton
        .disableProperty()
        .bind(simulator.stateProperty().isNotEqualTo(Simulator.State.SUCCEEDED));

    simulator.setOnFailed(event -> onFailed(getCurrentNetworkEditorController()));
    simulator.setOnSucceeded(event -> visualizePerformanceMeasures(simulator));
    simulator.setOnCancelled(event -> simulatorCancelled());
    simulator.getNetworkModel().setModified(false);

    if (getCurrentSimulatorType() == SimulatorType.GUIDED) {
      ControllerInjector.registerControllerFactory(
          GuidedSimulatorController.class, () -> new GuidedSimulatorController(simulator));
      UIUtil.loadWindow(
          "GuidedSimulator",
          VIPET.getPrimaryStage(),
          true,
          true,
          model.getCurrentNetwork().getName());
    } else {
      ControllerInjector.registerControllerFactory(
          SimulatorController.class, () -> new SimulatorController(simulator));
      UIUtil.loadWindow(
          "Simulator", VIPET.getPrimaryStage(), false, true, model.getCurrentNetwork().getName());
    }
  }

  @FXML
  private void onGuidedSimulator() {
    currentSimulatorTypeProperty.set(SimulatorType.GUIDED);
  }

  @FXML
  private void onAutomaticSimulator() {
    currentSimulatorTypeProperty.set(SimulatorType.AUTOMATIC);
  }

  @FXML
  private void onSimulationResults() throws IOException {
    final Simulator simulator = getCurrentSimulator();

    if (simulator.getState() == Simulator.State.SUCCEEDED) {
      performanceMeasuresControllerFactory.setPerformanceMeasuresProviderType(
          PerformanceMeasuresProviderType.SIMULATOR);
      UIUtil.loadWindow(
          "PerformanceMeasures",
          VIPET.getPrimaryStage(),
          true,
          false,
          model.getCurrentNetwork().getName(),
          resources.getString("main.windowTitleParameter.simulation"));
    } else {
      final var resultsAlert =
          UIUtil.createAlert(
              Alert.AlertType.WARNING,
              rootVBox.getScene().getWindow(),
              "main.dialogs.simulationResults.title",
              "main.dialogs.simulationResults.header",
              "main.dialogs.simulationResults.content");

      resultsAlert.showAndWait();
    }
  }

  @FXML
  private void onWhatIfSimulation() throws IOException {
    whatIfControllerFactory.setProviderType(PerformanceMeasuresProviderType.SIMULATOR);
    UIUtil.loadWindow(
        "WhatIf",
        VIPET.getPrimaryStage(),
        true,
        true,
        model.getCurrentNetwork().getName(),
        localizationManager.getString("main.windowTitleParameter.simulation"));
  }

  @FXML
  private void onAnalyze() throws IOException {
    getCurrentNetworkEditorController().setState(new Idle());

    var analyzer = getCurrentAnalyzer();

    if (analyzer.isRunning()) {
      return;
    }

    if (model.getCurrentNetwork().isOpen() && analyzer instanceof GordonNewellAnalyzer) {
      final var jacksonNetworkAnalyzer =
          new JacksonNetworkAnalyzer(model.getCurrentNetwork(), true);

      final var index = analyzers.indexOf(analyzer);
      LOGGER.debug("Replacing GordonNewellAnalyzer at index {}", index);
      analyzers.set(index, jacksonNetworkAnalyzer);
      analyzer = jacksonNetworkAnalyzer;

      LOGGER.debug("Solving open network {}", model.getCurrentNetwork().getName());
    } else if (model.getCurrentNetwork().isClosed() && analyzer instanceof JacksonNetworkAnalyzer) {
      final var gordonNewellAnalyzer = new GordonNewellAnalyzer(model.getCurrentNetwork(), true);

      final var index = analyzers.indexOf(analyzer);
      LOGGER.debug("Replacing JacksonNetworkAnalyzer at index {}", index);
      analyzers.set(index, gordonNewellAnalyzer);
      analyzer = gordonNewellAnalyzer;

      LOGGER.debug("Solving closed network {}", model.getCurrentNetwork().getName());
    }

    currentAnalyzerProperty.set(analyzer);

    analysisResultsButton
        .disableProperty()
        .bind(analyzer.stateProperty().isNotEqualTo(Simulator.State.SUCCEEDED));

    final var finalAnalyzer = analyzer;

    finalAnalyzer.setOnFailed(event -> onFailed(getCurrentNetworkEditorController()));
    finalAnalyzer.setOnSucceeded(event -> visualizePerformanceMeasures(finalAnalyzer));

    finalAnalyzer.getNetworkModel().setModified(false);

    ControllerInjector.registerControllerFactory(
        AnalyzerController.class, () -> new AnalyzerController<>(finalAnalyzer));
    UIUtil.loadWindow(
        "Analysis", VIPET.getPrimaryStage(), true, true, model.getCurrentNetwork().getName());
  }

  @FXML
  private void onAnalysisResults() throws IOException {
    final var analyzer = getCurrentAnalyzer();

    if (analyzer.getState() == Simulator.State.SUCCEEDED) {
      performanceMeasuresControllerFactory.setPerformanceMeasuresProviderType(
          PerformanceMeasuresProviderType.ANALYZER);
      UIUtil.loadWindow(
          "PerformanceMeasures",
          VIPET.getPrimaryStage(),
          true,
          true,
          model.getCurrentNetwork().getName(),
          localizationManager.getString("main.windowTitleParameter.analysis"));
    } else {
      final var resultsAlert =
          UIUtil.createAlert(
              Alert.AlertType.WARNING,
              rootVBox.getScene().getWindow(),
              "main.dialogs.analysisResults.title",
              "main.dialogs.analysisResults.header",
              "main.dialogs.analysisResults.content");

      resultsAlert.showAndWait();
    }
  }

  @FXML
  private void onWhatIfAnalysis() throws IOException {
    whatIfControllerFactory.setProviderType(PerformanceMeasuresProviderType.ANALYZER);
    UIUtil.loadWindow(
        "WhatIf",
        VIPET.getPrimaryStage(),
        true,
        true,
        model.getCurrentNetwork().getName(),
        localizationManager.getString("main.windowTitleParameter.analysis"));
  }

  private void replaceTabAtIndex(final int index, final NetworkModel networkModel)
      throws IOException {
    ControllerInjector.registerControllerFactory(
        NetworkEditorController.class,
        () -> {
          final var networkEditorController = new NetworkEditorController(networkModel);

          networkEditorControllers.set(index, networkEditorController);

          return networkEditorController;
        });
    final var newTab = UIUtil.loadTabAtIndex("NetworkEditor", networkEditorTabPane, index);

    setupTab(networkModel, newTab);
    networkEditorTabPane.getSelectionModel().select(newTab);
  }

  private void addNewTab(final NetworkModel networkModel) throws IOException {
    ControllerInjector.registerControllerFactory(
        NetworkEditorController.class,
        () -> {
          final var networkEditorController = new NetworkEditorController(networkModel);

          networkEditorControllers.add(networkEditorController);

          return networkEditorController;
        });
    final var newTab = UIUtil.loadTab("NetworkEditor", networkEditorTabPane);

    setupTab(networkModel, newTab);
    networkEditorTabPane.getSelectionModel().select(newTab);
  }

  private void setupTab(final NetworkModel networkModel, final Tab newTab) {
    newTab.textProperty().bind(networkModel.nameProperty());
    newTab
        .disableProperty()
        .bind(
            Bindings.createBooleanBinding(
                () -> {
                  if (currentSimulatorProperty.get() == null
                      || !currentSimulatorProperty.get().isRunning()) {
                    return false;
                  } else {
                    return currentAnalyzerProperty.get() != null
                        && currentAnalyzerProperty.get().isRunning();
                  }
                },
                currentSimulatorProperty,
                currentAnalyzerProperty));

    final var tabTooltip = new Tooltip();

    tabTooltip.textProperty().bind(networkModel.descriptionProperty());
    newTab
        .tooltipProperty()
        .bind(
            Bindings.when(networkModel.descriptionProperty().isNotEmpty())
                .then(tabTooltip)
                .otherwise((Tooltip) null));

    newTab.setOnCloseRequest(
        event -> {
          if (!networkModel.isEmpty()) {
            final var closeNetworkAlert =
                UIUtil.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    rootVBox.getScene().getWindow(),
                    "main.dialogs.closeNetwork.title",
                    "main.dialogs.closeNetwork.header");

            closeNetworkAlert.setContentText(
                localizationManager.getString(
                    "main.dialogs.closeNetwork.content", networkModel.getName()));

            final var result = closeNetworkAlert.showAndWait().orElse(ButtonType.CANCEL);
            if (result.getButtonData() != ButtonType.OK.getButtonData()) {
              event.consume();
            }
          }
        });
    newTab.setOnClosed(event -> model.remove(networkModel));
  }

  private void importNetwork(final File file) {
    Alert openNetworkAlert;

    try (final var fileInputStream = new FileInputStream(file)) {
      final NetworkImporter networkImporter = new JSONNetworkImporter(fileInputStream);

      if (networkImporter.importNetwork()) {
        final var networkModel = networkImporter.getNetwork();

        networkModel.setFile(file);

        model.add(networkModel);

        openNetworkAlert =
            UIUtil.createAlert(
                Alert.AlertType.INFORMATION,
                rootVBox.getScene().getWindow(),
                "main.dialogs.open.title",
                "main.dialogs.open.success.header");
        openNetworkAlert.setContentText(
            localizationManager.getString(
                "main.dialogs.open.success.content", networkModel.getName(), file.getName()));
      } else {
        openNetworkAlert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                rootVBox.getScene().getWindow(),
                "main.dialogs.open.title",
                "main.dialogs.open.error.header");
        openNetworkAlert.setContentText(
            localizationManager.getString("main.dialogs.open.error.content", file.getName()));
      }
    } catch (final Exception e) {
      openNetworkAlert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              rootVBox.getScene().getWindow(),
              "main.dialogs.open.title",
              "main.dialogs.open.error.header");
      openNetworkAlert.setContentText(
          localizationManager.getString("main.dialogs.open.error.content.reading", file.getName()));
    }

    openNetworkAlert.showAndWait();
  }

  private void addToRecentNetworks(final NetworkModel networkModel) {
    if (networkModel.getFile() != null
        && recentFiles.stream()
            .noneMatch(
                recentFile ->
                    recentFile
                        .getAbsolutePath()
                        .equals(networkModel.getFile().getAbsolutePath()))) {
      recentFiles.add(networkModel.getFile());
      LOGGER.debug("Added file {} to recent files", networkModel.getFile().getName());

      final var label = new Label("%s".formatted(networkModel.getName()));
      final MenuItem recentMenuItem = new CustomMenuItem(label);

      Tooltip.install(label, new Tooltip(networkModel.getFile().getAbsolutePath()));
      recentMenuItem.setOnAction(event -> importNetwork(networkModel.getFile()));
      recentNetworksMenu.getItems().add(recentMenuItem);
    }
  }

  private void onFailed(final NetworkEditorController networkEditorController) {
    rebindControls(networkEditorController);
  }

  private void visualizePerformanceMeasures(
      final PerformanceMeasuresProvider performanceMeasuresProvider) {
    LOGGER.debug("Visualizing performance measures");

    rebindControls(getCurrentNetworkEditorController());

    var queueLengthSum = 0.0;
    for (final var serviceCenterModel :
        performanceMeasuresProvider.getNetworkModel().getServiceCenterModels()) {
      for (var i = 0; i < serviceCenterModel.getNumberOfServers(); i++) {
        serviceCenterModel
            .getServerModel(i)
            .setVisualizationParameter(
                performanceMeasuresProvider
                    .getValue()
                    .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.UTILIZATION)
                    .center());
      }
      queueLengthSum +=
          performanceMeasuresProvider
              .getValue()
              .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.QUEUE_LENGTH)
              .center();
    }
    for (final var serviceCenterModel :
        performanceMeasuresProvider.getNetworkModel().getServiceCenterModels()) {
      for (var i = 0; i < serviceCenterModel.getNumberOfServers(); i++) {
        serviceCenterModel
            .getQueueModel()
            .setVisualizationParameter(
                performanceMeasuresProvider
                        .getValue()
                        .getPerformanceMeasure(
                            serviceCenterModel, PerformanceMeasureType.QUEUE_LENGTH)
                        .center()
                    / queueLengthSum);
        serviceCenterModel.getQueueModel().setJobVisualizationParameter(0);
      }
    }
  }

  private void rebindControls(final NetworkEditorController networkEditorController) {
    final var networkModel = networkEditorController.getModel();
    final var index = networkEditorControllers.indexOf(networkEditorController);

    LOGGER.debug("Binding controls for network \"{}\"", networkModel.getName());

    simulatorParametersButton.disableProperty().unbind();
    simulatorParametersButton.disableProperty().bind(networkModel.validProperty().not());

    startSimulatorSplitMenuButton.disableProperty().unbind();
    startSimulatorSplitMenuButton.disableProperty().bind(networkModel.validProperty().not());

    if (index < simulators.size()) {
      final var simulator = simulators.get(model.getCurrentNetworkIndex());

      simulator
          .guidedSimulationProperty()
          .bind(currentSimulatorTypeProperty.isEqualTo(SimulatorType.GUIDED));
    }

    whatIfSimulationButton.disableProperty().unbind();
    whatIfSimulationButton.disableProperty().bind(networkModel.validProperty().not());

    analyzeButton.disableProperty().unbind();
    analyzeButton.disableProperty().bind(networkModel.solvableProperty().not());

    analysisResultsButton.disableProperty().unbind();
    if (index < analyzers.size()) {
      final var analyzer = analyzers.get(model.getCurrentNetworkIndex());

      analysisResultsButton
          .disableProperty()
          .bind(
              analyzer
                  .stateProperty()
                  .isNotEqualTo(Simulator.State.SUCCEEDED)
                  .or(networkModel.modifiedProperty()));
    } else {
      analysisResultsButton.setDisable(true);
    }

    whatIfAnalysisButton.disableProperty().unbind();
    whatIfAnalysisButton.disableProperty().bind(networkModel.solvableProperty().not());

    final var selectedNetworkElementProperty = networkModel.selectedNetworkElementModelProperty();

    selectedNetworkElementMenu.disableProperty().unbind();
    selectedNetworkElementMenu.disableProperty().bind(selectedNetworkElementProperty.isNull());
  }

  private void simulatorCancelled() {
    LOGGER.debug("Resetting buttons");

    simulatorParametersButton.disableProperty().unbind();
    startSimulatorSplitMenuButton.disableProperty().unbind();
    simulationResultsButton.disableProperty().unbind();
    exportSamplesButton.disableProperty().unbind();

    final var networkModel = getCurrentNetworkEditorController().getModel();
    for (final var serviceCenterModel : networkModel.getServiceCenterModels()) {
      serviceCenterModel.getQueueModel().setJobVisualizationParameter(0);
      serviceCenterModel.getQueueModel().setVisualizationParameter(0.0);
      serviceCenterModel
          .getServerModels()
          .forEach(serverModel -> serverModel.setVisualizationParameter(0.0));
      serviceCenterModel.setState(NetworkElementModel.State.NORMAL);
    }
  }

  private void updateNetworkNodeStates(final NetworkElementModel.State state) {
    for (final var serviceCenterModel : getModel().getCurrentNetwork().getServiceCenterModels()) {
      serviceCenterModel.setState(state);
    }
  }
}
