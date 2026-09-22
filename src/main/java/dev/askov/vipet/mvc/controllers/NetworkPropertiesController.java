package dev.askov.vipet.mvc.controllers;

import dev.askov.vipet.common.ValidatorUtil;
import dev.askov.vipet.mvc.common.IntegerSpinnerValueFactory;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

public final class NetworkPropertiesController extends AbstractController<NetworkModel> {

  private static final int MIN_NUMBER_OF_JOBS = 1;

  @FXML private TextField nameTextField;
  @FXML private TextArea descriptionTextArea;
  @FXML private Label totalNumberOfJobsLabel;
  @FXML private Spinner<Integer> totalNumberOfJobsSpinner;
  @FXML private Label centralServiceCenterLabel;
  @FXML private ChoiceBox<ServiceCenterModel> centralServiceCenterChoiceBox;

  public NetworkPropertiesController(NetworkModel model) {
    super(model);
  }

  @Override
  protected void initialize() {
    nameTextField.setText(model.getName());
    descriptionTextArea.textProperty().bindBidirectional(model.descriptionProperty());

    totalNumberOfJobsLabel
        .disableProperty()
        .bind(model.openProperty().or(model.nodesProperty().emptyProperty()));

    final var atLeastOneInfiniteCapacityNode =
        model.getServiceCenterModels().stream()
            .anyMatch(serviceCenterModel -> serviceCenterModel.getQueueModel().isInfinite());
    final var networkCapacity =
        atLeastOneInfiniteCapacityNode
            ? Integer.MAX_VALUE
            : model.getServiceCenterModels().stream()
                .mapToInt(ServiceCenterModel::getCapacity)
                .sum();

    final var maxValue = Math.max(MIN_NUMBER_OF_JOBS, networkCapacity);

    totalNumberOfJobsSpinner.setValueFactory(
        new IntegerSpinnerValueFactory(MIN_NUMBER_OF_JOBS, maxValue));
    totalNumberOfJobsSpinner.getValueFactory().setValue(model.getNumberOfJobs());
    totalNumberOfJobsSpinner
        .valueProperty()
        .addListener(
            (totalNumberOfJobs, oldTotalNumberOfJobs, newTotalNumberOfJobs) ->
                model.setNumberOfJobs(newTotalNumberOfJobs));

    totalNumberOfJobsSpinner
        .disableProperty()
        .bind(model.openProperty().or(model.nodesProperty().emptyProperty()));

    centralServiceCenterLabel
        .disableProperty()
        .bind(model.openProperty().or(model.nodesProperty().emptyProperty()));
    centralServiceCenterChoiceBox
        .disableProperty()
        .bind(model.openProperty().or(model.nodesProperty().emptyProperty()));
    centralServiceCenterChoiceBox.getItems().addAll(model.getServiceCenterModels());
    centralServiceCenterChoiceBox.setConverter(
        new StringConverter<>() {

          @Override
          public String toString(final ServiceCenterModel serviceCenterModel) {
            return serviceCenterModel != null ? serviceCenterModel.getName() : "";
          }

          @Override
          public ServiceCenterModel fromString(final String string) {
            if (string == null || string.isBlank()) {
              return null;
            } else {
              return model.getServiceCenterModels().stream()
                  .filter(serviceCenter -> serviceCenter.getName().equals(string))
                  .findFirst()
                  .orElse(null);
            }
          }
        });

    final var centralServiceCenter = model.getCentralServiceCenterModel();

    if (centralServiceCenter != null && !centralServiceCenterChoiceBox.getItems().isEmpty()) {
      centralServiceCenterChoiceBox.getSelectionModel().select(centralServiceCenter);
    }

    model
        .centralServiceCenterModelProperty()
        .bind(centralServiceCenterChoiceBox.getSelectionModel().selectedItemProperty());
  }

  @Override
  public void onWindowClosed(final WindowEvent event) {
    if (ValidatorUtil.validateNameFormat(nameTextField.getText())) {
      model.setName(nameTextField.getText());
    } else {
      final var alert = new Alert(Alert.AlertType.ERROR);

      alert.setTitle(resources.getString("networkProperties.errorAlert.title"));
      alert.setHeaderText(resources.getString("networkProperties.errorAlert.headerText"));
      alert.setContentText(
          resources.getString("networkProperties.errorAlert.contentText.invalidName"));

      alert.showAndWait();

      event.consume();
    }
  }
}
