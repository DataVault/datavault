package org.datavaultplatform.worker.tasks;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.util.RetrievedChunks;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.retry.DvRetryException;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;
import org.datavaultplatform.worker.tasks.retrieve.ArchiveDeviceInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils;
import org.datavaultplatform.worker.tasks.retrieve.UserStoreInfo;
import org.datavaultplatform.worker.test.TestClockConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.common.storage.Verify.SHA_1_ALGORITHM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Slf4j
@ExtendWith(MockitoExtension.class)
public class RetrieveTest {

    public static final String TEST_RETRIEVE_PATH = "retrieve-path";
    public static final String TEST_TIMESTAMP_DIR_NAME = "dv_20220329141516";
    private static final String TEST_ARCHIVE_ID = "test-archive-id";
    @Mock
    EventSender mEventSender;

    @Mock
    Context mContext;

    @Captor
    ArgumentCaptor<Event> argEvent;

    @Mock
    StorageClassNameResolver mStorageClassResolver;

    @Mock
    SFTPFileSystemSSHD mSftpUserStore;

    @Mock
    LocalFileSystem mNonSftpUserStore;

    @Mock
    LocalFileSystem mArchiveFS;

    org.datavaultplatform.common.model.ArchiveStore storageModel;

    @Captor
    ArgumentCaptor<String> argTimestampDirName1;

    @Captor
    ArgumentCaptor<String> argTimestampDirName2;


    @Captor
    ArgumentCaptor<Context> argContext;

    @Captor
    ArgumentCaptor<File> argTarFile;

    @Captor
    ArgumentCaptor<ArchiveDeviceInfo> argArchiveDeviceInfo;

    @Captor
    ArgumentCaptor<UserStoreInfo> argUserStoreInfo;
    
    @Captor
    ArgumentCaptor<File> argDataVaultHiddenFile;
    
    @Captor
    ArgumentCaptor<RetrievedChunks> argRetrievedChunks;

    @BeforeEach
    void setup() {
        RetryTestUtils.setLoggingLevelInfo(log);
        this.storageModel = new ArchiveStore();
        this.storageModel.setStorageClass("ARCHIVE_STORE_STORAGE_CLASS_NAME");
    }
    
    @Nested
    class SftpUserStoreTests {

        @BeforeEach
        void setup() {
            setupSftpUserStore();
        }

        @Test
        void testSingleCopyStoreWhenUpfrontUserStoreWriteSucceeds() {
            checkRetrieve(true, true, true);
        }

        @Test
        void testMultipleCopiesWhenUpfrontUserStoreWriteSucceeds() {
            checkRetrieve(true, false, true);
        }

        @Test
        void testSingleCopyStoreWhenUpfrontUserStoreWriteFails() {
            checkRetrieve(true, true, false);
        }

        @Test
        void testMultipleCopiesWhenUpfrontUserStoreWriteFails() {
            checkRetrieve(true, false, false);
        }

    }

    @Nested
    class NonSftpUserStoreTests {

        @BeforeEach
        void setup() {
            setupNonSftpUserStore();
        }

        @Test
        void testSingleCopyStoreWhenUpfrontUserStoreWriteSucceeds() {
            checkRetrieve(false, true, true);
        }

        @Test
        void testMultipleCopiesWhenUpfrontUserStoreWriteSucceeds() {
            checkRetrieve(false, false, true);
        }

        @Test
        void testSingleCopyStoreWhenUpfrontUserStoreWriteFails() {
            checkRetrieve(false, true, false);
        }

        @Test
        void testMultipleCopiesWhenUpfrontUserStoreWriteFails() {
            checkRetrieve(false, false, false);
        }

    }

