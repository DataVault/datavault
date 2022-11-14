package org.datavaultplatform.broker.config;


import org.datavaultplatform.broker.queue.Sender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/*
This class creates mock beans which are placed into the spring context.
 */
@TestConfiguration
public class MockRabbitConfig {

  @MockBean
  Sender mSender;

}
