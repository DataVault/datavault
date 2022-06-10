package org.datavaultplatform.broker.services;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * User: Robin Taylor
 * Date: 04/11/2015
 * Time: 09:54
 */
@Service
public class UserKeyPairService {

  // comment added at the end of public key
  public static final String PUBKEY_COMMENT = "datavault";
  private final String passphrase;

  @Autowired
  public UserKeyPairService(@Value("${sftp.passphrase}") String passphrase) {
    this.passphrase = passphrase;
  }

  public String getPassphrase() {
    return passphrase;
  }

  public KeyPairInfo generateNewKeyPair() {

    JSch jschClient = new JSch();

    try {
      KeyPair keyPair = KeyPair.genKeyPair(jschClient, KeyPair.RSA);
      String privateKey = getPrivateKey(keyPair);
      String publicKey = getPublicKey(keyPair);

      return KeyPairInfo.builder()
          .privateKey(privateKey)
          .publicKey(publicKey)
          .fingerPrint(keyPair.getFingerPrint())
          .build();

    } catch (JSchException e) {
      throw new IllegalArgumentException("problem with generating ssh key pair", e);
    }
  }

  private String getPrivateKey(KeyPair keyPair) {
    return convert(os -> keyPair.writePrivateKey(os, passphrase.getBytes(StandardCharsets.UTF_8)));
  }

  private String getPublicKey(KeyPair keyPair) {
    return convert(os -> keyPair.writePublicKey(os, PUBKEY_COMMENT));
  }

  public String convert(Consumer<OutputStream> consumer) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    consumer.accept(baos);
    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
  }

  @Data
  @Builder
  public static class KeyPairInfo {

    private final String publicKey;
    private final String privateKey;
    private final String fingerPrint;
  }

}
