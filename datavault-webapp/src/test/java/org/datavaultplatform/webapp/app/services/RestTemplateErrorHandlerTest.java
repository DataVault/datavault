package org.datavaultplatform.webapp.app.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.rmi.ServerException;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.datavaultplatform.common.api.ApiError;
import org.datavaultplatform.webapp.exception.ForbiddenException;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.datavaultplatform.webapp.config.logging=DEBUG")
public class RestTemplateErrorHandlerTest {

  @Autowired
  RestTemplate restTemplate;

  MockRestServiceServer server;

  @Autowired
  ObjectMapper mapper;

  @BeforeEach
  void setup() {
    server = MockRestServiceServer.bindTo(restTemplate).bufferContent().build();
  }

  private String getJson(ApiError api) throws JsonProcessingException {
    return mapper.writeValueAsString(api);
  }

  private ApiError getApiError(String json) throws JsonProcessingException {
    return mapper.readValue(json, ApiError.class);
  }

  @Test
  void testForbiddenResultsInException() {
    server
        .expect(once(), requestTo("/forbidden"))
        .andExpect(method(GET)).andRespond(withStatus(HttpStatus.FORBIDDEN));

    ForbiddenException ex = assertThrows(ForbiddenException.class, () ->
        restTemplate.getForEntity("/forbidden", String.class));

    assertThat(ex.getMessage()).isEqualTo("Access denied");
    server.verify();
  }

  @Test
  @SneakyThrows
  void testInternalServerErrorWithApiError() {
    UnsupportedOperationException brokerEx = new UnsupportedOperationException("oops");
    String rand = UUID.randomUUID().toString();
    ApiError error = new ApiError(rand, brokerEx);
    Date timestamp = new Date();
    error.setTimestamp(timestamp);

    server
        .expect(once(), requestTo("/internal/api"))
        .andExpect(method(GET)).andRespond(
            withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(getJson(error)));

    ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
        restTemplate.getForEntity("/internal/api", String.class));

    ServerException nested1 = (ServerException) ex.getCause();
    assertEquals("There has been an error while calling the broker Api:; nested exception is: \n"
        + "\tjava.lang.Exception: oops", nested1.getMessage());
    Throwable nested2 = nested1.getCause();
    assertEquals("oops", nested2.getMessage());
    server.verify();
  }

  @Test
  void testSimpleExampleNoError() {

    server.expect(once(), requestTo("/hotels/42")).andExpect(method(GET))
        .andRespond(withSuccess("{ \"id\" : \"42\", \"name\" : \"Holiday Inn\"}",
            MediaType.APPLICATION_JSON));

    Hotel hotel = restTemplate.getForObject("/hotels/{id}", Hotel.class, 42);
    // Use the hotel instance...

    assertEquals("42", hotel.getId());
    assertEquals("Holiday Inn", hotel.getName());
    server.verify();
  }


  @Data
  @NoArgsConstructor
  static class Hotel {

    private String id;
    private String name;
  }
}
