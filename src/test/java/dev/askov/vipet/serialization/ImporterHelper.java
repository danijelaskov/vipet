package dev.askov.vipet.serialization;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.serialization.json.JSONNetworkImporter;
import dev.askov.vipet.serialization.json.JSONPerformanceMeasuresImporter;
import java.io.*;
import java.util.Objects;

public class ImporterHelper {

  private static final String NETWORKS_DIRECTORY = "json/networks/";
  private static final String CLOSED_NETWORKS_DIRECTORY = NETWORKS_DIRECTORY + "closed/";
  private static final String OPEN_NETWORKS_DIRECTORY = NETWORKS_DIRECTORY + "open/";
  private static final String GORDON_NEWELL_NETWORK =
      Objects.requireNonNull(
              ImporterHelper.class.getResource(CLOSED_NETWORKS_DIRECTORY + "net_gn_1.json"))
          .getPath();
  private static final String JACKSON_NETWORK_1 =
      Objects.requireNonNull(
              ImporterHelper.class.getResource(OPEN_NETWORKS_DIRECTORY + "net_jackson_1.json"))
          .getPath();
  private static final String JACKSON_NETWORK_2 =
      Objects.requireNonNull(
              ImporterHelper.class.getResource(OPEN_NETWORKS_DIRECTORY + "net_jackson_2.json"))
          .getPath();

  private static final String PERFORMANCE_MEASURES_DIRECTORY = "json/performancemeasures/";
  private static final String JACKSON_NETWORK_1_EXPECTED_RESULTS =
      Objects.requireNonNull(
              ImporterHelper.class.getResource(
                  PERFORMANCE_MEASURES_DIRECTORY + "perf_jackson_1.json"))
          .getPath();
  private static final String JACKSON_NETWORK_2_EXPECTED_RESULTS =
      Objects.requireNonNull(
              ImporterHelper.class.getResource(
                  PERFORMANCE_MEASURES_DIRECTORY + "perf_jackson_2.json"))
          .getPath();
  private static final String GORDON_NEWELL_NETWORK_EXPECTED_RESULTS =
      Objects.requireNonNull(
              ImporterHelper.class.getResource(PERFORMANCE_MEASURES_DIRECTORY + "perf_gn_1.json"))
          .getPath();

  private static NetworkModel jacksonNetwork1;
  private static NetworkModel jacksonNetwork2;
  private static NetworkModel gordonNewellNetwork;

  private static PerformanceMeasureCollection jacksonNetwork1ExpectedPerformanceMeasures;
  private static PerformanceMeasureCollection jacksonNetwork2ExpectedPerformanceMeasures;
  private static PerformanceMeasureCollection gordonNewellNetworkExpectedPerformanceMeasures;

  public static NetworkModel getJacksonNetwork1() {
    if (jacksonNetwork1 == null) {
      importJackson1Network();
    }

    return jacksonNetwork1;
  }

  public static boolean importJackson1Network() {
    if (jacksonNetwork1 != null) {
      return true;
    }

    final var file = new File(JACKSON_NETWORK_1);
    try (final var fileInputStream = new FileInputStream(file)) {
      final var importer = new JSONNetworkImporter(fileInputStream);

      if (importer.importNetwork()) {
        jacksonNetwork1 = importer.getNetwork();
        return true;
      }
    } catch (IOException exception) {
      return false;
    }

    return false;
  }

  public static NetworkModel getJacksonNetwork2() {
    if (jacksonNetwork2 == null) {
      importJacksonNetwork2();
    }

    return jacksonNetwork2;
  }

  public static boolean importJacksonNetwork2() {
    if (jacksonNetwork2 != null) {
      return true;
    }

    final var file = new File(JACKSON_NETWORK_2);
    try (final var fileInputStream = new FileInputStream(file)) {
      final var importer = new JSONNetworkImporter(fileInputStream);

      if (importer.importNetwork()) {
        jacksonNetwork2 = importer.getNetwork();
        return true;
      }
    } catch (IOException exception) {
      return false;
    }

    return false;
  }

  public static NetworkModel getGordonNewellNetwork() {
    if (gordonNewellNetwork == null) {
      importGordonNewellNetwork();
    }

    return gordonNewellNetwork;
  }

  public static boolean importGordonNewellNetwork() {
    if (gordonNewellNetwork != null) {
      return true;
    }

    final var file = new File(GORDON_NEWELL_NETWORK);
    try (final var fileInputStream = new FileInputStream(file)) {
      final var importer = new JSONNetworkImporter(fileInputStream);

      if (importer.importNetwork()) {
        gordonNewellNetwork = importer.getNetwork();
        return true;
      }
    } catch (IOException exception) {
      return false;
    }

    return false;
  }

  public static PerformanceMeasureCollection getJacksonNetwork1ExpectedPerformanceMeasures() {
    if (jacksonNetwork1ExpectedPerformanceMeasures == null) {
      final var file = new File(JACKSON_NETWORK_1_EXPECTED_RESULTS);
      final var jackson1Network = getJacksonNetwork1();

      jacksonNetwork1ExpectedPerformanceMeasures =
          new JSONPerformanceMeasuresImporter(file, jackson1Network).importData();
    }

    return jacksonNetwork1ExpectedPerformanceMeasures;
  }

  public static PerformanceMeasureCollection getJacksonNetwork2ExpectedPerformanceMeasures() {
    if (jacksonNetwork2ExpectedPerformanceMeasures == null) {
      final var file = new File(JACKSON_NETWORK_2_EXPECTED_RESULTS);
      final var jackson2Network = getJacksonNetwork2();

      jacksonNetwork2ExpectedPerformanceMeasures =
          new JSONPerformanceMeasuresImporter(file, jackson2Network).importData();
    }

    return jacksonNetwork2ExpectedPerformanceMeasures;
  }

  public static PerformanceMeasureCollection getGordonNewellNetworkExpectedPerformanceMeasures() {
    if (gordonNewellNetworkExpectedPerformanceMeasures == null) {
      final var file = new File(GORDON_NEWELL_NETWORK_EXPECTED_RESULTS);
      final var gordonNewell1Network = getGordonNewellNetwork();

      gordonNewellNetworkExpectedPerformanceMeasures =
          new JSONPerformanceMeasuresImporter(file, gordonNewell1Network).importData();
    }

    return gordonNewellNetworkExpectedPerformanceMeasures;
  }
}
