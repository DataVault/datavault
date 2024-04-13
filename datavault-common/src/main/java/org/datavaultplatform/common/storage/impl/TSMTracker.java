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
    private final int retryTimeMins;

    public TSMTracker(String location, File working, Progress progress, String description, int maxRetries, int retryTimeMins) {
        this.location = location;
        this.working = working;
        this.progress = progress;
        this.description = description;
        this.maxRetries = maxRetries;
        this.retryTimeMins = retryTimeMins;
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
        ProcessHelper helper = new ProcessHelper("tsmStore",
                "dsmc", "archive", working.getAbsolutePath(), "-description=" + description, "-optfile=" + location);
        boolean stored = false;
        for (int r = 0; r < maxRetries && !stored; r++) {
            ProcessHelper.ProcessInfo info = helper.execute();
            if (info.wasSuccess()) {
                stored = true;
            } else {
                String errMsg = String.format("Deposit of [%s] failed using location[%s]", working.getName(), location);
                TivoliStorageManager.logProcessOutput(info, errMsg);
                boolean lastAttempt = r == (maxRetries - 1);
                if (lastAttempt) {
                    throw new Exception(errMsg);
                }
                log.info("{}  Retrying in [{}] mins", errMsg, retryTimeMins);
                TimeUnit.MINUTES.sleep(retryTimeMins);
            }
        }
        return description;
    }
}
