package org.datavaultplatform.common.task;

public class TaskInterruptedException extends RuntimeException {
    public TaskInterruptedException(String message) {
        super(message);
    }
}
