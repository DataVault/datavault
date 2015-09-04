package org.datavaultplatform.worker.operations;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.event.deposit.TransferProgress;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.worker.queue.EventSender;

public class ProgressTracker implements Runnable {
    
    private static final int SLEEP_INTERVAL_MS = 250;
    private boolean active = true;
    private long lastByteCount = 0;
    private long startTime = 0;
    
    Progress progress;
    String depositId;
    EventSender eventSender;
    
    public ProgressTracker(Progress progress, String depositId, EventSender eventSender) {
        this.progress = progress;
        this.depositId = depositId;
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
        
        if (byteCount != lastByteCount) {
            
            // Calcuate the transfer rate and the estimate time remaining
            long msElapsed = (eventTime - startTime);
            double secondsElapsed = (double)msElapsed / 1000.0;
            long bytesPerSec = 0;
            if (secondsElapsed > 0) {
                bytesPerSec = (long)((double)byteCount / secondsElapsed);
            }
            
            System.out.println("\tCopied: " + dirCount + " dirs, "
                                            + fileCount + " files, "
                                            + byteCount + " bytes ("
                                            + FileUtils.byteCountToDisplaySize(byteCount) + ") @ "
                                            + bytesPerSec + " bytes ("
                                            + FileUtils.byteCountToDisplaySize(bytesPerSec) + ") / sec. "
                                            + "Elapsed time: " + msElapsed + " ms");
            
            // Signal progress to the broker
            TransferProgress progressEvent = new TransferProgress(depositId, byteCount, bytesPerSec);
            lastByteCount = byteCount;
            eventSender.send(progressEvent);
        }
    }
    
    @Override
    public void run() {
        
        startTime = System.currentTimeMillis();
        
        try {
            while(active) {
                reportProgress();
                Thread.sleep(SLEEP_INTERVAL_MS);
            }
            
            // Report final counts before exiting
            reportProgress();
            
        } catch (Exception e) {
            // ...
        }
    }
}