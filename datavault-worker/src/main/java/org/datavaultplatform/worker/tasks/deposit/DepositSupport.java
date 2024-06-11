package org.datavaultplatform.worker.tasks.deposit;

import com.oracle.bmc.util.internal.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Slf4j
public abstract class DepositSupport {

    private static final int DEFAULT_USERFS_MAX_ATTEMPTS = 10;
    private static final int DEFAULT_USERFS_DELAY_SECS_1 = 60;
    private static final int DEFAULT_USERFS_DELAY_SECS_2 = 60;

    protected final String userID;
    protected final String jobID;
    protected final String depositId;
    protected final UserEventSender userEventSender;
    protected final String bagID;
    protected final Event lastEvent;
    protected final Context context;
    protected final Map<String,String> properties;
    private final TwoSpeedRetry userFsTwoSpeedRetry;
    protected final String lastEventClass = "DUMMY";

    public DepositSupport(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String,String> properties) {
        Assert.isTrue(StringUtils.isNotBlank(userID), "The userID cannot be blank");
        Assert.isTrue(StringUtils.isNotBlank(jobID), "The jobID cannot be blank");
        Assert.isTrue(StringUtils.isNotBlank(depositId), "The depositId cannot be blank");
        Assert.isTrue(userEventSender != null, "The userEventSender cannot be null");
        Assert.isTrue(StringUtils.isNotBlank(bagID), "The bagID cannot be blank");
        Assert.isTrue(context != null, "The context cannot be null");
        //lastEvent CAN BE NULL
        Assert.isTrue(properties != null, "The properties cannot be null");
        this.userID = userID;
        this.jobID = jobID;
        this.depositId = depositId;
        this.userEventSender = userEventSender;
        this.bagID = bagID;
        this.context = context;
        this.lastEvent = lastEvent;
        this.properties = properties;
        this.userFsTwoSpeedRetry = getUserFsTwoSpeedRetry(properties);
    }

    protected void sendError(String message) {
        log.error(message);
        sendEvent(new Error(jobID, depositId, message));
    }

    protected void sendEvent( Event event) {
        this.userEventSender.send(event);
    }

    protected int getNumberOfChunkThreads() {
        int noOfThreads = context.getNoChunkThreads();
        if (noOfThreads < 0) {
            noOfThreads = 25;
        }
        log.debug("Number of threads: [{}]", noOfThreads);
        return noOfThreads;
    }

    protected static TwoSpeedRetry getUserFsTwoSpeedRetry(Map<String, String> properties) {

        int userFsRetryMaxAttempts = DEFAULT_USERFS_MAX_ATTEMPTS;
        long userFsRetryDelayMs1 = TimeUnit.SECONDS.toMillis(DEFAULT_USERFS_DELAY_SECS_1);
        long userFsRetryDelayMs2 = TimeUnit.SECONDS.toMillis(DEFAULT_USERFS_DELAY_SECS_2);

        if (properties.containsKey(PropNames.USER_FS_RETRY_MAX_ATTEMPTS)) {
            userFsRetryMaxAttempts = Integer.parseInt(properties.get(PropNames.USER_FS_RETRY_MAX_ATTEMPTS));
        }
        if (properties.containsKey(PropNames.USER_FS_RETRY_DELAY_MS_1)) {
            userFsRetryDelayMs1 = Long.parseLong(properties.get(PropNames.USER_FS_RETRY_DELAY_MS_1));
        }
        if (properties.containsKey(PropNames.USER_FS_RETRY_DELAY_MS_2)) {
            userFsRetryDelayMs2 = Long.parseLong(properties.get(PropNames.USER_FS_RETRY_DELAY_MS_2));
        }

        return new TwoSpeedRetry(userFsRetryMaxAttempts, userFsRetryDelayMs1, userFsRetryDelayMs2);
    }

    public boolean eventHasNotBeenSeenBefore(Class<? extends Event> eventClass) {
        return DepositEvents.isLastEventBefore(lastEvent, eventClass);
    }
}
