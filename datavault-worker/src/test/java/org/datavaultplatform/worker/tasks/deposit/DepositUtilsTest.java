package org.datavaultplatform.worker.tasks.deposit;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepositUtilsTest {

    @Nested
    class FinalEventTests {

        @Test
        void testNoChunkingNoEncryption() {
            check(false, false, ComputedDigest.class);            
        }

        @Test
        void testChunkingNoEncryption() {
            check(true, false, ComputedChunks.class);
        }

        @Test
        void testNoChunkingWithEncryption() {
            check(false, true, ComputedEncryption.class);
        }

        @Test
        void testChunkingWithEncryption() {
            check(true, true, ComputedEncryption.class);
        }


        void check(boolean isChunkingEnabled, boolean isEncryptionEnabled, Class<? extends Event> expectedLastEventClass) {
            assertThat(DepositUtils.getFinalPackageEvent(isChunkingEnabled, isEncryptionEnabled).getName()).isEqualTo(expectedLastEventClass.getName());
        }
        
    }
}