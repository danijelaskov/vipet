package dev.askov.vipet.mvc.models.network.queue;

import dev.askov.vipet.mvc.models.network.JobModel;
import org.apache.commons.math3.random.RandomGenerator;

public abstract class QueueDisciplineModel {

  protected final QueueModel queueModel;
  protected RandomGenerator randomGenerator;

  public QueueDisciplineModel(final QueueModel queueModel) {
    this.queueModel = queueModel;
  }

  public abstract JobModel getNextJobModel();

  public abstract String getName();

  public RandomGenerator getRandomGenerator() {
    return randomGenerator;
  }

  public void setRandomGenerator(final RandomGenerator randomGenerator) {
    this.randomGenerator = randomGenerator;
  }
}
