package org.datavaultplatform.worker.operations;

import lombok.SneakyThrows;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChunkRetrieveTrackerTest {

    private static final byte[] TEST_BYTE_ARRAY = new byte[0];
    private static final int TEST_CHUNK_NUMBER = 5;
    private static final String LABEL_RET_CHUNK = "ret-chunk";
    private static final String LABEL_ENC_RET_CHUNK = "ret-enc-chunk";
    private static final String TEST_LOCATION = "test-location";
    private static final String TEST_ARCHIVE_ID = "test-archive-id";
    private static final String TEST_ARCHIVE_ID_WITH_CHUNK = "test-archive-id.5";

    public static final String TEST_CHUNKS_DIGEST = "TEST-CHUNKS-DIGEST";
    public static final String TEST_ENC_CHUNKS_DIGEST = "TEST-ENC-CHUNKS-DIGEST";

    @Mock
    private Device mDevice;

    @Mock
    private Context mContext;

    File chunkFile;

    @BeforeEach
    @SneakyThrows
    void setup() {
        chunkFile = File.createTempFile("chunkFile", ".tar.5");
    }

    @Captor
    ArgumentCaptor<String> argArchiveId;

    @Captor
    ArgumentCaptor<File> argChunkFile;

    @Captor
    ArgumentCaptor<Progress> argProgress;

    @Captor
    ArgumentCaptor<List<String>> argLocations;

    @Test
    void testRetrieveSingleCopyNotEncrypted() {
        checkRetrieve(false, false);
    }

    @Test
    void testRetrieveSingleCopyEncrypted() {
        checkRetrieve(false, true);
    }

    @Test
    void testRetrieveMultiCopyNotEncrypted() {
        checkRetrieve(true, false);
    }

    @Test
    void testRetrieveMultiCopyEncrypted() {
        checkRetrieve(true, true);
    }

    @Captor
    ArgumentCaptor<String> argHashLabel;
    @Captor
    ArgumentCaptor<File> argHashFile;
    @Captor
    ArgumentCaptor<String> argHashExpected;

    @SneakyThrows
    void checkRetrieve(boolean isMulti, boolean isEncrypted) {
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class)) {
            try (MockedStatic<Encryption> mockEncryption = Mockito.mockStatic(Encryption.class)) {
                mockUtils.when(() ->
                        Utils.checkFileHash(argHashLabel.capture(), argHashFile.capture(), argHashExpected.capture())
                ).thenAnswer((Answer<String>) invocation -> {
                    String label = invocation.getArgument(0);
                    if (LABEL_RET_CHUNK.equals(label)) {
                        return TEST_CHUNKS_DIGEST;
                    } else {
                        return TEST_ENC_CHUNKS_DIGEST;
                    }
                });
                Map<Integer, byte[]> ivs = new HashMap<>();

                Progress progress = new Progress();
                Map<Integer, String> chunksDigest = new HashMap<>();
                chunksDigest.put(TEST_CHUNK_NUMBER, TEST_CHUNKS_DIGEST);
                Map<Integer, String> encChunksDigest = new HashMap<>();
                if (isEncrypted) {
                    ivs.put(TEST_CHUNK_NUMBER, TEST_BYTE_ARRAY);
                    encChunksDigest.put(TEST_CHUNK_NUMBER, TEST_ENC_CHUNKS_DIGEST);
                }
                if (isMulti) {
                    Mockito.doNothing().when(mDevice).retrieve(argArchiveId.capture(), argChunkFile.capture(), argProgress.capture(), argLocations.capture());
                } else {
                    Mockito.doNothing().when(mDevice).retrieve(argArchiveId.capture(), argChunkFile.capture(), argProgress.capture());
                }

                ChunkRetrieveTracker retriever = new ChunkRetrieveTracker(
                        TEST_ARCHIVE_ID, mDevice, mContext, TEST_CHUNK_NUMBER,
                        ivs, Arrays.asList(TEST_LOCATION), isMulti, progress, chunksDigest, encChunksDigest,
                        chunkFile);
                File result = retriever.call();
                assertThat(result).isEqualTo(chunkFile);

                if (isMulti) {
                    assertThat(argArchiveId.getValue()).isEqualTo(TEST_ARCHIVE_ID_WITH_CHUNK);
                    assertThat(argChunkFile.getValue()).isEqualTo(chunkFile);
                    assertThat(argProgress.getValue()).isEqualTo(progress);
                    assertThat(argLocations.getValue()).isEqualTo(Arrays.asList(TEST_LOCATION));

                    Mockito.verify(mDevice).retrieve(TEST_ARCHIVE_ID_WITH_CHUNK, chunkFile, progress, Arrays.asList(TEST_LOCATION));
                } else {
                    assertThat(argArchiveId.getValue()).isEqualTo(TEST_ARCHIVE_ID_WITH_CHUNK);
                    assertThat(argChunkFile.getValue()).isEqualTo(chunkFile);
                    assertThat(argProgress.getValue()).isEqualTo(progress);
                    Mockito.verify(mDevice).retrieve(TEST_ARCHIVE_ID_WITH_CHUNK, chunkFile, progress);
                }
                InOrder inOrderUtils = Mockito.inOrder(Utils.class);

                if (isEncrypted) {
                    inOrderUtils.verify(mockUtils, () ->
                            Utils.checkFileHash(LABEL_ENC_RET_CHUNK, chunkFile, TEST_ENC_CHUNKS_DIGEST));

                    mockEncryption.verify(() -> Encryption.decryptFile(mContext, chunkFile, TEST_BYTE_ARRAY));
                }

                inOrderUtils.verify(mockUtils, () ->
                        Utils.checkFileHash(LABEL_RET_CHUNK, chunkFile, TEST_CHUNKS_DIGEST));

                Mockito.verifyNoMoreInteractions(mDevice);
            }
        }
    }
}