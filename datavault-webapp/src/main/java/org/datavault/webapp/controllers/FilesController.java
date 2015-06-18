package org.datavault.webapp.controllers;

import org.datavault.common.model.FileInfo;
import org.datavault.webapp.model.FancytreeNode;
import org.datavault.webapp.services.RestService;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

@Controller
public class FilesController {

    private RestService restService;
    
    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping("/files")
    public @ResponseBody ArrayList<FancytreeNode> getFilesListing(HttpServletRequest request) {

        // Fancytree parameters
        String mode = request.getParameter("mode");
        String parent = request.getParameter("parent");
        String filePath = "";
        
        if (parent != null) {
            // This is a request for a sub-path
            filePath = parent;
        }

        FileInfo[] files = restService.getFilesListing(filePath);
        ArrayList<FancytreeNode> nodes = new ArrayList<>();
        
        for (FileInfo info : files) {
            
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
}
