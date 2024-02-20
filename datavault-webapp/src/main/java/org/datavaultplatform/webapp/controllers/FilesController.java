package org.datavaultplatform.webapp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.FileUtils;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.response.DepositSize;
import org.datavaultplatform.webapp.model.FancytreeNode;
import org.datavaultplatform.webapp.services.RestService;
import java.util.ArrayList;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import org.apache.commons.codec.binary.Base64;

@Controller
@ConditionalOnBean(RestService.class)
@Slf4j
public class FilesController {

    private final RestService restService;

    @Autowired
    public FilesController(RestService restService) {
        this.restService = restService;
    }

    public ArrayList<FancytreeNode> getNodes(String parent, boolean directoryOnly) throws Exception{

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
    
    @RequestMapping("/files")
    public @ResponseBody ArrayList<FancytreeNode> getFilesListing(HttpServletRequest request) throws Exception{

        // Fancytree parameters
        String mode = request.getParameter("mode");
        String parent = request.getParameter("parent");
        
        return getNodes(parent, false);
    }
    
    @RequestMapping("/dir")
    public @ResponseBody ArrayList<FancytreeNode> getDirListing(HttpServletRequest request) throws Exception{

        // Fancytree parameters
        String mode = request.getParameter("mode");
        String parent = request.getParameter("parent");
        
        return getNodes(parent, true);
    }
    
    @RequestMapping("/filesize")
    public @ResponseBody String getFilesize(HttpServletRequest request) throws Exception{

        String filepath = request.getParameter("filepath");
        
        return restService.getFilesize(filepath);
    }

    @RequestMapping("/checkdepositsize")
    public @ResponseBody String checkDepositSize(HttpServletRequest request) throws Exception{

        String[] filePaths = request.getParameterValues("filepath[]");

        for(String filePath : filePaths){
            log.info("filePaths: " + filePath);
        }

        DepositSize result = restService.checkDepositSize(filePaths);
        Boolean success = result.getResult();
        String max = FileUtils.getGibibyteSizeStr(result.getMax());
        log.info("Max deposit (web): " + max);
        return "{ \"success\":\"" + success + "\", \"max\":\"" + max + "\"}";
    }
    
    @RequestMapping(value = "/fileupload", method = RequestMethod.POST)
    public void fileUpload(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String flowChunkNumber = request.getParameter("flowChunkNumber");
        String flowTotalChunks = request.getParameter("flowTotalChunks");
        String flowChunkSize = request.getParameter("flowChunkSize");
        String flowTotalSize = request.getParameter("flowTotalSize");
        String flowIdentifier = request.getParameter("flowIdentifier");
        String flowFilename = request.getParameter("flowFilename");
        String flowRelativePath = request.getParameter("flowRelativePath");
        String fileUploadHandle = request.getParameter("fileUploadHandle");
        
        /*
        logger.info("webapp fileupload:" +
                " flowChunkNumber=" + flowChunkNumber +
                " flowTotalChunks=" + flowTotalChunks +
                " flowChunkSize=" + flowChunkSize +
                " flowTotalSize=" + flowTotalSize +
                " flowIdentifier=" + flowIdentifier +
                " flowFilename=" + flowFilename +
                " flowRelativePath=" + flowRelativePath +
                " fileUploadHandle=" + fileUploadHandle);
        */
        
        MultipartFile file = request.getFile("file");
        
        // Send this chunk to the broker
        String encodedRelativePath = new String(Base64.encodeBase64(flowRelativePath.getBytes()));
        restService.addFileChunk(fileUploadHandle, flowFilename, encodedRelativePath, flowChunkNumber, flowTotalChunks, flowChunkSize, flowTotalSize, file.getBytes());
        
        // Send a response to the client
        java.io.PrintWriter wr = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        wr.flush();
        wr.close();
    }
}
