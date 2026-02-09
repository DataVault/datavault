package org.datavaultplatform.common.util;

/**
 * Lets us customize how we interpret the ProcessInfo for failure.
 *
 **/
public interface ProcessInfoExitStatusSupport {

    ProcessInfoExitStatusSupport DEFAULT = ProcessHelper.ProcessInfo::wasFailure;

    boolean isProcessInfoFailure(ProcessHelper.ProcessInfo processInfo);
}
