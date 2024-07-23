package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.MultiLocalFileSystem;
import org.datavaultplatform.common.task.*;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StoredChunks;
import org.datavaultplatform.common.util.TestUtils;
import org.datavaultplatform.worker.tasks.deposit.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class DepositRestartTest {

    public static final String TEST_BAG_ID = "test-bag-id";
    public static final String TEST_DEPOSIT_ID = "test-deposit-id";
    public static final String TEST_USER_ID = "test-user-id";
    public static final String TEST_JOB_ID = "test-job-id";

    public final String USER_FILE_STORE_ONE = "user-file-store-one";

    public static final String VAULT_ADDRESS = null;
    public static final String VAULT_TOKEN = null;
    public static final String VAULT_KEY_PATH = null;
    public static final String VAULT_KEY_NAME = null;
    public static final String VAULT_SSL_PEM_PATH = null;
    public static final int NUMBER_CHUNK_THREADS = 5;
    public final Context.AESMode AES_MODE = Context.AESMode.GCM;

    public static final long CHUNKING_BYTE_SIZE = 1024 * 10; //10K
    public static final String RECOMPOSE_DATE_STRING = "1234";

    private final ArrayList<Event> events = new ArrayList<>();

    Path tempDir;
    Path metaDir;
    Path userFileStorePath;
    Path multiFileStorePath1;
    Path multiFileStorePath2;
    EventSender eventSender;
    StorageClassNameResolver storageClassNameResolver;

    final Deposit deposit = Mockito.spy(new Deposit());

    @Mock
    TaskStageEventListener mTaskStageEventListener;

    List<TaskStageEvent> taskStageEvents;

    @BeforeEach
    void setup() throws IOException {
        taskStageEvents = new ArrayList<>();
        lenient().doAnswer(invocation -> {
            taskStageEvents.add(invocation.getArgument(0, TaskStageEvent.class));
            return null;
        }).when(mTaskStageEventListener).onTaskStageEvent(any(TaskStageEvent.class));
        Path baseDir = Files.createTempDirectory("deposit-restart-tmp");
        tempDir = baseDir.resolve("tempDir");
        metaDir = baseDir.resolve("metaDir");
        userFileStorePath = baseDir.resolve("userFileStore");
        multiFileStorePath1 = baseDir.resolve("location1");
        multiFileStorePath2 = baseDir.resolve("location2");

        Files.createDirectories(tempDir);
        Files.createDirectories(metaDir);
        Files.createDirectories(userFileStorePath.resolve("src-path-1"));
        Files.createDirectories(multiFileStorePath1);
        Files.createDirectories(multiFileStorePath2);

        eventSender = createEventSender();
        storageClassNameResolver = StorageClassNameResolver.FIXED_JSCH;

        HashMap<String, String> properties = new HashMap<>();
        properties.put(PropNames.DEPOSIT_ID, TEST_DEPOSIT_ID);
        properties.put(PropNames.USER_ID, TEST_USER_ID);
        properties.put(PropNames.BAG_ID, TEST_BAG_ID);

        deposit.setTaskClass(Job.TASK_CLASS_DEPOSIT);
        deposit.setProperties(properties);

        //USER FILE STORE STUFF
        deposit.setFileStorePaths(List.of(USER_FILE_STORE_ONE + "/src-path-1"));
        deposit.setFileUploadPaths(List.of("src-file-upload-1"));
        deposit.setUserFileStoreClasses(Map.of(USER_FILE_STORE_ONE, LocalFileSystem.class.getName()));
        deposit.setUserFileStoreProperties(Map.of(USER_FILE_STORE_ONE, Map.of(PropNames.ROOT_PATH, userFileStorePath.toString())));

        ArchiveStore store = new ArchiveStore() {
            public String getID() {
                return "test-archive-store-id";
            }
        };
        store.setLabel("test-archive-store");
        store.setRetrieveEnabled(true);
        String rootPathValue = multiFileStorePath1.toFile() + "," + multiFileStorePath2.toString();
        store.setProperties(new HashMap<>(Map.of(PropNames.ROOT_PATH, rootPathValue)));
        store.setStorageClass(MultiLocalFileSystem.class.getName());

        deposit.setJobID(TEST_JOB_ID);
        deposit.setArchiveFileStores(List.of(store));
        
    }

    Context getContext(boolean chunkingEnabled, boolean encryptionEnabled, boolean multipleValidationEnabled) {
        ContextVaultInfo vaultInfo = new ContextVaultInfo(
                VAULT_ADDRESS,
                VAULT_TOKEN,
                VAULT_KEY_PATH,
                VAULT_KEY_NAME,
                VAULT_SSL_PEM_PATH);
        return new Context(
                tempDir,
                metaDir,
                eventSender,
                chunkingEnabled,
                CHUNKING_BYTE_SIZE,
                encryptionEnabled,
                AES_MODE,
                vaultInfo,
                multipleValidationEnabled,
                NUMBER_CHUNK_THREADS,
                storageClassNameResolver,
                false,
                RECOMPOSE_DATE_STRING,
                mTaskStageEventListener);
    }

    EventSender createEventSender() {
        return events::add;
    }

    @NullSource
    @ParameterizedTest
    @MethodSource("eventsBeforeComputedSize")
    void testSizeIsComputedWhenLastEventIsBeforeComputedSize(Event lastEvent) {

        Context context = getContext(true, true, true);

        DepositSizeComputer mComputer = Mockito.mock(DepositSizeComputer.class);

        doReturn(mComputer).when(deposit).getDepositSizeComputer(any(Map.class));

        when(mComputer.calculateTotalDepositSize()).thenThrow(new RuntimeException("oops"));

        // configure deposit for test
        deposit.setLastEvent(lastEvent);

        @SuppressWarnings("CodeBlock2Expr")
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            deposit.performAction(context);
        });
        assertThat(ex).hasMessage("Deposit failed: oops");

        verify(deposit).getDepositSizeComputer(any(Map.class));
        verify(mComputer).calculateTotalDepositSize();

        checkEvents(3);
        Event event1 = events.get(0);
        Event event2 = events.get(1);
        Event event3 = events.get(2);

        checkEventIsInitStates(event1);

        checkEventIsStart(event2);

        assertThat(event3).isInstanceOf(Error.class);
        Error error = (Error) event3;
        assertThat(error.getMessage()).isEqualTo("Deposit failed: oops");
        
        checkTaskStageEvents(this.taskStageEvents, TaskStage.Deposit1ComputeSize.INSTANCE);
    }

    @NullSource
    @SneakyThrows
    @ParameterizedTest
    @MethodSource("eventsBeforeTransferComplete")
    void testUserFilesAreTransferredWhenLastEventIsBeforeTransferComplete(Event lastEvent) {

        // configure deposit for test
        deposit.setLastEvent(lastEvent);
        
        // if we are starting from ComputedSize - this step is skipped
        DepositSizeComputer mComputer = Mockito.mock(DepositSizeComputer.class);
        lenient().doReturn(mComputer).when(deposit).getDepositSizeComputer(any(Map.class));
        lenient().when(mComputer.calculateTotalDepositSize()).thenReturn(1234L);

        DepositUserStoreDownloader mDownloader = Mockito.mock(DepositUserStoreDownloader.class);
        doReturn(mDownloader).when(deposit).getDepositUserStoreDownloader(any(Map.class));
        
        when(mDownloader.transferFromUserStoreToWorker()).thenThrow(new RuntimeException("oops"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            Context context = getContext(true, true, true);
            deposit.performAction(context);
        });
        assertThat(ex).hasMessage("Deposit failed: oops");

        verify(deposit, atMost(1)).getDepositSizeComputer(any(Map.class));
        verify(mComputer, atMost(1)).calculateTotalDepositSize();

        verify(deposit).getDepositUserStoreDownloader(any(Map.class));
        verify(mDownloader).transferFromUserStoreToWorker();

        checkEvents(3);
        checkEventIsInitStates(events.get(0));
        checkEventIsStart(events.get(1));
        checkEventIsError(events.get(2), "Deposit failed: oops");

        checkTaskStageEvents(this.taskStageEvents, TaskStage.Deposit1ComputeSize.INSTANCE);
    }

    @NullSource
    @ParameterizedTest
    @MethodSource("eventsBeforeComputedEncryption")
    void testFilesArePackagedWhenLastEventIsBeforeComputedEncryption(Event lastEvent)  throws Exception {

        File bagDir = Files.createTempDirectory("bagDir").toFile();
        
        // configure deposit for test
        deposit.setLastEvent(lastEvent);

        // if we are starting from ComputedSize - this step is skipped
        DepositSizeComputer mComputer = Mockito.mock(DepositSizeComputer.class);
        lenient().doReturn(mComputer).when(deposit).getDepositSizeComputer(any(Map.class));
        lenient().when(mComputer.calculateTotalDepositSize()).thenReturn(1234L);

        // if we are starting from TransferComplete - this step is skipped
        DepositUserStoreDownloader mDownloader = Mockito.mock(DepositUserStoreDownloader.class);
        lenient().doReturn(mDownloader).when(deposit).getDepositUserStoreDownloader(any(Map.class));
        when(mDownloader.transferFromUserStoreToWorker()).thenAnswer((Answer<File>) invocation -> {
            taskStageEvents.add(new TaskStageEvent(TaskStage.Deposit2Transfer.INSTANCE, true));
            return bagDir;
        });


        // we are proving that this step gets called
        DepositPackager mPackager = Mockito.mock(DepositPackager.class);
        doReturn(mPackager).when(deposit).getDepositPackager();
        when(mPackager.packageStep(bagDir)).thenThrow(new RuntimeException("oops"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            Context context = getContext(true, true, true);
            deposit.performAction(context);
        });
        assertThat(ex).hasMessage("Deposit failed: oops");

        verify(deposit, atMost(1)).getDepositSizeComputer(any(Map.class));
        verify(mComputer, atMost(1)).calculateTotalDepositSize();

        verify(deposit, atMost(1)).getDepositUserStoreDownloader(any(Map.class));
        verify(mDownloader, atMost(1)).transferFromUserStoreToWorker();

        verify(deposit).getDepositPackager();
        verify(mPackager).packageStep(bagDir);

        checkEvents(3);
        checkEventIsInitStates(events.get(0));
        checkEventIsStart(events.get(1));
        checkEventIsError(events.get(2), "Deposit failed: oops");
        
        checkTaskStageEvents(this.taskStageEvents, TaskStage.Deposit3PackageEncrypt.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @NullSource
    @ParameterizedTest
    @MethodSource("eventsBeforeUploadComplete")
    void testFilesAreUploadedWhenLastEventIsBeforeUploadComplete(Event lastEvent)  throws Exception {

        File bagDir = Files.createTempDirectory("bagDir").toFile();
        PackageHelper packageHelper = new PackageHelper();

        // configure deposit for test
        deposit.setLastEvent(lastEvent);

        // if we are starting from ComputedSize - this step is skipped
        DepositSizeComputer mComputer = Mockito.mock(DepositSizeComputer.class);
        lenient().doReturn(mComputer).when(deposit).getDepositSizeComputer(any(Map.class));
        lenient().when(mComputer.calculateTotalDepositSize()).thenReturn(1234L);

        // if we are starting from TransferComplete - this step is skipped
        DepositUserStoreDownloader mDownloader = Mockito.mock(DepositUserStoreDownloader.class);
        lenient().doReturn(mDownloader).when(deposit).getDepositUserStoreDownloader(any(Map.class));
        lenient().when(mDownloader.transferFromUserStoreToWorker()).thenAnswer((Answer<File>) invocation -> {
            taskStageEvents.add(new TaskStageEvent(TaskStage.Deposit2Transfer.INSTANCE, true));
            return bagDir;
        });

        // if we are staring from ComputedEncryption - this step is skipped
        DepositPackager mPackager = Mockito.mock(DepositPackager.class);
        lenient().doReturn(mPackager).when(deposit).getDepositPackager();
        lenient().when(mPackager.packageStep(bagDir)).thenReturn(packageHelper);

        // we are checking that this step gets invoked
        DepositArchiveStoresUploader mUploader = Mockito.mock(DepositArchiveStoresUploader.class);
        
        doReturn(mUploader).when(deposit).getDepositArchiveStoresUploader(any(ArchiveStoreContext.class));
        when(mUploader.uploadToStorage(any(PackageHelper.class), any(StoredChunks.class))).thenThrow(new RuntimeException("oops"));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            Context context = getContext(true, true, true);
            deposit.performAction(context);
        });
        assertThat(ex).hasMessage("Deposit failed: oops");

        verify(deposit, atMost(1)).getDepositSizeComputer(any(Map.class));
        verify(mComputer, atMost(1)).calculateTotalDepositSize();

        verify(deposit, atMost(1)).getDepositUserStoreDownloader(any(Map.class));
        verify(mDownloader, atMost(1)).transferFromUserStoreToWorker();

        verify(deposit, atMost(1)).getDepositPackager();
        verify(mPackager, atMost(1)).packageStep(bagDir);

        verify(deposit).getDepositArchiveStoresUploader(any(ArchiveStoreContext.class));
        verify(mUploader).uploadToStorage(any(PackageHelper.class), any(StoredChunks.class));

        checkEvents(3);
        checkEventIsInitStates(events.get(0));
        checkEventIsStart(events.get(1));
        checkEventIsError(events.get(2),"Deposit failed: oops");
        
        checkTaskStageEvents(this.taskStageEvents, TaskStage.Deposit4Archive.INSTANCE);
    }

    @NullSource
    @ParameterizedTest
    @MethodSource("eventsBeforeValidationComplete")
    void testFilesAreValidatedWhenLastEventIsBeforeValidationComplete(Event lastEvent)  throws Exception {

        File bagDir = Files.createTempDirectory("bagDir").toFile();
        PackageHelper packageHelper = new PackageHelper();

        // configure deposit for test
        deposit.setLastEvent(lastEvent);

        // if we are starting from ComputedSize - this step is skipped
        DepositSizeComputer mComputer = Mockito.mock(DepositSizeComputer.class);
        lenient().doReturn(mComputer).when(deposit).getDepositSizeComputer(any(Map.class));
        lenient().when(mComputer.calculateTotalDepositSize()).thenReturn(1234L);

        // if we are starting from TransferComplete - this step is skipped
        DepositUserStoreDownloader mDownloader = Mockito.mock(DepositUserStoreDownloader.class);
        lenient().doReturn(mDownloader).when(deposit).getDepositUserStoreDownloader(any(Map.class));
        lenient().when(mDownloader.transferFromUserStoreToWorker()).thenAnswer((Answer<File>) invocation -> {
            taskStageEvents.add(new TaskStageEvent(TaskStage.Deposit2Transfer.INSTANCE, true));
            return bagDir;
        });

        // if we are staring from ComputedEncryption - this step is skipped
        DepositPackager mPackager = Mockito.mock(DepositPackager.class);
        lenient().doReturn(mPackager).when(deposit).getDepositPackager();
        lenient().when(mPackager.packageStep(bagDir)).thenReturn(packageHelper);

        // we are checking that this step gets invoked
        DepositArchiveStoresUploader mUploader = Mockito.mock(DepositArchiveStoresUploader.class);
        lenient().doReturn(mUploader).when(deposit).getDepositArchiveStoresUploader(any(ArchiveStoreContext.class));
        lenient().when(mUploader.uploadToStorage(any(PackageHelper.class),any(StoredChunks.class))).thenReturn(null);

        // we are checking that this step gets invoked
        DepositVerifier mVerifier = Mockito.mock(DepositVerifier.class);
        doReturn(mVerifier).when(deposit).getDepositVerifier(any(Set.class));
        doThrow(new RuntimeException("oops")).when(mVerifier).verifyArchive(any(PackageHelper.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            Context context = getContext(true, true, true);
            deposit.performAction(context);
        });
        assertThat(ex).hasMessage("Deposit failed: oops");

        verify(deposit, atMost(1)).getDepositSizeComputer(any(Map.class));
        verify(mComputer, atMost(1)).calculateTotalDepositSize();

        verify(deposit, atMost(1)).getDepositUserStoreDownloader(any(Map.class));
        verify(mDownloader, atMost(1)).transferFromUserStoreToWorker();

        verify(deposit, atMost(1)).getDepositPackager();
        verify(mPackager, atMost(1)).packageStep(bagDir);
        
        verify(deposit, atMost(1)).getDepositArchiveStoresUploader(any(ArchiveStoreContext.class));
        verify(mUploader, atMost(1)).uploadToStorage(any(PackageHelper.class), any(StoredChunks.class));

        verify(deposit).getDepositVerifier(any(Set.class));
        verify(mVerifier).verifyArchive(any(PackageHelper.class));

        checkEvents(3);
        checkEventIsInitStates(events.get(0));
        checkEventIsStart(events.get(1));
        checkEventIsError(events.get(2),"Deposit failed: oops");

        checkTaskStageEvents(this.taskStageEvents, TaskStage.Deposit5Verify.INSTANCE);
    }

    private static TaskStageEvent checkTaskStageEvents(List<TaskStageEvent> taskStageEvents, TaskStage lastStage) {
        assertThat(taskStageEvents).isEqualTo(TestUtils.sort(taskStageEvents));

        List<Integer> actualStageNumbers = taskStageEvents.stream()
                .map(TaskStageEvent::stage)
                .map(TaskStage::getOrder)
                .toList();
        List<Integer> expectedStageNumbers = IntStream.rangeClosed(1, taskStageEvents.size()).boxed().toList();
        assertThat(actualStageNumbers).isEqualTo(expectedStageNumbers);

        List<TaskStageEvent> lastStageEvents = taskStageEvents.stream()
                .dropWhile(evt -> !evt.stage().equals(lastStage))
                .toList();

        assertThat(lastStageEvents).hasSize(1);
        TaskStageEvent lastStageEvent = lastStageEvents.get(0);
        return lastStageEvent;
    }

    @NullSource
    @ParameterizedTest
    @MethodSource("eventsBeforeComplete")
    void testFilesAreValidatedWhenLastEventIsBeforeComplete(Event lastEvent)  throws Exception {

        File bagDir = Files.createTempDirectory("bagDir").toFile();
        PackageHelper packageHelper = new PackageHelper();

        // configure deposit for test
        deposit.setLastEvent(lastEvent);

        // if we are starting from ComputedSize - this step is skipped
        DepositSizeComputer mComputer = Mockito.mock(DepositSizeComputer.class);
        lenient().doReturn(mComputer).when(deposit).getDepositSizeComputer(any(Map.class));
        lenient().when(mComputer.calculateTotalDepositSize()).thenReturn(1234L);

        // if we are starting from TransferComplete - this step is skipped
        DepositUserStoreDownloader mDownloader = Mockito.mock(DepositUserStoreDownloader.class);
        lenient().doReturn(mDownloader).when(deposit).getDepositUserStoreDownloader(any(Map.class));
        lenient().when(mDownloader.transferFromUserStoreToWorker()).thenAnswer((Answer<File>) invocation -> {
            taskStageEvents.add(new TaskStageEvent(TaskStage.Deposit2Transfer.INSTANCE, true));
            return bagDir;
        });

        // if we are staring from ComputedEncryption - this step is skipped
        DepositPackager mPackager = Mockito.mock(DepositPackager.class);
        lenient().doReturn(mPackager).when(deposit).getDepositPackager();
        lenient().when(mPackager.packageStep(bagDir)).thenReturn(packageHelper);

        // if we are staring from UploadComplete - this step is skipped
        DepositArchiveStoresUploader mUploader = Mockito.mock(DepositArchiveStoresUploader.class);
        lenient().doReturn(mUploader).when(deposit).getDepositArchiveStoresUploader(any(ArchiveStoreContext.class));
        lenient().when(mUploader.uploadToStorage(any(PackageHelper.class), any(StoredChunks.class))).thenReturn(null);

        // if we are staring from ValidationComplete - this step is skipped
        DepositVerifier mVerifier = Mockito.mock(DepositVerifier.class);
        lenient().doReturn(mVerifier).when(deposit).getDepositVerifier(any(Set.class));
        lenient().doNothing().when(mVerifier).verifyArchive(any(PackageHelper.class));


        Context context = getContext(true, true, true);
        deposit.performAction(context);

        verify(deposit, atMost(1)).getDepositSizeComputer(any(Map.class));
        verify(mComputer, atMost(1)).calculateTotalDepositSize();

        verify(deposit, atMost(1)).getDepositUserStoreDownloader(any(Map.class));
        verify(mDownloader, atMost(1)).transferFromUserStoreToWorker();

        verify(deposit, atMost(1)).getDepositPackager();
        verify(mPackager, atMost(1)).packageStep(bagDir);

        verify(deposit, atMost(1)).getDepositArchiveStoresUploader(any(ArchiveStoreContext.class));
        verify(mUploader, atMost(1)).uploadToStorage(any(PackageHelper.class), any(StoredChunks.class));

        verify(deposit, atMost(1)).getDepositVerifier(any(Set.class));
        verify(mVerifier, atMost(1)).verifyArchive(any(PackageHelper.class));

        checkEvents(3);
        checkEventIsInitStates(events.get(0));
        checkEventIsStart(events.get(1));
        checkEventIsComplete(events.get(2));

        checkTaskStageEvents(this.taskStageEvents, TaskStage.Deposit6Final.INSTANCE);
    }

    private void checkEvents(int expectedSize) {
        assertThat(events).hasSize(expectedSize);
        this.events.forEach(evt -> checkEvent(evt, "test-job-id", "test-deposit-id"));
    }

    private void checkEvent(Event event, String jobId, String depositId) {
        assertThat(event.getJobId()).isEqualTo(jobId);
        assertThat(event.getDepositId()).isEqualTo(depositId);
    }

    private static Stream<Arguments> eventsBeforeComputedSize() {
        return priorEvents(ComputedSize.class);
    }
    
    private static Stream<Arguments> eventsBeforeTransferComplete() {
        return priorEvents(TransferComplete.class);
    }

    private static Stream<Arguments> eventsBeforeComputedEncryption() {
        return priorEvents(ComputedEncryption.class);
    }

    private static Stream<Arguments> eventsBeforeUploadComplete() {
        return priorEvents(UploadComplete.class);
    }

    private static Stream<Arguments> eventsBeforeValidationComplete() {
        return priorEvents(ValidationComplete.class);
    }

    private static Stream<Arguments> eventsBeforeComplete() {
        return priorEvents(Complete.class);
    }

    @SneakyThrows
    private static Stream<Arguments> priorEvents(Class<? extends Event> event) {
        return DepositEvents.INSTANCE.getEventsBefore(event).stream()
                .map(DepositRestartTest::eventFromClassName)
                .map(Arguments::of);
    }

    @SneakyThrows
    private static Event eventFromClassName(String evenClassName) {
        Class<? extends Event> eventClass = Class.forName(evenClassName).asSubclass(Event.class);
        Event event = eventClass.getConstructor().newInstance();
        event.setEventClass(eventClass.getName());
        return event;
    }
    
    private void checkEventIsInitStates(Event event) {
        assertThat(event).isInstanceOf(InitStates.class);
        InitStates initStates = (InitStates) event;
        assertThatIterable(initStates.getStates()).containsExactly("Calculating size",
                "Transferring",
                "Packaging",
                "Storing in archive",
                "Verifying",
                "Complete");
    }
    
    private void checkEventIsError(Event event, String message) {
        assertThat(event).isInstanceOf(Error.class);
        Error error = (Error) event;
        assertThat(error.getMessage()).isEqualTo(message);
    }
    private void checkEventIsComplete(Event event){
        assertThat(event).isInstanceOf(Complete.class);
        Complete complete = (Complete) event;
    }
    private void checkEventIsStart(Event event) {
        assertThat(event).isInstanceOf(Start.class);
        Start start = (Start) event;
        assertThat(start.getDepositId()).isEqualTo("test-deposit-id");
        assertThat(start.getJobId()).isEqualTo("test-job-id");
    }
}
