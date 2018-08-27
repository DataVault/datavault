package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.FileStore;
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
    private String activeDir;
    private String sftpHost;
    private String sftpPort;
    private String sftpRootPath;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setActiveDir(String activeDir) {
        this.activeDir = activeDir;
    }

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public void setSftpPort(String sftpPort) {
        this.sftpPort = sftpPort;
    }

    public void setSftpRootPath(String sftpRootPath) {
        this.sftpRootPath = sftpRootPath;
    }

    // Return the 'Storage Options' page
    @RequestMapping(value = "/filestores", method = RequestMethod.GET)
    public String listFilestores(ModelMap model) {
        model.addAttribute("activeDir", activeDir);
        model.addAttribute("sftpHost", sftpHost);
        model.addAttribute("sftpPort", sftpPort);
        model.addAttribute("sftpRootPath", sftpRootPath);
        model.addAttribute("filestoresLocal", restService.getFileStoresLocal());
        model.addAttribute("filestoresSFTP", restService.getFileStoresSFTP());

        return "filestores/index";
    }

    // Process the 'add local FileStore' Ajax request
    @RequestMapping(value = "/filestores/local", method = RequestMethod.POST)
    @ResponseBody
    public void addLocalFilestore(@RequestParam("path") String path) {
        HashMap<String,String> storeProperties = new HashMap<>();
        // In theory we could allow the user to define the path, however that would allow them access to anything that the
        // Datavault app can read. So for now we will just use the configured default value.
        //storeProperties.put("rootPath", path);
        storeProperties.put("rootPath", activeDir);
        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Filesystem (local)");
        restService.addFileStore(store);
    }

    // Process the 'add SFTP FileStore' Ajax request
    @RequestMapping(value = "/filestores/sftp", method = RequestMethod.POST)
    @ResponseBody
    public void addSFTPFilestore(@RequestParam("hostname") String hostname, @RequestParam("port") String port, @RequestParam("path") String path, ModelMap model) {
        //todo : replace the separate parms above with one Filestore model attribute?

        // Generate a partially complete Filestore
        HashMap<String,String> storeProperties = new HashMap<>();
        storeProperties.put("host", hostname);
        storeProperties.put("port", port);
        storeProperties.put("rootPath", path);

        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.SFTPFileSystem", storeProperties, path);
        restService.addFileStoreSFTP(store);
    }


    // Process the 'delete filestore' Ajax request
    @RequestMapping(value = "/filestores/{filestoreId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFileStore(ModelMap model, @PathVariable("filestoreId") String filestoreId) {
        restService.deleteFileStore(filestoreId);
    }




}
