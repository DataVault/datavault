package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.retrieve.ArchiveDeviceInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveChunkInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChunkRetrieveTrackerTest {

    public static final int TEST_CHUNK_NUMBER=2112;
    public static final String TEST_CHUNK_DIGEST="test-chunk-digest";
    public static final String TEST_ENC_CHUNK_DIGEST="test-enc-chunk-digest";
    
    String archiveId = "TEST-ARCHIVE-ID";
    
    @Mock
    ArchiveDeviceInfo mArchiveDeviceInfo;
    
    @Mock
    Context mContext;
    
    @Mock
    Device mDevice;
    
    @Mock
    File mChunkFile;

    @Mock
    RetrieveChunkInfo mChunkInfo;

    byte[] testIV = new byte[0];
    
    Progress progress = new Progress();

    List<String> locations = new ArrayList<>();

    @BeforeEach
    void setup() {
        when(mArchiveDeviceInfo.archiveFs()).thenReturn(mDevice);

        when(mChunkInfo.chunkNumber()).thenReturn(TEST_CHUNK_NUMBER);
        when(mChunkInfo.chunkFile()).thenReturn(mChunkFile);
        when(mChunkInfo.chunkDigest()).thenReturn(TEST_CHUNK_DIGEST);
        when(mChunkInfo.encChunkDigest()).thenReturn(TEST_ENC_CHUNK_DIGEST);
        when(mChunkInfo.iv()).thenReturn(testIV);
        when(mArchiveDeviceInfo.locations()).thenReturn(locations);

    }
    
    @Captor
    ArgumentCaptor<String> argChunkArchiveId;
    
    @Test
    void testMultiCopy() throws Exception {
        
        try (MockedStatic<RetrieveUtils> mockUtils = Mockito.mockStatic(RetrieveUtils.class)) {
            
            when(mArchiveDeviceInfo.multiCopy()).thenReturn(true);

            doNothing().when(mDevice).retrieve(argChunkArchiveId.capture(), eq(mChunkFile), eq(progress), eq(locations));

            ChunkRetrieveTracker tracker = new ChunkRetrieveTracker(archiveId, mArchiveDeviceInfo, mContext, progress, mChunkInfo);

            File result = tracker.call();
            assertThat(result).isEqualTo(mChunkFile);

            String actualChunkArchiveId = argChunkArchiveId.getValue();
            assertThat(actualChunkArchiveId).isEqualTo(archiveId+"."+TEST_CHUNK_NUMBER);

            verify(mDevice).retrieve(actualChunkArchiveId, mChunkFile, progress,locations);
            
            mockUtils.verify(() -> RetrieveUtils.decryptAndCheckTarFile("chunk-2112", mContext, testIV, mChunkFile, TEST_ENC_CHUNK_DIGEST, TEST_CHUNK_DIGEST ));

            mockUtils.verifyNoMoreInteractions();
            verifyNoMoreInteractions(mDevice,mChunkInfo,mChunkFile,mArchiveDeviceInfo, mContext);
        }
    }
    @Test
    void testSingleCopy() throws Exception {

        try (MockedStatic<RetrieveUtils> mockUtils = Mockito.mockStatic(RetrieveUtils.class)) {

            when(mArchiveDeviceInfo.multiCopy()).thenReturn(false);

            doNothing().when(mDevice).retrieve(argChunkArchiveId.capture(), eq(mChunkFile), eq(progress));

            ChunkRetrieveTracker tracker = new ChunkRetrieveTracker(archiveId, mArchiveDeviceInfo, mContext, progress, mChunkInfo);

            File result = tracker.call();
            assertThat(result).isEqualTo(mChunkFile);

            String actualChunkArchiveId = argChunkArchiveId.getValue();
            assertThat(actualChunkArchiveId).isEqualTo(archiveId+"."+TEST_CHUNK_NUMBER);

            verify(mDevice).retrieve(actualChunkArchiveId, mChunkFile, progress);

            mockUtils.verify(() -> RetrieveUtils.decryptAndCheckTarFile("chunk-2112", mContext, testIV, mChunkFile, TEST_ENC_CHUNK_DIGEST, TEST_CHUNK_DIGEST ));

            mockUtils.verifyNoMoreInteractions();
            verifyNoMoreInteractions(mDevice,mChunkInfo,mChunkFile,mArchiveDeviceInfo,mContext);
        }
    }
}
