package org.datavaultplatform.broker.controllers;

import org.apache.commons.codec.binary.Base64;
import org.datavaultplatform.broker.services.FilesService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.io.FileUtils;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
            return FileUtils.getGibibyteSizeStr(size);
        }
    }
    
    @RequestMapping(value="/upload/{fileUploadHandle}/{filename:.+}", method = RequestMethod.POST)
    public String postFileChunk(@RequestHeader(value = "X-UserID", required = true) String userID,
                                HttpServletRequest request,
                                @PathVariable("fileUploadHandle") String fileUploadHandle,
                                @PathVariable("filename") String filename) throws Exception {
        
        User user = usersService.getUser(userID);
        
        System.out.println("Broker postFileChunk for " + user.getID() + " - " + filename);
        
        String relativePath = new String(Base64.decodeBase64(request.getParameter("relativePath").getBytes()));
        Long chunkNumber = Long.parseLong(request.getParameter("chunkNumber"));
        Long totalChunks = Long.parseLong(request.getParameter("totalChunks"));
        Long chunkSize = Long.parseLong(request.getParameter("chunkSize"));
        Long totalSize = Long.parseLong(request.getParameter("totalSize"));
        
        System.out.println("fileUploadHandle =" + fileUploadHandle);
        
        /*
        System.out.println("Broker postFileChunk:" +
                " relativePath=" + relativePath +
                " chunkNumber=" + chunkNumber +
                " totalChunks=" + totalChunks +
                " chunkSize=" + chunkSize +
                " totalSize=" + totalSize);
        */
        
        // Get the top-level upload directory
        String dirName = "uploads";
        Path uploadDirPath = Paths.get(tempDir, dirName);
        File uploadDir = uploadDirPath.toFile();
        
        if (!uploadDir.exists()) {
            System.out.println("Creating uploadDir: " + uploadDir.getPath());
            Boolean success = uploadDir.mkdir();
            if (!success && !uploadDir.exists()) {
                throw new Exception("Unable to create uploadDir");
            }
        }
        
        // Create the directory for this user
        Path userUploadDirPath = uploadDirPath.resolve(userID);
        File userUploadDir = userUploadDirPath.toFile();
        if (!userUploadDir.exists()) {
            System.out.println("Creating userUploadDir: " + userUploadDir.getPath());
            Boolean success = userUploadDir.mkdir();
            if (!success && !userUploadDir.exists()) {
                throw new Exception("Unable to create userUploadDir");
            }
        }
        
        // Create the upload directory within the user directory
        Path handleUploadDirPath = userUploadDirPath.resolve(fileUploadHandle);
        File handleUploadDir = handleUploadDirPath.toFile();
        if (!handleUploadDir.exists()) {
            System.out.println("Creating handleUploadDir: " + handleUploadDir.getPath());
            Boolean success = handleUploadDir.mkdir();
            if (!success && !handleUploadDir.exists()) {
                throw new Exception("Unable to create handleUploadDir");
            }
        }
        
        // Get the actual file
        File f = handleUploadDirPath.resolve(relativePath).toFile();
        
        // Check the path is a valid sub-path
        Path canonicalBase = Paths.get(handleUploadDir.getCanonicalPath());
        Path canonicalPath = Paths.get(f.getCanonicalPath());
        if (!canonicalPath.startsWith(canonicalBase)) {
            throw new Exception("Relative path error");
        }
        
        // Create subdirectories (if needed)
        f.getParentFile().mkdirs();
        
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
