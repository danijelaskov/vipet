package dev.askov.vipet.core.analyzer.exceptions;

public class AnalysisDataNotAvailableException extends RuntimeException {

  public AnalysisDataNotAvailableException() {
    super("No analysis data available.");
  }
}
