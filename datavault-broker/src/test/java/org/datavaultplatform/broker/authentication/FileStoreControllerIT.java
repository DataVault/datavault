package org.datavaultplatform.broker.authentication;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jcraft.jsch.KeyPairRSA;
import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.dao.FileStoreDAO;
import org.datavaultplatform.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
@AutoConfigureMockMvc
public class FileStoreControllerIT extends BaseDatabaseTest {

  @Value("${sftp.passphrase}")
  String passphrase;

  @MockBean
  Sender sender;

  private static final String KEY_STORE_PASSWORD = "thePassword";
  private static final String KEY_NAME = "thekeyname";

  @TempDir
  File temp;

  ObjectMapper mapper = new ObjectMapper();

  @Autowired
  MockMvc mvc;

  @Autowired
  FileStoreDAO fileStoreDAO;

  String keyStorePath;

  /*
    Creates and configures a Java Key Store specifically for this test run.
   */
  @BeforeEach
  @SneakyThrows
  void setup() {
    keyStorePath = temp.toPath().resolve("test.ks").toString();
    log.info("TEMP KEY IS AT [{}]",keyStorePath);
    Encryption enc = new Encryption();
    enc.setVaultEnable(false);
    enc.setVaultPrivateKeyEncryptionKeyName(KEY_NAME);

    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);

    SecretKey keyForKeyStore = Encryption.generateSecretKey();

    assertFalse(new File(this.keyStorePath).exists());

    // Encryption class uses 'vaultPrivateKeyEncryptionKeyName' property as the default key name for JavaKeyStore'
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(), keyForKeyStore);
    assertTrue(new File(this.keyStorePath).exists());

  }

  @Test
  void testPostAddSftpFileStore() throws Exception {

    assertEquals(0, fileStoreDAO.count());

    assertTrue(Encryption.isInitialised());
    FileStore filestore = new FileStore();
    filestore.setLabel("label-one");
    filestore.setStorageClass("org.datavaultplatform.common.storage.impl.SFTPFileSystem");
    HashMap<String, String> props = new HashMap<>();
    filestore.setProperties(props);

    MvcResult result = mvc.perform(post("/filestores/sftp")
        .with(req -> {
          req.setRemoteAddr("127.0.0.1");
          return req;
        })
        .header(Constants.HEADER_CLIENT_KEY, "datavault-webapp")
        .header(Constants.HEADER_USER_ID, "admin1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(filestore)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();


    DocumentContext jsonContext = JsonPath.parse(result.getResponse().getContentAsString());
    String fileStoreId = jsonContext.read("$['id']");
    log.info("FileStoreId {}", fileStoreId);

    assertEquals(1,fileStoreDAO.count());

    FileStore fs = fileStoreDAO.findById(fileStoreId).get();
    assertEquals(fileStoreId, fs.getID());

    assertEquals("admin1", fs.getUser().getID());

    assertEquals("org.datavaultplatform.common.storage.impl.SFTPFileSystem", fs.getStorageClass());
    assertEquals("label-one", fs.getLabel());
    HashMap<String, String> storedProps = fs.getProperties();
    assertEquals(new HashSet(
        Arrays.asList("username","password","publicKey","privateKey","iv","passphrase")),storedProps.keySet());
    /*
        storeProperties.put("username", user.getID());
        storeProperties.put("password", "");
        storeProperties.put("publicKey", keypair.getPublicKey());
        storeProperties.put("privateKey", Base64.toBase64String(encrypted));
        storeProperties.put("iv", Base64.toBase64String(iv));
        storeProperties.put("passphrase", passphrase);
     */
    assertEquals("admin1", storedProps.get("username"));
    assertEquals("", storedProps.get("password"));
    assertEquals(passphrase, storedProps.get("passphrase"));
    assertTrue(isBase64(storedProps.get("privateKey")));
    assertTrue(isBase64(storedProps.get("iv")));
    assertTrue(storedProps.get("publicKey").startsWith("ssh-rsa "));
    log.info("properties {}", storedProps);
    log.info("fin");

    byte[] privateKeyData = Base64.getDecoder().decode(storedProps.get("privateKey"));
    byte[] ivData = Base64.getDecoder().decode(storedProps.get("iv"));

    // what I should do now is place the public key on a container that supports SSH/SCP
    // restart container or ssh daemon



  }

  public boolean isBase64(String path) {
    try {
      Base64.getDecoder().decode(path);
      return true;
    } catch(IllegalArgumentException e) {
      return false;
    }
  }

}
