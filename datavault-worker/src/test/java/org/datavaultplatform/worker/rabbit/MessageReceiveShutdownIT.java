package org.datavaultplatform.worker.rabbit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@Slf4j
/*
 Checks that a shutdown message will stop the processing of further messages.
*/
class MessageReceiveShutdownIT extends BaseReceiveIT {

  private static final int MESSAGES_TO_SEND = 5;
  private static final int MESSAGES_TO_PROCESS = 2;

  @MockBean
  MessageProcessor mProcessor;

  @MockBean
  ShutdownHandler mShutdownHandler;

  @Captor
  ArgumentCaptor<MessageInfo> argMessageInfo;

  private CountDownLatch shutdownLatch;

  @Test
  @SneakyThrows
  void testSendAndRecvMessages() {
    this.shutdownLatch = new CountDownLatch(1);

    String shutdownMessageId = null;
    for (long i = 0; i < MESSAGES_TO_SEND; i++) {
      if (i == 2) {
        shutdownMessageId = sendShutdownTestMessage(NORMAL_PRIORITY);
      } else {
        sendNormalMessage(i);
      }
    }
    //okay is true only after we've recvd 1 shutdown message

    boolean okay = shutdownLatch.await(300, TimeUnit.SECONDS);
    if (!okay) {
      Assertions.fail("problem waiting for messageInfos");
    }
    Assertions.assertEquals(MESSAGES_TO_PROCESS, messageInfos.size());

    // check that the shutdown message id is the one we expected
    Assertions.assertEquals(shutdownMessageId, argMessageInfo.getValue().getId());

    verify(mProcessor, times(MESSAGES_TO_PROCESS)).processMessage(any(MessageInfo.class));
    verify(mShutdownHandler, times(1)).handleShutdown(any(MessageInfo.class));
    verifyNoMoreInteractions(mProcessor, mShutdownHandler);

    // check that there are still 2 unprocessed messages
    Assertions.assertEquals(2,
        admin.getQueueInfo(this.workerQueue.getActualName()).getMessageCount());

  }

  @BeforeEach
  void setupMocks() {

    //when we process a message, we record it
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      MessageInfo info = invocation.getArgument(0);
      messageInfos.add(info);
      return false;
    }).when(mProcessor).processMessage(any(MessageInfo.class));

    //when we get a shutdown message, we count down the latch
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      this.shutdownLatch.countDown();
      return null;
    }).when(mShutdownHandler).handleShutdown(argMessageInfo.capture());
  }

}
