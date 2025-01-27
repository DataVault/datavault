package org.datavaultplatform.common.storage.impl;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.util.ProcessHelper;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class TSMTracker implements Callable<String> {

    private final String location;
    private final File working;
    private final Progress progress;
    private final String description;
    private final int maxRetries;
    private final int retryTimeSecs;

    public TSMTracker(String location, File working, Progress progress, String description, int maxRetries, int retryTimeSecs) {
        this.location = location;
        this.working = working;
        this.progress = progress;
        this.description = description;
        this.maxRetries = maxRetries;
        this.retryTimeSecs = retryTimeSecs;
    }

    @Override
    public String call() throws Exception {
        try {
            return this.storeInTSMNode();
        } catch (Exception ex) {
            String msg = String.format("Storing [%s] in TSM location[%s] failed.", description, location);
            throw new Exception(msg, ex);
        }
    }
    
    private String storeInTSMNode() throws Exception {
        boolean stored = false;
        for (int r = 0; r < maxRetries && !stored; r++) {
            String attemptCtx = String.format("attempt[%s/%s]", r+1, maxRetries);
            ProcessHelper.ProcessInfo info = getProcessInfo("tsmStore",
                    "dsmc", "archive", working.getAbsolutePath(), "-description=" + description, "-optfile=" + location);
            if (info.wasSuccess()) {
                stored = true;
                String msg = String.format("Deposit of [%s] succeeded using location[%s]%s", working.getAbsolutePath(), location, attemptCtx);
                log.info(msg);
            } else {
                String errMsg = String.format("Deposit of [%s] failed using location[%s]%s", working.getAbsolutePath(), location, attemptCtx);
                TivoliStorageManager.logProcessOutput(info, errMsg);
                boolean lastAttempt = r == (maxRetries - 1);
                if (lastAttempt) {
                    throw new Exception(errMsg);
                }
                log.info("{}  Retrying in [{}] mins", errMsg, retryTimeSecs);
                TimeUnit.SECONDS.sleep(retryTimeSecs);
            }
        }
        return description;
    }

    /*
     * This allows creation of ProcessInfo to be faked during unit tests.
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    protected ProcessHelper.ProcessInfo getProcessInfo(String desc, String... commands) throws Exception {
        ProcessHelper helper = new ProcessHelper(desc, commands);
        ProcessHelper.ProcessInfo info = helper.execute();
        return info;
    }
}
