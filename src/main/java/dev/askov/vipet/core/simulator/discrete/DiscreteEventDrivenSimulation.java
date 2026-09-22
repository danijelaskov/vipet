package dev.askov.vipet.core.simulator.discrete;

import dev.askov.vipet.common.NumericFormatUtil;
import dev.askov.vipet.common.TimeFormatUtil;
import dev.askov.vipet.core.simulator.Simulation;
import dev.askov.vipet.core.simulator.sampling.generation.SampleGenerator;
import dev.askov.vipet.mvc.models.network.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiscreteEventDrivenSimulation extends Simulation {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscreteEventDrivenSimulation.class);

  private final EventQueue eventQueue = new EventQueue();
  private Event currentEvent;
  private int numberOfGeneratedEvents;
  private long wallClockTime; // Wall clock time in nanoseconds
  private double simulationTime; // In seconds
  private final Semaphore semaphore = new Semaphore(1);
  private final List<SampleGenerator<?>> sampleGenerators;
  private Consumer<Event> onNextEvent;
  private Consumer<ConnectionModel> onNextJobTransition;

  private volatile boolean isGuided;
  private final int maxNumberOfEvents;
  private final boolean awaitJobServiceCompletion; // Relevant only for open queueing networks
  private int droppedJobsCount; // Track jobs dropped due to queue overflow

  public DiscreteEventDrivenSimulation(
      final int id,
      final NetworkModel networkModel,
      final boolean isGuided,
      final double maxDuration,
      final double maxSimulatedTime,
      final int maxSampleSize,
      final int maxNumberOfEvents,
      final boolean awaitJobServiceCompletion,
      final List<SampleGenerator<?>> sampleGenerators) {
    super(id, networkModel, maxDuration, maxSimulatedTime, maxSampleSize);

    LOGGER.debug(
        "New {} simulation has been created with ID {}. Max duration: {}; max simulated time: {}; max sample size: {}; max number of events: {}",
        isGuided ? "guided" : "automatic",
        id,
        maxDuration,
        maxSimulatedTime,
        maxSampleSize,
        maxNumberOfEvents);

    this.isGuided = isGuided;
    this.maxNumberOfEvents = maxNumberOfEvents;
    this.awaitJobServiceCompletion = networkModel.isOpen() && awaitJobServiceCompletion;

    for (final var serviceCenterModel : networkModel.getServiceCenterModels()) {
      final var initialNumberOfJobs = serviceCenterModel.getInitialNumberOfJobs();
      final List<JobModel> jobsToStartImmediately = new ArrayList<>();

      for (var i = 0; i < initialNumberOfJobs; i++) {
        final var jobModel = new JobModel();

        if (i < serviceCenterModel.getNumberOfServers()) {
          jobsToStartImmediately.add(jobModel);
        } else {
          serviceCenterModel.addToQueue(jobModel, simulationTime);
        }
      }

      var serverIndex = 0;
      for (final var jobModel : jobsToStartImmediately) {
        final var startJobEvent =
            new Event(
                Event.Type.START_SERVICE,
                0.0,
                jobModel,
                serviceCenterModel,
                serviceCenterModel.getServerModel(serverIndex++));
        eventQueue.add(startJobEvent);
        if (++numberOfGeneratedEvents >= maxNumberOfEvents) {
          break;
        }
      }

      networkModel.incrementCurrentNumberOfJobs(initialNumberOfJobs);
    }

    if (networkModel.isOpen()) {
      for (final var sourceModel : networkModel.getSourceModels()) {
        if (numberOfGeneratedEvents < maxNumberOfEvents) {
          final var createJobEvent =
              new Event(
                  Event.Type.CREATE_JOB,
                  sourceModel.getSelectedTimeDistribution().getNextRandomValue(),
                  sourceModel);
          eventQueue.add(createJobEvent);
          numberOfGeneratedEvents++;
        } else {
          break;
        }
      }
    }

    this.sampleGenerators = sampleGenerators;
  }

  @Override
  protected Void call() throws InterruptedException {
    LOGGER.debug(
        "Simulation {} started. Measurement generators: {}; max events: {}; {}; max duration: {}; max simulated time: {}; max samples: {}",
        id,
        sampleGenerators.size(),
        NumericFormatUtil.formatNumber(maxNumberOfEvents),
        (awaitJobServiceCompletion ? "await" : "do not await") + " all jobs",
        TimeFormatUtil.shortTimeString(maxDuration),
        TimeFormatUtil.shortTimeString(maxSimulatedTime),
        NumericFormatUtil.formatNumber(maxSampleSize));

    var startWallClockTime = System.nanoTime();

    while (!isCancelled() && !shouldStop()) {
      if (isGuided) {
        semaphore.acquire();
      }

      currentEvent = eventQueue.poll();

      if (isGuided && onNextEvent != null) {
        onNextEvent.accept(currentEvent);
      }

      simulationTime = currentEvent.getTime();

      final var eventNode = currentEvent.getNetworkNodeModel();
      final var eventJob = currentEvent.getJobModel();

      switch (currentEvent.getType()) {
        case CREATE_JOB -> {
          final var eventSource = (SourceModel) eventNode;
          final var connection = eventSource.getSelectedRoutingStrategy().getConnection();
          final var destinationNode = connection.getDestination();

          if (isGuided && onNextJobTransition != null) {
            onNextJobTransition.accept(connection);
          }

          if (destinationNode instanceof ServiceCenterModel serviceCenter) {
            if (serviceCenter.getQueueModel().isEmpty() && serviceCenter.hasFreeServer()) {
              // Create a new job
              final var newJobModel = new JobModel();
              newJobModel.setNetworkEnterTime(simulationTime);
              newJobModel.setLastArrivalTime(simulationTime);
              networkModel.incrementCurrentNumberOfJobs();

              // Start the service immediately
              eventQueue.add(
                  new Event(
                      Event.Type.START_SERVICE,
                      simulationTime,
                      newJobModel,
                      serviceCenter,
                      serviceCenter.getFreeServerModel()));
              numberOfGeneratedEvents++;

              // Create an event for the next job creation
              eventQueue.add(
                  new Event(
                      Event.Type.CREATE_JOB,
                      simulationTime
                          + eventSource.getSelectedTimeDistribution().getNextRandomValue(),
                      eventSource));
              numberOfGeneratedEvents++;
            } else if (serviceCenter.hasFreeQueueSlot()) {
              // Create a new job
              final var newJobModel = new JobModel();
              newJobModel.setNetworkEnterTime(simulationTime);
              networkModel.incrementCurrentNumberOfJobs();

              // Add the job to the queue, the job will be started when a server is free
              serviceCenter.addToQueue(newJobModel, simulationTime);

              // Create an event for the next job creation
              eventQueue.add(
                  new Event(
                      Event.Type.CREATE_JOB,
                      simulationTime
                          + eventSource.getSelectedTimeDistribution().getNextRandomValue(),
                      eventSource));
              numberOfGeneratedEvents++;
            } else { // No free server or queue slot
              droppedJobsCount++;

              eventQueue.add(
                  new Event(
                      Event.Type.CREATE_JOB,
                      simulationTime
                          + eventSource.getSelectedTimeDistribution().getNextRandomValue(),
                      eventSource));
              numberOfGeneratedEvents++;
            }
          } else {
            LOGGER.error("There's no destination for the job");
          }
        }
        case START_SERVICE -> {
          final var eventServiceCenter = (ServiceCenterModel) eventNode;
          final var eventServer = currentEvent.getServerModel();

          // Remove the job from the queue (if it's there) and start the service
          // Additionally, if the job was waiting in the queue and someone was waiting for a free
          // queue slot, the handler that the waiting node registered will be called
          eventServiceCenter.getQueueModel().removeJob(eventJob);
          eventServiceCenter.startService(eventJob, eventServer, simulationTime);

          // Create an event for the service completion
          eventQueue.add(
              new Event(
                  Event.Type.FINISH_SERVICE,
                  simulationTime
                      + eventServiceCenter.getSelectedTimeDistribution().getNextRandomValue(),
                  eventJob,
                  eventServiceCenter,
                  eventServer));
          numberOfGeneratedEvents++;
        }
        case FINISH_SERVICE -> {
          final var eventServiceCenter = (ServiceCenterModel) eventNode;
          final var eventServer = currentEvent.getServerModel();
          final var connection = eventServiceCenter.getSelectedRoutingStrategy().getConnection();
          final var destinationNode = connection.getDestination();

          if (isGuided && onNextJobTransition != null) {
            onNextJobTransition.accept(connection);
          }

          if (destinationNode instanceof SinkModel) {
            // The job is leaving the network
            eventServiceCenter.finishService(eventServer, simulationTime);
            networkModel.addDepartedJobModel(eventJob, simulationTime);

            // Start servicing the next job in the queue
            if (!eventServiceCenter.getQueueModel().isEmpty()) {
              final var nextJob =
                  eventServiceCenter.getQueueModel().getSelectedQueueDiscipline().getNextJobModel();
              eventQueue.add(
                  new Event(
                      Event.Type.START_SERVICE,
                      simulationTime,
                      nextJob,
                      eventServiceCenter,
                      eventServiceCenter.getFreeServerModel()));
              numberOfGeneratedEvents++;
            }
          } else if (destinationNode instanceof ServiceCenterModel serviceCenterDestination) {
            // The job is going to another service center, then we behave in a similar way as when
            // a new job enters the network
            if (serviceCenterDestination.getQueueModel().isEmpty()
                && serviceCenterDestination.hasFreeServer()) {
              eventServiceCenter.finishService(eventServer, simulationTime);
              eventJob.setLastArrivalTime(simulationTime);

              // Start the service immediately
              eventQueue.add(
                  new Event(
                      Event.Type.START_SERVICE,
                      simulationTime,
                      eventJob,
                      serviceCenterDestination,
                      serviceCenterDestination.getFreeServerModel()));
              numberOfGeneratedEvents++;

              // Start servicing the next job in the queue
              if (!eventServiceCenter.getQueueModel().isEmpty()) {
                final var nextJob =
                    eventServiceCenter
                        .getQueueModel()
                        .getSelectedQueueDiscipline()
                        .getNextJobModel();
                eventQueue.add(
                    new Event(
                        Event.Type.START_SERVICE,
                        simulationTime,
                        nextJob,
                        eventServiceCenter,
                        eventServiceCenter.getFreeServerModel()));
                numberOfGeneratedEvents++;
              }
            } else if (serviceCenterDestination.hasFreeQueueSlot()) {
              finishServiceAndStartNextJob(
                  eventServiceCenter, eventServer, serviceCenterDestination, eventJob);
            } else { // No free server or queue slot
              droppedJobsCount++;
              eventServiceCenter.finishService(eventServer, simulationTime);

              if (!eventServiceCenter.getQueueModel().isEmpty()) {
                final var nextJob =
                    eventServiceCenter
                        .getQueueModel()
                        .getSelectedQueueDiscipline()
                        .getNextJobModel();
                eventQueue.add(
                    new Event(
                        Event.Type.START_SERVICE,
                        simulationTime,
                        nextJob,
                        eventServiceCenter,
                        eventServiceCenter.getFreeServerModel()));
                numberOfGeneratedEvents++;
              }
            }
          }
        }
      }

      for (final var sampleGenerator : sampleGenerators) {
        sampleGenerator.takeMeasurement(simulationTime);
      }

      final var currentWallClockTime = System.nanoTime();
      wallClockTime = currentWallClockTime - startWallClockTime;

      updateProgress(calculateProgress(), 1.0);
    }

    final var minSampleSizeSampleGenerator =
        sampleGenerators.stream()
            .min(Comparator.comparingInt(SampleGenerator::getSampleSize))
            .orElse(null);
    final var maxSampleSizeSampleGenerator =
        sampleGenerators.stream()
            .max(Comparator.comparingInt(SampleGenerator::getSampleSize))
            .orElse(null);

    LOGGER.debug(
        "Simulation with ID {} has been {}. Current simulated time: {}; elapsed wall clock time: {}, min number of generated samples: {}; max number of generated samples: {}; number of generated events: {}; dropped jobs: {}",
        id,
        isCancelled() ? "cancelled" : "completed",
        TimeFormatUtil.shortTimeString(simulationTime),
        TimeFormatUtil.formatNanoseconds(wallClockTime),
        minSampleSizeSampleGenerator != null ? minSampleSizeSampleGenerator.getSampleSize() : 0,
        maxSampleSizeSampleGenerator != null ? maxSampleSizeSampleGenerator.getSampleSize() : 0,
        numberOfGeneratedEvents,
        droppedJobsCount);

    return null;
  }

  private void finishServiceAndStartNextJob(
      final ServiceCenterModel sourceServiceCenter,
      final ServerModel sourceServer,
      final ServiceCenterModel destinationServiceCenter,
      final JobModel job) {
    sourceServiceCenter.finishService(sourceServer, simulationTime);

    // Add the job to the queue, the job will be started when a server is free
    destinationServiceCenter.addToQueue(job, simulationTime);

    // Start servicing the next job in the queue
    if (!sourceServiceCenter.getQueueModel().isEmpty()) {
      final var nextJob =
          sourceServiceCenter.getQueueModel().getSelectedQueueDiscipline().getNextJobModel();
      eventQueue.add(
          new Event(
              Event.Type.START_SERVICE,
              simulationTime,
              nextJob,
              sourceServiceCenter,
              sourceServiceCenter.getFreeServerModel()));
      numberOfGeneratedEvents++;
    }
  }

  private boolean shouldStop() {
    return (!isGuided && wallClockTime >= maxDuration * 1E9)
        || simulationTime >= maxSimulatedTime
        || numberOfGeneratedEvents >= maxNumberOfEvents
        || sampleGenerators.stream().allMatch(SampleGenerator::isFull)
        || eventQueue.isEmpty();
  }

  private double calculateProgress() {
    var elapsedWallClockTimeProgress =
        !isGuided ? (double) wallClockTime / (maxDuration * 1E9) : 0.0;
    var simulatedTimeProgress = simulationTime / maxSimulatedTime;
    var eventProgress = (double) numberOfGeneratedEvents / maxNumberOfEvents;
    var sampleProgress =
        sampleGenerators.stream().mapToDouble(SampleGenerator::getSampleSize).min().orElse(0)
            / maxSampleSize;

    return Math.max(
        Math.max(elapsedWallClockTimeProgress, simulatedTimeProgress),
        Math.max(eventProgress, sampleProgress));
  }

  public void setOnNextEvent(final Consumer<Event> onNextEvent) {
    this.onNextEvent = onNextEvent;
  }

  public void setOnNextJobTransition(final Consumer<ConnectionModel> onNextJobTransition) {
    this.onNextJobTransition = onNextJobTransition;
  }

  public Consumer<ConnectionModel> getOnNextJobTransition() {
    return onNextJobTransition;
  }

  public void setGuided(final boolean guided) {
    isGuided = guided;
  }

  public void nextEvent(final boolean force) {
    if (isGuided || force) {
      semaphore.release();
    }
  }

  public void nextEvent() {
    nextEvent(false);
  }

  public Event getCurrentEvent() {
    return currentEvent;
  }
}
