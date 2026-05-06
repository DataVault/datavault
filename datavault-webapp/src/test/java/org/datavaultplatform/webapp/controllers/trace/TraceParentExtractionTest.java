package org.datavaultplatform.webapp.controllers.trace;

import org.datavaultplatform.common.util.TraceUtils;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "logging.level.org.springframework.security=DEBUG",
        "output.traceid.on.error=true"
})
@AddTestProperties
class TraceParentExtractionTest extends BaseTraceIdDemoControllerTest {
    
    public static final String TRACE_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final String SPAN_ID = "bbbbbbbbbbbbbbbb";
    public static final String TRACEPARENT_VALUE = "00-" + TRACE_ID + "-" + SPAN_ID + "-01";
    
    @Test
    void traceParentShouldBeExtracted() {

        HttpHeaders headers = new HttpHeaders();
        headers.add(TraceUtils.TRACE_PARENT, TRACEPARENT_VALUE);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/trace-test", HttpMethod.GET, entity, String.class);

        assertEquals(TRACE_ID, response.getBody());
    }

    @Test
    void traceParentShouldBeGeneratedIfMissing() {
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/trace-test", HttpMethod.GET, entity, String.class);

        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).isNotEqualTo(TRACE_ID);
    }

}

