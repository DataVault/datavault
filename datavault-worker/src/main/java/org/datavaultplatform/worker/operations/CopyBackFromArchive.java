package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CopyBackFromArchive {
    private static final Logger logger = LoggerFactory.getLogger(CopyBackFromArchive.class);

    public static void copyBackFromArchive(ArchiveStore archiveStore, String archiveId, File tarFile) throws Exception {

        CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveId, tarFile, null);
    }

    public static void copyBackFromArchive(ArchiveStore archiveStore, String archiveId, File tarFile, String location) throws Exception {

        // Ask the driver to copy files to the temp directory
        Progress progress = new Progress();
        if (location == null) {
            ((Device)archiveStore).retrieve(archiveId, tarFile, progress);
        } else {
            ((Device)archiveStore).retrieve(archiveId, tarFile, progress, location);
        }
        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
    }
}
