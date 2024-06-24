package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.util.RetrievedChunks;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.Utils;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveChunkInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils;
import org.datavaultplatform.worker.tasks.retrieve.UserStoreInfo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class RetrieveRestartChunksTest {

    static final String TEST_BAG_ID_TAR_HASH = "C8E8A4EEBD02DD89787CEA613D12CC297F329584";

    static final int NUM_CHUNKS = 5;
    static final String TEST_ARCHIVE_ID = "test-archive-id";
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
    ArgumentCaptor<Progress> argProgress;
    @Captor
    ArgumentCaptor<File> argTarFile;

    ArchiveStore archiveStore;

    List<Event> sentEvents;

    final ClassPathResource testBagIdTar = new ClassPathResource("retrieve/test-bag-id.tar");

    @Captor
    ArgumentCaptor<File[]> argFileArray;

    @Mock
    Device mArchiveDevice;

    @Mock
    Device mUserDevice;

    @Captor
    ArgumentCaptor<String> argRetArchiveId;

    @Captor
    ArgumentCaptor<File> argRetChunkFile;

    @Captor
    ArgumentCaptor<Progress> argRetProgress;

    @Captor
    ArgumentCaptor<RetrieveChunkInfo> argChunkInfo;
    
    @Mock
    RetrievedChunkFileChecker mChunkFileChecker;

    static Stream<Arguments> retrievedSingleChunkPreviouslySource() {
        return Stream.of(
                Arguments.of(1, Set.of(2, 3, 4, 5)),
                Arguments.of(2, Set.of(3, 4, 5, 1)),
                Arguments.of(3, Set.of(4, 5, 1, 2)),
                Arguments.of(4, Set.of(5, 1, 2, 3)),
                Arguments.of(5, Set.of(1, 2, 3, 4))
        );
    }

    @SneakyThrows
    HashMap<String, String> getProperties(String archiveDigest, Set<Integer> retrievedPreviously) {
        var props = new HashMap<String, String>();
        props.put(PropNames.ARCHIVE_ID, TEST_ARCHIVE_ID);
        props.put(PropNames.DEPOSIT_ID, TEST_DEPOSIT_ID);
        props.put(PropNames.BAG_ID, TEST_BAG_ID);
        props.put(PropNames.USER_ID, TEST_USER_ID);
        RetrievedChunks rc = new RetrievedChunks();
        retrievedPreviously.forEach(rc::addRetrievedChunk);
        props.put(PropNames.DEPOSIT_CHUNKS_RETRIEVED, RetrievedChunks.toJson(rc));
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

        ArchiveStore tempArchiveStore = new ArchiveStore();
        tempArchiveStore.setStorageClass(TivoliStorageManager.class.getName());
        tempArchiveStore.setRetrieveEnabled(true);
        tempArchiveStore.setProperties(new HashMap<>());
        tempArchiveStore.setLabel("TSM");

        archiveStore = Mockito.spy(tempArchiveStore);
    }

    Retrieve getRetrieve(Event lastEvent, String archiveDigest, Set<Integer> retrievedPreviously) {
        Retrieve result = new Retrieve();
        result.setLastEvent(lastEvent);
        result.setJobID(TEST_JOB_ID);
        result.setIsRedeliver(false);
        result.setProperties(getProperties(archiveDigest, retrievedPreviously));
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
    @Test
    void testRetrieveChunksNonPreviously() throws Exception {
        checkRetrieveChunks(Collections.emptySet(), Collections.emptySet(), Set.of(1, 2, 3, 4, 5));
    }

    @Order(20)
    @ParameterizedTest
    @MethodSource("retrievedSingleChunkPreviouslySource")
    void testRetrievedSingleChunkPreviously(Integer chunkNumber, Set<Integer> retrieveExpected) throws Exception {
        checkRetrieveChunks(Collections.singleton(chunkNumber), Collections.singleton(chunkNumber), retrieveExpected);
    }

    @Order(30)
    @Test
    void testRetrievedPreviously1and3and5() throws Exception {
        checkRetrieveChunks(Set.of(1, 3, 5), Set.of(1, 3, 5), Set.of(2, 4));
    }

    @Order(40)
    @Test
    void testRetrievedPreviously1and3and5butBadFiles() throws Exception {
        checkRetrieveChunks(Set.of(1, 3, 5), Set.of(3), Set.of(1, 2, 4, 5));
    }

    @Order(50)
    @Test
    void testRetrievedPreviously2and4() throws Exception {
        checkRetrieveChunks(Set.of(2, 4), Set.of(2, 4), Set.of(1, 3, 5));
    }

    @Order(60)
    @Test
    void testRetrievedPreviously2and4butBadFiles() throws Exception {
        checkRetrieveChunks(Set.of(2, 4), Collections.emptySet(), Set.of(1, 2, 3, 4, 5));
    }

    void checkRetrieveChunks(Set<Integer> retrievedPreviously, Set<Integer> chunkFiles, Set<Integer> expectedRetrievedChunks) throws Exception {

        Event lastEvent = new ArchiveStoreRetrievedChunk(TEST_JOB_ID, TEST_DEPOSIT_ID, TEST_RETRIEVE_ID,
                -1);
        Retrieve temp = getRetrieve(lastEvent, TEST_RETRIEVE_ID, retrievedPreviously);
        Map<String, String> props = temp.getProperties();

        Map<Integer, String> chunksDigestMap = new HashMap<>();
        Map<Integer, String> chunksEncDigestMap = new HashMap<>();
        Map<Integer, byte[]> chunksIVMap = new HashMap<>();

        for (int chunkNumber = 1; chunkNumber <= NUM_CHUNKS; chunkNumber++) {
            chunksEncDigestMap.put(chunkNumber, "enc-digest-" + chunkNumber);
            chunksIVMap.put(chunkNumber, ("iv-" + chunkNumber).getBytes(StandardCharsets.UTF_8));

            File tarChunkFile = Files.createFile(tempPath.resolve(TEST_BAG_ID + ".tar." + chunkNumber)).toFile();
            String digest;
            if (chunkFiles.contains(chunkNumber)) {
                digest = Verify.getDigest(tarChunkFile);
            } else {
                digest = "bad-digest";
            }
            chunksDigestMap.put(chunkNumber, digest);
        }

        props.put(PropNames.NUM_OF_CHUNKS, String.valueOf(NUM_CHUNKS));
        temp.setChunksIVs(chunksIVMap);
        temp.setChunkFilesDigest(chunksDigestMap);
        temp.setEncChunksDigest(chunksEncDigestMap);

        try (MockedStatic<RetrieveUtils> mRetrievedUtils = mockStatic(RetrieveUtils.class)) {

            mRetrievedUtils.when(() -> RetrieveUtils.getTarFileName(TEST_BAG_ID)).thenReturn(TEST_BAG_ID + ".tar");
            mRetrievedUtils.when(() -> RetrieveUtils.createDevice(any(String.class), any(Map.class), any(StorageClassNameResolver.class))).thenAnswer(
                    invocation -> {
                        String storageClassName = (String) invocation.getArguments()[0];
                        if (storageClassName.equals(LocalFileSystem.class.getName())) {
                            return mUserDevice;
                        } else {
                            return mArchiveDevice;
                        }
                    }
            );

            Retrieve retrieve = Mockito.spy(temp);
            doReturn(mChunkFileChecker).when(retrieve).getChunkFileChecker();

            assertThat(mContext.getTempDir()).isNotNull();

            when(mArchiveDevice.hasMultipleCopies()).thenReturn(false);

            doNothing().when(mArchiveDevice).retrieve(argRetArchiveId.capture(), argRetChunkFile.capture(), argRetProgress.capture());

            doNothing().when(mChunkFileChecker).decryptAndCheckTarFile(eq(mContext), argChunkInfo.capture());

            doNothing().when(retrieve).doRecomposeUsingCorrectVersion(eq(mContext), argFileArray.capture(), argTarFile.capture());

            doNothing().when(retrieve).doRetrieveFromWorkerToUserFs(eq(mContext), argUserStoreInfo.capture(), argTarFile.capture(), argProgress.capture());

            retrieve.performAction(mContext);

            //STAGE 0 - check user store free space - SKIPPED due to event
            {
                verify(retrieve, never()).checkUserStoreFreeSpaceAndPermissions(any(UserStoreInfo.class));
            }

            //STAGE 1 - we have to retrieve some chunks !!!!
            {
                assertThat(argChunkInfo.getAllValues().size()).isEqualTo(expectedRetrievedChunks.size());
                assertThat(argRetChunkFile.getAllValues().size()).isEqualTo(expectedRetrievedChunks.size());
                assertThat(argRetArchiveId.getAllValues().size()).isEqualTo(expectedRetrievedChunks.size());
                assertThat(argRetChunkFile.getAllValues().size()).isEqualTo(expectedRetrievedChunks.size());

                Set<Integer> actualRetrievedChunks = new HashSet<>();
                for (int i = 0; i < expectedRetrievedChunks.size(); i++) {
                    RetrieveChunkInfo chunkInfo = argChunkInfo.getAllValues().get(i);
                    int actualChunkNumber = chunkInfo.chunkNumber();
                    actualRetrievedChunks.add(actualChunkNumber);
                    checkChunk(chunkInfo, actualChunkNumber, chunksDigestMap.get(actualChunkNumber));
                    verify(mChunkFileChecker).decryptAndCheckTarFile(mContext, chunkInfo);

                    String actualRetArchiveId = argRetArchiveId.getAllValues().get(i);
                    File actualRetChunkFile = argRetChunkFile.getAllValues().get(i);
                    Progress actualRetProgress = argRetProgress.getAllValues().get(i);
           
                    assertThat(actualRetArchiveId).isEqualTo(TEST_ARCHIVE_ID + "." + actualChunkNumber);
                    verify(mArchiveDevice).retrieve(actualRetArchiveId, actualRetChunkFile, actualRetProgress);
                }
                assertThat(actualRetrievedChunks).isEqualTo(expectedRetrievedChunks);
            }

            File[] actualFileArray = argFileArray.getValue();
            assertThat(actualFileArray.length).isEqualTo(5);
            assertThat(actualFileArray[0]).isEqualTo(tempPath.resolve(TEST_BAG_ID + ".tar.1").toFile());
            assertThat(actualFileArray[1]).isEqualTo(tempPath.resolve(TEST_BAG_ID + ".tar.2").toFile());
            assertThat(actualFileArray[2]).isEqualTo(tempPath.resolve(TEST_BAG_ID + ".tar.3").toFile());
            assertThat(actualFileArray[3]).isEqualTo(tempPath.resolve(TEST_BAG_ID + ".tar.4").toFile());
            assertThat(actualFileArray[4]).isEqualTo(tempPath.resolve(TEST_BAG_ID + ".tar.5").toFile());
            
            //STAGE 3 - recompose chunk files - skipped because of mocking
            verify(retrieve).doRecomposeUsingCorrectVersion(eq(mContext), eq(actualFileArray), argTarFile.capture());

            //STAGE 4 - store bag to userFS - skipped because of mocking
            UserStoreInfo actualUserStoreInfo = argUserStoreInfo.getValue();
            assertThat(actualUserStoreInfo.userFs()).isEqualTo(mUserDevice);
            File actualTarFile = argTarFile.getValue();
            assertThat(actualTarFile.toPath()).isEqualTo(tempPath.resolve(TEST_BAG_ID+".tar"));
            Progress actualProgress = argProgress.getValue();
            verify(retrieve).doRetrieveFromWorkerToUserFs(mContext, actualUserStoreInfo, actualTarFile, actualProgress);

            verifyNoMoreInteractions(mChunkFileChecker, mArchiveDevice, mUserDevice);
            
            assertThat(sentEvents.size()).isEqualTo(5 + expectedRetrievedChunks.size());

            assertThat(sentEvents.get(0)).isInstanceOf(InitStates.class);
            assertThat(sentEvents.get(1)).isInstanceOf(RetrieveStart.class);
            assertThat(sentEvents.get(2)).isInstanceOf(UpdateProgress.class);

            int afterChunkEvents = 3 + expectedRetrievedChunks.size();
            assertThat(sentEvents.get(afterChunkEvents)).isInstanceOf(ArchiveStoreRetrievedAll.class);
            assertThat(sentEvents.get(afterChunkEvents+1)).isInstanceOf(RetrieveComplete.class);

            Set<Integer> eventChunkNumbers = sentEvents.stream()
                    .filter(ArchiveStoreRetrievedChunk.class::isInstance)
                    .map(ArchiveStoreRetrievedChunk.class::cast)
                    .map(ArchiveStoreRetrievedChunk::getChunkNumber)
                    .collect(Collectors.toSet());

            assertThat(eventChunkNumbers).isEqualTo(expectedRetrievedChunks);
        }
    }

    private void checkChunk(RetrieveChunkInfo chunkInfo, int chunkNumber, String expectedDigest) {
        assertThat(chunkInfo.chunkNumber()).isEqualTo(chunkNumber);

        assertThat(chunkInfo.chunkDigest()).isEqualTo(expectedDigest);

        assertThat(chunkInfo.encChunkDigest()).isEqualTo("enc-digest-" + chunkNumber);

        assertThat(chunkInfo.iv()).isEqualTo(("iv-" + chunkNumber).getBytes(StandardCharsets.UTF_8));

        assertThat(chunkInfo.chunkFile().toPath()).isEqualTo(tempPath.resolve(TEST_BAG_ID + ".tar." + chunkNumber));
    }

}
