package org.datavaultplatform.common.storage.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.io.Progress;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
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
        // @TODO check params all set

        return this.storeInTSMNode(working, progress, location, description);
    }


    private String storeInTSMNode(File working, Progress progress, String location, String description) throws Exception {

        log.info("Store command is " + "dsmc" + " archive " + working.getAbsolutePath() +  " -description=" + description + " -optfile=" + location);
        ProcessBuilder pb = new ProcessBuilder("dsmc", "archive", working.getAbsolutePath(), "-description=" + description, "-optfile=" + location);
        //pb.directory(path);
        for (int r = 0; r < maxRetries; r++) {
            Process p = pb.start();

            // This class is already running in its own thread so it can happily pause until finished.
            p.waitFor();

            if (p.exitValue() != 0) {
                log.info("Deposit of " + working.getName() + " using " + location + " failed. ");
                InputStream error = p.getErrorStream();
                if (error != null) {
                    log.info(IOUtils.toString(error, StandardCharsets.UTF_8));
                }
                InputStream output = p.getInputStream();
                if (output != null) {
                    log.info(IOUtils.toString(output, StandardCharsets.UTF_8));
                }
                if (r == (maxRetries -1)) {
                    throw new Exception("Deposit of " + working.getName() + " using " + location + " failed. ");
                }
                log.info("Deposit of " + working.getName() + " using " + location + " failed.  Retrying in " + retryTimeMins + " mins");
                TimeUnit.MINUTES.sleep(retryTimeMins);
            } else {
                break;
            }
        }
        return description;
    }

    public File getWorking() {
        return this.working;
    }

    public Progress getProgress() {
        return this.progress;
    }

    public String getDescription() {
        return this.description;
    }

}
