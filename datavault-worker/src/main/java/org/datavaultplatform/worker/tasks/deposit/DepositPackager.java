package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.PackageComplete;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.worker.operations.ChecksumTracker;
import org.datavaultplatform.worker.operations.EncryptionTracker;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.operations.PackagerV2;
import org.datavaultplatform.worker.tasks.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.datavaultplatform.worker.tasks.deposit.DepositState.DepositState03StoringInArchive;

@Slf4j
public class DepositPackager extends DepositSupport {

    private final PackagerV2 packagerV2 = new PackagerV2();

    private final String tarHashAlgorithm = Verify.getAlgorithm();

    public DepositPackager(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String, String> properties) {
        super(userID, jobID, depositId, userEventSender, bagID, context, lastEvent, properties);
    }
    
    public PackageHelper packageStep(File bagDir) throws Exception {
        final PackageHelper retVal = new PackageHelper();
        doPackageStep(bagDir, retVal);
        return retVal;
    }

    protected void doPackageStep(File bagDir, PackageHelper packageHelper) throws Exception {
        log.info("Creating bag ...");
        createBag(bagDir,
                properties.get(PropNames.DEPOSIT_METADATA),
                properties.get(PropNames.VAULT_METADATA),
                properties.get(PropNames.EXTERNAL_METADATA));
        // Tar the bag directory
        log.info("Creating tar file ...");
        File tarFile = DepositUtils.createTar(context, bagID, bagDir);
        packageHelper.setTarFile(tarFile);
        log.info("Tar file: " + tarFile.length() + " bytes");
        log.info("Tar file location: " + packageHelper.getTarFile().getAbsolutePath());

        sendEvent(new PackageComplete(jobID, depositId).withNextState(DepositState03StoringInArchive.getStateNumber()));

        //at this point, we have a tar file

        packageHelper.setTarHash(Verify.getDigest(packageHelper.getTarFile()));
        packageHelper.setArchiveSize(packageHelper.getTarFile().length());
        log.info("Checksum algorithm: " + tarHashAlgorithm);
        log.info("Checksum: " + packageHelper.getTarHash());
        sendEvent(new ComputedDigest(jobID, depositId, packageHelper.getTarHash(), tarHashAlgorithm));
        
        if (context.isChunkingEnabled()) {
            createChunks(packageHelper);
        } else {
            log.debug("Last event is: " + getLastEventClass() + " skipping chunking");
        }

        // Encryption
        if (context.isEncryptionEnabled()) {
            log.info("Encrypting file(s)...");
            if (context.isChunkingEnabled()) {
                log.info("Encrypting [{}] chunk files ", packageHelper.getChunkHelpers().size());
                encryptChunks(packageHelper);
            } else {
                log.info("Encrypting single non-chunk file [{}]", packageHelper.getTarFile());
                encryptFullTar(packageHelper);
            }
        } else {
            log.debug("Last event is: " + getLastEventClass() + " skipping enc checksum");
        }

        // at this point, we have finished all packaging so can delete the bag dir
        log.info("We have successfully created the Tar, so lets delete the Bag to save space");
        FileUtils.deleteDirectory(bagDir);
    }

    private void createBag(File bagDir, String depositMetadata, String vaultMetadata, String externalMetadata) throws Exception {
        log.debug("Creating bag in bag dir: " + bagDir.getCanonicalPath());
        packagerV2.createBag(bagDir);

        // Add vault/deposit/type metadata to the bag
        packagerV2.addMetadata(bagDir, depositMetadata, vaultMetadata, null, externalMetadata);
    }

    private void createChunks(PackageHelper packageHelper) throws Exception {
        log.info("Chunking tar file ...");
        File tarFile = packageHelper.getTarFile();
        File[] chunkFiles = FileSplitter.splitFile(tarFile, context.getChunkingByteSize());

        var chunkHelpers = packageHelper.getChunkHelpers();
        for (int i = 0; i < chunkFiles.length; i++) {
            int chunkNumber = i + 1;
            var chunkHelper = new PackageHelper.ChunkHelper(chunkNumber);
            chunkHelper.setChunkFile(chunkFiles[i]);
            chunkHelpers.put(chunkNumber, chunkHelper);
        }

        TaskExecutor<ChecksumHelper> executor = new TaskExecutor<>(getNumberOfChunkThreads(), "Chunk encryption failed.");
        for (int i = 0; i < chunkFiles.length; i++) {
            int chunkNumber = i + 1;
            File chunk = chunkFiles[i];
            ChecksumTracker ct = new ChecksumTracker(chunk, chunkNumber);

            log.debug("Creating chunk checksum task:" + chunkNumber);
            executor.add(ct);

        }
        executor.execute((ChecksumHelper result) -> {
            int chunkNumber = result.chunkNumber();
            PackageHelper.ChunkHelper helper = chunkHelpers.get(chunkNumber);
            helper.setChunkHash(result.chunkHash());
            log.info("Chunk file " + chunkNumber + ": " + helper.getChunkFile().length() + " bytes");
            log.info("Chunk file location: " + helper.getChunkFile().getAbsolutePath());
            log.info("Checksum algorithm: " + tarHashAlgorithm);
            log.info("Checksum: " + helper.getChunkHash());
        });

        if (!context.isEncryptionEnabled()) {
            HashMap<Integer, String> chunksDigest = new HashMap<>();
            chunkHelpers.forEach((chunkNumber, chunkHelper) -> chunksDigest.put(chunkNumber, chunkHelper.getChunkHash()));
            sendEvent(new ComputedChunks(jobID, depositId, chunksDigest, tarHashAlgorithm));
        }
        log.info(chunkFiles.length + " chunk files created.");
    }

    private void encryptChunks(PackageHelper packageHelper) throws Exception {

        TaskExecutor<EncryptionChunkHelper> executor = new TaskExecutor<>(getNumberOfChunkThreads(), "Chunk encryption failed.");

        Map<Integer, PackageHelper.ChunkHelper> chunkHelpers = packageHelper.getChunkHelpers();
        chunkHelpers.forEach((chunkNumber, chunkHelper) -> {
            EncryptionTracker et = new EncryptionTracker(chunkNumber, chunkHelper.getChunkFile(), context);
            log.debug("Creating chunk encryption task:" + chunkNumber);
            executor.add(et);
        });

        executor.execute((EncryptionChunkHelper result) -> {
            int chunkNumber = result.getChunkNumber();
            PackageHelper.ChunkHelper helper = packageHelper.getChunkHelpers().get(chunkNumber);
            helper.setChunkEncHash(result.getEncTarHash());
            helper.setChunkIV(result.getIv());
        });
        log.info("{} chunk files encrypted.", chunkHelpers.size());
        sendEvent(new ComputedEncryption(jobID, depositId, packageHelper.getChunksIVs(), null,
                context.getEncryptionMode().toString(), null, packageHelper.getEncChunkHashes(),
                packageHelper.getChunkHashes(), tarHashAlgorithm));
    }

    private void encryptFullTar(PackageHelper packageHelper) throws Exception {

        File tarFile = packageHelper.getTarFile();
        byte[] iv = Encryption.encryptFile(context, tarFile);
        String encTarHash = Verify.getDigest(tarFile);

        log.info("Encrypted Tar file: " + tarFile.length() + " bytes");
        log.info("Encrypted tar checksum: " + encTarHash);

        sendEvent(new ComputedEncryption(jobID, depositId, null,
                iv, context.getEncryptionMode().toString(),
                encTarHash, null, null, null));

        packageHelper.setIv(iv);
        packageHelper.setEncTarHash(encTarHash);
    }
}

