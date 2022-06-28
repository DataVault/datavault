package org.datavaultplatform.webapp.app.authentication.shib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileShib;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerExceptionResolver;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ProfileShib
@Slf4j
public class LoginUsingShibAltTest {

  @Autowired
  TestRestTemplate template;

  @Autowired
  @Qualifier("handlerExceptionResolver")
  HandlerExceptionResolver resolver;

  /**
   * If we try and access a page without 'uid' request header, we should get error.
   */
  @Test
  void testErrorOnMissingReqestHeader() {
    ResponseEntity<String> response = template.getForEntity("/", String.class);
    log.info("status {}", response.getStatusCode());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    Document doc = Jsoup.parse(response.getBody());
    Element elem = doc.selectFirst("span#error-heading");
    assertEquals("Error:",elem.text());
    String actualErrorMessage = doc.selectFirst("span#error-message").text();
    assertTrue(actualErrorMessage.startsWith("Error code 500 returned for / with message: org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException: uid header not found in request"));
  }

  /**
   * If we go direct to the error page, we do not get PreAuthenticatedCredentialsNotFoundException
   */
  @Test
  void testErrorPageIgnoresFilter() {
    ResponseEntity<String> response = template.getForEntity("/error", String.class);
    log.info("status {}", response.getStatusCode());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    Document doc = Jsoup.parse(response.getBody());
    Element elem = doc.selectFirst("span#error-heading");
    assertEquals("Error:",elem.text());
    String actualErrorMessage = doc.selectFirst("span#error-message").text();
    assertFalse(actualErrorMessage.contains("PreAuthenticatedCredentialsNotFoundException"));
  }

  /**
   * If we go direct to resources, we do not get PreAuthenticatedCredentialsNotFoundException
   */
  @Test
  void testStaticResourcesByPassShibFilter() {
    ResponseEntity<byte[]> response = template.getForEntity("/resources/favicon.ico", byte[].class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.valueOf("image/x-icon")));
    assertEquals(3008, response.getHeaders().getContentLength());
  }

  @Nested
  class Actuator {

    ResponseEntity<String> actuatorEndpoint(String endpoint){
      String path = String.format("/actuator/%s",endpoint);
      return template.getForEntity(path, String.class);
    }

    @ParameterizedTest
    @ValueSource(strings={"info", "health", "customtime"})
    void testPublicActuatorEndpoints(String endpoint){
      assertEquals(HttpStatus.OK, actuatorEndpoint(endpoint).getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings={"mappings", "beans", "logging"})
    void testNonPublicActuatorEndpoints(String endpoint){
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actuatorEndpoint(endpoint).getStatusCode());
    }
  }

}
