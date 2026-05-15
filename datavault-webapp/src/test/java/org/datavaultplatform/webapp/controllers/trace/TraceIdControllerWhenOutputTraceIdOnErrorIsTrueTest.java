package org.datavaultplatform.webapp.controllers.trace;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TraceUtils;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.org.springframework.security=DEBUG",
                "output.traceid.on.error=true",
                "logging.level.org.datavaultplatform.webapp.controllers.trace.mvc=DEBUG"
        })
@Slf4j
@AddTestProperties
class TraceIdControllerWhenOutputTraceIdOnErrorIsTrueTest extends BaseTraceIdDemoControllerTest {

    private static final String TEST_TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736"; // A sample W3C trace ID
    private static final String TEST_SPAN_ID = "00f067aa0ba902b7"; // A sample W3C span ID
    // The traceparent header format: 00-TRACE_ID-SPAN_ID-01 (version-trace-id-parent-id-trace-flags)
    private static final String TRACEPARENT_HEADER_VALUE = "00-" + TEST_TRACE_ID + "-" + TEST_SPAN_ID + "-01";

    @Test
    void testErrorPageWhenOutputTraceOnErrorIsTrue() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(TraceUtils.TRACE_PARENT, TRACEPARENT_HEADER_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("/oops", HttpMethod.GET, entity, String.class);

        log.info(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getHeaders().getContentType()).isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
        assertThat(response.getBody()).contains("An error has occurred!");
        assertThat(response.getBody()).contains("<title>Error Page</title>");

        // The MyErrorController formats the message as: "An error has occurred. The traceId is [%s]. Report this TraceId to support."
        String expectedTraceIdMessagePart = BaseErrorController.MESSAGE_PATTERN.formatted(TEST_TRACE_ID);
        assertThat(response.getBody()).contains(expectedTraceIdMessagePart);

        // For a more robust assertion, you can extract the trace ID using a regex
        Pattern traceIdPattern = Pattern.compile("Trace Id \\[([a-f0-9]{32})\\]");
        Matcher matcher = traceIdPattern.matcher(response.getBody());
        assertThat(matcher.find()).isTrue(); // Ensure a trace ID was found
        assertThat(matcher.group(1)).isEqualTo(TEST_TRACE_ID); // Assert it matches the expected trace ID
        log.info("Trace ID extracted from response: {}", matcher.group(1));
    }
}
