package org.datavaultplatform.common.crypto;

import java.io.File;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Takes example, Base64 encoded, RSA public/private key pair - formatted (for ssh/sftp)
 * and checks we can parse then correctly using SshShaKeyUtils.
 * The **test** files are base64 encoded -
 * to (hopefully) stop GitHub security scan thinking we are leaking important ssh private keys.
 * The test keys were generated via UserKeyPairService using JSch on 19thAugust2022.
 * All datavault SSH public keys, for SFTP, generated before 19thAugust2022, should be of this format.
 *
 */
@Slf4j
public class SshRsaKeyUtilsTest {

  private String testPublicKey;
  private String testPrivateKey;

  @BeforeEach
  @SneakyThrows
  void setup() {
    ClassPathResource privateKeyResource = new ClassPathResource("example-ssh-keys/encodedPrivateKey.txt");
    File privateKeyFile = privateKeyResource.getFile();

    ClassPathResource publicKeyResource = new ClassPathResource("example-ssh-keys/encodedPublicKey.txt");
    File publicKeyFile = publicKeyResource.getFile();

    String rawPrivateKey = FileUtils.readFileToString(privateKeyFile, StandardCharsets.UTF_8);
    String rawPublicKey = FileUtils.readFileToString(publicKeyFile, StandardCharsets.UTF_8);

    testPublicKey = decode(rawPublicKey);
    log.info("public key[{}]", testPublicKey);
    testPrivateKey = decode(rawPrivateKey);
    log.info("private key[{}]", testPrivateKey);
  }

  @Test
  @SneakyThrows
  void testParseKeyFiles() {

    Assertions.assertTrue(testPrivateKey.startsWith("-----BEGIN RSA PRIVATE KEY-----"));
    Assertions.assertTrue(testPrivateKey.contains("Proc-Type: 4,ENCRYPTED"));
    Assertions.assertTrue(testPrivateKey.contains("DEK-Info: DES-EDE3-CBC,"));
    Assertions.assertTrue(testPrivateKey.endsWith("-----END RSA PRIVATE KEY-----"));

    Assertions.assertTrue(testPublicKey.startsWith("ssh-rsa AAAA"));
    Assertions.assertTrue(testPublicKey.endsWith("== datavault"));


    RSAPrivateKey rsaPrivateKey = SshRsaKeyUtils.readPrivateKey(testPrivateKey, "tenet");
    RSAPublicKey rsaPublicKey = SshRsaKeyUtils.readPublicKey(testPublicKey);
    BigInteger expectedModulus = new BigInteger(
        "145819497446504665407052454873049578702605434271985535289477974800824103967370144831619755390946889058701098955910318621017852918758875105210827549183463503443000501645102031182177258964161879484467749566840583633960627631427812469962241441974819543825063777374868339736348045780148884786447901424407496204987");
    assertEquals(expectedModulus, rsaPrivateKey.getModulus());
    assertEquals(rsaPublicKey.getModulus(), rsaPrivateKey.getModulus());
  }

  @SneakyThrows
  private String decode(String encoded) {

    String temp = URLDecoder.decode(encoded, StandardCharsets.UTF_8);

    temp = temp.replace("\n","");

    byte[] decoded = Base64.getDecoder().decode(temp.getBytes(StandardCharsets.UTF_8));

    return new String(decoded, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  private String encode(String plainText) {

    byte[] encoded = Base64.getEncoder().encode(plainText.getBytes(StandardCharsets.UTF_8));

    return new String(encoded, StandardCharsets.UTF_8);
  }
  @Test
  void testEncodeDecode() {
    String plainText1 = UUID.randomUUID().toString();
    String plainText2 = decode(encode(plainText1));
    assertEquals(plainText1, plainText2);
  }


  @Test
  @SneakyThrows
  void testPrivateKeyFromPublicKey() {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(1024);
    KeyPair kp = generator.generateKeyPair();

    RSAPublicKey publicKey1 = (RSAPublicKey) kp.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();
    RSAPublicKey publicKey2 = (RSAPublicKey) SshRsaKeyUtils.getKeyPairFromRSAPrivateKey(privateKey).getPublic();
    assertEquals(publicKey1, publicKey2);
  }

  @Test
  @SneakyThrows
  void convertPublicKeyPairToStringAndBack() {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

    KeyPair keyPair1 = gen.generateKeyPair();
    RSAPublicKey publicKey1 = (RSAPublicKey)keyPair1.getPublic();
    RSAPrivateKey privateKey1 = (RSAPrivateKey)keyPair1.getPrivate();

    String publicKeyString = SshRsaKeyUtils.encodePublicKey(publicKey1, "comment");
    String privateKeyString  = SshRsaKeyUtils.encodePrivateKey(privateKey1, "password");

    RSAPrivateKey privateKey2 = SshRsaKeyUtils.readPrivateKey(privateKeyString, "password");
    RSAPublicKey publicKey2 = SshRsaKeyUtils.readPublicKey(publicKeyString);

    assertEquals(publicKey1, publicKey2);
    assertEquals(privateKey1, privateKey2);
  }

  @Test
  @SneakyThrows
  void testFingerPrint() {
    String expected = "fa:d5:b3:5f:a7:2c:bb:ea:cb:da:dd:bd:d7:6a:57:29";
    String fingerPrint = SshRsaKeyUtils.calculateFingerprint(testPublicKey);
    assertEquals(expected, fingerPrint);
  }
}
