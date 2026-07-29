package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.FilesService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.io.DataVaultFileUtils;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.response.DepositSize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Set;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 13:21
 */


@RestController
@Slf4j
public class FilesController {

    public static final String FILE_INFORMATION_NOT_AVAILABLE = "File information not available.";
    private final FilesService filesService;
    private final UsersService usersService;
    private final AdminService adminService;
    private final String tempDir;
    private final Long maxDepositByteSize;
    private final Long maxAdminDepositByteSize;

    public FilesController(FilesService filesService, UsersService usersService,
                           AdminService adminService,
                           @Value("${tempDir}") String tempDir,
                           @Value("${max.deposit.size}") String maxDepositByteSize,
                           @Value("${max.admin.deposit.size}") String maxAdminDepositByteSize) {
        this.filesService = filesService;
        this.usersService = usersService;
        this.adminService = adminService;
        this.tempDir = tempDir;
        this.maxDepositByteSize = DataVaultFileUtils.parseFormattedSizeToBytes(maxDepositByteSize);
        this.maxAdminDepositByteSize = DataVaultFileUtils.parseFormattedSizeToBytes(maxAdminDepositByteSize);
    }

    @GetMapping(value = "/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileInfo> getStorageListing(@RequestHeader(HEADER_USER_ID) String userId) {
        
        User user = usersService.getUser(userId);
        
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

    @GetMapping(value = "/files/{storageId}/{*filePath}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileInfo> getFilesListing(@RequestHeader(HEADER_USER_ID) String userId,
                                          @PathVariable String storageId,
                                          @Parameter(description = "The relative path to the file (captured greedily)", example = "backups/2023/data.zip")
                                          @PathVariable String filePath) throws Exception {

        User user = usersService.getUser(userId);
        
        FileStore store = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore userStore : userStores) {
            if (userStore.getID().equals(storageId)) {
                store = userStore;
            }
        }
        
        if (store == null) {
            throw new Exception("Storage device '" + storageId + "' not found!");
        }
        
        // "GET /files/storageid" will display files from the base directory.
        // "GET /files/storageid/abc" will display files from the "abc" directory under the base.
        
        List<FileInfo> files = filesService.getFilesListing(filePath, store);
        
        // Add the storage key to the start of the returned path/key
        for (FileInfo file : files) {
            String fullKey = file.getKey();
            
            if (!fullKey.startsWith("/")) {
                fullKey = "/" + fullKey;
            }
            
            fullKey = storageId + fullKey;
            file.setKey(fullKey);
        }
        
        return files;
    }

