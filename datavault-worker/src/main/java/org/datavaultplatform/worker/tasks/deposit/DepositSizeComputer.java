package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.ComputedSize;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.task.Context;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class DepositSizeComputer extends DepositSupport {

    private final Map<String, UserStore> userStores;
    private final List<String> fileStorePaths;

    public DepositSizeComputer(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String, String> properties,
                               Map<String,UserStore> userStores, List<String> fileStorePaths) {
        super(userID, jobID, depositId, userEventSender, bagID, context, lastEvent, properties);
        this.userStores = userStores;
        this.fileStorePaths = fileStorePaths;
    }

    public long calculateTotalDepositSize() {
        if (fileStorePaths == null) {
            String msg = "Deposit failed: null list of fileStorePaths";
            Exception e = new Exception(msg);
            log.error(msg);
            sendError(msg);
            throw new RuntimeException(e);
        }
        // Calculate the total deposit size of selected files
        long depositTotalSize = fileStorePaths.stream()
                .map(this::getFileStorePathSize)
                .mapToLong(Long::longValue)
                .sum();
        // Store the calculated deposit size
        sendEvent(new ComputedSize(this.jobID, this.depositId, depositTotalSize));
        return depositTotalSize;
    }

    private long getFileStorePathSize(String fileStorePath) {
        var fStorePath = new FileStorePath(fileStorePath);
        String storageID = fStorePath.storageID();
        String storagePath = fStorePath.storagePath();
        try {
            UserStore userStore = userStores.get(storageID);
            long fileStorePathSize = userStore.getSize(storagePath);
            return fileStorePathSize;
        } catch (Exception e) {
            String msg = "Deposit failed: could not access user filesystem";
            log.error(msg, e);
            sendError(msg);
            throw new RuntimeException(e);
        }
    }

}
