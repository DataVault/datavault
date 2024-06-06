package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.storage.*;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StoredChunks;
import org.datavaultplatform.worker.tasks.deposit.*;
import org.springframework.security.core.parameters.P;
import org.springframework.util.Assert;

import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.datavaultplatform.worker.tasks.deposit.DepositState.*;

/**
 * A class that extends Task which is used to handle Deposits to the vault
 */
@Slf4j
public class Deposit extends Task {

    private UserEventSender userEventSender;

    private String depositId;
    private String bagID;
    private String userID;
    private Context context;

    @Override
    public void performAction(Context context) {
        DepositUtils.initialLogging(context);

        this.context = context;

        var properties = getProperties();
        depositId = properties.get(PropNames.DEPOSIT_ID);
        bagID = properties.get(PropNames.BAG_ID);
        userID = properties.get(PropNames.USER_ID);

        Assert.isTrue(StringUtils.isNotBlank(depositId), "The depositId cannot be blank");
        Assert.isTrue(StringUtils.isNotBlank(bagID), "The bagID cannot be blank");
        Assert.isTrue(StringUtils.isNotBlank(userID), "The userID cannot be blank");

        userEventSender = new UserEventSender(context.getEventSender(), userID);

        // not sure that we should be sending these for restarts ??
        sendEvent(new InitStates(jobID, depositId, getDepositStates()));
        sendEvent(new Start(jobID, depositId).withNextState(DepositState00CalculatingSize.getStateNumber()));

        try {

            StorageClassNameResolver resolver = context.getStorageClassNameResolver();

            var archiveStoreContext = new ArchiveStoreContext(getArchiveFileStores(resolver), getRestartArchiveIds());

            var userStores = getUserFileStores(resolver);

            // CALCULATE SIZE - before downloading
            calculateTotalDepositSize(userStores);

            // DOWNLOAD FROM USER STORE
            File bagDir = transferFromUserStoreToWorker(userStores);

            // PACKAGE DOWNLOAD INTO ENCRYPTED CHUNKS
            PackageHelper packageHelper = packageStep(bagDir);

            StoredChunks previouslyStoredChunks = getStoredChunks();

            // returns archive ids for verification
            Set<ArchiveStoreInfo> archiveStoreInfos = uploadToStorage(packageHelper, previouslyStoredChunks, archiveStoreContext);

            verifyArchive(packageHelper, archiveStoreInfos);

            log.info("Deposit ID [{}] complete", depositId);

            HashMap<String, String> archiveIds = getArchiveIds(archiveStoreInfos);

            log.debug("The jobID: [{}]", jobID);
            log.debug("The depositId: [{}]", depositId);
            log.debug("The archiveIds: {}", archiveIds);
            sendEvent(new Complete(jobID, depositId, archiveIds, packageHelper.getArchiveSize()).withNextState(DepositState05Complete.getStateNumber()));
        } catch (Exception ex) {
            String msg = "Deposit failed: " + ex.getMessage();
            log.error(msg, ex);
            sendError(msg);
            throw new RuntimeException(msg, ex);
        }
    }

    protected static HashMap<String, String> getArchiveIds(Set<ArchiveStoreInfo> archiveStoreInfos) {
        HashMap<String, String> result = new HashMap<>();
        if (archiveStoreInfos != null) {
            Map<String, String> temp = archiveStoreInfos.stream().collect(
                toMap(ArchiveStoreInfo::archiveStoreId, ArchiveStoreInfo::archiveId)
            );
            result.putAll(temp);
        }
        return result;
    }

    @SneakyThrows
    private StoredChunks getStoredChunks() {
        String storedChunksJson = getProperties().get(PropNames.DEPOSIT_CHUNKS_STORED);
        if (StringUtils.isBlank(storedChunksJson)) {
            return new StoredChunks();
        } else {
            StoredChunks result = new ObjectMapper().readValue(storedChunksJson, StoredChunks.class);
            return result;
        }
    }

    private void sendError(String message) {
        sendEvent(new Error(jobID, depositId, message));
    }

    private void sendEvent(Event event) {
        userEventSender.send(event);
    }

    private void verifyArchive(PackageHelper packageHelper, Set<ArchiveStoreInfo> archiveStoreInfos) throws Exception {
        log.info("Verifying archive package ...");
        if (isLastEventIsBefore(ValidationComplete.class)) {
            DepositVerifier verifier = getDepositVerifier(archiveStoreInfos);
            verifier.verifyArchive(packageHelper);
        } else {
            log.debug("Last event is: " + getLastEventClass() + " skipping validation");
        }
    }

