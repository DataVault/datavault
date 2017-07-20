package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Robin Taylor
 * Date: 27/11/2015
 * Time: 11:00
 */
@Controller
public class AdminArchiveStoreController {

    private static final Logger logger = LoggerFactory.getLogger(AdminArchiveStoreController.class);

    private RestService restService;
    private String archiveDir;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }

    // Return the 'Archive Stores' page
    @RequestMapping(value = "/admin/archivestores", method = RequestMethod.GET)
    public String listArchivestores(ModelMap model) {
        model.addAttribute("archiveDir", archiveDir);

        ArchiveStore[] archiveStores = restService.getArchiveStores();
        List<ArchiveStore> localStores = new ArrayList<>();
        List<ArchiveStore> oracleStores = new ArrayList<>();

        for (ArchiveStore archiveStore : archiveStores) {
            if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.LocalFileSystem")) {
                localStores.add(archiveStore);
            } else if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.OracleArchive")) {
                oracleStores.add(archiveStore);
            }
        }

        model.addAttribute("archivestoresLocal", localStores);
        model.addAttribute("archivestoresOracle", oracleStores);

        return "admin/archivestores/index";
    }

    // Process the 'add local ArchiveStore' Ajax request
    @RequestMapping(value = "/admin/archivestores/local", method = RequestMethod.POST)
    @ResponseBody
    public void addLocalArchivestore(@RequestParam("path") String path) {
        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("rootPath", path);

        ArchiveStore store = new ArchiveStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Default archive store (local)");
        restService.addArchiveStore(store);
    }

    // Process the 'add Oracle ArchiveStore' Ajax request
    @RequestMapping(value = "/admin/archivestores/oracle", method = RequestMethod.POST)
    @ResponseBody
    public void addOracleArchivestore(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("serviceName") String serviceName, @RequestParam("serviceUrl") String serviceUrl, @RequestParam("identityDomain") String identityDomain, @RequestParam("containerName") String containerName) {
        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("username", username);
        storeProperties.put("password", password);
        storeProperties.put("serviceName", serviceName);
        storeProperties.put("serviceUrl", serviceUrl);
        storeProperties.put("identityDomain", identityDomain);
        storeProperties.put("containerName", containerName);

        ArchiveStore store = new ArchiveStore("org.datavaultplatform.common.storage.impl.OracleArchive", storeProperties, "Archive store (Oracle)");
        restService.addArchiveStore(store);
    }

    // Process the 'delete archivestore' Ajax request
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteArchiveStore(ModelMap model, @PathVariable("archivestoreId") String archivestoreId) {
        restService.deleteArchiveStore(archivestoreId);
    }


}
