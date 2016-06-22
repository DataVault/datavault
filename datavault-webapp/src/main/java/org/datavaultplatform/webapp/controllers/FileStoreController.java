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
    public void addLocalFilestore(@RequestParam String dirname) {
        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("rootPath", dirname);
        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Filesystem (local)");
        restService.addFileStore(store);
    }

    // Process the 'add SFTP FileStore' Ajax request
    @RequestMapping(value = "/filestores/sftp", method = RequestMethod.POST)
    @ResponseBody
    public void addSFTPFilestore(@RequestParam("hostname") String hostname, @RequestParam("port") String port, @RequestParam("path") String path, ModelMap model) {
        //todo : replace the separate parms above with one Filestore model attribute?

        // Generate a partially complete Filestore
        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("host", hostname);
        storeProperties.put("port", port);
        storeProperties.put("rootPath", path);

        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.SFTPFileSystem", storeProperties, "SFTP filesystem");
        restService.addFileStoreSFTP(store);
    }


    // Process the 'delete filestore' Ajax request
    @RequestMapping(value = "/filestores/{filestoreId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFileStore(ModelMap model, @PathVariable("filestoreId") String filestoreId) {
        restService.deleteFileStore(filestoreId);
    }




}
