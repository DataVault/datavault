package org.datavaultplatform.common.util;

public class ProcessExitCodes {
    // Standard Unix/Linux exit codes
    public static final int EXIT_SUCCESS = 0; // 0
    public static final int EXIT_SIGKILL = 137; // 128 + 9
    public static final int EXIT_SIGTERM = 143; // 128 + 15

    private ProcessExitCodes() {
    }

   public static String getExitCodeString(int exitCode) {
        if (exitCode == EXIT_SIGKILL) {
            return "SIGKILL(%d)".formatted(exitCode);
        }
        if (exitCode == EXIT_SIGTERM) {
            return "SIGTERM(%d)".formatted(exitCode);
        }
       if (exitCode == EXIT_SUCCESS) {
           return "SUCCESS(%d)".formatted(exitCode);
       }
        return String.valueOf(exitCode);
    }

}