package org.datavaultplatform.webapp.app.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.datavaultplatform.webapp.test.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@ProfileStandalone
@AutoConfigureMockMvc
@Slf4j
/**
 * Tests the Roles required to access protected paths.
 * @see org.datavaultplatform.webapp.config.standalone.StandaloneWebSecurityConfig
 */
public class ProtectedPathsTest {

  private static final String ROLE_XXX = "XXX";
  private static final AtomicInteger COUNTER = new AtomicInteger();
  private static final int EXPECTED_TESTS = 20;

  @Autowired
  MockMvc mvc;

  @BeforeAll
  static void setup() {
    COUNTER.set(0);
  }

  @AfterAll
  static void tearDown() {
    assertEquals(EXPECTED_TESTS, COUNTER.intValue());
  }

  /**
   * The error page responds with 500 Internal Error. Not 200 OK
   */
  @Test
  void testSpecialCaseErrorPath() {
    MvcResult result = accessPathUsingRoles("/error");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
  }


  /**
   * Tests that anyone who is not logged in can access /auth/security_check
   */
  @Test
  @SneakyThrows
  void testSpecialCaseAuthSecurityCheck() {
    mvc.perform(formLogin("/auth/security_check")
        .user("user")
        .password("password"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/"));
  }

  @ParameterizedTest(name = "[{index}] path={0}, role={1}")
  @CsvFileSource(resources = "/protected-paths.csv", numLinesToSkip = 1)
  void testPathRequiresRole(String path, String roleWithPrefix) {

    String[] roles = getRolesWithoutPrefix(roleWithPrefix);

    checkPathCanBeAccessedWithRole(path, roles);

    if (roles.length > 0) {
      checkPathCannotBeAccessedWithoutCorrectRole(path);
    }

    COUNTER.incrementAndGet();
  }

  /**
   * Maps non-null roleWithPrefix to single element array with role stripped of prefix Maps null
   * input to empty array.
   *
   * @param roleWithPrefix - may be null
   * @return String[]
   */
  private String[] getRolesWithoutPrefix(String roleWithPrefix) {
    return Collections.singleton(roleWithPrefix)
        .stream()
        .filter(Objects::nonNull)
        .map(rwp -> TestUtils.stripPrefix("ROLE_", rwp))
        .toArray(String[]::new);
  }

  @SneakyThrows
  private MvcResult accessPathUsingRoles(String path, String... roles) {
    return mvc.perform(get(path)
            .with(user("user")
                .password("password")
                .roles(roles)))
        .andReturn();
  }

  private void checkPathCanBeAccessedWithRole(String path, String... roles) {
    MvcResult result = accessPathUsingRoles(path, roles);
    assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
  }

  private void checkPathCannotBeAccessedWithoutCorrectRole(String path) {
    MvcResult result = accessPathUsingRoles(path, ROLE_XXX);

    // We should get 403 FORBIDDEN, when we fail the security checks
    assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
  }

  /**
   * In standalone profile, we have paths mapped to roles but, the controllers associated with those
   * paths are not registered. This class registers a controller for /admin/** and /welcome paths.
   * This allows us to test path protections.
   */
  @TestConfiguration
  @RestController
  static class ProtectedPathsController {

    @GetMapping({"/admin", "/admin/**"})
    String adminPathsHandler(HttpServletRequest request) {
      String suffix = TestUtils.stripPrefix("/admin", request.getPathInfo());
      log.info("path suffix [{}]", suffix);
      return suffix;
    }

    @GetMapping("/welcome")
    String welcomePathHandler() {
      return "WELCOME";
    }
  }
}
