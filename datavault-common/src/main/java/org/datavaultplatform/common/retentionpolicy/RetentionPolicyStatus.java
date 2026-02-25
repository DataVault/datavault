package org.datavaultplatform.common.retentionpolicy;

public class RetentionPolicyStatus {

    public static final int UNCHECKED = 0;
    public static final int OK = 1;
    public static final int REVIEW = 2;
    public static final int ERROR = 3;

    private RetentionPolicyStatus() {
        /* This utility class should not be instantiated */
    }

    public static String getDescription(int retentionPolicyStatusCode) {
        return switch (retentionPolicyStatusCode) {
            case UNCHECKED -> "UNCHECKED";
            case OK -> "OK";
            case REVIEW -> "REVIEW";
            case ERROR -> "ERROR";
            default -> "UNKNOWN[" + retentionPolicyStatusCode + "]";
        };
    }
}
