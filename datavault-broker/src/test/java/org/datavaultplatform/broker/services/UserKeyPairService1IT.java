package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.datavaultplatform.common.crypto.SshRsaKeyUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This test generates a key pair and checks that the keypair is valid by ...
 * regenerating the rsa public key from the private key only and
 * checking the regenerated public is the same as the original public key.
 */
@Slf4j
public class UserKeyPairService1IT extends BaseUserKeyPairServiceTest {

  /**
   * Tests that the key pair is valid by
   * regenerating public key from private key
   */
  @ParameterizedTest
  @MethodSource("provideUserKeyPairService")
  @Override
  @SneakyThrows
  void testKeyPair(UserKeyPairService service) {
    assertEquals(TEST_PASSPHRASE, service.getPassphrase());
    KeyPairInfo info = service.generateNewKeyPair();

    assertNotNull(info.getPublicKey());
    assertNotNull(info.getPrivateKey());

    assertNotEquals(info.getPublicKey(), info.getPrivateKey());
    assertEquals(1024, info.getKeySize());

    log.info("public  [{}]", info.getPublicKey());
    log.info("private [{}]", info.getPrivateKey());
    log.info("finger print [{}]", info.getFingerPrint());

    assertTrue(info.getPublicKey().trim().startsWith("ssh-rsa"));
    assertTrue(info.getPublicKey().trim().endsWith(UserKeyPairService.PUBKEY_COMMENT));

    assertTrue(info.getPrivateKey().trim().startsWith("-----BEGIN RSA PRIVATE KEY-----"));
    assertTrue(info.getPrivateKey().trim().endsWith("-----END RSA PRIVATE KEY-----"));

    checkPrivateAndPublicKeyPairing(info);
  }

  /**
   * We regenerate the public key from the encrypted private key and check we get same public key.
   * This proves that the private key and public key are paired correctly.
   *
   * @param info the key pair information
   */
  private void checkPrivateAndPublicKeyPairing(KeyPairInfo info) {

    RSAPrivateKey rsaPrivateKey = SshRsaKeyUtils.readPrivateKey(
        info.getPrivateKey(), TEST_PASSPHRASE);

    RSAPublicKey rsaPublicKey = (RSAPublicKey) SshRsaKeyUtils.getKeyPairFromRSAPrivateKey(rsaPrivateKey).getPublic();
    String publicKey1 = SshRsaKeyUtils.encodePublicKey(rsaPublicKey, UserKeyPairService.PUBKEY_COMMENT);

    //check that the re-created public key is same as original public key
    //this proves that the original private/public key pair are matched
    assertEquals(info.getPublicKey(), publicKey1);
  }


}
