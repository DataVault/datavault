package org.datavaultplatform.common.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helps with testing by allowing you to record all events sent from worker while processing an inbound message.
 * We know that the Worker only processes 1 inbound message at a time.
 * We also know that the Worker sends messages from different threads
 */
public class RecordingEventSender implements EventSender {

  private final EventSender eventSender;

  private final ArrayList<Event> events = new ArrayList<>();

  public RecordingEventSender(EventSender eventSender) {
    this.eventSender = eventSender;
  }

  @Override
  public void send(Event event) {
    synchronized (this) {
      events.add(event);
      eventSender.send(event);
    }
  }

  public List<Event> getEvents() {
    synchronized (this) {
      return Collections.unmodifiableList(events);
    }
  }

  public void clear() {
    synchronized (this) {
      events.clear();
    }
  }
}

