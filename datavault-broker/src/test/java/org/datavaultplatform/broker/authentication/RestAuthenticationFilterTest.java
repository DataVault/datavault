package org.datavaultplatform.broker.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class RestAuthenticationFilterTest {

  @Test
  @SneakyThrows
  void testRequestMatcherOkay() {
    RequestMatcher matcher = request -> true;

    RestAuthenticationFilter filter = new RestAuthenticationFilter(matcher);
    RequestMatcher result = filter.getRequestMatcher();
    assertEquals(matcher,result);
  }

  @Test
  @SneakyThrows
  void testRequestMatcherOverridden() {
    RequestMatcher matcher = request -> true;

    RestAuthenticationFilter filter = new RestAuthenticationFilter(matcher);
    filter.setFilterProcessesUrl("/bob");
    RequestMatcher result = filter.getRequestMatcher();
    assertNotEquals(matcher, result);
  }
}
