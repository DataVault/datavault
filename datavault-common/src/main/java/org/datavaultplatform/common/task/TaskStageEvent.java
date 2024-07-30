package org.datavaultplatform.common.task;

import java.util.Comparator;

public record TaskStageEvent(TaskStage stage, boolean skipped) implements Comparable<TaskStageEvent> {

    public static final Comparator<TaskStageEvent> COMPARATOR = Comparator
            .comparing(TaskStageEvent::stage)
            .thenComparing(TaskStageEvent::skipped);

    @Override
    public int compareTo(TaskStageEvent other) {
        return COMPARATOR.compare(this, other);
    }
}
