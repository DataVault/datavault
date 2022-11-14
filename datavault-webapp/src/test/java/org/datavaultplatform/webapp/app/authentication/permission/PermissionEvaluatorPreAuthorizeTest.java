package org.datavaultplatform.webapp.app.authentication.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ProfileStandalone
@Slf4j
/**
 * Checks that the SpringSecurity @PreAuthorization annotation in Java code
 * will delegate to the registered PermissionEvaluator bean when the annotation
 * uses hasPermission.
 * This test uses a mock PermissionEvaluator bean - we are not testing
 * the actual PermissionEvaluator - just that it gets called correctly.
 */
class PermissionEvaluatorPreAuthorizeTest {
  
  @Autowired
  MockMvc mvc;

  @MockBean
  PermissionEvaluator mPermissionEvaluator;

  @Nested
  class Unsecure {

    @ParameterizedTest
    @CsvSource({"1,info_one", "2,info_two", "3,info_three"})
    @SneakyThrows
    void testUnsecure(String id, String expectedInfoName) {
      mvc.perform(get("/test/info/unsecure/" + id))
          .andExpect(status().is2xxSuccessful())
          .andExpect(content().string(expectedInfoName));
    }
  }

  @Nested
  class Secure {

    @SneakyThrows
    MvcResult getPathOne(String id) {
      return mvc.perform(get("/test/info/secure/one/" + id))
          .andReturn();
    }

    void checkNotAuthenticatedPathOne() {
      MvcResult result = getPathOne("1");
      assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
      assertEquals("http://localhost/auth/login", result.getResponse().getRedirectedUrl());
    }

    @SneakyThrows
    void checkAllowedPathOne() {
      MvcResult result = getPathOne("1");
      assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
      assertEquals("info_one", result.getResponse().getContentAsString());
    }

    @Test
    void testNotAuthenticatedNoUser() {
      checkNotAuthenticatedPathOne();
    }

    @Test
    @WithAnonymousUser
    void testNotAuthenticatedAnonUser() {
      checkNotAuthenticatedPathOne();
    }

    /**
     * Contains tests for both the 3 and 4 arg version of PermissionEvaluator.hasPermission
     */
    @Nested
    @WithMockUser(username = "testuser", password = "testpassword")
    class UsingPermissionEvaluator {

      @Captor
      ArgumentCaptor<Authentication> argAuth;

      @Captor
      ArgumentCaptor<Object> argPermission;

      @BeforeEach
      void resetMocks() {
        Mockito.reset(mPermissionEvaluator);
      }

      @AfterEach
      void verifyNoMoreCalls() {
        //verify no more calls to permission evaluator
        verifyNoMoreInteractions(mPermissionEvaluator);
      }

      /**
       * Checks that the PermissionEvaluator.hasPermission with 3 args is called as expected.
       */
      @Nested
      class PathOneUsesHasPersmissionFourArgs {

        @Captor
        ArgumentCaptor<Serializable> argTargetId;

        @Captor
        ArgumentCaptor<String> argTargetType;

        void checkDeniedOne() {
          MvcResult result = getPathOne("1");
          assertEquals("/auth/denied", result.getResponse().getForwardedUrl());
          assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
        }

        @Test
        void testEvaluatorAllows() {

          //setup the mock permission evaluator to return true
          setupEvaluator(true);

          //invoke the endpoint - should invoke permission evaluator - and be allowed
          checkAllowedPathOne();

          checkEvaluatorWasCalledWithExpectedArgs();

        }

        @Test
        void testEvaluatorDenies() {

          //setup the mock permission evaluator to return true
          setupEvaluator(false);

          //invoke the endpoint - should invoke permission evaluator - and be denied
          checkDeniedOne();

          checkEvaluatorWasCalledWithExpectedArgs();

          //verify no more calls to permission evaluator
          verifyNoMoreInteractions(mPermissionEvaluator);
        }

