package org.datavaultplatform.webapp.app.setup;

import static org.assertj.core.api.Assertions.assertThat;

import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.session.SessionRegistry;

@SpringBootTest
@ProfileStandalone
public class SessionRegistrySetupTest {

  /**
   * We test the name of the bean here - it was explicitly set to 'sessionRegistry' in the non-boot DataVaultWebApp
   */
  @Test
  void testSessionRegistryBean(ApplicationContext ctx) {
    SessionRegistry sessionRegistry = ctx.getBean("sessionRegistry", SessionRegistry.class);
    assertThat(sessionRegistry.getAllPrincipals()).isEmpty();
  }

}
