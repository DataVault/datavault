package org.datavaultplatform.common.storage.impl;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.crypto.SshRsaKeyUtils;

@Slf4j
public class SFTPConnectionInfo {

  public static final String PATH_SEPARATOR = "/";

  public static final int RETRIES = 25;

  private final String host;
  private final int port;
  private final String rootPath;
  private final String username;

  private final Clock clock;
  private final KeyPair keyPair;
  private final String password;

  public SFTPConnectionInfo(String rootPath, String username, String host, int port,
      KeyPair keyPair, String password, Clock clock) {
    this.rootPath = rootPath;
    this.host = host;
    this.port = port;
    this.username = username;
    this.keyPair = keyPair;
    this.password = password;
    this.clock = clock;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getRootPath() {
    return rootPath;
  }

  public Clock getClock() {
    return clock;
  }

  public String getUsername() {
    return username;
  }

  public KeyPair getKeyPair() {
    return keyPair;
  }

  public String getPassword() {
    return password;
  }

  @SneakyThrows
  public static KeyPair getKeyPair(byte[] encPrivateKey, byte[] encIV, String keyPassphrase) {
    //we don't need the password for the keystore - that's already baked into Encryption class.
    byte[] passPhraseProtectedPrivateKey = Encryption.decryptSecret(encPrivateKey, encIV);
    RSAPrivateKey privateKey = SshRsaKeyUtils.readPrivateKey(
        new String(passPhraseProtectedPrivateKey, StandardCharsets.UTF_8), keyPassphrase);
    KeyPair result = SshRsaKeyUtils.getKeyPairFromRSAPrivateKey(privateKey);
    log.info("AFTER PUBLIC  KEY MODULUS [{}]", ((RSAPublicKey)result.getPublic()).getModulus().toString(16));
    log.info("AFTER PRIVATE KEY MODULUS [{}]", ((RSAPrivateKey)result.getPrivate()).getModulus().toString(16));
    return result;
  }

  public static SFTPConnectionInfo getConnectionInfo(Map<String, String> config, Clock clock) {
    String rootPath = config.get(PropNames.ROOT_PATH);
    String username = config.get(PropNames.USERNAME);
    String host = config.get(PropNames.HOST);
    int port = Integer.parseInt(config.get(PropNames.PORT));
    String password = config.get(PropNames.PASSWORD);
    String privateKey = config.get(PropNames.PRIVATE_KEY);
    String iv = config.get(PropNames.IV);
    String passphrase = config.get(PropNames.PASSPHRASE);
    final KeyPair keyPair;
    if (privateKey != null && iv != null && passphrase != null) {
      keyPair = getKeyPair(Base64.decode(privateKey),
          Base64.decode(iv), passphrase);
    } else {
      keyPair = null;
    }
    return new SFTPConnectionInfo(rootPath, username, host, port, keyPair, password, clock);
  }
}

