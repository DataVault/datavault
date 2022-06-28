package org.datavaultplatform.webapp.app.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ProfileStandalone
public class LdapSetupTest {

  @Autowired
  LDAPService ldapService;

  @Test
  void testLdapServiceBean() {
    assertNotNull(ldapService);
  }

  @Test
  void testLdapServiceProperties() {
    checkField("host", "hostname");
    checkField("port", 636);
    checkField("useSsl", false);
    checkField("dn", "uid=uun,ou=people,o=myu.ed");
    checkField("password", "hello123");
    checkField("searchContext", "ou=people,o=myu.ed");
    checkField("searchFilter", "uid");
    checkField("attrs", Stream.of("attr1","attr2","etc").collect(Collectors.toSet()));
  }

  @SneakyThrows
  void checkField(String fName, Object expected) {
    Field f = LDAPService.class.getDeclaredField(fName);
    f.setAccessible(true);
    Object actual = f.get(this.ldapService);
    assertEquals(expected, actual);
  }

}
