package org.datavaultplatform.worker.operations;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;


class DeviceTrackerTest {
    
    @Nested
    class FormatArchiveIdTests {
        
        @Test
        void testArchiveIdsWithDotTarDotChunkNumber(){
            String archiveId = "abcd.tar.1";
            String expected = "abcd.tar";
            assertThat(DeviceTracker.formatArchiveId(archiveId)).isEqualTo(expected);
        }
        @Test
        void testArchiveIdswuthDotChunkNumber(){
            String archiveId = "deposit-id123.3";
            String expected = "deposit-id123";
            assertThat(DeviceTracker.formatArchiveId(archiveId)).isEqualTo(expected);
        }

        @Test
        void testArchiveIdsWithNoDot() {
            String archiveId = "deposit123";
            assertThrows(StringIndexOutOfBoundsException.class, () -> DeviceTracker.formatArchiveId(archiveId));
        }
    }
}