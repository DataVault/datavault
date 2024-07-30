package org.datavaultplatform.common.task;

public interface TaskStageEventListener {
    void onTaskStageEvent(TaskStageEvent event);

    default void doStage(TaskStage stage) {
        onTaskStageEvent(new TaskStageEvent(stage, false));
    }
    
    default void skipStage(TaskStage stage) {
        onTaskStageEvent(new TaskStageEvent(stage, true));
    }
}
