package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.nio.file.Path;

public class DepositTransferHelper {
    private final Path bagDataPath;
    private final File bagDir;

    public DepositTransferHelper(Path bagDataPath, File bagDir) {
        this.bagDataPath = bagDataPath;
        this.bagDir = bagDir;
    }

    public Path getBagDataPath() {
        return bagDataPath;
    }

    public File getBagDir() {
        return bagDir;
    }

}
