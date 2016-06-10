package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * User: Robin Taylor
 * Date: 27/11/2015
 * Time: 11:00
 */
@Controller
public class FileStoreController {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreController.class);

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    // Return the 'Storage Options' page
    @RequestMapping(value = "/filestores", method = RequestMethod.GET)
    public String listFilestores(ModelMap model) {
        model.addAttribute("filestoresLocal", restService.getFileStoresLocal());
        model.addAttribute("filestoresSFTP", restService.getFileStoresSFTP());

        return "filestores/index";
    }


    // Process the 'add local FileStore' Ajax request
    @RequestMapping(value = "/filestores/local", method = RequestMethod.POST)
    @ResponseBody
    public FileStore addLocalFilestore(@RequestParam String dirname) {
        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("rootPath", dirname);
        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Filesystem (local)");
        FileStore returnedStore = restService.addFileStore(store);

        return returnedStore;
    }

    // Process the 'add keys' Ajax request
    @RequestMapping(value = "/filestores/keys", method = RequestMethod.POST)
    @ResponseBody
    public String addKeys(ModelMap model) {
        logger.info("About to addkeys in webapp controller");

        String publicKey = restService.addKeys();

        return publicKey;
    }

    // Process the 'delete filestore' Ajax request
    @RequestMapping(value = "/filestores/{filestoreId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFileStore(ModelMap model, @PathVariable("filestoreId") String filestoreId) {

        logger.info("Trying to delete filestore " + filestoreId);
        restService.deleteFileStore(filestoreId);
    }




}
