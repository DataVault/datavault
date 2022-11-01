package org.datavaultplatform.broker.services;

import static java.nio.file.Files.createTempDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.datavaultplatform.common.docker.DockerImage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

/**
 * This test generates a key pair and checks that the keypair is valid by ...
 * regenerating the rsa public key from the private key only and
 * checking the regenerated public is the same as the original public key.
 */
@Slf4j
public class UserKeyPairService1IT extends BaseUserKeyPairServiceTest {

  public static final String TEST_PASSPHRASE = "tenet";

  static Path TMP_DIR;
  static GenericContainer<?> linuxWithOpenSSL;

  /**
   * Tests that the key pair is valid by
   * regenerating public key from private key
   */
  @Test
  @Override
  @SneakyThrows
  void testKeyPair() {
    UserKeyPairService service = new UserKeyPairService(TEST_PASSPHRASE);
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

    // just check that we can re-create a key-pair from the private-key and public-key and we get the same fingerprint
    KeyPair kp2 = getKeyPair(info);
    assertEquals(info.getFingerPrint(), kp2.getFingerPrint());
    assertEquals(UserKeyPairService.PUBKEY_COMMENT, kp2.getPublicKeyComment());
    assertEquals(KeyPair.RSA, kp2.getKeyType());

    checkPrivateAndPublicKeyPairing(info);
  }

  /**
   * We regenerate the public key from the encrypted private key and check we get same public key.
   * This proves that the private key and public key are paired correctly.
   *
   * @param info
   * @throws JSchException
   */
  private void checkPrivateAndPublicKeyPairing(KeyPairInfo info) throws JSchException {

    //first write the two keys to files within the nginx container which has openssl
    File tempRSA = writeToFileInTmpDirectory("rsa", info.getPrivateKey());

    linuxWithOpenSSL.copyFileToContainer( MountableFile.forHostPath(tempRSA.getAbsolutePath()), "/tmp/dv5-temp/rsa");

    execInContainer(linuxWithOpenSSL, "ls rsa", "ls -l /tmp/dv5-temp/rsa");
    execInContainer(linuxWithOpenSSL, "cat rsa", "cat /tmp/dv5-temp/rsa");
    //we use openssl to decrypt the encrypted private key (easier than doing it in Java)
    String decrypted = decryptPrivateKey();

    // create a new keypair from the decrypted private key only (private key should contains all public key information)
    KeyPair keyPair1 = KeyPair.load(new JSch(), decrypted.getBytes(StandardCharsets.UTF_8), null);

    ByteArrayOutputStream publicStream1 = new ByteArrayOutputStream();
    keyPair1.writePublicKey(publicStream1, UserKeyPairService.PUBKEY_COMMENT);
    String publicKey1 = new String(publicStream1.toByteArray(), StandardCharsets.UTF_8);

    //check that the re-created public key is same as original public key
    //this proves that the original private/public key pair are matchd
    assertEquals(info.getPublicKey(), publicKey1);
  }

  @SneakyThrows
  private String decryptPrivateKey() {

    String command = "openssl rsa -in /tmp/dv5-temp/rsa -passin pass:" + TEST_PASSPHRASE +" -out /tmp/dv5-temp/rsa.decrypted";
    execInContainer(linuxWithOpenSSL, "decrypr private key", command);

    File file = new File(TMP_DIR.toFile(), "rsa.decrypted");
    file.createNewFile();

    linuxWithOpenSSL.copyFileFromContainer("/tmp/dv5-temp/rsa.decrypted", file.getAbsolutePath());
    String decryptedPrivateKey = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

    return decryptedPrivateKey;
  }


  private KeyPair getKeyPair(KeyPairInfo info) throws JSchException {
    return KeyPair.load(new JSch(),
        info.getPrivateKey().getBytes(StandardCharsets.UTF_8),
        info.getPublicKey().getBytes(StandardCharsets.UTF_8));
  }

  @SneakyThrows
  private File writeToFileInTmpDirectory(String filename, String contents) {
    File file = new File(TMP_DIR.toFile(), filename);
    try (FileWriter fw = new FileWriter(file)) {
      fw.write(contents);
    }
    return file;
  }

  @BeforeAll
  static void setup() {
    try {
      TMP_DIR = createTempDirectory("dv-tmp");
      log.info("TMP_DIR IS {}", TMP_DIR);
      linuxWithOpenSSL = new GenericContainer<>(DockerImage.NGINX_IMAGE);
      linuxWithOpenSSL.start();
      execInContainer(linuxWithOpenSSL, "mk temp dir", "mkdir -p /tmp/dv5-temp");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterAll
  static void tearDown() throws IOException {
    linuxWithOpenSSL.stop();
  }
}
