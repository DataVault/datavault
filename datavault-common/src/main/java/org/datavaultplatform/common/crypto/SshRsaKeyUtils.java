package org.datavaultplatform.common.crypto;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StreamUtils;

/**
 * Utility class that converts between Public/Private RSA Keys and their String representations for SSH/SFTP
 */
@Slf4j
public class SshRsaKeyUtils {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @SneakyThrows
  public static RSAPrivateKey readPrivateKey(File privateKeyFile, String keyPassphrase) {
    return readPrivateKey(new FileReader(privateKeyFile), keyPassphrase);
  }

  @SneakyThrows
  public static RSAPrivateKey readPrivateKey(String privateKey, String keyPassphrase) {
    return readPrivateKey(new StringReader(privateKey), keyPassphrase);
  }

  @SneakyThrows
  public static RSAPrivateKey readPrivateKey(Reader privateKeyReader, String keyPassphrase) {
    PEMParser pemParser = new PEMParser(privateKeyReader);
    Object object = pemParser.readObject();
    final PEMKeyPair pemKeyPair;
    if (object instanceof PEMEncryptedKeyPair) {
      PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(keyPassphrase.toCharArray());
      log.info("Encrypted key - will use provided passphrase]");
      pemKeyPair = ((PEMEncryptedKeyPair) object).decryptKeyPair(decProv);
    } else {
      log.info("Unencrypted key - no passphrase needed");
      pemKeyPair = (PEMKeyPair) object;
    }
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    KeyPair kp = converter.getKeyPair(pemKeyPair);
    return (RSAPrivateKey) kp.getPrivate();
  }

  @SneakyThrows
  public static RSAPublicKey readPublicKey(File sshRsaFile) {
    // input stream of .pub file
    InputStream inputStream = Files.newInputStream(sshRsaFile.toPath());
    String sshRsaPublicKeySingleLine = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
    return readPublicKey(sshRsaPublicKeySingleLine);
  }

  // TODO - There's probably a better way to do this.
  @SneakyThrows
  public static RSAPublicKey readPublicKey(String sshRsaPublicKeySingleLine) {
    String[] parts = sshRsaPublicKeySingleLine.split(" ");
    for (String part : parts) {
      if (part.startsWith("AAAA")) {
        byte[] decodeBuffer = Base64Utils.decode(part.getBytes());
        ByteBuffer bb = ByteBuffer.wrap(decodeBuffer);
        // using 4 bytes from bb to generate integer which gives us length of key -
        // format type, in this case len=7 as "ssh-rsa" has 7 chars
        int len = bb.getInt();
        byte[] type = new byte[len];
        bb.get(type);
        if ("ssh-rsa".equals(new String(type))) {
          // extracting exponent and modulus from remaining byte-buffer
          BigInteger exponent = decodeBigInt(bb);
          BigInteger modulus = decodeBigInt(bb);
          RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
          return (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(spec);
        } else {
          throw new IllegalArgumentException("Only supports RSA");
        }
      }
    }
    return null;
  }

  private static BigInteger decodeBigInt(ByteBuffer bb) {
    // use first 4 bytes to generate an Integer that gives the length of bytes to create BigInteger
    int len = bb.getInt();
    byte[] bytes = new byte[len];
    bb.get(bytes);
    return new BigInteger(bytes);
  }

  public static KeyPair getKeyPairFromRSAPrivateKey(RSAPrivateKey rsaPrivateKey) {
    RSAPublicKey rsaPublicKey = getRSAPublicKey(rsaPrivateKey);
    Assert.isTrue(rsaPublicKey.getModulus().equals(rsaPrivateKey.getModulus()),
        () -> "private and public modulus must be the same");
    if(log.isTraceEnabled()) {
      log.trace("RSA MODULUS {}", rsaPublicKey.getModulus().toString(16));
    }
    KeyPair keyPair = new KeyPair(rsaPublicKey, rsaPrivateKey);
    return keyPair;
  }

  @SneakyThrows
  private static RSAPublicKey getRSAPublicKey(RSAPrivateKey rsaPrivateKey) {
    BigInteger modulus = rsaPrivateKey.getModulus();
    BigInteger publicExponent = getRSAPrivateCrtKey(rsaPrivateKey).getPublicExponent();
    RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulus, publicExponent);
    return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(publicSpec);
  }

  private static RSAPrivateCrtKey getRSAPrivateCrtKey(RSAPrivateKey privateKey) {
    RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey;
    return rsaPrivateCrtKey;
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
  public static String calculateFingerprint(String publicKey) {
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

