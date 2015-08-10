package org.datavault.worker.operations;

import org.datavault.common.io.Progress;

public class ProgressTracker implements Runnable {
    
    private static final int SLEEP_INTERVAL_MS = 100;
    private boolean active = true;
    Progress progress;
    
    public ProgressTracker(Progress progress) {
        this.progress = progress;
    }
    
    public void stop() {
        active = false;
    }
    
    void reportProgress() {
        System.out.println("\tCopied: " + progress.dirCount + " dirs, "
                                        + progress.fileCount + " files, "
                                        + progress.byteCount + " bytes");
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