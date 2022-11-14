package org.datavaultplatform.broker.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.config.BaseExternalPropertyFileConfigTest;
import org.datavaultplatform.common.crypto.Encryption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * The WebApp will read from external properties files.
 * The location of the external properties files is based on environmental variables:
 * HOME, DATAVAULT_HOME and DATAVAULT_ETC
 * This test:
 *   creates some temp directories,
 *   create properties files in the temp directories
 *   sets ENV variables(HOME, DEFAULT_HOME and DATAVAULT_ETC) to the temp directories,
 *   checks that the application can read the properties via PropertiesConfig using ENV variables
 */
@SpringBootTest(classes = DataVaultBrokerApp.class)
@Slf4j
@DirtiesContext
@EnableAutoConfiguration(exclude= {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class })
@TestPropertySource(properties = {
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.services.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.initialise.enabled=false",
    "broker.database.enabled=false",
    "keystore.password=test-keystore-dummy-password"})
@AddTestProperties
@Import(MockServicesConfig.class) //spring security relies on services
public class EncryptionConfigTest extends BaseExternalPropertyFileConfigTest {

  @Autowired
  Encryption encryption;


  @Test
  void checkCommonProperties() {
    assertEquals(123, Encryption.getEncBufferSize());
  }

  @Test
  void checkKeyStoreProperties() {
    assertFalse(Encryption.getKeystoreEnable());
    assertEquals("test-keystore-dummy-password", Encryption.getKeystorePassword());
    assertEquals("test-keystore-path", Encryption.getKeystorePath());
  }

  @Test
  void checkHashicorpVaultProperties() {
    assertFalse(Encryption.getVaultEnable());
    assertEquals("test-hc-vault-secret-path", Encryption.getVaultKeyPath());
    assertEquals("test-hc-vault-address", Encryption.getVaultAddress());
    assertEquals("test-hc-vault-token", Encryption.getVaultToken());
    assertEquals("test-hc-vault-ssl-pem-path", Encryption.getVaultSslPEMPath());
    assertEquals("test-hc-vault-data-enc-key-name", Encryption.getVaultDataEncryptionKeyName());
    assertEquals("test-hc-vault-private-key-enc-key-name", Encryption.getVaultPrivateKeyEncryptionKeyName());
  }

  @Test
  void testBouncyCastleProvider() {
    Optional<Provider> optBouncyCastle = Arrays.stream(Security.getProviders())
        .filter(prov -> prov instanceof BouncyCastleProvider)
        .findFirst();
    assertThat(optBouncyCastle).isPresent();
  }
}
