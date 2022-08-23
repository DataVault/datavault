package org.datavaultplatform.broker.services;


import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.datavaultplatform.common.crypto.Encryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: David Hay
 * Date: 22/Aug/2022
 * Time: 16:45
 */
@Service
@Slf4j
@Transactional
public class UserKeyPairServiceImpl implements UserKeyPairService {

  static {
    Encryption.addBouncyCastleSecurityProvider();
  }

  private final String passphrase;

  private static final int KEY_SIZE = 1024;

  @Autowired
  public UserKeyPairServiceImpl(@Value("${sftp.passphrase}") String passphrase) {
    this.passphrase = passphrase;
  }

  @Override
  public String getPassphrase() {
    return passphrase;
  }

  @Override
  @SneakyThrows
  public KeyPairInfo generateNewKeyPair() {

    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(1024);

    KeyPair kp = generator.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

    String privateKeyValue = encodePrivateKey(privateKey, passphrase);

    String publicKeyValue = encodePublicKey(publicKey, PUBKEY_COMMENT);

      return KeyPairInfo.builder()
          .privateKey(privateKeyValue)
          .publicKey(publicKeyValue)
          .fingerPrint(calculateFingerprint(publicKeyValue))
          .keySize(KEY_SIZE)
          .build();

  }


  @SneakyThrows
  public static String encodePrivateKey(RSAPrivateKey privateKey, String password) {
    StringWriter sw = new StringWriter();
    JcePEMEncryptorBuilder builder = new JcePEMEncryptorBuilder("DES-EDE3-CBC");
    builder.setSecureRandom(new SecureRandom());
    PEMEncryptor pemEncryptor = builder.build(password.toCharArray());
    try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
      pemWriter.writeObject(privateKey, pemEncryptor);
    }
    return sw.toString();
  }


  @SneakyThrows
  public static String encodePublicKey(RSAPublicKey publicKey, String comment) {
    RSAKeyParameters params = new RSAKeyParameters(false, publicKey.getModulus(),
        publicKey.getPublicExponent());
    byte[] data = OpenSSHPublicKeyUtil.encodePublicKey(params);
    String publicKeyEncoded = new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8);
    return "ssh-rsa " + publicKeyEncoded + " " + comment + "\n";
  }


  /**
   * Calculate fingerprint
   *
   * @param publicKey public key
   * @return fingerprint
   */
  private static String calculateFingerprint(String publicKey) {
    String derFormat = publicKey.split(" ")[1].trim();
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("Could not get fingerprint", e);
    }
    byte[] digest = messageDigest.digest(Base64.getDecoder().decode(derFormat));
    final StringBuilder toRet = new StringBuilder();
    for (int i = 0; i < digest.length; i++) {
      if (i != 0) toRet.append(":");
      int b = digest[i] & 0xff;
      String hex = Integer.toHexString(b);
      if (hex.length() == 1) toRet.append("0");
      toRet.append(hex);
    }
    return toRet.toString();
  }
}
