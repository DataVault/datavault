package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(RestService.class)
public class FileStoreController {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreController.class);

    private final RestService restService;
    private final String activeDir;
    private final String sftpHost;
    private final String sftpPort;
    private final String sftpRootPath;

    @Autowired
    public FileStoreController(RestService restService,
        @Value("${activeDir}") String activeDir,
        @Value("${sftp.host}") String sftpHost,
        @Value("${sftp.port}") String sftpPort,
        @Value("${sftp.rootPath}") String sftpRootPath) {
        this.restService = restService;
        this.activeDir = activeDir;
        this.sftpHost = sftpHost;
        this.sftpPort = sftpPort;
        this.sftpRootPath = sftpRootPath;
    }

    // Return the 'Storage Options' page
    @RequestMapping(value = "/filestores", method = RequestMethod.GET)
    public String listFilestores(ModelMap model) throws Exception {
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
    public void addLocalFilestore(@RequestParam("path") String path) throws Exception {
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
    public void addSFTPFilestore(@RequestParam("hostname") String hostname, @RequestParam("port") String port, @RequestParam("path") String path, ModelMap model) throws Exception {
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
    public void deleteFileStore(ModelMap model, @PathVariable("filestoreId") String filestoreId) throws Exception {
        restService.deleteFileStore(filestoreId);
    }




}
