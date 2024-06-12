package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.io.Progress;

public record ProgressTrackerWrapper(Progress progress, String jobID, String depositId, long expectedBytes, EventSender eventSender) {

    public void performActionTrackingProgress(Trackable trackable) throws Exception {
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, expectedBytes, eventSender);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();
        try {
            trackable.track();
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
        }
    }

}
