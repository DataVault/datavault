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
import org.datavaultplatform.worker.queue.Receiver;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
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
  Receiver mProcessor;

  @MockBean
  ShutdownHandler mShutdownHandler;
  
  @Autowired
  RabbitMessageSelectorScheduler selectorScheduler;

  @Captor
  ArgumentCaptor<RabbitMessageInfo> argMessageInfo;

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

    boolean okay = shutdownLatch.await(500, TimeUnit.SECONDS);
    if (!okay) {
      Assertions.fail("problem waiting for messageInfos");
    }
    Assertions.assertEquals(MESSAGES_TO_PROCESS, messageInfos.size());

    // check that the shutdown message id is the one we expected
    Assertions.assertEquals(shutdownMessageId, argMessageInfo.getValue().message().getMessageProperties().getMessageId());

    verify(mProcessor, times(MESSAGES_TO_PROCESS)).onMessage(any(RabbitMessageInfo.class));
    verify(mShutdownHandler, times(1)).handleShutdown(any(RabbitMessageInfo.class));
    verifyNoMoreInteractions(mProcessor, mShutdownHandler);

    // ensure we are passed any race conditions
    Thread.sleep(2000);

    // check that there are still 2 unprocessed messages
    Assertions.assertEquals(2,
        admin.getQueueInfo(this.workerQueue.getActualName()).getMessageCount());

  }

  @BeforeEach
  void setupMocks() {

    //when we process a message, we record it
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      RabbitMessageInfo info = invocation.getArgument(0);
      log.info("processing normal message [{}]", info.getMessageBody());
      messageInfos.add(info);
      return false;
    }).when(mProcessor).onMessage(any(RabbitMessageInfo.class));

    //when we get a shutdown message, we count down the latch
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      RabbitMessageInfo info = invocation.getArgument(0);
      log.info("processing Shutdown message [{}]", info.getMessageBody());

      Assertions.assertEquals(1, invocation.getArguments().length);
      selectorScheduler.disable();
      this.shutdownLatch.countDown();
      return null;
    }).when(mShutdownHandler).handleShutdown(argMessageInfo.capture());
  }

}
