package org.datavaultplatform.common.event;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.task.TaskInterrupter;

/**
 * Saves the repeated boilerplate of adding user to event before it's sent.
 * @param eventSender the event sender
 * @param userId the userId to add to every event before it's sent
 */
@Slf4j
public record UserEventSender(EventSender eventSender, String userId) implements EventSender {
    @Override
    public void send(Event event) {
        log.info("XXXX SENDING EVENT [{}]", event.getClass().getSimpleName());
        eventSender.send(event.withUserId(userId));
        TaskInterrupter.checkForInterrupt(event);
    }
}
