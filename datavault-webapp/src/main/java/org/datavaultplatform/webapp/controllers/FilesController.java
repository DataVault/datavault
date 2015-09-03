package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.webapp.model.FancytreeNode;
import org.datavaultplatform.webapp.services.RestService;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
