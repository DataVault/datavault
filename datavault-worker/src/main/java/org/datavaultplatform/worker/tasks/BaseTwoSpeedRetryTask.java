package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class BaseTwoSpeedRetryTask extends Task  {
    private static final int DEFAULT_USERFS_RETRIEVE_ATTEMPTS = 10;
    private static final int DEFAULT_USERFS_DELAY_SECS_1 = 60;
    private static final int DEFAULT_USERFS_DELAY_SECS_2 = 60;
    protected TwoSpeedRetry userFsTwoSpeedRetry;

    protected void setupUserFsTwoSpeedRetry(Map<String, String> properties) {
        this.userFsTwoSpeedRetry = getUserFsTwoSpeedRetry(properties);
    }

    protected static TwoSpeedRetry getUserFsTwoSpeedRetry(Map<String, String> properties) {

        int userFsRetrieveMaxAttempts = DEFAULT_USERFS_RETRIEVE_ATTEMPTS;
        long userFsRetrieveDelayMs1 = TimeUnit.SECONDS.toMillis(DEFAULT_USERFS_DELAY_SECS_1);
        long userFsRetrieveDelayMs2 = TimeUnit.SECONDS.toMillis(DEFAULT_USERFS_DELAY_SECS_2);

        if (properties.containsKey(PropNames.USER_FS_RETRIEVE_MAX_ATTEMPTS)) {
            userFsRetrieveMaxAttempts = Integer.parseInt(properties.get(PropNames.USER_FS_RETRIEVE_MAX_ATTEMPTS));
        }
        if (properties.containsKey(PropNames.USER_FS_RETRIEVE_DELAY_MS_1)) {
            userFsRetrieveDelayMs1 = Long.parseLong(properties.get(PropNames.USER_FS_RETRIEVE_DELAY_MS_1));
        }
        if (properties.containsKey(PropNames.USER_FS_RETRIEVE_DELAY_MS_2)) {
            userFsRetrieveDelayMs2 = Long.parseLong(properties.get(PropNames.USER_FS_RETRIEVE_DELAY_MS_2));
        }

        return new TwoSpeedRetry(userFsRetrieveMaxAttempts, userFsRetrieveDelayMs1, userFsRetrieveDelayMs2);
    }
}
