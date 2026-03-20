package org.datavaultplatform.common.model;

// todo : convert to enum

public class DepositReviewDeleteStatus {

    public static final int RETAIN = 0;

    public static final int ONREVIEW = 1;

    public static final int ONEXPIRY = 2;

    public static final int NOW = 3;

    public static String getDescription(int deleteStatus) {
        return switch (deleteStatus) {
            case RETAIN -> "Retain";
            case ONREVIEW -> "On Review";
            case ONEXPIRY -> "On Expiry";
            case NOW -> "Now";
            default -> "Unknown[%s]".formatted(deleteStatus);
        };
    }
}
