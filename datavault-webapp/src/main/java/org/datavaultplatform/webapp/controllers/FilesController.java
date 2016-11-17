package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.webapp.model.FancytreeNode;
import org.datavaultplatform.webapp.services.RestService;
import java.util.ArrayList;

import javax.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

@Controller
public class FilesController {

    private RestService restService;
    
    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public ArrayList<FancytreeNode> getNodes(String parent, boolean directoryOnly) {

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
    public @ResponseBody ArrayList<FancytreeNode> getFilesListing(HttpServletRequest request) {

        // Fancytree parameters
        String mode = request.getParameter("mode");
        String parent = request.getParameter("parent");
        
        return getNodes(parent, false);
    }
    
    @RequestMapping("/dir")
    public @ResponseBody ArrayList<FancytreeNode> getDirListing(HttpServletRequest request) {

        // Fancytree parameters
        String mode = request.getParameter("mode");
        String parent = request.getParameter("parent");
        
        return getNodes(parent, true);
    }
    
    @RequestMapping("/filesize")
    public @ResponseBody String getFilesize(HttpServletRequest request) {

        String filepath = request.getParameter("filepath");
        
        return restService.getFilesize(filepath);
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
        System.out.println("webapp fileupload:" +
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
        restService.addFileChunk(fileUploadHandle, flowFilename, flowChunkNumber, flowTotalChunks, flowChunkSize, flowTotalSize, file.getBytes());
        
        // Send a response to the client
        java.io.PrintWriter wr = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        wr.flush();
        wr.close();
    }
}
