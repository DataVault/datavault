package org.datavaultplatform.broker.services;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.crypto.SshRsaKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * User: David Hay
 * Date: 22/Aug/2022
 * Time: 16:45
 */
@Service
@Slf4j
@Transactional
@Primary
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

    String privateKeyValue = SshRsaKeyUtils.encodePrivateKey(privateKey, passphrase);

    String publicKeyValue = SshRsaKeyUtils.encodePublicKey(publicKey, PUBKEY_COMMENT);

      return KeyPairInfo.builder()
          .privateKey(privateKeyValue)
          .publicKey(publicKeyValue)
          .fingerPrint(SshRsaKeyUtils.calculateFingerprint(publicKeyValue))
          .keySize(KEY_SIZE)
          .build();

  }
}
