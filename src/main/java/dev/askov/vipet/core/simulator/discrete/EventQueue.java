package dev.askov.vipet.core.simulator.discrete;

import java.util.Comparator;
import java.util.PriorityQueue;

public final class EventQueue {

  private final PriorityQueue<Event> queue =
      new PriorityQueue<>(Comparator.comparingDouble(Event::getTime));

  public void add(Event event) {
    queue.add(event);
  }

  public Event poll() {
    return queue.poll();
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public int size() {
    return queue.size();
  }

  public void clear() {
    queue.clear();
  }

  @Override
  public String toString() {
    return "EventQueue [queue=%s]".formatted(queue);
  }
}