    private Retrieve getRetrieveForTest(boolean singleCopy) {
        lenient().when(mContext.getEventSender()).thenReturn(mEventSender);
        lenient().when(mContext.getStorageClassNameResolver()).thenReturn(mStorageClassResolver);
        lenient().when(mContext.getTempDir()).thenReturn(Paths.get("/tmp/dir"));

        lenient().doNothing().when(mEventSender).send(argEvent.capture());
        lenient().when(mArchiveFS.hasMultipleCopies()).thenReturn(!singleCopy);

        Retrieve retrieve = Mockito.spy(new Retrieve());
        retrieve.setArchiveIds(new String[]{TEST_ARCHIVE_ID});
        Map<String, String> properties = new HashMap<>();

        properties.put(PropNames.DEPOSIT_ID, "test-deposit-id");
        properties.put(PropNames.DEPOSIT_CREATION_DATE, "13-mar-2024");
        properties.put(PropNames.RETRIEVE_ID, "retrieve-id");
        properties.put(PropNames.BAG_ID, "bag-id");
        properties.put(PropNames.RETRIEVE_PATH, TEST_RETRIEVE_PATH);
        properties.put(PropNames.ARCHIVE_ID, "archive-id");
        properties.put(PropNames.USER_ID, "user-id");
        properties.put(PropNames.ARCHIVE_DIGEST, "archive-digest");
        properties.put(PropNames.ARCHIVE_DIGEST_ALGORITHM, SHA_1_ALGORITHM);
        properties.put(PropNames.NUM_OF_CHUNKS, "1");
        properties.put(PropNames.ARCHIVE_SIZE, "2112");
        properties.put(PropNames.USER_FS_RETRY_MAX_ATTEMPTS, "10");
        properties.put(PropNames.USER_FS_RETRY_DELAY_MS_1, "50");
        properties.put(PropNames.USER_FS_RETRY_DELAY_MS_2, "50");
        retrieve.setProperties(properties);

        Map<String, String> userFileStoreClasses = new HashMap<>();
        userFileStoreClasses.put("USER_FILE_STORE_1", "USER_FILE_STORE_1_CLASS");
        retrieve.setUserFileStoreClasses(userFileStoreClasses);
        Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
        userFileStoreProperties.put("USER_FILE_STORE_1", new HashMap<>());
        retrieve.setUserFileStoreProperties(userFileStoreProperties);

        retrieve.setArchiveFileStores(Collections.singletonList(storageModel));
        return retrieve;
    }

    @SneakyThrows
    void checkRetrieve(boolean useSftpUserStore, boolean singleCopy, boolean upfrontUserStoreWriteSucceeds) {

        Retrieve retrieve = getRetrieveForTest(singleCopy);

        // override the clock with a fixed clock for testing
        lenient().when(retrieve.getClock()).thenReturn(TestClockConfig.TEST_CLOCK);

        lenient().doNothing().when(retrieve).fromArchiveStoreToUserStore(
                argContext.capture(),
                argTarFile.capture(),
                argArchiveDeviceInfo.capture(),
                argUserStoreInfo.capture(),
                argRetrievedChunks.capture());

        try (MockedStatic<StorageClassUtils> storageClassUtilsStatic = Mockito.mockStatic(StorageClassUtils.class)) {

            storageClassUtilsStatic.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any())).thenAnswer(invocation -> {
                String storageClassName = invocation.getArgument(0, String.class);
                if (storageClassName.equals("USER_FILE_STORE_1_CLASS")) {
                    if (useSftpUserStore) {
                        return mSftpUserStore;
                    } else {
                        return mNonSftpUserStore;
                    }
                } else {
                    return mArchiveFS;
                }
            });

            if (useSftpUserStore) {
                when(mSftpUserStore.getUsableSpace()).thenReturn(10_000L);
                when(mSftpUserStore.exists(any())).thenReturn(true);
                when(mSftpUserStore.isDirectory(any())).thenReturn(true);
            } else {
                when(mNonSftpUserStore.getUsableSpace()).thenReturn(10_000L);
                when(mNonSftpUserStore.exists(any())).thenReturn(true);
                when(mNonSftpUserStore.isDirectory(any())).thenReturn(true);

            }

            if (useSftpUserStore) {
                if (upfrontUserStoreWriteSucceeds) {
                    when(mSftpUserStore.store(any(), any(), any(), argTimestampDirName1.capture())).thenReturn("STORED");
                } else {
                    when(mSftpUserStore.store(any(), any(), any(), argTimestampDirName1.capture())).thenThrow(new RuntimeException("UserStoreFS : store failed"));
                }
            } else {
                if (upfrontUserStoreWriteSucceeds) {
                    when(mNonSftpUserStore.store(any(), any(), any())).thenReturn("STORED");
                } else {
                    when(mNonSftpUserStore.store(any(), any(), any())).thenThrow(new RuntimeException("UserStoreFS : store failed"));
                }
            }

