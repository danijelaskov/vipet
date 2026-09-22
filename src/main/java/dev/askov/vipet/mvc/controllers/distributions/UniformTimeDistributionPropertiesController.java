package dev.askov.vipet.mvc.controllers.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeUnit;
import dev.askov.vipet.mvc.common.LocaleAwareDoubleConverter;
import dev.askov.vipet.mvc.common.TimeUnitAwareNumberConverter;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.models.distributions.UniformTimeDistributionModel;
import java.text.ParseException;
import java.util.function.UnaryOperator;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UniformTimeDistributionPropertiesController
    extends AbstractController<UniformTimeDistributionModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(UniformTimeDistributionPropertiesController.class);

  private final UnaryOperator<TextFormatter.Change> minInputFilter =
      change -> {
        try {
          var newValue = NumericFormatUtil.parseDouble(change.getControlNewText());
          if (newValue >= 0.0 && newValue < model.getMax()) {
            return change;
          }
        } catch (ParseException e) {
          return null;
        }
        return null;
      };
  private final UnaryOperator<TextFormatter.Change> maxInputFilter =
      change -> {
        try {
          var newValue = NumericFormatUtil.parseDouble(change.getControlNewText());
          if (newValue > model.getMin()) {
            return change;
          }
        } catch (ParseException e) {
          return null;
        }
        return null;
      };

  @FXML private TextField minTextField;
  @FXML private ChoiceBox<TimeUnit> minTimeUnitChoiceBox;
  @FXML private TextField maxTextField;
  @FXML private ChoiceBox<TimeUnit> maxTimeUnitChoiceBox;

  public UniformTimeDistributionPropertiesController(final UniformTimeDistributionModel model) {
    super(model);
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    minTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    minTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    minTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedMinTimeUnit, oldMinTimeUnit, newMinTimeUnit) ->
                minTextField.setText(
                    NumericFormatUtil.useLocalizedDecimalSeparator(
                        model.getMin() / newMinTimeUnit.convertToSeconds())));

    minTextField.setTextFormatter(
        new TextFormatter<>(new LocaleAwareDoubleConverter(), model.getMin(), minInputFilter));
    Bindings.bindBidirectional(
        minTextField.textProperty(),
        model.minProperty(),
        new TimeUnitAwareNumberConverter(minTimeUnitChoiceBox));

    maxTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    maxTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    maxTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedMaxTimeUnit, oldMaxTimeUnit, newMaxTimeUnit) ->
                maxTextField.setText(
                    NumericFormatUtil.useLocalizedDecimalSeparator(
                        model.getMax() / newMaxTimeUnit.convertToSeconds())));

    maxTextField.setTextFormatter(
        new TextFormatter<>(new LocaleAwareDoubleConverter(), model.getMax(), maxInputFilter));
    Bindings.bindBidirectional(
        maxTextField.textProperty(),
        model.maxProperty(),
        new TimeUnitAwareNumberConverter(maxTimeUnitChoiceBox));
  }
}
