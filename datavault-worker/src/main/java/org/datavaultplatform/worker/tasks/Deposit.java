package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.storage.*;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.TaskStage;
import org.datavaultplatform.common.task.TaskStageEvent;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StoredChunks;
import org.datavaultplatform.worker.tasks.deposit.*;
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

        if (isRedeliver()) {
            sendError("Retrieve stopped: the message had been redelivered, please investigate");
            return;
        }

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

            //if (true) {
            //    throw new Exception("Forced to fail after packaging");
            //}

            StoredChunks previouslyStoredChunks = getStoredChunks();

            // returns archive ids for verification
            Set<ArchiveStoreInfo> archiveStoreInfos = uploadToStorage(packageHelper, previouslyStoredChunks, archiveStoreContext);

            //if (true) {
            //    throw new Exception("Forced to fail after upload");
            //}

            verifyArchive(packageHelper, archiveStoreInfos);

            //if (true) {
            //    throw new Exception("Forced to fail after verification");
            //}

            if (isLastEventIsBefore(Complete.class)) {
                doStage(TaskStage.Deposit6Final.INSTANCE);
                log.info("Deposit ID [{}] complete", depositId);

                HashMap<String, String> archiveIds = getArchiveIds(archiveStoreInfos);

                log.debug("The jobID: [{}]", jobID);
                log.debug("The depositId: [{}]", depositId);
                log.debug("The archiveIds: {}", archiveIds);
                sendEvent(new Complete(jobID, depositId, archiveIds, packageHelper.getArchiveSize()).withNextState(DepositState05Complete.getStateNumber()));

                // at this point, we have finished everything, so can delete the bag dir
                deleteBagDir(bagDir);
            } else {
                skipStage(TaskStage.Deposit6Final.INSTANCE);
            }
        } catch (Exception ex) {
            String msg = "Deposit failed: " + ex.getMessage();
            log.error(msg, ex);
            sendError(msg);
            throw new RuntimeException(msg, ex);
        }
    }

    @SneakyThrows
    private void deleteBagDir(File bagDir) { 
        if (bagDir == null) {
            return;
        }
        log.info("We have successfully created the Tar, so lets delete the Bag to save space");
        FileUtils.deleteDirectory(bagDir);
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
        return StoredChunks.fromJsop(storedChunksJson);
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
            doStage(TaskStage.Deposit5Verify.INSTANCE);
            DepositVerifier verifier = getDepositVerifier(archiveStoreInfos);
            verifier.verifyArchive(packageHelper);
        } else {
            skipStage(TaskStage.Deposit5Verify.INSTANCE);
            log.debug("Last event is: {} skipping validation", getLastEventClass());
        }
    }

    private Set<ArchiveStoreInfo> uploadToStorage(PackageHelper packageHelper, StoredChunks previouslyStoredChunks, ArchiveStoreContext archiveStoreContext) throws Exception {
        if (isLastEventIsBefore(UploadComplete.class)) {
            doStage(TaskStage.Deposit4Archive.INSTANCE);
            // Copy the resulting tar file to the archive area
            log.info("Copying tar file(s) to archive ...");
            DepositArchiveStoresUploader uploader = getDepositArchiveStoresUploader(archiveStoreContext);
            uploader.uploadToStorage(packageHelper, previouslyStoredChunks);
        } else {
            skipStage(TaskStage.Deposit4Archive.INSTANCE);
        }
        return archiveStoreContext.getArchiveStoreInfo();
    }

    protected PackageHelper packageStep(File bagDir) throws Exception {
        log.info("LAST EVENT CLASS [{}]", getLastEventClass());

        PackageHelper result;

        if (isLastEventIsBefore(PackageChunkEncryptComplete.class)) {
            doStage(TaskStage.Deposit3PackageEncrypt.INSTANCE);
            DepositPackager packager = getDepositPackager();
            result = packager.packageStep(bagDir);
        } else {
            skipStage(TaskStage.Deposit3PackageEncrypt.INSTANCE);
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
            doStage(TaskStage.Deposit1ComputeSize.INSTANCE);
            DepositSizeComputer computer = getDepositSizeComputer(userStores);
            computer.calculateTotalDepositSize();
        } else {
            skipStage(TaskStage.Deposit1ComputeSize.INSTANCE);
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
        return new DepositSizeComputer(userID, jobID, depositId, userEventSender, bagID, context, getLastEvent(), getProperties(), userStores, fileStorePaths);
    }

    protected boolean isLastEventIsBefore(Class<? extends Event> eventClass) {
        return DepositEvents.INSTANCE.isLastEventBefore(getLastEvent(), eventClass);
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

    private void recordStage(TaskStage stage, boolean skipped) {
        context.getTaskStageEventListener().onTaskStageEvent(new TaskStageEvent(stage, skipped));
    }
    private void doStage(TaskStage.DepositTaskStage stage) {
        recordStage(stage, false);
    }
    private void skipStage(TaskStage.DepositTaskStage stage) {
        recordStage(stage, true);
    }
}