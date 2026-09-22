package dev.askov.vipet.serialization.json.network.queue;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.askov.vipet.mvc.models.network.queue.*;
import dev.askov.vipet.serialization.NetworkSpecificationException;
import java.io.IOException;

public final class QueueTypeAdapter extends TypeAdapter<QueueModel> {

  public static final QueueTypeAdapter INSTANCE = new QueueTypeAdapter();
  public static final String CAPACITY = "capacity";
  public static final String DISCIPLINE = "discipline";
  public static final String FIFO = "FIFO";
  public static final String LIFO = "LIFO";
  public static final String SIRO = "SIRO";

  public static TypeAdapter<QueueModel> getInstance() {
    return INSTANCE;
  }

  @Override
  public void write(JsonWriter out, QueueModel queueModel) throws IOException {
    out.beginObject();
    {
      if (queueModel.isFinite()) {
        out.name(CAPACITY).value(queueModel.getCapacity());
      }
      out.name(DISCIPLINE).value(queueModel.getSelectedQueueDiscipline().getName());
    }
    out.endObject();
  }

  @Override
  public QueueModel read(JsonReader in) throws IOException {
    final var queueModel = new QueueModel();

    in.beginObject();
    {
      while (in.hasNext()) {
        switch (in.nextName()) {
          case CAPACITY -> {
            final var capacity = in.nextInt();
            queueModel.setInfiniteCapacity(false);
            queueModel.setCapacity(capacity);
          }
          case DISCIPLINE -> {
            QueueDisciplineModel queueDisciplineModel;
            switch (in.nextString()) {
              case FIFO -> queueDisciplineModel = new FIFOQueueDisciplineModel(queueModel);
              case LIFO -> queueDisciplineModel = new LIFOQueueDisciplineModel(queueModel);
              case SIRO -> queueDisciplineModel = new SIROQueueDisciplineModel(queueModel);
              default -> throw new NetworkSpecificationException("Unknown queueModel discipline");
            }
            final int index =
                queueModel.getQueueDisciplines().stream()
                    .filter(
                        currentQueueDiscipline ->
                            currentQueueDiscipline
                                .getClass()
                                .isAssignableFrom(queueDisciplineModel.getClass()))
                    .findFirst()
                    .map(queueModel.getQueueDisciplines()::indexOf)
                    .orElse(-1);
            if (index != -1) {
              queueModel.getQueueDisciplines().set(index, queueDisciplineModel);
            } else {
              queueModel.addQueueDiscipline(queueDisciplineModel);
            }
            queueModel.setSelectedQueueDiscipline(queueDisciplineModel);
          }
          default -> throw new NetworkSpecificationException("Unknown queueModel property");
        }
      }
    }
    in.endObject();

    return queueModel;
  }
}
