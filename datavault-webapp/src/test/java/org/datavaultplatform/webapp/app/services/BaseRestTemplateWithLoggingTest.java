package org.datavaultplatform.webapp.app.services;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.datavaultplatform.webapp.config.logging.LoggingInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.WireMockRestServiceServer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = "logging.level.org.datavaultplatform.webapp.config.logging=DEBUG")
public abstract class BaseRestTemplateWithLoggingTest {

  @Autowired
  protected RestTemplate restTemplate;

  protected final ListAppender<ILoggingEvent> logBackListAppender = new ListAppender<>();

  protected MockRestServiceServer server;


  @SneakyThrows
  protected final void setupInternal(String... stubs) {
    server = WireMockRestServiceServer.with(this.restTemplate) //
            .baseUrl("http://www.example.com:1234") //
            .stubs(stubs)
            .bufferContent()
            .build();

    logBackListAppender.start();
    getLoggingInterceptorLogbackLogger().addAppender(logBackListAppender);
  }

  @AfterEach
  void tearDown() {
    logBackListAppender.stop();
    getLoggingInterceptorLogbackLogger().detachAppender(logBackListAppender);
  }

  private ch.qos.logback.classic.Logger getLoggingInterceptorLogbackLogger() {
    ch.qos.logback.classic.Logger result = (ch.qos.logback.classic.Logger) LoggingInterceptor.LOGGER;
    assertTrue(result.isDebugEnabled());
    return result;
  }

}
