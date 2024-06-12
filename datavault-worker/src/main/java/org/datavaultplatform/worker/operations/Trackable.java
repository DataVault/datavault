package org.datavaultplatform.worker.operations;

@FunctionalInterface
public interface Trackable {
    void track() throws Exception;
}
