package dev.askov.vipet.mvc.models.network.queue;

import dev.askov.vipet.mvc.models.network.JobModel;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class QueueModel {

  public static final int MIN_QUEUE_CAPACITY = 1;
  public static final int DEFAULT_QUEUE_CAPACITY = MIN_QUEUE_CAPACITY;

  private static final boolean DEFAULT_INFINITE_QUEUE_CAPACITY = true;

  private final BooleanProperty infiniteCapacityProperty;
  private final IntegerProperty capacityProperty;
  private final DoubleProperty visualizationParameterProperty;
  private final LongProperty jobVisualizationParameterProperty;
  private final IntegerProperty initialNumberOfJobsProperty;
  private final ListProperty<QueueDisciplineModel> queueDisciplinesPropertyModel =
      new SimpleListProperty<>(this, "queueDisciplines", null);
  private final ObjectProperty<QueueDisciplineModel> selectedQueueDisciplineProperty;

  private final List<JobModel> jobModelListProperty = new ArrayList<>();

  private final List<Runnable> onQueueSlotAvailableHandlers = new ArrayList<>();

  public QueueModel(final boolean infiniteCapacity, final int capacity) {
    if (capacity < MIN_QUEUE_CAPACITY) {
      throw new IllegalArgumentException("Queue capacity must be at least " + MIN_QUEUE_CAPACITY);
    }

    infiniteCapacityProperty =
        new SimpleBooleanProperty(this, "infiniteCapacity", infiniteCapacity);
    capacityProperty = new SimpleIntegerProperty(this, "capacity", capacity);
    visualizationParameterProperty = new SimpleDoubleProperty(this, "visualizationParameter", 0.0);
    jobVisualizationParameterProperty =
        new SimpleLongProperty(this, "jobVisualizationParameter", 0);
    initialNumberOfJobsProperty = new SimpleIntegerProperty(this, "initialNumberOfJobs", 0);
    queueDisciplinesPropertyModel.set(createQueueDisciplines());
    selectedQueueDisciplineProperty =
        new SimpleObjectProperty<>(
            this, "selectedQueueDiscipline", queueDisciplinesPropertyModel.getFirst());
  }

  public QueueModel() {
    this(DEFAULT_INFINITE_QUEUE_CAPACITY, DEFAULT_QUEUE_CAPACITY);
  }

  public QueueModel(final QueueModel queueModel) {
    infiniteCapacityProperty = queueModel.infiniteCapacityProperty;
    capacityProperty = queueModel.capacityProperty;
    visualizationParameterProperty = queueModel.visualizationParameterProperty;
    jobVisualizationParameterProperty = queueModel.jobVisualizationParameterProperty;
    initialNumberOfJobsProperty =
        new SimpleIntegerProperty(this, "initialNumberOfJobs", queueModel.getInitialNumberOfJobs());
    selectedQueueDisciplineProperty =
        new SimpleObjectProperty<>(
            this,
            "selectedQueueDiscipline",
            copyQueueDiscipline(queueModel.getSelectedQueueDiscipline()));
  }

  public IntegerProperty capacityProperty() {
    return capacityProperty;
  }

  public int getCapacity() {
    return capacityProperty.get();
  }

  public void setCapacity(int queueCapacity) {
    capacityProperty.set(queueCapacity);
  }

  public DoubleProperty visualizationParameterProperty() {
    return visualizationParameterProperty;
  }

  public void setVisualizationParameter(double visualizationParameter) {
    visualizationParameterProperty.set(visualizationParameter);
  }

  public LongProperty jobVisualizationParameterProperty() {
    return jobVisualizationParameterProperty;
  }

  public void setJobVisualizationParameter(final long discreteVisualizationParameter) {
    jobVisualizationParameterProperty.set(discreteVisualizationParameter);
  }

  public IntegerProperty initialNumberOfJobsProperty() {
    return initialNumberOfJobsProperty;
  }

  public int getInitialNumberOfJobs() {
    return initialNumberOfJobsProperty.get();
  }

  public void setInitialNumberOfJobs(final int initialNumberOfJobs) {
    if (initialNumberOfJobs < 0) {
      throw new IllegalArgumentException("Initial number of jobs must be non-negative");
    }
    initialNumberOfJobsProperty.set(initialNumberOfJobs);
  }

  public BooleanProperty infiniteCapacityProperty() {
    return infiniteCapacityProperty;
  }

  public boolean isInfinite() {
    return infiniteCapacityProperty().get();
  }

  public void setInfiniteCapacity(boolean infiniteQueueCapacity) {
    infiniteCapacityProperty.set(infiniteQueueCapacity);
  }

  public boolean isFinite() {
    return !isInfinite();
  }

  public ListProperty<QueueDisciplineModel> queueDisciplinesProperty() {
    return queueDisciplinesPropertyModel;
  }

  public List<QueueDisciplineModel> getQueueDisciplines() {
    return queueDisciplinesPropertyModel.get();
  }

  public void addQueueDiscipline(final QueueDisciplineModel queueDisciplineModel) {
    queueDisciplinesPropertyModel.get().add(queueDisciplineModel);
  }

  public ObjectProperty<QueueDisciplineModel> selectedQueueDisciplineProperty() {
    return selectedQueueDisciplineProperty;
  }

  public QueueDisciplineModel getSelectedQueueDiscipline() {
    return selectedQueueDisciplineProperty.get();
  }

  public void setSelectedQueueDiscipline(final QueueDisciplineModel queueDisciplineModel) {
    selectedQueueDisciplineProperty.set(queueDisciplineModel);
  }

  public List<JobModel> getJobList() {
    return jobModelListProperty;
  }

  public void addJob(final JobModel jobModel) {
    jobModelListProperty.add(jobModel);
  }

  public void removeJob(final JobModel jobModel) {
    if (jobModelListProperty.remove(jobModel) && !onQueueSlotAvailableHandlers.isEmpty()) {
      onQueueSlotAvailableHandlers.getFirst().run();
      onQueueSlotAvailableHandlers.removeFirst();
    }
  }

  public void clear() {
    jobModelListProperty.clear();
  }

  public long getCurrentNumberOfJobs() {
    return jobModelListProperty.size();
  }

  public boolean isEmpty() {
    return jobModelListProperty.isEmpty();
  }

  public void addOnQueueSlotAvailableHandler(final Runnable onQueueSlotAvailable) {
    onQueueSlotAvailableHandlers.add(onQueueSlotAvailable);
  }

  private ObservableList<QueueDisciplineModel> createQueueDisciplines() {
    return FXCollections.observableArrayList(
        new FIFOQueueDisciplineModel(this),
        new LIFOQueueDisciplineModel(this),
        new SIROQueueDisciplineModel(this));
  }

  private QueueDisciplineModel copyQueueDiscipline(
      final QueueDisciplineModel queueDisciplineModel) {
    return switch (queueDisciplineModel) {
      case FIFOQueueDisciplineModel ignored -> new FIFOQueueDisciplineModel(this);
      case LIFOQueueDisciplineModel ignored -> new LIFOQueueDisciplineModel(this);
      case SIROQueueDisciplineModel ignored -> new SIROQueueDisciplineModel(this);
      default -> null;
    };
  }
}
