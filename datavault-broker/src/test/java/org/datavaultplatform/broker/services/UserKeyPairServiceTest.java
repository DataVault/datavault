package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class UserKeyPairServiceTest {

  @Value("${sftp.passphrase}")
  String passphrase;

  @Test
  @SneakyThrows
  void testKeyPairGeneration() {
      String passphase = "tenet";
      UserKeyPairService service = new UserKeyPairService(passphase);
      assertEquals(passphase, service.getPassphrase());
      KeyPairInfo info = service.generateNewKeyPair();

      assertNotNull(info.getPublicKey());
      assertNotNull(info.getPrivateKey());

      assertNotEquals(info.getPublicKey(), info.getPrivateKey());

      log.info("public  [{}]", info.getPublicKey());
      log.info("private [{}]", info.getPrivateKey());
      log.info("finger print [{}]", info.getFingerPrint());

      assertTrue(info.getPublicKey().trim().startsWith("ssh-rsa"));
      assertTrue(info.getPublicKey().trim().endsWith(UserKeyPairService.PUBKEY_COMMENT));

      assertTrue(info.getPrivateKey().trim().startsWith("-----BEGIN RSA PRIVATE KEY-----"));
      assertTrue(info.getPrivateKey().trim().endsWith("-----END RSA PRIVATE KEY-----"));

      // just check that we can re-create a key-pair from the private-key and public-key and we get the same fingerprint
      KeyPair kp2 = getKeyPair(info);
      assertEquals(info.getFingerPrint(), kp2.getFingerPrint());
      assertEquals(UserKeyPairService.PUBKEY_COMMENT, kp2.getPublicKeyComment());
      assertEquals(KeyPair.RSA, kp2.getKeyType());

      //TODO - take some plaintext and use public key to encrypt,
      // followed by use of private key to decrypt back to original plaintext
  }

  private KeyPair getKeyPair(KeyPairInfo info) throws JSchException {
    return KeyPair.load(new JSch(),
        info.getPrivateKey().getBytes(StandardCharsets.UTF_8),
        info.getPublicKey().getBytes(StandardCharsets.UTF_8));
  }
}
