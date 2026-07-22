package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class UserEntityTest extends BaseEntityTest<User, String> {

  @Test
  void testEntity() {
    checkEntity(User.class, this::generateID);
  }

  @ParameterizedTest
  @CsvSource(nullValues = "null", textBlock = """
          null, false
          "invalid", false
          "@", true
          "bob@example.com", true
          """)
  void testIsValidEmail(String email, boolean expectedIsValid) {
    
    //test instance method
    User user = new User();
    user.setEmail(email);
    boolean actualIsValid = user.isValidEmail();
    assertThat(actualIsValid).isEqualTo(expectedIsValid);
      
    //test static method  
    assertThat(User.isValidEmail(email)).isEqualTo(expectedIsValid);
  }
}
