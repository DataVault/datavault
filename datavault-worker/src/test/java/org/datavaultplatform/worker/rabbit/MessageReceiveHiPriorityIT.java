package org.datavaultplatform.worker.rabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
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

  private static final int LO_MESSAGES_TO_SEND = 10;

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

    MessageListenerContainer container = registry.getListenerContainer("rabbit-listener");

    container.stop();

    this.shutdownLatch = new CountDownLatch(1);

    for (int i = 0; i < LO_MESSAGES_TO_SEND; i++) {
      sendNormalMessage(i);
    }
    
    //okay is true only after we've recvd 1 shutdown message
    String shutdownMessageId = sendShutdownTestMessage();

    Thread.sleep(100);
    //wait long enough for rabbit to put shutdown message first

    container.start();
    
    this.shutdownLatch.await();

    // check that the shutdown message id is the one we expected
    Assertions.assertEquals(shutdownMessageId, argMessageInfo.getValue().getId());
    Assertions.assertEquals(shutdownMessageId, messageInfos.get(0).getId());

    verify(mShutdownHandler, times(1)).handleShutdown(any(MessageInfo.class));
    verify(mProcessor, never()).processMessage(any(MessageInfo.class));
    verifyNoMoreInteractions(mProcessor, mShutdownHandler);

    assertEquals(1, messageInfos.size());
    
    Assertions.assertEquals(LO_MESSAGES_TO_SEND,
        admin.getQueueInfo(this.workerQueue.getActualName()).getMessageCount());
  }


  @BeforeEach
  void setupMocks() {

    // when we process a message, we record it
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      MessageInfo info = invocation.getArgument(0, MessageInfo.class);
      messageInfos.add(info);

      return null;
    }).when(mProcessor).processMessage(any(MessageInfo.class));

    // when we get a shutdown message, we count down the latch
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      MessageInfo info = invocation.getArgument(0, MessageInfo.class);
      messageInfos.add(info);

      this.shutdownLatch.countDown();
      return null;
    }).when(mShutdownHandler).handleShutdown(argMessageInfo.capture());

  }

}
