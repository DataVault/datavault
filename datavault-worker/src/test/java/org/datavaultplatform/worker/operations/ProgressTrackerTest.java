package org.datavaultplatform.worker.operations;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.io.ProgressEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.common.io.ProgressEventType.BYTE_COUNT_INC;
import static org.datavaultplatform.common.io.ProgressEventType.START_TIME_SET;

@Slf4j
class ProgressTrackerTest {

    public static final String JOB_ID = "test-job-id";
    public static final String DEPOSIT_ID = "test-deposit-id";
    private static final long EXPECTED_BYTES = 2112;

    // progressEvents - created by track method
    List<ProgressEvent> progressEvents;
    
    // events - an UpdateProgress event is created every 250ms by tracker
    List<Event> events;

    @Test
    @SneakyThrows
    void testProgressTracker() {
        progressEvents = new ArrayList<>();
        events = new ArrayList<>();
        Progress progress = new Progress(progressEvents::add);
        EventSender eventSender = events::add;
        ProgressTracker progressTracker = new ProgressTracker(progress, JOB_ID, DEPOSIT_ID, EXPECTED_BYTES, eventSender);
        progressTracker.track(() -> {
            for (int i = 0; i < 5; i++) {
                TimeUnit.MILLISECONDS.sleep(500);
                progress.incByteCount(100);
            }
        });
        assertThat(progress.getByteCount()).isEqualTo(500);
        assertThat(progressEvents.size()).isEqualTo(6);
        assertThat(progressEvents.get(0).getType()).isEqualTo(START_TIME_SET);
        for (int i = 1; i < progressEvents.size(); i++) {
            ProgressEvent pe = progressEvents.get(i);
            assertThat(pe.getType()).isEqualTo(BYTE_COUNT_INC);
            assertThat(pe.getValue()).isEqualTo(100 * i);
        }
        assertThat(events.size()).isEqualTo(5);
        for (int i = 0; i < events.size(); i++) {
            UpdateProgress pe = (UpdateProgress) events.get(i);
            assertThat(pe.jobId).isEqualTo(JOB_ID);
            assertThat(pe.depositId).isEqualTo(DEPOSIT_ID);
            assertThat(pe.progress).isEqualTo(100 * (i + 1));
            assertThat(pe.progressMax).isEqualTo(2112);
            assertThat(pe.message).isEqualTo("Job progress update");
        }
        log.info("FIN");
    }
}