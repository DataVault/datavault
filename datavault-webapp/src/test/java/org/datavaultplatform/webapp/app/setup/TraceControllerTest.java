package org.datavaultplatform.webapp.app.setup;

import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TraceInfo;
import org.datavaultplatform.common.util.TraceUtils;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ProfileDatabase
@TestPropertySource(properties = {
        "logging.level.org.springframework.security=TRACE",
        "logging.level.org.springframework.web.filter=DEBUG",
        "logging.level.io.micrometer.tracing=DEBUG",
        "management.tracing.sampling.probability=1.0",
        "management.tracing.propagation.type=w3c"})
@Slf4j
@AutoConfigureObservability
class TraceControllerTest {

  public static final String TRACE_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
  public static final String SPAN_ID = "bbbbbbbbbbbbbbbb";
  public static final String TRACE_PARENT_VALUE = "00-" + TRACE_ID + "-" + SPAN_ID + "-01";

  @Autowired
  Tracer tracer;
  
  @Autowired
  Propagator propagator;
  
  RestTemplate restTemplate;

  @Autowired
  AuthenticationManager authManager;
  
  @LocalServerPort
  int serverPort;
  
  @BeforeEach
  void setup() {
    assertThat(tracer).isNotNull();
    assertThat(propagator).isNotNull();
    restTemplate = new RestTemplateBuilder()
            .rootUri("http://localhost:" + serverPort)
            .basicAuthentication("wactor", "wactorpass")
            .build();
    restTemplate.setInterceptors(List.of(new RequestLoggingInterceptor()));
  }

  @Test
  void testHardcodedLogin() {
    log.info("authManager class : {}", authManager.getClass().getName());
    // 1. Create the "Unauthenticated" token
    UsernamePasswordAuthenticationToken authRequest =
            new UsernamePasswordAuthenticationToken("wactor", "wactorpass");

    // 2. Pass it to the Manager
    Authentication result = authManager.authenticate(authRequest);

    // 3. Assert the result
    assertThat(result).isNotNull();
    assertThat(result.isAuthenticated()).isTrue();
    assertThat(result.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()).containsExactlyInAnyOrder("ROLE_ACTUATOR");
  }
  
  private ResponseEntity<TraceInfo> getTraceInfo(boolean addTraceParentHeader) {
    HttpHeaders headers = new HttpHeaders();
    if (addTraceParentHeader) {
      headers.add(TraceUtils.TRACE_PARENT, TRACE_PARENT_VALUE);
    }
    // Perform the GET request
    ResponseEntity<TraceInfo> response = restTemplate.exchange(
            "/trace/info",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            TraceInfo.class
    );
    return response;
  }
  
  @Test
  void testTimeControllerNoTraceIdSupplied() {

    ResponseEntity<TraceInfo> response = getTraceInfo(false);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TraceInfo traceInfo = response.getBody();
    assertThat(traceInfo.traceId()).isNotNull();
    assertThat(traceInfo.traceId()).isNotEqualTo(TRACE_ID);
    assertThat(TraceId.isValid(traceInfo.traceId())).isTrue();
  }
  
  @Test
  void testTimeControllerWithTraceIdSupplied() {

    ResponseEntity<TraceInfo> response = getTraceInfo(true);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TraceInfo traceInfo = response.getBody();
    assertThat(traceInfo.traceId()).isNotNull();
    assertThat(traceInfo.traceId()).isEqualTo(TRACE_ID);
    assertThat(TraceId.isValid(traceInfo.traceId())).isTrue();
  }

  public static class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
      log.info("=== Request Start ===");
      log.info("URI    : {}", request.getURI());
      log.info("Method : {}", request.getMethod());
      log.info("Headers: {}", request.getHeaders());
      log.info("=== Request End ===");

      return execution.execute(request, body);
    }
  }
}
