package org.datavaultplatform.worker.tasks.deposit;

import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.Tar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositUtilsTest {

    @Nested
    class FinalEventTests {

        @Test
        void testNoChunkingNoEncryption() {
            check(false, false, ComputedDigest.class);
        }

        @Test
        void testChunkingNoEncryption() {
            check(true, false, ComputedChunks.class);
        }

        @Test
        void testNoChunkingWithEncryption() {
            check(false, true, ComputedEncryption.class);
        }

        @Test
        void testChunkingWithEncryption() {
            check(true, true, ComputedEncryption.class);
        }


        void check(boolean isChunkingEnabled, boolean isEncryptionEnabled, Class<? extends Event> expectedLastEventClass) {
            assertThat(DepositUtils.getFinalPackageEvent(isChunkingEnabled, isEncryptionEnabled).getName()).isEqualTo(expectedLastEventClass.getName());
        }

    }

    @Nested
    class GetChunkTarFileTests {

        @Mock
        Context mContext;

        Path tmpPath = Path.of("/tmp");

        @Test
        void testInvalidContext() {
            checkIllegal(null, "bag-id", 1, "The context cannot be null");
        }

        @Test
        void testNullBagId() {
            checkIllegal(mContext, null, 1, "The bagID cannot be blank");
        }

        @Test
        void testEmptyBagId() {
            checkIllegal(mContext, " ", 1, "The bagID cannot be blank");
        }

        @Test
        void testInvalidTmpDir() {
            checkIllegal(mContext, "bag-id", 1, "The tempDir cannot be null");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0})
        void testInvalidChunkNumber(int chunkNumber) {
            when(mContext.getTempDir()).thenReturn(tmpPath);
            checkIllegal(mContext, "bag-id", chunkNumber, "The chunkNumber must be greater than 0");
        }

        private void checkIllegal(Context context, String bagID, int chunkNumber, String expectedMessage) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.getChunkTarFile(context, bagID, chunkNumber);
            });
            assertThat(ex).hasMessage(expectedMessage);
        }

        @Test
        void testGetChunkTarFile1() {
            when(mContext.getTempDir()).thenReturn(tmpPath);
            File result = DepositUtils.getChunkTarFile(mContext, "bag-id", 1);
            assertThat(result.toPath()).isEqualTo(Path.of("/tmp/bag-id.tar.1"));
        }
    }

    @Nested
    class GetTarFileTests {

        @Mock
        Context mContext;

        Path tmpPath = Path.of("/tmp");

        @Test
        void testInvalidContext() {
            checkIllegal(null, "bag-id", "The context cannot be null");
        }

        @Test
        void testNullBagId() {
            checkIllegal(mContext, null, "The bagID cannot be blank");
        }

        @Test
        void testEmptyBagId() {
            checkIllegal(mContext, " ", "The bagID cannot be blank");
        }

        @Test
        void testInvalidTmpDir() {
            checkIllegal(mContext, "bag-id", "The tempDir cannot be null");
        }

        private void checkIllegal(Context context, String bagID, String expectedMessage) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.getTarFile(context, bagID);
            });
            assertThat(ex).hasMessage(expectedMessage);
        }

        @Test
        void testGetTarFile() {
            when(mContext.getTempDir()).thenReturn(tmpPath);
            File result = DepositUtils.getTarFile(mContext, "bag-id");
            assertThat(result.toPath()).isEqualTo(Path.of("/tmp/bag-id.tar"));
        }
    }

    @Nested
    class GetFinalPackageEventTests {
        static Stream<Arguments> finalPackageEventProvider() {
            return Stream.of(
                    Arguments.of(false, false, ComputedDigest.class),
                    Arguments.of(true, false, ComputedChunks.class),
                    Arguments.of(true, true, ComputedEncryption.class),
                    Arguments.of(false, true, ComputedEncryption.class)
            );
        }

        @ParameterizedTest
        @MethodSource("finalPackageEventProvider")
        void testGetFinalPackageEvent(boolean isChunkingEnabled, boolean isEncryptionEnabled, Class<? extends Event> expectedLastEventClass) {
            assertThat(DepositUtils.getFinalPackageEvent(isChunkingEnabled, isEncryptionEnabled)).isEqualTo(expectedLastEventClass);
        }
    }

    @Nested
    class GetNumberOfChunksTests {

        static Stream<Arguments> numberOfChunksProvider() {
            return Stream.of(
                    Arguments.of("-1", -1),
                    Arguments.of("0", 0),
                    Arguments.of("1", 1),
                    Arguments.of("2112", 2112)
            );
        }

        @Test
        void testNullProperties() {
            assertThat(DepositUtils.getNumberOfChunks(null)).isEqualTo(0);
        }

        @Test
        void testEmptyProperties() {
            assertThat(DepositUtils.getNumberOfChunks(Collections.emptyMap())).isEqualTo(0);
        }

        @Test
        void testNonInteger() {
            assertThat(DepositUtils.getNumberOfChunks(Map.of(PropNames.NUM_OF_CHUNKS, "blah"))).isEqualTo(0);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        void testBlankProperty(String blank) {
            var props = Map.of(PropNames.NUM_OF_CHUNKS, blank);
            assertThat(DepositUtils.getNumberOfChunks(props)).isEqualTo(0);
        }

        @ParameterizedTest
        @MethodSource("numberOfChunksProvider")
        void testGetNumberOfChunks(String numberOfChunksStr, int expectedNumberOfChunks) {
            var props = Map.of(PropNames.NUM_OF_CHUNKS, numberOfChunksStr);
            assertThat(DepositUtils.getNumberOfChunks(props)).isEqualTo(expectedNumberOfChunks);
        }
    }

    @Nested
    class CreateTarTests {

        File bagDir;

        @Mock
        Context mContext;

        @BeforeEach
        void setup() {
            bagDir = Paths.get("/tmp/bagDir").toFile();
        }

        @Test
        void testNullContext() {
            checkIllegal(null, "bagID", bagDir, "The context cannot be null");
        }

        @Test
        void testNullBagDir() {
            checkIllegal(mContext, "bagID", null, "The bagDir cannot be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @NullSource
        void testBlankNullBagDir(String blank) {
            checkIllegal(mContext, blank, bagDir, "The bagID cannot be blank");
        }

        void checkIllegal(Context context, String bagID, File bagDir, String expectedMessage) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.createTar(context, bagID, bagDir);
            });
            assertThat(ex).hasMessage(expectedMessage);
        }

        @Test
        @SneakyThrows
        void testCreateTar() {
            when(mContext.getTempDir()).thenReturn(Path.of("/tmp"));
            try (MockedStatic<Tar> mockedStatic = Mockito.mockStatic(Tar.class)) {

                File tarFile = DepositUtils.createTar(mContext, "bagID", bagDir);
                assertThat(tarFile.toPath()).isEqualTo(Path.of("/tmp/bagID.tar"));

                mockedStatic.verify(() -> {
                    try {
                        Tar.createTar(bagDir, Paths.get("/tmp/bagID.tar").toFile());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        }
    }

    @Nested
    class InitialiseLoggingTests {


        @Mock
        Context mContext;

        @Test
        void testInitialiseLoggingNullContext() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.initialLogging(null);
            });
            assertThat(ex).hasMessage("The context cannot be null");
        }

        @Test
        void testInitialiseLoggingNonNullContext() {

            when(mContext.isChunkingEnabled()).thenReturn(true);
            when(mContext.getChunkingByteSize()).thenReturn(2112L);
            when(mContext.isEncryptionEnabled()).thenReturn(true);
            when(mContext.getEncryptionMode()).thenReturn(Context.AESMode.GCM);
            when(mContext.isMultipleValidationEnabled()).thenReturn(true);

            DepositUtils.initialLogging(mContext);

            verify(mContext).isChunkingEnabled();
            verify(mContext).getChunkingByteSize();
            verify(mContext).isEncryptionEnabled();
            verify(mContext).getEncryptionMode();
            verify(mContext).isMultipleValidationEnabled();

            verifyNoMoreInteractions(mContext);
        }

    }

    @Nested
    class CreatedDirTests {

        @Test
        void testNullPath() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.createDir(null);
            });
            assertThat(ex).hasMessage("The path cannot be null");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void testNonNullPath(boolean mkdirsResult) {

            Path mPath = Mockito.mock(Path.class);
            File mDir = Mockito.mock(File.class);

            when(mPath.toFile()).thenReturn(mDir);
            doReturn(mkdirsResult).when(mDir).mkdirs();

            assertThat(DepositUtils.createDir(mPath)).isEqualTo(mDir);

            verify(mPath).toFile();
            verify(mDir).mkdirs();
            verifyNoMoreInteractions(mPath, mDir);
        }
    }

    @Nested
    class SetupArchiveStoresTests {

        @Mock
        StorageClassNameResolver mResolver;

        @Mock
        org.datavaultplatform.common.model.ArchiveStore mArchiveFileStore1;

        @Mock
        org.datavaultplatform.common.model.ArchiveStore mArchiveFileStore2;

        @Mock
        org.datavaultplatform.common.model.ArchiveStore mArchiveFileStore3;
        @Mock
        HashMap<String, String> mProperties1;
        @Mock
        HashMap<String, String> mProperties2;
        @Mock
        HashMap<String, String> mProperties3;

        @Mock
        ArchiveStore mArchiveStore1;

        @Mock
        ArchiveStore mArchiveStore2;

        @Mock
        ArchiveStore mArchiveStore3;

        @Test
        void testNullResolver() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.setupArchiveStores(null, Collections.emptyList());
            });
            assertThat(ex).hasMessage("The resolver cannot be null");
        }

        @Test
        void testNullArchiveStores() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.setupArchiveStores(mResolver, null);
            });
            assertThat(ex).hasMessage("The archiveFileStores cannot be null");
        }

        @Test
        void testSetupArchiveStores() {

            try (MockedStatic<StorageClassUtils> mockedStorageClassUtils = Mockito.mockStatic(StorageClassUtils.class)) {

                when(mArchiveFileStore1.getProperties()).thenReturn(mProperties1);
                when(mArchiveFileStore2.getProperties()).thenReturn(mProperties2);
                when(mArchiveFileStore3.getProperties()).thenReturn(mProperties3);
                when(mArchiveFileStore1.getStorageClass()).thenReturn("storageClass1");
                when(mArchiveFileStore2.getStorageClass()).thenReturn("storageClass2");
                when(mArchiveFileStore3.getStorageClass()).thenReturn("storageClass3");
                when(mArchiveFileStore1.getID()).thenReturn("archiveStoreId1");
                when(mArchiveFileStore2.getID()).thenReturn("archiveStoreId2");
                when(mArchiveFileStore3.getID()).thenReturn("archiveStoreId3");
                var archiveFileStores = List.of(mArchiveFileStore1, mArchiveFileStore2, mArchiveFileStore3);

                mockedStorageClassUtils.when(() -> StorageClassUtils.createStorage(
                        any(String.class), //storageClassName
                        any(HashMap.class), //storageClassProperties
                        any(Class.class), //storageClass
                        any(StorageClassNameResolver.class))).thenAnswer((Answer<ArchiveStore>) invocation -> {
                    String storageClass = invocation.getArgument(0, String.class);
                    ArchiveStore result1 = switch (storageClass) {
                        case "storageClass1" -> mArchiveStore1;
                        case "storageClass2" -> mArchiveStore2;
                        case "storageClass3" -> mArchiveStore3;
                        default -> mArchiveStore3;
                    };
                    return result1;

                });

                HashMap<String, ArchiveStore> result = DepositUtils.setupArchiveStores(mResolver, archiveFileStores);

                mockedStorageClassUtils.verify(() -> StorageClassUtils.createStorage(any(String.class), any(HashMap.class), eq(ArchiveStore.class), eq(mResolver)), times(3));
                
                assertThat(result).hasSize(3);
                assertThat(result.get("archiveStoreId1")).isEqualTo(mArchiveStore1);
                assertThat(result.get("archiveStoreId2")).isEqualTo(mArchiveStore2);
                assertThat(result.get("archiveStoreId3")).isEqualTo(mArchiveStore3);

            }


        }

        @Test
        void testSetupArchiveStoresProblem() {

            try (MockedStatic<StorageClassUtils> mockedStorageClassUtils = Mockito.mockStatic(StorageClassUtils.class)) {

                lenient().when(mArchiveFileStore1.getProperties()).thenReturn(mProperties1);
                lenient().when(mArchiveFileStore1.getStorageClass()).thenReturn("storageClass1");
                lenient().when(mArchiveFileStore1.getID()).thenReturn("archiveStoreId1");
                var archiveFileStores = List.of(mArchiveFileStore1, mArchiveFileStore2, mArchiveFileStore3);

                mockedStorageClassUtils.when(() -> StorageClassUtils.createStorage(
                        any(String.class), //storageClassName
                        any(HashMap.class), //storageClassProperties
                        any(Class.class), //storageClass
                        any(StorageClassNameResolver.class))).thenAnswer((Answer<ArchiveStore>) invocation -> {

                    throw new RuntimeException("oops");

                });

                var rte = assertThrows(RuntimeException.class, () -> {
                    DepositUtils.setupArchiveStores(mResolver, archiveFileStores);
                });

                mockedStorageClassUtils.verify(() -> StorageClassUtils.createStorage("storageClass1", mProperties1, ArchiveStore.class, mResolver));
                assertThat(rte).hasMessage("Deposit failed: could not access ArchiveStore filesystem : storageClass1");
            }
        }
    }

    @Nested
    class SetupUserFileStoresTests {

        @Mock
        UserStore mUserStore1;
        @Mock
        UserStore mUserStore2;
        @Mock
        UserStore mUserStore3;

        @Mock
        StorageClassNameResolver mResolver;

        HashMap<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
        @Mock
        HashMap<String, String> mProps1;
        @Mock
        HashMap<String, String> mProps2;
        @Mock
        HashMap<String, String> mProps3;

        HashMap<String, String> properties = new HashMap<>();

        @Test
        void testNullResolver() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.setupUserFileStores(null, Collections.emptyMap(), Collections.emptyMap());
            });
            assertThat(ex).hasMessage("The resolver cannot be null");
        }

        @Test
        void testNullUserFileStoreProperties() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.setupUserFileStores(mResolver, null, Collections.emptyMap());
            });
            assertThat(ex).hasMessage("The userFileStoreProperties cannot be null");
        }

        @Test
        void testNullUserFileStoreClasses() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                DepositUtils.setupUserFileStores(mResolver, Collections.emptyMap(), null);
            });
            assertThat(ex).hasMessage("The userFileStoreClasses cannot be null");
        }

        @Test
        void testSetupUserFileStores() {

            try (MockedStatic<StorageClassUtils> mockedStorageClassUtils = Mockito.mockStatic(StorageClassUtils.class)) {

                properties.put("storageID1", "storageClass1");
                properties.put("storageID2", "storageClass2");
                properties.put("storageID3", "storageClass3");

                userFileStoreProperties.put("storageID1", mProps1);
                userFileStoreProperties.put("storageID2", mProps2);
                userFileStoreProperties.put("storageID3", mProps3);

                mockedStorageClassUtils.when(() -> StorageClassUtils.createStorage(
                        any(String.class), //storageClassName
                        any(Map.class), //storageClassProperties
                        any(Class.class), //storageClass
                        any(StorageClassNameResolver.class))).thenAnswer((Answer<UserStore>) invocation -> {

                    String pStorageClass = invocation.getArgument(0, String.class);
                    Map<String, String> pProperties = invocation.getArgument(1, Map.class);
                    Class pClazz = invocation.getArgument(2, Class.class);
                    assertThat(pClazz).isEqualTo(UserStore.class);
                    StorageClassNameResolver pResolver = invocation.getArgument(3, StorageClassNameResolver.class);
                    assertThat(pResolver).isEqualTo(mResolver);

                    UserStore result1 = switch (pStorageClass) {
                        case "storageClass1" -> {
                            assertThat(pProperties).isEqualTo(mProps1);
                            yield mUserStore1;
                        }
                        case "storageClass2" -> {
                            assertThat(pProperties).isEqualTo(mProps2);
                            yield mUserStore2;
                        }
                        case "storageClass3" -> {
                            assertThat(pProperties).isEqualTo(mProps3);
                            yield mUserStore3;
                        }
                        default -> mUserStore3;
                    };
                    return result1;

                });

                HashMap<String, UserStore> result = DepositUtils.setupUserFileStores(mResolver, userFileStoreProperties, properties);

                mockedStorageClassUtils.verify(() -> StorageClassUtils.createStorage(
                        any(String.class), any(HashMap.class), eq(UserStore.class), eq(mResolver)), times(3));
                
                assertThat(result).hasSize(3);
                assertThat(result.get("storageID1")).isEqualTo(mUserStore1);
                assertThat(result.get("storageID2")).isEqualTo(mUserStore2);
                assertThat(result.get("storageID3")).isEqualTo(mUserStore3);
            }

        }

        @Test
        void testSetupUserFileStoresProblem() {

            try (MockedStatic<StorageClassUtils> mockedStorageClassUtils = Mockito.mockStatic(StorageClassUtils.class)) {

                properties.put("storageID1", "storageClass1");
                properties.put("storageID2", "storageClass2");
                properties.put("storageID3", "storageClass3");

                userFileStoreProperties.put("storageID1", mProps1);
                userFileStoreProperties.put("storageID2", mProps2);
                userFileStoreProperties.put("storageID3", mProps3);

                mockedStorageClassUtils.when(() -> StorageClassUtils.createStorage(
                        any(String.class), //storageClassName
                        any(Map.class), //storageClassProperties
                        any(Class.class), //storageClass
                        any(StorageClassNameResolver.class))).thenAnswer((Answer<UserStore>) invocation -> {

                    String pStorageClass = invocation.getArgument(0, String.class);
                    Map<String, String> pProperties = invocation.getArgument(1, Map.class);
                    Class pClazz = invocation.getArgument(2, Class.class);
                    assertThat(pClazz).isEqualTo(UserStore.class);
                    StorageClassNameResolver pResolver = invocation.getArgument(3, StorageClassNameResolver.class);
                    assertThat(pResolver).isEqualTo(mResolver);

                    throw new RuntimeException("oops");

                });

                var rte = assertThrows(RuntimeException.class, () -> {
                   DepositUtils.setupUserFileStores(mResolver, userFileStoreProperties, properties);
                });
                mockedStorageClassUtils.verify(() -> StorageClassUtils.createStorage(
                        eq("storageClass1"), eq(mProps1), eq(UserStore.class), eq(mResolver)));

                assertThat(rte).hasMessage("Deposit failed: could not access UserStore filesystem : storageClass1");
            }
        }
    }

    @Nested
    class FileTests {

        @Test
        void testFilesExistFalse() {
            assertThat(DepositUtils.filesExist(List.of(Path.of("/bob")))).isFalse();
        }

        @Test
        @SneakyThrows
        void testFilesExistTrue() {
            Path temp1 = Files.createTempFile("tmp1", ".txt");
            Path temp2 = Files.createTempFile("tmp2", ".txt");
            assertThat(DepositUtils.filesExist(List.of(temp1, temp2))).isTrue();
        }
    }

    @Nested
    class DirectoryTests {

        @Test
        void testDirsExistFalse() {
            assertThat(DepositUtils.directoriesExist(List.of(Path.of("/tmp/tmp")))).isFalse();
        }

        @Test
        @SneakyThrows
        void testDirsExistTrue() {
            Path tempDir1 = Files.createTempDirectory("tmpDir1");
            Path tempDir2 = Files.createTempDirectory("tmpDir2");
            assertThat(DepositUtils.directoriesExist(List.of(tempDir1, tempDir2))).isTrue();
        }
    }
}