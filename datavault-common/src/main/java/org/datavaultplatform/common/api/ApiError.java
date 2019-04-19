package org.datavaultplatform.common.api;

import java.util.Date;

public class ApiError {
    private Date timestamp;
    private String message;
    private Exception exception;

    private ApiError() {
        setTimestamp(new Date());
    }

    public ApiError(Exception ex) {
        this();
        this.setException(ex);
        this.setMessage("Unexpected error");
    }

    public ApiError(String message, Exception ex) {
        this();
        this.setException(ex);
        this.setMessage(message);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
