package dev.askov.vipet.core.analyzer;

import dev.askov.vipet.core.PerformanceMeasuresProviderTest;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.ImporterHelper;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JacksonAnalyzerTest extends PerformanceMeasuresProviderTest {

  protected static NetworkModel jackson1Network;
  protected static NetworkModel jackson2Network;

  protected static PerformanceMeasureCollection jackson1ExpectedPerformanceMeasures;
  protected static PerformanceMeasureCollection jackson2ExpectedPerformanceMeasures;

  @BeforeAll
  public static void beforeAll() {
    PerformanceMeasuresProviderTest.beforeAll();

    jackson1Network = ImporterHelper.getJacksonNetwork1();
    jackson2Network = ImporterHelper.getJacksonNetwork2();

    jackson1ExpectedPerformanceMeasures =
        ImporterHelper.getJacksonNetwork1ExpectedPerformanceMeasures();
    jackson2ExpectedPerformanceMeasures =
        ImporterHelper.getJacksonNetwork2ExpectedPerformanceMeasures();
  }

  @Test
  @DisplayName("Jackson analysis test: net_jackson_1")
  void jackson1NetworkTest() throws ExecutionException, InterruptedException {
    final var analyzer = new JacksonNetworkAnalyzer(jackson1Network);

    final var openNetworkAnalysis = analyzer.createTask();
    openNetworkAnalysis.run();

    compare(openNetworkAnalysis.get(), jackson1ExpectedPerformanceMeasures);
  }

  @Test
  @DisplayName("Jackson analysis test: net_jackson_2")
  void jackson2NetworkTest() throws ExecutionException, InterruptedException {
    final var analyzer = new JacksonNetworkAnalyzer(jackson2Network);

    final var openNetworkAnalysis = analyzer.createTask();
    openNetworkAnalysis.run();

    compare(openNetworkAnalysis.get(), jackson2ExpectedPerformanceMeasures);
  }
}
