package org.datavaultplatform.common.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.services.LDAPService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.junit.jupiter.Container;

/*
 The LDAPService is defined in datavault-commons but we have to test it in broker because are not just testing LDAPService functionality
 but also that it's wired up/configured correctly within the datavault-broker too.
 This test class uses OpenLdap in a testcontainer with users add via an '.ldif' file.
 */

@Slf4j
public abstract class BaseLDAPServiceTest {

  /*
  DataVault does NOT authenticate/bind to LDAP using the admin user - it uses 'uid=uun,ou=people,o=myu.ed'
   */
  public static final String LDAP_ADMIN_PASSWORD = "test-password";

  private static final int LDAP_EXPOSED_PORT = 1389;

  @Container
  private static final GenericContainer<?> LDAP_CONTAINER = new GenericContainer<>(
      "bitnami/openldap:latest")
      .withEnv("LDAP_ROOT", "o=myu.ed")
      .withEnv("LDAP_ADMIN_PASSWORD", LDAP_ADMIN_PASSWORD)
      //SCHEMA - allows 'eduniRefNo' attributes - via LDIF file
      .withClasspathResourceMapping("ldap/eduniPersonSchema.ldif", "/schema/custom.ldif",
          BindMode.READ_ONLY)
      //USERS via LDIF file
      .withClasspathResourceMapping("ldap/testUsers.ldif", "/custom/testUsers.ldif",
          BindMode.READ_ONLY)
      .withEnv("LDAP_CUSTOM_LDIF_DIR", "/custom")
      .withExposedPorts(LDAP_EXPOSED_PORT)
      .withStartupCheckStrategy(
          //Gotta allow time for openldap to initialise
          new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(2))
      );


  @Autowired
  LDAPService ldapService;

  @Value("${ldap.enabled}")
  private String ldapEnabled;

  @Value("${ldap.useSsl}")
  private String ldapUseSSL;

  @Value("${ldap.searchFilter}")
  private String ldapSearchFilter;

  @Value("${ldap.searchContext}")
  private String ldapSearchContext;

  @Value("${ldap.dn}")
  private String ldapDN;

  @Value("${ldap.password}")
  private String ldapPassword;

  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) {
    registry.add("ldap.host", LDAP_CONTAINER::getHost);
    registry.add("ldap.port", () -> LDAP_CONTAINER.getMappedPort(LDAP_EXPOSED_PORT));
    registry.add("ldap.attrs", () -> "uid, mail, cn, mail"); //add duplicates which are filtered out
  }

  @BeforeEach
  void setup() {
    assertNotNull(this.ldapService);
    assertEquals("true", ldapEnabled);
    assertEquals("false", ldapUseSSL);
    assertEquals("uid", ldapSearchFilter);
    assertEquals("ou=people,o=myu.ed", ldapSearchContext);
    assertEquals("uid=uun,ou=people,o=myu.ed", ldapDN);
    assertEquals("hello123", ldapPassword);
  }

  @Test
  void testAutoCompleteBasedOnSurname() throws CursorException, IOException, LdapException {
    log.info("OPENLDAP PORT [{}]", LDAP_CONTAINER.getMappedPort(LDAP_EXPOSED_PORT));
    log.info("OPENLDAP USERNAME [{}]", ldapDN);
    log.info("OPENLDAP PASSWORD [{}]", ldapPassword);

    List<String> info = ldapService.autocompleteUID("Bond");
    assertEquals(Arrays.asList("bbond - Basildon Bond", "jamesbond - James Bond"), info);
  }

  @Test
  void testAutoCompleteBasedOnFirstName() throws CursorException, IOException, LdapException {
    List<String> info = ldapService.autocompleteUID("Basildon");
    assertEquals(Collections.singletonList("bbond - Basildon Bond"), info);
  }

  @Test
  void testAutoCompleteBasedOnUID() throws CursorException, IOException, LdapException {
    List<String> info = ldapService.autocompleteUID("jamesbond");
    assertEquals(Collections.singletonList("jamesbond - James Bond"), info);
  }


  @Test
  void testAutoCompleteBasedOnPartialUID() throws CursorException, IOException, LdapException {
    List<String> info = ldapService.autocompleteUID("jamesbo");
    assertEquals(Arrays.asList("jamesbond - James Bond"), info);
  }

  @Test
  void testAutoCompleteBasedOnPartialSurname()
      throws CursorException, IOException, LdapException {
    List<String> info = ldapService.autocompleteUID("Blog");
    assertEquals(Collections.singletonList("joebloggs - Joe Bloggs"), info);
  }


  @Test
  void testUserInfoJamesBond() throws CursorException, IOException, LdapException {
    HashMap<String, String> info = ldapService.getLdapUserInfo("jamesbond");
    assertEquals(4, info.size());
    assertEquals("jamesbond", info.get("uid"));
    assertEquals("james.bond@test.com", info.get("mail"));
    assertEquals("James Bond", info.get("cn"));
    assertEquals("v1jbond1", info.get("edunirefno"));
  }

  @Test
  void testUserInfoBasildonBond() throws CursorException, IOException, LdapException {
    HashMap<String, String> info = ldapService.getLdapUserInfo("bbond");
    assertEquals(4, info.size());
    assertEquals("bbond", info.get("uid"));
    assertEquals("basildon.bond@test.com", info.get("mail"));
    assertEquals("Basildon Bond", info.get("cn"));
    assertEquals("v1bbond2", info.get("edunirefno"));
  }

  @Test
  void testUserInfoJoeBloggs() throws CursorException, IOException, LdapException {
    HashMap<String, String> info = ldapService.getLdapUserInfo("joebloggs");
    assertEquals(4, info.size());
    assertEquals("joebloggs", info.get("uid"));
    assertEquals("joe.bloggs@test.com", info.get("mail"));
    assertEquals("Joe Bloggs", info.get("cn"));
    assertEquals("v1jbloggs3", info.get("edunirefno"));
  }

  @Test
  void testLDAPAttributesJamesBond() throws CursorException, IOException, LdapException {
    HashMap<String, String> info = ldapService.getLDAPAttributes("jamesbond");
    assertEquals(3, info.size());
    assertEquals("jamesbond", info.get("uid"));
    assertEquals("james.bond@test.com", info.get("mail"));
    assertEquals("James Bond", info.get("cn"));
  }

  @Test
  void testLDAPAttributesJoeBloggs() throws CursorException, IOException, LdapException {
    HashMap<String, String> info = ldapService.getLDAPAttributes("joebloggs");
    Assertions.assertEquals(3, info.size());
    assertEquals("joebloggs", info.get("uid"));
    assertEquals("joe.bloggs@test.com", info.get("mail"));
    assertEquals("Joe Bloggs", info.get("cn"));
  }


}
