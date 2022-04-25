package org.datavaultplatform.common.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CreateClientEventTest {

  static final String ADDRESS = "1.2.3.4";
  static final String USER_AGENT = "Chrome";
  static final String SESSION_ID = "12345";
  static final String SERIALIZED_EVENT = "{\"remoteAddress\":\"1.2.3.4\",\"userAgent\":\"Chrome\"}";
  static String SERIALIZED_EVENT_WITH_SESSION_ID = "{\"remoteAddress\":\"1.2.3.4\",\"userAgent\":\"Chrome\",\"sessionId\":\"IGNORE\"}";
  static final CreateClientEvent EVENT = new CreateClientEvent(ADDRESS, USER_AGENT);

  ObjectMapper mapper = new ObjectMapper();

  @Nested
  class SerializationTests {

    @Test
    void testWithoutSessionId() throws JsonProcessingException {
      CreateClientEvent event = new CreateClientEvent(ADDRESS, USER_AGENT);
      String actualJson = mapper.writeValueAsString(event);
      assertEquals(SERIALIZED_EVENT, actualJson);
    }

    @Test
    void testSessionId() throws JsonProcessingException {
      CreateClientEvent event = new CreateClientEvent(ADDRESS, USER_AGENT);
      event.setSessionId(SESSION_ID);

      String actualJson = mapper.writeValueAsString(event);
      assertEquals(SERIALIZED_EVENT, actualJson);
    }
  }

  @Nested
  class DeSerializationTests {

    @Test
    void testWithoutSessionId() throws IOException {
      CreateClientEvent actual = mapper.readValue(SERIALIZED_EVENT.getBytes(), CreateClientEvent.class);
      assertNull(actual.getSessionId());
      assertEquals(EVENT, actual);
    }

    @Test
    void testWithSessionId() throws IOException {
      CreateClientEvent actual = mapper.readValue(SERIALIZED_EVENT_WITH_SESSION_ID.getBytes(), CreateClientEvent.class);
      assertNull(actual.getSessionId());
      assertEquals(EVENT, actual);
    }

  }

}
