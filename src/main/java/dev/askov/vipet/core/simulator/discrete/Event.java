package dev.askov.vipet.core.simulator.discrete;

import dev.askov.vipet.localization.LocalizationManager;
import dev.askov.vipet.mvc.models.network.JobModel;
import dev.askov.vipet.mvc.models.network.NetworkNodeModel;
import dev.askov.vipet.mvc.models.network.ServerModel;

public final class Event {

  public enum Type {
    CREATE_JOB("createJob"),
    START_SERVICE("startService"),
    FINISH_SERVICE("finishService");

    private static final LocalizationManager LOCALIZATION_MANAGER =
        LocalizationManager.getInstance();

    private final String key;

    Type(final String key) {
      this.key = key;
    }

    public String getName() {
      return LOCALIZATION_MANAGER.getString("eventType." + key);
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  private static long idCounter = 0;

  private final long id;

  private final Type type;
  private final double time; // In seconds
  private final NetworkNodeModel networkNodeModel;
  private final ServerModel serverModel;
  private final JobModel jobModel;

  public Event(
      final Type type,
      final double time,
      final JobModel jobModel,
      final NetworkNodeModel networkNodeModel,
      final ServerModel serverModel) {
    id = idCounter++;
    this.type = type;
    this.time = time;
    this.jobModel = jobModel;
    this.networkNodeModel = networkNodeModel;
    this.serverModel = serverModel;
  }

  public Event(
      final Type type,
      final double time,
      final JobModel jobModel,
      final NetworkNodeModel networkNodeModel) {
    this(type, time, jobModel, networkNodeModel, null);
  }

  public Event(final Type type, final double time, final NetworkNodeModel networkNodeModel) {
    this(type, time, null, networkNodeModel);
  }

  public Type getType() {
    return type;
  }

  public double getTime() {
    return time;
  }

  public JobModel getJobModel() {
    return jobModel;
  }

  public NetworkNodeModel getNetworkNodeModel() {
    return networkNodeModel;
  }

  public ServerModel getServerModel() {
    return serverModel;
  }

  @Override
  public String toString() {
    if (jobModel != null) {
      return "Event [ID=%d, type=%s, t=%07.2f ms, server=%s, job=%-20s]"
          .formatted(id, type, time * 1000.0, serverModel, jobModel);
    } else {
      return "Event [ID=%d, type=%s, t=%07.2f ms, node=%s]"
          .formatted(id, type, time * 1000.0, networkNodeModel);
    }
  }
}
