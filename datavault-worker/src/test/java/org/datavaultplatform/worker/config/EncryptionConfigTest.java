package org.datavaultplatform.worker.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.datavaultplatform.common.config.BaseExternalPropertyFileConfigTest;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.queue.EventSender;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@Slf4j
@DirtiesContext
@TestPropertySource(properties = {
    "worker.rabbit.enabled=false",
    "keystore.password=test-keystore-dummy-password"})
@AddTestProperties
public class EncryptionConfigTest extends BaseExternalPropertyFileConfigTest {

  @Autowired
  Encryption encryption;

  @MockBean
  //we have to mock this because it depends on Rabbit which we've not configured
  EventSender mEventSender;

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
