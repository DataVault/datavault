package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
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
public class FileStoreController implements FileStoreControllerApi {

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
    @Override
    @GetMapping(value = "/filestores", produces = MediaType.TEXT_HTML_VALUE)
    public String listFilestores(ModelMap model) throws Exception{
        model.addAttribute("activeDir", activeDir);
        model.addAttribute("sftpHost", sftpHost);
        model.addAttribute("sftpPort", sftpPort);
        model.addAttribute("sftpRootPath", sftpRootPath);
        model.addAttribute("filestoresLocal", restService.getFileStoresLocal());
        model.addAttribute("filestoresSFTP", restService.getFileStoresSFTP());

        return "filestores/index";
    }

    // Process the 'add local FileStore' Ajax request
    @Override
    @PostMapping(value = "/filestores/local", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void addLocalFilestore(@RequestParam("path") String path) throws Exception {
        HashMap<String,String> storeProperties = new HashMap<>();
        // In theory we could allow the user to define the path, however that would allow them access to anything that the
        // Datavault app can read. So for now we will just use the configured default value.
        //storeProperties.put("rootPath", path);
        storeProperties.put(PropNames.ROOT_PATH, activeDir);
        FileStore store = new FileStore(StorageConstants.LOCAL_FILE_SYSTEM, storeProperties, "Filesystem (local)");
        restService.addFileStore(store);
    }

    // Process the 'add SFTP FileStore' Ajax request
    @Override
    @PostMapping(value = "/filestores/sftp", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void addSFTPFilestore(@RequestParam("hostname") String hostname, @RequestParam("port") String port, @RequestParam("path") String path, ModelMap model) throws Exception {
        //todo : replace the separate parms above with one Filestore model attribute?

        // Generate a partially complete Filestore
        HashMap<String,String> storeProperties = new HashMap<>();
        storeProperties.put(PropNames.HOST, hostname);
        storeProperties.put(PropNames.PORT, port);
        storeProperties.put(PropNames.ROOT_PATH, path);

        FileStore store = new FileStore(StorageConstants.SFTP_FILE_SYSTEM, storeProperties, path);
        restService.addFileStoreSFTP(store);
    }


    @Override
    @DeleteMapping(value = "/filestores/{fileStoreId}")
    @ResponseBody
    public void deleteFileStore(ModelMap model, @PathVariable String fileStoreId) {
        restService.deleteFileStore(fileStoreId);
    }




}
