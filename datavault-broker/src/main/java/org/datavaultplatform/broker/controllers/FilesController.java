package org.datavaultplatform.broker.controllers;

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
    
    private String activeDir;
    
    public void setFilesService(FilesService filesService) {
        this.filesService = filesService;
    }
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }
    
    // NOTE: this a placeholder and will eventually be handled by per-user config
    public void setActiveDir(String activeDir) {
        this.activeDir = activeDir;
    }

    @RequestMapping("/files/**")
    public List<FileInfo> getFilesListing(@RequestHeader(value = "X-UserID", required = true) String userID,
                                          HttpServletRequest request) {
        
        User user = usersService.getUser(userID);
        
        // File store config ...
        FileStore store = null;
        List<FileStore> userStores = user.getFileStores();
        if (userStores.size() > 0) {
            // For now, just use the first configured store ...
            store = userStores.get(0);
        } else {
            // For now, use a default store ...
            HashMap<String,String> storeProperties = new HashMap<String,String>();
            storeProperties.put("rootPath", activeDir);
            store = new FileStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties);
        }
        
        // "GET /files/" will display files from the base directory.
        // "GET /files/abc" will display files from the "abc" directory under the base.
        
        // TODO: is there a cleaner way to extract the request path?
        String requestPath = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String filePath = requestPath.replaceFirst("^/files", "");
        
        return filesService.getFilesListing(filePath, store);
    }
}
