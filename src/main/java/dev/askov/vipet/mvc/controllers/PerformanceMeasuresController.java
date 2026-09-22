package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasure;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasuresProvider;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.views.formatters.LocaleAwareNumberAxisFormatter;
import dev.askov.vipet.serialization.PerformanceMeasuresExporter;
import dev.askov.vipet.serialization.csv.CSVPerformanceMeasuresExporter;
import dev.askov.vipet.serialization.excel.XLSPerformanceMeasuresExporter;
import dev.askov.vipet.serialization.excel.XLSXPerformanceMeasuresExporter;
import dev.askov.vipet.serialization.json.JSONPerformanceMeasuresExporter;
import java.io.File;
import java.util.Comparator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;

public final class PerformanceMeasuresController
    extends AbstractController<PerformanceMeasuresProvider> {

  private static final Logger LOGGER =
      org.slf4j.LoggerFactory.getLogger(PerformanceMeasuresController.class);
  private static final FileChooser.ExtensionFilter CSV_EXTENSION_FILTER =
      new FileChooser.ExtensionFilter("Comma-Separated Values (*.csv)", "*.csv");
  private static final FileChooser.ExtensionFilter XLSX_EXTENSION_FILTER =
      new FileChooser.ExtensionFilter("Microsoft Excel Workbook (*.xlsx)", "*.xlsx");
  private static final FileChooser.ExtensionFilter XLS_EXTENSION_FILTER =
      new FileChooser.ExtensionFilter("Microsoft Excel 97-2003 Workbook (*.xls)", "*.xls");
  private static final FileChooser.ExtensionFilter JSON_EXTENSION_FILTER =
      new FileChooser.ExtensionFilter("JavaScript Object Notation (*.json)", "*.json");
  private static final FileChooser.ExtensionFilter[] FILE_CHOOSER_EXTENSION_FILTERS =
      new FileChooser.ExtensionFilter[] {
        CSV_EXTENSION_FILTER, XLSX_EXTENSION_FILTER, XLS_EXTENSION_FILTER, JSON_EXTENSION_FILTER
      };

  @FXML private VBox rootVBox;
  @FXML private TabPane performanceIndicesTabPane;
  @FXML private TableView<PerformanceMeasureCollection.Metadata> performanceIndicesTableView;
  @FXML private Label centralServiceCenterLabel;

  public PerformanceMeasuresController(PerformanceMeasuresProvider model) {
    super(model);
  }

  @Override
  protected void initialize() {
    for (final var performanceIndicator : PerformanceMeasureType.values()) {
      createAndPopulateBarChart(performanceIndicator);
      createAndPopulateTableColumn(performanceIndicator);
    }
    performanceIndicesTableView.setItems(
        model.getValue().getPerformanceMeasureCollectionMetadataList());
    if (model.getNetworkModel().isOpen()) {
      rootVBox.getChildren().remove(centralServiceCenterLabel);
    }
  }

  private void createAndPopulateBarChart(final PerformanceMeasureType performanceMeasureType) {
    if (model.getValue().hasPerformanceMeasure(performanceMeasureType)) {
      final var tab = new Tab(performanceMeasureType.getName());
      final var anchorPane = new AnchorPane();
      final var xAxis = new CategoryAxis();
      final var yAxis = new NumberAxis();
      final var barChart = new BarChart<>(xAxis, yAxis);

      yAxis.setLabel(performanceMeasureType.getUnit());
      yAxis.setTickLabelFormatter(new LocaleAwareNumberAxisFormatter(yAxis));
      barChart.setAnimated(false);

      final var series = new XYChart.Series<String, Number>();
      for (final var serviceCenterModel : model.getNetworkModel().getServiceCenterModels()) {
        final var name =
            model.getNetworkModel().isOpen()
                ? serviceCenterModel.getName()
                : model.getNetworkModel().getCentralServiceCenterModel().equals(serviceCenterModel)
                    ? serviceCenterModel.getName()
                        + resources
                            .getString("performanceMeasures.centralServiceCenterFootnote")
                            .charAt(0)
                    : serviceCenterModel.getName();
        final var data =
            new XYChart.Data<String, Number>(
                name,
                model
                    .getValue()
                    .getPerformanceMeasure(serviceCenterModel, performanceMeasureType)
                    .center());

        series.getData().add(data);
      }

      if (model.getValue().hasSystemPerformanceMeasure(performanceMeasureType)) {
        series
            .getData()
            .add(
                new XYChart.Data<>(
                    resources.getString("performanceMeasures.system"),
                    model.getValue().getSystemPerformanceMeasure(performanceMeasureType).center()));
      }

      barChart.getData().add(series);

      for (var i = 0; i < model.getNetworkModel().getServiceCenterModels().size(); i++) {
        final var serviceCenterModel = model.getNetworkModel().getServiceCenterModels().get(i);
        final var performanceMeasure =
            model.getValue().getPerformanceMeasure(serviceCenterModel, performanceMeasureType);
        final var node = barChart.lookup(".data" + i + ".chart-bar");

        node.setStyle(
            "-fx-bar-fill: %s;".formatted(UIUtil.toRGB(serviceCenterModel.getFillColor())));

        UIUtil.installTooltip(node, performanceMeasure.toString());
      }

      if (model.getValue().hasSystemPerformanceMeasure(performanceMeasureType)) {
        final var node =
            barChart.lookup(
                ".data" + model.getNetworkModel().getServiceCenterModels().size() + ".chart-bar");

        node.setStyle("-fx-bar-fill: %s;".formatted(UIUtil.toRGB(NetworkModel.DEFAULT_COLOR)));

        UIUtil.installTooltip(
            node, model.getValue().getSystemPerformanceMeasure(performanceMeasureType).toString());
      }

      barChart.setLegendVisible(false);

      AnchorPane.setTopAnchor(barChart, 5.0);
      AnchorPane.setBottomAnchor(barChart, 5.0);
      AnchorPane.setLeftAnchor(barChart, 5.0);
      AnchorPane.setRightAnchor(barChart, 5.0);

      anchorPane.getChildren().add(barChart);
      tab.setContent(anchorPane);
      performanceIndicesTabPane.getTabs().add(tab);
    }
  }

  private void createAndPopulateTableColumn(final PerformanceMeasureType performanceMeasureType) {
    if (model.getValue().hasPerformanceMeasure(performanceMeasureType)) {
      if (performanceIndicesTableView.getColumns().isEmpty()) {
        final var resourceNameTableColumn =
            new TableColumn<PerformanceMeasureCollection.Metadata, String>(
                resources.getString("performanceMeasures.name"));

        resourceNameTableColumn.setCellValueFactory(
            cellData -> {
              if (cellData.getValue().isSystemPerformanceMeasureCollection()) {
                return new SimpleStringProperty(resources.getString("performanceMeasures.system"));
              } else {
                final var serviceCenterModel = cellData.getValue().getServiceCenter();
                final var name =
                    model.getNetworkModel().isOpen()
                        ? serviceCenterModel.getName()
                        : model
                                .getNetworkModel()
                                .getCentralServiceCenterModel()
                                .equals(serviceCenterModel)
                            ? serviceCenterModel.getName()
                                + resources
                                    .getString("performanceMeasures.centralServiceCenterFootnote")
                                    .charAt(0)
                            : serviceCenterModel.getName();

                return new SimpleStringProperty(name);
              }
            });

        performanceIndicesTableView.getColumns().add(resourceNameTableColumn);
      }

      final var performanceMeasureTableColumn =
          getPerformanceMeasureTableColumn(performanceMeasureType);

      performanceIndicesTableView.getColumns().add(performanceMeasureTableColumn);
    }
  }

  private TableColumn<PerformanceMeasureCollection.Metadata, PerformanceMeasure>
      getPerformanceMeasureTableColumn(PerformanceMeasureType performanceMeasureType) {
    final var performanceMeasureTableColumn =
        new TableColumn<PerformanceMeasureCollection.Metadata, PerformanceMeasure>(
            performanceMeasureType.getName());
    performanceMeasureTableColumn.setCellValueFactory(
        cellData -> {
          final var metadata = cellData.getValue();

          PerformanceMeasure performanceMeasure;

          if (metadata.isSystemPerformanceMeasureCollection()) {
            performanceMeasure =
                model.getValue().getSystemPerformanceMeasure(performanceMeasureType);
          } else {
            performanceMeasure = cellData.getValue().getPerformanceMeasure(performanceMeasureType);
          }

          return new SimpleObjectProperty<>(performanceMeasure);
        });
    performanceMeasureTableColumn.setComparator(
        Comparator.nullsLast(Comparator.comparingDouble(PerformanceMeasure::center)));
    return performanceMeasureTableColumn;
  }

  @FXML
  void onExport() {
    final var fileChooser = new FileChooser();

    fileChooser.setTitle(resources.getString("performanceMeasures.dialogs.export.title"));
    fileChooser.setInitialFileName(getModel().getNetworkModel().getName());
    fileChooser.getExtensionFilters().addAll(FILE_CHOOSER_EXTENSION_FILTERS);

    final var file = fileChooser.showSaveDialog(VIPET.getPrimaryStage());

    if (file != null) {
      final var performanceMeasuresExporter = getPerformanceMeasuresExporter(fileChooser, file);
      Alert alert;

      if (performanceMeasuresExporter != null) {
        LOGGER.debug("Exporting performance indices to file {}", file.getName());

        if (performanceMeasuresExporter.export()) {
          alert =
              UIUtil.createAlert(
                  Alert.AlertType.INFORMATION,
                  performanceIndicesTabPane.getScene().getWindow(),
                  "performanceMeasures.dialogs.export.success.title",
                  "performanceMeasures.dialogs.export.success.header");
          alert.setContentText(
              resources
                  .getString("performanceMeasures.dialogs.export.success.content")
                  .formatted(file.getName()));

          LOGGER.debug("Successfully exported performance indices to file {}", file.getName());
        } else {
          alert =
              UIUtil.createAlert(
                  Alert.AlertType.ERROR,
                  performanceIndicesTabPane.getScene().getWindow(),
                  "performanceMeasures.dialogs.export.error.title",
                  "performanceMeasures.dialogs.export.error.header");
          alert.setContentText(
              resources
                  .getString("performanceMeasures.dialogs.export.error.content")
                  .formatted(file.getName()));

          LOGGER.error("Failed to export performance indices to file {}", file.getName());
        }
      } else {
        alert =
            UIUtil.createAlert(
                Alert.AlertType.ERROR,
                performanceIndicesTabPane.getScene().getWindow(),
                "dialogs.export.error.title",
                "dialogs.export.error.header");
        alert.setContentText(
            localizationManager.getString("dialogs.export.error.content", file.getName()));

        LOGGER.error("Failed to export performance indices to file {}", file.getName());
      }

      alert.showAndWait();
    }
  }

  private PerformanceMeasuresExporter getPerformanceMeasuresExporter(
      final FileChooser fileChooser, final File file) {
    final PerformanceMeasuresExporter performanceMeasuresExporter;

    if (fileChooser.getSelectedExtensionFilter() == CSV_EXTENSION_FILTER) {
      performanceMeasuresExporter =
          new CSVPerformanceMeasuresExporter(file, model.getValue(), resources);
    } else if (fileChooser.getSelectedExtensionFilter() == XLSX_EXTENSION_FILTER) {
      performanceMeasuresExporter =
          new XLSXPerformanceMeasuresExporter(file, model.getValue(), resources);
    } else if (fileChooser.getSelectedExtensionFilter() == XLS_EXTENSION_FILTER) {
      performanceMeasuresExporter =
          new XLSPerformanceMeasuresExporter(file, model.getValue(), resources);
    } else if (fileChooser.getSelectedExtensionFilter() == JSON_EXTENSION_FILTER) {
      performanceMeasuresExporter =
          new JSONPerformanceMeasuresExporter(
              file, model.getValue(), resources, model.getNetworkModel());
    } else {
      return null;
    }

    return performanceMeasuresExporter;
  }
}
