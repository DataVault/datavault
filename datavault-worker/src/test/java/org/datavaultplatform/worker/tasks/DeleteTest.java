package org.datavaultplatform.worker.tasks;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeletedChunk;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.MultiLocalFileSystem;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.TaskConfigTL;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestClassOrder(ClassOrderer.OrderAnnotation.class) // Necessary!
class DeleteTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteTest.class);

    private static final String ARCHIVE_STORE_ID = "TEST-ARCHIVE-STORE-ID";
    private static final String ARCHIVE_ID = "TEST-ARCHIVE-ID";
    private static final String JOB_ID = "TEST-JOB-ID";
    private static final String USER_ID = "TEST-USER-ID";
    private static final String DEPOSIT_ID = "TEST-DEPOSIT-ID";
    private static final String BAG_ID = "TEST-BAG-ID";
    private static final String ERROR_CHUNK_NUMBER = "ERROR-CHUNK-NUMBER";
    private static final String LOCATION_ONE = "location-one";
    private static final String LOCATION_TWO = "location-two";

    // CopyOnWriteArrayList allows for concurrent tasks to updated in parallel 
    // has to be static because it's referenced by static class instances
    private static final List<File> deletedFiles = new CopyOnWriteArrayList<>();

    // has to be static because it's referenced by static class instances
    private static final Map<String, List<File>> deletedFilesByLocation = new ConcurrentHashMap<>();

    final Path tempBase = Path.of("/tmp/delete");
    final Path archiveStoreRoot = tempBase.resolve("archiveStoreRoot");

    @Mock
    EventSender mEventSender;
    @Mock
    Context mContext;
    @Mock
    StorageClassNameResolver mStorageClassResolver;
    @Mock
    Job mJob;

    org.datavaultplatform.common.model.ArchiveStore archiveStoreSuccess;
    org.datavaultplatform.common.model.ArchiveStore archiveStoreFailure;
    List<Event> nonDeletedChunks;
    List<DeletedChunk> deletedChunkEvents;
    Date startTimestamp;

    @AfterEach
    void tearDown() {
        deletedFiles.clear();
        deletedFilesByLocation.clear();
        TaskConfigTL.reset();
    }

    @BeforeEach
    @SneakyThrows
    void setup() {
        TaskConfigTL.get().setExecutorProperShutdownEnabled(true);
        Files.createDirectories(archiveStoreRoot);

        this.startTimestamp = new Date();
        this.nonDeletedChunks = new CopyOnWriteArrayList<>();
        this.deletedChunkEvents = new CopyOnWriteArrayList<>();
        deletedFiles.clear();
        deletedFilesByLocation.clear();

        this.archiveStoreSuccess = createArchiveStore("TEST ARCHIVE STORE SUCCESS");
        this.archiveStoreFailure = createArchiveStore("TEST ARCHIVE STORE FAILURE");

        Path tempDir = tempBase.resolve("tempDir");
        Path metaDir = tempBase.resolve("metaDir");
        when(mContext.getTempDir()).thenReturn(tempDir);
        when(mContext.getMetaDir()).thenReturn(metaDir);
        when(mContext.isChunkingEnabled()).thenReturn(true);
        when(mContext.getNoChunkThreads()).thenReturn(Runtime.getRuntime().availableProcessors());
        when(mContext.getStorageClassNameResolver()).thenReturn(mStorageClassResolver);
        when(mJob.getID()).thenReturn(JOB_ID);

        when(mContext.getEventSender()).thenReturn(mEventSender);
        doAnswer(invocationOnMock -> {
            // when the mock eventSender is sent an event - we put it into deletedChunks or nonDeletedChunks
            Event event = invocationOnMock.getArgument(0);
            if (event instanceof DeletedChunk dc) {
                deletedChunkEvents.add(dc);
            } else {
                nonDeletedChunks.add(event);
            }
            return null;
        }).when(mEventSender).send(any(Event.class));

        // a no-op - will return the value passed in ( without modification )
        doAnswer((Answer<String>) invocation ->
                invocation.getArgument(0)).when(mStorageClassResolver).resolveStorageClassName(any(String.class));
    }

    private void checkNoErrors() {
        assertThat(nonDeletedChunks).noneMatch(Error.class::isInstance);
    }

    private Error findDeleteError() {
        List<Error> errors = nonDeletedChunks.stream()
                .filter(Error.class::isInstance)
                .map(Error.class::cast)
                .toList();
        // we are only expecting 1 error
        assertThat(errors).hasSize(1);
        // when we look for an error - we can also check that there's not a DeleteComplete!
        assertThat(errors).noneMatch(DeleteComplete.class::isInstance);
        return errors.get(0);
    }

    private Task getTask(org.datavaultplatform.common.model.ArchiveStore archiveStore, Integer numberOfChunks, boolean sendDeletedChunks) {
        Map<String, String> properties = new HashMap<>();
        properties.put(ARCHIVE_STORE_ID, ARCHIVE_ID);
        if (numberOfChunks != null) {
            properties.put(PropNames.NUM_OF_CHUNKS, String.valueOf(numberOfChunks));
        }
        properties.put(PropNames.ARCHIVE_SIZE, "10000");
        properties.put(PropNames.DEPOSIT_ID, DEPOSIT_ID);
        properties.put(PropNames.BAG_ID, BAG_ID);
        properties.put(PropNames.USER_ID, USER_ID);
        // we only want to send WORKERS_SEND_DELETED_CHUNK_EVENTS if true - to test that a missing value defaults to "false"
        if (sendDeletedChunks) {
            properties.put(PropNames.WORKERS_SEND_DELETED_CHUNK_EVENTS, "true");
        }
        List<org.datavaultplatform.common.model.ArchiveStore> archiveStores = List.of(archiveStore);
        Map<String, Map<String, String>> userFileStoreProperties = Collections.emptyMap();
        Map<String, String> userFileStoreClasses = Collections.emptyMap();
        List<String> fileStorePaths = Collections.emptyList();
        List<String> fileUploadPaths = Collections.emptyList();
        Map<Integer, String> chunkFilesDigest = Collections.emptyMap();
        byte[] tarIV = new byte[0];
        Map<Integer, byte[]> chunksIVs = Collections.emptyMap();
        String encTarDigest = null;
        Map<Integer, String> encChunksDigest = Collections.emptyMap();
        Event lastEvent = null;
        //noinspection ConstantValue
        return new Task(mJob,
                properties,
                archiveStores,
                userFileStoreProperties,
                userFileStoreClasses,
                fileStorePaths,
                fileUploadPaths,
                chunkFilesDigest,
                tarIV,
                chunksIVs,
                encTarDigest,
                encChunksDigest,
                lastEvent);
    }

    @SneakyThrows
    private Delete getDelete(Task commonTask) {
        ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(commonTask);
        Class<? extends Task> clazz = Class.forName(commonTask.getTaskClass()).asSubclass(Task.class);
        assertThat(Delete.class).isAssignableFrom(clazz);
        LOGGER.info(message);
        return mapper.readValue(message, Delete.class);
    }

    /**
     * we want to check that the delete tasks sent by generic Delete Task match the deleted Files recorded by MultiLocationsArchiveStoreFailureImpl
     *
     * @param expectedNumberOfChunksDeleted - the expected number of DeleteChunk events
     * @param deletedChunksByLocation       the Map of Location to List of DeleteChunk events
     */
    private void pairUpDeletedChunksAndDeletedFilesWithLocation(int expectedNumberOfChunksDeleted, Map<String, List<DeletedChunk>> deletedChunksByLocation) {
        long totalDeletedChunks = deletedChunksByLocation.values().stream().mapToInt(List::size).sum();
        assertThat(totalDeletedChunks).isEqualTo(expectedNumberOfChunksDeleted);
        assertThat(deletedChunksByLocation).hasSameSizeAs(deletedFilesByLocation);

        deletedChunksByLocation.forEach((location, deletedChunksAtLocation) -> {
            List<File> deletedFilesAtLocation = deletedFilesByLocation.get(location);
            assertThat(deletedChunksAtLocation).hasSameSizeAs(deletedFilesAtLocation);
            deletedChunksAtLocation.forEach(dc -> {
                int chunkNumber = dc.getChunkNumber();
                Stream<File> chunkFilesAtLocationStream = deletedFilesAtLocation.stream().
                        filter(file -> file.getAbsolutePath().contains("/tmp/delete/tempDir/TEST-BAG-ID.tar"));
                if (chunkNumber > 0) {
                    chunkFilesAtLocationStream = chunkFilesAtLocationStream.filter(file -> file.getName().endsWith("." + dc.getChunkNumber()));
                }
                List<File> chunkFilesAtLocation = chunkFilesAtLocationStream.toList();
                assertThat(chunkFilesAtLocation).hasSize(1);
            });
        });
    }

    /**
     * When we are no using Locations...
     * we want to check that the delete tasks sent by generic Delete Task match the deleted Files recorded by ArchiveStoreFailureImpl
     *
     * @param expectedNumberOfDeletedChunks the expected number of DeleteChunk events
     * @param deletedChunks                 the list of DeletedChunk events
     */
    private void pairUpDeletedChunksAndDeletedFilesNoLocation(int expectedNumberOfDeletedChunks, List<DeletedChunk> deletedChunks) {
        assertThat(deletedChunks).hasSize(expectedNumberOfDeletedChunks);
        assertThat(deletedChunks).hasSameSizeAs(deletedFiles);

        deletedChunks.forEach(dc -> {
            int chunkNumber = dc.getChunkNumber();
            Stream<File> chunkFilesStream = deletedFiles.stream().
                    filter(file -> file.getAbsolutePath().contains("/tmp/delete/tempDir/TEST-BAG-ID.tar"));
            if (chunkNumber > 0) {
                chunkFilesStream = chunkFilesStream.filter(file -> file.getName().endsWith("." + dc.getChunkNumber()));
            }
            List<File> chunkFilesAt = chunkFilesStream.toList();
            assertThat(chunkFilesAt).hasSize(1);
        });
    }

    private void checkDeletedChunk(DeletedChunk dc, String location, int chunkNumber, String message) {
        checkEvent(DeletedChunk.class, dc, location, chunkNumber, message);
    }

    private void checkError(Error error, String location, int chunkNumber, String message) {
        checkEvent(Error.class, error, location, chunkNumber, message);
    }

    private void checkEvent(Class<? extends Event> expectedEventClass, Event event, String location, int chunkNumber, String message) {
        assertThat(event.getEventClass()).isEqualTo(expectedEventClass.getCanonicalName());
        assertThat(event.getChunkNumber()).isEqualTo(chunkNumber);
        assertThat(event.getLocation()).isEqualTo(location);
        assertThat(event.getMessage()).isEqualTo(message);

        assertThat(event.getTimestamp()).isAfterOrEqualTo(startTimestamp);
        assertThat(event.getDepositId()).isEqualTo(DEPOSIT_ID);
        assertThat(event.getJobId()).isEqualTo(JOB_ID);
        assertThat(event.getUserId()).isEqualTo(USER_ID);
        assertThat(event.getArchiveStoreId()).isEqualTo(ARCHIVE_STORE_ID);
        assertThat(event.getArchiveId()).isEqualTo(ARCHIVE_ID);
    }

    private void performDeleteSuccess(Delete delete) {
        try {
            delete.performAction(mContext);
        } catch (RuntimeException rte) {
            Assertions.fail("unexpected exception", rte);
        }
    }

    private void performDeleteAndCheckForDeleteFileException(Delete delete, String expectedDteMessage) {
        performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage, 0);
    }

    private void performDeleteAndCheckForDeleteFileException(Delete delete, String expectedDteMessage, int errorChunkNumber, String location) {
        try {
            delete.performAction(mContext);
            Assertions.fail("expected the Delete Task to fail!");
        } catch (RuntimeException rte) {
            if (rte.getCause() instanceof DeleteFileException dte) {
                assertThat(dte.getLocation()).isEqualTo(location);
                assertThat(dte.getArchiveStoreId()).isEqualTo(ARCHIVE_STORE_ID);
                assertThat(dte.getChunkNumber()).isEqualTo(errorChunkNumber);
                assertThat(dte.getMessage()).isEqualTo(expectedDteMessage);
            } else {
                Assertions.fail("expected DeleteFileException!");
            }
        }
    }

    private void performDeleteAndCheckForDeleteFileException(Delete delete, String expectedDteMessage, int errorChunkNumber) {
        performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage, errorChunkNumber, Delete.NO_LOCATION);
    }

    private Map<String, List<DeletedChunk>> getDeletedChunksByLocation() {
        return deletedChunkEvents.stream().
                sorted(Comparator.comparing(DeletedChunk::getChunkNumber)).
                collect(Collectors.groupingBy(DeletedChunk::getLocation));
    }

    private int getErrorChunkNumber(int numberOfChunks) {
        int errorChunkNumber = ThreadLocalRandom.current().nextInt(1, numberOfChunks + 1);
        assertThat(errorChunkNumber)
                .isGreaterThanOrEqualTo(1)
                .isLessThanOrEqualTo(numberOfChunks);
        return errorChunkNumber;
    }

    private org.datavaultplatform.common.model.ArchiveStore createArchiveStore(String label) {
        // usually the ArchiveStore ID is injected by hibernate/JPA - you can't normally do it
        var result = new org.datavaultplatform.common.model.ArchiveStore() {
            public String getId() {
                return ARCHIVE_STORE_ID;
            }
        };
        HashMap<String, String> archiveStoreProps = new HashMap<>();
        archiveStoreProps.put(PropNames.ROOT_PATH, archiveStoreRoot.toAbsolutePath().toString());
        result.setProperties(archiveStoreProps);
        result.setLabel(label);
        return result;
    }

    private Delete createDelete(ArchiveStore archiveStore, int numberOfChunks) {
        Task task = getTask(archiveStore, numberOfChunks, true);
        task.setTaskClass(Delete.class.getName());
        return getDelete(task);
    }

    private Delete createDeleteNoDeletedChunks(ArchiveStore archiveStore, int numberOfChunks) {
        Task task = getTask(archiveStore, numberOfChunks, false);
        task.setTaskClass(Delete.class.getName());
        return getDelete(task);
    }

    @Order(1)
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class NotMultipleLocationsTests {

        @BeforeEach
        void setup() {
            archiveStoreSuccess.setStorageClass(ArchiveStoreSuccessImpl.class.getName());
            archiveStoreFailure.setStorageClass(ArchiveStoreFailureImpl.class.getName());
        }

        @Order(1)
        @Test
        void testSuccessWithNoChunks() {
            when(mContext.isChunkingEnabled()).thenReturn(false);

            Delete delete = createDelete(archiveStoreSuccess, 0);
            performDeleteSuccess(delete);

            // check non-DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            File chunkFile = new File("/tmp/delete/tempDir/TEST-BAG-ID.tar");

            //check deletedFiles
            assertThat(deletedFiles)
                    .hasSize(1)
                    .contains(chunkFile);

            // check non-DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            // check each deleted chunk
            DeletedChunk dc = deletedChunkEvents.get(0);
            checkDeletedChunk(dc, Delete.NO_LOCATION, 0, "Deleted Chunk [0/0] from (ArchiveStoreSuccessImpl/TEST-ARCHIVE-STORE-ID/no-location)");

            verify(mContext, times(1)).isChunkingEnabled();
            verify(mEventSender, times(4 + 1)).send(any(Event.class));
            pairUpDeletedChunksAndDeletedFilesNoLocation(1, deletedChunkEvents);
        }

        @Order(2)
        @SneakyThrows
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 100, 1000})
        void testSuccessWithChunks(int numberOfChunks) {
            assertThat(numberOfChunks).isGreaterThan(0);
            Delete delete = createDelete(archiveStoreSuccess, numberOfChunks);
            performDeleteSuccess(delete);

            String template = "/tmp/delete/tempDir/TEST-BAG-ID.tar.%d";

            // we are using deletedFiles - not deletedFilesByLocation
            assertThat(deletedFiles).hasSize(numberOfChunks);
            assertThat(deletedFilesByLocation).isEmpty();

            for (int i = 0; i < numberOfChunks; i++) {
                int chunkNumber = i + 1;
                File chunkFile = new File(template.formatted(chunkNumber));
                assertThat(deletedFiles).contains(chunkFile);
            }

            // eventSender
            verify(mEventSender, times(4 + numberOfChunks)).send(any(Event.class));

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            assertThat(deletedChunkEvents).hasSize(numberOfChunks);
            deletedChunkEvents.sort(Comparator.comparingInt(DeletedChunk::getChunkNumber));
            for (int i = 0; i < numberOfChunks; i++) {
                int chunkNumber = i + 1;
                DeletedChunk dc = deletedChunkEvents.get(i);
                String expectedMessage = "Deleted Chunk [%s/%s] from (ArchiveStoreSuccessImpl/TEST-ARCHIVE-STORE-ID/no-location)".formatted(chunkNumber, numberOfChunks);
                checkDeletedChunk(dc, Delete.NO_LOCATION, chunkNumber, expectedMessage);
            }
            verify(mContext).isChunkingEnabled();
            checkNoErrors();
            pairUpDeletedChunksAndDeletedFilesNoLocation(numberOfChunks, deletedChunkEvents);
        }


        @Order(3)
        @Test
        @SneakyThrows
        void testFailureNoChunks() {
            when(mContext.isChunkingEnabled()).thenReturn(false);

            Delete delete = createDelete(archiveStoreFailure, 0);
            String expectedDteMessage = "ArchiveStore[ArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID]Location[no-location]ChunkNum[0]Cause[java.lang.RuntimeException/oops@no-chunks]";
            performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage);

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            // the single delete chunk that was attempted failed
            assertThat(deletedFiles).isEmpty();

            // deleted chunks
            assertThat(deletedChunkEvents).isEmpty();

            // verify
            verify(mEventSender, atLeast(4)).send(any(Event.class));
            verify(mContext).isChunkingEnabled();

            Error error = findDeleteError();
            checkError(error, Delete.NO_LOCATION, 0, "Deposit delete failed: " + expectedDteMessage);

            pairUpDeletedChunksAndDeletedFilesNoLocation(0, deletedChunkEvents);
        }

        @Order(4)
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 100, 1000})
        @SneakyThrows
        void testFailureWithChunks(int numberOfChunks) {
            assertThat(numberOfChunks).isGreaterThan(0);
            // put the ERROR_CHUNK_NUMBER into the 'fake archive' so it knows when to throw error
            int errorChunkNumber = getErrorChunkNumber(numberOfChunks);
            archiveStoreFailure.getProperties().put(ERROR_CHUNK_NUMBER, String.valueOf(errorChunkNumber));

            Delete delete = createDelete(archiveStoreFailure, numberOfChunks);
            String expectedDteMessage = "ArchiveStore[ArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID]Location[no-location]ChunkNum[%d]Cause[java.lang.RuntimeException/oops@%d]".formatted(errorChunkNumber, errorChunkNumber);
            performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage, errorChunkNumber);

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            int expectedDeletedChunks = numberOfChunks - 1;
            assertThat(deletedChunkEvents).hasSize(expectedDeletedChunks);

            // we are using deleteFiles not deletedFilesByLocation
            assertThat(deletedFiles).hasSize(expectedDeletedChunks);

            deletedChunkEvents.sort(Comparator.comparingInt(DeletedChunk::getChunkNumber));

            // 1 .. errorChunkNumber-1
            for (int i = 0; i < errorChunkNumber - 1; i++) {
                DeletedChunk dc = deletedChunkEvents.get(i);
                int chunkNumber = i + 1;
                String expectedMessage = "Deleted Chunk [%s/%s] from (ArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID/no-location)".formatted(chunkNumber, numberOfChunks);
                checkDeletedChunk(dc, Delete.NO_LOCATION, chunkNumber, expectedMessage);
            }

            // errorChunkNumber+1 .. numberOfChunks
            for (int i = errorChunkNumber - 1; i < numberOfChunks - 1; i++) {
                DeletedChunk dc = deletedChunkEvents.get(i);
                int chunkNumber = i + 2;
                String expectedMessage = "Deleted Chunk [%s/%s] from (ArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID/no-location)".formatted(chunkNumber, numberOfChunks);
                checkDeletedChunk(dc, Delete.NO_LOCATION, chunkNumber, expectedMessage);
            }

            // check that there's no DeletedChunk errorChunkNumber
            Optional<DeletedChunk> optionalDeletedChunk = deletedChunkEvents.stream()
                    .filter(dc -> dc.getChunkNumber().equals(errorChunkNumber))
                    .findFirst();
            assertThat(optionalDeletedChunk).isEmpty();

            verify(mContext).isChunkingEnabled();
            verify(mEventSender, times(4 + expectedDeletedChunks)).send(any(Event.class));

            Error error = findDeleteError();
            checkError(error, Delete.NO_LOCATION, errorChunkNumber, "Deposit delete failed: " + expectedDteMessage);

            pairUpDeletedChunksAndDeletedFilesNoLocation(numberOfChunks - 1, deletedChunkEvents);
        }
        
        @Order(5)
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 100, 1000})
        @SneakyThrows
        void testFailureWithChunksButNoDeletedChunkEventsSent(int numberOfChunks) {
            assertThat(numberOfChunks).isGreaterThan(0);
            // put the ERROR_CHUNK_NUMBER into the 'fake archive' so it knows when to throw error
            int errorChunkNumber = getErrorChunkNumber(numberOfChunks);
            archiveStoreFailure.getProperties().put(ERROR_CHUNK_NUMBER, String.valueOf(errorChunkNumber));

            ch.qos.logback.classic.Logger deleteLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Delete.class);
            ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
            deleteLogger.addAppender(listAppender);
            listAppender.start();

            Delete delete = createDeleteNoDeletedChunks(archiveStoreFailure, numberOfChunks);
            String expectedDteMessage = "ArchiveStore[ArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID]Location[no-location]ChunkNum[%d]Cause[java.lang.RuntimeException/oops@%d]".formatted(errorChunkNumber, errorChunkNumber);
            performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage, errorChunkNumber);

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            int expectedDeletedChunks = numberOfChunks - 1;
            assertThat(deletedChunkEvents).isEmpty();

            // we are using deleteFiles not deletedFilesByLocation
            assertThat(deletedFiles).hasSize(expectedDeletedChunks);
            
            verify(mContext).isChunkingEnabled();
            verify(mEventSender, times(4)).send(any(Event.class));

            Error error = findDeleteError();
            checkError(error, Delete.NO_LOCATION, errorChunkNumber, "Deposit delete failed: " + expectedDteMessage);
            
            assertThat(deletedFiles).hasSize(expectedDeletedChunks);
            
            listAppender.stop();
            deleteLogger.detachAppender(listAppender);
            List<String> notSendingMessages = listAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .filter(m -> m.startsWith("NOT SENDING"))
                    .toList();
            assertThat(notSendingMessages).hasSize(expectedDeletedChunks);
            String prefixTemplate = "NOT SENDING Deleted Chunk [%d/%d]";
            for (int i = 1; i <= numberOfChunks; i++) {
                if (i != errorChunkNumber) {
                    String prefix = prefixTemplate.formatted(i, numberOfChunks);
                    assertThat(notSendingMessages.stream().anyMatch(m -> m.startsWith(prefix))).isTrue();
                }
            }
            String prefix = prefixTemplate.formatted(errorChunkNumber, numberOfChunks);
            assertThat(notSendingMessages.stream().noneMatch(m -> m.startsWith(prefix))).isTrue();
        }

        /**
         * Subclasses LocalFileSystem to make sure we get he same class/interface hierarchy with ArchiveStore/Device
         * We only need to override delete.
         */
        public static class ArchiveStoreSuccessImpl extends LocalFileSystem {
            public ArchiveStoreSuccessImpl(String name, Map<String, String> config) throws FileNotFoundException {
                super(name, config);
            }

            @Override
            public void delete(String path, File working, Progress progress) {
                deletedFiles.add(working);
            }
        }

        /**
         * Subclasses LocalFileSystem to make sure we get he same class/interface hierarchy with ArchiveStore/Device
         * We only need to override delete.
         * It is set up to throw an Exception when it encounters the chunk numbered : ERROR_CHUNK_NUMBER
         * This is hard enough in a Unit Test - very, very hard to do within an Integration Test
         */
        public static class ArchiveStoreFailureImpl extends ArchiveStoreSuccessImpl {
            final Pattern regex = Pattern.compile("^(.*)(\\.)(\\d+)$");
            private final Long errorChunkNumber;

            public ArchiveStoreFailureImpl(String name, Map<String, String> config) throws FileNotFoundException {
                super(name, config);
                if (config.containsKey(ERROR_CHUNK_NUMBER)) {
                    this.errorChunkNumber = Long.valueOf(config.get(ERROR_CHUNK_NUMBER));
                } else {
                    this.errorChunkNumber = null;
                }
            }

            @Override
            public void delete(String path, File working, Progress progress) {
                if (errorChunkNumber == null) {
                    throw new RuntimeException("oops@no-chunks");
                }
                Matcher matcher = regex.matcher(path);
                Assert.isTrue(matcher.matches(), "failed to file chunkNumber at end of filename!");
                Long chunkNumber = Long.valueOf(matcher.group(3));
                if (errorChunkNumber.equals(chunkNumber)) {
                    throw new RuntimeException("oops@" + chunkNumber);
                } else {
                    deletedFiles.add(working);
                }
            }
        }

    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MultipleLocationsTests {

        String location1;
        String location2;

        @BeforeEach
        @SneakyThrows
        void setup() {
            archiveStoreSuccess.setStorageClass(MultiLocationsArchiveStoreSuccessImpl.class.getName());
            archiveStoreFailure.setStorageClass(MultiLocationsArchiveStoreFailureImpl.class.getName());

            Path location1path = tempBase.resolve(LOCATION_ONE);
            Path location2path = tempBase.resolve(LOCATION_TWO);
            Files.createDirectories(location1path);
            Files.createDirectories(location2path);
            location1 = location1path.toFile().getCanonicalPath();
            location2 = location2path.toFile().getCanonicalPath();
            String multiLocationRootPath = location1 + "," + location2;
            archiveStoreSuccess.getProperties().put(PropNames.ROOT_PATH, multiLocationRootPath);
            archiveStoreFailure.getProperties().put(PropNames.ROOT_PATH, multiLocationRootPath);
        }


        @Order(1)
        @Test
        void testSuccessWithNoChunks() {
            when(mContext.isChunkingEnabled()).thenReturn(false);

            Delete delete = createDelete(archiveStoreSuccess, 0);
            performDeleteSuccess(delete);

            // check non-DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            File chunkFile = new File("/tmp/delete/tempDir/TEST-BAG-ID.tar");

            //check deletedFiles by location
            assertThat(deletedFilesByLocation).containsOnlyKeys(location1, location2);
            //noinspection CodeBlock2Expr
            deletedFilesByLocation.forEach((location, locationDeletedFiles) -> {
                assertThat(locationDeletedFiles)
                        .hasSize(1)
                        .contains(chunkFile);
            });

            // check non-DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            // check each deleted chunk
            Map<String, List<DeletedChunk>> deletedChunksByLocation = getDeletedChunksByLocation();
            assertThat(deletedChunksByLocation.keySet()).containsExactlyInAnyOrder(location1, location2);
            //noinspection CodeBlock2Expr
            deletedChunksByLocation.forEach((location, deletedChunksAtLocation) -> {
                deletedChunksAtLocation.forEach(dc -> {
                    String expectedMessage = "Deleted Chunk [0/0] from (MultiLocationsArchiveStoreSuccessImpl/TEST-ARCHIVE-STORE-ID/%s)".formatted(location);
                    checkDeletedChunk(dc, location, 0, expectedMessage);
                });
            });

            verify(mContext, times(2)).isChunkingEnabled();
            verify(mEventSender, times(4 + 2)).send(any(Event.class));
            pairUpDeletedChunksAndDeletedFilesWithLocation(2, deletedChunksByLocation);
        }

        @Order(2)
        @SneakyThrows
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 100, 1000})
        void testSuccessWithChunks(int numberOfChunks) {
            assertThat(numberOfChunks).isGreaterThan(0);
            int numLocations = 2;
            Delete delete = createDelete(archiveStoreSuccess, numberOfChunks);
            performDeleteSuccess(delete);

            String template = "/tmp/delete/tempDir/TEST-BAG-ID.tar.%d";

            // we are using deletedFilesByLocation - not deletedFiles
            assertThat(deletedFiles).isEmpty();
            assertThat(deletedFilesByLocation).containsOnlyKeys(location1, location2);

            deletedFilesByLocation.forEach((location, deletedLocationFiles) -> {
                assertThat(deletedLocationFiles).hasSize(numberOfChunks);
                for (int i = 0; i < numberOfChunks; i++) {
                    int chunkNumber = i + 1;
                    File chunkFile = new File(template.formatted(chunkNumber));
                    assertThat(deletedLocationFiles).contains(chunkFile);
                }
            });

            // eventSender
            verify(mEventSender, times(4 + (numberOfChunks * numLocations))).send(any(Event.class));

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            Map<String, List<DeletedChunk>> deletedChunksByLocation = getDeletedChunksByLocation();
            assertThat(deletedChunksByLocation).containsOnlyKeys(location1, location2);
            //noinspection CodeBlock2Expr
            deletedChunksByLocation.forEach((location, deletedLocationChunks) -> {
                deletedLocationChunks.forEach(dc -> {
                    int cn = dc.getChunkNumber();
                    String expectedMessage = "Deleted Chunk [%s/%s] from (MultiLocationsArchiveStoreSuccessImpl/TEST-ARCHIVE-STORE-ID/%s)".formatted(cn, numberOfChunks, location);
                    checkDeletedChunk(dc, location, dc.getChunkNumber(), expectedMessage);
                });
            });
            verify(mContext, times(numLocations)).isChunkingEnabled();
            checkNoErrors();
            pairUpDeletedChunksAndDeletedFilesWithLocation(numberOfChunks * 2, deletedChunksByLocation);
        }

        @Order(3)
        @Test
        @SneakyThrows
        void testFailureNoChunks() {
            when(mContext.isChunkingEnabled()).thenReturn(false);

            Delete delete = createDelete(archiveStoreFailure, 0);
            String expectedDteMessage = "ArchiveStore[MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID]Location[%s]ChunkNum[0]Cause[java.lang.RuntimeException/oops@no-chunks/%s]".formatted(location2, location2);
            performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage, 0, location2);

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            // the single delete chunk that was attempted failed for location2
            assertThat(deletedFilesByLocation).hasSize(1);
            File noChunkFile = new File("/tmp/delete/tempDir/TEST-BAG-ID.tar"); //this resolves to /private/tmp/delete/tempDir/TEST-BAG-ID.tar on mac
            assertThat(deletedFilesByLocation.get(location1)).contains(noChunkFile);

            // deleted chunks
            assertThat(deletedChunkEvents).hasSize(1);
            DeletedChunk dc1 = deletedChunkEvents.get(0);
            String tmpPrefix = SystemUtils.IS_OS_MAC ? "/private" : "";
            checkDeletedChunk(dc1, location1, 0, "Deleted Chunk [0/0] from (MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID/" + tmpPrefix + "/tmp/delete/location-one)");

            // verify
            verify(mEventSender, times(5)).send(any(Event.class));
            verify(mContext, times(2)).isChunkingEnabled();//once per location

            Error error = findDeleteError();
            checkError(error, location2, 0, "Deposit delete failed: " + expectedDteMessage);

            Map<String, List<DeletedChunk>> deletedChunksByLocation = getDeletedChunksByLocation();
            pairUpDeletedChunksAndDeletedFilesWithLocation(1, deletedChunksByLocation);
        }

        /**
         * This test is sensitive to the way the order that Tasks are submitted to TaskExecutor.
         * For Delete Task - tasks are submitted in 2 nested loops - outer loop being location, inner loop being chunkNumber
         * TaskExecutor waits for tasks to finish in submission order - location then chunkNumber Order.
         * So when MultiLocationsArchiveStoreFailureImpl throws an error processing 'location 2 and errorChunkNumber',
         * some tasks will have finished - after an error is detected - tasks not yet checked for completion tasks have 5 minutes to complete before being terminated.
         */
        @Order(4)
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 100, 1000})
        @SneakyThrows
        void testFailureWithChunks(int numberOfChunks) {
            assertThat(numberOfChunks).isGreaterThan(0);
            // put the ERROR_CHUNK_NUMBER into the 'fake archive' so it knows when to throw error
            int errorChunkNumber = getErrorChunkNumber(numberOfChunks);
            archiveStoreFailure.getProperties().put(ERROR_CHUNK_NUMBER, String.valueOf(errorChunkNumber));

            Delete delete = createDelete(archiveStoreFailure, numberOfChunks);
            String expectedDteMessage = "ArchiveStore[MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID]Location[%s]ChunkNum[%d]Cause[java.lang.RuntimeException/oops@%d/%s]".formatted(location2, errorChunkNumber, errorChunkNumber, location2);
            performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage, errorChunkNumber, location2);

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            @SuppressWarnings("UnnecessaryLocalVariable")
            int expectedDeletedChunksLocation1 = numberOfChunks;
            int expectedDeletedChunksLocation2 = numberOfChunks - 1;
            int expectedDeletedChunks = expectedDeletedChunksLocation1 + expectedDeletedChunksLocation2;
            assertThat(deletedChunkEvents).hasSize(expectedDeletedChunks);

            // we are using deleteFiles not deletedFilesByLocation
            Map<String, List<DeletedChunk>> deletedChunksByLocation = getDeletedChunksByLocation();

            // LOCATION 1
            List<DeletedChunk> deletedLocation1Chunks = deletedChunksByLocation.get(location1);
            assertThat(deletedLocation1Chunks).hasSize(expectedDeletedChunksLocation1);
            for (int i = 0; i < numberOfChunks; i++) {
                DeletedChunk dc = deletedLocation1Chunks.get(i);
                int chunkNumber = i + 1;
                checkDeletedChunk(dc, location1, chunkNumber, "Deleted Chunk [%s/%s] from (MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID/%s)".formatted(chunkNumber, numberOfChunks, location1));
            }

            // LOCATION 2
            List<DeletedChunk> deletedLocation2Chunks = deletedChunksByLocation.getOrDefault(location2, Collections.emptyList());
            assertThat(deletedLocation2Chunks).hasSize(expectedDeletedChunksLocation2);

            // LOCATION 2 : 1 .. errorChunkNumber-1
            for (int i = 0; i < errorChunkNumber - 1; i++) {
                DeletedChunk dc = deletedLocation2Chunks.get(i);
                int chunkNumber = i + 1;
                checkDeletedChunk(dc, location2, chunkNumber, "Deleted Chunk [%s/%s] from (MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID/%s)".formatted(chunkNumber, numberOfChunks, location2));
            }

            // LOCATION 2 : errorChunkNumber+1 .. numberOfChunks
            for (int i = errorChunkNumber - 1; i < numberOfChunks - 1; i++) {
                DeletedChunk dc = deletedLocation2Chunks.get(i);
                int chunkNumber = i + 2;
                checkDeletedChunk(dc, location2, chunkNumber, "Deleted Chunk [%s/%s] from (MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID/%s)".formatted(chunkNumber, numberOfChunks, location2));
            }

            // check that there's no DeletedChunk for location2 and errorChunkNumber
            Optional<DeletedChunk> optionalDeletedChunk = deletedLocation2Chunks.stream()
                    .filter(dc -> dc.getChunkNumber().equals(errorChunkNumber))
                    .findFirst();
            assertThat(optionalDeletedChunk).isEmpty();

            verify(mContext, times(2)).isChunkingEnabled();
            verify(mEventSender, atLeast(4 + expectedDeletedChunks)).send(any(Event.class));

            Error error = findDeleteError();
            checkError(error, location2, errorChunkNumber, "Deposit delete failed: " + expectedDteMessage);

            pairUpDeletedChunksAndDeletedFilesWithLocation((numberOfChunks * 2) - 1, deletedChunksByLocation);
        }
        
        @Order(5)
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 100, 1000})
        @SneakyThrows
        void testFailureWithChunksButNoDeletedChunkEventsSent(int numberOfChunks) {
            assertThat(numberOfChunks).isGreaterThan(0);
            // put the ERROR_CHUNK_NUMBER into the 'fake archive' so it knows when to throw error
            int errorChunkNumber = getErrorChunkNumber(numberOfChunks);
            archiveStoreFailure.getProperties().put(ERROR_CHUNK_NUMBER, String.valueOf(errorChunkNumber));

            ch.qos.logback.classic.Logger deleteLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Delete.class);
            ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
            deleteLogger.addAppender(listAppender);
            listAppender.start();

            Delete delete = createDeleteNoDeletedChunks(archiveStoreFailure, numberOfChunks);
            String expectedDteMessage = "ArchiveStore[MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID]Location[%s]ChunkNum[%d]Cause[java.lang.RuntimeException/oops@%d/%s]".formatted(location2, errorChunkNumber, errorChunkNumber, location2);
            performDeleteAndCheckForDeleteFileException(delete, expectedDteMessage, errorChunkNumber, location2);

            // non DeletedChunk events
            assertThat(nonDeletedChunks).hasSize(4);

            @SuppressWarnings("UnnecessaryLocalVariable")
            int expectedDeletedChunksLocation1 = numberOfChunks;
            int expectedDeletedChunksLocation2 = numberOfChunks - 1;
            int expectedDeletedChunks = expectedDeletedChunksLocation1 + expectedDeletedChunksLocation2;
            assertThat(deletedChunkEvents).isEmpty();

            verify(mContext, times(2)).isChunkingEnabled();
            verify(mEventSender, atLeast(4)).send(any(Event.class));

            Error error = findDeleteError();
            checkError(error, location2, errorChunkNumber, "Deposit delete failed: " + expectedDteMessage);

            assertThat(deletedFilesByLocation.get(location1)).hasSize(numberOfChunks);
            if (numberOfChunks == 1) {
                assertThat(deletedFilesByLocation.containsKey(location2)).isFalse();
            } else {
                assertThat(deletedFilesByLocation.get(location2)).hasSize(numberOfChunks - 1);
            }

            listAppender.stop();
            deleteLogger.detachAppender(listAppender);
            List<String> notSendingMessages = listAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .filter(m -> m.startsWith("NOT SENDING"))
                    .toList();
            assertThat(notSendingMessages).hasSize(expectedDeletedChunks);

            String msgTemplate = "NOT SENDING Deleted Chunk [%d/%d] from (MultiLocationsArchiveStoreFailureImpl/TEST-ARCHIVE-STORE-ID/%s)";

            List<String> nonSentLocation1messages = notSendingMessages.stream().filter(m -> m.contains(location1)).toList();
            assertThat(nonSentLocation1messages).hasSize(expectedDeletedChunksLocation1);
            List<String> nonSentLocation2messages = notSendingMessages.stream().filter(m -> m.contains(location2)).toList();
            assertThat(nonSentLocation2messages).hasSize(expectedDeletedChunksLocation2);
            // LOCATION 1
            for (int i = 1; i <= numberOfChunks; i++) {
                String msg = msgTemplate.formatted(i, numberOfChunks, location1);
                assertThat(nonSentLocation1messages.stream().anyMatch(m -> m.endsWith(msg))).isTrue();
            }
            // LOCATION 2
            for (int i = 1; i <= numberOfChunks; i++) {
                String msg = msgTemplate.formatted(i, numberOfChunks, location2);
                if (i != errorChunkNumber) {
                    assertThat(nonSentLocation2messages.stream().anyMatch(msg::equals)).isTrue();
                }
            }
            String msg = msgTemplate.formatted(errorChunkNumber, numberOfChunks, location2);
            assertThat(nonSentLocation2messages.stream().noneMatch(msg::equals)).isTrue();
        }

        /**
         * Subclasses MultiLocalFileSystem to make sure we get he same class/interface hierarchy with ArchiveStore/Device
         * We only need to override delete.
         * It is set up to throw an Exception when it encounters the chunk numbered : ERROR_CHUNK_NUMBER
         * This is hard enough in a Unit Test - very, very hard to do within an Integration Test
         */
        public static class MultiLocationsArchiveStoreSuccessImpl extends MultiLocalFileSystem {

            public MultiLocationsArchiveStoreSuccessImpl(String name, Map<String, String> config) throws FileNotFoundException {
                super(name, config);
            }

            @Override
            public void delete(String path, File working, Progress progress, String location) {
                recordDeletedFile(working, location);
            }

            void recordDeletedFile(File working, String location) {
                deletedFilesByLocation.computeIfAbsent(location, k -> new CopyOnWriteArrayList<>()).add(working);
            }
        }

        /**
         * Subclasses MultiLocalFileSystem to make sure we get he same class/interface hierarchy with ArchiveStore/Device
         * We only need to override delete.
         * It is set up to throw an Exception when it encounters the chunk numbered : ERROR_CHUNK_NUMBER
         * This is hard enough in a Unit Test - very, very hard to do within an Integration Test
         */
        public static class MultiLocationsArchiveStoreFailureImpl extends MultiLocationsArchiveStoreSuccessImpl {
            final Pattern regex = Pattern.compile("^(.*)(\\.)(\\d+)$");
            private final Long errorChunkNumber;

            public MultiLocationsArchiveStoreFailureImpl(String name, Map<String, String> config) throws FileNotFoundException {
                super(name, config);
                if (config.containsKey(ERROR_CHUNK_NUMBER)) {
                    this.errorChunkNumber = Long.valueOf(config.get(ERROR_CHUNK_NUMBER));
                } else {
                    this.errorChunkNumber = null;
                }
            }

            // we will fail when the 'chunkNumber' matches 'errorChunkNumber' AND the 'location' contains 'one'
            @Override
            public void delete(String path, File working, Progress progress, String location) {
                if (location.contains(LOCATION_ONE)) {
                    //all location-one tasks will succeed
                    recordDeletedFile(working, location);
                } else {
                    deleteFromLocationTwo(path, working, progress, location);
                }
            }

            private void deleteFromLocationTwo(String path, File working, Progress progress, String location) {
                Assert.notNull(path, "path cannot be null");
                Assert.notNull(working, "working cannot be null");
                Assert.notNull(progress, "progress cannot be null");
                Assert.isTrue(location.contains(LOCATION_TWO), "unexpected location " + location);

                // if no chunks - then always fail for location-two
                if (errorChunkNumber == null) {
                    throw new RuntimeException("oops@no-chunks/" + location);
                } else {
                    // we have location-two and chunks
                    Matcher matcher = regex.matcher(path);
                    Assert.isTrue(matcher.matches(), "failed to file chunkNumber at end of filename!");
                    Long chunkNumber = Long.valueOf(matcher.group(3));

                    // task for location-two and errorChunkNumber will fail
                    if (errorChunkNumber.equals(chunkNumber)) {
                        throw new RuntimeException("oops@" + chunkNumber + "/" + location);
                    } else {
                        recordDeletedFile(working, location);
                    }
                }
            }
        }
    }
}