        void setupEvaluator(boolean evaluatorResult) {
          when(mPermissionEvaluator.hasPermission(
              argAuth.capture(),
              argTargetId.capture(),
              argTargetType.capture(),
              argPermission.capture())).thenReturn(evaluatorResult);
        }

        private void checkEvaluatorWasCalledWithExpectedArgs() {
          assertEquals("testuser", argAuth.getValue().getName());
          assertEquals(Integer.valueOf(1), argTargetId.getValue());

          assertEquals("java.lang.String", argTargetType.getValue());

          assertEquals("READ", argPermission.getValue());

          verify(mPermissionEvaluator).hasPermission(
              argAuth.getValue(),
              argTargetId.getValue(),
              argTargetType.getValue(),
              argPermission.getValue());
        }
      }

      /**
       * Checks that the PermissionEvaluator.hasPermission with 3 args is called as expected.
       */
      @Nested
      class PathTwoUsesHasPermissionThreeArgs {

        @SneakyThrows
        MvcResult getPathTwo() {
          return mvc.perform(get("/test/info/secure/two/exampleDomainObj"))
              .andReturn();
        }

        @SneakyThrows
        void checkAllowedPathTwo() {
          MvcResult result = getPathTwo();
          assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
          assertEquals("result-exampleDomainObj", result.getResponse().getContentAsString());
        }

        @Captor
        ArgumentCaptor<Object> argTargetDomainObject;

        void checkDenied() {
          MvcResult result = getPathTwo();
          assertEquals("/auth/denied", result.getResponse().getForwardedUrl());
          assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
        }

        @Test
        void testEvaluatorAllows() {

          //setup the mock permission evaluator to return true
          setupEvaluator(true);

          //invoke the endpoint - should invoke permission evaluator - and be allowed
          checkAllowedPathTwo();

          checkEvaluatorWasCalledWithExpectedArgs();

        }

        @Test
        void testEvaluatorDenies() {

          //setup the mock permission evaluator to return true
          setupEvaluator(false);

          //invoke the endpoint - should invoke permission evaluator - and be denied
          checkDenied();

          checkEvaluatorWasCalledWithExpectedArgs();

          //verify no more calls to permission evaluator
          verifyNoMoreInteractions(mPermissionEvaluator);
        }

        void setupEvaluator(boolean evaluatorResult) {
          when(mPermissionEvaluator.hasPermission(
              argAuth.capture(),
              argTargetDomainObject.capture(),
              argPermission.capture())).thenReturn(evaluatorResult);
        }

        private void checkEvaluatorWasCalledWithExpectedArgs() {
          assertEquals("testuser", argAuth.getValue().getName());
          assertEquals("exampleDomainObj", argTargetDomainObject.getValue());
          assertEquals("PERMISSION_ONE", argPermission.getValue());

          verify(mPermissionEvaluator).hasPermission(
              argAuth.getValue(),
              argTargetDomainObject.getValue(),
              argPermission.getValue());
        }
      }
    }
  }

  /**
   * A controller that uses both 2 and 3 arg version of hasPermission in @PreAuthorize annotation.
   * These should result in calls to 3 and 4 arg version of
   * PermissionEvaluator bean's hasPermission methods.
   * Spring adds Authentication as first arg in both hasPermission method calls.
   */
  @RestController
  @TestConfiguration
  @RequestMapping("/test/info")
  static class InfoController {

    Map<Integer,String> info = new HashMap<Integer,String>(){{
      put(1, "info_one");
      put(2, "info_two");
      put(3, "info_three");
    }};

    @GetMapping("/unsecure/{id}")
    String getInfoUnsecure(@PathVariable int id) {
      return info.get(id);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id,'java.lang.String','READ')")
    @GetMapping("/secure/one/{id}")
    String getInfoSecureOne(@PathVariable int id) {
      return info.get(id);
    }

    @GetMapping("/secure/two/{domain}")
    @PreAuthorize("isAuthenticated() and hasPermission(#domain,'PERMISSION_ONE')")
    String getInfoSecureTwo(@PathVariable String domain) {
      return "result-"+domain;
    }
  }
}
