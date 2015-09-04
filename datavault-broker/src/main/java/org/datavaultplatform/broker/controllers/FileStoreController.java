package org.datavaultplatform.broker.controllers;

import java.util.List;
import java.util.ArrayList;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.FileStore;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;

import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.services.UsersService;

@RestController
public class FileStoreController {
    
    private UsersService usersService;
    private FileStoreService fileStoreService;
    
    public void setFileStoreService(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
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
}
