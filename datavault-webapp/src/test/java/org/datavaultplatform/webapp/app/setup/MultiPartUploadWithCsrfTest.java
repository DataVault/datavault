package org.datavaultplatform.webapp.app.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.CsrfInfo;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartResolver;

/**
 * This test checks that we can use MultiPartFileUpload together with CSRF.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
@ProfileStandalone
public class MultiPartUploadWithCsrfTest {

  @Value("classpath:images/logo-dvsmall.jpg")
  Resource dvLogo;

  @Value("classpath:person.json")
  Resource person;

  @Autowired
  TestRestTemplate template;

  @Autowired
  MultipartResolver mpResolver;

  long expectedFile1Size;

  @BeforeEach
  void setup() throws IOException {
    expectedFile1Size = dvLogo.contentLength();
    assertNotNull(mpResolver);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/test/upload/file/one", "/test/upload/file/two"})
  void testUploadFile(String uploadURL) {

    String expectedResult = String.format("name[file]type[image/jpeg]size[%d]", expectedFile1Size);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity(
        new HashMap<String, Object>() {{
          put("file", dvLogo);
        }});
    ResponseEntity<String> response = template.postForEntity(uploadURL, requestEntity, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResult, response.getBody());
  }

  @Test
  public void testUploadMulti() {

    String expectedResult = String.format("name[file]type[image/jpeg]size[%d]first[James]last[Bond]", expectedFile1Size);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity(
        new HashMap<String, Object>() {{
          put("file", dvLogo);
          put("person", person);
        }});

    ResponseEntity<String> response = template.postForEntity("/test/upload/multi", requestEntity, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResult, response.getBody());
  }

  private HttpEntity<MultiValueMap<String,Object>> getRequestEntity(
      Map<String,Object> multiPartFormData){

    CsrfInfo csrfInfo = CsrfInfo.generate(template.getRestTemplate());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setAccept(Collections.singletonList(MediaType.ALL));
    csrfInfo.addJSessionIdCookie(headers);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    multiPartFormData.entrySet().forEach(entry -> {
      body.add(entry.getKey(), entry.getValue());
    });
    csrfInfo.addCsrfParam(body);

    return new HttpEntity<>(body, headers);
  }

}
