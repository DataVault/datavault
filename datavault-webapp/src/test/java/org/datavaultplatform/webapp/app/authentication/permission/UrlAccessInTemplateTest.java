package org.datavaultplatform.webapp.app.authentication.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Checks that the Spring Security authorize Jsp Tag in freemarker templates will evaluate the
 * whether the current user has access to a specified url.
 * For example '<@sec.authorize url="/admin/retentionpolicies/">'
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ProfileStandalone
public class UrlAccessInTemplateTest {

  public static final String ROLE_ADMIN_RETENTION_POLICIES = "ADMIN_RETENTIONPOLICIES";
  public static final String ROLE_ADMIN_EVENTS = "ADMIN_EVENTS";

  public static final String ID_ADMIN_EVENTS = "admin-events";
  public static final String ID_ADMIN_RETENTION_POLICIES = "admin-retention-policies";

  @Autowired
  MockMvc mvc;

  @SneakyThrows
  Document getDocument() {
    MvcResult result = mvc.perform(get("/test/info/template"))
        .andExpect(status().is2xxSuccessful())
        .andReturn();

    return Jsoup.parse(result.getResponse().getContentAsString());
  }

  @WithMockUser(username = "tuser1", roles = {"ONE", "TWO"})
  @SneakyThrows
  @Test
  void testNoAccessToEitherUrl() {
    Document doc = getDocument();

    checkUser(doc, "tuser1");

    checkNoAccessToAdminEvents(doc);

    checkNoAccessToAdminRetentionPolicies(doc);
  }

  @WithMockUser(username = "tuser2", roles = {"ONE", "TWO", ROLE_ADMIN_EVENTS})
  @SneakyThrows
  @Test
  void testAccessToAdminEventsUrl() {
    Document doc = getDocument();

    checkUser(doc, "tuser2");

    checkAccessToAdminEvents(doc);

    checkNoAccessToAdminRetentionPolicies(doc);
  }

  @WithMockUser(username = "tuser3", roles = {"ONE", "TWO", ROLE_ADMIN_RETENTION_POLICIES})
  @SneakyThrows
  @Test
  void testAccessToAdminRetentionPoliciesUrl() {
    Document doc = getDocument();

    checkUser(doc, "tuser3");

    checkNoAccessToAdminEvents(doc);

    checkAccessToAdminRetentionPolicies(doc);
  }

  @WithMockUser(username = "tuser4", roles = {"ONE", "TWO", ROLE_ADMIN_RETENTION_POLICIES,
      ROLE_ADMIN_EVENTS})
  @SneakyThrows
  @Test
  void testAccessToBothAdminUrls() {
    Document doc = getDocument();

    checkUser(doc, "tuser4");

    checkAccessToAdminEvents(doc);

    checkAccessToAdminRetentionPolicies(doc);
  }

  void checkNoAccessToAdminEvents(Document doc) {
    assertNull(doc.getElementById(ID_ADMIN_EVENTS));
  }

  void checkNoAccessToAdminRetentionPolicies(Document doc) {
    assertNull(doc.getElementById(ID_ADMIN_RETENTION_POLICIES));
  }

  void checkAccessToAdminEvents(Document doc) {
    assertEquals("ACCESS TO /admin/events",
        doc.getElementById(ID_ADMIN_EVENTS).text());
  }

  void checkAccessToAdminRetentionPolicies(Document doc) {
    assertEquals("ACCESS TO /admin/retentionpolicies",
        doc.getElementById(ID_ADMIN_RETENTION_POLICIES).text());
  }

  void checkUser(Document doc, String expectedUserName) {
    Element user = doc.getElementById("user");
    assertEquals("AUTHENTICATED:" + expectedUserName, user.text());
  }

  @TestConfiguration
  @Controller
  static class InfoController {

    @GetMapping("/test/info/template")
    String getTemplate() {
      return "test/securityTagHasAccessToUrl";
    }
  }

}
