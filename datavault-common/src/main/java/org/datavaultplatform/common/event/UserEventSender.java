package org.datavaultplatform.common.event;

/**
 * Saves the repeated boilerplate of adding user to event before it's sent.
 * @param eventSender the event sender
 * @param userId the userId to add to every event before it's sent
 */
public record UserEventSender(EventSender eventSender, String userId) implements EventSender {
    @Override
    public void send(Event event) {
        eventSender.send(event.withUserId(userId));
    }
}
