package org.datavaultplatform.worker.tasks.retrieve;

import org.datavaultplatform.common.storage.Device;

public record UserStoreInfo(Device userFs, String timeStampDirName){
}