    @Operation(summary = "Get file size", description = "Gets filesize of storageId/<filepath>")
    //"5 GB"
    @ApiResponse(
            responseCode = "200",
            description = "gets filesize of storageId/<filepath>",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(
                            type = "string",
                            description = "english description of size",
                            examples = {"5 GB", FILE_INFORMATION_NOT_AVAILABLE}
                    )
            )
    )
    @GetMapping(value = "/filesize/{storageId}/{*filePath}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getFilesize(
            @RequestHeader(HEADER_USER_ID) String userId,
            @PathVariable String storageId,
            @Parameter(description = "The relative path to the file (captured greedily)", example = "backups/2023/data.zip")
            @PathVariable String filePath
    ) throws Exception {
        
        User user = usersService.getUser(userId);
        
        FileStore store = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore userStore : userStores) {
            if (userStore.getID().equals(storageId)) {
                store = userStore;
            }
        }
        
        if (store == null) {
            throw new Exception("Storage device '" + storageId + "' not found!");
        }
        
        Long size = filesService.getFilesize(filePath, store);
        
        if (size == null) {
            return FILE_INFORMATION_NOT_AVAILABLE;
        } else {
            return DataVaultFileUtils.getGibibyteSizeStr(size);
        }
    }

    //"5 GB"
    @ApiResponse(
            responseCode = "200",
            description = "gets combined filesize of selected files",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(
                            type = "string",
                            description = "english description of size",
                            examples = {"5 GB", FILE_INFORMATION_NOT_AVAILABLE}
                    )
            )
    )
    @GetMapping(value = "/sizeofselectedfiles", produces = MediaType.TEXT_PLAIN_VALUE)
    public String sizeOfSelectedFiles(
            @RequestHeader(HEADER_USER_ID) String userId,
            @RequestParam(value = "filepath") List<String> filePaths
    ) {
        log.info("Start of sizeOfSelectedFiles");

        // Get storage id from the file path of first
        if (filePaths.size() > 0) {
            // Start timing
            long start = System.nanoTime();

            Set<String> storageIDSet = new HashSet<>();
            for (String filePath : filePaths) {
                String fileStorageID = filePath.split("/")[1];
                storageIDSet.add(fileStorageID);
            }

            long size = 0L;
            for (String storageID: storageIDSet) {
                log.info("storageID: " + storageID);
                User user = usersService.getUser(userId);
                FileStore store = null;
                List<FileStore> userStores = user.getFileStores();
                
                for (FileStore userStore : userStores) {
                    if (userStore.getID().equals(storageID)) {
                        store = userStore;
                    }
                }
        
                if (store == null) {
                    // throw new Exception("Storage device '" + storageID + "' not found!");
                    return FILE_INFORMATION_NOT_AVAILABLE + " Storage device '" + storageID + "' not found!.";
                }
            
                for (String filePath : filePaths) {
                    log.info("Loop filePaths: storageID: " + storageID);
                    log.info("Loop filePaths: filePath: " + filePath);
                    if (filePath.startsWith("/" + storageID)) {
                        String modifiedFilepath = filePath.replace("/" + storageID, "");
                        log.info("Loop filePaths: filePath: " + filePath);
                        Long fSize = filesService.getFilesize(modifiedFilepath, store);
                        log.info("Loop filePaths: fSize: " + fSize);
                        if(fSize != null) {
                           size += fSize;
                        }
                        log.info("Loop filePaths: size: " + size);
                    }
                }

                long finish = System.nanoTime();
                long timeElapsed = TimeUnit.SECONDS.convert(finish - start, TimeUnit.NANOSECONDS);
                log.info("sizeOfSelectedFiles(): timeElapsed for calculating size: " + timeElapsed + " seconds");
            }  
            if (size == 0L) {
                return FILE_INFORMATION_NOT_AVAILABLE;
            } else {
                return DataVaultFileUtils.getGibibyteSizeStr(size);
            }
        } else {
            return FILE_INFORMATION_NOT_AVAILABLE;
        }
    }

    @GetMapping(value = "/checkdepositsize", produces = MediaType.APPLICATION_JSON_VALUE)
    public DepositSize checkDepositSize(@RequestHeader(HEADER_USER_ID) String userId,
                              HttpServletRequest request) throws Exception {

        User user = usersService.getUser(userId);
        String[] filePaths = request.getParameterValues("filepath");

        // Start timing
        long start = System.nanoTime();

        Long size = 0L;
        for (String filePath : filePaths) {
            log.info("Full filePath: " + filePath);
            filePath = filePath.replaceFirst("/storage/", "");
            // remove first slashes
            filePath = filePath.replaceFirst("^\\/+", "");
            log.info("Full filePath cleaned: " + filePath);
            String storageID = filePath.substring(0,filePath.indexOf("/"));
            log.info("storageID: " + storageID);
            filePath = filePath.substring(filePath.indexOf("/"));
            log.info("filePath: " + filePath);

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

            log.info("size: " + size);

            Long fileSize = filesService.getFilesize(filePath, store);

            size += fileSize;
        }

        log.info("Total size: " + size);
        String sizeWithUnits = DataVaultFileUtils.getGibibyteSizeStr(size);
        log.info("checkDepositSize() - sizeWithUnits:" + sizeWithUnits);
        long finish = System.nanoTime();
        long timeElapsed = TimeUnit.SECONDS.convert(finish - start, TimeUnit.NANOSECONDS);
        log.info("checkDepositSize(): timeElapsed for calculating size: " + timeElapsed + " seconds");

        Long max = (this.adminService.isAdminUser(user)) ? this.maxAdminDepositByteSize : this.maxDepositByteSize;
        DepositSize retVal = new DepositSize();
        retVal.setMax(max);
        retVal.setSizeWithUnits(sizeWithUnits);

        log.info("Max (broker) is:" + max);
        if (size <= max) {
            retVal.setResult(Boolean.TRUE);
        } else {
            retVal.setResult(Boolean.FALSE);
        }
        log.info("retVal: " + retVal);
        return retVal;
    }

    @Operation(
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "post a file chunk",
                            content = @Content(
                                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                                    schema = @Schema(
                                            type = "string",
                                            description = "Always an empty string",
                                            allowableValues = {""}
                                    )
                            )
                    ) // This was the missing closing brace for @ApiResponse
            }
    )
    @PostMapping(value = "/upload/{fileUploadHandle}/{filename:.+}",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String postFileChunk(
            @RequestHeader(HEADER_USER_ID) String userId,
            @PathVariable String fileUploadHandle,
            @PathVariable String filename,
            @Parameter(
                    description = "Base64 encoded relative path",
                    schema = @Schema(type = "string"),
                    examples = {@ExampleObject("L3Vzci9sb2NhbC90ZW1w")}
            )
            @RequestParam String relativePathBase64, // Decoded in logic
            @RequestParam long chunkNumber,
            @RequestParam long chunkSize,
            @Parameter(description = "bytes of file chunk")
            @RequestBody byte[] chunkBytes
    ) throws Exception {

        // Spring automatically parses the Longs for you!
 
        User user = usersService.getUser(userId);

        log.info("Broker postFileChunk for " + user.getID() + " - " + filename);

        String relativePath = new String(Base64.decodeBase64(relativePathBase64.getBytes()));

        //Long totalChunks = Long.parseLong(request.getParameter("totalChunks"));
        //Long totalSize = Long.parseLong(request.getParameter("totalSize"));
        
        log.info("fileUploadHandle =" + fileUploadHandle);
        
        /*
        log.info("Broker postFileChunk:" +
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
            log.info("Creating uploadDir: " + uploadDir.getPath());
            boolean success = uploadDir.mkdir();
            if (!success && !uploadDir.exists()) {
                throw new Exception("Unable to create uploadDir");
            }
        }
        
        // Create the directory for this user
        Path userUploadDirPath = uploadDirPath.resolve(userId);
        File userUploadDir = userUploadDirPath.toFile();
        if (!userUploadDir.exists()) {
            log.info("Creating userUploadDir: " + userUploadDir.getPath());
            boolean success = userUploadDir.mkdir();
            if (!success && !userUploadDir.exists()) {
                throw new Exception("Unable to create userUploadDir");
            }
        }
        
        // Create the upload directory within the user directory
        Path handleUploadDirPath = userUploadDirPath.resolve(fileUploadHandle);
        File handleUploadDir = handleUploadDirPath.toFile();
        if (!handleUploadDir.exists()) {
            log.info("Creating handleUploadDir: " + handleUploadDir.getPath());
            boolean success = handleUploadDir.mkdir();
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
        
        // Write chunk bytes to file
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            // Seek to the correct offset
            raf.seek((chunkNumber - 1) * chunkSize);

            // Write the byte array directly
            raf.write(chunkBytes);
        } // raf.close() is called automatically here, even if an error occurs
        return "";
    }
}
