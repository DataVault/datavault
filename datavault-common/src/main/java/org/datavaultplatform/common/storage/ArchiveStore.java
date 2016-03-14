package org.datavaultplatform.common.storage;

// Interface for back-end archive storage systems

public interface ArchiveStore {
    
    // Properties and methods relating to archive organisation and operation
    
    public Verify.Method verificationMethod = Verify.Method.COPY_BACK;
    public Verify.Method getVerifyMethod();
}
