package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.ValidationComplete;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.Utils;
import org.datavaultplatform.worker.operations.ChunkDownloadTracker;
import org.datavaultplatform.worker.operations.CopyBackFromArchive;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.tasks.PackageHelper;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class DepositVerifier extends DepositSupport {
    
    private final Set<ArchiveStoreInfo> archiveStoresInfos;

    public DepositVerifier(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String, String> properties,
                            Set<ArchiveStoreInfo> archiveStoresInfos
    ) {
        super(userID, jobID, depositId, userEventSender, bagID, context, lastEvent, properties);
        if (archiveStoresInfos == null) {
            String msg = "Deposit failed: null list of userStores";
            sendInvalidArgumentMessage(msg);
        }
        this.archiveStoresInfos = archiveStoresInfos;
    }
    
    public void verifyArchive(PackageHelper packageHelper) throws Exception {
        File tarFile = packageHelper.getTarFile();
        String tarHash = packageHelper.getTarHash();
        if(context.isChunkingEnabled() ) {
            verifyArchiveWithChunks(context, packageHelper);
        } else {
            String encTarHash = packageHelper.getEncTarHash();
            byte[] iv = packageHelper.getIv();
            verifyArchiveNoChunks(context, tarFile, tarHash, iv, encTarHash);
        }
        userEventSender.send(new ValidationComplete(jobID, depositId).withUserId(userID));
    }
 
    private void verifyArchiveWithChunks(Context context, PackageHelper packageHelper) throws Exception {

        for (ArchiveStoreInfo archiveStoreInfo : archiveStoresInfos) {

            ArchiveStore archiveStore = archiveStoreInfo.archiveStore();
            String archiveId = archiveStoreInfo.archiveId();
            
            Device device = (Device) archiveStore;

            if (archiveStore.getVerifyMethod() != Verify.Method.COPY_BACK && archiveStore.getVerifyMethod() != Verify.Method.CLOUD ) {
                throw new Exception("Wrong Verify Method: [" + archiveStore.getVerifyMethod() + "] has to be " + Verify.Method.COPY_BACK);
            }

            if (device.hasMultipleCopies()) {
                boolean firstArchiveStoreLocation = true;
                for (String loc : device.getLocations()) {
                    if (firstArchiveStoreLocation || context.isMultipleValidationEnabled()) {
                        verifyArchiveWithLocation(context, packageHelper, archiveStore, archiveId, loc, true, firstArchiveStoreLocation);
                    }
                    firstArchiveStoreLocation = false;
                }
            } else {
                if (archiveStore.getVerifyMethod() != Verify.Method.CLOUD) {
                    verifyArchiveSingleCopy(context, packageHelper, archiveStore, archiveId, true);
                } else {
                    verifyArchiveForCloud();
                }
            }
        }
    }

    private void verifyArchiveForCloud() {
        log.info("Skipping verification as a cloud plugin (for now)");
    }

    private void verifyArchiveSingleCopy(Context context, PackageHelper packageHelper, ArchiveStore archiveStore,
                                         String archiveId, boolean doVerification) throws Exception {
        verifyArchiveWithLocation(context, packageHelper, archiveStore, archiveId, null,  false, doVerification);
    }

    /**
     * @param context
     * @param tarFile
     * @param tarHash
     * @throws Exception
     */
    private void verifyArchiveNoChunks(Context context, File tarFile, String tarHash, byte[] iv, String encTarHash) throws Exception {

        boolean alreadyVerified = false;

        for (ArchiveStoreInfo archiveStoreInfo : archiveStoresInfos) {
            ArchiveStore archiveStore = archiveStoreInfo.archiveStore();
            Device device = (Device) archiveStore;
            String archiveId = archiveStoreInfo.archiveId();

            Verify.Method vm = archiveStore.getVerifyMethod();
            log.info("Verification method: [{}]", vm);

            log.debug("verifyArchive - archiveId: [{}]", archiveId);

            // Get the tar file

            if ((vm == Verify.Method.LOCAL_ONLY) && (!alreadyVerified)){

                // Decryption
                if(iv != null) {
                    Encryption.decryptFile(context, tarFile, iv);
                }

                // Verify the contents of the temporary file
                verifyTarFile(context.getTempDir(), tarFile, null);

            } else if (vm == Verify.Method.COPY_BACK) {

                alreadyVerified = true;

                // Delete the existing temporary file
                tarFile.delete();
                // Copy file back from the archive storage
                if (device.hasMultipleCopies()) {
                    for (String loc : device.getLocations()) {
                        CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveId, tarFile, loc);

                        // check encrypted tar
                        Utils.checkFileHash("enc-tar", tarFile, encTarHash);

                        // Decryption
                        if(iv != null) {
                            Encryption.decryptFile(context, tarFile, iv);
                        }

                        // Verify the contents
                        verifyTarFile(context.getTempDir(), tarFile, tarHash);
                    }
                } else {
                    CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveId, tarFile);

                    // check encrypted tar
                    Utils.checkFileHash("enc-tar", tarFile, encTarHash);

                    // Decryption
                    if(iv != null) {
                        Encryption.decryptFile(context, tarFile, iv);
                    }

                    // Verify the contents
                    verifyTarFile(context.getTempDir(), tarFile, tarHash);
                }
            } else if (vm == Verify.Method.CLOUD) {
                // do nothing for now but we hope to extend this so that we can compare checksums supplied by the cloud api
                log.info("Skipping verification as a cloud plugin (for now)");
            }
        }
    }

    private void verifyArchiveWithLocation(Context context, PackageHelper packageHelper, ArchiveStore archiveStore,
                                           String archiveId, String location, boolean multipleCopies, boolean doVerification) throws Exception {

        File[] chunkFiles = packageHelper.getChunkFiles();
        HashMap<Integer, byte[]> ivs = packageHelper.getChunksIVs();
        String[] encChunksHash = packageHelper.getEncChunksHash();
        
        int noOfThreads = context.getNoChunkThreads();
        log.debug("Number of threads: [{}]", noOfThreads);
        TaskExecutor<Object> executor = new TaskExecutor<>(noOfThreads, "Chunk download failed.");
        for (int i = 0; i < chunkFiles.length; i++) {
            int chunkNumber = i + 1;
            // if less that max threads started, start new one
            File chunkFile = chunkFiles[i];
            //String chunkHash = chunksHash[i];

            ChunkDownloadTracker cdt = new ChunkDownloadTracker(
                    archiveId, archiveStore,
                    chunkFile, context,
                    chunkNumber, doVerification,
                    encChunksHash, ivs,
                    location, multipleCopies);
            log.debug("Creating chunk download task: [{}]", chunkNumber);
            executor.add(cdt);
        }
        executor.execute(result -> {});
        
        if(doVerification) {
            File tarFile = packageHelper.getTarFile();
            String tarHash = packageHelper.getTarHash();
            FileSplitter.recomposeFile(chunkFiles, tarFile);

            // Verify the contents
            verifyTarFile(context.getTempDir(), tarFile, tarHash);
        }
    }

    /**
     * Compare the SHA hash of the passed in tar file and the passed in original hash.
     * <p>
     * First compare the tar file then the bag then clean up
     * <p>
     * If the verification fails at either check throw an exception (maybe we could throw separate exceptions here)
     * @param tempPath Path to the temp storage location
     * @param tarFile File 
     * @param origTarHash String representing the orig hash
     * @throws Exception
     */
    private void verifyTarFile(Path tempPath, File tarFile, String origTarHash) throws Exception {

        Utils.checkFileHash("tar", tarFile, origTarHash);

        // Cleanup
        log.info("Cleaning up ...");
        tarFile.delete();
    }

}
