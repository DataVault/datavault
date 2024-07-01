package org.datavaultplatform.worker.rabbit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@Slf4j
/*
 checks that messages are sent and not re-delivered.
*/
class MessageReceiveAndNoRedeliverIT extends BaseReceiveIT {

  public static final int MESSAGES_TO_SEND = 10;

  // none of the messages are re-delivered
  public static final int MESSAGES_TO_RECV = 10;

  @MockBean
  MessageProcessor mProcessor;

  @MockBean
  ShutdownHandler mShutdownHandler;

  CountDownLatch latch;

  @Test
  @SneakyThrows
  void testSendAndRecvMessages() {
    Map<Long, String> messages = new HashMap<>();
    this.latch = new CountDownLatch(MESSAGES_TO_RECV);
    for (long i = 0; i < MESSAGES_TO_SEND; i++) {
      messages.put(i, sendNormalMessage(i));
    }
    //okay is true only after we've recvd 15 messages
    boolean okay = latch.await(3, TimeUnit.SECONDS);
    if (!okay) {
      Assertions.fail("problem waiting for messageInfos");
    }
    for (int i = 0; i < messageInfos.size(); i++) {
      log.info("[{}]{}", i, messageInfos.get(i));
    }

    verify(mProcessor, times(MESSAGES_TO_RECV)).processMessage(any(MessageInfo.class));
    verifyNoMoreInteractions(mProcessor, mShutdownHandler);
    for (Long index : messages.keySet()) {
      String id = messages.get(index);
      long recvdCount = this.messageInfos.stream().filter(mi -> mi.getId().equals(id)).count();
      Assertions.assertEquals(1, recvdCount);
    }
  }

  @BeforeEach
  void setupMocks() {
    doAnswer(invocation -> {
      Assertions.assertEquals(1, invocation.getArguments().length);
      MessageInfo info = invocation.getArgument(0);
      messageInfos.add(info);
      this.latch.countDown();
      return null;
    }).when(mProcessor).processMessage(any(MessageInfo.class));
  }

}
