package dev.askov.vipet.mvc.controllers.networkeditor.states;

import static dev.askov.vipet.mvc.views.network.connection.MultiSegmentPathWithArcs.NODE_TO_CONNECTION_DISTANCE;

import dev.askov.vipet.localization.LocalizationManager;
import dev.askov.vipet.mvc.controllers.networkeditor.NetworkEditorController;
import dev.askov.vipet.mvc.models.network.NetworkElementModel;
import dev.askov.vipet.mvc.models.network.NetworkNodeModel;
import dev.askov.vipet.mvc.views.network.connection.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import javafx.scene.input.MouseButton;

public abstract class AddingNode<SomeNetworkNodeModel extends NetworkNodeModel>
    extends NetworkEditorState {

  private static final Map<NetworkEditorController, Map<Class<? extends NetworkNodeModel>, Integer>>
      COUNTERS_MAP = new HashMap<>();

  protected Supplier<SomeNetworkNodeModel> networkNodeModelSupplier;
  protected SomeNetworkNodeModel networkNodeModel;
  protected ResourceBundle localizationBundle;
  protected boolean isNodeTemporarilyAdded;
  protected boolean isNodeAdded;

  @Override
  public void enter() {
    super.enter();

    networkNodeModel = networkNodeModelSupplier.get();
    networkNodeModel.setState(NetworkElementModel.State.OVERLAPPING);

    isNodeAdded = false;

    assignName();

    networkEditorController
        .getScrollPane()
        .setOnMouseEntered(
            event -> {
              networkEditorController.getModel().addNodeModel(networkNodeModel);
              isNodeTemporarilyAdded = true;
            });
    networkEditorController
        .getScrollPane()
        .setOnMouseMoved(
            event -> {
              if (!isNodeTemporarilyAdded) {
                networkEditorController.getModel().addNodeModel(networkNodeModel);
                isNodeTemporarilyAdded = true;
              }
              moveNetworkNode(event.getX(), event.getY());
            });

    networkEditorController
        .getScrollPane()
        .setOnMouseReleased(
            event -> {
              final var networkNode = networkEditorController.getNetworkNode(networkNodeModel);

              if (event.getButton() == MouseButton.PRIMARY
                  && !networkEditorController.getNetwork().hasNodeThatOverlapsWith(networkNode)) {
                networkNodeModel.setState(NetworkNodeModel.State.NORMAL);

                isNodeAdded = true;
                LOGGER.debug("Added node: {}", networkNodeModel.getName());

                isNodeTemporarilyAdded = false;

                networkEditorController.setState(this);
              } else if (event.getButton() != MouseButton.PRIMARY) {
                isNodeAdded = false;

                networkEditorController.setState(new Idle());
              } else {
                LOGGER.debug("Node {} overlaps with another node", networkNodeModel.getName());
              }
            });

    networkEditorController
        .getScrollPane()
        .setOnMouseExited(
            event -> networkEditorController.getModel().removeNodeModel(networkNodeModel));
  }

  protected void moveNetworkNode(final double x, final double y) {
    final var network = networkEditorController.getNetwork();
    final var networkNode = networkEditorController.getNetworkNode(networkNodeModel);

    final var newX = x - 0.5 * networkNodeModel.getWidth() - network.getTranslateX();
    final var newY = y - 0.5 * networkNodeModel.getHeightWithoutText() - network.getTranslateY();

    networkNodeModel.setX(
        Math.max(
            newX, NODE_TO_CONNECTION_DISTANCE + Connection.TRAVELLING_JOB_CIRCLE_RADIUS + 1.0));
    networkNodeModel.setY(
        Math.max(
            newY, NODE_TO_CONNECTION_DISTANCE + Connection.TRAVELLING_JOB_CIRCLE_RADIUS + 1.0));

    if (networkEditorController.getNetwork().hasNodeThatOverlapsWith(networkNode)) {
      networkNodeModel.setState(NetworkElementModel.State.OVERLAPPING);
    } else {
      networkNodeModel.setState(NetworkElementModel.State.CREATING);
    }
  }

  @Override
  public void exit() {
    super.exit();

    if (!isNodeAdded) {
      networkEditorController.getModel().removeNodeModel(networkNodeModel);
      COUNTERS_MAP
          .get(networkEditorController)
          .put(
              networkNodeModel.getClass(),
              COUNTERS_MAP.get(networkEditorController).get(networkNodeModel.getClass()) - 1);
    }

    networkEditorController.getScrollPane().setOnMouseEntered(null);
    networkEditorController.getScrollPane().setOnMouseMoved(null);
    networkEditorController.getScrollPane().setOnMouseReleased(null);
    networkEditorController.getScrollPane().setOnMouseExited(null);
  }

  @Override
  public void setNetworkEditorController(final NetworkEditorController networkEditorController) {
    super.setNetworkEditorController(networkEditorController);

    if (networkEditorController != null) {
      localizationBundle = LocalizationManager.getInstance().getResourceBundle();
      if (!COUNTERS_MAP.containsKey(networkEditorController)) {
        COUNTERS_MAP.put(networkEditorController, new HashMap<>());
      }
    }
  }

  @Override
  protected String getName() {
    return "Adding New Node";
  }

  private void assignName() {
    if (networkEditorController != null) {
      if (!COUNTERS_MAP.get(networkEditorController).containsKey(networkNodeModel.getClass())) {
        COUNTERS_MAP.get(networkEditorController).put(networkNodeModel.getClass(), 1);
      }
      int nodeCounter = COUNTERS_MAP.get(networkEditorController).get(networkNodeModel.getClass());

      String name;
      boolean isTaken;

      do {
        final var currentPotentialName =
            String.format(
                localizationBundle.getString(
                    "networkEditor.default%sName".formatted(networkNodeModel.getType())),
                nodeCounter);

        isTaken =
            networkEditorController.getModel().getNodeModels().stream()
                .anyMatch(
                    currentNetworkNode ->
                        !currentNetworkNode.equals(networkNodeModel)
                            && currentPotentialName.equals(currentNetworkNode.getName()));
        name = currentPotentialName;
        nodeCounter++;
      } while (isTaken);

      networkNodeModel.setName(name);

      COUNTERS_MAP.get(networkEditorController).put(networkNodeModel.getClass(), nodeCounter);
    }
  }
}
