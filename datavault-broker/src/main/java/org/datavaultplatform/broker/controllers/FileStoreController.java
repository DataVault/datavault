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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.services.UsersService;

@RestController
public class FileStoreController {
    
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
    public List<FileStore> getFileStores(@RequestHeader(value = "X-UserID", required = true) String userID) {
        User user = usersService.getUser(userID);
        
        List<FileStore> userStores = user.getFileStores();
        for (FileStore store : userStores) {
            // For now - strip out config information
            store.setProperties(null);
        }
        
        return userStores;
    }
    
    @RequestMapping(value = "/filestores", method = RequestMethod.POST)
    public FileStore addFileStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @RequestBody FileStore store) throws Exception {
        
        User user = usersService.getUser(userID);
        store.setUser(user);
        fileStoreService.addFileStore(store);
        return store;
    }

    @RequestMapping(value = "/filestores/keys", method = RequestMethod.POST)
    public String addKeyPair(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {
        User user = usersService.getUser(userID);

        userKeyPairService.generateNewKeyPair();

        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("host", host);
        storeProperties.put("port", port);
        storeProperties.put("rootPath", rootPath);
        storeProperties.put("username", user.getID());
        storeProperties.put("password", "");
        storeProperties.put("publicKey", userKeyPairService.getPublicKey());
        storeProperties.put("privateKey", userKeyPairService.getPrivateKey());
        storeProperties.put("passphrase", passphrase);

        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.SFTPFileSystem", storeProperties, "SFTP filesystem");
        store.setUser(user);
        fileStoreService.addFileStore(store);

        return userKeyPairService.getPublicKey();
    }
}
