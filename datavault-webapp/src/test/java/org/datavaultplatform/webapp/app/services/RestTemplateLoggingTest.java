package org.datavaultplatform.webapp.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.micrometer.tracing.Tracer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.util.TraceIdWrapper;
import org.datavaultplatform.common.util.TraceUtils;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/*
 This class checks that the LoggingInterceptor attached to the RestTemplate is working as expected.
 1) use wire mock to mock http://www.example.org/resource
 2) link RestTemplate to MockRestServiceServer
 3) add an appender to LoggingInterceptor to capture actual log messages
 */
@SuppressWarnings("GrazieInspectionRunner")
@SpringBootTest
@Slf4j
@ProfileDatabase
@TestPropertySource(properties = "management.tracing.enabled=false")
public class RestTemplateLoggingTest extends BaseRestTemplateWithLoggingTest {

  @Autowired
  Tracer tracer;
  
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
  void testLoggingInterceptor() {

    String traceId = "aaaabbbbccccddddaaaabbbbccccdddd";
    TraceIdWrapper wrapper = new TraceIdWrapper(traceId, tracer);
    wrapper.runWithinWrapper(() -> {
      ResponseEntity<String> response = restTemplate.getForEntity(
          "http://www.example.com:1234/resource", String.class);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("Hello World", response.getBody());
      server.verify();

      Thread.sleep(5000);

      List<String> actualLogEvents = logBackListAppender.list.stream().map(Object::toString).toList();
      
      String traceParent = actualLogEvents.stream().filter(msg -> msg.indexOf(TraceUtils.TRACE_PARENT) > 0).findFirst().get();
      assertTrue(traceParent.contains(traceId));
      
      List<String> expectedLogEvents = IOUtils.readLines(this.expectedLogEventsResource.getInputStream(), StandardCharsets.UTF_8);
      assertTrue(actualLogEvents.containsAll(expectedLogEvents));
      return null;
    });
  }
}
