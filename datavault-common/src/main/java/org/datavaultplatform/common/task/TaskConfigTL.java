package org.datavaultplatform.common.task;

public class TaskConfigTL {
    private static final ThreadLocal<TaskConfig> INSTANCE = ThreadLocal.withInitial(TaskConfig::new);

    public static TaskConfig get() {
        return INSTANCE.get();
    }

    public static void set(TaskConfig config) {
        INSTANCE.set(config);
    }

    public static void reset() {
        INSTANCE.remove();
    }
}