package org.datavaultplatform.broker.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.datavaultplatform.broker.services.UserKeyPairService;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.FileStore;

import org.jsondoc.core.annotation.ApiHeader;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.services.UsersService;

@RestController
public class FileStoreController {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreController.class);

    private UsersService usersService;
    private FileStoreService fileStoreService;
    private UserKeyPairService userKeyPairService;
    private String host;
    private String port;
    private String rootPath;
    private String passphrase;
    
    public void setFileStoreService(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public void setUserKeyPairService(UserKeyPairService userKeyPairService) {
        this.userKeyPairService = userKeyPairService;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }


    @RequestMapping(value = "/filestores", method = RequestMethod.GET)
    public ResponseEntity<List<FileStore>> getFileStores(@RequestHeader(value = "X-UserID", required = true) String userID) {
        User user = usersService.getUser(userID);
        
        List<FileStore> userStores = user.getFileStores();
        for (FileStore store : userStores) {
            // For now - strip out config information
            store.setProperties(null);
        }
        
        return new ResponseEntity<>(userStores, HttpStatus.OK);
    }



    @RequestMapping(value = "/filestores", method = RequestMethod.POST)
    public ResponseEntity<FileStore> addFileStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @RequestBody FileStore store) throws Exception {

        User user = usersService.getUser(userID);
        store.setUser(user);
        fileStoreService.addFileStore(store);
        return new ResponseEntity<>(store, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/filestores/{filestoreid}", method = RequestMethod.GET)
    public ResponseEntity<FileStore> getFileStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                               @PathVariable("filestoreid") String filestoreid) {

        FileStore store = fileStoreService.getFileStore(filestoreid);
        return new ResponseEntity<>(store, HttpStatus.OK);
    }

    @RequestMapping(value = "filestores/{filestoreid}", method = RequestMethod.DELETE)
    public ResponseEntity<Object>  deleteFileStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                               @PathVariable("filestoreid") String filestoreid) {

        fileStoreService.deleteFileStore(filestoreid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/filestores/local", method = RequestMethod.GET)
    public ResponseEntity<List<FileStore>> getFileStoresLocal(@RequestHeader(value = "X-UserID", required = true) String userID) {
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



    @RequestMapping(value = "/filestores/sftp", method = RequestMethod.POST)
    public ResponseEntity<FileStore> addFileStoreSFTP(@RequestHeader(value = "X-UserID", required = true) String userID,
                                      @RequestBody FileStore store) throws Exception {

        User user = usersService.getUser(userID);

        userKeyPairService.generateNewKeyPair();

        HashMap<String,String> storeProperties = store.getProperties();

        // Add the confidential properties.
        storeProperties.put("username", user.getID());
        storeProperties.put("password", "");
        storeProperties.put("publicKey", userKeyPairService.getPublicKey());
        storeProperties.put("privateKey", userKeyPairService.getPrivateKey());
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

    @RequestMapping(value = "/filestores/sftp", method = RequestMethod.GET)
    public ResponseEntity<List<FileStore>> getFileStoresSFTP(@RequestHeader(value = "X-UserID", required = true) String userID) {
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

    @RequestMapping(value = "/filestores/sftp/{filestoreid}", method = RequestMethod.GET)
    public ResponseEntity<FileStore> getFilestoreSFTP(@RequestHeader(value = "X-UserID", required = true) String userID,
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
