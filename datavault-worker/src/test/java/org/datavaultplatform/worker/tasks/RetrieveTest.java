package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.test.TestClockConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class RetrieveTest {

    public static final String TEST_RETRIEVE_PATH = "retrieve-path";
    public static final String TEST_TIMESTAMP_DIR_NAME = "dv_20220329141516";
    private static final String TEST_ARCHIVE_ID = "test-archive-id";

    @Mock
    EventSender mEventSender;
    @Mock
    Context mContext;
    @Mock
    StorageClassNameResolver mStorageClassResolver;
    @Mock
    LocalFileSystem mArchiveFS;
    @Mock
    SFTPFileSystemSSHD mSftpUserStore;
    @Mock
    LocalFileSystem mNonSftpUserStore;

    org.datavaultplatform.common.model.ArchiveStore storageModel;

    @Captor
    ArgumentCaptor<Event> argEvent;

    @Captor
    ArgumentCaptor<String> argSftpUserStoreTimestampDirName;

    @Captor
    ArgumentCaptor<String> argRetrieveTimestampDirName;

    @Captor
    ArgumentCaptor<Context> argContext;

    @Captor
    ArgumentCaptor<String> argTarFileName;

    @Captor
    ArgumentCaptor<File> argTarFile;

    @Captor
    ArgumentCaptor<Device> argArchiveFs;

    @Captor
    ArgumentCaptor<Device> argUserStoreFs;

    @Captor
    ArgumentCaptor<File> argDataVaultHiddenFile;

    @BeforeEach
    void setup() {
        this.storageModel = new ArchiveStore();
        this.storageModel.setStorageClass("ARCHIVE_STORE_STORAGE_CLASS_NAME");
    }

    @Test
    @SneakyThrows
    void testThrowCheckSumError() {
        File mFile = mock(File.class);
        when(mFile.getCanonicalPath()).thenReturn("/tmp/some/file.1.tar");
        Exception ex = assertThrows(Exception.class, () -> Retrieve.throwChecksumError("<actualCS>", "<expectedCS>",
                mFile, "test"));
        assertEquals("Checksum failed:test:(actual)<actualCS> != (expected)<expectedCS>:/tmp/some/file.1.tar", ex.getMessage());
    }


    @Test
    void testSftpSingleCopyStoreWhenUpfrontUserStoreWriteSucceeds() {
        setupSftpUserStore();
        checkRetrieve(true, true, true);
    }

    @Test
    void testSftpMultipleCopiesWhenUpfrontUserStoreWriteSucceeds() {
        setupSftpUserStore();
        checkRetrieve(true, false, true);
    }

    @Test
    void testSftpSingleCopyStoreWhenUpfrontUserStoreWriteFails() {
        setupSftpUserStore();
        checkRetrieve(true, true, false);
    }

    @Test
    void testSftpMultipleCopiesWhenUpfrontUserStoreWriteFails() {
        setupSftpUserStore();
        checkRetrieve(true, false, false);
    }

    @Test
    void testNonSftpSingleCopyStoreWhenUpfrontUserStoreWriteSucceeds() {
        setupNonSftpUserStore();
        checkRetrieve(false, true, true);
    }

    @Test
    void testNonSftpMultipleCopiesWhenUpfrontUserStoreWriteSucceeds() {
        setupNonSftpUserStore();
        checkRetrieve(false, false, true);
    }

    @Test
    void testNonSftpSingleCopyStoreWhenUpfrontUserStoreWriteFails() {
        setupNonSftpUserStore();
        checkRetrieve(false, true, false);
    }

    @Test
    void testNonSftpMultipleCopiesWhenUpfrontUserStoreWriteFails() {
        setupNonSftpUserStore();
        checkRetrieve(false, false, false);
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
        properties.put(PropNames.ARCHIVE_DIGEST_ALGORITHM, "archive-digest-algorithm");
        properties.put(PropNames.NUM_OF_CHUNKS, "1");
        properties.put(PropNames.ARCHIVE_SIZE, "2112");
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
        when(retrieve.getClock()).thenReturn(TestClockConfig.TEST_CLOCK);
        if (upfrontUserStoreWriteSucceeds) {
            if (singleCopy) {
                lenient().doNothing().when(retrieve).singleCopy(
                        argContext.capture(),
                        argTarFileName.capture(),
                        argTarFile.capture(),
                        argArchiveFs.capture(),
                        argUserStoreFs.capture(),
                        argRetrieveTimestampDirName.capture());
            } else {
                lenient().doNothing().when(retrieve).multipleCopies(
                        argContext.capture(),
                        argTarFileName.capture(),
                        argTarFile.capture(),
                        argArchiveFs.capture(),
                        argUserStoreFs.capture(),
                        argRetrieveTimestampDirName.capture());
            }
        }

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
                    when(mSftpUserStore.store(any(), any(), any(), argSftpUserStoreTimestampDirName.capture())).thenReturn("STORED");
                } else {
                    when(mSftpUserStore.store(any(), any(), any(), argSftpUserStoreTimestampDirName.capture())).thenThrow(new RuntimeException("UserStoreFS : store failed"));
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
                verify(mSftpUserStore).getUsableSpace();
                verify(mSftpUserStore).exists(any());
                verify(mSftpUserStore).isDirectory(any());
                verify(mSftpUserStore).store(any(), argDataVaultHiddenFile.capture(), any(), any());
            } else {
                verify(mNonSftpUserStore).getUsableSpace();
                verify(mNonSftpUserStore).exists(any());
                verify(mNonSftpUserStore).isDirectory(any());
                verify(mNonSftpUserStore).store(any(), argDataVaultHiddenFile.capture(), any());
            }

            if (upfrontUserStoreWriteSucceeds) {

                Context actualContext = argContext.getValue();
                assertThat(actualContext).isEqualTo(mContext);

                String actualTarFileName = argTarFileName.getValue();
                assertThat(actualTarFileName).isEqualTo("bag-id.tar");

                File actualTarFile = argTarFile.getValue();
                assertThat(actualTarFile).isEqualTo(new File("/tmp/dir/bag-id.tar"));

                Device actualArchiveFs = argArchiveFs.getValue();
                assertThat(actualArchiveFs).isEqualTo(mArchiveFS);

                Device actualUserStoreFs = argUserStoreFs.getValue();

                if (useSftpUserStore) {
                    assertThat(actualUserStoreFs).isEqualTo(mSftpUserStore);

                    String actualSftpUserStoreTimestampDirName = argSftpUserStoreTimestampDirName.getValue();
                    assertThat(actualSftpUserStoreTimestampDirName).isEqualTo(TEST_TIMESTAMP_DIR_NAME);

                    assertThat(argDataVaultHiddenFile.getValue().getName()).isEqualTo(Retrieve.DATA_VAULT_HIDDEN_FILE_NAME);
                } else {
                    assertThat(actualUserStoreFs).isEqualTo(mNonSftpUserStore);
                }
                String actualRetrieveTimestampDir = argRetrieveTimestampDirName.getValue();
                assertThat(actualRetrieveTimestampDir).isEqualTo(TEST_TIMESTAMP_DIR_NAME);

                if (singleCopy) {
                    verify(retrieve).singleCopy(any(), any(), any(), any(), any(), any());
                } else {
                    verify(retrieve).multipleCopies(any(), any(), any(), any(), any(), any());
                }

                checkErrorMessages("Job states: 5", "Deposit retrieve started", "Job progress update");
            } else {
                verify(retrieve, never()).singleCopy(any(), any(), any(), any(), any(), any());
                verify(retrieve, never()).multipleCopies(any(), any(), any(), any(), any(), any());

                checkErrorMessages(
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

            checkErrorMessages("Job states: 5",
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

            checkErrorMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Retrieve failed: could not access archive filesystem");
        }
    }

    @Test
    void testRedeliverTrue() {
        Retrieve retrieve = getRetrieveForTest(true);
        retrieve.setIsRedeliver(true);
        retrieve.performAction(mContext);
        checkErrorMessages("Retrieve stopped: the message had been redelivered, please investigate");
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

            checkErrorMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Not enough free space to retrieve data!",
                    "Data retrieve failed: Not enough free space to retrieve data!");
            verify(mSftpUserStore).exists(any());
            verify(mSftpUserStore).isDirectory(any());
            verify(mSftpUserStore).getUsableSpace();
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
                assertThat(ex).hasMessage("java.lang.Exception: problemGettingUsableSpace!");
            }

            checkErrorMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Unable to determine free space",
                    "Data retrieve failed: problemGettingUsableSpace!");

            verify(mSftpUserStore).exists(any());
            verify(mSftpUserStore).isDirectory(any());
            verify(mSftpUserStore).getUsableSpace();
        }
    }


    private void checkErrorMessages(String... expectedMessages) {
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

            checkErrorMessages("Job states: 5",
                    "Deposit retrieve started",
                    "Target directory not found or is not a directory ! [retrieve-path]",
                    "Data retrieve failed: Target directory not found or is not a directory ! [retrieve-path]"
            );
        }
    }

    @AfterEach
    void tearDown() {
        Mockito.verifyNoMoreInteractions(mSftpUserStore, mSftpUserStore);
    }

}


