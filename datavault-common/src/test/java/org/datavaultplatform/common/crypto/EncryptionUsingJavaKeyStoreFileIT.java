package org.datavaultplatform.common.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.DigestUtils;

/**
 * Tests parts of the Encryption class that uses Java Key Store File.
 */
@Slf4j
public class EncryptionUsingJavaKeyStoreFileIT {

  private static final String KEY_STORE_TYPE_JCEKS = "JCEKS";
  private static final String KEY_STORE_TYPE_PKCS12 = "PKCS12";

  private static final String KEY_STORE_PASSWORD = "thePassword";

  //The Encryption class uses same password for keystore and key!
  private static final String KEY_PASSWORD = KEY_STORE_PASSWORD;
  private static final String KEY_NAME_PRIVATE_KEYS = "keynamepks";
  private static final String KEY_NAME_DATA = "keynamedata";

  @TempDir
  static File temp;

  String keyStorePath;

  @BeforeAll
  static void init() {
    Encryption.addBouncyCastleSecurityProvider();
    assertTrue(Encryption.isInitialised());
  }

  @BeforeEach
  void setup() {
    keyStorePath = temp.toPath().resolve("test.ks").toString();
    log.info("TEMP KEY IS AT [{}]",keyStorePath);
  }

  @Test
  @SneakyThrows
  void testActualJCEKeyStore() {
    Encryption enc = new Encryption();
    enc.setVaultEnable(false);
    enc.setVaultPrivateKeyEncryptionKeyName(KEY_NAME_PRIVATE_KEYS);
    enc.setVaultDataEncryptionKeyName(KEY_NAME_DATA);
    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);
    Encryption.checkKeyNamesAreNotSame();


    SecretKey keyForPrivateKeys = Encryption.generateSecretKey();
    SecretKey keyForData = Encryption.generateSecretKey();


    assertFalse(new File(this.keyStorePath).exists());

    // Encryption class uses 'vaultPrivateKeyEncryptionKeyName' property as the default key name for JavaKeyStore'
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(), keyForPrivateKeys);
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultDataEncryptionKeyName(), keyForData);
    assertTrue(new File(this.keyStorePath).exists());

    checkEncryptDecrypt(Encryption.generateSecretKey());
    checkEncryptDecrypt(keyForPrivateKeys);
    checkEncryptDecrypt(keyForData);

    KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE_JCEKS);
    ks.load(new FileInputStream(keyStorePath), KEY_STORE_PASSWORD.toCharArray());
    Enumeration<String> aliases = ks.aliases();
    List<String> al = new ArrayList<>();
    while(aliases.hasMoreElements()) {
      al.add(aliases.nextElement());
    }

    // Alias names are stored in JKS as lowercase!
    assertIterableEquals(Arrays.asList(KEY_NAME_PRIVATE_KEYS, KEY_NAME_DATA), al);
    SecretKey directFromKSPrivateKeys = (SecretKey) ks.getKey(KEY_NAME_PRIVATE_KEYS, KEY_PASSWORD.toCharArray());
    checkAES(directFromKSPrivateKeys);
    SecretKey directFromKSData = (SecretKey) ks.getKey(KEY_NAME_DATA, KEY_PASSWORD.toCharArray());
    checkAES(directFromKSData);
    assertEquals(keyForPrivateKeys, directFromKSPrivateKeys);
    assertEquals(keyForData, directFromKSData);

    readKeyFromJCEKeyStore(keyStorePath, KEY_STORE_PASSWORD, KEY_PASSWORD);
   }

  private void checkAES(SecretKey secretKey) {
    assertEquals("AES", secretKey.getAlgorithm());
  }

  @SneakyThrows
  private void checkEncryptDecrypt(SecretKey... secretKeys) {
    String plainText = UUID.randomUUID().toString();
    byte[] iv = Encryption.generateIV();
    Set<String> allEncrypted = new HashSet<>();
    for(SecretKey secretKey : secretKeys) {
      byte[] encrypted = Encryption.encryptSecret(plainText, secretKey, iv);
      byte[] decrypted = Encryption.decryptSecret(encrypted, iv, secretKey);
      assertEquals(plainText, toString(decrypted));
      allEncrypted.add(toString(encrypted));
    }
    assertEquals(1, allEncrypted.size());
  }

  private String toString(byte[] data){
    return new String(data, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public static void readKeyFromPKCS12KeyStore(final String keyStorePath, final String keyStorePassword, final String keyPassword) {
    readKeyFromKeyStore(KEY_STORE_TYPE_PKCS12, keyStorePath, keyStorePassword, keyPassword);
  }

   @SneakyThrows
   public static void readKeyFromJCEKeyStore(final String keyStorePath, final String keyStorePassword, final String keyPassword) {
     readKeyFromKeyStore(KEY_STORE_TYPE_JCEKS, keyStorePath, keyStorePassword, keyPassword);
   }

   @SneakyThrows
   public static void readKeyFromKeyStore(final String instanceType, final String keyStorePath, final String keyStorePassword, final String keyPassword) {
     KeyStore ks = KeyStore.getInstance(instanceType);
     byte[] digest = DigestUtils.md5Digest(new FileInputStream(keyStorePath));
     String md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();

     ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
     Enumeration<String> aliases = ks.aliases();
     List<String> keyNames = new ArrayList<>();
     while (aliases.hasMoreElements()) {
       keyNames.add(aliases.nextElement());
     }
     for(String keyName : keyNames) {
       boolean isKey = ks.isKeyEntry(keyName);
       if(!isKey){
         continue;
       }
       // ooh - alias names are stored in JKS as lowercase!
       SecretKey directFromKS = (SecretKey) ks.getKey(keyName, keyPassword.toCharArray());
       Instant created = toInstant(ks.getCreationDate(keyName));
       byte[] encoded = directFromKS.getEncoded();
       String hex = toHex(encoded);
       log.info("Keystore Path [{}]", keyStorePath);
       log.info("Keystore MD5 [{}]", md5);
       log.info("Keystore Provider [{}]", ks.getProvider());
       log.info("Keystore Type [{}]", ks.getType());
       log.info("Secret Key Name [{}] ", keyName);
       log.info("\tKey Created [{}]", formatInstant(created));
       log.info("\tKey Format [{}]", directFromKS.getFormat());
       log.info("\tKey Size [{}]bits", encoded.length * 8);
       log.info("\tKey Algorithm [{}]", directFromKS.getAlgorithm());
       log.info("\tKey Encoded [{}]", hex);
     }
   }

   public static String formatInstant(Instant timestamp) {
     String newYorkDateTimePattern = "dd/MMM/yyyy HH:mm:ss z";
     DateTimeFormatter formatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern).withLocale(
         Locale.getDefault())
         .withZone(ZoneId.systemDefault());
     return formatter.format(timestamp);
   }

   public static String toHex(byte[] encoded){
     StringBuilder encodedHex = new StringBuilder("0x");
     for(int i=0;i<encoded.length;i++){
       encodedHex.append(byteToHex(encoded[i]));
     }
     return encodedHex.toString();
   }

  public static String byteToHex(byte num) {
    char[] hexDigits = new char[2];
    hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
    hexDigits[1] = Character.forDigit((num & 0xF), 16);
    return new String(hexDigits).toUpperCase();
  }

  public static Instant toInstant(Date dateToConvert) {
    return Instant.ofEpochMilli(dateToConvert.getTime());
  }

  @Disabled
  @Test
  void testDebugExistingKeyStore() {
    readKeyFromJCEKeyStore("/path/to/existing.ks","thePassword","thePassword");
  }
}
