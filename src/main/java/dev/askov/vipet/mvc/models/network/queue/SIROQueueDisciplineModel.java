package dev.askov.vipet.mvc.models.network.queue;

import dev.askov.vipet.mvc.models.network.JobModel;

public final class SIROQueueDisciplineModel extends QueueDisciplineModel {

  public SIROQueueDisciplineModel(final QueueModel queueModel) {
    super(queueModel);
  }

  @Override
  public JobModel getNextJobModel() {
    return queueModel.getCurrentNumberOfJobs() > 0
        ? queueModel
            .getJobList()
            .get((int) (getRandomGenerator().nextDouble() * queueModel.getJobList().size()))
        : null;
  }

  @Override
  public String getName() {
    return "SIRO";
  }
}
