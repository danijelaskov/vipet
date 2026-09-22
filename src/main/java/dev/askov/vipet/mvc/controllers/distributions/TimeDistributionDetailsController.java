package dev.askov.vipet.mvc.controllers.distributions;

import dev.askov.vipet.common.LaTeXUtil;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.models.distributions.TimeDistributionModel;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TimeDistributionDetailsController
    extends AbstractController<TimeDistributionModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TimeDistributionDetailsController.class);

  @FXML private ImageView pdfEquationImageView;
  @FXML private ImageView cdfEquationImageView;

  public TimeDistributionDetailsController(final TimeDistributionModel model) {
    super(model);
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    pdfEquationImageView.setImage(LaTeXUtil.createImageFromLaTeX(model.getPDFLaTeXFormula()));
    cdfEquationImageView.setImage(LaTeXUtil.createImageFromLaTeX(model.getCDFLaTeXFormula()));
  }
}
