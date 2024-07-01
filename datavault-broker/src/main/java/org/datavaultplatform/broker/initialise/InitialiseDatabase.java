package org.datavaultplatform.broker.initialise;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.config.ConfigUtils;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


/**
 *  By default this class is enabled in the Spring XML config, to disable it just comment it out.
 */

@Component
@Slf4j
//TODO - DHAY this class *might* be redundant - if we use flyway/liquibase to manage db
public class InitialiseDatabase {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseDatabase.class);
    public static final String ARCHIVE_STORE_LOCAL_ROOT_PATH = "archive.store.local.root.path";

    private final ArchiveStoreService archiveStoreService;
    private final String archiveDir;

    private final RolesAndPermissionsService rolesAndPermissionsService;
    private final Environment env;

    @Autowired
    public InitialiseDatabase(Environment env, ArchiveStoreService archiveStoreService,
        @Value("${archiveDir}") String archiveDir, RolesAndPermissionsService rolesAndPermissionsService) {
        this.archiveStoreService = archiveStoreService;
        this.archiveDir = archiveDir;
        this.rolesAndPermissionsService = rolesAndPermissionsService;
        this.env = env;
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

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();

        List<ArchiveStore> initStores = new ArrayList<>();
        if (archiveStores.isEmpty()) {
            HashMap<String,String> storeProperties = new HashMap<>();
            storeProperties.put(PropNames.ROOT_PATH, archiveDir);
            ArchiveStore tsm    = new ArchiveStore(StorageConstants.TIVOLI_STORAGE_MANAGER, storeProperties, "Default archive store (TSM)", true);
            ArchiveStore oracle = new ArchiveStore(StorageConstants.CLOUD_ORACLE, storeProperties, "Cloud archive store", false);
            archiveStoreService.addArchiveStore(tsm);
            archiveStoreService.addArchiveStore(oracle);
            initStores.add(tsm);
            initStores.add(oracle);
        }
        configureLocalFileSystem(archiveStores, initStores);

        BrokerInitialisedEvent initEvent = new BrokerInitialisedEvent(this, initStores.toArray(new ArchiveStore[0]));
        return initEvent;
    }

    private void configureLocalFileSystem(List<ArchiveStore> archiveStores, List<ArchiveStore> initStores) {
        if (ConfigUtils.isLocal(env) == false) {
            return;
        }
        boolean hasLocal = archiveStores
            .stream()
            .anyMatch(ArchiveStore::isLocalFileSystem);
        if (hasLocal) {
            return;
        }
        HashMap<String, String> storeProperties = new HashMap<>();
        Path rootPath = getLocalArchiveStoreRootPath();
        String rootPathValue = rootPath.toAbsolutePath().toString();
        storeProperties.put(LocalFileSystem.ROOT_PATH, rootPathValue);
        ArchiveStore local = new ArchiveStore(StorageConstants.LOCAL_FILE_SYSTEM, storeProperties,
            "LocalFileSystem", true);
        archiveStoreService.addArchiveStore(local);
        initStores.add(local);
    }

    protected Path getLocalArchiveStoreRootPath(){
        String localDir = env.getProperty(ARCHIVE_STORE_LOCAL_ROOT_PATH);
        log.debug("{} - [{}]", ARCHIVE_STORE_LOCAL_ROOT_PATH, localDir);
        Path rootPath = Paths.get(localDir);
        Assert.isTrue(rootPath.toFile().exists(), () ->
            String.format("the [%s] file [%s] does not exist", ARCHIVE_STORE_LOCAL_ROOT_PATH,
                rootPath.toAbsolutePath()));
        return rootPath;
    }

    @PostConstruct
    void afterPropertiesSet() {
        if (ConfigUtils.isLocal(env) == false) {
            return;
        }
        getLocalArchiveStoreRootPath();
    }
}
