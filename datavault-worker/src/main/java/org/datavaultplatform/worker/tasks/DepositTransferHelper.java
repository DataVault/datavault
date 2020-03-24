package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.nio.file.Path;

public class DepositTransferHelper {
    private Path bagDataPath;
    private File bagDir;


    public Path getBagDataPath() {
        return bagDataPath;
    }

    public void setBagDataPath(Path bagDataPath) {
        this.bagDataPath = bagDataPath;
    }

    public File getBagDir() {
        return bagDir;
    }

    public void setBagDir(File bagDir) {
        this.bagDir = bagDir;
    }
}
