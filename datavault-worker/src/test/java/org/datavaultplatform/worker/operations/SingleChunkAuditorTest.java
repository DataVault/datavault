package org.datavaultplatform.worker.operations;


import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.audit.AuditError;
import org.datavaultplatform.common.event.audit.ChunkAuditComplete;
import org.datavaultplatform.common.event.audit.ChunkAuditStarted;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SingleChunkAuditorTest {

    @Mock
    Device mArchiveFS;
    @Mock
    EventSender mEventSender;

    @Mock
    Context mContext;

    static final String JOB_ID = "test-job-id";
    static final String AUDIT_ID = "test-audit-id";

    static final int CHUNK_IDX = 2112;

    static final byte[] CHUNK_IV = "test-chunk-iv".getBytes(StandardCharsets.UTF_8);

    static final String ENCRYPTED_CHUNK_DIGEST = "encrypted-chunk-digest";
    static final String DECRYPTED_CHUNK_DIGEST = "decrypted-chunk-digest";

    static final String BASE_CHUNK_ARCHIVE_ID = "base-chunk-archive-id";

    static final int TOTAL_NUMBER_CHUNKS = 234;

    static final String TEST_CHUNK_ID = "test-chunk-id";

    static final String TEST_CHUNK_ARCHIVE_ID = "base-chunk-archive-id.123";

    static final String TEST_LOCATION = "test-location";
    @Captor
    ArgumentCaptor<String> argChunkArchiveId;

    @Captor
    ArgumentCaptor<String> argPath;

    @Captor
    ArgumentCaptor<File> argWorking;
    @Captor
    ArgumentCaptor<Progress> argProgress;

    @Captor
    ArgumentCaptor<String> argLocation;

    @Captor
    ArgumentCaptor<File> argChunkFile;

    @Captor
    ArgumentCaptor<Event> argEvent;


    HashMap<String, String> properties = new HashMap() {{
        this.put(SingleChunkAuditor.PROP_ID, TEST_CHUNK_ID);
        this.put(SingleChunkAuditor.PROP_CHUNK_NUM, "123");
    }};

    @BeforeEach
    void setup() throws Exception {
        lenient().doNothing().when(mEventSender).send(argEvent.capture());
        lenient().when(mContext.getTempDir()).thenReturn(Paths.get("/tmp/dir"));
        lenient().doNothing().when(mArchiveFS).retrieve(argPath.capture(), argWorking.capture(), argProgress.capture());
        lenient().doNothing().when(mArchiveFS).retrieve(argPath.capture(), argWorking.capture(), argProgress.capture(), argLocation.capture());
    }

    @Test
    void testSingleCopyWithChunkIvSuccess() throws Exception {
        try (MockedStatic<Verify> verifyStatic = Mockito.mockStatic(Verify.class)) {
            try (MockedStatic<Encryption> encryptionStatic = Mockito.mockStatic(Encryption.class)) {

                verifyStatic.when(() -> Verify.getDigest(argChunkFile.capture()))
                        .thenReturn(ENCRYPTED_CHUNK_DIGEST, DECRYPTED_CHUNK_DIGEST);

                SingleChunkAuditor auditor = getAuditor(true, null);
                Boolean result = auditor.call();
                assertThat(result).isTrue();

                InOrder inOrderEventSender = Mockito.inOrder(mEventSender);

                File expectedChunkFile = Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile();
                File actualChunkFile1 = argChunkFile.getAllValues().get(0);
                File actualChunkFile2 = argChunkFile.getAllValues().get(1);
                assertThat(actualChunkFile1).isEqualTo(expectedChunkFile);
                assertThat(actualChunkFile2).isEqualTo(expectedChunkFile);

                verifyStatic.verify(() -> Verify.getDigest(expectedChunkFile), times(2));
                encryptionStatic.verify(() -> Encryption.decryptFile(mContext, expectedChunkFile, CHUNK_IV));

                List<Event> events = argEvent.getAllValues();

                assertThat(events.size()).isEqualTo(3);

                assertThat(events.get(0)).isInstanceOf(ChunkAuditStarted.class)
                        .extracting(Event::getMessage).isEqualTo("Audit Chunk Started: test-chunk-id");

                assertThat(events.get(1)).isInstanceOf(UpdateProgress.class)
                        .extracting(Event::getMessage).isEqualTo("Job progress update");

                assertThat(events.get(2)).isInstanceOf(ChunkAuditComplete.class)
                        .extracting(Event::getMessage).isEqualTo("Chunk Audit Complete: test-chunk-id");

                for (Event event : events) {
                    assertThat(event.getJobId()).isEqualTo(JOB_ID);
                    if (!(event instanceof UpdateProgress)) {
                        assertThat(event.getAuditId()).isEqualTo(AUDIT_ID);
                        assertThat(event.getChunkId()).isEqualTo(TEST_CHUNK_ID);
                        assertThat(event.getArchiveId()).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                        assertThat(event.getLocation()).isEqualTo(null);
                    }
                    inOrderEventSender.verify(mEventSender).send(event);
                }

                String actualPath = argPath.getValue();
                File actualWorking = argWorking.getValue();
                Progress actualProgress = argProgress.getValue();

                assertThat(actualPath).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                assertThat(actualWorking).isEqualTo(Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile());
                assertThat(actualProgress).isNull();

                verify(mArchiveFS).retrieve(actualPath, actualWorking, actualProgress);

            }
        }
    }

    @Test
    void testMultipleCopyWithChunkIvSuccess() throws Exception {
        try (MockedStatic<Verify> verifyStatic = Mockito.mockStatic(Verify.class)) {
            try (MockedStatic<Encryption> encryptionStatic = Mockito.mockStatic(Encryption.class)) {

                verifyStatic.when(() -> Verify.getDigest(argChunkFile.capture())).thenReturn(ENCRYPTED_CHUNK_DIGEST, DECRYPTED_CHUNK_DIGEST);

                SingleChunkAuditor auditor = getAuditor(false, TEST_LOCATION);
                Boolean result = auditor.call();
                assertThat(result).isTrue();

                InOrder inOrderEventSender = Mockito.inOrder(mEventSender);

                File expectedChunkFile = Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile();
                File actualChunkFile1 = argChunkFile.getAllValues().get(0);
                File actualChunkFile2 = argChunkFile.getAllValues().get(1);
                assertThat(actualChunkFile1).isEqualTo(expectedChunkFile);
                assertThat(actualChunkFile2).isEqualTo(expectedChunkFile);

                verifyStatic.verify(() -> Verify.getDigest(expectedChunkFile), times(2));
                encryptionStatic.verify(() -> Encryption.decryptFile(mContext, expectedChunkFile, CHUNK_IV));

                List<Event> events = argEvent.getAllValues();

                assertThat(events.size()).isEqualTo(3);

                assertThat(events.get(0)).isInstanceOf(ChunkAuditStarted.class)
                        .extracting(Event::getMessage).isEqualTo("Audit Chunk Started: test-chunk-id");

                assertThat(events.get(1)).isInstanceOf(UpdateProgress.class)
                        .extracting(Event::getMessage).isEqualTo("Job progress update");

                assertThat(events.get(2)).isInstanceOf(ChunkAuditComplete.class)
                        .extracting(Event::getMessage).isEqualTo("Chunk Audit Complete: test-chunk-id");

                for (Event event : events) {
                    assertThat(event.getJobId()).isEqualTo(JOB_ID);
                    if (!(event instanceof UpdateProgress)) {
                        assertThat(event.getAuditId()).isEqualTo(AUDIT_ID);
                        assertThat(event.getChunkId()).isEqualTo(TEST_CHUNK_ID);
                        assertThat(event.getArchiveId()).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                        assertThat(event.getLocation()).isEqualTo(TEST_LOCATION);
                    }
                    inOrderEventSender.verify(mEventSender).send(event);
                }

                String actualPath = argPath.getValue();
                File actualWorking = argWorking.getValue();
                Progress actualProgress = argProgress.getValue();
                String actualLocation = argLocation.getValue();

                assertThat(actualPath).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                assertThat(actualWorking).isEqualTo(Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile());
                assertThat(actualProgress).isNull();
                assertThat(actualLocation).isEqualTo(TEST_LOCATION);

                verify(mArchiveFS).retrieve(actualPath, actualWorking, actualProgress, actualLocation);
            }
        }
    }


    @Test
    void testMultipleCopyWithChunkIvVerify1Fails() throws Exception {
        try (MockedStatic<Verify> verifyStatic = Mockito.mockStatic(Verify.class)) {
            verifyStatic.when(() -> Verify.getDigest(argChunkFile.capture())).thenReturn("BAD_DIGEST");

                SingleChunkAuditor auditor = getAuditor(false, TEST_LOCATION);
                Boolean result = auditor.call();
                assertThat(result).isFalse();

                InOrder inOrderEventSender = Mockito.inOrder(mEventSender);

                File expectedChunkFile = Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile();
                File actualChunkFile1 = argChunkFile.getAllValues().get(0);
                assertThat(actualChunkFile1).isEqualTo(expectedChunkFile);

                verifyStatic.verify(() -> Verify.getDigest(expectedChunkFile), times(1));

                List<Event> events = argEvent.getAllValues();

                assertThat(events.size()).isEqualTo(3);

                assertThat(events.get(0)).isInstanceOf(ChunkAuditStarted.class)
                        .extracting(Event::getMessage).isEqualTo("Audit Chunk Started: test-chunk-id");

                assertThat(events.get(1)).isInstanceOf(UpdateProgress.class)
                        .extracting(Event::getMessage).isEqualTo("Job progress update");

                assertThat(events.get(2)).isInstanceOf(AuditError.class)
                    .extracting(Event::getMessage).isEqualTo("Encrypted checksum failed: BAD_DIGEST != encrypted-chunk-digest");

                for (Event event : events) {
                    assertThat(event.getJobId()).isEqualTo(JOB_ID);
                    if (!(event instanceof UpdateProgress || event instanceof AuditError)) {
                        assertThat(event.getAuditId()).isEqualTo(AUDIT_ID);
                        assertThat(event.getChunkId()).isEqualTo(TEST_CHUNK_ID);
                        assertThat(event.getArchiveId()).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                        assertThat(event.getLocation()).isEqualTo(TEST_LOCATION);
                    }
                    inOrderEventSender.verify(mEventSender).send(event);
                }

                String actualPath = argPath.getValue();
                File actualWorking = argWorking.getValue();
                Progress actualProgress = argProgress.getValue();
                String actualLocation = argLocation.getValue();

                assertThat(actualPath).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                assertThat(actualWorking).isEqualTo(Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile());
                assertThat(actualProgress).isNull();
                assertThat(actualLocation).isEqualTo(TEST_LOCATION);

                verify(mArchiveFS).retrieve(actualPath, actualWorking, actualProgress, actualLocation);
            }
    }


    @Test
    void testMultipleCopyWithChunkIvVerify2Fails() throws Exception {
        try (MockedStatic<Verify> verifyStatic = Mockito.mockStatic(Verify.class)) {
            try (MockedStatic<Encryption> encryptionStatic = Mockito.mockStatic(Encryption.class)) {

                AtomicInteger verifyCount = new AtomicInteger(0);
                verifyStatic.when(() -> Verify.getDigest(argChunkFile.capture())).thenAnswer(invocation -> {
                    if (verifyCount.incrementAndGet() == 1) {
                        return ENCRYPTED_CHUNK_DIGEST;
                    } else {
                        return "BAD_DIGEST";
                    }
                });

                SingleChunkAuditor auditor = getAuditor(false, TEST_LOCATION);
                Boolean result = auditor.call();
                assertThat(result).isFalse();

                InOrder inOrderEventSender = Mockito.inOrder(mEventSender);

                File expectedChunkFile = Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile();
                File actualChunkFile1 = argChunkFile.getAllValues().get(0);
                assertThat(actualChunkFile1).isEqualTo(expectedChunkFile);

                verifyStatic.verify(() -> Verify.getDigest(expectedChunkFile), times(2));
                encryptionStatic.verify(() -> Encryption.decryptFile(mContext, expectedChunkFile, CHUNK_IV));

                List<Event> events = argEvent.getAllValues();

                assertThat(events.size()).isEqualTo(3);

                assertThat(events.get(0)).isInstanceOf(ChunkAuditStarted.class)
                        .extracting(Event::getMessage).isEqualTo("Audit Chunk Started: test-chunk-id");

                assertThat(events.get(1)).isInstanceOf(UpdateProgress.class)
                        .extracting(Event::getMessage).isEqualTo("Job progress update");

                assertThat(events.get(2)).isInstanceOf(AuditError.class)
                        .extracting(Event::getMessage).isEqualTo("Decrypted checksum failed: BAD_DIGEST != decrypted-chunk-digest");

                for (Event event : events) {
                    assertThat(event.getJobId()).isEqualTo(JOB_ID);
                    if (!(event instanceof UpdateProgress || event instanceof AuditError)) {
                        assertThat(event.getAuditId()).isEqualTo(AUDIT_ID);
                        assertThat(event.getChunkId()).isEqualTo(TEST_CHUNK_ID);
                        assertThat(event.getArchiveId()).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                        assertThat(event.getLocation()).isEqualTo(TEST_LOCATION);
                    }
                    inOrderEventSender.verify(mEventSender).send(event);
                }

                String actualPath = argPath.getValue();
                File actualWorking = argWorking.getValue();
                Progress actualProgress = argProgress.getValue();
                String actualLocation = argLocation.getValue();

                assertThat(actualPath).isEqualTo(TEST_CHUNK_ARCHIVE_ID);
                assertThat(actualWorking).isEqualTo(Paths.get("/tmp/dir/base-chunk-archive-id.123").toFile());
                assertThat(actualProgress).isNull();
                assertThat(actualLocation).isEqualTo(TEST_LOCATION);

                verify(mArchiveFS).retrieve(actualPath, actualWorking, actualProgress, actualLocation);
            }
        }
    }


    private SingleChunkAuditor getAuditor(boolean singleCopy, String location) {
        SingleChunkAuditor auditor = new SingleChunkAuditor(mContext, mEventSender, mArchiveFS,
                JOB_ID, AUDIT_ID,
                BASE_CHUNK_ARCHIVE_ID, CHUNK_IDX,
                ENCRYPTED_CHUNK_DIGEST, DECRYPTED_CHUNK_DIGEST,
                CHUNK_IV, properties,
                singleCopy, location,
                TOTAL_NUMBER_CHUNKS);
        return auditor;
    }
}