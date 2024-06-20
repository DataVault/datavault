package org.datavaultplatform.worker.tasks.deposit;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepositStateTest {

    @Test
     void testGetDepositStates() {
             ArrayList<String> expected = new ArrayList<>();
             expected.add("Calculating size");     // 0
             expected.add("Transferring");         // 1
             expected.add("Packaging");            // 2
             expected.add("Storing in archive");   // 3
             expected.add("Verifying");            // 4
             expected.add("Complete");             // 5
        
        assertThat(DepositState.getDepositStates()).isEqualTo(expected);
     }

    List<Boolean> checkEvent(Event lastEvent) {
        List<Class<? extends Event>> eventClazzes = List.of(ComputedSize.class, TransferComplete.class, PackageComplete.class, ComputedDigest.class, ComputedChunks.class, ComputedEncryption.class, UploadComplete.class, ValidationComplete.class, Complete.class);
        List<Boolean> result = new ArrayList<>();
        for (Class<? extends Event> eventClazz : eventClazzes) {
            boolean isBefore = DepositEvents.INSTANCE.isLastEventBefore(lastEvent, eventClazz);
            if (isBefore) {
                System.out.printf("The event [%s] is before [%s]%n", lastEvent.getClass().getSimpleName(), eventClazz.getSimpleName());
            } else {
                System.out.printf("The event [%s] is NOT before [%s]%n", lastEvent.getClass().getSimpleName(), eventClazz.getSimpleName());
            }
            result.add(isBefore);
        }
        return result;
    }

    @Test
    void testComputedEncryption() {
        assertThat(checkEvent(new ComputedEncryption())).isEqualTo(List.of(false, false, false, false, false, false, true, true, true));
    }
}