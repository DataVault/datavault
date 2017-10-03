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

        for (ArchiveStore archiveStore : archiveStores) {
            if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.LocalFileSystem")) {
                localStores.add(archiveStore);
            }
        }

        model.addAttribute("archivestoresLocal", localStores);

        return "admin/archivestores/index";
    }

    // Process the 'add local ArchiveStore' Ajax request
    @RequestMapping(value = "/admin/archivestores/local", method = RequestMethod.POST)
    @ResponseBody
    public void addLocalArchivestore(@RequestParam("path") String path) {
        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("rootPath", path);
        ArchiveStore store = new ArchiveStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Default archive store (local)", false);
        restService.addArchiveStore(store);
    }

    // Process the 'delete archivestore' Ajax request
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteArchiveStore(ModelMap model, @PathVariable("archivestoreId") String archivestoreId) {
        restService.deleteArchiveStore(archivestoreId);
    }

    // Mark this archive store as being the preferred one for retrieval
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}/enable", method = RequestMethod.POST)
    @ResponseBody
    public void enableRetrieve(ModelMap model, @PathVariable("archivestoreId") String archivestoreId) {
        ArchiveStore archiveStore = restService.getArchiveStore(archivestoreId);
        archiveStore.setRetrieveEnabled(true);
        restService.editArchiveStore(archiveStore);
    }

    // Mark this archivestore as no longer being preferred for retrieval
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}/disable", method = RequestMethod.POST)
    @ResponseBody
    public void disableRetrieve(ModelMap model, @PathVariable("archivestoreId") String archivestoreId) {
        ArchiveStore archiveStore = restService.getArchiveStore(archivestoreId);
        archiveStore.setRetrieveEnabled(false);
        restService.editArchiveStore(archiveStore);
    }


}
