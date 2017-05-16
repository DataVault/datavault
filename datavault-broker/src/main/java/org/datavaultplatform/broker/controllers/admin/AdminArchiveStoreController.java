package org.datavaultplatform.broker.controllers.admin;

import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminArchiveStoreController {
    
    private ArchiveStoreService archiveStoreService;
    
    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {
        this.archiveStoreService = archiveStoreService;
    }
    
    @RequestMapping(value = "/admin/archivestores", method = RequestMethod.GET)
    public List<ArchiveStore> getArchiveStores(@RequestHeader(value = "X-UserID", required = true) String userID) {
               
        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        /*
        for (ArchiveStore store : archiveStores) {
            // For now - strip out config information
            store.setProperties(null);
        }
        */
        
        return archiveStores;
    }
    
    @RequestMapping(value = "/admin/archivestores", method = RequestMethod.POST)
    public ResponseEntity<ArchiveStore> addArchiveStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestBody ArchiveStore store) throws Exception {
        
        archiveStoreService.addArchiveStore(store);
        return new ResponseEntity<>(store, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/admin/archivestores/{archivestoreid}", method = RequestMethod.DELETE)
    public ResponseEntity<Object>  deleteArchiveStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                   @PathVariable("archivestoreid") String archivestoreid) {

        archiveStoreService.deleteArchiveStore(archivestoreid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
