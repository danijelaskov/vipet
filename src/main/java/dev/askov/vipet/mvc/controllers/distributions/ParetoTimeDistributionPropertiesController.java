package dev.askov.vipet.mvc.controllers.distributions;

import dev.askov.vipet.common.FilterUtil;
import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeUnit;
import dev.askov.vipet.mvc.common.LocaleAwareDoubleConverter;
import dev.askov.vipet.mvc.common.TimeUnitAwareNumberConverter;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.models.distributions.ParetoTimeDistributionModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParetoTimeDistributionPropertiesController
    extends AbstractController<ParetoTimeDistributionModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ParetoTimeDistributionPropertiesController.class);

  @FXML private TextField scaleTextField;
  @FXML private ChoiceBox<TimeUnit> scaleTimeUnitChoiceBox;
  @FXML private TextField shapeTextField;

  public ParetoTimeDistributionPropertiesController(
      final ParetoTimeDistributionModel paretoTimeDistribution) {
    super(paretoTimeDistribution);
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    scaleTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    scaleTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    scaleTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedScaleTimeUnit, oldScaleTimeUnit, newScaleTimeUnit) ->
                scaleTextField.setText(
                    NumericFormatUtil.useLocalizedDecimalSeparator(
                        model.getScale() / newScaleTimeUnit.convertToSeconds())));

    scaleTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareDoubleConverter(), model.getScale(), FilterUtil.NON_NEGATIVE_VALUES));
    Bindings.bindBidirectional(
        scaleTextField.textProperty(),
        model.scaleProperty(),
        new TimeUnitAwareNumberConverter(scaleTimeUnitChoiceBox));

    shapeTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareDoubleConverter(), model.getShape(), FilterUtil.NON_NEGATIVE_VALUES));
    Bindings.bindBidirectional(
        shapeTextField.textProperty(),
        model.shapeProperty(),
        new StringConverter<>() {

          @Override
          public String toString(Number modelShape) {
            return NumericFormatUtil.useLocalizedDecimalSeparator(modelShape.doubleValue());
          }

          @Override
          public Number fromString(String shapeTextFieldValue) {
            shapeTextFieldValue = NumericFormatUtil.useDefaultDecimalSeparator(shapeTextFieldValue);
            try {
              return Double.parseDouble(shapeTextFieldValue);
            } catch (NumberFormatException e) {
              return null;
            }
          }
        });
  }
}
