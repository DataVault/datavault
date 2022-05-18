package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.services.UserKeyPairService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class FileStoreController {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreController.class);

    private final UsersService usersService;
    private final FileStoreService fileStoreService;
    private final UserKeyPairService userKeyPairService;
    private final String host;
    private final String port;
    private final String rootPath;
    private final String passphrase;

    @Autowired
    public FileStoreController(UsersService usersService, FileStoreService fileStoreService,
        UserKeyPairService userKeyPairService,
        @Value("${sftp.host}") String host,
        @Value("${sftp.port}") String port,
        @Value("${sftp.rootPath}") String rootPath,
        @Value("${sftp.passphrase}") String passphrase) {
        this.usersService = usersService;
        this.fileStoreService = fileStoreService;
        this.userKeyPairService = userKeyPairService;
        this.host = host;
        this.port = port;
        this.rootPath = rootPath;
        this.passphrase = passphrase;
    }


    @GetMapping("/filestores")
    public ResponseEntity<List<FileStore>> getFileStores(@RequestHeader(HEADER_USER_ID) String userID) {
        User user = usersService.getUser(userID);
        
        List<FileStore> userStores = user.getFileStores();
        for (FileStore store : userStores) {
            // For now - strip out config information
            store.setProperties(null);
        }
        
        return new ResponseEntity<>(userStores, HttpStatus.OK);
    }



    @PostMapping("/filestores")
    public ResponseEntity<FileStore> addFileStore(@RequestHeader(HEADER_USER_ID) String userID,
                                  @RequestBody FileStore store) {

        User user = usersService.getUser(userID);
        store.setUser(user);
        fileStoreService.addFileStore(store);
        return new ResponseEntity<>(store, HttpStatus.CREATED);
    }

    @GetMapping("/filestores/{filestoreid}")
    public ResponseEntity<FileStore> getFileStore(@RequestHeader(HEADER_USER_ID) String userID,
                               @PathVariable("filestoreid") String filestoreid) {

        FileStore store = fileStoreService.getFileStore(filestoreid);
        return new ResponseEntity<>(store, HttpStatus.OK);
    }

    @DeleteMapping("/filestores/{filestoreid}")
    public ResponseEntity<Void>  deleteFileStore(@RequestHeader(HEADER_USER_ID) String userID,
                                               @PathVariable("filestoreid") String filestoreid) {

        fileStoreService.deleteFileStore(filestoreid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/filestores/local")
    public ResponseEntity<List<FileStore>> getFileStoresLocal(@RequestHeader(HEADER_USER_ID) String userID) {
        User user = usersService.getUser(userID);

        List<FileStore> userStores = user.getFileStores();
        List<FileStore> localStores = new ArrayList<>();

        for (FileStore userStore : userStores) {
            if (userStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.LocalFileSystem")) {
                localStores.add(userStore);
                logger.info("Adding entry to list of local filestores" + userStore);
            }
        }

        return new ResponseEntity<>(localStores, HttpStatus.OK);
    }



    @PostMapping("/filestores/sftp")
    public ResponseEntity<FileStore> addFileStoreSFTP(@RequestHeader(HEADER_USER_ID) String userID,
                                      @RequestBody FileStore store) {

        User user = usersService.getUser(userID);

        userKeyPairService.generateNewKeyPair();

        byte[] encrypted = null;
        byte[] iv = Encryption.generateIV();
        try {
            // We got to encrypt the private key before putting it int he database.
            encrypted = Encryption.encryptSecret(userKeyPairService.getPrivateKey(), null, iv);
        } catch (Exception e) {
            logger.error("Error when encrypting private key: "+e);
            e.printStackTrace();
            return new ResponseEntity<>(store, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HashMap<String, String> storeProperties = store.getProperties();

        // Add the confidential properties.
        storeProperties.put("username", user.getID());
        storeProperties.put("password", "");
        storeProperties.put("publicKey", userKeyPairService.getPublicKey());
        storeProperties.put("privateKey", Base64.toBase64String(encrypted));
        storeProperties.put("iv", Base64.toBase64String(iv));
        storeProperties.put("passphrase", passphrase);

        store.setUser(user);
        fileStoreService.addFileStore(store);

        // Remove sensitive information that should only be held server side.
        storeProperties = store.getProperties();
        storeProperties.remove("password");
        storeProperties.remove("privateKey");
        storeProperties.remove("passphrase");
        store.setProperties(storeProperties);

        return new ResponseEntity<>(store, HttpStatus.CREATED);
    }

    @GetMapping("/filestores/sftp")
    public ResponseEntity<List<FileStore>> getFileStoresSFTP(@RequestHeader(HEADER_USER_ID) String userID) {
        User user = usersService.getUser(userID);

        List<FileStore> userStores = user.getFileStores();
        List<FileStore> localStores = new ArrayList<>();

        for (FileStore userStore : userStores) {
            if (userStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.SFTPFileSystem")) {
                localStores.add(userStore);
            }
        }

        return new ResponseEntity<>(localStores, HttpStatus.OK);
    }

    @GetMapping("/filestores/sftp/{filestoreid}")
    public ResponseEntity<FileStore> getFilestoreSFTP(@RequestHeader(HEADER_USER_ID) String userID,
                                   @PathVariable("filestoreid") String filestoreid) {

        FileStore store = fileStoreService.getFileStore(filestoreid);

        // Remove sensitive information that should only be held server side.
        HashMap<String,String> storeProperties = store.getProperties();
        storeProperties.remove("password");
        storeProperties.remove("privateKey");
        storeProperties.remove("passphrase");
        store.setProperties(storeProperties);

        return new ResponseEntity<>(store, HttpStatus.OK);

    }



}
