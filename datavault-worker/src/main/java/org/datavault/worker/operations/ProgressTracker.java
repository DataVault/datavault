package org.datavault.worker.operations;

import org.datavault.common.event.deposit.TransferProgress;
import org.datavault.common.io.Progress;
import org.datavault.worker.queue.EventSender;

public class ProgressTracker implements Runnable {
    
    private static final int SLEEP_INTERVAL_MS = 250;
    private boolean active = true;
    
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
        
        System.out.println("\tCopied: " + dirCount + " dirs, "
                                        + fileCount + " files, "
                                        + byteCount + " bytes");
        
        TransferProgress progressEvent = new TransferProgress(depositId, byteCount);
        eventSender.send(progressEvent);
    }
    
    @Override
    public void run() {
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