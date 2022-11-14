package org.datavaultplatform.worker.rabbit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.CountDownLatch;
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
 Checks that a hi-priority shutdown message will get processed before
 other normal messages.
*/
class MessageReceiveHiPriorityIT extends BaseReceiveIT {

  private static final int LO_MESSAGES_TO_SEND = 100;
  private static final int MESSAGES_TO_PROCESS = 1;

  @MockBean
  MessageProcessor mProcessor;

  @MockBean
  ShutdownHandler mShutdownHandler;

  @Captor
  ArgumentCaptor<MessageInfo> argMessageInfo;

  CountDownLatch shutdownLatch;

  @Test
  @SneakyThrows
  void testSendAndRecvMessages() {

    this.shutdownLatch = new CountDownLatch(1);

    for (long i = 0; i < LO_MESSAGES_TO_SEND; i++) {
      sendNormalMessage(i);
    }
    //wait long enough for processing of first normal message
    Thread.sleep(100);
    //okay is true only after we've recvd 1 shutdown message
    String shutdownMessageId = sendShutdownTestMessage(HI_PRIORITY);
    shutdownLatch.await();
    Assertions.assertEquals(MESSAGES_TO_PROCESS, messageInfos.size());

    // check that the shutdown message id is the one we expected
    Assertions.assertEquals(shutdownMessageId, argMessageInfo.getValue().getId());

    verify(mProcessor, times(MESSAGES_TO_PROCESS)).processMessage(any(MessageInfo.class));
    verify(mShutdownHandler, times(1)).handleShutdown(any(MessageInfo.class));
    verifyNoMoreInteractions(mProcessor, mShutdownHandler);

    Assertions.assertEquals(LO_MESSAGES_TO_SEND - 1,
        admin.getQueueInfo(this.workerQueue.getActualName()).getMessageCount());
  }


  @BeforeEach
  void setupMocks() {

    // when we process a message, we record it
    doAnswer(invocation -> {
      // by sleeping, we ensure the hi-priority message has time to get to head of the queue
      Thread.sleep(1000);
      Assertions.assertEquals(1, invocation.getArguments().length);
      MessageInfo info = invocation.getArgument(0);
      messageInfos.add(info);
      return false;
    }).when(mProcessor).processMessage(any(MessageInfo.class));

    // when we get a shutdown message, we count down the latch
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      this.shutdownLatch.countDown();
      return null;
    }).when(mShutdownHandler).handleShutdown(argMessageInfo.capture());
  }

}
