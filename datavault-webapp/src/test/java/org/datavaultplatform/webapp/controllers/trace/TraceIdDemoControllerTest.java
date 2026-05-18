package org.datavaultplatform.webapp.controllers.trace;


import io.opentelemetry.api.trace.TraceId;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TraceUtils;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.controllers.trace.mvc.TraceIdDemoController;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "logging.level.org.springframework.security=DEBUG"
})
@Slf4j
@AddTestProperties
class TraceIdDemoControllerTest extends BaseTraceIdDemoControllerTest {
    
    @Autowired
    private TraceIdDemoController traceIdDemoController;

    
    @BeforeEach
    @Override
    void setup() {
        assertThat(traceIdDemoController).isNotNull();
    }
    @Test
    void testPage1TraceIdWillBeAdded() {
        ResponseEntity<String> response = getResponse("/page1");
        log.info(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getHeaders().getContentType()).toString()).contains("text/html");

        Document doc = Jsoup.parse(response.getBody());
        assertThat(doc.title()).isEqualTo("Page One");

        Element traceIdSpan = doc.selectFirst("span#traceid");
        assertThat(traceIdSpan).as("Span with id 'traceid' should exist").isNotNull();

        String traceIdValue = traceIdSpan.text();
        assertThat(traceIdValue).as("Trace ID should not be empty").isNotBlank();
        assertThat(TraceId.isValid(traceIdValue)).isTrue();
    }
    
    private ResponseEntity<String> getResponse(String url) {
        return getResponse(url, new HttpEntity<>(new HttpHeaders()));
    }
    
    private ResponseEntity<String> getResponse(String url, HttpEntity<String> entity) {

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    @Test
    void testPage1WithSuppliedTraceId() {
        String expectedTraceId = "c0106a3cbb4b86444167dcca646ca08d"; // A custom 128-bit trace ID
        String traceparentHeader = "00-" + expectedTraceId + "-0000000000000001-01"; // W3C Trace Context header

        HttpHeaders headers = new HttpHeaders();
        headers.add(TraceUtils.TRACE_PARENT, traceparentHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = getResponse("/page1", entity);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getHeaders().getContentType()).toString()).contains("text/html");

        Document doc = Jsoup.parse(response.getBody());
        assertThat(doc.title()).isEqualTo("Page One");

        Element traceIdSpan = doc.selectFirst("span#traceid");
        assertThat(traceIdSpan).as("Span with id 'traceid' should exist").isNotNull();

        String actualTraceId = traceIdSpan.text();
        assertThat(actualTraceId).as("Trace ID on page should match supplied trace ID").isEqualTo(expectedTraceId);

        log.info(response.getBody());
    }
    
}
