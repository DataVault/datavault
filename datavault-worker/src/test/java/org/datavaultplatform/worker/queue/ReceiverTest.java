package org.datavaultplatform.worker.queue;

import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.worker.rabbit.RabbitMessageInfo;
import org.datavaultplatform.worker.tasks.Deposit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiverTest {

    private static final String TEST_JOB_ID = "test-job-id";
    private static final String NON_RESTART_JOB_ID = "non-restart-job-id";

    Receiver receiver;
    final boolean chunkingEnabled = true;
    final String chunkingByteSize = "1234";
    final boolean encryptionEnabled = true;
    final String encryptionMode = "GCM";
    final boolean multipleValidationEnabled = true;
    final int noChunkThreads = 25;
    final boolean oldRecompose = false;
    final String recomposeDate = "test-recompose-date";

    @Mock
    RabbitMessageInfo mMessageInfo;

    @Mock
    Message mMessage;

    @Mock
    MessageProperties mMessageProperties;

    @Mock
    StorageClassNameResolver mStorageClassNameResolver;

    @Mock
    ProcessedJobStore mProcessedJobStore;

    @Mock
    EventSender mEventSender;

    @Mock
    Deposit mDeposit;

    @Mock
    Event mLastEvent;

    @Mock
    Map<String, String> mDepositProperties;
    
    String jsonMessageBody;

    String testApplicationName = "worker-2";
    
    @Mock
    Context mContext;

    @SneakyThrows
    @BeforeEach
    void setup() {

        Path basePath = Files.createTempDirectory("test");
        Path tempDirPath = basePath.resolve("tempDir");
        Path metaDirPath = basePath.resolve("tempDir");

        Files.createDirectories(tempDirPath);
        Files.createDirectories(metaDirPath);

        jsonMessageBody = StreamUtils.copyToString(new ClassPathResource("sampleMessages/sampleDepositMessage.json").getInputStream(), StandardCharsets.UTF_8);

        receiver = spy(new Receiver(
                tempDirPath.toString(),
                metaDirPath.toString(),
                chunkingEnabled,
                chunkingByteSize,

                encryptionEnabled,
                encryptionMode,

                multipleValidationEnabled,
                noChunkThreads,

                mEventSender,
                mStorageClassNameResolver,
                oldRecompose,
                recomposeDate,
                mProcessedJobStore,
                testApplicationName
        ));

        Mockito.lenient().when(mMessageInfo.message()).thenReturn(mMessage);
        Mockito.lenient().when(mMessageInfo.getMessageBody()).thenReturn(jsonMessageBody);
        Mockito.lenient().when(mMessage.getMessageProperties()).thenReturn(mMessageProperties);
        Mockito.lenient().when(mMessageProperties.getMessageId()).thenReturn("test-message-id");
        Mockito.lenient().when(mDeposit.getJobID()).thenReturn(TEST_JOB_ID);
        Mockito.lenient().when(mLastEvent.getAgent()).thenReturn("worker-one");
        Mockito.lenient().when(mDeposit.getProperties()).thenReturn(mDepositProperties);

        assertThat(mockingDetails(receiver).isSpy()).isTrue();
    }

    @Test
    void testGetTask() {
        Task task = receiver.getConcreteTask(jsonMessageBody);
        if (!(task instanceof Deposit deposit)) {
            throw new IllegalStateException("Deposit expected!");
        }
        assertThat(deposit.getJobID()).isEqualTo(TEST_JOB_ID);
        assertThat(deposit.getProperties()).contains(Map.entry("bagId", "bf73a7f5-42d1-4c3f-864a-a171af8373d4"));
    }

    @Test
    void testRedeliveredMessagesAreNotProcessed() {
        Deposit mTask = mock(Deposit.class);

        when(mMessageProperties.isRedelivered()).thenReturn(true);
        doReturn(mTask).when(receiver).getConcreteTask(jsonMessageBody);
        doNothing().when(mTask).setIsRedeliver(true);

        receiver.onMessage(mMessageInfo);

        verify(receiver).onMessage(mMessageInfo);
        verify(receiver).getConcreteTask(jsonMessageBody);
        verify(receiver).getContext(any(Path.class));
        verify(mMessageProperties).isRedelivered();
        verify(mMessageProperties).getMessageId();
        verify(mMessageInfo).message();
        verify(mMessageInfo).getMessageBody();
        verify(mMessage).getMessageProperties();

        verify(mTask).getJobID();
        verify(mTask, times(1)).setIsRedeliver(true);
        verify(mTask, never()).performAction(any(Context.class));

        verifyNoMoreInteractions(mMessageInfo, mMessage, mMessageProperties, mTask, mProcessedJobStore);
    }

    @Test
    void testDepositsWithoutLastEventsJobIdsStored() {

        doReturn(null).when(mDeposit).getLastEvent();
        doReturn(false).when(mMessageProperties).isRedelivered();
        doReturn(mDeposit).when(receiver).getConcreteTask(jsonMessageBody);
        doReturn(mContext).when(receiver).getContext(any(Path.class));
        doNothing().when(mProcessedJobStore).storeProcessedJob(TEST_JOB_ID);
        doNothing().when(mDeposit).performAction(mContext);

        receiver.onMessage(mMessageInfo);

        verify(receiver).onMessage(mMessageInfo);
        verify(receiver).getConcreteTask(jsonMessageBody);
        verify(receiver).getContext(any(Path.class));
        verify(mMessageProperties).isRedelivered();
        verify(mMessageProperties).getMessageId();
        verify(mMessageInfo).message();
        verify(mMessageInfo).getMessageBody();
        verify(mMessage).getMessageProperties();

        verify(mDeposit, atLeast(1)).getJobID();
        verify(mDeposit, atLeast(1)).getLastEvent();
        verify(mDeposit, never()).setIsRedeliver(any(Boolean.class));
        verify(mDeposit).performAction(mContext);

        verify(mProcessedJobStore).storeProcessedJob(TEST_JOB_ID);

        verifyNoMoreInteractions(mMessageInfo, mMessage, mMessageProperties, mDeposit, mProcessedJobStore);
    }

    @Test
    void testDepositsWithLastEventWhereNonRestartJobIdIsStored() {

        doReturn(mLastEvent).when(mDeposit).getLastEvent();
        doReturn(false).when(mMessageProperties).isRedelivered();
        doReturn(mDeposit).when(receiver).getConcreteTask(jsonMessageBody);
        doReturn(mContext).when(receiver).getContext(any(Path.class));
        doReturn(true).when(mProcessedJobStore).isProcessedJob(NON_RESTART_JOB_ID);
        doReturn(NON_RESTART_JOB_ID).when(mDepositProperties).get(PropNames.NON_RESTART_JOB_ID);

        doNothing().when(mProcessedJobStore).storeProcessedJob(TEST_JOB_ID);
        doNothing().when(mDeposit).performAction(mContext);

        receiver.onMessage(mMessageInfo);

        verify(receiver).onMessage(mMessageInfo);
        verify(receiver).getConcreteTask(jsonMessageBody);
        verify(receiver).getContext(any(Path.class));

        verify(mMessageProperties).isRedelivered();
        verify(mMessageProperties).getMessageId();
        verify(mMessageInfo).message();
        verify(mMessageInfo).getMessageBody();
        verify(mMessage).getMessageProperties();

        verify(mDeposit, atLeast(1)).getJobID();
        verify(mDeposit, atLeast(1)).getLastEvent();
        verify(mDeposit, never()).setIsRedeliver(any(Boolean.class));
        verify(mDeposit).getProperties();

        verify(mDepositProperties).get(PropNames.NON_RESTART_JOB_ID);

        verify(mProcessedJobStore).isProcessedJob(NON_RESTART_JOB_ID);

        verify(mDeposit).performAction(mContext);
        verify(mProcessedJobStore).storeProcessedJob(TEST_JOB_ID);

        verifyNoMoreInteractions(mMessageInfo, mMessage, mMessageProperties, mDeposit, mProcessedJobStore);
    }

    @Test
    void testDepositsWithLastEventWhereNonRestartJobIdIsNotStored() {

        doReturn(mLastEvent).when(mDeposit).getLastEvent();
        doReturn(false).when(mMessageProperties).isRedelivered();
        doReturn(mDeposit).when(receiver).getConcreteTask(jsonMessageBody);
        doReturn(mContext).when(receiver).getContext(any(Path.class));
        doReturn(false).when(mProcessedJobStore).isProcessedJob(NON_RESTART_JOB_ID);
        doReturn(NON_RESTART_JOB_ID).when(mDepositProperties).get(PropNames.NON_RESTART_JOB_ID);

        receiver.onMessage(mMessageInfo);

        verify(receiver).onMessage(mMessageInfo);
        verify(receiver).getConcreteTask(jsonMessageBody);
        verify(receiver).getContext(any(Path.class));

        verify(mMessageProperties).isRedelivered();
        verify(mMessageProperties).getMessageId();
        verify(mMessageInfo).message();
        verify(mMessageInfo).getMessageBody();
        verify(mMessage).getMessageProperties();

        verify(mDeposit, atLeast(1)).getJobID();
        verify(mDeposit, atLeast(1)).getLastEvent();
        verify(mDeposit, never()).setIsRedeliver(any(Boolean.class));
        verify(mDeposit).getProperties();

        verify(mDepositProperties).get(PropNames.NON_RESTART_JOB_ID);

        verify(mProcessedJobStore).isProcessedJob(NON_RESTART_JOB_ID);

        verify(mProcessedJobStore, never()).storeProcessedJob(TEST_JOB_ID);
        verify(mDeposit, never()).performAction(mContext);

        verifyNoMoreInteractions(mMessageInfo, mMessage, mMessageProperties, mDeposit, mProcessedJobStore);
    }
}