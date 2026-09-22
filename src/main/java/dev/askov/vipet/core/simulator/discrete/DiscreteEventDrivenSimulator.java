package dev.askov.vipet.core.simulator.discrete;

import dev.askov.vipet.common.ConcurrentUtil;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import dev.askov.vipet.core.simulator.Simulator;
import dev.askov.vipet.core.simulator.sampling.analysis.SampleAnalyzer;
import dev.askov.vipet.core.simulator.sampling.analysis.StandardSampleAnalyzer;
import dev.askov.vipet.core.simulator.sampling.generation.SampleGenerator;
import dev.askov.vipet.core.simulator.sampling.generation.network.NumberOfJobsNetworkSampleGenerator;
import dev.askov.vipet.core.simulator.sampling.generation.network.ResponseTimeNetworkSampleGenerator;
import dev.askov.vipet.core.simulator.sampling.generation.network.ThroughputNetworkSampleGenerator;
import dev.askov.vipet.core.simulator.sampling.generation.servicecenter.*;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import dev.askov.vipet.mvc.models.simulation.GuidedDiscreteEventDrivenSimulationModel;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.apache.commons.math3.random.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiscreteEventDrivenSimulator extends Simulator {

  public static final List<Class<? extends RandomGenerator>> AVAILABLE_RANDOM_GENERATORS =
      List.of(
          Well512a.class,
          Well1024a.class,
          Well19937a.class,
          Well19937c.class,
          Well44497a.class,
          Well44497b.class,
          MersenneTwister.class);
  public static final double DEFAULT_DISCARD_RATIO = 0.0;
  public static final double DEFAULT_CONFIDENCE_LEVEL = 0.95;

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscreteEventDrivenSimulator.class);
  private static final int DEFAULT_NUMBER_OF_SIMULATION_RUNS = 5;
  private static final int DEFAULT_MAX_NUMBER_OF_EVENTS = 1_000_000;
  private static Class<? extends RandomGenerator> DEFAULT_RANDOM_GENERATOR_TYPE =
      AVAILABLE_RANDOM_GENERATORS.getFirst();
  private static long DEFAULT_SEED = 1000;

  private final IntegerProperty numberOfRunsProperty =
      new SimpleIntegerProperty(this, "numberOfRuns", DEFAULT_NUMBER_OF_SIMULATION_RUNS);
  private final IntegerProperty maxNumberOfEventsProperty =
      new SimpleIntegerProperty(this, "maxNumberOfEvents", DEFAULT_MAX_NUMBER_OF_EVENTS);
  private final BooleanProperty waitUntilAllJobsAreDoneProperty =
      new SimpleBooleanProperty(this, "waitUntilAllJobsAreDone", false);
  private final BooleanProperty guidedSimulationProperty =
      new SimpleBooleanProperty(this, "guidedSimulation", false);
  private final ReadOnlyObjectWrapper<GuidedDiscreteEventDrivenSimulationModel>
      guidedSimulationModelPropertyWrapper =
          new ReadOnlyObjectWrapper<>(this, "guidedSimulationModel");
  private final ObjectProperty<Class<? extends RandomGenerator>> randomGeneratorTypeProperty =
      new SimpleObjectProperty<>(this, "randomGeneratorType", DEFAULT_RANDOM_GENERATOR_TYPE);
  private final LongProperty seedProperty = new SimpleLongProperty(this, "seed", DEFAULT_SEED);
  private final DoubleProperty confidenceLevelProperty =
      new SimpleDoubleProperty(this, "confidenceLevel", DEFAULT_CONFIDENCE_LEVEL);
  private final DoubleProperty sampleDiscardRatioProperty =
      new SimpleDoubleProperty(this, "sampleDiscardRatio", DEFAULT_DISCARD_RATIO);

  private final List<DiscreteEventDrivenSimulation> simulations = new ArrayList<>();
  private final List<List<SampleGenerator<?>>> sampleGenerators = new ArrayList<>();
  private final List<ReadOnlyDoubleProperty> progressList = new ArrayList<>();

  private final List<SampleAnalyzer<ServiceCenterModel>> serviceCenterSampleAnalyzers =
      new ArrayList<>();
  private final List<SampleAnalyzer<NetworkModel>> networkSampleAnalyzers = new ArrayList<>();

  private int currentSimulationIndex = 0;
  private Runnable onNextGuidedSimulation = () -> {};

  private CountDownLatch numberOfFinishedRuns;
  private ExecutorService executorService;

  public DiscreteEventDrivenSimulator(final NetworkModel networkModel) {
    super(networkModel);
  }

  public DiscreteEventDrivenSimulator(
      final NetworkModel networkModel, final double confidenceLevel, final int maxNumberOfEvents) {
    this(networkModel);

    confidenceLevelProperty.set(confidenceLevel);
    maxNumberOfEventsProperty.set(maxNumberOfEvents);
  }

  public int getNumberOfRuns() {
    return numberOfRunsProperty.get();
  }

  public void setNumberOfRuns(final int numberOfRuns) {
    this.numberOfRunsProperty.set(numberOfRuns);
  }

  public int getMaxNumberOfEvents() {
    return maxNumberOfEventsProperty.get();
  }

  public void setMaxNumberOfEvents(final int maxNumberOfEvents) {
    this.maxNumberOfEventsProperty.set(maxNumberOfEvents);
  }

  public boolean getWaitUntilAllJobsAreDone() {
    return waitUntilAllJobsAreDoneProperty.get();
  }

  public BooleanProperty guidedSimulationProperty() {
    return guidedSimulationProperty;
  }

  public boolean isGuidedSimulation() {
    return guidedSimulationProperty.get();
  }

  public ObjectProperty<Class<? extends RandomGenerator>> randomGeneratorTypeProperty() {
    return randomGeneratorTypeProperty;
  }

  public Class<? extends RandomGenerator> getRandomGeneratorType() {
    return randomGeneratorTypeProperty.get();
  }

  public LongProperty seedProperty() {
    return seedProperty;
  }

  public long getSeed() {
    return seedProperty.get();
  }

  public void setSeed(final long seed) {
    seedProperty.set(seed);
  }

  public DoubleProperty confidenceLevelProperty() {
    return confidenceLevelProperty;
  }

  public double getConfidenceLevel() {
    return confidenceLevelProperty.get();
  }

  public void setConfidenceLevel(final double confidenceLevel) {
    confidenceLevelProperty.set(confidenceLevel);
  }

  public DoubleProperty sampleDiscardRatioProperty() {
    return sampleDiscardRatioProperty;
  }

  public double getSampleDiscardRatio() {
    return sampleDiscardRatioProperty.get();
  }

  public void setSampleDiscardRatio(final double warmupDiscardRatio) {
    sampleDiscardRatioProperty.set(warmupDiscardRatio);
  }

  public static Class<? extends RandomGenerator> getDefaultRandomGeneratorType() {
    return DEFAULT_RANDOM_GENERATOR_TYPE;
  }

  public static void setDefaultRandomGeneratorType(
      Class<? extends RandomGenerator> defaultRandomGeneratorType) {
    DEFAULT_RANDOM_GENERATOR_TYPE = defaultRandomGeneratorType;
  }

  public static long getDefaultSeed() {
    return DEFAULT_SEED;
  }

  public static void setDefaultSeed(long defaultSeed) {
    DEFAULT_SEED = defaultSeed;
  }

  @Override
  public Task<PerformanceMeasureCollection> createTask() {
    return new Task<>() {

      @Override
      protected PerformanceMeasureCollection call()
          throws NoSuchMethodException,
              InvocationTargetException,
              InstantiationException,
              IllegalAccessException {
        simulations.clear();
        sampleGenerators.clear();
        progressList.clear();

        serviceCenterSampleAnalyzers.clear();
        networkSampleAnalyzers.clear();

        currentSimulationIndex = 0;

        numberOfFinishedRuns = new CountDownLatch(getNumberOfRuns());

        updateProgress(0.0, 1.0);

        setStartTime(System.nanoTime());

        executorService = ConcurrentUtil.getExecutorService(ConcurrentUtil.TaskType.CPU_BOUND);

        for (var i = 0; i < getNumberOfRuns(); i++) {
          final var networkModel = new NetworkModel(getNetworkModel());
          networkModel.setRandomGenerator(
              getRandomGeneratorType().getConstructor(long.class).newInstance(getSeed() + i));
          final var currentNetworkSampleGenerators = createSampleGenerators(networkModel);
          final var simulation =
              createDiscreteEventDrivenSimulation(
                  i, networkModel, isGuidedSimulation(), currentNetworkSampleGenerators);

          simulations.add(simulation);
          sampleGenerators.add(currentNetworkSampleGenerators);
          progressList.add(simulation.progressProperty());
          executorService.submit(simulation);
        }

        progressList.forEach(
            progressProperty ->
                progressProperty.addListener(
                    (progress, oldProgress, newProgress) ->
                        updateProgress(
                            progressList.stream()
                                .mapToDouble(ReadOnlyDoubleProperty::get)
                                .average()
                                .orElse(0.0),
                            1.0)));

        if (isGuidedSimulation()) {
          onNextGuidedSimulation.run();
        }

        try {
          executorService.shutdown();

          while (numberOfFinishedRuns.getCount() > 0 && !isCancelled()) {
            try {
              numberOfFinishedRuns.await(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
            }
          }

          setEndTime(System.nanoTime());

          if (isCancelled()) {
            LOGGER.debug("Simulation task was cancelled, returning null");
            return null;
          }

          final List<SampleGenerator<?>> sampleGeneratorList = new ArrayList<>();
          for (final var sampleGeneratorSublist : sampleGenerators) {
            sampleGeneratorList.addAll(sampleGeneratorSublist);
          }

          for (final var serviceCenterModel : getNetworkModel().getServiceCenterModels()) {
            for (final var performanceMeasureType : PerformanceMeasureType.values()) {
              serviceCenterSampleAnalyzers.add(
                  new StandardSampleAnalyzer<>(
                      sampleGeneratorList.stream()
                          .filter(
                              sampleGenerator ->
                                  sampleGenerator.getNamedModel()
                                          instanceof ServiceCenterModel model
                                      && Objects.equals(
                                          model.getName(), serviceCenterModel.getName()))
                          .filter(
                              sampleGenerator ->
                                  sampleGenerator.getPerformanceMeasureType()
                                      == performanceMeasureType)
                          .toList(),
                      getSampleDiscardRatio(),
                      getConfidenceLevel(),
                      serviceCenterModel,
                      performanceMeasureType));
            }
          }
          for (final var performanceMeasureType : PerformanceMeasureType.values()) {
            final var networkSampleGenerators =
                sampleGeneratorList.stream()
                    .filter(
                        sampleGenerator -> sampleGenerator.getNamedModel() instanceof NetworkModel)
                    .filter(
                        sampleGenerator ->
                            sampleGenerator.getPerformanceMeasureType() == performanceMeasureType)
                    .toList();
            if (networkSampleGenerators.isEmpty()) {
              continue;
            }
            networkSampleAnalyzers.add(
                new StandardSampleAnalyzer<>(
                    networkSampleGenerators,
                    getSampleDiscardRatio(),
                    getConfidenceLevel(),
                    getNetworkModel(),
                    performanceMeasureType));
          }

          final var results = new PerformanceMeasureCollection(getNetworkModel());

          for (final var sampleAnalyzer : serviceCenterSampleAnalyzers) {
            results.setPerformanceMeasure(
                sampleAnalyzer.getEntity(),
                sampleAnalyzer.getPerformanceIndicatorType(),
                sampleAnalyzer.calculatePerformanceMeasure());
          }
          for (final var sampleAnalyzer : networkSampleAnalyzers) {
            results.setSystemPerformanceMeasure(
                sampleAnalyzer.getPerformanceIndicatorType(),
                sampleAnalyzer.calculatePerformanceMeasure());
          }

          return results;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private List<SampleGenerator<?>> createSampleGenerators(final NetworkModel networkModel) {
    final List<SampleGenerator<?>> sampleGenerators = new ArrayList<>();

    for (final var serviceCenterModel : networkModel.getServiceCenterModels()) {
      sampleGenerators.add(
          new UtilizationServiceCenterSampleGenerator(getMaxSampleSize(), serviceCenterModel));
      sampleGenerators.add(
          new NumberOfJobsServiceCenterSampleGenerator(getMaxSampleSize(), serviceCenterModel));
      sampleGenerators.add(
          new ThroughputServiceCenterSampleGenerator(getMaxSampleSize(), serviceCenterModel));
      sampleGenerators.add(
          new ResponseTimeServiceCenterSampleGenerator(getMaxSampleSize(), serviceCenterModel));
      sampleGenerators.add(
          new QueueLengthServiceCenterSampleGenerator(getMaxSampleSize(), serviceCenterModel));
      sampleGenerators.add(
          new WaitingTimeServiceCenterSampleGenerator(getMaxSampleSize(), serviceCenterModel));
    }
    sampleGenerators.add(new ThroughputNetworkSampleGenerator(getMaxSampleSize(), networkModel));
    sampleGenerators.add(new NumberOfJobsNetworkSampleGenerator(getMaxSampleSize(), networkModel));
    sampleGenerators.add(new ResponseTimeNetworkSampleGenerator(getMaxSampleSize(), networkModel));

    return sampleGenerators;
  }

  private DiscreteEventDrivenSimulation createDiscreteEventDrivenSimulation(
      final int id,
      final NetworkModel networkModel,
      final boolean isGuided,
      final List<SampleGenerator<?>> currentNetworkSampleGenerators) {
    final var simulation =
        new DiscreteEventDrivenSimulation(
            id,
            networkModel,
            isGuided,
            getMaxDuration(),
            getMaxSimulatedTime(),
            getMaxSampleSize(),
            getMaxNumberOfEvents(),
            getWaitUntilAllJobsAreDone(),
            currentNetworkSampleGenerators);

    simulation.setOnSucceeded(
        workerStateEvent -> {
          numberOfFinishedRuns.countDown();
          currentSimulationIndex++;

          if (isGuidedSimulation() && currentSimulationIndex < getNumberOfRuns()) {
            onNextGuidedSimulation.run();
          }
        });
    simulation.setOnFailed(
        workerStateEvent -> {
          numberOfFinishedRuns.countDown();
          currentSimulationIndex++;

          if (isGuidedSimulation() && currentSimulationIndex < getNumberOfRuns()) {
            onNextGuidedSimulation.run();
          }

          LOGGER.error(
              "Simulation failed: {}", workerStateEvent.getSource().getException().getMessage());
          for (final var stackTraceElement :
              workerStateEvent.getSource().getException().getStackTrace()) {
            LOGGER.error("\t{}", stackTraceElement);
          }
        });
    simulation.setOnCancelled(
        workerStateEvent -> {
          numberOfFinishedRuns.countDown();
          currentSimulationIndex++;
          LOGGER.debug("Simulation {} was cancelled", simulation.getID());
        });

    return simulation;
  }

  public GuidedDiscreteEventDrivenSimulationModel createNextSimulationModel() {
    final var guidedSimulationModel =
        new GuidedDiscreteEventDrivenSimulationModel(simulations.get(currentSimulationIndex));
    guidedSimulationModelPropertyWrapper.set(guidedSimulationModel);
    return guidedSimulationModel;
  }

  public ReadOnlyProperty<GuidedDiscreteEventDrivenSimulationModel>
      guidedSimulationModelProperty() {
    return guidedSimulationModelPropertyWrapper.getReadOnlyProperty();
  }

  public GuidedDiscreteEventDrivenSimulationModel getGuidedSimulationModel() {
    return guidedSimulationModelPropertyWrapper.get();
  }

  public List<SampleGenerator<?>> getSampleGenerators(final int runIndex) {
    return sampleGenerators.get(runIndex);
  }

  public void setOnNextGuidedSimulation(final Runnable onNextGuidedSimulation) {
    this.onNextGuidedSimulation = onNextGuidedSimulation;
  }

  @Override
  public boolean cancel() {
    LOGGER.debug("Cancelling simulator and all running simulations");

    for (final var simulation : simulations) {
      if (simulation != null) {
        if (!simulation.isDone()) {
          simulation.cancel();
        }
      }
    }

    if (executorService != null && !executorService.isShutdown()) {
      executorService.shutdownNow();
    }

    return super.cancel();
  }
}
