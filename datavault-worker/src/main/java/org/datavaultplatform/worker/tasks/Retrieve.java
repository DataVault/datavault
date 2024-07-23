package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.impl.SftpUtils;
import org.datavaultplatform.common.task.*;
import org.datavaultplatform.common.util.RetrievedChunks;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.Utils;
import org.datavaultplatform.worker.operations.*;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;
import org.datavaultplatform.worker.tasks.retrieve.*;
import org.springframework.util.Assert;

import java.io.File;
import java.time.Clock;
import java.util.*;
import java.util.function.Consumer;

import static org.datavaultplatform.worker.operations.FileSplitter.CHUNK_SEPARATOR;
import static org.datavaultplatform.worker.tasks.retrieve.RetrieveState.*;
import static org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils.DATA_VAULT_HIDDEN_FILE_NAME;

/**
 * A class that extends Task which is used to handle Retrievals from the vault
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "CodeBlock2Expr"})
@Slf4j
public class Retrieve extends BaseTwoSpeedRetryTask {
    public static final String STARTING_TXFR = "Starting transfer ...";
    public static final String DATA = "data";

    private String archiveId;
    private long   archiveSize;
    private String archiveDigest;
    private String archiveDigestAlgorithm;

    private String bagID;
    private String depositId;
    private String depositCreationDate;

    private int numOfChunks;
    private Map<Integer, String> chunksDigest;
    private Map<Integer, String> encChunksDigest;
    private String encTarDigest;

    private String retrieveId;
    private String retrievePath;

    private UserEventSender userEventSender;

    private TaskStageEventListener taskStageEventListener;
    
    /* (non-Javadoc)
     * @see org.datavaultplatform.common.task.Task#performAction(org.datavaultplatform.common.task.Context)
     * 
     * Connect to the user's file store, check if they have enough free space, retrieve the archive and transfer to the users file store.
     * During the operation we untar / validate the archive before cleaning up.
     * Events are created for each stage of the operation.
     * 
     * The user's file store can be a local disk, Drop box, Amazon Glacier, SFTP ...
     *  
     * @param context
     */
    @Override
    public void performAction(Context context) {
        log.info("Retrieve job - performAction()");

        init(context);

        if (isRedeliver()) {
            sendError("Retrieve stopped: the message had been redelivered, please investigate");
            return;
        }

        sendEvent(new InitStates(jobID, depositId, RetrieveState.getRetrieveStates()));
        sendEvent(new RetrieveStart(jobID, depositId, retrieveId).withNextState(RetrieveState00ComputingFreeSpace.getStateNumber()));

        StorageClassNameResolver resolver = context.getStorageClassNameResolver();
        Device userFs = setupUserFileStores(resolver);
        Device archiveFs = setupArchiveFileStores(resolver);

        String timestampDirName = SftpUtils.getTimestampedDirectoryName(getClock());
        var userStoreInfo = new UserStoreInfo(userFs, timestampDirName);

        try {
            // Verify integrity with deposit checksum
            if (!Verify.getAlgorithm().equals(archiveDigestAlgorithm)) {
                throw new Exception("Unsupported checksum algorithm: [%s]".formatted(archiveDigestAlgorithm));
            }

            if (isLastEventBefore(UserStoreSpaceAvailableChecked.class)) {
                doStage(TaskStage.Retrieve1CheckUserStoreFreeSpace.INSTANCE);
                checkUserStoreFreeSpaceAndPermissions(userStoreInfo);
                sendEvent(new UserStoreSpaceAvailableChecked(jobID, depositId, retrieveId));
            } else {
                skipStage(TaskStage.Retrieve1CheckUserStoreFreeSpace.INSTANCE);
            }

            // Retrieve the archived data
            String tarFileName = RetrieveUtils.getTarFileName(bagID);
            
            // Copy the tar file from the archive to the temporary area
            File tarFile = context.getTempDir().resolve(tarFileName).toFile();
            
            sendEvent(getUpdateProgress(archiveSize).withNextState(RetrieveState01RetrievingFromArchive.getStateNumber()));

            var archiveDeviceInfo = ArchiveDeviceInfo.fromArchiveFs(archiveFs);
            var retrievedChunks = getRetrievedChunks();
            fromArchiveStoreToUserStore(context, tarFile, archiveDeviceInfo, userStoreInfo, retrievedChunks);

        } catch (Exception e) {
            throw getRuntimeException(e, "Data retrieve failed: " + e.getMessage());
        }
    }

    protected void retrieveChunksAndRecompose(Context context, ArchiveDeviceInfo archiveDeviceInfo, Progress progress,
                                            File tarFile, RetrievedChunks retrievedChunks) throws Exception {

        File[] chunks = retrieveFiles(context, archiveDeviceInfo, progress, tarFile, retrievedChunks);

        log.info("Recomposing tar file from [{}] chunk(s)", numOfChunks);
        doRecomposeUsingCorrectVersion(context, chunks, tarFile);

        // On the assumption that we have the tarfile now, delete the chunks
        log.info("Deleting the chunks now we have the recomposed tarfile");
        Arrays.stream(chunks).filter(Objects::nonNull).forEach(File::delete);
    }

    private File[] retrieveFiles(Context context, ArchiveDeviceInfo archiveDeviceInfo, Progress progress, File tarFile, RetrievedChunks retrievedChunks) throws Exception {
        File[] chunks = new File[numOfChunks];
        log.info("Retrieving [{}] chunk(s)", numOfChunks);
        
        Consumer<Integer> chunkRetrievedEventSender = chunkNumber -> {
            sendEvent(new ArchiveStoreRetrievedChunk(jobID, depositId, retrieveId, chunkNumber));            
        };
        
        TaskExecutor<File> executor = getTaskExecutor(context);
        for (int chunkNum = 1; chunkNum <= numOfChunks; chunkNum++) {
            log.debug("Creating chunk download task: [{}]", chunkNum);
            RetrieveChunkInfo chunkInfo = getChunkInfo(context, tarFile, chunkNum);
            chunks[chunkNum - 1] = chunkInfo.chunkFile();

            if (retrievedChunks.isRetrieved(chunkNum) && chunkInfo.isRetrieveValid()) {
                log.info("chunkNum[{}] retrieved already - skipping...", chunkNum);
            } else {
                ChunkRetrieveTracker crt = new ChunkRetrieveTracker(
                        archiveId, archiveDeviceInfo,
                        context, progress, chunkInfo, chunkRetrievedEventSender, getChunkFileChecker()
                );
                executor.add(crt);
            }
        }

        executor.execute();
        return chunks;
    }
    
    private TaskExecutor<File> getTaskExecutor(Context context) {
        int noOfThreads = context.getNoChunkThreads();
        log.debug("Number of threads: [{}]", noOfThreads);
        return new TaskExecutor<>(noOfThreads, "Chunk download failed.");
    }

    protected void doRecomposeUsingCorrectVersion(Context context, File[] chunks, File tarFile) throws Exception {
        // if we have a recompose date, oldRecompose is false and the deposit was created before the date set oldRecompose to true
        // set oldRecompose to true
        // if oldRecompose is false
        boolean oldRecompose = context.isOldRecompose();

        if (!oldRecompose) {
            oldRecompose = RetrieveUtils.getOldRecompose(depositCreationDate, context.getRecomposeDate());
        }

        if (oldRecompose) {
            log.info("Recomposing tar file using DV4 method");
            FileSplitter.recomposeFileDV4(chunks, tarFile);
        } else {
            log.info("Recomposing tar file using DV5 method");
            FileSplitter.recomposeFile(chunks, tarFile);
        }
        log.info("RECOMPOSED TAR FILE [{}]length[{}]", tarFile, tarFile.length());
    }

    protected void doRetrieveFromWorkerToUserFs(Context context, UserStoreInfo userStoreInfo, File tarFile, Progress progress) throws Exception {
        if (isLastEventBefore(UploadedToUserStore.class)) {
            doStage(TaskStage.Retrieve3UploadedToUserStore.INSTANCE);
            Assert.isTrue(tarFile.exists(), "The tar file [%s] does not exist".formatted(tarFile.toString()));
            logProgress(progress);
            log.info("Validating data ...");
            sendEvent(new UpdateProgress(jobID, depositId).withNextState(RetrieveState02ValidatingData.getStateNumber()));
            Utils.checkFileHash("ret-tar", tarFile, archiveDigest);
            File bagDir = Tar.unTar(tarFile, context.getTempDir());
            try {
                // Decompress to the temporary directory
                // Get the payload data directory
                File payloadDir = bagDir.toPath().resolve(DATA).toFile();

                long bagDirSize = FileUtils.sizeOfDirectory(bagDir);

                // Copy the extracted files to the target retrieve area
                log.info("Copying to user directory ...");
                sendEvent(getUpdateProgress(bagDirSize).withNextState(RetrieveState03TransferrinfFiles.getStateNumber()));

                // COPY FILES FROM WORKER BACK TO USER FS
                copyFilesToUserFs(progress, payloadDir, userStoreInfo, bagDirSize);

                logProgress(progress);
                sendEvent(new UploadedToUserStore(jobID, depositId, retrieveId));

                log.info("Data retrieve complete: [{}]", retrievePath);
                // only delete the tarFile AFTER we've sent the UploadedToUserStore message
                FileUtils.delete(tarFile);
            } finally {
                FileUtils.deleteDirectory(bagDir);
            }
        } else {
            skipStage(TaskStage.Retrieve3UploadedToUserStore.INSTANCE);
        }
    }

    private Device setupUserFileStores(StorageClassNameResolver resolver) {
        Device userFs = null;
        List<String> storageIDs = new ArrayList<>(userFileStoreClasses.keySet());
        for (int i = 0; i < storageIDs.size(); i++) {

            String storageID = storageIDs.get(i);
            String storageClass = userFileStoreClasses.get(storageID);
            Map<String, String> storageProperties = userFileStoreProperties.get(storageID);

            // Connect to the first user storage device (we only expect one for a retrieval)
            try {
                userFs = RetrieveUtils.createDevice(storageClass, storageProperties, resolver);
                log.info("Connected to user store: [{}], class: [{}]", storageID, storageClass);
                break;
            } catch (Exception e) {
                String msg = "Retrieve failed: could not access user filesystem";
                log.error(msg, e);
                sendError(msg);
                boolean lastStorageID = i == (storageIDs.size() - 1);
                if (lastStorageID) {
                    throw RetrieveUtils.getRuntimeException(e);
                }
            }
        }
        return userFs;
    }
    
    private Device setupArchiveFileStores(StorageClassNameResolver resolver) {
    	// We get passed a list because it is a parameter common to deposits and retrieves, but for retrieve there should only be one.
        ArchiveStore archiveFileStore = archiveFileStores.get(0);
        try {
            return RetrieveUtils.createDevice(archiveFileStore.getStorageClass(), archiveFileStore.getProperties(), resolver);
        } catch (Exception e) {
            throw getRuntimeException(e, "Retrieve failed: could not access archive filesystem");
        }
    }
    
    protected void checkUserStoreFreeSpaceAndPermissions(UserStoreInfo userStoreInfo) throws Exception {
        var userFs = userStoreInfo.userFs();
   	    UserStore userStore = ((UserStore) userFs);
        if (!userStore.exists(retrievePath) || !userStore.isDirectory(retrievePath)) {
            // Target path must exist and be a directory
            String msg = String.format("Target directory not found or is not a directory ! [%s]", retrievePath);
            log.error(msg);
            sendError(msg);
            throw new RuntimeException(msg);
        }

        // Check that there's enough free space ...
        final long freespace;
        try {
            var template = userFsTwoSpeedRetry.getRetryTemplate(String.format("calcSizeToFS - %s", retrievePath));
            freespace = template.execute(retryContext -> userFs.getUsableSpace());

        } catch (Exception e) {
            String msg = "Unable to determine free space";
            log.error(msg, e);
            sendError(msg);
            throw e;
        }
        log.info("Free space: {} bytes ({})", freespace, FileUtils.byteCountToDisplaySize(freespace));
        if (freespace < archiveSize) {
            String msg = "Not enough free space to retrieve data!";
            log.error(msg);
            sendError(msg);
            throw new RuntimeException(msg);
        }

        try {
            File createTempDataVaultHiddenFile = RetrieveUtils.createTempDataVaultHiddenFile();
            store(userStoreInfo, createTempDataVaultHiddenFile, new Progress());
        } catch (Exception e) {
            throw getRuntimeException(e, "Unable to perform test write of file[" + DATA_VAULT_HIDDEN_FILE_NAME  + "] to user space");
        }
    }
    
    protected boolean isFinalTarValid(File tarFile, String archiveDigest) {
        try {
            log.info("tarFile[{}] archiveDigest[{}]", tarFile, archiveDigest);
            Utils.checkFileHash("single-verified-tar", tarFile, archiveDigest);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    protected void fromArchiveStoreToUserStore(Context context, File tarFile, ArchiveDeviceInfo archiveDeviceInfo, UserStoreInfo userStoreInfo, RetrievedChunks retrievedChunks) throws Exception {
        var progress = new Progress();
        trackProgress(progress, archiveSize, () -> {
            if (isLastEventBefore(ArchiveStoreRetrievedAll.class) || !isFinalTarValid(tarFile, archiveDigest)) {
                doStage(TaskStage.Retrieve2RetrieveFromArchiveAndRecompose.INSTANCE);
                if (context.isChunkingEnabled()) {
                    retrieveChunksAndRecompose(context, archiveDeviceInfo, progress, tarFile, retrievedChunks);
                } else {
                    archiveDeviceInfo.retrieve(archiveId, tarFile, progress);
                    RetrieveUtils.decryptAndCheckTarFile("no-chunk", context, getTarIV(), tarFile, encTarDigest, archiveDigest);
                }
                sendEvent(new ArchiveStoreRetrievedAll(jobID, depositId, retrieveId));
            } else {
                skipStage(TaskStage.Retrieve2RetrieveFromArchiveAndRecompose.INSTANCE);
            }

            doRetrieveFromWorkerToUserFs(context, userStoreInfo, tarFile, progress);
        });
        if (isLastEventBefore(RetrieveComplete.class)) {
            doStage(TaskStage.Retrieve4Final.INSTANCE);
            sendEvent(new RetrieveComplete(jobID, depositId, retrieveId).withNextState(RetrieveState04DataRetrieveComplete.getStateNumber()));
        } else {
            skipStage(TaskStage.Retrieve4Final.INSTANCE);
        }
    }

    protected void copyFilesToUserFs(Progress progress, File payloadDir, UserStoreInfo userStoreInfo, long bagDirSize) throws Exception {
        trackProgress(progress, bagDirSize, () -> {
            File[] contents = payloadDir.listFiles();
            if (contents == null) {
                return;
            }
            Arrays.sort(contents);
            for (File content : contents) {
                String taskDesc = payloadDir.toPath().relativize(content.toPath()).toString();
                TwoSpeedRetry.DvRetryTemplate template = userFsTwoSpeedRetry.getRetryTemplate(String.format("toUserFs - %s", taskDesc));
                template.execute(retryContext -> store(userStoreInfo, content, progress));
            }
        });
    }
    private void sendError(String msg) {
        sendEvent(new RetrieveError(jobID, depositId, retrieveId, msg));
    }
    private void logProgress(Progress progress) {
        log.info("Copied: [{}] directories, [{}] files, [{}] bytes", progress.getDirCount(), progress.getFileCount(), progress.getByteCount());
    }
    protected void sendEvent(Event event) {
        userEventSender.send(event.withRetrieveId(retrieveId));
    }
    /*
    This method allows us to override the clock in tests.
     */
    protected Clock getClock() {
        return Clock.systemDefaultZone();
    }
    private void trackProgress(Progress progress, long expectedSizeBytes, Trackable trackable) throws Exception {
        var tracker = new ProgressTracker(progress, jobID, depositId, retrieveId, expectedSizeBytes, userEventSender);
        tracker.track(trackable);
    }
    private String store(UserStoreInfo userStoreInfo, File content, Progress progress) throws Exception {
        Device userFs = userStoreInfo.userFs();
        if (userFs instanceof SFTPFileSystemDriver sftpDriver) {
            return sftpDriver.store(retrievePath, content, progress, userStoreInfo.timeStampDirName());
        } else {
            return userFs.store(retrievePath, content, progress);
        }
    }
    private UpdateProgress getUpdateProgress(long totalSize){
        return new UpdateProgress(jobID, depositId, 0, totalSize, STARTING_TXFR);
    }
    private RuntimeException getRuntimeException(Exception ex, String msg) {
        log.error(msg, ex);
        sendError(msg);
        return RetrieveUtils.getRuntimeException(ex);
    }
    protected void init(Context context) {
        taskStageEventListener = context.getTaskStageEventListener();
        String userID;
        {
            Map<String, String> properties = getProperties();
            depositId = properties.get(PropNames.DEPOSIT_ID);
            depositCreationDate = properties.get(PropNames.DEPOSIT_CREATION_DATE);
            retrieveId = properties.get(PropNames.RETRIEVE_ID);
            bagID = properties.get(PropNames.BAG_ID);
            retrievePath = properties.get(PropNames.RETRIEVE_PATH);
            archiveId = properties.get(PropNames.ARCHIVE_ID);
            userID = properties.get(PropNames.USER_ID);
            archiveDigest = properties.get(PropNames.ARCHIVE_DIGEST);
            archiveDigestAlgorithm = properties.get(PropNames.ARCHIVE_DIGEST_ALGORITHM);
            numOfChunks = Integer.parseInt(properties.get(PropNames.NUM_OF_CHUNKS));
            archiveSize = Long.parseLong(properties.get(PropNames.ARCHIVE_SIZE));
            chunksDigest = getChunkFilesDigest();
            encChunksDigest = getEncChunksDigest();
            encTarDigest = getEncTarDigest();

            setupUserFsTwoSpeedRetry(properties);
        }
        userEventSender = new UserEventSender(context.getEventSender(), userID);
        Assert.isTrue(archiveFileStores.size() == 1, "There are [%s] archiveFileStores, should be 1".formatted(archiveFileStores.size()));

        log.info("bagID: [{}]", bagID);
        log.info("retrievePath: [{}]", retrievePath);
    }

    protected RetrieveChunkInfo getChunkInfo(Context context, File tarFile, int chunkNum) {
        String tarFileName = tarFile.getName();
        File chunkFile = context.getTempDir().resolve(tarFileName + CHUNK_SEPARATOR + chunkNum).toFile();
        byte[] iv = getChunksIVs().get(chunkNum);
        String encChunkDigest = encChunksDigest.get(chunkNum);
        String chunkDigest = chunksDigest.get(chunkNum);
        return new RetrieveChunkInfo(chunkNum, chunkFile, encChunkDigest, iv, chunkDigest);
    }
    
    protected boolean isLastEventBefore(Class<? extends Event> eventClass) {
        return RetrieveEvents.INSTANCE.isLastEventBefore(getLastEvent(), eventClass);
    }

    @SneakyThrows
    private RetrievedChunks getRetrievedChunks() {
        String retrievedChunksJson = getProperties().get(PropNames.DEPOSIT_CHUNKS_RETRIEVED);
        return RetrievedChunks.fromJson(retrievedChunksJson);
    }

    protected RetrievedChunkFileChecker getChunkFileChecker() {
        return new RetrievedChunkFileChecker();
    }

    private void recordStage(TaskStage stage, boolean skipped) {
        taskStageEventListener.onTaskStageEvent(new TaskStageEvent(stage, skipped));
    }
    private void doStage(TaskStage.RetrieveTaskStage stage) {
        recordStage(stage, false);
    }
    private void skipStage(TaskStage.RetrieveTaskStage stage) {
        recordStage(stage, true);
    }
}
