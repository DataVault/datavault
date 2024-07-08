package org.datavaultplatform.common.task;

public record TaskStageEvent(TaskStage stage, boolean skipped)  {
}
