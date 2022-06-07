package org.datavaultplatform.webapp.ldap;

import org.datavaultplatform.common.ldap.BaseLDAPServiceTest;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = DataVaultWebApp.class)
@ProfileDatabase
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
/*
 The LDAPService is defined in datavault-commons but we have to test it in -broker because are not just testing LDAPService functionality
 but also that it's wired up/configured correctly within the datavault-broker too.
 This test class uses OpenLdap in a testcontainer with users add via an '.ldif' file.
 */
public class LDAPServiceTest extends BaseLDAPServiceTest {

}
