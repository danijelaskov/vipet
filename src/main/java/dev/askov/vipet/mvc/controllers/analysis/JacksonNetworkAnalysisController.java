package dev.askov.vipet.mvc.controllers.analysis;

import dev.askov.vipet.common.LaTeXUtil;
import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.analyzer.JacksonNetworkAnalysisModel;
import dev.askov.vipet.mvc.models.distributions.ExponentialTimeDistributionModel;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public final class JacksonNetworkAnalysisController
    extends AbstractAnalysisController<JacksonNetworkAnalysisModel> {

  @FXML private ImageView step1ImageView;
  @FXML private ImageView step2ImageView;
  @FXML private ImageView step3ImageView;
  @FXML private ImageView step4ImageView;
  @FXML private ImageView step5ImageView;
  @FXML private Accordion jacksonNetworkAnalysisAccordion;
  @FXML private TitledPane calculationTitledPane;
  @FXML private VBox performanceMeasuresCalculationsVBox;
  @FXML private ImageView numberOfServersImageView;
  @FXML private ImageView serviceSpeedImageView;
  @FXML private ImageView rhoImageView;
  @FXML private ImageView pi0ImageView;
  @FXML private ImageView performanceMeasure1ImageView;
  @FXML private ImageView performanceMeasure2ImageView;
  @FXML private ImageView performanceMeasure3ImageView;
  @FXML private ImageView performanceMeasure4ImageView;
  @FXML private ImageView performanceMeasure5ImageView;
  @FXML private ImageView performanceMeasure6ImageView;
  @FXML private ChoiceBox<ServiceCenterModel> serviceCenterChoiceBox;
  @FXML private BarChart<String, Number> stateProbabilityBarChart;
  @FXML private NumberAxis probabilityNumberAxis;

  public JacksonNetworkAnalysisController(
      final JacksonNetworkAnalysisModel jacksonNetworkAnalysisModel,
      final NetworkModel networkModel) {
    super(jacksonNetworkAnalysisModel, networkModel);
  }

  @Override
  protected void initialize() {
    final var probabilities = getModel().probabilities();

    var step1Formula = new StringBuilder("\\mathbf{P}=\\begin{bmatrix}");

    for (var i = 0; i < probabilities.getRowDimension(); i++) {
      for (var j = 0; j < probabilities.getColumnDimension(); j++) {
        final var probability = probabilities.getEntry(i, j);

        step1Formula.append(NumericFormatUtil.formatNumber(probability, true, false));
        if (j < probabilities.getColumnDimension() - 1) {
          step1Formula.append(" & ");
        }
      }

      if (i < probabilities.getRowDimension() - 1) {
        step1Formula.append(" \\\\ ");
      }
    }

    step1Formula.append("\\end{bmatrix}");
    UIUtil.renderLaTeXFormula(step1Formula.toString(), step1ImageView);

    var step2Formula = new StringBuilder("\\mathbf{I}-\\mathbf{P}^\\intercal=");
    final var intermediateMatrix1 = getModel().intermediateMatrix1();
    LaTeXUtil.populateMatrix(intermediateMatrix1, step2Formula);
    UIUtil.renderLaTeXFormula(step2Formula.toString(), step2ImageView);

    var step3Formula = new StringBuilder("\\left(\\mathbf{I}-\\mathbf{P}^\\intercal\\right)^{-1}=");
    final var intermediateMatrix2 = getModel().intermediateMatrix2();
    LaTeXUtil.populateMatrix(intermediateMatrix2, step3Formula);
    UIUtil.renderLaTeXFormula(step3Formula.toString(), step3ImageView);

    var step4Formula = new StringBuilder("\\mathbf{\\alpha}=");
    final var alpha = getModel().externalArrivalRates();
    LaTeXUtil.populateColumnVector(alpha, step4Formula);
    UIUtil.renderLaTeXFormula(step4Formula.toString(), step4ImageView);

    final var step5Formula =
        new StringBuilder(
            "\\mathbf{\\lambda}=\\left(\\mathbf{I}-\\mathbf{P}^\\intercal\\right)^{-1}\\cdot\\mathbf{\\alpha}=");
    final var lambda = getModel().averageArrivalRates();
    LaTeXUtil.populateColumnVector(lambda, step5Formula);
    UIUtil.renderLaTeXFormula(step5Formula.toString(), step5ImageView);

    setup(
        serviceCenterChoiceBox,
        jacksonNetworkAnalysisAccordion,
        calculationTitledPane,
        probabilityNumberAxis);
  }

  @Override
  protected void updatePerformanceMeasuresCalculation() {
    final var selectedServiceCenterModel =
        serviceCenterChoiceBox.getSelectionModel().getSelectedItem();
    final var selectedServiceCenterIndex =
        networkModel.getServiceCenterModels().indexOf(selectedServiceCenterModel);

    final var lambda = model.averageArrivalRates().getEntry(selectedServiceCenterIndex);
    final var numberOfServers = selectedServiceCenterModel.getNumberOfServers();
    final var serviceRate =
        ((ExponentialTimeDistributionModel)
                selectedServiceCenterModel.getSelectedTimeDistribution())
            .getRate();
    final var rho = model.rhoVector().getEntry(selectedServiceCenterIndex);

    final var index = selectedServiceCenterIndex + 1;

    if (numberOfServers > 1) {
      UIUtil.renderLaTeXFormula(
          String.format("m_{%d}=%d", index, numberOfServers), numberOfServersImageView);
      if (!performanceMeasuresCalculationsVBox.getChildren().contains(numberOfServersImageView)) {
        performanceMeasuresCalculationsVBox.getChildren().addFirst(numberOfServersImageView);
      }
    } else {
      performanceMeasuresCalculationsVBox.getChildren().remove(numberOfServersImageView);
    }
    UIUtil.renderLaTeXFormula(
        String.format(
            "\\mu_{%d}=%s\\,\\text{sec}^{-1}",
            index, NumericFormatUtil.formatNumber(serviceRate, true)),
        serviceSpeedImageView);

    if (numberOfServers > 1) {
      UIUtil.renderLaTeXFormula(
          String.format(
              "\\rho_{%d}=\\frac{\\lambda_{%d}}{m_{%d}\\cdot\\mu_{%d}}=\\frac{%s}{%s\\cdot %s}=%s",
              index,
              index,
              index,
              index,
              NumericFormatUtil.formatNumber(lambda, true),
              NumericFormatUtil.formatNumber(numberOfServers, true),
              NumericFormatUtil.formatNumber(serviceRate, true),
              NumericFormatUtil.formatNumber(rho, true)),
          rhoImageView);
    } else {
      UIUtil.renderLaTeXFormula(
          String.format(
              "\\rho_{%d}=\\frac{\\lambda_{%d}}{\\mu_{%d}}=\\frac{%s}{%s}=%s",
              index,
              index,
              index,
              NumericFormatUtil.formatNumber(lambda, true),
              NumericFormatUtil.formatNumber(serviceRate, true),
              NumericFormatUtil.formatNumber(rho, true)),
          rhoImageView);
    }

    if (numberOfServers > 1) {
      String pi0Formula;
      final double pi0 =
          model.stateProbabilities().get(selectedServiceCenterModel).getFirst().getValue();
      pi0Formula =
          String.format(
              "\\pi_{%d}(0)=\\frac{1}{\\sum_{i=0}^{m_{%d}-1}\\frac{\\left(m_{%d}\\rho_{%d}\\right)^i}{i!}+\\frac{\\left(m_{%d}\\rho_{%d}\\right)^{m_{%d}}}{m_{%d}!}\\cdot\\frac{1}{1-\\rho_{%d}}}=\\frac{1}{\\sum_{i=0}^{%s}\\frac{\\left(%s\\cdot%s\\right)^i}{i!}+\\frac{\\left(%s\\cdot%s\\right)^%s}{%s!}\\cdot\\frac{1}{1-%s}}=%s",
              index,
              index,
              index,
              index,
              index,
              index,
              index,
              index,
              index,
              NumericFormatUtil.formatNumber(numberOfServers - 1, true),
              NumericFormatUtil.formatNumber(numberOfServers, true),
              NumericFormatUtil.formatNumber(rho, true),
              NumericFormatUtil.formatNumber(numberOfServers, true),
              NumericFormatUtil.formatNumber(rho, true),
              NumericFormatUtil.formatNumber(numberOfServers, true),
              NumericFormatUtil.formatNumber(numberOfServers, true),
              NumericFormatUtil.formatNumber(rho, true),
              NumericFormatUtil.formatNumber(pi0, true));
      UIUtil.renderLaTeXFormula(pi0Formula, pi0ImageView);
      if (!performanceMeasuresCalculationsVBox.getChildren().contains(pi0ImageView)) {
        performanceMeasuresCalculationsVBox.getChildren().add(3, pi0ImageView);
      }
    } else {
      performanceMeasuresCalculationsVBox.getChildren().remove(pi0ImageView);
    }

    final var throughputSymbol =
        resources.getString("performanceMeasures.throughput.symbol").formatted(index);
    final var throughput =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(selectedServiceCenterModel, PerformanceMeasureType.THROUGHPUT)
            .center();
    UIUtil.renderLaTeXFormula(
        "%s=\\lambda_{%d}=%s\\,\\text{sec}^{-1}"
            .formatted(throughputSymbol, index, NumericFormatUtil.formatNumber(throughput)),
        performanceMeasure1ImageView);

    final var utilization =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(selectedServiceCenterModel, PerformanceMeasureType.UTILIZATION)
            .center();
    UIUtil.renderLaTeXFormula(
        "%s=\\rho_{%d}=%s"
            .formatted(
                resources.getString("performanceMeasures.utilization.symbol").formatted(index),
                index,
                NumericFormatUtil.formatNumber(utilization, true)),
        performanceMeasure2ImageView);

    final var numberOfJobsSymbol =
        resources.getString("performanceMeasures.numberOfJobs.symbol").formatted(index);
    final var numberOfJobs =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(selectedServiceCenterModel, PerformanceMeasureType.NUM_JOBS)
            .center();

    final var responseTimeSymbol =
        resources.getString("performanceMeasures.responseTime.symbol").formatted(index);
    final var responseTime =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(selectedServiceCenterModel, PerformanceMeasureType.RESPONSE_TIME)
            .center();

    final var waitingTimeSymbol =
        resources.getString("performanceMeasures.waitingTime.symbol").formatted(index);
    final var waitingTime =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(selectedServiceCenterModel, PerformanceMeasureType.WAITING_TIME)
            .center();

    final var queueLengthSymbol =
        resources.getString("performanceMeasures.queueLength.symbol").formatted(index);
    final var queueLength =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(selectedServiceCenterModel, PerformanceMeasureType.QUEUE_LENGTH)
            .center();

    if (numberOfServers > 1) {
      UIUtil.renderLaTeXFormula(
          "%s=\\frac{m_{%d}^{m_{%d}}\\rho_{%d}^{m_{%d}+1}}{m_{%d}!\\left(1-\\rho_{%d}\\right)^2}\\pi_{%d}(0)=\\frac{%s^{%s}\\cdot \\left(%s\\right)^{%s}}{%s!\\left(1-%s\\right)^2}\\cdot %s=%s"
              .formatted(
                  queueLengthSymbol,
                  index,
                  index,
                  index,
                  index,
                  index,
                  index,
                  index,
                  NumericFormatUtil.formatNumber(numberOfServers, true),
                  NumericFormatUtil.formatNumber(numberOfServers, true),
                  NumericFormatUtil.formatNumber(rho, true),
                  NumericFormatUtil.formatNumber(numberOfServers + 1, true),
                  NumericFormatUtil.formatNumber(numberOfServers, true),
                  NumericFormatUtil.formatNumber(rho, true),
                  NumericFormatUtil.formatNumber(
                      model
                          .stateProbabilities()
                          .get(selectedServiceCenterModel)
                          .getFirst()
                          .getValue(),
                      true),
                  NumericFormatUtil.formatNumber(queueLength, true)),
          performanceMeasure3ImageView);

      UIUtil.renderLaTeXFormula(
          "%s=\\frac{%s}{%s}=\\frac{%s}{%s}=%s\\,\\text{sec}"
              .formatted(
                  waitingTimeSymbol,
                  queueLengthSymbol,
                  throughputSymbol,
                  NumericFormatUtil.formatNumber(queueLength, true),
                  NumericFormatUtil.formatNumber(lambda, true),
                  NumericFormatUtil.formatNumber(waitingTime, true)),
          performanceMeasure4ImageView);

      UIUtil.renderLaTeXFormula(
          "%s=%s+\\frac{1}{\\mu_{%d}}=%s+\\frac{1}{%s}=%s\\,\\text{sec}"
              .formatted(
                  responseTimeSymbol,
                  waitingTimeSymbol,
                  index,
                  NumericFormatUtil.formatNumber(waitingTime, true),
                  NumericFormatUtil.formatNumber(serviceRate, true),
                  NumericFormatUtil.formatNumber(responseTime, true)),
          performanceMeasure5ImageView);

      UIUtil.renderLaTeXFormula(
          "%s=%s\\cdot %s=%s\\cdot %s=%s"
              .formatted(
                  numberOfJobsSymbol,
                  responseTimeSymbol,
                  throughputSymbol,
                  NumericFormatUtil.formatNumber(responseTime, true),
                  NumericFormatUtil.formatNumber(throughput, true),
                  NumericFormatUtil.formatNumber(numberOfJobs, true)),
          performanceMeasure6ImageView);
    } else {
      UIUtil.renderLaTeXFormula(
          "%s=\\frac{\\rho_{%d}}{1-\\rho_{%d}}=\\frac{%s}{1-%s}=%s"
              .formatted(
                  numberOfJobsSymbol,
                  index,
                  index,
                  NumericFormatUtil.formatNumber(rho, true),
                  NumericFormatUtil.formatNumber(rho, true),
                  NumericFormatUtil.formatNumber(numberOfJobs, true)),
          performanceMeasure3ImageView);

      UIUtil.renderLaTeXFormula(
          "%s=\\frac{%s}{%s}=\\frac{%s}{%s}=%s\\,\\text{sec}"
              .formatted(
                  responseTimeSymbol,
                  numberOfJobsSymbol,
                  throughputSymbol,
                  NumericFormatUtil.formatNumber(numberOfJobs, true),
                  NumericFormatUtil.formatNumber(throughput, true),
                  NumericFormatUtil.formatNumber(responseTime, true)),
          performanceMeasure4ImageView);

      UIUtil.renderLaTeXFormula(
          "%s=%s-\\frac{1}{\\mu_{%d}}=%s-\\frac{1}{%s}=%s\\,\\text{sec}"
              .formatted(
                  waitingTimeSymbol,
                  responseTimeSymbol,
                  index,
                  NumericFormatUtil.formatNumber(responseTime, true),
                  NumericFormatUtil.formatNumber(serviceRate, true),
                  NumericFormatUtil.formatNumber(waitingTime, true)),
          performanceMeasure5ImageView);

      UIUtil.renderLaTeXFormula(
          "%s=%s\\cdot %s=%s\\cdot %s=%s"
              .formatted(
                  queueLengthSymbol,
                  throughputSymbol,
                  waitingTimeSymbol,
                  NumericFormatUtil.formatNumber(throughput, true),
                  NumericFormatUtil.formatNumber(waitingTime, true),
                  NumericFormatUtil.formatNumber(queueLength, true)),
          performanceMeasure6ImageView);
    }
  }

  @Override
  protected void updateStateProbabilityBarChart() {
    final var series = new XYChart.Series<String, Number>();

    final var selectedServiceCenterModel =
        serviceCenterChoiceBox.getSelectionModel().getSelectedItem();

    var previousNumJobs = -1;
    final List<Integer> dotsIndices = new ArrayList<>();
    for (var stateProbabilityPair :
        getModel().stateProbabilities().get(selectedServiceCenterModel)) {
      if (previousNumJobs != -1 && stateProbabilityPair.getKey() - previousNumJobs > 1) {
        dotsIndices.add(series.getData().size());
        series.getData().add(new XYChart.Data<>(" ... ", 0));
      }
      previousNumJobs = stateProbabilityPair.getKey();
      series
          .getData()
          .add(
              new XYChart.Data<>(
                  String.valueOf(stateProbabilityPair.getKey()), stateProbabilityPair.getValue()));
    }

    dotsIndices.add(series.getData().size());
    series.getData().add(new XYChart.Data<>(" ...", 0));

    stateProbabilityBarChart.getData().clear();
    stateProbabilityBarChart.getData().add(series);

    stateProbabilityBarChart.setBarGap(0);
    stateProbabilityBarChart.setCategoryGap(0);

    var stateProbabilityIndex = 0;
    for (var j = 0; j < series.getData().size(); j++) {
      final var node = stateProbabilityBarChart.lookup(".data" + j + ".chart-bar");

      node.setStyle(
          "-fx-bar-fill: %s;".formatted(UIUtil.toRGB(selectedServiceCenterModel.getFillColor())));

      if (dotsIndices.contains(j)) {
        continue;
      }

      UIUtil.installTooltip(
          node,
          getModel()
              .stateProbabilities()
              .get(selectedServiceCenterModel)
              .get(stateProbabilityIndex)
              .getValue());

      stateProbabilityIndex++;
    }
  }
}
