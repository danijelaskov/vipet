package dev.askov.vipet.mvc.views.components;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.ValidatorUtil;
import java.text.ParseException;
import java.util.function.BiConsumer;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public final class ProbabilityTableCell<S> extends TableCell<S, String> {

  private final TextField textField = new TextField();

  public ProbabilityTableCell(final BiConsumer<S, Double> update) {
    textField.setAlignment(Pos.CENTER);
    textField.maxWidthProperty().bind(widthProperty().multiply(0.5));
    textField
        .textProperty()
        .addListener((text, oldText, newText) -> updateProbability(update, newText));
    textField.setOnAction(event -> updateProbability(update, textField.getText()));
  }

  private void updateProbability(
      final BiConsumer<S, Double> update, final String probabilityString) {
    final var probability = ValidatorUtil.validateProbability(probabilityString);
    if (probability != null) {
      update.accept(getTableRow().getItem(), probability);
    }
  }

  @Override
  public void startEdit() {
    super.startEdit();

    setText(null);

    textField.setText(getItem());
    setGraphic(textField);
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();

    final var probability = ValidatorUtil.validateProbability(textField.getText());
    if (probability != null) {
      setText(textField.getText());
    } else {
      try {
        setText(NumericFormatUtil.formatNumber(NumericFormatUtil.parseDouble(getItem())));
      } catch (ParseException e) {
        setText(textField.getText());
      }
    }

    setGraphic(null);
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    final var hasPercentageSign = item != null && item.contains("%");

    if (hasPercentageSign) {
      item = item.replace("%", "");
    }

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      if (isEditing()) {
        setText(null);
        try {
          textField.setText(
              NumericFormatUtil.formatNumber(NumericFormatUtil.parseDouble(item))
                  + (hasPercentageSign ? "%" : ""));
        } catch (ParseException ignored) {
        }
        setGraphic(textField);
      } else {
        setText(getItem());
        setGraphic(null);
      }
    }
  }
}
