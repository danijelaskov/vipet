package dev.askov.vipet.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.core.performancemeasures.PerformanceMeasureType;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceMeasuresProviderTest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PerformanceMeasuresProviderTest.class);

  private static boolean toolkitInitialized = false;

  public static void beforeAll() {
    if (!toolkitInitialized) {
      Platform.startup(() -> {});
      toolkitInitialized = true;
    }
  }

  protected static void compare(
      final PerformanceMeasureCollection actualPerformanceMeasureCollection,
      final PerformanceMeasureCollection expectedPerformanceMeasureCollection) {
    for (final var serviceCenterModel :
        actualPerformanceMeasureCollection.getSystem().getServiceCenterModels()) {
      for (final var performanceMeasureType : PerformanceMeasureType.values()) {
        if (actualPerformanceMeasureCollection.hasPerformanceMeasure(
            serviceCenterModel, performanceMeasureType)) {
          if (expectedPerformanceMeasureCollection.hasPerformanceMeasure(
              serviceCenterModel, performanceMeasureType)) {
            final var actualPerformanceMeasure =
                actualPerformanceMeasureCollection.getPerformanceMeasure(
                    serviceCenterModel, performanceMeasureType);
            final var expectedPerformanceMeasure =
                expectedPerformanceMeasureCollection.getPerformanceMeasure(
                    serviceCenterModel, performanceMeasureType);

            assertTrue(
                actualPerformanceMeasure.contains(expectedPerformanceMeasure.center()),
                "%s of %s — actual performance measure (%s) is not equal to or does not contain expected performance measure (%s)"
                    .formatted(
                        performanceMeasureType.name(),
                        serviceCenterModel.getName(),
                        actualPerformanceMeasure,
                        expectedPerformanceMeasure.center()));
          } else {
            LOGGER.warn(
                "Expected performance measures do not contain performance measure {} for {}",
                performanceMeasureType.name(),
                serviceCenterModel.getName());
          }
        } else {
          LOGGER.warn(
              "Actual performance measures do not contain performance measure {} for {}",
              performanceMeasureType.name(),
              serviceCenterModel.getName());
        }
      }
    }

    for (final var performanceMeasureType : PerformanceMeasureType.values()) {
      if (actualPerformanceMeasureCollection.hasSystemPerformanceMeasure(performanceMeasureType)) {
        if (expectedPerformanceMeasureCollection.hasSystemPerformanceMeasure(
            performanceMeasureType)) {
          final var actualPerformanceMeasure =
              actualPerformanceMeasureCollection.getSystemPerformanceMeasure(
                  performanceMeasureType);
          final var expectedPerformanceMeasure =
              expectedPerformanceMeasureCollection.getSystemPerformanceMeasure(
                  performanceMeasureType);

          assertTrue(
              actualPerformanceMeasure.contains(expectedPerformanceMeasure.center()),
              "%s of system — actual performance measure (%s) is not equal to or does not contain expected performance measure (%s)"
                  .formatted(
                      performanceMeasureType.name(),
                      actualPerformanceMeasure,
                      expectedPerformanceMeasure.center()));
        } else {
          LOGGER.warn(
              "Expected performance measures do not contain system performance measure {}",
              performanceMeasureType.name());
        }
      }
    }
  }
}
