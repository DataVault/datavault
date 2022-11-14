package org.datavaultplatform.worker.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.crypto.EncryptionValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EncryptionConfig {

  //01
  @Value("${encryption.bufferSizeInByte:0}")
  private String $01_encBufferSize;

  //02
  @Value("${vault.enable:false}")
  private boolean $02_vaultEnable;

  //03
  @Value("${vault.address:#{null}}")
  private String $03_vaultAddress;

  //04
  @Value("${vault.token:#{null}}")
  private String $04_vaultToken;

  //05
  @Value("${vault.secretPath:#{null}}")
  private String $05_vaultKeyPath;

  //06
  @Value("${vault.dataEncryptionKeyName:#{null}}")
  private String $06_vaultDataEncryptionKeyName;

  //07
  @Value("${vault.privateKeyEncryptionKeyName:#{null}}")
  private String $07_vaultPrivateKeyEncryptionKeyName;

  //08
  @Value("${vault.sslPEMPath:#{null}}")
  private String $08_vaultSslPEMPath;

  //09
  @Value("${keystore.enable:false}")
  private boolean $09_keystoreEnable;

  //10
  @Value("${keystore.path:#{null}}")
  private String $10_keystorePath;

  //11
  @Value("${keystore.password:#{null}}")
  private String $11_keystorePassword;

  /*
      <bean id="encryption" class="org.datavaultplatform.common.crypto.Encryption">
        1<property name="encBufferSize" value="${encryption.bufferSizeInByte}"/>
        2<property name="vaultEnable" value="${vault.enable}"/>
        3<property name="vaultAddress" value="${vault.address}"/>
        4<property name="vaultToken" value="${vault.token}"/>
        5<property name="vaultKeyPath" value="${vault.secretPath}"/>
        6<property name="vaultDataEncryptionKeyName" value="${vault.dataEncryptionKeyName}"/>
        7<property name="vaultPrivateKeyEncryptionKeyName" value="${vault.privateKeyEncryptionKeyName}"/>
        8<property name="vaultSslPEMPath" value="${vault.sslPEMPath:}"/>

        9<property name="keystoreEnable" value="${keystore.enable}"/>
        10<property name="keystorePath" value="${keystore.path}"/>
        11<property name="keystorePassword" value="${keystore.password}"/>
    </bean>

   */

  @Bean
  Encryption setupEncryption() {
    // shared properties (1)
    Encryption encryption = new Encryption();
    encryption.setEncBufferSize($01_encBufferSize);

    // hashicorp vault (5)
    encryption.setVaultEnable($02_vaultEnable);
    encryption.setVaultAddress($03_vaultAddress);
    encryption.setVaultToken($04_vaultToken);
    encryption.setVaultKeyPath($05_vaultKeyPath);
    encryption.setVaultSslPEMPath($08_vaultSslPEMPath);

    // key names (2) - can refer to Vault or Keystore keys - NOT JUST VAULT KEYS
    encryption.setVaultDataEncryptionKeyName($06_vaultDataEncryptionKeyName);
    encryption.setVaultPrivateKeyEncryptionKeyName($07_vaultPrivateKeyEncryptionKeyName);

    // keystore (3)
    encryption.setKeystoreEnable($09_keystoreEnable);
    encryption.setKeystorePath($10_keystorePath);
    encryption.setKeystorePassword($11_keystorePassword);

    return encryption;
  }

  @Bean
  EncryptionValidator encryptionValidator() {
    return new EncryptionValidator();
  }
}
