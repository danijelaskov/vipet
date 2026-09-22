package dev.askov.vipet.mvc.models.network.queue;

import dev.askov.vipet.mvc.models.network.JobModel;

public final class LIFOQueueDisciplineModel extends QueueDisciplineModel {

  public LIFOQueueDisciplineModel(final QueueModel queueModel) {
    super(queueModel);
  }

  @Override
  public JobModel getNextJobModel() {
    return queueModel.getCurrentNumberOfJobs() > 0 ? queueModel.getJobList().getLast() : null;
  }

  @Override
  public String getName() {
    return "LIFO";
  }
}
