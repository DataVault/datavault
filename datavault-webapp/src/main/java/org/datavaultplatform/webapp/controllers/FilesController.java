package org.datavaultplatform.webapp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.DataVaultFileUtils;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.response.DepositSize;
import org.datavaultplatform.webapp.model.FancytreeNode;
import org.datavaultplatform.webapp.services.RestService;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import org.apache.commons.codec.binary.Base64;

@RestController
@ConditionalOnBean(RestService.class)
@Slf4j
public class FilesController implements FilesControllerApi {

    private final RestService restService;

    @Autowired
    public FilesController(RestService restService) {
        this.restService = restService;
    }

    @Override
    public ArrayList<FancytreeNode> getNodes(String parent, boolean directoryOnly) throws Exception {

        String filePath = "";
        
        if (parent != null) {
            // This is a request for a sub-path
            filePath = parent;
        }

        FileInfo[] files = restService.getFilesListing(filePath);
        ArrayList<FancytreeNode> nodes = new ArrayList<>();
        
        for (FileInfo info : files) {
            
            if (directoryOnly && !info.getIsDirectory()) {
                continue;
            }
            
            FancytreeNode node = new FancytreeNode();
            node.setKey(info.getKey());
            node.setTitle(info.getName());
            
            if (info.getIsDirectory()) {
                node.setFolder(true);
                node.setLazy(true);
            }

            nodes.add(node);
        }
        
        return nodes;
    }

    @Override
    @GetMapping(value = "/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public ArrayList<FancytreeNode> getFilesListing(
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "parent", required = false) String parent) throws Exception {

        // The 'mode' parameter is currently not used in the getNodes method,
        // but it's included here for completeness if Fancytree requires it.
        log.debug("Fancytree mode: {}", mode);
        
        return getNodes(parent, false);
    }
    
    @Override
    @GetMapping(value = "/dir", produces = MediaType.APPLICATION_JSON_VALUE)
    public ArrayList<FancytreeNode> getDirListing(String mode, String parent) throws Exception{

        // The 'mode' parameter is currently not used in the getNodes method,
        // but it's included here for completeness if Fancytree requires it.
        log.debug("Fancytree mode: {}", mode);
        
        return getNodes(parent, true);
    }

    @Override
    @GetMapping(value = "/filesize", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getFilesize(@RequestParam("filepath") String filepath) {
        
        return restService.getFilesize(filepath);
    }

    @Override
    @GetMapping(value = "/checkdepositsize", produces = MediaType.APPLICATION_JSON_VALUE)
    public CheckDepositSizeResponse checkDepositSize(@RequestParam(value = "filepath[]") String[] filePaths) {

        for(String filePath : filePaths){
            log.info("filePaths: " + filePath);
        }

        DepositSize result = restService.checkDepositSize(filePaths);
        Boolean success = result.getResult();
        String max = DataVaultFileUtils.getGibibyteSizeStr(result.getMax());
        log.info("Max deposit (web): {}", max);
        return new CheckDepositSizeResponse(success, max);
    }

    @Override
    @PostMapping(value = "/fileupload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> fileUpload(
            @ModelAttribute FileUploadRequest metadata,
            @RequestPart("file") MultipartFile file) throws Exception {

        String encodedRelativePath = new String(Base64.encodeBase64(metadata.getFlowRelativePath().getBytes()));

        restService.addFileChunk(
                metadata.getFileUploadHandle(),
                metadata.getFlowFilename(),
                encodedRelativePath,
                metadata.getFlowChunkNumber(),
                metadata.getFlowTotalChunks(),
                metadata.getFlowChunkSize(),
                metadata.getFlowTotalSize(),
                file.getBytes()
        );

        return ResponseEntity.ok().build();
    }
}
