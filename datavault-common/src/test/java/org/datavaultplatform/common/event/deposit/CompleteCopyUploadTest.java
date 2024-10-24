package org.datavaultplatform.common.event.deposit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CompleteCopyUploadTest {

    @Nested
    class ArgumentValidationTests{
        
        @ParameterizedTest
        @CsvSource(nullValues = "null", textBlock = """
                null, jobId, test-type, 123, archiveStoreId, archiveId, 'The depositId cannot be null',
                depositId, null, test-type, 123, archiveStoreId, archiveId, 'The jobId cannot be null',
                depositId, jobId, null, 123, archiveStoreId, archiveId, 'The type cannot be null',
                depositId, jobId, test-type, null, null, archiveId, 'The archiveStoreId cannot be blank',
                depositId, jobId, test-type, null, archiveStoreId, null, 'The archiveId cannot be blank',
                depositId, jobId, test-type, null, '  ', archiveId, 'The archiveStoreId cannot be blank',
                depositId, jobId, test-type, null, archiveStoreId, '   ', 'The archiveId cannot be blank',
                """)
        void testCreate(String depositId, String jobId, String type, Integer chunkNumber, String archiveStoreId, String archiveId, String expectedMessage) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new CompleteCopyUpload(depositId, jobId, type, chunkNumber, archiveStoreId, archiveId));
            assertThat(ex).hasMessage(expectedMessage);
        }

        @Test
        void testNullChunkNumberIsOkay() {
            CompleteCopyUpload event = new CompleteCopyUpload("depositId","jobId","test-type",null,"archiveStoreId","archiveId");
            assertThat(event != null);
            assertThat(event.getChunkNumber()).isNull();
        }

        @Test
        void testAllNonNullArgs() {
            CompleteCopyUpload event = new CompleteCopyUpload("depositId","jobId","test-type",123,"archiveStoreId","archiveId");
            assertThat(event != null);
            assertThat(event.getDepositId()).isEqualTo("depositId");
            assertThat(event.getJobId()).isEqualTo("jobId");
            assertThat(event.getChunkNumber()).isEqualTo(123);
            assertThat(event.getArchiveStoreId()).isEqualTo("archiveStoreId");
            assertThat(event.getArchiveId()).isEqualTo("archiveId");}
    }
}