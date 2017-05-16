package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;


/**
 *  By default this class is enabled in the Spring XML config, to disable it just comment it out.
 */

@Component
public class InitialiseDatabase {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseDatabase.class);

    private ArchiveStoreService archiveStoreService;
    private String archiveDir;

    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {
        this.archiveStoreService = archiveStoreService;
    }

    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }


    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {

        logger.info("Initialising database");

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.isEmpty()) {
            HashMap<String,String> storeProperties = new HashMap<String,String>();
            storeProperties.put("rootPath", archiveDir);
            ArchiveStore store = new ArchiveStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Default archive store (local)");
            archiveStoreService.addArchiveStore(store);
        }

    }
}
