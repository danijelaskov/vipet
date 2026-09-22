package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.common.ValidatorUtil;
import dev.askov.vipet.mvc.ControllerInjector;
import dev.askov.vipet.mvc.common.IntegerSpinnerValueFactory;
import dev.askov.vipet.mvc.controllers.distributions.*;
import dev.askov.vipet.mvc.models.distributions.*;
import dev.askov.vipet.mvc.models.network.*;
import dev.askov.vipet.mvc.models.network.queue.QueueDisciplineModel;
import dev.askov.vipet.mvc.models.network.routing.ProbabilityRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RoutingStrategyModel;
import dev.askov.vipet.mvc.views.components.ProbabilityTableCell;
import java.io.IOException;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkNodePropertiesController extends AbstractController<NetworkNodeModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(NetworkNodePropertiesController.class);

  private final StringConverter<QueueDisciplineModel> queueDisciplineStringConverter =
      new StringConverter<>() {

        private static final String QUEUE_DISCIPLINE_KEY =
            "networkNodeProperties.queue.discipline.%s";

        @Override
        public String toString(final QueueDisciplineModel routingStrategy) {
          if (routingStrategy == null) {
            return null;
          }

          return resources.getString(
              String.format(QUEUE_DISCIPLINE_KEY, routingStrategy.getName()));
        }

        @Override
        public QueueDisciplineModel fromString(final String string) {
          return null;
        }
      };

  private final StringConverter<TimeDistributionModel> timeDistributionStringConverter =
      new StringConverter<>() {

        private static final String SERVICE_TIME_DISTRIBUTION_KEY_PREFIX =
            "networkNodeProperties.timeDistribution.%s";

        @Override
        public String toString(final TimeDistributionModel timeDistributionModel) {
          return resources.getString(
              String.format(
                  SERVICE_TIME_DISTRIBUTION_KEY_PREFIX,
                  timeDistributionModel.getName().toLowerCase()));
        }

        @Override
        public TimeDistributionModel fromString(final String string) {
          return null;
        }
      };

  private final StringConverter<RoutingStrategyModel> routingStrategyStringConverter =
      new StringConverter<>() {

        private static final String ROUTING_STRATEGY_KEY_PREFIX =
            "networkNodeProperties.routing.strategy.%s";

        @Override
        public String toString(final RoutingStrategyModel routingStrategyModel) {
          return resources.getString(
              String.format(ROUTING_STRATEGY_KEY_PREFIX, routingStrategyModel.getName()));
        }

        @Override
        public RoutingStrategyModel fromString(final String string) {
          return null;
        }
      };

  @FXML private VBox resourcePropertiesVBox;
  @FXML private Accordion resourcePropertiesAccordion;
  @FXML private TextField nameTextField;
  @FXML private ColorPicker fillColorPicker;
  @FXML private ColorPicker strokeColorPicker;
  @FXML private TextArea descriptionTextArea;
  @FXML private TitledPane queueTitledPane;
  @FXML private RadioButton infiniteRadioButton;
  @FXML private RadioButton finiteRadioButton;
  @FXML private Spinner<Integer> queueCapacitySpinner;
  @FXML private ChoiceBox<QueueDisciplineModel> queueDisciplineChoiceBox;
  @FXML private TitledPane serviceTitledPane;
  @FXML private Spinner<Integer> numberOfServersSpinner;
  @FXML private ChoiceBox<TimeDistributionModel> serviceTimeDistributionChoiceBox;
  @FXML private TitledPane jobGenerationTitledPane;
  @FXML private ChoiceBox<TimeDistributionModel> interArrivalTimeDistributionChoiceBox;
  @FXML private TitledPane routesTitledPane;
  @FXML private ChoiceBox<RoutingStrategyModel> routingStrategyChoiceBox;
  @FXML private VBox routesVBox;
  @FXML private TableView<Map.Entry<NetworkNodeModel, DoubleProperty>> routesTableView;

  @FXML
  private TableColumn<Map.Entry<NetworkNodeModel, DoubleProperty>, String>
      destinationNameTableColumn;

  @FXML
  private TableColumn<Map.Entry<NetworkNodeModel, DoubleProperty>, String> probabilityTableColumn;

  @FXML private CheckBox usePercentageProbabilityCheckBox;

  private final BooleanProperty nameIsValidProperty =
      new SimpleBooleanProperty(this, "nameIsValidProperty", true);

  private final NetworkModel networkModel;

  private boolean sumOfProbabilitiesIs1 = true;
  private boolean zeroProbability = false;

  public NetworkNodePropertiesController(
      final NetworkNodeModel networkNode, final NetworkModel networkModel) {
    super(networkNode);

    this.networkModel = networkModel;

    if (networkNode instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
      ControllerInjector.registerControllerFactory(
          ExponentialTimeDistributionPropertiesController.class,
          () ->
              new ExponentialTimeDistributionPropertiesController(
                  (ExponentialTimeDistributionModel)
                      nonTerminalNetworkNode.getSelectedTimeDistribution()));
      ControllerInjector.registerControllerFactory(
          UniformTimeDistributionPropertiesController.class,
          () ->
              new UniformTimeDistributionPropertiesController(
                  (UniformTimeDistributionModel)
                      nonTerminalNetworkNode.getSelectedTimeDistribution()));
      ControllerInjector.registerControllerFactory(
          ErlangTimeDistributionPropertiesController.class,
          () ->
              new ErlangTimeDistributionPropertiesController(
                  (ErlangTimeDistributionModel)
                      nonTerminalNetworkNode.getSelectedTimeDistribution()));
      ControllerInjector.registerControllerFactory(
          DeterministicTimeDistributionPropertiesController.class,
          () ->
              new DeterministicTimeDistributionPropertiesController(
                  (DeterministicTimeDistributionModel)
                      nonTerminalNetworkNode.getSelectedTimeDistribution()));
      ControllerInjector.registerControllerFactory(
          ParetoTimeDistributionPropertiesController.class,
          () ->
              new ParetoTimeDistributionPropertiesController(
                  (ParetoTimeDistributionModel)
                      nonTerminalNetworkNode.getSelectedTimeDistribution()));
      ControllerInjector.registerControllerFactory(
          HyperexponentialTimeDistributionPropertiesController.class,
          () ->
              new HyperexponentialTimeDistributionPropertiesController(
                  (HyperexponentialTimeDistributionModel)
                      nonTerminalNetworkNode.getSelectedTimeDistribution()));
    }
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Network node: {}", model.getName());

    resourcePropertiesAccordion.setExpandedPane(resourcePropertiesAccordion.getPanes().getFirst());
    resourcePropertiesAccordion
        .expandedPaneProperty()
        .addListener(
            (expandedPane, oldPane, newPane) -> {
              if (newPane == null) {
                resourcePropertiesAccordion.setExpandedPane(oldPane);
              } else if (oldPane != null) {
                oldPane.setExpanded(false);
                resourcePropertiesAccordion.setExpandedPane(newPane);
              }
            });

    nameTextField.setText(model.getName());
    nameIsValidProperty.bind(
        Bindings.createBooleanBinding(
            () ->
                ValidatorUtil.validateNameFormat(nameTextField.getText())
                    && ValidatorUtil.validateNameUniqueness(
                        nameTextField.getText(), networkModel, model),
            nameTextField.textProperty()));

    fillColorPicker.valueProperty().bindBidirectional(model.fillColorProperty());
    strokeColorPicker.valueProperty().bindBidirectional(model.strokeColorProperty());
    descriptionTextArea.textProperty().bindBidirectional(model.descriptionProperty());
    descriptionTextArea
        .promptTextProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    resources
                        .getString("networkNodeProperties.general.description.placeholderText")
                        .formatted(model.getName()),
                model.nameProperty()));

    if (model instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
      ControllerInjector.registerControllerFactory(
          TimeDistributionDetailsController.class,
          () ->
              new TimeDistributionDetailsController(
                  nonTerminalNetworkNode.getSelectedTimeDistribution()));

      if (nonTerminalNetworkNode instanceof ServiceCenterModel serviceCenterModel) {
        final var queueCapacityToggleGroup = new ToggleGroup();

        infiniteRadioButton.setToggleGroup(queueCapacityToggleGroup);
        finiteRadioButton.setToggleGroup(queueCapacityToggleGroup);

        infiniteRadioButton
            .selectedProperty()
            .bindBidirectional(serviceCenterModel.getQueueModel().infiniteCapacityProperty());
        finiteRadioButton.setSelected(!serviceCenterModel.getQueueModel().isInfinite());

        queueCapacitySpinner.setValueFactory(
            new IntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, serviceCenterModel.getQueueModel().getCapacity(), 1));
        serviceCenterModel
            .getQueueModel()
            .capacityProperty()
            .bind(queueCapacitySpinner.getValueFactory().valueProperty());
        queueCapacitySpinner
            .disableProperty()
            .bind(serviceCenterModel.getQueueModel().infiniteCapacityProperty());

        queueDisciplineChoiceBox.setItems(
            serviceCenterModel.getQueueModel().queueDisciplinesProperty());
        queueDisciplineChoiceBox.setConverter(queueDisciplineStringConverter);
        queueDisciplineChoiceBox
            .getSelectionModel()
            .select(serviceCenterModel.getQueueModel().getSelectedQueueDiscipline());
        serviceCenterModel
            .getQueueModel()
            .selectedQueueDisciplineProperty()
            .bind(queueDisciplineChoiceBox.getSelectionModel().selectedItemProperty());

        numberOfServersSpinner.setValueFactory(
            new IntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, serviceCenterModel.getNumberOfServers(), 1));
        serviceCenterModel
            .numberOfServersProperty()
            .bind(numberOfServersSpinner.getValueFactory().valueProperty());

        resourcePropertiesAccordion.getPanes().remove(jobGenerationTitledPane);
      } else {
        resourcePropertiesAccordion.getPanes().remove(queueTitledPane);
        resourcePropertiesAccordion.getPanes().remove(serviceTitledPane);
      }

      final var timeDistributionChoiceBox =
          nonTerminalNetworkNode instanceof SourceModel
              ? interArrivalTimeDistributionChoiceBox
              : serviceTimeDistributionChoiceBox;

      timeDistributionChoiceBox.setItems(nonTerminalNetworkNode.timeDistributionsProperty());
      timeDistributionChoiceBox.setConverter(timeDistributionStringConverter);
      timeDistributionChoiceBox
          .getSelectionModel()
          .select(nonTerminalNetworkNode.getSelectedTimeDistribution());
      nonTerminalNetworkNode
          .selectedTimeDistributionProperty()
          .bind(timeDistributionChoiceBox.getSelectionModel().selectedItemProperty());

      routesTitledPane
          .disableProperty()
          .bind(nonTerminalNetworkNode.connectionsProperty().emptyProperty());

      routingStrategyChoiceBox.setItems(nonTerminalNetworkNode.getRoutingStrategies());
      routingStrategyChoiceBox.setConverter(routingStrategyStringConverter);
      routingStrategyChoiceBox
          .getSelectionModel()
          .select(nonTerminalNetworkNode.getSelectedRoutingStrategy());
      nonTerminalNetworkNode
          .selectedRoutingStrategyProperty()
          .bind(routingStrategyChoiceBox.getSelectionModel().selectedItemProperty());

      var indexOfProbabilityRoutingStrategy = -1;
      for (var i = 0; i < nonTerminalNetworkNode.getRoutingStrategies().size(); i++) {
        if (nonTerminalNetworkNode.getRoutingStrategies().get(i)
            instanceof ProbabilityRoutingStrategyModel) {
          indexOfProbabilityRoutingStrategy = i;
          break;
        }
      }
      routesVBox
          .disableProperty()
          .bind(
              routingStrategyChoiceBox
                  .getSelectionModel()
                  .selectedIndexProperty()
                  .isNotEqualTo(indexOfProbabilityRoutingStrategy));

      final var probabilityRoutingStrategy =
          nonTerminalNetworkNode.getRoutingStrategies().stream()
              .filter(ProbabilityRoutingStrategyModel.class::isInstance)
              .map(ProbabilityRoutingStrategyModel.class::cast)
              .findFirst();

      destinationNameTableColumn.setCellValueFactory(
          cellData -> cellData.getValue().getKey().nameProperty());
      probabilityTableColumn.setCellValueFactory(
          cellData -> {
            final StringProperty probabilityStringProperty =
                new SimpleStringProperty(String.valueOf(cellData.getValue().getValue()));
            final var percentageProbabilityStringProperty =
                new SimpleStringProperty(
                    NumericFormatUtil.formatPercentage(cellData.getValue().getValue().get()));

            probabilityStringProperty.bind(
                Bindings.when(usePercentageProbabilityCheckBox.selectedProperty())
                    .then(
                        Bindings.createStringBinding(
                            () ->
                                NumericFormatUtil.formatPercentage(
                                    cellData.getValue().getValue().get()),
                            percentageProbabilityStringProperty))
                    .otherwise(
                        NumericFormatUtil.formatNumber(cellData.getValue().getValue().get())));

            return probabilityStringProperty;
          });

      if (probabilityRoutingStrategy.isPresent()) {
        final var probabilityMap = probabilityRoutingStrategy.get().getProbabilityMap();

        if (probabilityMap.isEmpty()) {
          final var probability = 1.0 / nonTerminalNetworkNode.getConnectionModels().size();

          nonTerminalNetworkNode
              .getConnectionModels()
              .forEach(
                  connection ->
                      probabilityMap.put(
                          connection.getDestination(),
                          new SimpleDoubleProperty(
                              probabilityRoutingStrategy.get(),
                              connection.getDestination().getName() + "Probability",
                              probability)));
        }

        routesTableView.setItems(FXCollections.observableArrayList(probabilityMap.entrySet()));
        probabilityTableColumn.setCellFactory(
            column -> new ProbabilityTableCell<>((entry, value) -> entry.getValue().set(value)));

        for (final var probability : probabilityMap.values()) {
          probability.addListener(
              (p, oldP, newP) -> {
                final var newProbability = newP.doubleValue();
                final var values = probabilityRoutingStrategy.get().getProbabilityMap().values();

                if (newProbability == 0) {
                  zeroProbability = true;
                } else {
                  zeroProbability = values.stream().anyMatch(value -> value.get() == 0.0);
                }

                final var sumOfProbabilities =
                    values.stream().mapToDouble(DoubleProperty::get).sum();

                sumOfProbabilitiesIs1 = values.isEmpty() || sumOfProbabilities == 1.0;
              });
        }
        zeroProbability = probabilityMap.values().stream().anyMatch(value -> value.get() == 0.0);
        sumOfProbabilitiesIs1 =
            probabilityMap.isEmpty()
                || probabilityMap.values().stream().mapToDouble(DoubleProperty::get).sum() == 1.0;
      }

      usePercentageProbabilityCheckBox
          .disableProperty()
          .bind(
              nonTerminalNetworkNode
                  .connectionsProperty()
                  .emptyProperty()
                  .or(
                      routingStrategyChoiceBox
                          .getSelectionModel()
                          .selectedIndexProperty()
                          .isNotEqualTo(indexOfProbabilityRoutingStrategy)));
    } else {
      resourcePropertiesAccordion.getPanes().remove(queueTitledPane);
      resourcePropertiesAccordion.getPanes().remove(serviceTitledPane);
      resourcePropertiesAccordion.getPanes().remove(jobGenerationTitledPane);
      resourcePropertiesAccordion.getPanes().remove(routesTitledPane);
    }
  }

  @FXML
  private void onEditTimeDistributionParameters() throws IOException {
    if (model instanceof NonTerminalNetworkNodeModel nonTerminalNetworkNode) {
      UIUtil.loadModalWindow(
          String.format(
              "distributions/%sTimeDistributionProperties",
              nonTerminalNetworkNode.getSelectedTimeDistribution().getName()),
          (Stage) resourcePropertiesVBox.getScene().getWindow(),
          nonTerminalNetworkNode.getName());
    }
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    if (isModelValid()) {
      model.setName(nameTextField.getText());
    } else {
      final var alert =
          UIUtil.createAlert(
              Alert.AlertType.ERROR,
              resourcePropertiesVBox.getScene().getWindow(),
              "networkNodeProperties.errorAlert.title",
              "networkNodeProperties.errorAlert.headerText");

      final var contentText = new StringBuilder();
      var numErrors = 0;

      if (!nameIsValidProperty.get()) {
        contentText.append(
            resources.getString("networkNodeProperties.errorAlert.contentText.invalidName"));
        numErrors++;
      }

      if (routingStrategyChoiceBox.getSelectionModel().getSelectedItem()
          instanceof ProbabilityRoutingStrategyModel) {
        if (!sumOfProbabilitiesIs1) {
          if (numErrors > 0) {
            contentText.append("\n");
          }
          contentText.append(
              resources.getString(
                  "networkNodeProperties.errorAlert.contentText.invalidSumOfProbabilities"));
        }
        if (zeroProbability) {
          if (numErrors > 0) {
            contentText.append("\n");
          }
          contentText.append(
              resources.getString("networkNodeProperties.errorAlert.contentText.zeroProbability"));
        }
      }

      alert.setContentText(contentText.toString());
      alert.showAndWait();
      event.consume();
    }
  }

  private boolean isModelValid() {
    return nameIsValidProperty.get()
        && (!(routingStrategyChoiceBox.getSelectionModel().getSelectedItem()
                instanceof ProbabilityRoutingStrategyModel)
            || sumOfProbabilitiesIs1 && !zeroProbability);
  }
}
