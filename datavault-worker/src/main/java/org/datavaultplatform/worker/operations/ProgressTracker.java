package org.datavaultplatform.worker.operations;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.worker.queue.EventSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressTracker implements Runnable {
    
    private static final int SLEEP_INTERVAL_MS = 250;
    private boolean active = true;
    private long lastByteCount = 0;
    private long expectedBytes = 0;
    
    Progress progress;
    String jobId;
    String depositId;
    EventSender eventSender;
    
    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class);
    
    public ProgressTracker(Progress progress, String jobId, String depositId, long expectedBytes, EventSender eventSender) {
        this.progress = progress;
        this.jobId = jobId;
        this.depositId = depositId;
        this.expectedBytes = expectedBytes;
        this.eventSender = eventSender;
    }
    
    public void stop() {
        active = false;
    }
    
    void reportProgress() {
        
        long dirCount = progress.dirCount;
        long fileCount = progress.fileCount;
        long byteCount = progress.byteCount;
        long eventTime = progress.timestamp;
        long startTime = progress.startTime;
        
        if (byteCount != lastByteCount) {
            
            // Calcuate the transfer rate and the estimate time remaining
            long msElapsed = (eventTime - startTime);
            double secondsElapsed = (double)msElapsed / 1000.0;
            long bytesPerSec = 0;
            if (secondsElapsed > 0) {
                bytesPerSec = (long)((double)byteCount / secondsElapsed);
            }
            
            String message = "Transferred " +
                             FileUtils.byteCountToDisplaySize(byteCount) +
                             " of " +
                             FileUtils.byteCountToDisplaySize(expectedBytes) +
                             " (" + FileUtils.byteCountToDisplaySize(bytesPerSec) + "/sec)";
            
            logger.info(message);
            
            // Signal progress to the broker
            
            UpdateProgress updateState = new UpdateProgress(jobId, depositId, byteCount, expectedBytes, message);
            lastByteCount = byteCount;
            eventSender.send(updateState);
        }
    }
    
    @Override
    public void run() {
        
        progress.startTime = System.currentTimeMillis();
        
        try {
            while(active) {
                reportProgress();
                Thread.sleep(SLEEP_INTERVAL_MS);
            }
            
            // Report final counts before exiting
            reportProgress();
            
        } catch (Exception e) {
            logger.error("Error in progress tracker", e);
        }
    }
}