package org.datavaultplatform.common.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;

public abstract class TaskStageSupport {
    public static final List<TaskStage.DepositTaskStage> DEPOSIT_STAGES = List.of(
            TaskStage.Deposit1ComputeSize.INSTANCE,
            TaskStage.Deposit2Transfer.INSTANCE,
            TaskStage.Deposit3PackageEncrypt.INSTANCE,
            TaskStage.Deposit4Archive.INSTANCE,
            TaskStage.Deposit5Verify.INSTANCE,
            TaskStage.Deposit6Final.INSTANCE);

    public static final List<TaskStage.RetrieveTaskStage> RETRIEVE_STAGES = List.of(
            TaskStage.Retrieve1CheckUserStoreFreeSpace.INSTANCE,
            TaskStage.Retrieve2RetrieveFromArchiveAndRecompose.INSTANCE,
            TaskStage.Retrieve3UploadedToUserStore.INSTANCE,
            TaskStage.Retrieve4Final.INSTANCE
    );

    public static final Map<String, TaskStage> TASK_STAGE_MAP;

    private static String normalize(String value){
        Assert.isTrue(StringUtils.isNotBlank(value), "The value cannot be blank");
        return value.trim().toLowerCase();
    }
    
    static {
        Map<String, TaskStage> temp = new LinkedHashMap<>();
        for (TaskStage stage : DEPOSIT_STAGES) {
            temp.put(normalize(stage.getClass().getSimpleName()), stage);
        }
        for (TaskStage stage : RETRIEVE_STAGES) {
            temp.put(normalize(stage.getClass().getSimpleName()), stage);
        }
        TASK_STAGE_MAP = Collections.unmodifiableMap(temp);
    }
    
    public static Optional<TaskStage> getTaskStage(String taskStageName) {
        if (StringUtils.isBlank(taskStageName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(TASK_STAGE_MAP.get(normalize(taskStageName)));
    }
}
