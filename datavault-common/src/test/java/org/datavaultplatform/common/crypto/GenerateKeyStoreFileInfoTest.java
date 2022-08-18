package org.datavaultplatform.common.crypto;

import static org.junit.Assert.assertEquals;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.datavaultplatform.common.crypto.Encryption.KeyStoreInfo;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

@Slf4j
public class GenerateKeyStoreFileInfoTest {

  @Test
  @SneakyThrows
  void testExtractKeyStoreInfo() {
    ClassPathResource res = new ClassPathResource("exampleGenerateKeyStoreFile.json");
    String fileName = res.getFile().getAbsolutePath();
    log.info("fileName[{}]", fileName);
    KeyStoreInfo info = Encryption.extractKeyStoreInfo(fileName);

    assertEquals("thePassword", info.getPassword());
    assertEquals("/tmp/testKeyStore.jks", info.getPath());
    assertEquals("/tmp/testKeyStore.jks", info.getPath());
    assertEquals(2, info.aliases.size());
    assertEquals("data-encryption-key", info.aliases.get(0));
    assertEquals("ssh-encryption-key", info.aliases.get(1));
  }

}
