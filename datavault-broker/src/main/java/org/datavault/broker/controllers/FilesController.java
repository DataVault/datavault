package org.datavault.broker.controllers;

import org.datavault.common.model.Files;
import org.datavault.broker.services.MacFilesService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import javax.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 13:21
 */


@RestController
public class FilesController {
    
    private MacFilesService macFilesService;

    public void setMacFilesService(MacFilesService macFilesService) {
        this.macFilesService = macFilesService;
    }

    @RequestMapping("/files/**")
    public Files getFilesListing(HttpServletRequest request) {
        
        // "GET /files/" will display files from the base directory.
        // "GET /files/abc" will display files from the "abc" directory under the base.
        
        // TODO: is there a cleaner way to extract the request path?
        String requestPath = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String filePath = requestPath.replaceFirst("^/files", "");
        
        return macFilesService.getFilesListing(filePath);
    }


}
