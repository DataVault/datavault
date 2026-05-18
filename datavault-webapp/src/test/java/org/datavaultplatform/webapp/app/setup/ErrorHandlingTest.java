package org.datavaultplatform.webapp.app.setup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.controllers.standalone.api.SimulateErrorController;
import org.datavaultplatform.webapp.model.test.EmailInfo;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.ui.Model;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"datavault.csrf.disabled=true","output.traceid.on.error=false"})
@Slf4j
@ProfileStandalone
public class ErrorHandlingTest {

  @Autowired
  TestRestTemplate restTemplate;

  /**
   * @see org.datavaultplatform.webapp.controllers.auth.ErrorController#customError(HttpServletRequest, HttpServletResponse, Model) (HttpServletRequest,
   */
  @Test
  void testErrorPageDirectly() {
    ResponseEntity<String> respEntity = restTemplate.getForEntity("/error", String.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respEntity.getStatusCode());
    String body = respEntity.getBody();
    checkNoStackTrace(body);

    //create doc with jsoup from body
    Document doc = Jsoup.parse(body);
    Element errorMessageSpan = doc.getElementById("error-message");
    assertThat(errorMessageSpan.tagName()).isEqualTo("span");
    assertThat(errorMessageSpan.text()).isEqualTo("Error code 500 returned for Unknown with message: Internal Server Error");
  }

  /**
   * @see SimulateErrorController#throwError()
   */
  @Test
  public void testErrorPageBecauseOfException() {
    ResponseEntity<String> respEntity = restTemplate.getForEntity("/test/oops", String.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respEntity.getStatusCode());
    String body = respEntity.getBody();
    checkHasStackTrace(body);

    assertThat(respEntity.getBody()).contains("An error has occurred!");
    assertThat(respEntity.getBody()).contains("SimulatedError");
  }

  /**
   * @see SimulateErrorController#forbidden()
   */
  @Test
  public void testForbiddenException() {
    ResponseEntity<String> respEntity = restTemplate.getForEntity("/test/forbidden", String.class);
    assertEquals(HttpStatus.FORBIDDEN, respEntity.getStatusCode());
    String body = respEntity.getBody();
    checkNoStackTrace(body);

    //response is from auth/denied template
    assertThat(body).contains("Access denied.");

    //response is NOT from error/error template
    assertThat(body).doesNotContain("An error has occured!");
  }

  /**
   * @see SimulateErrorController#entityNotFound()
   */
  @Test
  public void testEntityNotFoundException() {
    ResponseEntity<String> respEntity = restTemplate.getForEntity("/test/entity-not-found",
        String.class);
    assertEquals(HttpStatus.NOT_FOUND, respEntity.getStatusCode());

    String body = respEntity.getBody();
    //response is from error/error template
    assertThat(body).contains("An error has occurred!");

    //response text is generic 404 / NOT FOUND message
    assertThat(body).contains(
        "Error code 404 returned for /test/entity-not-found with message:<br/> Not Found");

    //error page does not have stack trace
    checkNoStackTrace(body);
  }


  /**
   * @see SimulateErrorController#invalidUUN()
   */
  @Test
  public void testInvalidUUNException() {
    ResponseEntity<String> respEntity = restTemplate.getForEntity("/test/invalid-uun",
        String.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respEntity.getStatusCode());
    String body = respEntity.getBody();

    assertThat(body)
        .contains("Error code 500 returned for /test/invalid-uun with message:");

    checkHasStackTrace(body);
    assertThat(body).contains(
        "Caused by: org.datavaultplatform.webapp.exception.InvalidUunException: Invalid UUN: blah");
  }

  /**
   * @see SimulateErrorController#email(EmailInfo)
   */
  @Test
  void testValidationAnnotation() throws URISyntaxException {
    ResponseEntity<EmailInfo> response = postEmail("bob@test.com", EmailInfo.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(new EmailInfo("bob@test.com"), response.getBody());
  }

  @Test
  /**
   * @see SimulateErrorController#email(EmailInfo)
   */
  void testValidationExceptionHandler() throws URISyntaxException {
    ResponseEntity<String> response = postEmail("THIS_IS_NOT_A_VALID_EMAIL", String.class);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertEquals("Not a valid Email Address", response.getBody());
  }

  private void checkNoStackTrace(String body) {
    assertThat(body).doesNotContain("Caused by:");
  }

  private void checkHasStackTrace(String body) {
    assertThat(body).contains("Caused by:");
  }

  private <T> ResponseEntity<T> postEmail(String emailAddress, Class<T> clazz)
      throws URISyntaxException {
    EmailInfo info = new EmailInfo(emailAddress);

    URI url = new URI("/test/email");
    RequestEntity<EmailInfo> request = RequestEntity
        .post(url)
        .accept(MediaType.ALL)
        .contentType(MediaType.APPLICATION_JSON)
        .body(info);
    return restTemplate.exchange(request, clazz);
  }
}
