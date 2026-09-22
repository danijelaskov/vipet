package dev.askov.vipet.mvc.common;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.common.UIUtil;
import dev.askov.vipet.localization.LocalizationManager;
import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import dev.askov.vipet.mvc.models.network.NetworkElementModel;
import dev.askov.vipet.mvc.models.network.NetworkNodeModel;
import java.io.IOException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SelectedNetworkElementModelChangeListener
    implements ChangeListener<NetworkElementModel> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SelectedNetworkElementModelChangeListener.class);
  private static final LocalizationManager LOCALIZATION_MANAGER = LocalizationManager.getInstance();

  private final NetworkEditorController networkEditorController;
  private final ObservableList<MenuItem> networkNodeMenuItems;

  public SelectedNetworkElementModelChangeListener(
      final NetworkEditorController networkEditorController,
      final ObservableList<MenuItem> networkNodeMenuItems) {
    this.networkEditorController = networkEditorController;
    this.networkNodeMenuItems = networkNodeMenuItems;

    updateNetworkNodeMenuItems(
        this.networkEditorController.getModel().getSelectedNetworkElementModel());
  }

  @Override
  public void changed(
      ObservableValue<? extends NetworkElementModel> selectedNetworkElementModel,
      NetworkElementModel oldSelectedNetworkElementModel,
      NetworkElementModel newSelectedNetworkElementModel) {
    updateNetworkNodeMenuItems(newSelectedNetworkElementModel);
  }

  private void updateNetworkNodeMenuItems(
      final NetworkElementModel newSelectedNetworkElementModel) {
    if (newSelectedNetworkElementModel != null) {
      networkNodeMenuItems.clear();

      final var networkNodeMenuItems =
          UIUtil.createNetworkNodePropertiesMenuItems(newSelectedNetworkElementModel);

      if (newSelectedNetworkElementModel instanceof NetworkNodeModel networkNodeModel) {
        networkNodeMenuItems.stream()
            .filter(networkNodeMenuItem -> "propertiesMenuItem".equals(networkNodeMenuItem.getId()))
            .findFirst()
            .ifPresent(
                networkNodeMenuItem ->
                    networkNodeMenuItem.setOnAction(
                        actionEvent -> {
                          try {
                            UIUtil.loadModalWindow(
                                "NetworkNodeProperties",
                                VIPET.getPrimaryStage(),
                                networkEditorController.getModel().getName(),
                                networkNodeModel.getName());
                          } catch (IOException e) {
                            LOGGER.error("Cannot load network node properties window", e);
                          }
                        }));
      }
      networkNodeMenuItems.stream()
          .filter(menuItem -> "removeMenuItem".equals(menuItem.getId()))
          .findFirst()
          .ifPresent(
              menuItem ->
                  menuItem.setOnAction(
                      actionEvent -> {
                        if (newSelectedNetworkElementModel
                            instanceof NetworkNodeModel newNetworkNodeModel) {
                          final var nodeName = newNetworkNodeModel.getName();
                          final var alert =
                              UIUtil.createAlert(
                                  Alert.AlertType.CONFIRMATION,
                                  networkEditorController.getNetwork().getScene().getWindow(),
                                  "edit.networkNode.delete.confirmation.title",
                                  "edit.networkNode.delete.confirmation.header");

                          alert.setContentText(
                              LOCALIZATION_MANAGER.getString(
                                  "edit.networkNode.delete.confirmation.content", nodeName));

                          final var result = alert.showAndWait();
                          if (result.isPresent()
                              && result.get().getButtonData() == ButtonType.OK.getButtonData()) {
                            networkEditorController
                                .getModel()
                                .removeNetworkElementModel(newSelectedNetworkElementModel);
                          }
                        } else {
                          networkEditorController
                              .getModel()
                              .removeNetworkElementModel(newSelectedNetworkElementModel);
                        }
                      }));

      this.networkNodeMenuItems.addAll(networkNodeMenuItems);
    }
  }
}
