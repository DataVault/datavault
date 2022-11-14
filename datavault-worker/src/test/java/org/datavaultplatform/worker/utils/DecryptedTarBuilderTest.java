package org.datavaultplatform.worker.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.Verify;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Disabled
public class DecryptedTarBuilderTest {

  @TempDir
  File tempDir;

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  @SneakyThrows
  @Disabled
  void testBuildDecryptedTarFileFromJsonParams() {
    DecryptedTarBuilder.main(new String[]{"<PATH TO JSON PARAM FILE>"});
  }

  /**
   * Rebuilds a tar file from encrypted chunks and
   * checks the resultant tar file against expectated tar file via sha1 hash.
   *
   * We take the decrypt-params.json file (which has checksums and ivs and password)
   * read it into DecryptedTarBuilderParams
   * update the DecryptedTarBuilderParams object with paths to temp files/directories.
   * create a DecryptedTarBuilder using the params
   * and then get DecryptedTarBuilder to rebuild tar file
   * we then check rebuilt tar file against expected tar file.
   */
  @Test
  @SneakyThrows
  void testDecryptionAndRebuildOfTarFile() {

    File targetTarFile = new ClassPathResource("crypto/tarFile/88584e22-f182-4135-8231-9c2a9d1d8d63.tar").getFile();

    File tarDir = new File(tempDir, "tarDir");
    tarDir.mkdir();

    File chunksDir = new ClassPathResource("crypto/encryptedChunks").getFile();

    ClassPathResource decryptParamsJSON = new ClassPathResource("crypto/decrypt-params.json");
    DecryptedTarBuilderParams params = mapper.readValue(decryptParamsJSON.getInputStream(), DecryptedTarBuilderParams.class);
    String password = params.getKeystorePassword();
    File tempKeyStoreFile = new File(tempDir, "temp.ks");
    KeyStore ks = createKeyStore(password);

    addKeyToKeyStore(ks, params.getDataKeyName(), params.getKeystorePassword(),
        "60636174202F6465762F7572616E646F6D207C2068656164202D6E2031323860");

    saveKeyStoreToFile(ks, tempKeyStoreFile);

    params.setKeystorePath(tempKeyStoreFile.getAbsolutePath());
    params.setChunksDir(chunksDir.getAbsolutePath());
    params.setTarDir(tarDir.getAbsolutePath());

    DecryptedTarBuilder builder = new DecryptedTarBuilder(params);
    File rebuiltTarFile = builder.rebuildTarFile();

    String targetTarSha1 = Verify.getDigest(targetTarFile);
    String rebuiltTarSha1 = Verify.getDigest(rebuiltTarFile);

    assertEquals(rebuiltTarSha1, targetTarSha1);
  }


  private KeyStore createKeyStore(String password) throws Exception {
      KeyStore ks = KeyStore.getInstance("JCEKS");
      ks.load(null, password.toCharArray());
      return ks;
  }

  @SneakyThrows
  private void saveKeyStoreToFile(KeyStore ks, File file) {
    try(FileOutputStream fos = new FileOutputStream(file)){
      ks.store(fos, "veryStrongPassword".toCharArray());
    }
  }

  @SneakyThrows
  private void addKeyToKeyStore(KeyStore ks, String keyName, String password, String base16key){

    SecretKey secretKey =
        new SecretKeySpec(BaseEncoding.base16().decode(base16key), "AES");

    KeyStore.ProtectionParameter protParam =
        new KeyStore.PasswordProtection(password.toCharArray());

    KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);

    ks.setEntry(keyName, skEntry, protParam);
  }

}
