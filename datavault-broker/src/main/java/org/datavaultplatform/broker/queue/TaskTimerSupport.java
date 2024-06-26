package org.datavaultplatform.broker.queue;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
@Component
public class TaskTimerSupport implements AutoCloseable {

    @Getter
    private final boolean enabled;
    private final long timeoutMs;
    private final Clock clock;
    private final ScheduledExecutorService executor;
    private final Map<String, TaskData> tasks = new ConcurrentHashMap<>();

    @Autowired
    public TaskTimerSupport(Clock clock,
                            @Value("${event.timer.support.enabled:true}") boolean enabled,
                            @Value("${event.timer.support.timeout.ms:600}") long timeoutMs,
                            @Value("${event.timer.support.number.threads:5}") int numberOfThreads) {
        Assert.isTrue(clock != null, "The clock cannot be null");
        Assert.isTrue(timeoutMs >= 100, "The timeout cannot be less than 100ms");
        Assert.isTrue(numberOfThreads > 0, "The numberOfThreads must be greater than 0");
        this.clock = clock;
        this.timeoutMs = timeoutMs;
        this.enabled = enabled;
        this.executor = Executors.newScheduledThreadPool(numberOfThreads);
    }

    /*
     This method ensures that we get stack traces from RuntimeExceptions thrown by the supplied Timeout Action
     */
    private static Consumer<String> wrapTimeoutAction(String label, String id, Consumer<String> timeoutAction) {
        Assert.isTrue(timeoutAction != null, "The timeoutAction cannot be null");
        return $id -> {
            try {
                timeoutAction.accept($id);
            } catch (RuntimeException ex) {
                log.warn("TimeOut for label[{}] id[{}] - problem", label, id, ex);
            } finally {
                log.warn("TimeOut for label[{}] id[{}]", label, id);
            }
        };
    }

    private static void validateId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new RuntimeException("The id cannot be blank");
        }
    }

    private static Runnable getCancelTimeoutAction(
            ScheduledExecutorService ses,
            boolean enabled,
            String label,
            String id,
            long timeoutMs,
            Runnable timeoutAction) {
        Runnable result;
        if (enabled) {
            Future<?> future = ses.schedule(timeoutAction, timeoutMs, TimeUnit.MILLISECONDS);
            result = () -> {
                try {
                    future.cancel(true);
                } catch (RuntimeException rte) {
                    log.warn("problem cancelling timeout action for label[{}]id[{}]", label, id, rte);
                } finally {
                    log.warn("cancelled timeout action for label[{}]id[{}]", label, id);
                }
            };
        } else {
            result = () -> log.warn("noop timeout cancelled for label[{}]id[{}]", label, id);
        }
        return result;
    }

    public synchronized void startTimer(String id, String label, Consumer<String> timeoutAction) {
        validateId(id);
        if (this.tasks.containsKey(id)) {
            throw new RuntimeException("The id [%s] already has a task".formatted(id));
        }
        Consumer<String> wrappedTimeoutAction = wrapTimeoutAction(label, id, timeoutAction);

        TaskInfo info = new TaskInfo(id,label,wrappedTimeoutAction, getNow());
        TaskData data = new TaskData(info);
        addTask(data);

        TaskState nextState = getNextState(info, null);
        data.setState(nextState);
        listenTimerStart(data);
    }

    public synchronized void resetTimer(String id) {
        validateId(id);
        TaskData taskData = tasks.get(id);
        if (taskData == null) {
            // probably already timed out
            String msg = "problem resetting timer for [%s] - no task found".formatted(id);
            log.warn(msg);
            return;
        }
        taskData.cancelTimeout();
        TaskState currentState = taskData.getState();
        TaskState nextState = getNextState(taskData.info, currentState);
        taskData.setState(nextState);
        listenTimerReset(taskData.info, currentState, nextState);
    }

    private TaskState getNextState(TaskInfo coreData, TaskState currentState) {
        int resetCount = currentState == null ? 0 : currentState.resetCount + 1;

        String id = coreData.id;
        String label = coreData.label;

        Runnable timeoutAction = () -> {
            coreData.timeoutAction.accept(id);
            listenTimeOut(label, id);
            removeTask(id);
        };
        Runnable cancelTimeoutAction = getCancelTimeoutAction(executor, enabled, label, id, timeoutMs, timeoutAction);
        return new TaskState(coreData, getNow(), cancelTimeoutAction, resetCount);
    }

    public synchronized void removeTimer(String id) {
        validateId(id);
        TaskData info = removeTask(id);
        if (info != null) {
            info.cancelTimeout();
            listenTimerEnd(info);
        } else {
            log.warn("task not found [{}]", id);
        }
    }

    @Override
    public synchronized void close() {
        executor.shutdownNow();
    }

    public synchronized boolean isEmpty() {
        return tasks.isEmpty();
    }

    public synchronized int size() {
        return tasks.size();
    }

    protected void listenTimerStart(TaskData info) {
        log.info("timer started for [{}]", info);
    }

    protected void listenTimerReset(TaskInfo coreData, TaskState before, TaskState after) {
        log.info("timer reset for core[{}] before[{}] after[{}]", coreData, before, after);
    }

    protected void listenTimerEnd(TaskData info) {
        log.info("timer ended for [{}]", info);
    }

    protected void listenTimeOut(String label, String id) {
        log.info("timer timed out for label[{}] id[{}]", label, id);
    }

    private void addTask(TaskData info) {
        synchronized (tasks) {
            Assert.isTrue(StringUtils.isNotBlank(info.info.id), "id is blank");
            tasks.put(info.info.id, info);
        }
    }

    private synchronized TaskData removeTask(String id) {
        synchronized (tasks) {
            Assert.isTrue(StringUtils.isNotBlank(id), "id is blank");
            return tasks.remove(id);
        }
    }

    private Instant getNow() {
        return clock.instant();
    }

    @Builder
    public record TaskInfo(String id, String label, Consumer<String> timeoutAction, Instant created) {
    }

    @Setter
    @Getter
    public static class TaskData {
        private final TaskInfo info;
        private TaskState state;

        public TaskData(TaskInfo info) {
            this.info = info;
        }

        public void cancelTimeout() {
            if (state != null) {
                state.cancelTimeout.run();
            }
        }
    }

    public record TaskState(
            TaskInfo info,
            Instant created,
            Runnable cancelTimeout,
            int resetCount) {

        public TaskState {
            Assert.isTrue(info != null, "info is null");
            Assert.isTrue(created != null, "created is null");
            Assert.isTrue(cancelTimeout != null, "cancelTimeout is null");
            Assert.isTrue(resetCount >= 0, "resetCount is negative");
        }
    }
}
