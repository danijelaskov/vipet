package dev.askov.vipet.core.analyzer;

import dev.askov.vipet.core.PerformanceMeasuresProviderTest;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.ImporterHelper;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GordonNewellAnalyzerTest extends PerformanceMeasuresProviderTest {

  protected static NetworkModel gordonNewell1Network;
  protected static PerformanceMeasureCollection gordonNewell1ExpectedPerformanceMeasures;

  @BeforeAll
  public static void beforeAll() {
    PerformanceMeasuresProviderTest.beforeAll();

    gordonNewell1Network = ImporterHelper.getGordonNewellNetwork();
    gordonNewell1ExpectedPerformanceMeasures =
        ImporterHelper.getGordonNewellNetworkExpectedPerformanceMeasures();
  }

  @Test
  @DisplayName("Gordon-Newell analysis test: net_gn_1")
  void gordonNewell1NetworkTest() throws ExecutionException, InterruptedException {
    final var analyzer = new GordonNewellAnalyzer(gordonNewell1Network);

    final var gordonNewellAnalysis = analyzer.createTask();
    gordonNewellAnalysis.run();

    compare(gordonNewellAnalysis.get(), gordonNewell1ExpectedPerformanceMeasures);
  }
}
