package org.datavaultplatform.worker.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.datavaultplatform.common.storage.impl.TivoliStorageManager.DSMC;

/**
 * This code will be run in the Worker after processing each WorkerTask message.
 * Each WorkerTask should have no forked-processes running after each WorkerTask message has been processed (success or fail).
 * This code is a catch-all. It should only find Processes to delete if there's a bug in the normal ProcessHelper/TaskExecutorclean-up logic.
 * Unlike the normal forked-process clean-up, it doesn't attempt to wait nicely for process exit or log any forked-process output.
 */
public class OperatingSystemChildProcessManagerImpl implements OperatingSystemChildProcessManager {

    public static final Logger LOG = LoggerFactory.getLogger(OperatingSystemChildProcessManagerImpl.class);

    public void findAndStopChildProcesses(boolean stopChildProcesses) {
        try {
            findAndStopChildProcessesInternal(stopChildProcesses);
        } catch (RuntimeException rte) {
            LOG.warn("ignoring error thrown in cleanup", rte);
        }
    }

    private void findAndStopChildProcessesInternal(boolean stopChildProcesses) {

        // 1. Get all living descendants of the CURRENT JVM
        // This ignores unrelated system processes (like Chrome, Docker, etc.)
        var stillRunningChildProcesses = getStillRunningChildProcesses();
        if (stillRunningChildProcesses.isEmpty()) {
            LOG.info("Clean shutdown: No still running processes found.");
        } else {
            LOG.error("CRITICAL: Found [{}] leaked processes!", stillRunningChildProcesses.size());
            for (ProcessHandle ph : stillRunningChildProcesses) {
                processChildProcessHandle(ph, stopChildProcesses);
            }
        }
    }

    void processChildProcessHandle(ProcessHandle ph, boolean stopChildProcesses) {
        String desc = getProcessDescription(ph);
        if (desc.contains(DSMC)) {
            if (stopChildProcesses) {
                // these processes have somehow escaped the normal shutdown logic
                LOG.warn("KILLING dsmc PROCESS({}) [{}]", ph.pid(), desc);
                boolean killed = forceKillAndVerify(ph);
                LOG.warn("KILLED? dsmc PROCESS({}) {} [{}] ", ph.pid(), killed, desc);
            } else {
                LOG.warn("NOT KILLING dsmc PROCESS({}}) [{}}]", ph.pid(), desc);
            }
        } else {
            LOG.warn("NOT KILLING non-dsmc PROCESS({}}) [{}}]", ph.pid(), desc);
        }
    }

    public String getProcessDescription(ProcessHandle ph) {
        ProcessHandle.Info info = ph.info();

        Optional<String> cmdLineOpt = info.commandLine();

        // 1. Try the full command line first (Best for 'stdbuf' etc.)
        if (cmdLineOpt.isPresent()) {
            return cmdLineOpt.get();
        }

        Optional<String> cmdOpt = info.command();
        // 2. Fallback: Reconstruct it from Command + Arguments
        if (cmdOpt.isPresent()) {
            String cmd = cmdOpt.get();
            String args = info.arguments()
                    .map(a -> String.join(" ", a))
                    .orElse("");
            return cmd + " " + args;
        }

        // 3. Last Resort: Just the PID
        return "Unknown Process (PID=" + ph.pid() + ")";
    }

    public boolean forceKillAndVerify(ProcessHandle handle) {

        // 1. Send the Nuclear Option (SIGKILL)
        handle.destroyForcibly();

        // 2. The Critical Wait (e.g. 2 seconds)
        // We give the OS kernel time to tear down the process.
        // CompletableFuture.get() blocks efficiently until death OR timeout.
        try {
            handle.onExit().get(2, TimeUnit.SECONDS);
            return true; // Success: Process is confirmed dead
        } catch (TimeoutException e) {
            // 3. The "Check Again"
            // If we timed out, check one last time explicitly
            if (handle.isAlive()) {
                LOG.error("CRITICAL: Process PROCESS({}) is stuck in Kernel/Zombie state!", handle.pid());
                return false; // Failed: It is truly stuck
            }
            return true; // Success: It died right at the finish line
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * By using this method - we can override it with Mockito for unit testing.
     *
     * @return a list of ProcessHandles for child processes
     */
    List<ProcessHandle> getStillRunningChildProcesses() {
        return ProcessHandle.current()
                .descendants()
                .toList();
    }
}
