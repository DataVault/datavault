package org.datavaultplatform.webapp.controllers.trace;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.org.springframework.security=DEBUG",
                "output.traceid.on.error=false",
                "logging.level.org.datavaultplatform.webapp.controllers.trace.mvc=DEBUG"
        })
@Slf4j
@AddTestProperties
class TraceIdControllerWhenOutputTraceIdOnErrorIsFalseTest extends BaseTraceIdDemoControllerTest {

    @Test
    void testErrorPageWhenOutputTraceOnErrorIsFalse() {

        ResponseEntity<String> response = restTemplate.getForEntity("/oops", String.class);

        log.info(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getHeaders().getContentType()).isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
        assertThat(response.getBody()).contains("An error has occurred!");
        assertThat(response.getBody()).contains("<title>Error Page</title>");
        assertThat(response.getBody()).contains("<span id=\"error-message\">Error code 500 returned for /oops with message:<br/> jakarta.servlet.ServletException: Request processing failed: java.lang.RuntimeException: oops");
    }
}
