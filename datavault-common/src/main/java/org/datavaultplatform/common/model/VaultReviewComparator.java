package org.datavaultplatform.common.model;

import java.util.Comparator;

public class VaultReviewComparator implements Comparator<VaultReview> {

    @Override
    public int compare(VaultReview first, VaultReview second) {
        // Sort in descending order - most recent date first
        return (second.getCreationTime().compareTo(first.getCreationTime()));
    }
}
