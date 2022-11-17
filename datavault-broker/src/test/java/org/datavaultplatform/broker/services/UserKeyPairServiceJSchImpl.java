package org.datavaultplatform.broker.services;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.JSchLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
/**
 * User: Robin Taylor
 * Date: 04/11/2015
 * Time: 09:54
 */
@Slf4j
public class UserKeyPairServiceJSchImpl implements UserKeyPairService {

  private final String passphrase;

  static {
    JSch.setLogger(JSchLogger.getInstance());
  }

  @Autowired
  public UserKeyPairServiceJSchImpl(@Value("${sftp.passphrase}") String passphrase) {
    this.passphrase = passphrase;
  }

  @Override
  public String getPassphrase() {
    return passphrase;
  }

  @Override
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
          .keySize(getKeySize(keyPair))
          .build();

    } catch (JSchException e) {
      throw new IllegalArgumentException("problem with generating ssh key pair", e);
    }
  }

  private Integer getKeySize(KeyPair keyPair) {
    Integer result = null;
    try {
      Method m = KeyPair.class.getDeclaredMethod("getKeySize");
      m.setAccessible(true);
      result = (Integer)m.invoke(keyPair);
    } catch(Exception ex) {
      //we don't need the keysize - it's just useful info
      log.debug("problem getting key size",ex);
    }
    return result;
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

}
