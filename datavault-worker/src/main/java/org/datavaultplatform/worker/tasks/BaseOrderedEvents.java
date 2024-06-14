package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.event.Event;
import org.springframework.util.Assert;

import java.util.List;

public abstract class BaseOrderedEvents {

    private final List<String> ordered;
    
    protected BaseOrderedEvents(List<String> ordered) {
        Assert.isTrue(ordered != null, "The ordered List cannot be null");
        this.ordered = ordered;
    }
    
    public boolean isLastEventBefore(Event lastEvent, Class<? extends Event> eventClass){
        if (lastEvent == null) {
            return true;
        }

        Assert.notNull(eventClass, "eventClass must not be null");
        String eventClassName = eventClass.getName();
        int eventIndex = ordered.indexOf(eventClassName);
        Assert.isTrue(eventIndex >= 0, "Unexpected eventClass [%s]".formatted(eventClassName));

        String lastEventClassName = lastEvent.getClass().getName();
        int lastEventClassIndex = ordered.indexOf(lastEventClassName);

        if (lastEventClassIndex < 0) {
            return true;
        } else {
            return lastEventClassIndex < eventIndex;
        }
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
