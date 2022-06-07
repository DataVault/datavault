package org.datavaultplatform.broker.initialise;

import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;


/**
 *  By default this class is enabled in the Spring XML config, to disable it just comment it out.
 */

@Component
//TODO - DHAY this class *might* be redundant - if we use flyway/liquibase to manage db
public class InitialiseDatabase {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseDatabase.class);

    private final ArchiveStoreService archiveStoreService;
    private final String archiveDir;

    private final RolesAndPermissionsService rolesAndPermissionsService;

    @Autowired
    public InitialiseDatabase(ArchiveStoreService archiveStoreService,
        @Value("${archiveDir}") String archiveDir, RolesAndPermissionsService rolesAndPermissionsService) {
        this.archiveStoreService = archiveStoreService;
        this.archiveDir = archiveDir;
        this.rolesAndPermissionsService = rolesAndPermissionsService;
    }


    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        BrokerInitialisedEvent initEvent = initialiseDatabase();
        event.getApplicationContext().publishEvent(initEvent);

    }
    public BrokerInitialisedEvent initialiseDatabase() {
        logger.info("Initialising database");

        rolesAndPermissionsService.initialiseRolesAndPermissions();

        return initialiseDataStores();
    }


    private BrokerInitialisedEvent initialiseDataStores(){

        final BrokerInitialisedEvent initEvent;

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.isEmpty()) {
            HashMap<String,String> storeProperties = new HashMap<>();
            storeProperties.put("rootPath", archiveDir);
            ArchiveStore tsm = new ArchiveStore(
                "org.datavaultplatform.common.storage.impl.TivoliStorageManager", storeProperties, "Default archive store (TSM)", true);
            //ArchiveStore s3 = new ArchiveStore("org.datavaultplatform.common.storage.impl.S3Cloud", storeProperties, "Cloud archive store", false);
            ArchiveStore oracle = new ArchiveStore("org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic", storeProperties, "Cloud archive store", false);
            //ArchiveStore lsf = new ArchiveStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Default archive store (Local)", true);
            archiveStoreService.addArchiveStore(tsm);
            //archiveStoreService.addArchiveStore(s3);
            //archiveStoreService.addArchiveStore(local);
            archiveStoreService.addArchiveStore(oracle);
            initEvent = new BrokerInitialisedEvent(this,tsm,oracle);
        }else{
            initEvent = new BrokerInitialisedEvent(this);
        }
        return initEvent;
    }
}
