package org.datavaultplatform.webapp.controllers.trace;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.datavaultplatform.common.util.TraceUtils;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.controllers.trace.mvc.TraceIdDemoController;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "logging.io.micrometer=DEBUG",
        "logging.level.org.springframework.security=DEBUG"
})
@AddTestProperties
class SimpleRestServiceTracePropagationTest extends BaseTraceIdDemoControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private ListAppender<ILoggingEvent> listAppender;

    private static final String HARDCODED_TRACE_ID = "abcdef1234567890abcdef1234567890";
    private static final String HARDCODED_SPAN_ID = "fedcba0987654321";
    private static final String TRACEPARENT_HEADER_VALUE = "00-" + HARDCODED_TRACE_ID + "-" + HARDCODED_SPAN_ID + "-01";

    @BeforeEach
    @Override
    void setup() {
        // Get the logger for the class that produces the log messages we want to capture
        Logger testControllerLogger = (Logger) LoggerFactory.getLogger(TraceIdDemoController.class);

        // Create and start a new ListAppender
        listAppender = new ListAppender<>();
        listAppender.start();

        // Add the appender to the logger
        testControllerLogger.addAppender(listAppender);
    }

    @AfterEach
    void teardown() {
        // Detach the appender after the test is done
        Logger testControllerLogger = (Logger) LoggerFactory.getLogger(TraceIdDemoController.class);
        testControllerLogger.detachAppender(listAppender);
    }

    @Test
    void testTraceIdIsPropagatedByRestTemplate() {
        // 1. Prepare the request with the hardcoded traceparent header
        HttpHeaders headers = new HttpHeaders();
        headers.add(TraceUtils.TRACE_PARENT, TRACEPARENT_HEADER_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 2. Call the initial endpoint that triggers the downstream call
        ResponseEntity<String> response = restTemplate.exchange("/call-downstream", HttpMethod.GET, entity, String.class);

        // 3. Assert the call was successful
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 4. Filter the captured logs to find the one from the downstream endpoint
        List<ILoggingEvent> logs = listAppender.list;
        List<String> downstreamLogs = logs.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(msg -> msg.startsWith("/downstream: Current traceId from tracer:"))
                .toList();

        // 5. Assert that the downstream endpoint logged the correct traceId
        assertThat(downstreamLogs).hasSize(1);
        assertThat(downstreamLogs.get(0)).contains(HARDCODED_TRACE_ID);
    }
}
