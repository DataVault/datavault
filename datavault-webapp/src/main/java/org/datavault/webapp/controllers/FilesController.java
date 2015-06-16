package org.datavault.webapp.controllers;

import org.datavault.webapp.model.FancytreeNode;
import org.datavault.webapp.services.RestService;
import java.util.ArrayList;
import java.util.Map;
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
    private String activeDir; // TODO: just for testing - ideally we won't need this information as a consumer of the API.

    public void setRestService(RestService restService) {
        this.restService = restService;
    }
    
    public void setactiveDir(String activeDir) {
        this.activeDir = activeDir;
    }

    @RequestMapping("/files")
    public @ResponseBody ArrayList<FancytreeNode> getFilesListing(HttpServletRequest request) {

        // Fancytree parameters
        String mode = request.getParameter("mode");
        String parent = request.getParameter("parent");
        String filePath = "";
        
        if (parent != null) {
            // This is a request for a sub-path
            // TODO: the broker should set proper keys instead of modifying it here.
            filePath = parent.replaceFirst(activeDir, ""); // Strip out the base path.
        }

        Map<String,String> files = restService.getFilesListing(filePath);
        ArrayList<FancytreeNode> nodes = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String path = entry.getKey();
            String type = entry.getValue();
            
            FancytreeNode node = new FancytreeNode();
            node.setKey(path);
            
            // TODO: handle filenames as part of the returned API object instead?
            String fileName = path;
            if (path.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/"));
                fileName = fileName.replaceFirst("/", "");
            }
            node.setTitle(fileName);
            
            if (type.equals("directory")) {
                node.setFolder(true);
                node.setLazy(true);
            }
            
            nodes.add(node);
        }
        
        return nodes;
    }
}
