package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.TransferComplete;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.task.*;
import org.datavaultplatform.worker.operations.ProgressTracker;
import org.datavaultplatform.worker.tasks.DepositTransferHelper;

import java.io.File;
import java.nio.file.Path;

import java.util.List;
import java.util.Map;

import static org.datavaultplatform.worker.tasks.deposit.DepositState.DepositState01Transferring;
import static org.datavaultplatform.worker.tasks.deposit.DepositState.DepositState02Packaging;

@Slf4j
public class DepositUserStoreDownloader extends DepositSupport {

    private static final String UPLOADS = "uploads";
    private final List<String> fileStorePaths;
    private final Map<String, UserStore> userStores;
    private final List<String> fileUploadPaths;
    private final TaskStageEventListener taskStageEventListener;
    public DepositUserStoreDownloader(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String, String> properties,
                                      List<String> fileStorePaths, Map<String,UserStore> userStores, List<String> fileUploadPaths) {
        super(userID, jobID, depositId, userEventSender, bagID, context, lastEvent, properties);
        this.taskStageEventListener = context.getTaskStageEventListener();
        if (fileStorePaths == null) {
            String msg = "Deposit failed: null list of fileStorePaths";
            sendInvalidArgumentMessage(msg);
        }
        if (userStores == null) {
            String msg = "Deposit failed: null list of userStores";
            sendInvalidArgumentMessage(msg);
        }
        this.fileStorePaths = fileStorePaths;
        this.userStores = userStores;
        this.fileUploadPaths = fileUploadPaths;
    }

    public File transferFromUserStoreToWorker() throws Exception {

        //2 of 5 - USER STORE TO WORKER
        DepositTransferHelper uploadHelper = initialTransferStep();
        Path bagDataPath = uploadHelper.bagDataPath();

        copyAdditionalUserData(context, bagDataPath);

        sendEvent(new TransferComplete(jobID, depositId).withNextState(DepositState02Packaging.getStateNumber()));
        return uploadHelper.bagPath().toFile();
    }
    
    protected void copySelectedUserDataToBagDataDir(Path bagDataPath) {
        long depositIndex = 0L;

        for (String fileStorePath : fileStorePaths) {
            
            FileStorePath fsPath = new FileStorePath(fileStorePath);
            String storageID = fsPath.storageID();
            String storagePath = fsPath.storagePath();

            UserStore userStore = userStores.get(storageID);

            // Retrieve the data straight into the Bag data directory.
            Path depositPath = bagDataPath;
            
            // If there are multiple deposits, then create a subdirectory for each one
            if (fileStorePaths.size() > 1) {
                depositIndex += 1;
                depositPath = bagDataPath.resolve(Long.toString(depositIndex));
                DepositUtils.createDir(depositPath);
            }

            try {
                if (userStore.exists(storagePath)) {

                    // Copy the target file to the bag directory
                    sendEvent(new UpdateProgress(getJobID(), getDepositId()).withNextState(DepositState01Transferring.getStateNumber()));

                    log.info("Copying target to bag directory ...");
                    copyFromUserStorage(userStore, storagePath, depositPath);

                } else {
                    String msg = String.format("StoragePath[%s] does not exist on userStore[%s]", storagePath, userStore);
                    throw new RuntimeException(msg);
                }
            } catch (Exception e) {
                String msg = "Deposit failed: " + e.getMessage();
                log.error(msg, e);
                sendError(msg);
                throw new RuntimeException(e);
            }
        }
    }

    private void copyAdditionalUserData(Context context, Path bagDataPath) throws Exception {
        // Add any directly uploaded files (direct move from temp dir)            
        if(fileUploadPaths != null){
            for (String fileUploadPath : fileUploadPaths) {
                moveFromUserUploads(context.getTempDir(), bagDataPath, fileUploadPath);
            }
        }
    }


    /**
     * @param tempPath
     * @param bagPath
     * @param uploadPath
     * @throws Exception
     */
    private void moveFromUserUploads(Path tempPath, Path bagPath, String uploadPath) throws Exception {

        File outputFile = bagPath.resolve(UPLOADS).resolve(uploadPath).toFile();

        // TODO: this is a bit of a hack to escape the per-worker temp directory
        File uploadDir = tempPath.getParent().resolve(UPLOADS).resolve(userID).resolve(uploadPath).toFile();
        if (uploadDir.exists()) {
            log.info("Moving user uploads [{}] to bag directory [{}]", uploadDir, outputFile);
            FileUtils.moveDirectory(uploadDir, outputFile);
        } else {
            log.warn("The uploadDir [{}] does not exist", uploadDir);
        }
    }

    /**
     * @param userStore
     * @param filePath
     * @param bagPath
     * @throws Exception
     */
    private void copyFromUserStorage(UserStore userStore, String filePath, Path bagPath) throws Exception {

        String fileName = userStore.getName(filePath);
        File outputFile = bagPath.resolve(fileName).toFile();

        // Compute bytes to copy
        long expectedBytes = userStore.getSize(filePath);

        // Display progress bar
        sendEvent(new UpdateProgress(jobID, depositId, 0, expectedBytes, "Starting transfer ..."));

        log.info("Size: {} bytes ({})", expectedBytes, FileUtils.byteCountToDisplaySize(expectedBytes));

        //it would be nice to extract this.
        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, expectedBytes, userEventSender);
        tracker.track(() -> {
            // Ask the driver to copy files to our working directory
            log.debug("CopyFromUserStorage filePath:{}", filePath);
            log.debug("CopyFromUserStorage outputFile:{}", outputFile.getAbsolutePath());
            ((Device) userStore).retrieve(filePath, outputFile, progress);
        });
    }
    
    private DepositTransferHelper initialTransferStep() {

        log.info("bagID: {}", bagID);
        Path bagPath = context.getTempDir().resolve(bagID);
        final DepositTransferHelper result = new DepositTransferHelper(bagPath);

        log.debug("Setting bag path to: {}", result.bagPath());
        log.debug("Setting bag data path to: {}", result.bagDataPath());

        boolean eventHasNotBeenSeenBefore = eventHasNotBeenSeenBefore(TransferComplete.class);
        boolean directoriesDoNotExist = !result.directoriesExist();
        boolean performDeposit2TransferStage = eventHasNotBeenSeenBefore || directoriesDoNotExist;
        log.info("lastEvent                    ??[{}]", lastEvent == null ? null : lastEvent.getClass().getSimpleName());
        log.info("eventHasNotBeenSeenBefore    ??[{}]", eventHasNotBeenSeenBefore);
        log.info("directoriesDoNotExist        ??[{}]", directoriesDoNotExist);
        log.info("performDeposit2TransferStage ??[{}]", performDeposit2TransferStage);
        if (performDeposit2TransferStage) {
            doStage(TaskStage.Deposit2Transfer.INSTANCE);
            result.createDirs();
            
            copySelectedUserDataToBagDataDir(result.bagDataPath());
        } else {
            skipStage(TaskStage.Deposit2Transfer.INSTANCE);
            log.debug("Last event is: {} skipping initial File copy", getLastEventClass());
        }
        return result;
    }
    private void recordStage(TaskStage stage, boolean skipped){
        context.getTaskStageEventListener().onTaskStageEvent(new TaskStageEvent(stage, skipped));
    }
    private void doStage(TaskStage.DepositTaskStage stage) {
        recordStage(stage, false);
    }
    private void skipStage(TaskStage.DepositTaskStage stage) {
        recordStage(stage, true);
    }
}
