package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.event.retrieve.RetrieveError;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.impl.SftpUtils;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.common.util.Utils;
import org.datavaultplatform.worker.operations.*;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveState;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

import org.springframework.retry.RetryCallback;

import static org.datavaultplatform.worker.tasks.retrieve.RetrieveState.*;
import static org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils.DATA_VAULT_HIDDEN_FILE_NAME;

/**
 * A class that extends Task which is used to handle Retrievals from the vault
 */
@Slf4j
public class Retrieve extends BaseTwoSpeedRetryTask {
    public static final String DATA = "data";

    private String archiveId;
    private long   archiveSize;
    private String archiveDigest;
    private String archiveDigestAlgorithm;

    private String userID;
    private String depositId;
    private String depositCreationDate;

    private int numOfChunks;
    private Map<Integer, String> chunksDigest;
    private Map<Integer, String> encChunksDigest;
    private String encTarDigest;

    private String retrieveId;
    private String retrievePath;

    private EventSender eventSender;

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
        Map<String, String> properties = getProperties();
        
        eventSender = context.getEventSender();
        depositId = properties.get(PropNames.DEPOSIT_ID);
        depositCreationDate = properties.get(PropNames.DEPOSIT_CREATION_DATE);
        retrieveId = properties.get(PropNames.RETRIEVE_ID);
        String bagID = properties.get(PropNames.BAG_ID);
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

        if (isRedeliver()) {
            sendError("Retrieve stopped: the message had been redelivered, please investigate");
            return;
        }

        initStates();

        sendEvent(new RetrieveStart(jobID, depositId, retrieveId)
                .withNextState(RetrieveState00ComputingFreeSpace.getStateNumber()));

        log.info("bagID: [{}]", bagID);
        log.info("retrievePath: [{}]", retrievePath);

        StorageClassNameResolver resolver = context.getStorageClassNameResolver();
        Device userFs = setupUserFileStores(resolver);
        Device archiveFs = setupArchiveFileStores(resolver);
        String timestampDirName = SftpUtils.getTimestampedDirectoryName(getClock());

