package dev.askov.vipet.core.simulator.discrete;

import dev.askov.vipet.core.PerformanceMeasuresProviderTest;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.ImporterHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DiscreteEventDrivenSimulatorTest extends PerformanceMeasuresProviderTest {

  protected static NetworkModel openNetworkModel;
  protected static NetworkModel closedNetworkModel;

  protected static PerformanceMeasureCollection openNetworkExpectedPerformanceMeasureCollection;
  protected static PerformanceMeasureCollection closedNetworkExpectedPerformanceMeasureCollection;

  private static final double CONFIDENCE_LEVEL = 0.99;
  private static final int NUMBER_OF_RUNS = 60;
  private static final int NUMBER_OF_SAMPLES = 10_000;

  @BeforeAll
  public static void beforeAll() {
    PerformanceMeasuresProviderTest.beforeAll();
  }

  @Test
  @DisplayName("Open network simulation test: complex_open_network_1")
  void openNetworkSimulationTest() throws Exception {
    openNetworkModel = ImporterHelper.getJacksonNetwork1();
    openNetworkExpectedPerformanceMeasureCollection =
        ImporterHelper.getJacksonNetwork1ExpectedPerformanceMeasures();

    final var simulator =
        new DiscreteEventDrivenSimulator(openNetworkModel, CONFIDENCE_LEVEL, NUMBER_OF_SAMPLES);
    simulator.setNumberOfRuns(NUMBER_OF_RUNS);

    final var discreteEventDrivenSimulation = simulator.createTask();
    discreteEventDrivenSimulation.run();
    final var performanceMeasureCollection = discreteEventDrivenSimulation.get();

    compare(performanceMeasureCollection, openNetworkExpectedPerformanceMeasureCollection);
  }

  @Test
  @DisplayName("Closed network simulation test: complex_closed_network_1")
  void closedNetworkSimulationTest() throws Exception {
    closedNetworkModel = ImporterHelper.getGordonNewellNetwork();
    closedNetworkExpectedPerformanceMeasureCollection =
        ImporterHelper.getGordonNewellNetworkExpectedPerformanceMeasures();

    final var simulator =
        new DiscreteEventDrivenSimulator(closedNetworkModel, CONFIDENCE_LEVEL, NUMBER_OF_SAMPLES);
    simulator.setNumberOfRuns(NUMBER_OF_RUNS);

    final var discreteEventDrivenSimulation = simulator.createTask();
    discreteEventDrivenSimulation.run();
    final var performanceMeasureCollection = discreteEventDrivenSimulation.get();

    compare(performanceMeasureCollection, closedNetworkExpectedPerformanceMeasureCollection);
  }
}
