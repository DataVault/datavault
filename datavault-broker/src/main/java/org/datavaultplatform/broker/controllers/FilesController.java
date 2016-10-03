package org.datavaultplatform.broker.controllers;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.broker.services.FilesService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 13:21
 */


@RestController
public class FilesController {
    
    private FilesService filesService;
    private UsersService usersService;
    private String tempDir;
    
    public void setFilesService(FilesService filesService) {
        this.filesService = filesService;
    }
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }
    
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
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
    
    @RequestMapping("/filesize/{storageid}/**")
    public String getFilesize(@RequestHeader(value = "X-UserID", required = true) String userID,
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
        
        // TODO: is there a cleaner way to extract the request path?
        String requestPath = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String filePath = requestPath.replaceFirst("^/filesize/" + storageID, "");
        
        Long size = filesService.getFilesize(filePath, store);
        
        if (size == null) {
            return "File information not available";
        } else {
            return FileUtils.byteCountToDisplaySize(size);
        }
    }
    
    @RequestMapping(value="/files/{filename:.+}", method = RequestMethod.POST)
    public String postFileChunk(@RequestHeader(value = "X-UserID", required = true) String userID,
                                HttpServletRequest request,
                                @PathVariable("filename") String filename) throws Exception {
        
        User user = usersService.getUser(userID);
        
        System.out.println("Broker postFileChunk for " + user.getID() + " - " + filename);
        
        Long chunkNumber = Long.parseLong(request.getParameter("chunkNumber"));
        Long totalChunks = Long.parseLong(request.getParameter("totalChunks"));
        Long chunkSize = Long.parseLong(request.getParameter("chunkSize"));
        Long totalSize = Long.parseLong(request.getParameter("totalSize"));
        
        System.out.println("Broker postFileChunk:" +
                " chunkNumber=" + chunkNumber +
                " totalChunks=" + totalChunks +
                " chunkSize=" + chunkSize +
                " totalSize=" + totalSize);
        
        String dirName = "uploads";
        Path uploadDirPath = Paths.get(tempDir, dirName);
        File uploadDir = uploadDirPath.toFile();
        
        // Get the top-level upload directory
        if (!uploadDir.exists()) {
            System.out.println("Creating uploadDir: " + uploadDir.getPath());
            Boolean success = uploadDir.mkdir();
            if (!success) {
                throw new Exception("Unable to create uploadDir");
            }
        }

        // Create the upload directory for this file
        Path userUploadDirPath = uploadDirPath.resolve(userID);
        File userUploadDir = userUploadDirPath.toFile();
        if (!userUploadDir.exists()) {
            System.out.println("Creating userUploadDir: " + userUploadDir.getPath());
            Boolean success = userUploadDir.mkdir();
            if (!success) {
                throw new Exception("Unable to create userUploadDir");
            }
        }
        
        // Get the actual file
        File f = userUploadDirPath.resolve(filename).toFile();
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        
        //Seek through the file to the start of this chunk
        raf.seek((chunkNumber - 1) * chunkSize);
        
        // Write chunk bytes to file
        InputStream is = request.getInputStream();
        long count = 0;
        long length = request.getContentLength();
        byte[] buf = new byte[1024 * 1024];
        while(count < length) {
            int r = is.read(buf);
            if (r < 0)  {
                break;
            }
            raf.write(buf, 0, r);
            count += r;
        }
        raf.close();
        
        return "";
    }
}