        try {
            checkUserStoreFreeSpaceAndPermissions(userFs, timestampDirName);

            // Retrieve the archived data
            String tarFileName = bagID + ".tar";
            
            // Copy the tar file from the archive to the temporary area
            Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            
            sendEvent(new UpdateProgress(jobID, depositId, 0, archiveSize, "Starting transfer ...")
                    .withNextState(RetrieveState01RetrievingFromArchive.getStateNumber()));

            if (archiveFs.hasMultipleCopies()) {
                fromArchiveStoreMultipleCopies(context, tarFileName, tarFile, archiveFs, userFs, timestampDirName);
            } else {
                fromArchiveStoreSingleCopy(context, tarFileName, tarFile, archiveFs, userFs, timestampDirName);
            }
            
        } catch (Exception e) {
            String msg = "Data retrieve failed: " + e.getMessage();
            log.error(msg, e);
            sendError(msg);
            throw RetrieveUtils.getRuntimeException(e);
        }
    }

    private void recomposeSingle(String tarFileName, Context context, Device archiveFs, Progress progress, File tarFile) throws Exception {
        recompose(tarFileName, context, archiveFs, progress, tarFile, false, null);
    }

    private void recomposeMulti(String tarFileName, Context context, Device archiveFs, Progress progress, File tarFile, List<String> locations) throws Exception {
        recompose(tarFileName, context, archiveFs, progress, tarFile, true, locations);
    }


    private void recompose(String tarFileName, Context context, Device archiveFs, Progress progress,
                           File tarFile, boolean multiCopy, List<String> locations) throws Exception {

        File[] chunks = new File[numOfChunks];
        log.info("Retrieving [{}] chunk(s)", numOfChunks);

        int noOfThreads = context.getNoChunkThreads();
        log.debug("Number of threads: [{}]", noOfThreads);

        TaskExecutor<File> executor = new TaskExecutor<>(noOfThreads, "Chunk download failed.");
        for (int chunkNum = 1; chunkNum <= numOfChunks; chunkNum++) {
            Path chunkPath = context.getTempDir().resolve(tarFileName + FileSplitter.CHUNK_SEPARATOR + chunkNum);
            File chunkFile = chunkPath.toFile();
            log.debug("Creating chunk download task: [{}]", chunkNum);
            
            //if not a restart OR (chunk not retrieved OR decrypted chunk file does not exist) then execute a task to retrieve it.
            
            ChunkRetrieveTracker crt = new ChunkRetrieveTracker(
                    archiveId, archiveFs,
                    context, chunkNum, getChunksIVs(), locations, multiCopy,
                    progress, chunksDigest, encChunksDigest, chunkFile
            );
            executor.add(crt);
            chunks[chunkNum - 1] = chunkFile;
        }

        executor.execute();

        log.info("Recomposing tar file from chunk(s)");
        doRecomposeUsingCorrectVersion(context, chunks, tarFile);

        // On the assumption that we have the tarfile now, delete the chunks
        log.info("Deleting the chunks now we have the recomposed tarfile");
        for (File chunk : chunks) {
            chunk.delete();
        }
    }

    private void doRecomposeUsingCorrectVersion(Context context, File[] chunks, File tarFile) throws Exception {
        // if we have a recompose date, oldRecompose is false and the deposit was created before the date set oldRecompose to true
        // set oldRecompose to true
        // if oldRecompose is false
        boolean oldRecompose = context.isOldRecompose();

        if (!oldRecompose) {
            
            LocalDate depositDate = RetrieveUtils.parseLocalDate(depositCreationDate);
            log.info("DepositDate is: [{}]", depositDate);
            
            LocalDate recomposeDate = RetrieveUtils.parseLocalDate(context.getRecomposeDate());
            log.info("RecomposeDate is: [{}]", recomposeDate);
            
            // if deposit creation date is before the recomposeDate
            if (depositDate != null && recomposeDate != null && depositDate.isBefore(recomposeDate)) {
                // set oldRecompose to true
                oldRecompose = true;
            }

        }

        if (oldRecompose) {
            log.info("Recomposing tar file using DV4 method");
            FileSplitter.recomposeFileDV4(chunks, tarFile);
        } else {
            log.info("Recomposing tar file using DV5 method");
            FileSplitter.recomposeFile(chunks, tarFile);
        }
    }

    private void doRetrieveFromWorkerToUserFs(Context context, Device userFs, File tarFile, Progress progress,
                                              String timeStampDirName) throws Exception {
        logProgress(progress);
        log.info("Validating data ...");
        sendEvent(new UpdateProgress(jobID, depositId)
                .withNextState(RetrieveState02ValidatingData.getStateNumber()));
        Utils.checkFileHash("ret-tar", tarFile, archiveDigest);
        // Decompress to the temporary directory
        File bagDir = Tar.unTar(tarFile, context.getTempDir());
        long bagDirSize = FileUtils.sizeOfDirectory(bagDir);

        // Get the payload data directory
        File payloadDir = bagDir.toPath().resolve(DATA).toFile();

        // Copy the extracted files to the target retrieve area
        log.info("Copying to user directory ...");
        sendEvent(new UpdateProgress(jobID, depositId, 0, bagDirSize, "Starting transfer ...")
                .withNextState(RetrieveState03TransferrinfFiles.getStateNumber()));

        // COPY FILES FROM WORKER BACK TO USER FS
        copyFilesToUserFs(progress, payloadDir, userFs, bagDirSize, timeStampDirName);

        logProgress(progress);

        // Cleanup
        log.info("Cleaning up ...");
        FileUtils.deleteDirectory(bagDir);
        tarFile.delete();

        log.info("Data retrieve complete: [{}]", retrievePath);
        sendEvent(new RetrieveComplete(jobID, depositId, retrieveId)
                .withNextState(RetrieveState04DataRetrieveComplete.getStateNumber()));
    }



    private Device setupUserFileStores(StorageClassNameResolver resolver) {
        Device userFs = null;

        for (String storageID : userFileStoreClasses.keySet()) {
            
            String storageClass = userFileStoreClasses.get(storageID);
            Map<String, String> storageProperties = userFileStoreProperties.get(storageID);
            
            // Connect to the first user storage device (we only expect one for a retrieval)
            try {
                userFs = StorageClassUtils.createStorage(storageClass, storageProperties, Device.class, resolver);
                log.info("Connected to user store: [{}], class: [{}]", storageID, storageClass);
                break;
            } catch (Exception e) {
                String msg = "Retrieve failed: could not access user filesystem";
                log.error(msg, e);
                sendError(msg);
                throw RetrieveUtils.getRuntimeException(e);
            }
        }
        
        return userFs;
    }
    
    private Device setupArchiveFileStores(StorageClassNameResolver resolver) {
    	// We get passed a list because it is a parameter common to deposits and retrieves, but for retrieve there should only be one.
        ArchiveStore archiveFileStore = archiveFileStores.get(0);

        // Connect to the archive storage
        try {
            Device archiveFs = StorageClassUtils.createStorage(
                archiveFileStore.getStorageClass(),
                archiveFileStore.getProperties(),
                Device.class,
                resolver);
            return archiveFs;
        } catch (Exception e) {
            String msg = "Retrieve failed: could not access archive filesystem";
            log.error(msg, e);
            sendError(msg);
            throw RetrieveUtils.getRuntimeException(e);
        }
    }
    
    private void checkUserStoreFreeSpaceAndPermissions(Device userFs, String timeStampDirName) throws Exception {
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
            TwoSpeedRetry.DvRetryTemplate template = userFsTwoSpeedRetry.getRetryTemplate(String.format("calcSizeToFS - %s", retrievePath));
            freespace = template.execute(retryContext -> userFs.getUsableSpace());

        } catch (Exception e) {
            String msg = "Unable to determine free space";
            log.error(msg, e);
            sendError(msg);
            throw e;
        }
        log.info("Free space: " + freespace + " bytes (" + FileUtils.byteCountToDisplaySize(freespace) + ")");
        if (freespace < archiveSize) {
            String msg = "Not enough free space to retrieve data!";
            log.error(msg);
            sendError(msg);
            throw new RuntimeException(msg);
        }

        try {
            File createTempDataVaultHiddenFile = RetrieveUtils.createTempDataVaultHiddenFile();
            // copy ".datavault" file to test we can actually write
            if (userFs instanceof SFTPFileSystemDriver) {
                ((SFTPFileSystemDriver) userFs).store(retrievePath, createTempDataVaultHiddenFile, new Progress(), timeStampDirName);
            } else {
                userFs.store(retrievePath, createTempDataVaultHiddenFile, new Progress());
            }
        } catch (Exception e) {
            String msg = "Unable to perform test write of file[" + DATA_VAULT_HIDDEN_FILE_NAME  + "] to user space";
            log.error(msg, e);
            sendError(msg);
            throw e;
        }
    }

    protected void fromArchiveStoreMultipleCopies(Context context, String tarFileName, File tarFile, Device archiveFs, Device userFs,
                                                  String timeStampDirName) throws Exception {
        log.info("Device has multiple copies");
        Progress progress = new Progress();
        trackProgress(progress, archiveSize, () -> {
            List<String> locations = archiveFs.getLocations();
            if (context.isChunkingEnabled()) {
                recomposeMulti(tarFileName, context, archiveFs, progress, tarFile, locations);
            } else {
                // Ask the driver to copy files to the temp directory
                archiveFs.retrieve(archiveId, tarFile, progress, locations);

                if (getTarIV() != null) {
                    // Decrypt tar file
                    Utils.checkFileHash("ret-single-tar", tarFile, encTarDigest);
                    Encryption.decryptFile(context, tarFile, getTarIV());
                }
            }

            log.info("Attempting retrieve on archive from {}", locations);
            doRetrieveFromWorkerToUserFs(context, userFs, tarFile, progress, timeStampDirName);
            log.info("Completed retrieve on archive from {}", locations);
        });
    }

    protected void fromArchiveStoreSingleCopy(Context context, String tarFileName, File tarFile, Device archiveFs,
                                              Device userFs, String timeStampDirName) throws Exception {
        log.info("Single copy device");
        Progress progress = new Progress();
        trackProgress(progress, archiveSize, () -> {
            // Verify integrity with deposit checksum
            String systemAlgorithm = Verify.getAlgorithm();
            if (!systemAlgorithm.equals(archiveDigestAlgorithm)) {
                throw new Exception("Unsupported checksum algorithm: [%s]".formatted(archiveDigestAlgorithm));
            }

            // Ask the driver to copy files to the temp directory
            if (context.isChunkingEnabled()) {
                // TODO can bypass this if there is only one chunk.
                recomposeSingle(tarFileName, context, archiveFs, progress, tarFile);
            } else {
                archiveFs.retrieve(archiveId, tarFile, progress);

                if (getTarIV() != null) {
                    // Decrypt tar file
                    Utils.checkFileHash("ret-single-tar", tarFile, encTarDigest);

                    Encryption.decryptFile(context, tarFile, getTarIV());
                }
            }
        });
        doRetrieveFromWorkerToUserFs(context, userFs, tarFile, progress, timeStampDirName);
    }

    protected void copyFilesToUserFs(Progress progress, File payloadDir, Device userFs, long bagDirSize, String timeStampDirName) throws Exception {
        trackProgress(progress, bagDirSize, () -> {
            Path basePath = payloadDir.toPath();
            File[] contents = payloadDir.listFiles();
            Arrays.sort(contents);
            for (File content : contents) {
                String taskDesc = basePath.relativize(content.toPath()).toString();
                TwoSpeedRetry.DvRetryTemplate template = userFsTwoSpeedRetry.getRetryTemplate(String.format("toUserFs - %s", taskDesc));
                template.execute((RetryCallback<Object, Exception>) retryContext -> {
                    if (userFs instanceof SFTPFileSystemDriver) {
                        return ((SFTPFileSystemDriver) userFs).store(retrievePath, content, progress, timeStampDirName);
                    } else {
                        return userFs.store(retrievePath, content, progress);
                    }
                });
            }
        });
    }
    private void sendError(String msg) {
        sendEvent(new RetrieveError(jobID, depositId, retrieveId, msg));
    }
    private void logProgress(Progress progress) {
        log.info("Copied: [{}] directories, [{}] files, [{}] bytes", progress.getDirCount(), progress.getFileCount(), progress.getByteCount());
    }
    private void sendEvent(Event event) {
        eventSender.send(event.withUserId(userID));
    }
    /*
    This method allows us to override the clock in tests.
     */
    protected Clock getClock() {
        return Clock.systemDefaultZone();
    }
    private void initStates() {
        sendEvent(new InitStates(jobID, depositId, RetrieveState.getRetrieveStates()));
    }
    private void trackProgress(Progress progress, long expectedSizeBytes, Trackable trackable) throws Exception {
        var tracker = new ProgressTracker(progress, jobID, depositId, expectedSizeBytes, eventSender);
        tracker.track(trackable);
    }
}
