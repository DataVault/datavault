package org.datavaultplatform.worker.tasks.retrieve;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;

import java.io.File;
import java.util.List;

@Slf4j
public record ArchiveDeviceInfo(Device archiveFs, boolean multiCopy, List<String> locations) {

    public ArchiveDeviceInfo(Device archiveFs) {
        this(archiveFs, false, null);
    }

    public ArchiveDeviceInfo(Device archiveFs, List<String> locations) {
        this(archiveFs, true, locations);
    }

    public static ArchiveDeviceInfo fromArchiveFs(Device archiveFs) {
        if (archiveFs.hasMultipleCopies()) {
            return new ArchiveDeviceInfo(archiveFs, archiveFs.getLocations());
        } else {
            return new ArchiveDeviceInfo(archiveFs);
        }
    }

    public void retrieve(String archiveId, File tarFile, Progress progress) throws Exception {
        if (multiCopy) {
            log.info("Device has multiple location");
            archiveFs.retrieve(archiveId, tarFile, progress, locations);
        } else {
            log.info("Device has single location");
            archiveFs.retrieve(archiveId, tarFile, progress);
        }
    }
}

