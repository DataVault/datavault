package org.datavaultplatform.broker.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.broker.services.FilesService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import javax.servlet.http.HttpServletRequest;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 13:21
 */


@RestController
public class FilesController {
    
    private FilesService filesService;
    private UsersService usersService;
    
    public void setFilesService(FilesService filesService) {
        this.filesService = filesService;
    }
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }
    
    @RequestMapping("/files")
    public List<FileInfo> getStorageListing(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            HttpServletRequest request) {
        
        User user = usersService.getUser(userID);
        
        ArrayList<FileInfo> files = new ArrayList<>();
        List<FileStore> userStores = user.getFileStores();
        for (FileStore userStore : userStores) {
            FileInfo info = new FileInfo(userStore.getID(),
                                         userStore.getID(),
                                         userStore.getLabel(),
                                         true);
            files.add(info);
        }
        
        // "GET /files/" will display a list of configured user storage systems.
        return files;
    }
    
    @RequestMapping("/files/{storageid}/**")
    public List<FileInfo> getFilesListing(@RequestHeader(value = "X-UserID", required = true) String userID,
                                          HttpServletRequest request,
                                          @PathVariable("storageid") String storageID) throws Exception {
        
        User user = usersService.getUser(userID);
        
        FileStore store = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore userStore : userStores) {
            if (userStore.getID().equals(storageID)) {
                store = userStore;
            }
        }
        
        if (store == null) {
            throw new Exception("Storage device '" + storageID + "' not found!");
        }
        
        // "GET /files/storageid" will display files from the base directory.
        // "GET /files/storageid/abc" will display files from the "abc" directory under the base.
        
        // TODO: is there a cleaner way to extract the request path?
        String requestPath = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String filePath = requestPath.replaceFirst("^/files/" + storageID, "");
        
        List<FileInfo> files = filesService.getFilesListing(filePath, store);
        
        // Add the storage key to the start of the returned path/key
        for (FileInfo file : files) {
            String fullKey = file.getKey();
            
            if (!fullKey.startsWith("/")) {
                fullKey = "/" + fullKey;
            }
            
            fullKey = storageID + fullKey;
            file.setKey(fullKey);
        }
        
        return files;
    }
}
