package dev.askov.vipet.mvc.controllers.distributions;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeUnit;
import dev.askov.vipet.mvc.common.LocaleAwareDoubleConverter;
import dev.askov.vipet.mvc.common.TimeUnitAwareNumberConverter;
import dev.askov.vipet.mvc.controllers.AbstractController;
import dev.askov.vipet.mvc.models.distributions.DeterministicTimeDistributionModel;
import java.text.ParseException;
import java.util.function.UnaryOperator;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import org.slf4j.Logger;

public final class DeterministicTimeDistributionPropertiesController
    extends AbstractController<DeterministicTimeDistributionModel> {

  private static final Logger LOGGER =
      org.slf4j.LoggerFactory.getLogger(DeterministicTimeDistributionPropertiesController.class);
  private static final UnaryOperator<TextFormatter.Change> NON_NEGATIVE_VALUE_FILTER =
      change -> {
        try {
          var newValue = NumericFormatUtil.parseDouble(change.getControlNewText());
          if (newValue > 0.0) {
            return change;
          }
        } catch (ParseException e) {
          return null;
        }
        return null;
      };

  @FXML private TextField valueTextField;
  @FXML private ChoiceBox<TimeUnit> valueTimeUnitChoiceBox;

  public DeterministicTimeDistributionPropertiesController(
      final DeterministicTimeDistributionModel model) {
    super(model);
  }

  @Override
  protected void initialize() {
    LOGGER.debug("Initializing");

    valueTimeUnitChoiceBox.getItems().addAll(TimeUnit.values());
    valueTimeUnitChoiceBox.getSelectionModel().select(TimeUnit.SECOND);
    valueTimeUnitChoiceBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (selectedValueTimeUnit, oldValueTimeUnit, newValueTimeUnit) ->
                valueTextField.setText(
                    NumericFormatUtil.useLocalizedDecimalSeparator(
                        model.getValue() / newValueTimeUnit.convertToSeconds())));

    valueTextField.setTextFormatter(
        new TextFormatter<>(
            new LocaleAwareDoubleConverter(), model.getValue(), NON_NEGATIVE_VALUE_FILTER));
    Bindings.bindBidirectional(
        valueTextField.textProperty(),
        model.valueProperty(),
        new TimeUnitAwareNumberConverter(valueTimeUnitChoiceBox));
  }
}
