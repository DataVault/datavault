package org.datavaultplatform.common.task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultTaskStageEventListener implements TaskStageEventListener {
        
    @Override
    public void onTaskStageEvent(TaskStageEvent event) {
        log.info("taskStageEvent:[{}]", event);
    }
}
