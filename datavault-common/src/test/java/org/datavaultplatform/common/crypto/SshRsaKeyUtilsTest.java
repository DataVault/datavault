package org.datavaultplatform.common.crypto;

import java.io.File;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Takes example, Base64 encoded, RSA public/private key pair - formatted (for ssh/sftp)
 * and checks we can parse then correctly using SshShaKeyUtils.
 * The **test** files are base64 encoded -
 * to (hopefully) stop GitHub security scan thinking we are leaking important ssh private keys.
 * The test keys were generated via UserKeyPairService using JSch on 19thAugust2022.
 * All datavault SSH public keys, for SFTP, generated before 19thAugust2022, should be of this format.
 *
 */
public class SshRsaKeyUtilsTest {

  @Test
  @SneakyThrows
  void testParseKeyFiles() {
    ClassPathResource privateKeyResource = new ClassPathResource("example-ssh-keys/encodedPrivateKey.txt");
    File privateKeyFile = privateKeyResource.getFile();

    ClassPathResource publicKeyResource = new ClassPathResource("example-ssh-keys/encodedPublicKey.txt");
    File publicKeyFile = publicKeyResource.getFile();

    String rawPrivateKey = FileUtils.readFileToString(privateKeyFile, StandardCharsets.UTF_8);
    String rawPublicKey = FileUtils.readFileToString(publicKeyFile, StandardCharsets.UTF_8);

    String publicKey = decode(rawPublicKey);
    System.out.println(publicKey);
    String privateKey = decode(rawPrivateKey);
    System.out.println(privateKey);

    Assertions.assertTrue(privateKey.startsWith("-----BEGIN RSA PRIVATE KEY-----"));
    Assertions.assertTrue(privateKey.contains("Proc-Type: 4,ENCRYPTED"));
    Assertions.assertTrue(privateKey.contains("DEK-Info: DES-EDE3-CBC,"));
    Assertions.assertTrue(privateKey.endsWith("-----END RSA PRIVATE KEY-----"));

    Assertions.assertTrue(publicKey.startsWith("ssh-rsa AAAA"));
    Assertions.assertTrue(publicKey.endsWith("== datavault"));


    RSAPrivateKey rsaPrivateKey = SshRsaKeyUtils.readPrivateKey(privateKey, "tenet");
    RSAPublicKey rsaPublicKey = SshRsaKeyUtils.readPublicKey(publicKey);
    BigInteger expectedModulus = new BigInteger(
        "145819497446504665407052454873049578702605434271985535289477974800824103967370144831619755390946889058701098955910318621017852918758875105210827549183463503443000501645102031182177258964161879484467749566840583633960627631427812469962241441974819543825063777374868339736348045780148884786447901424407496204987");
    Assertions.assertEquals(expectedModulus, rsaPrivateKey.getModulus());
    Assertions.assertEquals(rsaPublicKey.getModulus(), rsaPrivateKey.getModulus());
  }

  @SneakyThrows
  private String decode(String encoded) {

    String temp = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());

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
    Assertions.assertEquals(plainText1, plainText2);
  }

}
