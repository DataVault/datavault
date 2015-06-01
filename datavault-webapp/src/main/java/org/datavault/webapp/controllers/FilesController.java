package org.datavault.webapp.controllers;


import org.datavault.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:32
 */

@Controller
@RequestMapping("/files")
public class FilesController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getFilesListing(ModelMap model) {

        model.addAttribute("files", restService.getFilesListing());

        return "files";
    }


}
