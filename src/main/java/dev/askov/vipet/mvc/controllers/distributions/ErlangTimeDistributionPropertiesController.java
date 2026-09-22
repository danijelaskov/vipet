package dev.askov.vipet.mvc.controllers.distributions;

import dev.askov.vipet.common.FilterUtil;
import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeUnit;
import dev.askov.vipet.mvc.common.LocaleAwareDoubleConverter;
import dev.askov.vipet.mvc.common.LocaleAwareIntegerConverter;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.models.distributions.ErlangTimeDistributionModel;
import java.text.ParseException;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErlangTimeDistributionPropertiesController
    extends AbstractController<ErlangTimeDistributionModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ErlangTimeDistributionPropertiesController.class);

  @FXML private TextField rateTextField;
  @FXML private ChoiceBox<TimeUnit> rateTimeUnitChoiceBox;
  @FXML private TextField scaleTextField;
  @FXML private ChoiceBox<TimeUnit> scaleTimeUnitChoiceBox;
  @FXML private TextField shapeTextField;

  public ErlangTimeDistributionPropertiesController(final ErlangTimeDistributionModel model) {
    super(model);
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    rateTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    rateTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    rateTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedRateTimeUnit, oldRateTimeUnit, newRateTimeUnit) ->
                rateTextField.setText(
                    NumericFormatUtil.useLocalizedDecimalSeparator(
                        model.getRate() * newRateTimeUnit.convertToSeconds())));

    rateTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareDoubleConverter(), model.getRate(), FilterUtil.NON_NEGATIVE_VALUES));
    Bindings.bindBidirectional(
        rateTextField.textProperty(),
        model.rateProperty(),
        new StringConverter<>() {

          @Override
          public String toString(Number modelRate) {
            return NumericFormatUtil.useLocalizedDecimalSeparator(
                modelRate.doubleValue() * rateTimeUnitChoiceBox.getValue().convertToSeconds());
          }

          @Override
          public Number fromString(String rateTextFieldValue) {
            rateTextFieldValue = NumericFormatUtil.useDefaultDecimalSeparator(rateTextFieldValue);
            try {
              return Double.parseDouble(rateTextFieldValue)
                  / rateTimeUnitChoiceBox.getValue().convertToSeconds();
            } catch (NumberFormatException e) {
              return null;
            }
          }
        });

    scaleTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    scaleTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    scaleTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedScaleTimeUnit, oldScaleTimeUnit, newScaleTimeUnit) ->
                scaleTextField.setText(
                    NumericFormatUtil.formatNumber(
                        model.getScale() / newScaleTimeUnit.convertToSeconds())));

    scaleTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareDoubleConverter(), model.getScale(), FilterUtil.NON_NEGATIVE_VALUES));
    Bindings.bindBidirectional(
        scaleTextField.textProperty(),
        model.rateProperty(),
        new StringConverter<>() {

          @Override
          public String toString(Number modelRate) {
            return NumericFormatUtil.useLocalizedDecimalSeparator(
                (1.0 / modelRate.doubleValue())
                    / scaleTimeUnitChoiceBox.getValue().convertToSeconds());
          }

          @Override
          public Number fromString(String scaleTextFieldValue) {
            scaleTextFieldValue = NumericFormatUtil.useDefaultDecimalSeparator(scaleTextFieldValue);
            try {
              return 1.0
                  / (Double.parseDouble(scaleTextFieldValue)
                      * scaleTimeUnitChoiceBox.getValue().convertToSeconds());
            } catch (NumberFormatException e) {
              return null;
            }
          }
        });

    shapeTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareIntegerConverter(), model.getShape(), FilterUtil.NON_NEGATIVE_VALUES));
    Bindings.bindBidirectional(
        shapeTextField.textProperty(),
        model.shapeProperty(),
        new StringConverter<>() {

          @Override
          public String toString(Number modelShape) {
            return NumericFormatUtil.formatNumber(modelShape.intValue());
          }

          @Override
          public Number fromString(String shapeTextFieldValue) {
            try {
              return NumericFormatUtil.parseInteger(shapeTextFieldValue);
            } catch (ParseException e) {
              return null;
            }
          }
        });
  }
}
