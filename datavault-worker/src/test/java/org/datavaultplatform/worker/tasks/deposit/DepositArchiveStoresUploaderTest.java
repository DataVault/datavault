package org.datavaultplatform.worker.tasks.deposit;

import lombok.SneakyThrows;
import org.assertj.core.util.Files;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.StoredChunks;
import org.datavaultplatform.worker.operations.ChunkUploadTracker;
import org.datavaultplatform.worker.operations.DeviceTracker;
import org.datavaultplatform.worker.tasks.PackageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositArchiveStoresUploaderTest {

    final String JOB_ID = "test-job-id";
    final String USER_ID = "test-user-id";
    final String DEPOSIT_ID = "test-deposit-id";
    final String BAG_ID = "test-bag-id";

    @Mock
    Context mContext;

    @Mock
    Event mLastEvent;

    @Mock
    UserEventSender mUserEventSender;

    DepositArchiveStoresUploader uploader;

    final Map<String, String> properties = new HashMap<>();

    ArchiveStoreContext archiveStoreContext;

    @Captor
    ArgumentCaptor<Integer> argNumberThreads;

    @Captor
    ArgumentCaptor<String> argErrorMessage;

    @SuppressWarnings("rawtypes")
    @Mock
    TaskExecutor mTaskExecutor;

    final String ARCHIVE_STORE_ID_1 = "archiveStoreId1";
    final String ARCHIVE_STORE_ID_2 = "archiveStoreId2";

    @Mock
    ArchiveStore mArchiveStore1;

    @Mock
    ArchiveStore mArchiveStore2;

    final List<Event> sentEvents = new ArrayList<>();

    @BeforeEach
    @SneakyThrows
    void setup() {
        lenient().when(mContext.getNoChunkThreads()).thenReturn(25);

        archiveStoreContext = new ArchiveStoreContext(new LinkedHashMap<>(Map.of(ARCHIVE_STORE_ID_1, mArchiveStore1, ARCHIVE_STORE_ID_2, mArchiveStore2)), new HashMap<>());

        uploader = spy(new DepositArchiveStoresUploader(USER_ID, JOB_ID, DEPOSIT_ID, mUserEventSender, BAG_ID, mContext, mLastEvent, properties, archiveStoreContext));
        lenient().when(uploader.getTaskExecutor(argNumberThreads.capture(), argErrorMessage.capture())).thenReturn(mTaskExecutor);

        lenient().doAnswer(invocation -> {
            Event event = (Event) invocation.getArguments()[0];
            sentEvents.add(event);
            return null;
        }).when(mUserEventSender).send(any(Event.class));
        
        
        doNothing().when(mTaskExecutor).execute(any(Consumer.class));
    }

    @Test
    @SneakyThrows
    void testNoChunks() {
        
        List<DeviceTracker> deviceTrackers = new ArrayList<>();
        doAnswer(invocation -> {
            DeviceTracker dt = (DeviceTracker) invocation.getArguments()[0];
            System.out.printf("XXX ArchiveStoreId[%s]%n", dt.getArchiveStoreId());
            deviceTrackers.add(dt);
            return null;
        }).when(mTaskExecutor).add(any(Callable.class));

        when(mContext.isChunkingEnabled()).thenReturn(false);

        PackageHelper helper = new PackageHelper();
        helper.setTarFile(Files.newTemporaryFile());

        uploader.uploadToStorage(helper, new StoredChunks());
        
        verify(mTaskExecutor, times(2)).add(any(DeviceTracker.class));

        assertThat(deviceTrackers).hasSize(2);

        deviceTrackers.sort(Comparator.comparing(DeviceTracker::getArchiveStoreId));
        
        DeviceTracker dt1 = deviceTrackers.get(0);
        DeviceTracker dt2 = deviceTrackers.get(1);

        assertThat(deviceTrackers.stream().allMatch(dt -> dt.getOptChunkNumber().isEmpty())).isTrue();
        assertThat(deviceTrackers.stream().allMatch(dt -> dt.getDepositId().equals(DEPOSIT_ID))).isTrue();
        assertThat(deviceTrackers.stream().allMatch(dt -> dt.getUserEventSender().equals(mUserEventSender))).isTrue();
        assertThat(deviceTrackers.stream().allMatch(dt -> dt.getJobID().equals(JOB_ID))).isTrue();
        assertThat(deviceTrackers.stream().allMatch(dt -> dt.getTarFile().equals(helper.getTarFile()))).isTrue();
        assertThat(deviceTrackers.stream().allMatch(dt -> dt.getArchiveStoreDepositedFiles() != null)).isTrue();

        assertThat(dt1.getArchiveStoreId()).isEqualTo(ARCHIVE_STORE_ID_1);
        assertThat(dt1.getArchiveStore()).isEqualTo(mArchiveStore1);

        assertThat(dt2.getArchiveStoreId()).isEqualTo(ARCHIVE_STORE_ID_2);
        assertThat(dt2.getArchiveStore()).isEqualTo(mArchiveStore2);

        verify(mUserEventSender, times(2)).send(any(Event.class));

        assertThat(sentEvents.get(0)).isInstanceOf(UpdateProgress.class);
        assertThat(sentEvents.get(1)).isInstanceOf(UploadComplete.class);
        verify(uploader, times(1)).getTaskExecutor(eq(2), any(String.class));
        verify(mTaskExecutor).execute(any(Consumer.class));
        verifyNoMoreInteractions(mTaskExecutor, mContext, mLastEvent, mArchiveStore1, mArchiveStore2);
    }

    @Test
    @SneakyThrows
    void testChunksNoneAlreadyUploaded() {

        List<ChunkUploadTracker> chunkUploadTrackers = new ArrayList<>();
        doAnswer(invocation -> {
            ChunkUploadTracker cut = (ChunkUploadTracker) invocation.getArguments()[0];
            chunkUploadTrackers.add(cut);
            return null;
        }).when(mTaskExecutor).add(any(Callable.class));
        
        when(mContext.isChunkingEnabled()).thenReturn(true);

        PackageHelper helper = new PackageHelper();
        helper.getChunkHelper(1).setChunkFile(Files.newTemporaryFile());
        helper.getChunkHelper(2).setChunkFile(Files.newTemporaryFile());
        helper.getChunkHelper(3).setChunkFile(Files.newTemporaryFile());
        helper.getChunkHelper(4).setChunkFile(Files.newTemporaryFile());
        
        uploader.uploadToStorage(helper, new StoredChunks());

        PackageHelper.ChunkHelper chunkHelper1 = helper.getChunkHelper(1);
        PackageHelper.ChunkHelper chunkHelper2 = helper.getChunkHelper(2);
        PackageHelper.ChunkHelper chunkHelper3 = helper.getChunkHelper(3);
        PackageHelper.ChunkHelper chunkHelper4 = helper.getChunkHelper(4);

        verify(mTaskExecutor, times(4)).add(any(ChunkUploadTracker.class));

        assertThat(chunkUploadTrackers).hasSize(4);

        ChunkUploadTracker cut1 = chunkUploadTrackers.get(0);
        ChunkUploadTracker cut2 = chunkUploadTrackers.get(1);
        ChunkUploadTracker cut3 = chunkUploadTrackers.get(2);
        ChunkUploadTracker cut4 = chunkUploadTrackers.get(3);

        assertThat(chunkUploadTrackers.stream().allMatch(cut -> cut.depositId().equals(DEPOSIT_ID))).isTrue();
        assertThat(chunkUploadTrackers.stream().allMatch(cut -> cut.jobID().equals(JOB_ID))).isTrue();
        assertThat(chunkUploadTrackers.stream().allMatch(cut -> cut.archiveStores().equals(archiveStoreContext.getArchiveStores()))).isTrue();
        assertThat(chunkUploadTrackers.stream().allMatch(cut -> cut.archiveStoresDepositedFiles() != null)).isTrue();
        assertThat(chunkUploadTrackers.stream().allMatch(cut -> cut.userEventSender().equals(mUserEventSender))).isTrue();
        
        assertThat(cut1.chunk()).isEqualTo(chunkHelper1.getChunkFile());
        assertThat(cut1.chunkNumber()).isEqualTo(1);
        
        assertThat(cut2.chunk()).isEqualTo(chunkHelper2.getChunkFile());
        assertThat(cut2.chunkNumber()).isEqualTo(2);
        
        assertThat(cut3.chunk()).isEqualTo(chunkHelper3.getChunkFile());
        assertThat(cut3.chunkNumber()).isEqualTo(3);
        
        assertThat(cut4.chunk()).isEqualTo(chunkHelper4.getChunkFile());
        assertThat(cut4.chunkNumber()).isEqualTo(4);
        
        verify(mUserEventSender, times(2)).send(any(Event.class));

        assertThat(sentEvents.get(0)).isInstanceOf(UpdateProgress.class);
        assertThat(sentEvents.get(1)).isInstanceOf(UploadComplete.class);
        verify(uploader, times(1)).getTaskExecutor(eq(25), any(String.class));
        verify(mContext).getNoChunkThreads();
        verify(mTaskExecutor).execute(any(Consumer.class));
        verifyNoMoreInteractions(mTaskExecutor, mContext, mLastEvent, mArchiveStore1, mArchiveStore2);
    }
    @Test
    @SneakyThrows
    void testChunksSomeAlreadyUploaded() {

        List<ChunkUploadTracker> chunkUploadTrackers = new ArrayList<>();
        doAnswer(invocation -> {
            ChunkUploadTracker cut = (ChunkUploadTracker) invocation.getArguments()[0];
            chunkUploadTrackers.add(cut);
            return null;
        }).when(mTaskExecutor).add(any(Callable.class));

        when(mContext.isChunkingEnabled()).thenReturn(true);

        PackageHelper helper = new PackageHelper();
        PackageHelper.ChunkHelper chunkHelper2 = new PackageHelper.ChunkHelper(2);
        PackageHelper.ChunkHelper chunkHelper3 = new PackageHelper.ChunkHelper(3);

        chunkHelper2.setChunkFile(Files.newTemporaryFile());
        chunkHelper3.setChunkFile(Files.newTemporaryFile());

        helper.addChunkHelper(chunkHelper2);
        helper.addChunkHelper(chunkHelper3);
        uploader.uploadToStorage(helper, new StoredChunks());

        verify(mTaskExecutor, times(2)).add(any(ChunkUploadTracker.class));

        assertThat(chunkUploadTrackers).hasSize(2);

        ChunkUploadTracker cut2 = chunkUploadTrackers.get(0);
        ChunkUploadTracker cut3 = chunkUploadTrackers.get(1);

        chunkUploadTrackers.stream().allMatch(cut -> cut.depositId().equals(DEPOSIT_ID));
        chunkUploadTrackers.stream().allMatch(cut -> cut.jobID().equals(JOB_ID));
        chunkUploadTrackers.stream().allMatch(cut -> cut.archiveStores().equals(archiveStoreContext.getArchiveStores()));
        chunkUploadTrackers.stream().allMatch(cut -> cut.archiveStoresDepositedFiles() != null);
        chunkUploadTrackers.stream().allMatch(cut -> cut.userEventSender().equals(mUserEventSender));

        assertThat(cut2.chunk()).isEqualTo(chunkHelper2.getChunkFile());
        assertThat(cut2.chunkNumber()).isEqualTo(2);

        assertThat(cut3.chunk()).isEqualTo(chunkHelper3.getChunkFile());
        assertThat(cut3.chunkNumber()).isEqualTo(3);
        
        verify(mUserEventSender, times(2)).send(any(Event.class));

        assertThat(sentEvents.get(0)).isInstanceOf(UpdateProgress.class);
        assertThat(sentEvents.get(1)).isInstanceOf(UploadComplete.class);
        verify(uploader, times(1)).getTaskExecutor(eq(25), any(String.class));
        verify(mContext).getNoChunkThreads();
        verify(mTaskExecutor).execute(any(Consumer.class));
        verifyNoMoreInteractions(mTaskExecutor, mContext, mLastEvent, mArchiveStore1, mArchiveStore2);
    }


}