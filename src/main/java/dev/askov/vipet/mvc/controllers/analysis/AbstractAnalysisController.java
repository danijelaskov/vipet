package dev.askov.vipet.mvc.controllers.analysis;

import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.controllers.converters.ServiceCenterConverter;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import dev.askov.vipet.mvc.views.formatters.LocaleAwareNumberAxisFormatter;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Accordion;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;

public abstract class AbstractAnalysisController<AnalysisModel>
    extends AbstractController<AnalysisModel> {

  protected final NetworkModel networkModel;

  public AbstractAnalysisController(
      final AnalysisModel analysisModel, final NetworkModel networkModel) {
    super(analysisModel);
    this.networkModel = networkModel;
  }

  protected final void setup(
      final ChoiceBox<ServiceCenterModel> serviceCenterModelChoiceBox,
      final Accordion accordion,
      final TitledPane defaultTitledPane,
      final NumberAxis probabilityNumberAxis) {
    serviceCenterModelChoiceBox.getItems().addAll(networkModel.getServiceCenterModels());
    serviceCenterModelChoiceBox.setConverter(new ServiceCenterConverter(networkModel));
    serviceCenterModelChoiceBox
        .valueProperty()
        .addListener(
            (serviceCenterModel, oldServiceCenterModel, newServiceCenterModel) -> {
              updatePerformanceMeasuresCalculation();
              updateStateProbabilityBarChart();
            });
    serviceCenterModelChoiceBox.getSelectionModel().selectFirst();

    accordion.setExpandedPane(defaultTitledPane);
    accordion
        .expandedPaneProperty()
        .addListener(
            (expandedPane, oldPane, newPane) -> {
              if (newPane == null) {
                accordion.setExpandedPane(oldPane);
              } else if (oldPane != null) {
                oldPane.setExpanded(false);
                accordion.setExpandedPane(newPane);
              }
            });

    probabilityNumberAxis.setTickLabelFormatter(
        new LocaleAwareNumberAxisFormatter(probabilityNumberAxis));
  }

  protected abstract void updatePerformanceMeasuresCalculation();

  protected abstract void updateStateProbabilityBarChart();
}
