package org.datavaultplatform.worker.operations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.io.Progress;

/**
 * A class to track the progress of a deposit (or maybe any Task not sure)
 */
@Slf4j
public class ProgressTracker implements Runnable {
    
    private static final int SLEEP_INTERVAL_MS = 250;
    private AtomicBoolean active = new AtomicBoolean(true);
    private long lastByteCount = 0;
    private final long expectedBytes;

    private final Progress progress;
    private final String jobId;
    private final String depositId;
    private final EventSender eventSender;
    
    /**
     * ProgressTracker constructor
     * @param progress The progress object
     * @param jobId Identifier for the job
     * @param depositId Identifier for the deposit
     * @param expectedBytes The expected size of the deposit
     * @param eventSender The event sender
     */
    public ProgressTracker(Progress progress, String jobId, String depositId, long expectedBytes, EventSender eventSender) {
        this.progress = progress;
        this.jobId = jobId;
        this.depositId = depositId;
        this.expectedBytes = expectedBytes;
        this.eventSender = eventSender;
    }
    
    /**
     * Stop method for threading
     */
    public void stop() {
        active.set(false);
    }
    
    /**
     * Update progress to the logs and broker
     */
    void reportProgress() {
        
        long dirCount = progress.getDirCount();
        long fileCount = progress.getFileCount();
        long byteCount = progress.getByteCount();
        long eventTime = progress.getTimestamp();
        long startTime = progress.getStartTime();
        
        if (byteCount != lastByteCount) {
            
            // Calculate the transfer rate and the estimate time remaining
            long msElapsed = eventTime - startTime;
            long secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(msElapsed);
            long bytesPerSec = 0;
            if (secondsElapsed > 0) {
                bytesPerSec = (long)((double)byteCount / secondsElapsed);
            }
            
            String message = "Transferred " +
                             FileUtils.byteCountToDisplaySize(byteCount) +
                             " of " +
                             FileUtils.byteCountToDisplaySize(expectedBytes) +
                             " (" + FileUtils.byteCountToDisplaySize(bytesPerSec) + "/sec)";
            
            log.info(message);
            
            // Signal progress to the broker
            
            UpdateProgress updateState = new UpdateProgress(jobId, depositId, byteCount, expectedBytes, message);
            lastByteCount = byteCount;
            eventSender.send(updateState);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        
        progress.setStartTime(System.currentTimeMillis());
        
        try {
            while(active.get()) {
                reportProgress();
                TimeUnit.MILLISECONDS.sleep(SLEEP_INTERVAL_MS);
            }
        } catch (Exception e) {
            log.error("Error in progress tracker", e);
        } finally {
            // Report final counts before exiting
            reportProgress();
        }
    }
    
    public void track(Trackable trackable) throws Exception {
        Thread trackerThread = new Thread(this);
        trackerThread.start();
        try {
            trackable.track();
        } finally {
            // Stop the tracking thread
            stop();
            trackerThread.join();
        }
    }
}