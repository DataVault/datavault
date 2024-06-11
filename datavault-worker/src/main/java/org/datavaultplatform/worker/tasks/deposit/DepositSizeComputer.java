package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.ComputedSize;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.task.Context;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class DepositSizeComputer extends DepositSupport {

    private final Map<String, UserStore> userStores;
    private final List<String> fileStorePaths;

    public DepositSizeComputer(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String, String> properties,
                               Map<String,UserStore> userStores, List<String> fileStorePaths) {
        super(userID, jobID, depositId, userEventSender, bagID, context, lastEvent, properties);
        if (userStores == null) {
            String msg = "Deposit failed: null list of userStores";
            sendInvalidArgumentMessage(msg);
        }
        if (fileStorePaths == null) {
            String msg = "Deposit failed: null list of fileStorePaths";
            sendInvalidArgumentMessage(msg);
        }
        this.userStores = userStores;
        this.fileStorePaths = fileStorePaths;
    }
    
    private void sendInvalidArgumentMessage(String msg) {
        var ex = new IllegalArgumentException(msg);
        log.error(msg);
        sendError(msg);
        throw ex;
    }

    public long calculateTotalDepositSize() {
        // Calculate the total deposit size of selected files
        long depositTotalSize = fileStorePaths.stream()
                .filter(Objects::nonNull)
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
            if(userStore != null) {
                long fileStorePathSize = userStore.getSize(storagePath);
                return fileStorePathSize;
            } else {
                throw new IllegalStateException("Could not get user store for " + storageID);
            }
        } catch (Exception e) {
            String msg = "Deposit failed: could not access user filesystem";
            log.error(msg, e);
            sendError(msg);
            throw new RuntimeException(e);
        }
    }

}
