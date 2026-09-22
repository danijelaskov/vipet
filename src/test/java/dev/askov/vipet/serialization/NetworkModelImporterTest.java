package dev.askov.vipet.serialization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NetworkModelImporterTest {

  @Test
  @DisplayName("Import Complex Open Network 1")
  void importComplexOpenNetwork1() {
    assertTrue(ImporterHelper.importJackson1Network());
  }

  @Test
  @DisplayName("Import Complex Open Network 2")
  void importComplexOpenNetwork2() {
    assertTrue(ImporterHelper.importJacksonNetwork2());
  }

  @Test
  @DisplayName("Import Complex Closed Network 1")
  void importComplexClosedNetwork1() {
    assertTrue(ImporterHelper.importGordonNewellNetwork());
  }
}
