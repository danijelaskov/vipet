package dev.askov.vipet.mvc.views.components;

import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;

public final class NonNegativeIntegerTableCell<S> extends TableCell<S, Integer> {

  private final Function<S, Integer> maxValueFunction;
  private final BiConsumer<S, Integer> update;
  private Spinner<Integer> spinner;

  public NonNegativeIntegerTableCell(
      final Function<S, Integer> maxValueFunction, final BiConsumer<S, Integer> update) {
    this.maxValueFunction = maxValueFunction;
    this.update = update;
  }

  @Override
  public void startEdit() {
    super.startEdit();

    setText(null);

    if (spinner == null) {
      final var currentItem = getTableRow().getItem();

      spinner = new Spinner<>(0, maxValueFunction.apply(currentItem), 0);
      spinner.maxWidthProperty().bind(widthProperty().multiply(0.5));
      spinner
          .valueProperty()
          .addListener((value, oldValue, newValue) -> update.accept(currentItem, newValue));
    }

    spinner.getValueFactory().setValue(getItem());
    setGraphic(spinner);
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    setText(getItem().toString());
    setGraphic(null);
  }

  @Override
  public void updateItem(Integer item, boolean empty) {
    super.updateItem(item, empty);

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      if (isEditing()) {
        setText(null);

        if (spinner == null) {
          final var currentItem = getTableRow().getItem();

          spinner = new Spinner<>(0, maxValueFunction.apply(currentItem), 0);
          spinner.maxWidthProperty().bind(widthProperty().multiply(0.5));
          spinner
              .valueProperty()
              .addListener((value, oldValue, newValue) -> update.accept(currentItem, newValue));
        }

        spinner.getValueFactory().setValue(item);
        setGraphic(spinner);
      } else {
        setText(getItem().toString());
        setGraphic(null);
      }
    }
  }
}
