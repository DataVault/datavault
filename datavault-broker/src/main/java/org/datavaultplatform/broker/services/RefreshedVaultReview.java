package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.VaultReview;

import java.util.List;

public record RefreshedVaultReview(VaultReview underway, List<DepositReview> depositReviewsAdded) {
}