            try {
                retrieve.performAction(mContext);
            } catch (RuntimeException ex) {
                if (upfrontUserStoreWriteSucceeds) {
                    fail("unexpected exception");
                } else {
                    assertThat(ex).hasMessage("UserStoreFS : store failed");
                }
            }


            if (useSftpUserStore) {
                // Check that we always store the DataVault hidden file on the userstore regardless of singleCopy or multipleCopies
                verify(mSftpUserStore).store(eq(TEST_RETRIEVE_PATH), argDataVaultHiddenFile.capture(), any(Progress.class), eq(TEST_TIMESTAMP_DIR_NAME));
            } else {
                verify(mNonSftpUserStore).store(eq(TEST_RETRIEVE_PATH), argDataVaultHiddenFile.capture(), any(Progress.class));
            }

            if (upfrontUserStoreWriteSucceeds) {

                Context actualContext = argContext.getValue();
                assertThat(actualContext).isEqualTo(mContext);

                File actualTarFile = argTarFile.getValue();
                assertThat(actualTarFile).isEqualTo(new File("/tmp/dir/bag-id.tar"));

                ArchiveDeviceInfo actualArchiveDeviceInfo = argArchiveDeviceInfo.getValue();
                Device actualArchiveFs = actualArchiveDeviceInfo.archiveFs();         
                //Device actualArchiveFs = argArchiveFs.getValue();
                assertThat(actualArchiveFs).isEqualTo(mArchiveFS);

                Device actualUserStoreFs = argUserStoreInfo.getValue().userFs();

                if (useSftpUserStore) {
                    assertThat(actualUserStoreFs).isEqualTo(mSftpUserStore);

                    String actualTimestampDir1 = argTimestampDirName1.getValue();
                    String actualTimestampDir2 = argUserStoreInfo.getValue().timeStampDirName();

                    assertThat(actualTimestampDir1).isEqualTo(actualTimestampDir2);
                    assertThat(actualTimestampDir1).isEqualTo(TEST_TIMESTAMP_DIR_NAME);
                    
                    assertThat(argDataVaultHiddenFile.getValue().getName()).isEqualTo(RetrieveUtils.DATA_VAULT_HIDDEN_FILE_NAME);
                } else {
                    assertThat(actualUserStoreFs).isEqualTo(mNonSftpUserStore);

                }
                
                verify(retrieve).fromArchiveStoreToUserStore(actualContext, actualTarFile, actualArchiveDeviceInfo, new UserStoreInfo(actualUserStoreFs, TEST_TIMESTAMP_DIR_NAME),  new RetrievedChunks());
                checkEventMessages("Job states: 5", "Deposit retrieve started", "User Store Space Available Checked", "Job progress update");
            } else {
                verify(retrieve, never()).fromArchiveStoreToUserStore(
                        any(Context.class), any(File.class), any(ArchiveDeviceInfo.class), any(UserStoreInfo.class), any(RetrievedChunks.class));

                checkEventMessages(
                        "Job states: 5",
                        "Deposit retrieve started",
                        "Unable to perform test write of file[.datavault] to user space",
                        "Data retrieve failed: UserStoreFS : store failed");
            }
        }
    }

    @Test
    void testProblemWithUserStore() {
        Retrieve retrieve = getRetrieveForTest(true);

        try (MockedStatic<StorageClassUtils> storageClassUtilsStatic = Mockito.mockStatic(StorageClassUtils.class)) {

            storageClassUtilsStatic.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any())).thenAnswer(invocation -> {
                String storageClassName = invocation.getArgument(0, String.class);
                assertThat(storageClassName).isEqualTo("USER_FILE_STORE_1_CLASS");
                throw new RuntimeException("problem creating UserStore");
            });

            try {
                retrieve.performAction(mContext);
                fail("exception expected!s");
            } catch (RuntimeException ex) {
                assertThat(ex).hasMessage("problem creating UserStore");
            }

            checkEventMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Retrieve failed: could not access user filesystem");
        }
    }

    @Test
    void testProblemWithArchiveStore() {
        Retrieve retrieve = getRetrieveForTest(true);

        try (MockedStatic<StorageClassUtils> storageClassUtilsStatic = Mockito.mockStatic(StorageClassUtils.class)) {

            storageClassUtilsStatic.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any())).thenAnswer(invocation -> {
                String storageClassName = invocation.getArgument(0, String.class);
                if (storageClassName.equals("USER_FILE_STORE_1_CLASS")) {
                    return mSftpUserStore;
                } else {
                    throw new RuntimeException("problem creating ArchiveStore");
                }
            });

            try {
                retrieve.performAction(mContext);
                fail("exception expected!");
            } catch (RuntimeException ex) {
                assertThat(ex).hasMessage("problem creating ArchiveStore");
            }

            checkEventMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Retrieve failed: could not access archive filesystem");
        }
    }

    @Test
    void testRedeliverTrue() {
        Retrieve retrieve = getRetrieveForTest(true);
        retrieve.setIsRedeliver(true);
        retrieve.performAction(mContext);
        checkEventMessages("Retrieve stopped: the message had been redelivered, please investigate");
    }

    @Test
    @SneakyThrows
    void testNotEnoughSpaceOnUserStore() {
        setupSftpUserStore();

        Retrieve retrieve = getRetrieveForTest(true);

        when(mSftpUserStore.getUsableSpace()).thenReturn(0L);

        try (MockedStatic<StorageClassUtils> storageClassUtilsStatic = Mockito.mockStatic(StorageClassUtils.class)) {

            storageClassUtilsStatic.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any())).thenAnswer(invocation -> {
                String storageClassName = invocation.getArgument(0, String.class);
                if (storageClassName.equals("USER_FILE_STORE_1_CLASS")) {
                    return mSftpUserStore;
                } else {
                    return mArchiveFS;
                }
            });

            try {
                retrieve.performAction(mContext);
                fail("exception expected!");
            } catch (RuntimeException ex) {
                assertThat(ex).hasMessage("Not enough free space to retrieve data!");
            }

            checkEventMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Not enough free space to retrieve data!",
                    "Data retrieve failed: Not enough free space to retrieve data!");
        }
    }
    @Test
    @SneakyThrows
    void testProblemWithUserStoreGetUsableSpace() {

        setupSftpUserStore();

        Retrieve retrieve = getRetrieveForTest(true);

        when(mSftpUserStore.getUsableSpace()).thenThrow(new Exception("problemGettingUsableSpace!"));

        try (MockedStatic<StorageClassUtils> storageClassUtilsStatic = Mockito.mockStatic(StorageClassUtils.class)) {

            storageClassUtilsStatic.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any())).thenAnswer(invocation -> {
                String storageClassName = invocation.getArgument(0, String.class);
                if (storageClassName.equals("USER_FILE_STORE_1_CLASS")) {
                    return mSftpUserStore;
                } else {
                    return mArchiveFS;
                }
            });

            try {
                retrieve.performAction(mContext);
                fail("exception expected!");
            } catch (RuntimeException ex) {
                assertThat(ex).hasMessage("org.datavaultplatform.worker.retry.DvRetryException: task[calcSizeToFS - retrieve-path]failed after[10] attempts");
            }

            checkEventMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Unable to determine free space",
                    "Data retrieve failed: task[calcSizeToFS - retrieve-path]failed after[10] attempts");
        }
    }


    private void checkEventMessages(String... expectedMessages) {
        String[] actualMessages = argEvent.getAllValues().stream().map(Event::getMessage).toArray(String[]::new);
        assertThat(actualMessages).isEqualTo(expectedMessages);
    }

    private void setupNonSftpUserStore() {
        lenient().when(mNonSftpUserStore.exists(TEST_RETRIEVE_PATH)).thenReturn(true);
        lenient().when(mNonSftpUserStore.isDirectory(TEST_RETRIEVE_PATH)).thenReturn(true);
    }

    @SneakyThrows
    private void setupSftpUserStore() {
        lenient().when(mSftpUserStore.exists(TEST_RETRIEVE_PATH)).thenReturn(true);
        lenient().when(mSftpUserStore.isDirectory(TEST_RETRIEVE_PATH)).thenReturn(true);
    }

    @Nested
    class RetrieveTargetErrorTests{

        @Test
        @SneakyThrows
        void testRetrieveTargetDoesNotExist() {
            when(mSftpUserStore.exists(TEST_RETRIEVE_PATH)).thenReturn(false);
            checkTargetDirectoryError();
            verify(mSftpUserStore).exists(TEST_RETRIEVE_PATH);
            verify(mSftpUserStore, never()).isDirectory(TEST_RETRIEVE_PATH);
        }

        @Test
        @SneakyThrows
        void testRetrieveTargetIsNotADirectory() {
            when(mSftpUserStore.exists(TEST_RETRIEVE_PATH)).thenReturn(true);
            when(mSftpUserStore.isDirectory(TEST_RETRIEVE_PATH)).thenReturn(false);
            checkTargetDirectoryError();
            verify(mSftpUserStore).exists(TEST_RETRIEVE_PATH);
            verify(mSftpUserStore).isDirectory(TEST_RETRIEVE_PATH);
        }

        void checkTargetDirectoryError() {
            Retrieve retrieve = getRetrieveForTest(true);
            try (MockedStatic<StorageClassUtils> storageClassUtilsStatic = Mockito.mockStatic(StorageClassUtils.class)) {
                storageClassUtilsStatic.when(() -> StorageClassUtils.createStorage(any(), any(), any(), any())).thenAnswer(invocation -> {
                    String storageClassName = invocation.getArgument(0, String.class);
                    if (storageClassName.equals("USER_FILE_STORE_1_CLASS")) {
                        return mSftpUserStore;
                    } else {
                        return mArchiveFS;
                    }
                });

                try {
                    retrieve.performAction(mContext);
                    fail("exception expected!");
                } catch (RuntimeException ex) {
                    assertThat(ex).hasMessage("Target directory not found or is not a directory ! [retrieve-path]");
                }

                checkEventMessages("Job states: 5",
                        "Deposit retrieve started",
                        "Target directory not found or is not a directory ! [retrieve-path]",
                        "Data retrieve failed: Target directory not found or is not a directory ! [retrieve-path]"
                );
            }
        }
    }

    @Nested
    class UserFsRetrieveTests {

        private static final int MAX_ATTEMPTS = 4;

        @TempDir
        private File payloadDir;


        ch.qos.logback.classic.Logger getRetryLogger() {
            @SuppressWarnings("UnnecessaryLocalVariable")
            ch.qos.logback.classic.Logger result = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TwoSpeedRetry.class);
            return result;
        }

        final ListAppender<ILoggingEvent> logBackListAppender = new ListAppender<>();

        @Mock
        Device mUserFs;

        @BeforeEach
        void setup() throws Exception {

            logBackListAppender.start();
            getRetryLogger().addAppender(logBackListAppender);


            assertThat(payloadDir).exists();

            File file1 = new File(payloadDir, "file1.txt");
            File file2 = new File(payloadDir, "file2.txt");
            File file3 = new File(payloadDir, "file3.txt");

            writeToFile(file1, "this is file 1");
            writeToFile(file2, "this is file 2");
            writeToFile(file3, "this is file 3");

        }

        @AfterEach
        void tearDown() {
            logBackListAppender.stop();
            getRetryLogger().detachAppender(logBackListAppender);
        }

        private void writeToFile(File file, String contents) throws Exception {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(contents);
            }
            assertThat(file).exists();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        void testSucceedsIfNthAttemptWorks(int attemptThatCopyFilesWorks) throws Exception {
            checkCopyFilesToUserFs(attemptThatCopyFilesWorks);

            if (attemptThatCopyFilesWorks == MAX_ATTEMPTS) {
                List<String> logMessages = this.logBackListAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());

                assertThat(logMessages).isEqualTo(Arrays.asList(
                        "delay1ms [10]",
                        "delay2ms [20]",
                        "totalNumberOfAttempts [4]",
                        
                        "created RetryTemplate [toUserFs - file1.txt]",
                        "Task[toUserFs - file1.txt] Initial attempt",
                        "Task[toUserFs - file1.txt] attempt[1/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file1.txt] attempt[1]]",
                        "Task[toUserFs - file1.txt] Backoff! attempts so far [1], retry in [010ms]",
                        "Task[toUserFs - file1.txt] Sleeping for [010ms]",
                        // CHANGE TO 2nd Backoff Delay of 20ms
                        "Task[toUserFs - file1.txt] attempt[2/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file1.txt] attempt[2]]",
                        "Task[toUserFs - file1.txt] Backoff! attempts so far [2], retry in [020ms]",
                        "Task[toUserFs - file1.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file1.txt] attempt[3/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file1.txt] attempt[3]]",
                        "Task[toUserFs - file1.txt] Backoff! attempts so far [3], retry in [020ms]",
                        "Task[toUserFs - file1.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file1.txt] Succeeded after [4] attempts",

                        "created RetryTemplate [toUserFs - file2.txt]",
                        "Task[toUserFs - file2.txt] Initial attempt",
                        "Task[toUserFs - file2.txt] attempt[1/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file2.txt] attempt[1]]",
                        "Task[toUserFs - file2.txt] Backoff! attempts so far [1], retry in [010ms]",
                        "Task[toUserFs - file2.txt] Sleeping for [010ms]",
                        // CHANGE TO 2nd Backoff Delay of 20ms
                        "Task[toUserFs - file2.txt] attempt[2/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file2.txt] attempt[2]]",
                        "Task[toUserFs - file2.txt] Backoff! attempts so far [2], retry in [020ms]",
                        "Task[toUserFs - file2.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file2.txt] attempt[3/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file2.txt] attempt[3]]",
                        "Task[toUserFs - file2.txt] Backoff! attempts so far [3], retry in [020ms]",
                        "Task[toUserFs - file2.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file2.txt] Succeeded after [4] attempts",

                        "created RetryTemplate [toUserFs - file3.txt]",
                        "Task[toUserFs - file3.txt] Initial attempt",
                        "Task[toUserFs - file3.txt] attempt[1/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file3.txt] attempt[1]]",
                        "Task[toUserFs - file3.txt] Backoff! attempts so far [1], retry in [010ms]",
                        "Task[toUserFs - file3.txt] Sleeping for [010ms]",
                        // CHANGE TO 2nd Backoff Delay of 20ms
                        "Task[toUserFs - file3.txt] attempt[2/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file3.txt] attempt[2]]",
                        "Task[toUserFs - file3.txt] Backoff! attempts so far [2], retry in [020ms]",
                        "Task[toUserFs - file3.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file3.txt] attempt[3/4] failed : Throwable[Exception]msg[java.lang.Exception: Failing on [file3.txt] attempt[3]]",
                        "Task[toUserFs - file3.txt] Backoff! attempts so far [3], retry in [020ms]",
                        "Task[toUserFs - file3.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file3.txt] Succeeded after [4] attempts"
                ));
            }
        }

        @SuppressWarnings("CodeBlock2Expr")
        @Test
        void testFailsAfter4FailedAttempts() {
            DvRetryException ex = assertThrows(DvRetryException.class, () -> {
                checkCopyFilesToUserFs(5);
            });
            assertThat(ex).hasMessage("task[toUserFs - file1.txt]failed after[4] attempts");
            assertThat(ex.getCause()).isInstanceOf(Exception.class);
            assertThat(ex.getCause()).hasMessage("Failing on [file1.txt] attempt[4]");
        }

        void checkCopyFilesToUserFs(int attemptThatCopyWorksOn) throws Exception {
            Progress progress = new Progress();
            Map<String, String> properties = new HashMap<>();
            properties.put(PropNames.USER_FS_RETRY_MAX_ATTEMPTS, String.valueOf(MAX_ATTEMPTS));
            properties.put(PropNames.USER_FS_RETRY_DELAY_MS_1, "10");
            properties.put(PropNames.USER_FS_RETRY_DELAY_MS_2, "20");
            Retrieve ret = spy(new Retrieve());
            lenient().doNothing().when(ret).sendEvent(argEvent.capture());
            
            ret.setupUserFsTwoSpeedRetry(properties);
            Map<File, Integer> attemptCountsPerFile = new HashMap<>();
            doAnswer(invocation -> {
                assertThat(invocation.getArguments()).hasSize(3);
                String path = invocation.getArgument(0, String.class);
                File argFile = invocation.getArgument(1, File.class);
                Progress argProgress = invocation.getArgument(2, Progress.class);

                assertThat(argProgress).isSameAs(progress);

                attemptCountsPerFile.merge(argFile, 1, Integer::sum);

                int attempts = attemptCountsPerFile.get(argFile);

                if (attempts == attemptThatCopyWorksOn) {
                    argProgress.incFileCount(1);
                    return path;
                }
                Path relativePath = payloadDir.toPath().relativize(argFile.toPath());
                throw new Exception(String.format("Failing on [%s] attempt[%d]", relativePath, attempts));

            }).when(mUserFs).store(any(), any(), any());

            ret.copyFilesToUserFs(progress, payloadDir, new UserStoreInfo(mUserFs,TEST_TIMESTAMP_DIR_NAME), 123_3456L);
        }
    }
}


