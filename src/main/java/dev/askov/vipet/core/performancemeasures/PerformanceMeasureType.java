package dev.askov.vipet.core.performancemeasures;

import dev.askov.vipet.localization.LocalizationManager;

public enum PerformanceMeasureType {
  UTILIZATION("utilization"),
  THROUGHPUT("throughput"),
  QUEUE_LENGTH("queueLength"),
  NUM_JOBS("numberOfJobs"),
  WAITING_TIME("waitingTime"),
  RESPONSE_TIME("responseTime");

  private static final LocalizationManager LOCALIZATION_MANAGER = LocalizationManager.getInstance();

  private final String key;

  PerformanceMeasureType(final String key) {
    this.key = key;
  }

  public String getName() {
    return LOCALIZATION_MANAGER.getString("performanceMeasures." + key + ".name");
  }

  public String getUnit() {
    return LOCALIZATION_MANAGER.getString("performanceMeasures." + key + ".unit");
  }

  @Override
  public String toString() {
    return getName();
  }
}
