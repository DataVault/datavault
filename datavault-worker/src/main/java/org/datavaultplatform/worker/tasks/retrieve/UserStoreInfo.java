package org.datavaultplatform.worker.tasks.retrieve;

import org.datavaultplatform.common.storage.Device;
import org.springframework.util.Assert;

public record UserStoreInfo(Device userFs, String timeStampDirName){
    public UserStoreInfo {
        Assert.isTrue(userFs != null, "The userFs cannot be null");
    }
}
