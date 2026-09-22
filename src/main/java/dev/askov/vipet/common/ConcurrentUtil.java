package dev.askov.vipet.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ConcurrentUtil {

  public enum TaskType {
    CPU_BOUND,
    IO_BOUND
  }

  private ConcurrentUtil() {}

  public static ExecutorService getExecutorService(final TaskType taskType) {
    return switch (taskType) {
      case CPU_BOUND -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      case IO_BOUND -> Executors.newCachedThreadPool();
    };
  }
}
