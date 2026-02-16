package org.datavaultplatform.common.util;


import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.task.TaskConfig;
import org.datavaultplatform.common.task.TaskConfigTL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;


@SuppressWarnings("deprecation")
public class ProcessHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessHelper.class);
    private static final int NUMBER_OF_LAST_LINES_TO_LOG = 1000;


    private final String[] commands;
    private final String desc;
    private final Duration maxProcessDuration;
    private final Path outPath;
    private final AtomicBoolean executed = new AtomicBoolean(false);

    public ProcessHelper(String description, String... commands) throws IOException {
        this(description, TaskConfig.DEFAULT_PROCESS_MAX_DURATION, commands);
    }

    public ProcessHelper(String desc, Duration maxProcessDuration, String... commands) throws IOException {
        Objects.requireNonNull(desc, "description must not be null");
        Objects.requireNonNull(maxProcessDuration, "maxProcessDuration must not be null");
        Objects.requireNonNull(commands, "commands must not be null");
        Assert.state(commands.length > 0, "commands must not be empty");

        if (maxProcessDuration.toMillis() < 1) {
            throw new IllegalArgumentException("Duration must be at least 1ms");
        }

        this.desc = desc;
        this.commands = commands.clone();
        this.maxProcessDuration = maxProcessDuration;
        this.outPath = Files.createTempFile("output_", ".log");
    }

    /**
     * Interrupting this method should shut down the process.
     */
    public ProcessInfo execute() throws InterruptedException, IOException, TimeoutException {
        if (!this.executed.compareAndSet(false, true)) {
            throw new IllegalStateException("Already executed once");
        }

        Process p = null;
        long startNanos = System.nanoTime();

        try {
            ProcessBuilder pb = new ProcessBuilder(commands)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.to(outPath.toFile()));

            p = pb.start();

            // we are not sending streaming input to the process so best if we close the stream
            IOUtils.closeQuietly(p.getOutputStream());

            if (LOG.isInfoEnabled()) {
                String commandStr = String.join(" ", commands);
                LOG.info("pid[{}] Process desc[{}] commands[{}]", safePid(p), desc, commandStr);
                LOG.info("pid[{}] temp file is [{}]", safePid(p), outPath.toAbsolutePath());
            }

            boolean finished = p.waitFor(maxProcessDuration.toMillis(), TimeUnit.MILLISECONDS);

            if (!finished) {
                boolean forcedToShutdown = terminateProcess(p);
                throw new TimeoutException(
                        "OS process desc[%s]pid[%s]TimedOutAfter[%s]forcedToShutdown[%b]"
                                .formatted(desc, safePid(p), maxProcessDuration, forcedToShutdown)
                );
            }

            List<String> lines = getLines();
            logProcessOutput(p, lines);

            Duration duration = Duration.ofNanos(System.nanoTime() - startNanos);
            return new ProcessInfo(desc, safePid(p), safeExitValue(p), lines, duration);
        } catch (InterruptedException ex) {
            cleanupAfterException(p, ex);
            Thread.currentThread().interrupt();
            throw ex;
        } catch (TimeoutException | IOException | RuntimeException ex) {
            cleanupAfterException(p, ex);
            throw ex;
        } finally {
            try {
                Files.deleteIfExists(outPath);
            } catch (IOException ex) {
                LOG.warn("Failed to delete temp process log file: {}", outPath, ex);
            }
        }
    }

    /**
     * Centralized cleanup used from exception handlers:
     * - best-effort terminate the process
     * - read the output file
     * - log process output and exception
     * This method swallows InterruptedException from 'terminateProcess()' attempts,
     * but will set the thread interrupt flag if it occurs.
     */
    private void cleanupAfterException(Process p, Exception ex) {
        if (p != null && p.isAlive()) {
            try {
                terminateProcess(p);
            } catch (InterruptedException ie) {
                LOG.warn("pid[{}] - Interrupted while terminating process during cleanup", safePid(p));
                Thread.currentThread().interrupt();
                // continue to read logs and rethrow the original exception in caller
            }
        }

        logProcessOutput(p, getLines(), ex);
    }
    
    private long safePid(Process p) {
        return (p == null) ? -1L : p.pid();
    }

    private int safeExitValue(Process p) {
        if (p == null) {
            return -1;
        }
        if (!p.isAlive()) {
            try {
                return p.exitValue();
            } catch (IllegalThreadStateException ignored) {
                // Should not happen if !isAlive(), but be defensive
            }
        }
        return -1;
    }

    private void logAtLevel(LoggingLevel level, String msg, Object... args) {
        if (level.equals(LoggingLevel.WARN)) {
            LOG.warn(msg, args);
        } else {
            LOG.info(msg, args); // fallback if needed
        }
    }

    private void logProcessOutput(Process p, List<String> outputLines) {
        this.logProcessOutput(p, outputLines, null);
    }

    private void logProcessOutput(Process p, List<String> outputLines, Exception ex) {
        int exitCode = safeExitValue(p);
        final LoggingLevel level = (exitCode == ProcessExitCodes.EXIT_SUCCESS) ? LoggingLevel.INFO : LoggingLevel.WARN;
        long pid = safePid(p);

        if (ex != null) {
            logAtLevel(level,
                    "pid[{}] OS process desc[{}] exitCode[{}] exception[{}]",
                    pid, desc, ProcessExitCodes.getExitCodeString(exitCode), ex.toString());
        } else {
            logAtLevel(level,
                    "pid[{}] OS process desc[{}] exitCode[{}] NO EXCEPTION",
                    pid, desc, ProcessExitCodes.getExitCodeString(exitCode));
        }

        logAtLevel(level, "pid[{}] out-start", pid);
        for (String outputLine : outputLines) {
            logAtLevel(level, "pid[{}] out {}", pid, outputLine);
        }
        logAtLevel(level, "pid[{}] out-end", pid);
    }

    private List<String> getLines() {
        if (!Files.exists(outPath)) {
            return Collections.emptyList();
        }

        // Using Apache Commons IO ReversedLinesFileReader for O(1) tail reading
        // instead of O(N) full file scan.
        try (var reader = new org.apache.commons.io.input.ReversedLinesFileReader(outPath.toFile(), java.nio.charset.StandardCharsets.UTF_8)) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null && result.size() < NUMBER_OF_LAST_LINES_TO_LOG) {
                result.add(line);
            }
            Collections.reverse(result); // Put back in correct order
            return result;
        } catch (IOException e) {
            LOG.warn("Error reading process log file: {}", outPath, e);
            return Collections.emptyList();
        }
    }

    /**
     * Terminates a unix process.
     *
     * @param process the process to terminate
     * @return true if forced to shut down after SIGTERM timeout
     */
    private boolean terminateProcess(Process process) throws InterruptedException {
        if (process == null || !process.isAlive()) {
            return false;
        }

        long pid = safePid(process);
        boolean forcedToShutDown = false;

        // 1. Snapshot the descendants BEFORE we kill the parent
        // We need a list because the stream is a live view that might change
        List<ProcessHandle> descendants = process.descendants().toList();

        try {
            LOG.info("pid[{}] - Sending SIGTERM to parent and [{}] descendants", pid, descendants.size());

            // 2. Terminate the whole tree gracefully
            // It's often best to stop children first, or parent first depending on app. 
            // Sending to all is usually safest for a "shut down now" scenario.
            descendants.forEach(ProcessHandle::destroy);
            process.destroy();

            // 3. Wait for the PARENT to die. 
            // Usually if parent dies, we assume success, but strictly speaking children might linger.
            if (!process.waitFor(TaskConfigTL.get().getProcessSigTermTimeoutDuration().toMillis(), TimeUnit.MILLISECONDS)) {
                LOG.warn("pid[{}] - process did not respond to SIGTERM, sending SIGKILL", pid);
                forcedToShutDown = true;

                // Pass the known descendants to forceShutdown so we don't lose track of them
                forceShutdown(process, descendants);
            }
        } catch (InterruptedException ex) {
            LOG.warn("pid[{}] - Interrupted during terminateProcess. Escalating to SIGKILL.", pid);
            forceShutdown(process, descendants);
            Thread.currentThread().interrupt();
            throw ex;
        }
        return forcedToShutDown;
    }

    private void forceShutdown(Process process, List<ProcessHandle> knownDescendants) {
        if (process == null || !process.isAlive()) {
            // Even if parent is dead, ensure children are dead
            if (knownDescendants != null) {
                knownDescendants.stream().filter(ProcessHandle::isAlive).forEach(ph -> {
                    LOG.info("pid[{}] - Parent dead but child alive. SIGKILL child.", ph.pid());
                    ph.destroyForcibly();
                });
            }
            return;
        }

        long pid = safePid(process);
        try {
            LOG.info("pid[{}] - Sending SIGKILL to hierarchy", pid);

            // 1. Force kill everything
            if (knownDescendants != null) {
                knownDescendants.forEach(ProcessHandle::destroyForcibly);
            }
            process.destroyForcibly();

            // 2. Wait for parent death
            boolean killed = process.waitFor(TaskConfigTL.get().getProcessPostSigKillTimeoutDuration().toMillis(), TimeUnit.MILLISECONDS);
            if (killed) {
                LOG.info("pid[{}] - process successfully killed with SIGKILL", pid);
            } else {
                LOG.error("pid[{}] - process may be a zombie - still not terminated after SIGKILL", pid);
            }
        } catch (InterruptedException e) {
            LOG.error("pid[{}] - Interrupted while waiting for SIGKILL cleanup", pid);
            Thread.currentThread().interrupt();
            // Best effort: ensure kill signal is sent one last time before giving up
            process.destroyForcibly();
        }
    }

    /**
     * Slf4j does not define logging levels.  
     */
    public enum LoggingLevel {
        INFO,
        WARN
    }
}