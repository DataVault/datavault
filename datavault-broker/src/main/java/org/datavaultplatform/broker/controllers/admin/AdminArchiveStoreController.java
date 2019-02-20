package org.datavaultplatform.broker.controllers.admin;

import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminArchiveStoreController {

    private ArchiveStoreService archiveStoreService;

    private static final Logger logger = LoggerFactory.getLogger(AdminArchiveStoreController.class);

    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {
        this.archiveStoreService = archiveStoreService;
    }

    @RequestMapping(value = "/admin/archivestores", method = RequestMethod.GET)
    public ResponseEntity<List<ArchiveStore>> getArchiveStores(@RequestHeader(value = "X-UserID", required = true) String userID) {

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        return new ResponseEntity<>(archiveStores, HttpStatus.OK);
    }

    @RequestMapping(value = "/admin/archivestores/{archivestoreid}", method = RequestMethod.GET)
    public ResponseEntity<ArchiveStore> getArchiveStore(@RequestHeader(value = "X-UserID", required = true) String userID, @PathVariable("archivestoreid") String archivestoreid) {

        return new ResponseEntity<>(archiveStoreService.getArchiveStore(archivestoreid), HttpStatus.OK);
    }

    @RequestMapping(value = "/admin/archivestores", method = RequestMethod.POST)
    public ResponseEntity<ArchiveStore> addArchiveStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                        @RequestBody ArchiveStore store) throws Exception {
        try{
            archiveStoreService.addArchiveStore(store);
        }catch(Exception e){
            System.err.println("Couldn't add archive store: "+ e.getMessage());
            return new ResponseEntity<>(store, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(store, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/admin/archivestores", method = RequestMethod.PUT)
    public ResponseEntity<ArchiveStore> editArchiveStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                         @RequestBody ArchiveStore store) throws Exception {

        archiveStoreService.updateArchiveStore(store);
        return new ResponseEntity<>(store, HttpStatus.OK);
    }

    @RequestMapping(value = "/admin/archivestores/{archivestoreid}", method = RequestMethod.DELETE)
    public ResponseEntity<Object>  deleteArchiveStore(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                      @PathVariable("archivestoreid") String archivestoreid) {

        archiveStoreService.deleteArchiveStore(archivestoreid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
