package org.datavaultplatform.webapp.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/*
 This class checks that the LoggingInterceptor attached to the RestTemplate is working as expected.
 1) use wire mock to mock http://www.example.org/resource
 2) link RestTemplate to MockRestServiceServer
 3) add an appender to LoggingInterceptor to capture actual log messages
 */
@SpringBootTest
@Slf4j
@ProfileDatabase
public class RestTemplateLoggingTest extends BaseRestTemplateWithLoggingTest {

  final Resource expectedLogEventsResource = new ClassPathResource("logs/expectedLogEvents.txt");

  @BeforeEach
  @SneakyThrows
  void setup() {
    setupInternal("classpath:/stubs/resource.json");

    File f = expectedLogEventsResource.getFile();
    assertTrue(f.getName().endsWith("txt"));
  }

  @Test
  @SneakyThrows
  public void testLoggingInterceptor() {

    ResponseEntity<String> response = restTemplate.getForEntity(
        "http://www.example.com:1234/resource", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Hello World", response.getBody());
    server.verify();

    Thread.sleep(5000);

    List<String> actualLogEvents = logBackListAppender.list.stream().map(Object::toString).toList();
    List<String> expectedLogEvents = IOUtils.readLines(this.expectedLogEventsResource.getInputStream(), StandardCharsets.UTF_8);
    assertTrue(actualLogEvents.containsAll(expectedLogEvents));
  }
}
