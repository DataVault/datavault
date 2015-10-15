package org.datavaultplatform.broker.controllers;

import java.util.List;

import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.broker.services.ArchiveStoreService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class ArchiveStoreController {
    
    private ArchiveStoreService archiveStoreService;
    
    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {
        this.archiveStoreService = archiveStoreService;
    }
    
    @RequestMapping(value = "/archivestores", method = RequestMethod.GET)
    public List<ArchiveStore> getArchiveStores(@RequestHeader(value = "X-UserID", required = true) String userID) {
               
        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        for (ArchiveStore store : archiveStores) {
            // For now - strip out config information
            store.setProperties(null);
        }
        
        return archiveStores;
    }
    
    @RequestMapping(value = "/archivestores", method = RequestMethod.POST)
    public ArchiveStore addArchiveStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestBody ArchiveStore store) throws Exception {
        
        archiveStoreService.addArchiveStore(store);
        return store;
    }
}
