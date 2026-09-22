package dev.askov.vipet.mvc.models.network.queue;

import dev.askov.vipet.mvc.models.network.JobModel;

public final class FIFOQueueDisciplineModel extends QueueDisciplineModel {

  public FIFOQueueDisciplineModel(final QueueModel queueModel) {
    super(queueModel);
  }

  @Override
  public JobModel getNextJobModel() {
    return queueModel.getCurrentNumberOfJobs() > 0 ? queueModel.getJobList().getFirst() : null;
  }

  @Override
  public String getName() {
    return "FIFO";
  }
}
