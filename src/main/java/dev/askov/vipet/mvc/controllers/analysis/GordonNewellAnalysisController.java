package dev.askov.vipet.mvc.controllers.analysis;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.common.LaTeXUtil;
import dev.askov.vipet.common.MathUtil;
import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.mvc.models.analyzer.GordonNewellAnalysisModel;
import dev.askov.vipet.mvc.models.distributions.ExponentialTimeDistributionModel;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GordonNewellAnalysisController
    extends AbstractAnalysisController<GordonNewellAnalysisModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GordonNewellAnalysisController.class);
  private static final int MAX_ADDEND_COUNT = 4;
  private static final int MAX_FACTOR_COUNT = 7;

  private final List<List<Rectangle>> fBackgroundRectangles = new ArrayList<>();
  private final List<List<Rectangle>> gBackgroundRectangles = new ArrayList<>();

  @FXML private ImageView step1ImageView;
  @FXML private ImageView step2ImageView;
  @FXML private ImageView step3ImageView;
  @FXML private ImageView step4ImageView;
  @FXML private ImageView step5ImageView;
  @FXML private ImageView step6ImageView;
  @FXML private GridPane step7GridPane;
  @FXML private GridPane step8GridPane;
  @FXML private Accordion gordonNewellAnalysisAccordion;
  @FXML private TitledPane calculationTitledPane;
  @FXML private VBox performanceMeasuresCalculationsVBox;
  @FXML private ImageView numberOfServersImageView;
  @FXML private ImageView serviceSpeedImageView;
  @FXML private ImageView performanceMeasure1ImageView;
  @FXML private ImageView performanceMeasure2ImageView;
  @FXML private ImageView performanceMeasure3ImageView;
  @FXML private ImageView performanceMeasure4ImageView;
  @FXML private ImageView performanceMeasure5ImageView;
  @FXML private ImageView performanceMeasure6ImageView;
  @FXML private ChoiceBox<ServiceCenterModel> serviceCenterChoiceBox;
  @FXML private BarChart<String, Number> stateProbabilityBarChart;
  @FXML private NumberAxis probabilityNumberAxis;

  public GordonNewellAnalysisController(
      final GordonNewellAnalysisModel gordonNewellAnalysisModel, final NetworkModel networkModel) {
    super(gordonNewellAnalysisModel, networkModel);
  }

  @Override
  protected void initialize() {
    final var gordonNewellAnalysisModel = getModel();

    final var pMatrix = gordonNewellAnalysisModel.probabilitySubMatrix();
    final var step1Formula = new StringBuilder("\\mathbf{P}_{\\text{sub}}=");
    LaTeXUtil.populateMatrix(pMatrix, step1Formula);
    UIUtil.renderLaTeXFormula(step1Formula.toString(), step1ImageView);

    final var intermediateMatrix1 = gordonNewellAnalysisModel.intermediateMatrix1();
    final var step2Formula = new StringBuilder("\\mathbf{I}-\\mathbf{P}_{\\text{sub}}^\\intercal=");
    LaTeXUtil.populateMatrix(intermediateMatrix1, step2Formula);
    UIUtil.renderLaTeXFormula(step2Formula.toString(), step2ImageView);

    final var intermediateMatrix2 = gordonNewellAnalysisModel.intermediateMatrix2();
    final var step3Formula =
        new StringBuilder("\\left(\\mathbf{I}-\\mathbf{P}_{\\text{sub}}^\\intercal\\right)^{-1}=");
    LaTeXUtil.populateMatrix(intermediateMatrix2, step3Formula);
    UIUtil.renderLaTeXFormula(step3Formula.toString(), step3ImageView);

    final var probabilitySubVector = gordonNewellAnalysisModel.probabilitySubVector();
    final var step4Formula = new StringBuilder("\\mathbf{p}=");
    LaTeXUtil.populateColumnVector(probabilitySubVector, step4Formula);
    UIUtil.renderLaTeXFormula(step4Formula.toString(), step4ImageView);

    final var visitRatioSubVector = gordonNewellAnalysisModel.visitRatioSubVector();
    final var step5Formula =
        new StringBuilder(
            "\\mathbf{v}_{\\text{sub}} = \\left(\\mathbf{I}-\\mathbf{P}_{\\text{sub}}^\\intercal\\right)^{-1}\\mathbf{p}=");
    LaTeXUtil.populateColumnVector(visitRatioSubVector, step5Formula);
    UIUtil.renderLaTeXFormula(step5Formula.toString(), step5ImageView);

    final var visitRatioVector = gordonNewellAnalysisModel.visitRatios();
    final var step6Formula = new StringBuilder("\\mathbf{v}=");
    LaTeXUtil.populateColumnVector(visitRatioVector, step6Formula);
    UIUtil.renderLaTeXFormula(step6Formula.toString(), step6ImageView);

    final var fMatrix = gordonNewellAnalysisModel.fMatrix();

    for (var i = 0; i < fMatrix.getColumnDimension(); i++) {
      fBackgroundRectangles.add(new ArrayList<>());
      gBackgroundRectangles.add(new ArrayList<>());

      for (var j = 0; j < fMatrix.getRowDimension(); j++) {
        fBackgroundRectangles.get(i).add(null);
        gBackgroundRectangles.get(i).add(null);
      }
    }

    for (var i = 0; i < fMatrix.getRowDimension(); i++) {
      for (var j = 0; j < fMatrix.getColumnDimension(); j++) {
        final var fValueFormula =
            String.format(
                "F_{%d}\\left(%d\\right)=%s",
                j + 1, i, NumericFormatUtil.formatNumber(fMatrix.getEntry(i, j), true));

        final var fValueImageViewContainer = createFValueImageViewContainer(i, j, fValueFormula);
        step7GridPane.add(fValueImageViewContainer, j, i);
      }
    }

    final var gMatrix = gordonNewellAnalysisModel.gMatrix();

    for (var i = 0; i < gMatrix.getRowDimension(); i++) {
      for (var j = 0; j < gMatrix.getColumnDimension(); j++) {
        String gValueFormula;
        if (j == gMatrix.getColumnDimension() - 1) {
          gValueFormula =
              String.format(
                  "G_{%d}\\left(%d\\right)=%s=G\\left(%d\\right)",
                  j + 1, i, NumericFormatUtil.formatNumber(gMatrix.getEntry(i, j), true), i);
        } else {
          gValueFormula =
              String.format(
                  "G_{%d}\\left(%d\\right)=%s",
                  j + 1, i, NumericFormatUtil.formatNumber(gMatrix.getEntry(i, j), true));
        }

        final var gValueImageViewContainer = createGValueImageViewContainer(i, j, gValueFormula);
        step8GridPane.add(gValueImageViewContainer, j, i);
      }
    }

    setup(
        serviceCenterChoiceBox,
        gordonNewellAnalysisAccordion,
        calculationTitledPane,
        probabilityNumberAxis);
  }

  private StackPane createFValueImageViewContainer(
      final int i, final int j, final String fValueFormula) {
    final var fImageViewContainer = new StackPane();

    final var backgroundRectangle = createBackgroundRectangle(j);

    fImageViewContainer.setOnMouseEntered(event -> backgroundRectangle.setOpacity(1.0));
    fImageViewContainer.setOnMouseExited(event -> backgroundRectangle.setOpacity(0));

    renderFormula(
        i, j, fValueFormula, fImageViewContainer, backgroundRectangle, fBackgroundRectangles);

    final var numberOfServers = networkModel.getServiceCenterModels().get(j).getNumberOfServers();
    final var mu =
        ((ExponentialTimeDistributionModel)
                networkModel.getServiceCenterModels().get(j).getSelectedTimeDistribution())
            .getRate();

    final var denominatorCalculationFormula = new StringBuilder();

    if (i == 0) {
      denominatorCalculationFormula.append("1");
    } else if (i <= MAX_FACTOR_COUNT) {
      appendFactors(numberOfServers, mu, denominatorCalculationFormula, 1, i);
    } else {
      final var startFactorCount =
          MAX_FACTOR_COUNT % 2 == 0 ? MAX_FACTOR_COUNT / 2 : (MAX_FACTOR_COUNT + 1) / 2;
      final var endFactorCount = MAX_FACTOR_COUNT / 2;

      appendFactors(numberOfServers, mu, denominatorCalculationFormula, 1, startFactorCount);
      denominatorCalculationFormula.append("\\cdots");
      appendFactors(numberOfServers, mu, denominatorCalculationFormula, i - endFactorCount + 1, i);
    }

    var fCalculationFormula =
        String.format(
            "F_{%d}\\left(%d\\right)=\\frac{%s^{%d}}{%s}",
            j + 1,
            i,
            NumericFormatUtil.formatNumber(model.visitRatios().getEntry(j)),
            i,
            denominatorCalculationFormula);

    final var fImageView = new ImageView();
    UIUtil.renderLaTeXFormula(
        fCalculationFormula,
        fImageView,
        Objects.equals(VIPET.getUserAgentStylesheet(), VIPET.STYLESHEET_MODENA)
            ? java.awt.Color.WHITE
            : java.awt.Color.BLACK);

    final var fTooltip = new Tooltip();
    fTooltip.setGraphic(fImageView);
    Tooltip.install(fImageViewContainer, fTooltip);

    return fImageViewContainer;
  }

  private void renderFormula(
      final int i,
      final int j,
      final String formula,
      final StackPane fImageViewContainer,
      final Rectangle backgroundRectangle,
      List<List<Rectangle>> backgroundRectangles) {
    final var fValueImageView = new ImageView();

    UIUtil.renderLaTeXFormula(formula, fValueImageView);

    backgroundRectangle.widthProperty().bind(fValueImageView.fitWidthProperty());
    backgroundRectangle.heightProperty().bind(fValueImageView.fitHeightProperty());

    fImageViewContainer.setAlignment(Pos.CENTER_LEFT);
    fImageViewContainer.getChildren().addAll(backgroundRectangle, fValueImageView);

    backgroundRectangles.get(j).set(i, backgroundRectangle);
  }

  private void appendFactors(
      final int numberOfServers,
      final double mu,
      final StringBuilder denominatorCalculationFormula,
      final int from,
      final int to) {
    for (var i = from; i <= to; i++) {
      if (i <= numberOfServers) {
        denominatorCalculationFormula.append(
            String.format(
                "\\left(%s\\cdot%s\\right)",
                NumericFormatUtil.formatNumber(i, true), NumericFormatUtil.formatNumber(mu, true)));
      } else {
        denominatorCalculationFormula.append(
            String.format(
                "\\left(%s\\cdot%s\\right)",
                NumericFormatUtil.formatNumber(numberOfServers, true),
                NumericFormatUtil.formatNumber(mu, true)));
      }
    }
  }

  private void appendAddends(
      final StringBuilder gCalculationFormula,
      final int serverIndex,
      final int numberOfJobs,
      final int from,
      final int to) {
    for (var i = from; i <= to; i++) {
      if (i > 0) {
        gCalculationFormula.append("+");
      }
      gCalculationFormula.append(
          "F_{%d}\\left(%d\\right)\\cdot G_{%d}\\left(%d\\right)"
              .formatted(serverIndex + 1, i, serverIndex, numberOfJobs - i));
    }
  }

  private StackPane createGValueImageViewContainer(
      final int numberOfJobs, final int serverIndex, final String gValueFormula) {
    final var gImageViewContainer = new StackPane();
    final var backgroundRectangle = createBackgroundRectangle(serverIndex);

    backgroundRectangle.widthProperty().bind(gImageViewContainer.widthProperty());
    backgroundRectangle.heightProperty().bind(gImageViewContainer.heightProperty());

    gImageViewContainer.setOnMouseEntered(
        event -> {
          backgroundRectangle.setOpacity(1.0);

          final var fBackgroundRectangles = this.fBackgroundRectangles.get(serverIndex);
          if (serverIndex > 0) {
            final var gBackgroundRectangles = this.gBackgroundRectangles.get(serverIndex - 1);
            for (var i = 0; i <= numberOfJobs; i++) {
              if (fBackgroundRectangles.get(i) != null) {
                fBackgroundRectangles.get(i).setOpacity(1.0);
              }
              gBackgroundRectangles.get(i).setOpacity(1.0);
            }
          } else {
            fBackgroundRectangles.get(numberOfJobs).setOpacity(1.0);
          }
        });
    gImageViewContainer.setOnMouseExited(
        event -> {
          backgroundRectangle.setOpacity(0);

          final var fBackgroundRectangles = this.fBackgroundRectangles.get(serverIndex);
          if (serverIndex > 0) {
            final var gBackgroundRectangles = this.gBackgroundRectangles.get(serverIndex - 1);
            for (var i = 0; i <= numberOfJobs; i++) {
              fBackgroundRectangles.get(i).setOpacity(0);
              gBackgroundRectangles.get(i).setOpacity(0);
            }
          } else {
            fBackgroundRectangles.get(numberOfJobs).setOpacity(0);
          }
        });

    renderFormula(
        numberOfJobs,
        serverIndex,
        gValueFormula,
        gImageViewContainer,
        backgroundRectangle,
        gBackgroundRectangles);

    final var gCalculationFormula =
        new StringBuilder("G_{%d}\\left(%d\\right)=".formatted(serverIndex + 1, numberOfJobs));

    if (serverIndex == 0) {
      gCalculationFormula.append(
          "F_{%d}\\left(%d\\right)".formatted(serverIndex + 1, numberOfJobs));
    } else if (numberOfJobs < MAX_ADDEND_COUNT) {
      appendAddends(gCalculationFormula, serverIndex, numberOfJobs, 0, numberOfJobs);
    } else {
      final var startAddendCount =
          (MAX_ADDEND_COUNT % 2 == 0 ? MAX_ADDEND_COUNT / 2 : (MAX_ADDEND_COUNT + 1) / 2) - 1;
      final var endAddendCount = MAX_ADDEND_COUNT / 2;

      appendAddends(gCalculationFormula, serverIndex, numberOfJobs, 0, startAddendCount);
      gCalculationFormula.append("+\\cdots");
      appendAddends(
          gCalculationFormula,
          serverIndex,
          numberOfJobs,
          numberOfJobs - endAddendCount + 1,
          numberOfJobs);
    }

    final var gImageView = new ImageView();
    UIUtil.renderLaTeXFormula(
        gCalculationFormula.toString(),
        gImageView,
        Objects.equals(VIPET.getUserAgentStylesheet(), VIPET.STYLESHEET_MODENA)
            ? java.awt.Color.WHITE
            : java.awt.Color.BLACK);

    final var gTooltip = new Tooltip();
    gTooltip.setGraphic(gImageView);
    Tooltip.install(gImageViewContainer, gTooltip);

    return gImageViewContainer;
  }

  private Rectangle createBackgroundRectangle(final int serverIndex) {
    final var serviceCenterModel = networkModel.getServiceCenterModels().get(serverIndex);

    final var backgroundRectangle = new Rectangle();
    backgroundRectangle.setFill(serviceCenterModel.getFillColor());
    backgroundRectangle.setArcWidth(15);
    backgroundRectangle.setArcHeight(15);
    backgroundRectangle.setOpacity(0);
    backgroundRectangle.setStroke(serviceCenterModel.getStrokeColor());

    return backgroundRectangle;
  }

  @Override
  protected void updatePerformanceMeasuresCalculation() {
    final var serviceCenterModel = serviceCenterChoiceBox.getSelectionModel().getSelectedItem();

    final var selectedServiceCenterIndex =
        networkModel.getServiceCenterModels().indexOf(serviceCenterModel);
    final var index = selectedServiceCenterIndex + 1;

    final var numberOfServers = serviceCenterModel.getNumberOfServers(); // m
    final var serviceRate =
        ((ExponentialTimeDistributionModel) serviceCenterModel.getSelectedTimeDistribution())
            .getRate(); // mu

    final var totalNumberOfJobs = model.numberOfJobs(); // K
    final var numberOfServiceCenters = model.numberOfServiceCenters(); // N
    final var visitRatio = model.visitRatios().getEntry(selectedServiceCenterIndex); // v
    final var previousNormalizingConstant =
        model.gMatrix().getEntry(totalNumberOfJobs - 1, numberOfServiceCenters - 1); // G(K-1)
    final var normalizingConstant =
        model.gMatrix().getEntry(totalNumberOfJobs, numberOfServiceCenters - 1); // G(K)

    LOGGER.debug(
        "Visit ratio: {}, G(K-1): {}, G(K): {}",
        visitRatio,
        previousNormalizingConstant,
        normalizingConstant);

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
        "\\mu_{%d}=%s\\,\\text{sec}^{-1}"
            .formatted(index, NumericFormatUtil.formatNumber(serviceRate, true)),
        serviceSpeedImageView);

    final var throughputSymbol =
        resources.getString("performanceMeasures.throughput.symbol").formatted(index);
    final var throughput =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.THROUGHPUT)
            .center();
    // X = v * G(K-1) / G(K)
    final var throughputFormula =
        "%s=v_{%d}\\frac{G\\left(%d\\right)}{G\\left(%d\\right)}=%s\\cdot\\frac{%s}{%s}=%s\\,\\text{sec}^{-1}"
            .formatted(
                throughputSymbol,
                index,
                totalNumberOfJobs - 1,
                totalNumberOfJobs,
                NumericFormatUtil.formatNumber(visitRatio, true),
                NumericFormatUtil.formatNumber(previousNormalizingConstant, true),
                NumericFormatUtil.formatNumber(normalizingConstant, true),
                NumericFormatUtil.formatNumber(throughput, true));
    UIUtil.renderLaTeXFormula(throughputFormula, performanceMeasure1ImageView);

    final var utilization =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.UTILIZATION)
            .center();
    final String utilizationFormula;
    if (numberOfServers > 1) {
      // U = X / (m * mu)
      utilizationFormula =
          "U_{%d}=\\frac{X_{%d}}{m_{%d}\\mu_{%d}}=\\frac{%s}{%d\\cdot%s}=%s"
              .formatted(
                  index,
                  index,
                  index,
                  index,
                  NumericFormatUtil.formatNumber(throughput, true),
                  numberOfServers,
                  NumericFormatUtil.formatNumber(serviceRate, true),
                  NumericFormatUtil.formatNumber(utilization, true));
    } else {
      // U = X / mu
      utilizationFormula =
          "U_{%d}=\\frac{X_{%d}}{\\mu_{%d}}=\\frac{%s}{%s}=%s"
              .formatted(
                  index,
                  index,
                  index,
                  NumericFormatUtil.formatNumber(throughput, true),
                  NumericFormatUtil.formatNumber(serviceRate, true),
                  NumericFormatUtil.formatNumber(utilization, true));
    }
    UIUtil.renderLaTeXFormula(utilizationFormula, performanceMeasure2ImageView);

    String queueLengthFormula;
    final var queueLengthSymbol =
        resources.getString("performanceMeasures.queueLength.symbol").formatted(index);
    final var queueLength =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.QUEUE_LENGTH)
            .center();
    if (numberOfServers == 1) {
      if (totalNumberOfJobs == 1) {
        // Q = 0
        queueLengthFormula = "%s=0".formatted(queueLengthSymbol);
      } else {
        // Q = 1 / G(K) * sum(2, K, (v/mu)^i * G(K-k))
        queueLengthFormula =
            "%s=\\frac{1}{G\\left(%d\\right)}\\sum_{i=2}^{%d}\\left(\\frac{v_{%d}}{\\mu_{%d}}\\right)^i\\cdot G\\left(%d-i\\right)=\\frac{1}{%s}\\sum_{i=2}^{%d}\\left(\\frac{%s}{%s}\\right)^i\\cdot G\\left(%d-i\\right)=%s"
                .formatted(
                    queueLengthSymbol,
                    totalNumberOfJobs,
                    totalNumberOfJobs,
                    index,
                    index,
                    totalNumberOfJobs,
                    NumericFormatUtil.formatNumber(normalizingConstant, true),
                    totalNumberOfJobs,
                    NumericFormatUtil.formatNumber(visitRatio, true),
                    NumericFormatUtil.formatNumber(serviceRate, true),
                    totalNumberOfJobs,
                    NumericFormatUtil.formatNumber(queueLength, true));
      }
    } else {
      if (totalNumberOfJobs <= numberOfServers) {
        // Q = 0
        queueLengthFormula = "%s=0".formatted(queueLengthSymbol);
      } else {
        // Q = sum(m + 1, K, (k-m) * P(k))
        queueLengthFormula =
            "%s=\\sum_{i=%d}^{%d}(i-%d)\\cdot \\pi\\left(i\\right)=%s"
                .formatted(
                    queueLengthSymbol,
                    numberOfServers + 1,
                    totalNumberOfJobs,
                    numberOfServers,
                    NumericFormatUtil.formatNumber(queueLength, true));
      }
    }
    UIUtil.renderLaTeXFormula(queueLengthFormula, performanceMeasure3ImageView);

    final var waitingTime =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.WAITING_TIME)
            .center();
    final var waitingTimeSymbol =
        resources.getString("performanceMeasures.waitingTime.symbol").formatted(index);
    // W = Q / X
    final var waitingTimeFormula =
        "%s=\\frac{%s}{%s}=\\frac{%s}{%s}=%s\\,\\text{sec}"
            .formatted(
                waitingTimeSymbol,
                queueLengthSymbol,
                throughputSymbol,
                NumericFormatUtil.formatNumber(queueLength, true),
                NumericFormatUtil.formatNumber(throughput, true),
                NumericFormatUtil.formatNumber(waitingTime, true));
    UIUtil.renderLaTeXFormula(waitingTimeFormula, performanceMeasure4ImageView);

    final var responseTimeSymbol =
        resources.getString("performanceMeasures.responseTime.symbol").formatted(index);
    final var responseTime =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.RESPONSE_TIME)
            .center();
    // R = W + 1 / mu
    final var responseTimeFormula =
        "%s=%s+\\frac{1}{\\mu_{%d}}=%s+%s=%s\\,\\text{sec}"
            .formatted(
                responseTimeSymbol,
                waitingTimeSymbol,
                index,
                NumericFormatUtil.formatNumber(queueLength, true),
                NumericFormatUtil.formatNumber(MathUtil.inv(serviceRate), true),
                NumericFormatUtil.formatNumber(responseTime, true));
    UIUtil.renderLaTeXFormula(responseTimeFormula, performanceMeasure5ImageView);

    String numberOfJobsFormula;
    final var numberOfJobsSymbol =
        resources.getString("performanceMeasures.numberOfJobs.symbol").formatted(index);
    final var numberOfJobs =
        model
            .performanceMeasureCollection()
            .getPerformanceMeasure(serviceCenterModel, PerformanceMeasureType.NUM_JOBS)
            .center();
    // J = X * R
    numberOfJobsFormula =
        "%s=X_{%d}\\cdot R_{%d}=%s\\cdot %s=%s"
            .formatted(
                numberOfJobsSymbol,
                index,
                index,
                NumericFormatUtil.formatNumber(throughput, true),
                NumericFormatUtil.formatNumber(responseTime, true),
                NumericFormatUtil.formatNumber(numberOfJobs, true));
    UIUtil.renderLaTeXFormula(numberOfJobsFormula, performanceMeasure6ImageView);
  }

  @Override
  protected void updateStateProbabilityBarChart() {
    final var series = new XYChart.Series<String, Number>();
    final var selectedServiceCenterModel =
        serviceCenterChoiceBox.getSelectionModel().getSelectedItem();

    for (var stateProbabilityPair :
        getModel().stateProbabilities().get(selectedServiceCenterModel)) {
      series
          .getData()
          .add(
              new XYChart.Data<>(
                  String.valueOf(stateProbabilityPair.getKey()), stateProbabilityPair.getValue()));
    }

    stateProbabilityBarChart.getData().clear();
    stateProbabilityBarChart.getData().add(series);

    stateProbabilityBarChart.setBarGap(0);
    stateProbabilityBarChart.setCategoryGap(0);

    for (var j = 0; j < series.getData().size(); j++) {
      final var node = stateProbabilityBarChart.lookup(".data" + j + ".chart-bar");

      node.setStyle(
          "-fx-bar-fill: %s;".formatted(UIUtil.toRGB(selectedServiceCenterModel.getFillColor())));

      UIUtil.installTooltip(
          node, getModel().stateProbabilities().get(selectedServiceCenterModel).get(j).getValue());
    }
  }
}
