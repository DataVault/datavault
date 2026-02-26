package org.datavaultplatform.common.retentionpolicy.impl;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Stuart Lewis on 30/10/2015.
 */
public class FiveMinuteRetentionPolicy implements RetentionPolicy {

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

    public LocalDate getReviewDate(Vault v) {
        LocalDateTime check = v.getCreationTime();

        // Add five minutes
        check = check.plusMinutes(5);
        
        return check.toLocalDate();
    }
}
