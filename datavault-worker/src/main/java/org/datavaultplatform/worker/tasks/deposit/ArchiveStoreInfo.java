package org.datavaultplatform.worker.tasks.deposit;

import com.oracle.bmc.util.internal.StringUtils;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.springframework.util.Assert;

public record ArchiveStoreInfo(String archiveStoreId, ArchiveStore archiveStore, String archiveId) {

    public ArchiveStoreInfo {
        Assert.isTrue(StringUtils.isNotBlank(archiveStoreId), "The archiveStoreId cannot be blank");
        Assert.isTrue(StringUtils.isNotBlank(archiveId), "The archiveId cannot be blank");
        Assert.isTrue(archiveStore != null, "The archiveStore cannot be null");
    }
}
