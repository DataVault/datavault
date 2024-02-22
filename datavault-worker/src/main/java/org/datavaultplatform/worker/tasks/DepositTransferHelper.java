package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.nio.file.Path;

public record DepositTransferHelper(Path bagDataPath, File bagDir) {

}