    private Set<ArchiveStoreInfo> uploadToStorage(PackageHelper packageHelper, StoredChunks previouslyStoredChunks, ArchiveStoreContext archiveStoreContext) throws Exception {
        if (isLastEventIsBefore(UploadComplete.class)) {
            // Copy the resulting tar file to the archive area
            log.info("Copying tar file(s) to archive ...");
            DepositArchiveStoresUploader uploader = getDepositArchiveStoresUploader(archiveStoreContext);
            uploader.uploadToStorage(packageHelper, previouslyStoredChunks);
        }
        return archiveStoreContext.getArchiveStoreInfo();
    }

    protected PackageHelper packageStep(File bagDir) throws Exception {
        log.info("LAST EVENT CLASS [{}]", getLastEventClass());
        Class<? extends Event> finalPackagingEventClass = DepositUtils.getFinalPackageEvent(context.isChunkingEnabled(), context.isEncryptionEnabled());

        PackageHelper result;

        if (isLastEventIsBefore(finalPackagingEventClass)) {
            DepositPackager packager = getDepositPackager();
            result = packager.packageStep(bagDir);
        } else {
            result = PackageHelper.constructFromDepositTask(bagID, context, this);
        }
        log.debug("Packaged Files {}", result.getPackagedFiles());
        return result;
    }

    protected File transferFromUserStoreToWorker(Map<String, UserStore> userStores) throws Exception {
        DepositUserStoreDownloader downloader = getDepositUserStoreDownloader(userStores);
        return downloader.transferFromUserStoreToWorker();
    }

    protected void calculateTotalDepositSize(Map<String, UserStore> userStores) {
        if (isLastEventIsBefore(ComputedSize.class)) {
            DepositSizeComputer computer = getDepositSizeComputer(userStores);
            computer.calculateTotalDepositSize();
        }
    }

    private Map<String, ArchiveStore> getArchiveFileStores(StorageClassNameResolver resolver) {
        try {
            return DepositUtils.setupArchiveStores(resolver, archiveFileStores);
        } catch (RuntimeException ex) {
            sendError(ex.getMessage());
            throw ex;
        }
    }

    protected Map<String, UserStore> getUserFileStores(StorageClassNameResolver resolver) {
        try {
            return DepositUtils.setupUserFileStores(resolver, userFileStoreProperties, userFileStoreClasses);
        } catch (RuntimeException ex) {
            sendError(ex.getMessage());
            throw ex;
        }
    }

    // below here - methods are protected to help testing using Mockito spies
    protected DepositSizeComputer getDepositSizeComputer(Map<String, UserStore> userStores) {
        DepositSizeComputer computer = new DepositSizeComputer(userID, jobID, depositId, userEventSender, bagID, context, getLastEvent(), getProperties(), userStores, fileStorePaths);
        return computer;
    }

    protected boolean isLastEventIsBefore(Class<? extends Event> eventClass) {
        return DepositEvents.isLastEventBefore(getLastEvent(), eventClass);
    }

    protected DepositUserStoreDownloader getDepositUserStoreDownloader(Map<String, UserStore> userStores) {
        return new DepositUserStoreDownloader(userID, jobID, depositId, userEventSender, bagID, context, getLastEvent(), getProperties(), fileStorePaths, userStores, fileUploadPaths);
    }

    protected DepositPackager getDepositPackager() {
        return new DepositPackager(userID, jobID, depositId, userEventSender, bagID, context, getLastEvent(), getProperties());
    }

    protected DepositArchiveStoresUploader getDepositArchiveStoresUploader(ArchiveStoreContext archiveStoreContext) {
        return new DepositArchiveStoresUploader(
                userID, jobID, depositId, userEventSender, bagID, context, getLastEvent(), getProperties(), archiveStoreContext);
    }

    protected DepositVerifier getDepositVerifier(Set<ArchiveStoreInfo> archiveStoreInfos) {
        return new DepositVerifier(
                userID, jobID, depositId, userEventSender, bagID, context, getLastEvent(), getProperties(), archiveStoreInfos);
    }

    public String getLastEventClass() {
        Event event = getLastEvent();
        if (event != null) {
            return event.getEventClass();
        } else {
            return null;
        }
    }
}