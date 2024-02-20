package org.datavaultplatform.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class UserJsonTest {
  private ObjectMapper mapper = new ObjectMapper();
  @Test
  @SneakyThrows
  void testJson(){
    User user = new User();
    user.setID("007");
    user.setLastname("bond");
    user.setFirstname("james");
    user.setEmail("james.bond@test.com");
    user.setPassword("tenet");
    user.getProperties().put("first","James");
    user.getProperties().put("last","Bond");


    String json1 = mapper.writeValueAsString(user);
    assertEquals("{\"id\":\"007\",\"firstname\":\"james\",\"lastname\":\"bond\",\"password\":\"tenet\",\"email\":\"james.bond@test.com\",\"properties\":{\"last\":\"Bond\",\"first\":\"James\"}}", json1);
  }

}
