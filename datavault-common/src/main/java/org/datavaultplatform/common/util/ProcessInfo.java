package org.datavaultplatform.common.util;

import java.time.Duration;
import java.util.List;

public record ProcessInfo(
        String description,
        long pid,
        int exitValue,
        List<String> outputMessages,
        Duration duration
) {

    public boolean wasSuccess() {
        return this.exitValue == 0;
    }

    public boolean wasFailure() {
        return !wasSuccess();
    }

    public List<String> getOutputMessages() {
        return outputMessages;
    }

    public int getExitValue() {
        return exitValue;
    }

    public Duration getDuration() {
        return duration;
    }
}
