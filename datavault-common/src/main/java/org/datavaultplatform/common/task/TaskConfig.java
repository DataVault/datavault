package org.datavaultplatform.common.task;


import org.datavaultplatform.common.PropNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class TaskConfig {

    public static final Logger LOG = LoggerFactory.getLogger(TaskConfig.class);

    public static final boolean DEFAULT_EXECUTOR_PROPER_SHUTDOWN_ENABLED = true;
    public static final Duration DEFAULT_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION = Duration.ofMinutes(5);

    public static final Duration DEFAULT_PROCESS_MAX_DURATION = Duration.ofHours(1);
    public static final Duration DEFAULT_PROCESS_SIGTERM_TIMEOUT_DURATION = Duration.ofSeconds(30);
    public static final Duration DEFAULT_PROCESS_POST_SIGKILL_TIMEOUT_DURATION = Duration.ofSeconds(5);

    // Whether each TaskExecutor will stop other sub-tasks should 1 sub-task have an error - defaults to true
    private boolean executorProperShutdownEnabled;

    // after initial sub-task error, how long does TaskExecutor wait for other sub-tasks to complete normally before telling them to stop - defaults to 5 minutes
    private Duration executorPreShutdownNowDuration;
    
    // How long each process has to run before timing out - defaults to 1 hour
    private Duration processMaxDuration;
    
    // how long we wait for a process to stop after sending SIGTERM before we send SIGKILL - defaults to 30 seconds
    private Duration processSigTermTimeoutDuration;

    // how long we wait for a process to stop after sending SIGKILL before we log error - defaults to 5 seconds
    private Duration processPostSigKillTimeoutDuration;

    public TaskConfig() {
        initialize();
    }

    private synchronized void initialize() {
        this.executorProperShutdownEnabled = DEFAULT_EXECUTOR_PROPER_SHUTDOWN_ENABLED;
        this.executorPreShutdownNowDuration = DEFAULT_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION;
        this.processMaxDuration = DEFAULT_PROCESS_MAX_DURATION;
        this.processSigTermTimeoutDuration = DEFAULT_PROCESS_SIGTERM_TIMEOUT_DURATION;
        this.processPostSigKillTimeoutDuration = DEFAULT_PROCESS_POST_SIGKILL_TIMEOUT_DURATION;
    }
    
    public synchronized void reset() {
        initialize();
    }

    public synchronized boolean isExecutorProperShutdownEnabled() {
        return executorProperShutdownEnabled;
    }

    public synchronized void setExecutorProperShutdownEnabled(boolean properShutdownEnabled) {
        this.executorProperShutdownEnabled = properShutdownEnabled;
    }

    public synchronized Duration getProcessMaxDuration() {
        return processMaxDuration;
    }

    public synchronized void setProcessMaxDuration(Duration processMaxDuration) {
        if (isValidDuration(processSigTermTimeoutDuration)) {
            this.processMaxDuration = processMaxDuration;
        }
    }

    public synchronized Duration getProcessSigTermTimeoutDuration() {
        return processSigTermTimeoutDuration;
    }

    public synchronized void setProcessSigTermTimeoutDuration(Duration processSigTermTimeoutDuration) {
        if (isValidDuration(processSigTermTimeoutDuration)) {
            this.processSigTermTimeoutDuration = processSigTermTimeoutDuration;
        }
    }

    public synchronized Duration getProcessPostSigKillTimeoutDuration() {
        return processPostSigKillTimeoutDuration;
    }

    public synchronized void setProcessPostSigKillTimeoutDuration(Duration processPostSigKillTimeoutDuration) {
        if (isValidDuration(processPostSigKillTimeoutDuration)) {
            this.processPostSigKillTimeoutDuration = processPostSigKillTimeoutDuration;
        }
    }

    private boolean isValidDuration(Duration value) {
        return value != null && value.toMillis() > 1;
    }

    public synchronized Duration getExecutorPreShutdownNowDuration() {
        return executorPreShutdownNowDuration;
    }

    public synchronized void setExecutorPreShutdownNowDuration(Duration executorPreShutdownNowDuration) {
        if (isValidDuration(executorPreShutdownNowDuration)) {
            this.executorPreShutdownNowDuration = executorPreShutdownNowDuration;
        }
    }

    /*
     * When we terminate a TaskExecutor Task and that task is a Process, we might wait for SIGTERM to timeout, then we issue SIGKILL and wait a bit longer
     * So the time that the TaskExecutor has to wait for all Tasks to complete has to be larger than the time a Process takes to complete.
     */
    public synchronized Duration getExecutorShutdownDuration() {
        return Duration.ofMinutes(1)
                .plus(getProcessSigTermTimeoutDuration())
                .plus(getProcessPostSigKillTimeoutDuration());
    }

    public void populate(Map<String, String> taskProperties) {
        if (taskProperties == null) {
            return;
        }
        Boolean properShutdownEnabled = parseBoolean(taskProperties, PropNames.EXECUTOR_PROPER_SHUTDOWN_ENABLED);
        if (properShutdownEnabled != null) {
            setExecutorProperShutdownEnabled(properShutdownEnabled);
        }
        setExecutorPreShutdownNowDuration(parseDuration(taskProperties, PropNames.EXECUTOR_PRE_SHUTDOWN_NOW_DURATION));

        setProcessMaxDuration(parseDuration(taskProperties, PropNames.PROCESS_MAX_DURATION));
        setProcessSigTermTimeoutDuration(parseDuration(taskProperties, PropNames.PROCESS_SIGTERM_TIMEOUT_DURATION));
        setProcessPostSigKillTimeoutDuration(parseDuration(taskProperties, PropNames.PROCESS_POST_SIGKILL_TIMEOUT_DURATION));
    }

    private Boolean parseBoolean(Map<String, String> properties, String name) {
        return parseBoolean(name, properties.get(name));
    }

    private Boolean parseBoolean(String name, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (DateTimeParseException rte) {
            LOG.error("Cannot convert [{}] value [{}] into Boolean", name, value, rte);
            return null;
        }
    }

    private Duration parseDuration(Map<String, String> properties, String name) {
        return parseDuration(name, properties.get(name));
    }

    private Duration parseDuration(String name, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Duration.parse(value);
        } catch (RuntimeException rte) {
            LOG.error("Cannot convert [{}] value [{}] into Duration", name, value, rte);
            return null;
        }
    }
}
