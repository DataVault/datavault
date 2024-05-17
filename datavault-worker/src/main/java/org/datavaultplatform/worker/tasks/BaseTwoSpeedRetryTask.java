package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class BaseTwoSpeedRetryTask extends Task  {
    private static final int DEFAULT_USERFS_MAX_ATTEMPTS = 10;
    private static final int DEFAULT_USERFS_DELAY_SECS_1 = 60;
    private static final int DEFAULT_USERFS_DELAY_SECS_2 = 60;
    protected TwoSpeedRetry userFsTwoSpeedRetry;

    protected void setupUserFsTwoSpeedRetry(Map<String, String> properties) {
        this.userFsTwoSpeedRetry = getUserFsTwoSpeedRetry(properties);
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
}
