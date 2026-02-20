package org.datavaultplatform.broker.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * This class is used to wrap the Sender class - allowing a single place where Worker Timeout properties can be configured
 * for all Worker Tasks. The worker timeout properties can be configured on the Broker, and they will be sent with each Task to the Workers.
 * This method is preferred to configuring the working timeouts on every worker individually.
 */
@Slf4j
@Component
public class TaskSender {

    public static final String WORKERS_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION = "workers.executor.pre.shutdown.now.duration";
    public static final String WORKERS_EXECUTOR_PROPER_SHUTDOWN_ENABLED = "workers.executor.proper.shutdown.enabled";

    public static final String WORKERS_PROCESS_MAX_DURATION = "workers.process.max.duration";
    public static final String WORKERS_PROCESS_SIGTERM_TIMEOUT_DURATION = "workers.process.sigterm.timeout.duration";
    public static final String WORKERS_PROCESS_POST_SIGKILL_TIMEOUT_DURATION = "workers.process.post.sigkill.timeout.duration";
    private static final Logger LOG = LoggerFactory.getLogger(TaskSender.class);

    @Getter
    private final Sender sender;

    @Getter
    // after initial sub-task error, how long do we wait for other sub-tasks to complete normally before telling them to stop - on worker, defaults to 5 minutes
    private final Duration workersExecutorPreShutdownNowDuration;

    @Getter
    // Whether each TaskExecutor will stop other sub-tasks should 1 sub-task have an error - on worker, defaults to true
    private final Boolean workersExecutorProperShutdownEnabled;
    
    @Getter
    // How long each process has to run before timing out - defaults to 1 hour
    private final Duration workersProcessMaxDuration;

    @Getter
    // how long we wait for a process to stop after sending SIGTERM before we send SIGKILL - on worker, defaults to 30 seconds
    private final Duration workersProcessSigTermTimeoutDuration;

    @Getter
    // how long we wait for a process to stop after sending SIGKILL before we log error - on worker, defaults to 5 seconds
    private final Duration workersProcessPostSigKillTimeoutDuration;

    public TaskSender(Sender sender,
                      @Value("${" + WORKERS_EXECUTOR_PROPER_SHUTDOWN_ENABLED + ":}") Boolean workersExecutorProperShutdownEnabled,
                      @Value("${" + WORKERS_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION + ":}") Duration workersExecutorPreShutdownNowDuration,

                      @Value("${" + WORKERS_PROCESS_MAX_DURATION + ":}") Duration workersProcessMaxDuration,
                      @Value("${" + WORKERS_PROCESS_SIGTERM_TIMEOUT_DURATION + ":}") Duration workersProcessSigTermTimeoutDuration,
                      @Value("${" + WORKERS_PROCESS_POST_SIGKILL_TIMEOUT_DURATION + ":}") Duration workersProcessPostSigKillTimeoutDuration
    ) {
        this.sender = sender;
        this.workersExecutorProperShutdownEnabled = workersExecutorProperShutdownEnabled;
        this.workersExecutorPreShutdownNowDuration = workersExecutorPreShutdownNowDuration;

        this.workersProcessMaxDuration = workersProcessMaxDuration;
        this.workersProcessSigTermTimeoutDuration = workersProcessSigTermTimeoutDuration;
        this.workersProcessPostSigKillTimeoutDuration = workersProcessPostSigKillTimeoutDuration;

        LOG.info("{} [{}]", WORKERS_EXECUTOR_PROPER_SHUTDOWN_ENABLED, workersExecutorProperShutdownEnabled);
        LOG.info("{} [{}]", WORKERS_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION, workersExecutorPreShutdownNowDuration);

        LOG.info("{} [{}]", WORKERS_PROCESS_MAX_DURATION, workersProcessMaxDuration);
        LOG.info("{} [{}]", WORKERS_PROCESS_SIGTERM_TIMEOUT_DURATION, workersProcessSigTermTimeoutDuration);
        LOG.info("{} [{}]", WORKERS_PROCESS_POST_SIGKILL_TIMEOUT_DURATION, workersProcessPostSigKillTimeoutDuration);
    }

    public String send(Task task, boolean restart) throws JsonProcessingException {
        Assert.notNull(task, "the task cannot be null");
        Assert.notNull(task.getTaskClass(), "the task's taskClass cannot be null");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> taskProperties = new TreeMap<>();
        Map<String, String> origTaskProperties = task.getProperties(); // this map might be read-only or null
        if (origTaskProperties != null) {
            taskProperties.putAll(origTaskProperties);
        }
        // put a writable Map back into the Task
        task.setProperties(taskProperties);
        addIfNotNull(taskProperties, PropNames.EXECUTOR_PROPER_SHUTDOWN_ENABLED, workersExecutorProperShutdownEnabled);
        addIfNotNull(taskProperties, PropNames.EXECUTOR_PRE_SHUTDOWN_NOW_DURATION, workersExecutorPreShutdownNowDuration);

        addIfNotNull(taskProperties, PropNames.PROCESS_MAX_DURATION, workersProcessMaxDuration);
        addIfNotNull(taskProperties, PropNames.PROCESS_SIGTERM_TIMEOUT_DURATION, workersProcessSigTermTimeoutDuration);
        addIfNotNull(taskProperties, PropNames.PROCESS_POST_SIGKILL_TIMEOUT_DURATION, workersProcessPostSigKillTimeoutDuration);

        String jsonTask = mapper.writeValueAsString(task);
        return sender.send(jsonTask, restart);
    }

    private void addIfNotNull(Map<String, String> map, String key, Object value) {
        map.put(key, Objects.toString(value, null));
    }

    public String send(Task task) throws JsonProcessingException {
        return this.send(task, false);
    }
}
