package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(RestService.class)
public class AdminArchiveStoreController {

    private static final Logger logger = LoggerFactory.getLogger(AdminArchiveStoreController.class);

    private final RestService restService;
    private final String archiveDir;

    @Autowired
    public AdminArchiveStoreController(
        RestService restService, @Value("${archiveDir}") String archiveDir) {
        this.restService = restService;
        this.archiveDir = archiveDir;
    }

    // Return the 'Archive Stores' page
    @RequestMapping(value = "/admin/archivestores", method = RequestMethod.GET)
    public String listArchivestores(ModelMap model) throws Exception {
        model.addAttribute("archiveDir", archiveDir);

        ArchiveStore[] archiveStores = restService.getArchiveStores();
        List<ArchiveStore> stores = new ArrayList<>();

        for (ArchiveStore archiveStore : archiveStores) {
//            if (archiveStore.isLocalFileSystem()) throws Exception {
//                localStores.add(archiveStore);
//            }
            stores.add(archiveStore);
        }

        model.addAttribute("archivestores", stores);

        return "admin/archivestores/index";
    }

    // Process the 'add local ArchiveStore' Ajax request
    @RequestMapping(value = "/admin/archivestores/local", method = RequestMethod.POST)
    @ResponseBody
    public void addLocalArchivestore(@RequestParam(value="properties",required=false) String properties,
                                     @RequestParam(value="label") String label,
                                     @RequestParam(value="type") String type,
                                     @RequestParam(value="retrieve",required=false) String retrieve) throws Exception {

        String storageClass = StorageConstants.getStorageClass(type).orElseThrow (
            () -> new IllegalArgumentException(String.format("The type[%s] is not valid", type))
        );

        boolean retrieveEnabled = "on".equals(retrieve);

        HashMap<String,String> storeProperties = buildStoreProperties(properties);

        ArchiveStore store = new ArchiveStore(storageClass, storeProperties, label, retrieveEnabled);
        restService.addArchiveStore(store);
    }

    // Process the 'delete archivestore' Ajax request
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteArchiveStore(ModelMap model, @PathVariable("archivestoreId") String archivestoreId) throws Exception {
        restService.deleteArchiveStore(archivestoreId);
    }

    // Mark this archive store as being the preferred one for retrieval
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}/enable", method = RequestMethod.POST)
    @ResponseBody
    public void enableRetrieve(ModelMap model, @PathVariable("archivestoreId") String archivestoreId) throws Exception {
        ArchiveStore archiveStore = restService.getArchiveStore(archivestoreId);
        archiveStore.setRetrieveEnabled(true);
        restService.editArchiveStore(archiveStore);
    }

    // Mark this archivestore as no longer being preferred for retrieval
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}/disable", method = RequestMethod.POST)
    @ResponseBody
    public void disableRetrieve(ModelMap model, @PathVariable("archivestoreId") String archivestoreId) throws Exception {
        ArchiveStore archiveStore = restService.getArchiveStore(archivestoreId);
        archiveStore.setRetrieveEnabled(false);
        restService.editArchiveStore(archiveStore);
    }

    // Process the 'update properties archivestore' Ajax request
    @RequestMapping(value = "/admin/archivestores/{archivestoreId}/update/properties", method = RequestMethod.POST)
    @ResponseBody
    public void updateArchiveStore(ModelMap model,
                                   @PathVariable("archivestoreId") String archivestoreId,
                                   @RequestParam("properties") String properties) throws Exception {
        ArchiveStore archiveStore = restService.getArchiveStore(archivestoreId);
        HashMap<String,String> storeProperties = buildStoreProperties(properties);
        archiveStore.setProperties(storeProperties);
        restService.editArchiveStore(archiveStore);
    }

    private HashMap<String,String> buildStoreProperties(String properties){
        HashMap<String,String> storeProperties = new HashMap<>();

        String[] lines = properties.split("\\r?\\n");
        int lineCount = 0;
        for(String line : lines){
            lineCount++;
            if(line.contains("=")){
                String[] prop = line.split("=");
                if(prop.length == 2) {
                    logger.info(prop[0].trim()+" = "+prop[1].trim());
                    storeProperties.put(prop[0].trim(), prop[1].trim());
                }else{
                    System.err.println("line #"+lineCount+" has wrong syntax: "+line);
                }
            }else{
                System.err.println("line #"+lineCount+" has wrong syntax: "+line);
            }
        }
        return storeProperties;
    }
}
