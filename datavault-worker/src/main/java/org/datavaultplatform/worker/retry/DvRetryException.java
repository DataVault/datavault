package org.datavaultplatform.worker.retry;

public class DvRetryException extends Exception {

    public DvRetryException(String msg, Exception rootCause){
        super(msg, rootCause);
    }
}
