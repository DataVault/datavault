package org.datavaultplatform.worker.rabbit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@Slf4j
/*
 Checks that we can recv and process messages, 1 at a time, in the order they are sent.
*/
class MessageReceiveIT extends BaseReceiveIT {

  private static final int MESSAGES_TO_SEND = 5;
  private static final int MESSAGES_TO_RECV = MESSAGES_TO_SEND;

  @MockBean
  MessageProcessor mProcessor;

  @MockBean
  ShutdownHandler mShutdownHandler;

  CountDownLatch latch;

  @Autowired
  Environment env;

  @Test
  @SneakyThrows
  void testSendAndRecvMessages() {

    Set<String> messageIds = new LinkedHashSet<>();
    this.latch = new CountDownLatch(MESSAGES_TO_RECV);
    for (long i = 0; i < MESSAGES_TO_SEND; i++) {
      messageIds.add(sendNormalMessage(i));
    }
    //okay is true only after we've recvd 10 messages
    boolean okay = latch.await(3, TimeUnit.SECONDS);
    if (!okay) {
      Assertions.fail("problem waiting for messageInfos");
    }
    for (int i = 0; i < messageInfos.size(); i++) {
      log.info("[{}]{}", i, messageInfos.get(i));
    }

    verify(mProcessor, times(MESSAGES_TO_RECV)).processMessage(any(MessageInfo.class));
    verifyNoMoreInteractions(mProcessor, mShutdownHandler);

    int i = 0;
    for (String messageId : messageIds) {
      Assertions.assertEquals(messageId, this.messageInfos.get(i++).getId());
    }
  }

  @BeforeEach
  void setupMocks() {
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      MessageInfo info = invocation.getArgument(0);
      messageInfos.add(info);
      this.latch.countDown();
      return false;
    }).when(mProcessor).processMessage(any(MessageInfo.class));
  }

}
