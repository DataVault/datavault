package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.audit.AuditComplete;
import org.datavaultplatform.common.event.audit.AuditStart;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.SingleChunkAuditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class AuditTest {

    private static final String TEST_AUDIT_ID = "test-audit-id";
    private static final String TEST_JOB_ID = "test-job-id";

    private static final String TEST_BASE_ARCHIVE_ID = "archive-id-for";
    @Mock
    private Context mContext;

    @Mock
    private EventSender mEventSender;

    @Mock
    private StorageClassNameResolver mResolver;

    @Mock
    private Device mDevice;

    @Mock
    private ArchiveStore mArchiveStore;

    @Mock
    private TaskExecutor<Boolean> mTaskExecutor;

    @Captor
    ArgumentCaptor<Integer> argNumThreads;
    @Captor
    ArgumentCaptor<String> argFailedMessage;

    @Captor
    ArgumentCaptor<Event> argEvent;

    @Captor
    ArgumentCaptor<Callable<Boolean>> argExecutorCallable;

    @BeforeEach
    void setup() {
        Mockito.lenient().doNothing().when(mEventSender).send(argEvent.capture());
        Mockito.lenient().when(mContext.getEventSender()).thenReturn(mEventSender);
        Mockito.lenient().when(mContext.getStorageClassNameResolver()).thenReturn(mResolver);
        Mockito.lenient().when(mContext.getTempDir()).thenReturn(Paths.get("/tmp/dir"));

        Mockito.lenient().when(mArchiveStore.getStorageClass()).thenReturn("archive-store-storage-class");
        Mockito.lenient().when(mArchiveStore.getID()).thenReturn("archive-store-id");
        Mockito.lenient().when(mArchiveStore.getLabel()).thenReturn("archive-store-label");
        Mockito.lenient().when(mArchiveStore.getProperties()).thenReturn(new HashMap<>());
        Mockito.lenient().doNothing().when(mTaskExecutor).add(argExecutorCallable.capture());

    }

    private Audit setupAuditNoSpy(boolean redeliver, int totalNumberChunks) {
        Audit audit = new Audit();
        audit.setJobID(TEST_JOB_ID);
        audit.setArchiveFileStores(Collections.singletonList(mArchiveStore));
        audit.setIsRedeliver(redeliver);
        Map<String, String> properties = new HashMap<>();
        audit.setProperties(properties);
        audit.getProperties().put(PropNames.AUDIT_ID, TEST_AUDIT_ID);

        List<HashMap<String, String>> chunksToAuditProperties = new ArrayList<>();

        String[] archiveIds = new String[totalNumberChunks];
        audit.setChunksIVs(new HashMap<>());
        audit.setChunkFilesDigest(new HashMap<>());
        audit.setEncChunksDigest(new HashMap<>());

        for (int idx = 0; idx < totalNumberChunks; idx++) {
            HashMap<String, String> chunkProps = getChunkProperties(idx);
            chunksToAuditProperties.add(chunkProps);

            archiveIds[idx] = TEST_BASE_ARCHIVE_ID + idx;
            audit.getChunksIVs().put(idx, ("chunk-iv-for" + idx).getBytes(StandardCharsets.UTF_8));
            audit.getEncChunksDigest().put(idx, "enc-chunk-digest-for" + idx);
            audit.getChunkFilesDigest().put(idx, "chunk-digest-for" + idx);
        }
        audit.setArchiveIds(archiveIds);
        audit.setChunksToAudit(chunksToAuditProperties);
        return audit;
    }

    private Audit setupAudit(boolean redeliver, int totalNumberChunks){
        Audit audit = setupAuditNoSpy(redeliver, totalNumberChunks);
        Audit spyResult = Mockito.spy(audit);
        Mockito.lenient().doReturn(mTaskExecutor).when(spyResult).createTaskExecutor(argNumThreads.capture(), argFailedMessage.capture());
        return spyResult;
    }

    private HashMap<String, String> getChunkProperties(int idx) {
        HashMap<String, String> result = new HashMap<>();
        result.put(PropNames.CHUNK_NUM, String.valueOf(idx + 1));
        result.put(PropNames.CHUNK_ID, "chunk-id-for-" + idx);
        return result;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testAuditSuccessSingleLocation(int numberOfChunks) throws Exception {
        checkAudit(numberOfChunks, numberOfChunks, false, null);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testAuditFailureSingleLocation(int numberOfChunks) throws Exception {
        checkAudit(numberOfChunks, numberOfChunks - 1, false, null);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testAuditFailureMultipleLocations(int numberOfChunks) throws Exception {
        checkAudit(numberOfChunks, numberOfChunks - 1, true, Arrays.asList("location1", "location2"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testAuditSuccessMultipleLocations(int numberOfChunks) throws Exception {
        checkAudit(numberOfChunks, numberOfChunks, true, Arrays.asList("location1", "location2"));
    }


    void checkSingleChunkAuditor(SingleChunkAuditor auditor, boolean singleCopy, String location, int chunkIdx, int numberOfChunks) {
        assertThat(auditor.getContext()).isEqualTo(mContext);
        assertThat(auditor.getEventSender()).isEqualTo(mEventSender);
        assertThat(auditor.getArchiveFS()).isEqualTo(mDevice);
        assertThat(auditor.getJobID()).isEqualTo(TEST_JOB_ID);

        assertThat(auditor.getAuditId()).isEqualTo(TEST_AUDIT_ID);
        assertThat(auditor.getChunkArchiveId()).isEqualTo(TEST_BASE_ARCHIVE_ID + chunkIdx + "." + (chunkIdx + 1));
        assertThat(auditor.getChunkId()).isEqualTo("chunk-id-for-" + chunkIdx);
        assertThat(auditor.getEncryptedChunkDigest()).isEqualTo("enc-chunk-digest-for" + chunkIdx);

        assertThat(auditor.getDecryptedChunkDigest()).isEqualTo("chunk-digest-for" + chunkIdx);
        assertThat(new String(auditor.getChunkIV(), StandardCharsets.UTF_8)).isEqualTo("chunk-iv-for" + chunkIdx);
        assertThat(auditor.isSingleCopy()).isEqualTo(singleCopy);

        assertThat(auditor.getChunkNum()).isEqualTo(chunkIdx + 1);
        assertThat(auditor.getChunkIdx()).isEqualTo(chunkIdx);
        assertThat(auditor.getTotalNumberOfChunks()).isEqualTo(numberOfChunks);
        if (singleCopy) {
            assertThat(auditor.getLocation()).isNull();
        } else {
            assertThat(auditor.getLocation()).isEqualTo(location);
        }
    }

    private void checkEachChunk(int numberOfChunksPerLocation, boolean singleCopy, List<String> locations) {
        ArrayList<String> auditLocations = new ArrayList<>();
        if (singleCopy) {
            auditLocations.add(null);
        } else {
            auditLocations.addAll(locations);
        }
        int auditorIdx = 0;
        for (int chunkIdx = 0; chunkIdx < numberOfChunksPerLocation; chunkIdx++) {
            for (String auditLocation : auditLocations) {
                SingleChunkAuditor auditor = (SingleChunkAuditor) argExecutorCallable.getAllValues().get(auditorIdx);
                checkSingleChunkAuditor(auditor, singleCopy, auditLocation, chunkIdx, numberOfChunksPerLocation);
                auditorIdx += 1;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void checkAudit(final int numberOfChunks, final int numberOfSuccessfulChunks, final boolean multipleCopies, final List<String> locations) throws Exception {
        boolean expectedSuccess = numberOfChunks == numberOfSuccessfulChunks;
        try (MockedStatic<StorageClassUtils> staticStorageClassUtils = Mockito.mockStatic(StorageClassUtils.class)) {
            staticStorageClassUtils.when(() -> StorageClassUtils.createStorage(any(String.class), any(Map.class), any(Class.class), any(StorageClassNameResolver.class)))
                    .thenReturn(mDevice);

            Mockito.lenient().when(mDevice.hasMultipleCopies()).thenReturn(multipleCopies);
            Mockito.lenient().when(mDevice.getLocations()).thenReturn(locations);

            Audit audit = setupAudit(false, numberOfChunks);

            Mockito.doAnswer(invocation -> {
                Consumer<Boolean> consumer = invocation.getArgument(0, Consumer.class);
                int numLocations = locations == null ? 1 : locations.size();
                for (int i = 0; i < numberOfSuccessfulChunks * numLocations; i++) {
                    consumer.accept(true);
                }
                return null;
            }).when(mTaskExecutor).execute(any());

            // PERFORM THE AUDIT on every chunk (for each location)
            audit.performAction(this.mContext);

            argExecutorCallable.getAllValues().forEach(callable -> {
                SingleChunkAuditor aud = (SingleChunkAuditor) callable;
                System.out.printf("location[%s]%n", aud.getLocation());
            });

            int numberOfLocations = locations == null ? 1 : locations.size();
            int expectedNumberOfChunks = numberOfChunks * numberOfLocations;
            assertThat(argExecutorCallable.getAllValues()).hasSize(expectedNumberOfChunks);
            checkEachChunk(numberOfChunks, !multipleCopies, locations);
            if (multipleCopies) {
                //this is checking what the audit failed message would be - doesn't mean audit failed
                assertThat(argFailedMessage.getValue()).isEqualTo("Audit Failed : AuditId[test-audit-id] : Locations " + locations);
            } else {
                //this is checking what the audit failed message would be - doesn't mean audit failed
                assertThat(argFailedMessage.getValue()).isEqualTo("Audit Failed : AuditId[test-audit-id] : Single Location");
            }

            assertThat(argEvent.getAllValues()).hasSize(4);
            checkCommonMessages();

            if (expectedSuccess) {
                AuditComplete event4 = (AuditComplete) argEvent.getAllValues().get(3);
                assertThat(event4.getMessage()).isEqualTo("Audit completed");
            } else {
                Error event4 = (Error) argEvent.getAllValues().get(3);
                assertThat(event4.getMessage()).isEqualTo(String.format("Audit - managed to successfully audit [%s/%s] chunks", numberOfSuccessfulChunks * numberOfLocations, expectedNumberOfChunks));
            }
        }
    }

    private void checkCommonMessages() {
        InitStates event1 = (InitStates) argEvent.getAllValues().get(0);
        assertThat(event1).extracting(InitStates::getStates).isEqualTo(Arrays.asList("Audit Data", "Data Audit complete"));

        AuditStart event2 = (AuditStart) argEvent.getAllValues().get(1);
        assertThat(event2.getMessage()).isEqualTo("Audit started");

        UpdateProgress event3 = (UpdateProgress) argEvent.getAllValues().get(2);
        assertThat(event3.getMessage()).isEqualTo("Job progress update");
    }

    @Test
    void testStorageClassUtilsCreateStorageThrowsException() {
        try (MockedStatic<StorageClassUtils> staticStorageClassUtils = Mockito.mockStatic(StorageClassUtils.class)) {
            staticStorageClassUtils.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("createStorageFailed"));

            Audit audit = setupAudit(false, 1);

            assertThatThrownBy(() -> {
                audit.performAction(this.mContext);
            }).isInstanceOf(RuntimeException.class).hasMessage("createStorageFailed");

            assertThat(argEvent.getAllValues().size()).isEqualTo(3);

            InitStates event1 = (InitStates) argEvent.getAllValues().get(0);
            assertThat(event1.getMessage()).isEqualTo("Job states: 2");

            AuditStart event2 = (AuditStart) argEvent.getAllValues().get(1);
            assertThat(event2.getMessage()).isEqualTo("Audit started");

            Error event3 = (Error) argEvent.getAllValues().get(2);
            assertThat(event3.getMessage()).isEqualTo("Audit failed: could not access archive filesystem");
        }

    }

    @Test
    void testRedeliverIsTrue() {

        Audit audit = setupAudit(true, 1);

        audit.performAction(this.mContext);

        assertThat(argEvent.getAllValues().size()).isEqualTo(1);

        Error event1 = (Error) argEvent.getAllValues().get(0);
        assertThat(event1.getMessage()).isEqualTo("Audit stopped: the message had been redelivered, please investigate");

    }

    @Test
    void testGetTaskExecutorThrowsException() {
        boolean multipleCopies = false;
        List<String> locations = null;
        try (MockedStatic<StorageClassUtils> staticStorageClassUtils = Mockito.mockStatic(StorageClassUtils.class)) {

            staticStorageClassUtils.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any()))
                    .thenReturn(mDevice);

            Mockito.lenient().when(mDevice.hasMultipleCopies()).thenReturn(multipleCopies);
            Mockito.lenient().when(mDevice.getLocations()).thenReturn(locations);

            Audit audit = setupAuditNoSpy(false, 1);
            Audit auditSpy = Mockito.spy(audit);
            assertThat(auditSpy).isNotNull();

            // let's hope this works
            Mockito.doAnswer(invocation -> {
                throw new RuntimeException("createTaskExecutorFailed");
            }).when(auditSpy).createTaskExecutor(anyInt(), any());

            assertThatThrownBy(() -> {
                auditSpy.performAction(this.mContext);
            }).isInstanceOf(RuntimeException.class).hasMessage("createTaskExecutorFailed");

            assertThat(argEvent.getAllValues().size()).isEqualTo(5);

            InitStates event1 = (InitStates) argEvent.getAllValues().get(0);
            assertThat(event1.getMessage()).isEqualTo("Job states: 2");

            AuditStart event2 = (AuditStart) argEvent.getAllValues().get(1);
            assertThat(event2.getMessage()).isEqualTo("Audit started");

            UpdateProgress event3 = (UpdateProgress) argEvent.getAllValues().get(2);
            assertThat(event3.getMessage()).isEqualTo("Job progress update");

            Error event4 = (Error) argEvent.getAllValues().get(3);
            assertThat(event4.getMessage()).isEqualTo("Audit - unexpected exception class[class java.lang.RuntimeException]message[createTaskExecutorFailed]");

            Error event5 = (Error) argEvent.getAllValues().get(4);
            assertThat(event5.getMessage()).isEqualTo("Audit failed: createTaskExecutorFailed");
        }

    }
}