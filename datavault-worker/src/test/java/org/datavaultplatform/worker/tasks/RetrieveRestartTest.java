package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;

import static org.assertj.core.api.Assertions.*;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskStageEvent;
import org.datavaultplatform.common.task.TaskStageEventListener;
import org.datavaultplatform.common.util.RetrievedChunks;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.Utils;
import org.datavaultplatform.worker.tasks.retrieve.ArchiveDeviceInfo;
import org.datavaultplatform.worker.tasks.retrieve.UserStoreInfo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RetrieveRestartTest {

    static final String TEST_BAG_ID_TAR_HASH = "C8E8A4EEBD02DD89787CEA613D12CC297F329584";
    
    static final String TEST_JOB_ID = "test-job-id";
    static final String TEST_DEPOSIT_ID = "test-deposit-id";
    static final String TEST_BAG_ID = "test-bag-id";
    static final String TEST_USER_ID = "test-user-id";
    static final String TEST_RETRIEVE_ID = "test-retrieve-id";
    private static final String FILE_STORE_ONE = "user-file-store-one";
    Path tempPath;
    Path metaPath;
    @Mock
    Context mContext;
    EventSender eventSender;
    @Captor
    ArgumentCaptor<UserStoreInfo> argUserStoreInfo;
    @Captor
    ArgumentCaptor<ArchiveDeviceInfo> argArchiveDeviceInfg;
    @Captor
    ArgumentCaptor<Progress> argProgress;
    @Captor
    ArgumentCaptor<File> argTarFile;
    @Captor
    ArgumentCaptor<RetrievedChunks> argRetrievedChunks;

    ArchiveStore archiveStore;

    List<Event> sentEvents;

    final ClassPathResource testBagIdTar = new ClassPathResource("retrieve/test-bag-id.tar");

    @Mock
    TaskStageEventListener mTaskStageEventListener;

    List<TaskStageEvent> taskStageEvents;

    private static Stream<Arguments> lastEventIsBeforeUserStoreSpaceAvailableCheckedArgs() {
        return Stream.of(
                Arguments.of(new InitStates()),
                Arguments.of(new RetrieveStart()),
                Arguments.of(new UploadComplete()), //a deposit event
                Arguments.of(new UpdateProgress()), //a general event
                Arguments.of(new Error())); //n general event
    }

    @SneakyThrows
    HashMap<String, String> getProperties(String archiveDigest) {
        var props = new HashMap<String, String>();
        props.put(PropNames.DEPOSIT_ID, TEST_DEPOSIT_ID);
        props.put(PropNames.BAG_ID, TEST_BAG_ID);
        props.put(PropNames.USER_ID, TEST_USER_ID);
        props.put(PropNames.DEPOSIT_CHUNKS_RETRIEVED, RetrievedChunks.toJson(new RetrievedChunks()));
        props.put(PropNames.NUM_OF_CHUNKS, "123");
        props.put(PropNames.ARCHIVE_SIZE, "123456");
        props.put(PropNames.ARCHIVE_DIGEST_ALGORITHM, Verify.SHA_1_ALGORITHM);
        props.put(PropNames.ARCHIVE_DIGEST, archiveDigest);
        return props;
    }

    @BeforeEach
    void setup() throws Exception {

        sentEvents = new ArrayList<>();
        eventSender = sentEvents::add;

        Path tempBase = Files.createTempDirectory("dv-temp-base");
        tempPath = tempBase.resolve("temp");
        metaPath = tempBase.resolve("meta");
        Files.createDirectories(tempPath);
        Files.createDirectories(metaPath);

        lenient().when(mContext.getTempDir()).thenReturn(tempPath);
        lenient().when(mContext.getMetaDir()).thenReturn(metaPath);
        lenient().when(mContext.getNoChunkThreads()).thenReturn(1);

        lenient().when(mContext.isChunkingEnabled()).thenReturn(true);
        lenient().when(mContext.isEncryptionEnabled()).thenReturn(true);
        lenient().when(mContext.isOldRecompose()).thenReturn(false);

        lenient().when(mContext.getEncryptionMode()).thenReturn(Context.AESMode.GCM);
        lenient().when(mContext.getStorageClassNameResolver()).thenReturn(StorageClassNameResolver.FIXED_JSCH);
        lenient().when(mContext.getRecomposeDate()).thenReturn("20240619");
        lenient().when(mContext.getChunkingByteSize()).thenReturn(1234L);
        lenient().when(mContext.getEventSender()).thenReturn(eventSender);

        archiveStore = new ArchiveStore();
        archiveStore.setStorageClass(TivoliStorageManager.class.getName());
        archiveStore.setRetrieveEnabled(true);
        archiveStore.setProperties(new HashMap<>());
        archiveStore.setLabel("TSM");

        this.taskStageEvents = new ArrayList<>();
        lenient().when(mContext.getTaskStageEventListener()).thenReturn(mTaskStageEventListener);

        lenient().doAnswer(invocation -> {
            TaskStageEvent taskStageEvent = invocation.getArgument(0, TaskStageEvent.class);
            taskStageEvents.add(taskStageEvent);
            return null;
        }).when(mTaskStageEventListener).onTaskStageEvent(any(TaskStageEvent.class));
    }
    
    Retrieve getRetrieve(Event lastEvent) {
        return getRetrieve(lastEvent, TEST_BAG_ID_TAR_HASH);
    }
        
    Retrieve getRetrieve(Event lastEvent, String archiveDigest) {
        Retrieve result = new Retrieve();
        result.setLastEvent(lastEvent);
        result.setJobID(TEST_JOB_ID);
        result.setIsRedeliver(false);
        result.setProperties(getProperties(archiveDigest));
        // TODO : why 2nd hashmap
        result.setupUserFsTwoSpeedRetry(new HashMap<>());
        result.setChunksToAudit(Collections.emptyList());
        result.setUserFileStoreClasses(new HashMap<>());
        result.setUserFileStoreClasses(new HashMap<>());
        result.setRestartArchiveIds(new HashMap<>());
        result.setArchiveFileStores(List.of(archiveStore));
        result.setUserFileStoreProperties(Map.of(FILE_STORE_ONE, Map.of(PropNames.ROOT_PATH, tempPath.toString())));
        result.setUserFileStoreClasses(Map.of(FILE_STORE_ONE, LocalFileSystem.class.getName()));
        return result;
        

    }
    
    @Test
    @Order(0)
    @SneakyThrows
    void testTarHash() {
        Utils.checkFileHash("test", testBagIdTar.getFile(), TEST_BAG_ID_TAR_HASH);
    }
    
    @Order(10)
    @ParameterizedTest
    @MethodSource("lastEventIsBeforeUserStoreSpaceAvailableCheckedArgs")
    @NullSource
    void testLastEventIsBeforeUserStoreSpaceAvailableChecked(Event lastEvent) throws Exception {
        Retrieve retrieve = Mockito.spy(getRetrieve(null));

        doNothing().when(retrieve).checkUserStoreFreeSpaceAndPermissions(argUserStoreInfo.capture());
        doNothing().when(retrieve).retrieveChunksAndRecompose(eq(mContext), argArchiveDeviceInfg.capture(),
                argProgress.capture(), argTarFile.capture(), argRetrievedChunks.capture());

        doNothing().when(retrieve).doRetrieveFromWorkerToUserFs(eq(mContext), argUserStoreInfo.capture(), argTarFile.capture(), argProgress.capture());

        retrieve.performAction(mContext);

        //STAGE 0 - check user store free space
        {
            UserStoreInfo actualUserStoreInfo1 = argUserStoreInfo.getAllValues().get(0);
            assertThat(actualUserStoreInfo1.timeStampDirName()).isNotNull();
            assertThat(actualUserStoreInfo1.userFs()).isInstanceOf(LocalFileSystem.class);
            LocalFileSystem localFileSystem = (LocalFileSystem) actualUserStoreInfo1.userFs();
            assertThat(localFileSystem.getLocations()).isNull();

            verify(retrieve).checkUserStoreFreeSpaceAndPermissions(actualUserStoreInfo1);
        }

        //STAGE 1 - download from arhive store, decrypt and re-compose
        {
            ArchiveDeviceInfo actualArchiveDeviceInfo1 = argArchiveDeviceInfg.getAllValues().get(0);

            File actualTarFile1 = argTarFile.getAllValues().get(0);
            assertThat(actualTarFile1.toPath()).isEqualTo(tempPath.resolve("test-bag-id.tar"));
            assertThat(actualTarFile1.exists()).isFalse();

            Progress actualProgress1 = argProgress.getAllValues().get(0);
            assertThat(actualProgress1).isNotNull();

            RetrievedChunks actualRetrievedChunks1 = argRetrievedChunks.getAllValues().get(0);
            assertThat(actualRetrievedChunks1.size()).isEqualTo(0);
            verify(retrieve).retrieveChunksAndRecompose(mContext, actualArchiveDeviceInfo1, actualProgress1, actualTarFile1, actualRetrievedChunks1);
        }

        //STAGE 2 - upload to user store
        {
            UserStoreInfo actualUserStoreInfo2 = argUserStoreInfo.getAllValues().get(1);
            File actualTarFile2 = argTarFile.getAllValues().get(1);
            Progress actualProgress2 = argProgress.getAllValues().get(1);
            verify(retrieve).doRetrieveFromWorkerToUserFs(mContext, actualUserStoreInfo2, actualTarFile2, actualProgress2);
        }

        var eventClasses = sentEvents.stream().map(evt -> evt.eventClass).toList();
        assertThat(eventClasses).isEqualTo(List.of(
                "org.datavaultplatform.common.event.InitStates",
                "org.datavaultplatform.common.event.retrieve.RetrieveStart",
                "org.datavaultplatform.common.event.retrieve.UserStoreSpaceAvailableChecked",
                "org.datavaultplatform.common.event.UpdateProgress",
                "org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedAll",
                "org.datavaultplatform.common.event.retrieve.RetrieveComplete"
        ));
    }

    @Order(20)
    @Test
    void testLastEventIsUserStoreSpaceAvailableChecked() throws Exception {
        Retrieve retrieve = Mockito.spy(getRetrieve(new UserStoreSpaceAvailableChecked()));

        doNothing().when(retrieve).retrieveChunksAndRecompose(eq(mContext), argArchiveDeviceInfg.capture(),
                argProgress.capture(), argTarFile.capture(), argRetrievedChunks.capture());

        doNothing().when(retrieve).doRetrieveFromWorkerToUserFs(eq(mContext), argUserStoreInfo.capture(), argTarFile.capture(), argProgress.capture());

        retrieve.performAction(mContext);

        //STAGE 0 - check user store free space - SKIPPED
        {
            verify(retrieve, never()).checkUserStoreFreeSpaceAndPermissions(any(UserStoreInfo.class));
        }

        //STAGE 1 - download from arhive store, decrypt and re-compose
        {
            ArchiveDeviceInfo actualArchiveDeviceInfo1 = argArchiveDeviceInfg.getAllValues().get(0);

            File actualTarFile1 = argTarFile.getAllValues().get(0);
            assertThat(actualTarFile1.toPath()).isEqualTo(tempPath.resolve("test-bag-id.tar"));
            assertThat(actualTarFile1.exists()).isFalse();

            Progress actualProgress1 = argProgress.getAllValues().get(0);
            assertThat(actualProgress1).isNotNull();

            RetrievedChunks actualRetrievedChunks1 = argRetrievedChunks.getAllValues().get(0);
            assertThat(actualRetrievedChunks1.size()).isEqualTo(0);
            verify(retrieve).retrieveChunksAndRecompose(mContext, actualArchiveDeviceInfo1, actualProgress1, actualTarFile1, actualRetrievedChunks1);
        }

        //STAGE 2 - upload to user store
        {
            UserStoreInfo actualUserStoreInfo2 = argUserStoreInfo.getAllValues().get(0);
            File actualTarFile2 = argTarFile.getAllValues().get(1);
            Progress actualProgress2 = argProgress.getAllValues().get(1);
            verify(retrieve).doRetrieveFromWorkerToUserFs(mContext, actualUserStoreInfo2, actualTarFile2, actualProgress2);
        }

        var eventClasses = sentEvents.stream().map(evt -> evt.eventClass).toList();
        assertThat(eventClasses).isEqualTo(List.of(
                //TODO : might need to check the InitStates and RetrieveStart are not sent again ?
                "org.datavaultplatform.common.event.InitStates",
                "org.datavaultplatform.common.event.retrieve.RetrieveStart",
                
                "org.datavaultplatform.common.event.UpdateProgress",
                "org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedAll",
                "org.datavaultplatform.common.event.retrieve.RetrieveComplete"
        ));
    }

    @Order(30)
    @Test
    void testLastEventIsArchiveStoreRetrievedAllAndHashIsOkay() throws Exception {

        File tarFile = tempPath.resolve(TEST_BAG_ID+".tar").toFile();
        FileUtils.copyURLToFile(testBagIdTar.getURL(), tarFile);
        
        Retrieve retrieve = Mockito.spy(getRetrieve(new ArchiveStoreRetrievedAll()));

        doNothing().when(retrieve).doRetrieveFromWorkerToUserFs(eq(mContext), argUserStoreInfo.capture(), argTarFile.capture(), argProgress.capture());

        retrieve.performAction(mContext);

        //STAGE 0 - check user store free space - SKIPPED
        {
            verify(retrieve, never()).checkUserStoreFreeSpaceAndPermissions(any(UserStoreInfo.class));
        }

        //STAGE 1 - download from arhive store, decrypt and re-compose - SKIPPED
        {
            verify(retrieve, never()).retrieveChunksAndRecompose(any(Context.class), any(ArchiveDeviceInfo.class), any(Progress.class), any(File.class), any(RetrievedChunks.class));
        }

        //STAGE 2 - upload to user store
        {
            UserStoreInfo actualUserStoreInfo2 = argUserStoreInfo.getAllValues().get(0);
            File actualTarFile2 = argTarFile.getAllValues().get(0);
            Progress actualProgress2 = argProgress.getAllValues().get(0);
            verify(retrieve).doRetrieveFromWorkerToUserFs(mContext, actualUserStoreInfo2, actualTarFile2, actualProgress2);
        }

        var eventClasses = sentEvents.stream().map(evt -> evt.eventClass).toList();
        assertThat(eventClasses).isEqualTo(List.of(
                //TODO : might need to check the InitStates and RetrieveStart are not sent again ?
                "org.datavaultplatform.common.event.InitStates",
                "org.datavaultplatform.common.event.retrieve.RetrieveStart",

                "org.datavaultplatform.common.event.UpdateProgress",
                "org.datavaultplatform.common.event.retrieve.RetrieveComplete"
        ));
    }

    @Order(35)
    @Test
    void testLastEventIsArchiveStoreRetrievedAllButHashIsBad() throws Exception {

        assertThat(mContext.isChunkingEnabled()).isTrue();
        
        var temp = getRetrieve(new ArchiveStoreRetrievedAll(), "BAD-HASH");
        Retrieve retrieve = Mockito.spy(temp);

        doNothing().when(retrieve).retrieveChunksAndRecompose(eq(mContext), argArchiveDeviceInfg.capture(), argProgress.capture(), argTarFile.capture(), argRetrievedChunks.capture());
        
        doNothing().when(retrieve).doRetrieveFromWorkerToUserFs(eq(mContext), argUserStoreInfo.capture(), argTarFile.capture(), argProgress.capture());

        retrieve.performAction(mContext);

        //STAGE 0 - check user store free space - SKIPPED
        {
            verify(retrieve, never()).checkUserStoreFreeSpaceAndPermissions(any(UserStoreInfo.class));
        }

        //STAGE 1 - download from arhive store, decrypt and re-compose - NOT SKIPPED because of bad-hash
        {
            ArchiveDeviceInfo archiveDeviceInfo = argArchiveDeviceInfg.getAllValues().get(0);
            Progress progress = argProgress.getAllValues().get(0);
            File tarFile = argTarFile.getAllValues().get(0);
            RetrievedChunks retrievedChunks = argRetrievedChunks.getAllValues().get(0);
            
            verify(retrieve).retrieveChunksAndRecompose(mContext, archiveDeviceInfo, progress, tarFile, retrievedChunks);
        }
        
        //STAGE 2 - upload to user store
        {
            UserStoreInfo actualUserStoreInfo2 = argUserStoreInfo.getAllValues().get(0);
            File actualTarFile2 = argTarFile.getAllValues().get(1);
            Progress actualProgress2 = argProgress.getAllValues().get(1);
            verify(retrieve).doRetrieveFromWorkerToUserFs(mContext, actualUserStoreInfo2, actualTarFile2, actualProgress2);
        }

        var eventClasses = sentEvents.stream().map(evt -> evt.eventClass).toList();
        assertThat(eventClasses).isEqualTo(List.of(
                //TODO : might need to check the InitStates and RetrieveStart are not sent again ?
                "org.datavaultplatform.common.event.InitStates",
                "org.datavaultplatform.common.event.retrieve.RetrieveStart",

                "org.datavaultplatform.common.event.UpdateProgress",
                "org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedAll",
                "org.datavaultplatform.common.event.retrieve.RetrieveComplete"
        ));
    }

    @Order(40)
    @Test
    void testLastEventIsUploadedToUserStore() throws Exception {

        File tarFile = tempPath.resolve(TEST_BAG_ID+".tar").toFile();
        FileUtils.copyURLToFile(testBagIdTar.getURL(), tarFile);
        
        assertThat(tarFile).exists();
        assertThat(tarFile).isFile();
        assertThat(tarFile).canRead();
        assertThat(tarFile).hasSize(3584);
        
        Retrieve retrieve = Mockito.spy(getRetrieve(new UploadedToUserStore()));
        retrieve.performAction(mContext);

        //STAGE 0 - check user store free space - SKIPPED
        {
            verify(retrieve, never()).checkUserStoreFreeSpaceAndPermissions(any(UserStoreInfo.class));
        }

        //STAGE 1 - download from arhive store, decrypt and re-compose - SKIPPED
        {
            verify(retrieve, never()).retrieveChunksAndRecompose(any(Context.class), any(ArchiveDeviceInfo.class), any(Progress.class), any(File.class), any(RetrievedChunks.class));
        }
        
        //STAGE 2 - copy files to user fs - SKIPPED
        {
            verify(retrieve, never()).copyFilesToUserFs(any(Progress.class),any(File.class),any(UserStoreInfo.class), any(Long.class));
        }

        var eventClasses = sentEvents.stream()
                .filter(e -> !(e instanceof UpdateProgress))
                .map(evt -> evt.eventClass).toList();
        assertThat(eventClasses).isEqualTo(List.of(
                //TODO : might need to check the InitStates and RetrieveStart are not sent again ?
                "org.datavaultplatform.common.event.InitStates",
                "org.datavaultplatform.common.event.retrieve.RetrieveStart",
                "org.datavaultplatform.common.event.retrieve.RetrieveComplete"
        ));
    }
}
