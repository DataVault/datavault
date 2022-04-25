package org.datavaultplatform.webapp.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.webapp.config.logging.LoggingInterceptor;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockRestServiceServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/*
 This class checks that the LoggingInterceptor attached to the RestTemplate is working as expected.
 1) use wire mock to mock http://www.example.org/resource
 2) link RestTemplate to MockRestServiceServer
 3) add an appender to LoggingInterceptor to capture actual log messages
 */
@SpringBootTest
@Slf4j
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.datavaultplatform.webapp.config.logging=DEBUG")
public class RestTemplateLoggingTest {

  @Autowired
  private RestTemplate restTemplate;

  ListAppender<ILoggingEvent> logBackListAppender = new ListAppender<>();

  MockRestServiceServer server;

  @Value("${classpath:/logs/expectedLogEvents.txt}")
  ClassPathResource expectedLogEventsResource;

  @BeforeEach
  void setup() {
    server = WireMockRestServiceServer.with(this.restTemplate) //
        .baseUrl("https://example.org") //
        .stubs("classpath:/stubs/resource.json")
        .bufferContent()
        .build();

    logBackListAppender.start();
    getLoggingInterceptorLogbackLogger().addAppender(logBackListAppender);

  }

  @Test
  @SneakyThrows
  public void testLoggingInterceptor() {

    ResponseEntity<String> response = restTemplate.getForEntity(
        "https://example.org/resource", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Hello World", response.getBody());
    server.verify();

    List<String> actualLogEvents = logBackListAppender.list.stream().map(Object::toString).collect(Collectors.toList());
    List<String> expectedLogEvents = IOUtils.readLines(this.expectedLogEventsResource.getInputStream(), StandardCharsets.UTF_8);;

    assertEquals(expectedLogEvents, actualLogEvents);
  }

  @AfterEach
  void tearDown() {
    logBackListAppender.stop();
    getLoggingInterceptorLogbackLogger().detachAppender(logBackListAppender);
  }

  private ch.qos.logback.classic.Logger getLoggingInterceptorLogbackLogger() {
    return (ch.qos.logback.classic.Logger) LoggingInterceptor.LOGGER;
  }

}
