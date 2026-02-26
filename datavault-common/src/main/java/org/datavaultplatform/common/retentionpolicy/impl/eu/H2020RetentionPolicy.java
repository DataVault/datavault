package org.datavaultplatform.common.retentionpolicy.impl.eu;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Stuart Lewis on 13/6/2016.
 *
 * No specific time limits set, so defaulting to 10 years
 */
public class H2020RetentionPolicy implements RetentionPolicy {

    @Override
    public int run(Vault v) {
        LocalDate now = LocalDate.now();
        LocalDate check = getReviewDate(v);

        // Is it time for review?
        if (check.isBefore(now)) {
            v.setRetentionPolicyStatus(RetentionPolicyStatus.REVIEW);
            return RetentionPolicyStatus.REVIEW;
        } else {
            v.setRetentionPolicyStatus(RetentionPolicyStatus.OK);
            return RetentionPolicyStatus.OK;
        }
    }

    @Override
    public LocalDate getReviewDate(Vault v) {
        // Work from the date of last deposit
        LocalDateTime check = v.getCreationTime();
        if (!v.getDeposits().isEmpty()) {
            check = v.getDeposits().get(v.getDeposits().size() - 1).getCreationTime();
        }

        // Add TEN years
        check = check.plusYears(10);

        return check.toLocalDate();
    }
}
