package org.datavaultplatform.worker.queue;

public interface OperatingSystemChildProcessManager {
    void findAndStopChildProcesses(boolean stopChildProcesses);
}
