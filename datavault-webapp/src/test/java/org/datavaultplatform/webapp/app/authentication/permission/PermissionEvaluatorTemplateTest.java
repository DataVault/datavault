package org.datavaultplatform.webapp.app.authentication.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.Serializable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ProfileStandalone
/**
 * Checks that the SpringSecurity authorize Jsp Tag in freemarker templates
 * will delegate to the registered PermissionEvaluator bean when the authorize tag
 * uses hasPermission.
 * This test uses a mock PermissionEvaluator bean - we are not testing
 * the actual PermissionEvaluator - just that it gets called correctly.
 * For Example :
 * '
 *  <#assign secExpression = "hasPermission('${targetId}', 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES')">
 *  <@sec.authorize access=secExpression>
 * '
 */
class PermissionEvaluatorTemplateTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  PermissionEvaluator mPermissionEvaluator;

  @Captor
  ArgumentCaptor<Authentication> argAuth;

  @Captor
  ArgumentCaptor<Serializable> argTargetId;

  @Captor
  ArgumentCaptor<String> argTargetType;

  @Captor
  ArgumentCaptor<Object> argPermission;

  @SneakyThrows
  MvcResult getTemplate(String targetId) {
    return mvc.perform(get("/test/info/template/" + targetId))
        .andReturn();
  }

  void configureEvalautor(boolean evalResult){
    when(mPermissionEvaluator.hasPermission(
        argAuth.capture(),
        argTargetId.capture(),
        argTargetType.capture(),
        argPermission.capture()
    )).thenReturn(evalResult);
  }

  @ParameterizedTest
  @ValueSource(strings = {"target-id-one","target-id-two"})
  @WithMockUser(username="testuser")
  @SneakyThrows
  void testDoesHavePermission(String targetId) {
    configureEvalautor(true);
    MvcResult result = getTemplate(targetId);
    assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    Document doc = Jsoup.parse(result.getResponse().getContentAsString());
    assertThat(doc.getElementById("targetId").text()).isEqualTo("["+targetId+"]");
    assertThat(doc.getElementById("result").text()).isEqualTo("DOES HAVE PERMISSION");

    verifyHasPermissionCalled(targetId);
  }

  @ParameterizedTest
  @ValueSource(strings = {"target-id-one","target-id-two"})
  @WithMockUser(username="testuser")
  @SneakyThrows
  void testDoesNotHavePermission(String targetId) {
    configureEvalautor(false);
    MvcResult result = getTemplate(targetId);
    assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    Document doc = Jsoup.parse(result.getResponse().getContentAsString());
    assertThat(doc.getElementById("targetId").text()).isEqualTo("["+targetId+"]");
    //we don't have permission - so no result div!
    assertThat(doc.getElementById("result")).isNull();
    verifyHasPermissionCalled(targetId);
  }

  private void verifyHasPermissionCalled(String targetId) {
    assertEquals("testuser", argAuth.getValue().getName());
    assertEquals(targetId, argTargetId.getValue());
    assertEquals("vault", argTargetType.getValue());
    assertEquals("VIEW_DEPOSITS_AND_RETRIEVES", argPermission.getValue());

    verify(mPermissionEvaluator).hasPermission(
        argAuth.getValue(),
        argTargetId.getValue(),
        argTargetType.getValue(),
        argPermission.getValue());
  }

  @AfterEach
  void verifyNoMoreCalls() {
    verifyNoMoreInteractions(mPermissionEvaluator);
  }

  /**
   * A controller that uses both 2 and 3 arg version of hasPermission in @PreAuthorize annotation.
   * These should result in calls to 3 and 4 arg version of
   * PermissionEvaluator bean's hasPermission methods.
   * Spring adds Authentication as first arg in both hasPermission method calls.
   */
  @TestConfiguration
  @Controller
  @RequestMapping("/test/info")
  static class InfoController {

    @GetMapping("/template/{targetId}")
    String getInfoSecureOne(@PathVariable String targetId, ModelMap model) {
      model.put("targetId",targetId);
      return "test/securityTagHasPermission";
    }
  }
}
