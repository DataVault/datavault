package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
public abstract class BaseOrderedEvents {

    private final List<String> ordered;
    
    protected BaseOrderedEvents(List<String> ordered) {
        Assert.isTrue(ordered != null, "The ordered List cannot be null");
        this.ordered = ordered;
    }
    
    public boolean isLastEventBefore(Event lastEvent, Class<? extends Event> eventClass){
        if (lastEvent == null) {
            log.warn("isLastEventBefore eventClass[{}] - The last event is null", eventClass);
            return true;
        }

        Assert.notNull(eventClass, "eventClass must not be null");
        String eventClassName = eventClass.getName();
        int eventIndex = ordered.indexOf(eventClassName);
        Assert.isTrue(eventIndex >= 0, "Unexpected eventClass [%s]".formatted(eventClassName));

        String lastEventClassName = lastEvent.getClass().getName();
        log.info("lastEventClassName[{}]", lastEventClassName);
        int lastEventClassIndex = ordered.indexOf(lastEventClassName);

        boolean result;
        if (lastEventClassIndex < 0) {
            result = true;
        } else {
            result = lastEventClassIndex < eventIndex;
        }
        log.info("isLastEvent[{}] Before eventClass[{}] ? [{}]", lastEvent, eventClass, result);
        return result;
    }
    
    /*
     * Used in testing
     */
    public List<String> getEventsBefore(Class<? extends Event> event) {
        String eventClassName = event.getName();
        Assert.isTrue(ordered.contains(eventClassName), "The event [%s] is invalid".formatted(eventClassName));
        int eventIdx = ordered.indexOf(eventClassName);
        return ordered.subList(0, eventIdx);
    }
}
