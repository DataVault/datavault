package org.datavaultplatform.common.retentionpolicy.impl.uk;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Stuart Lewis on 13/6/2016.
 *
 * See: http://www.mrc.ac.uk/research/research-policy-ethics/good-research-practice/guidelines-and-standards/
 *  Basic research: If no restrictions apply, deposit primary/raw data and related material in an appropriate repository
 *  and/or publication should be considered. Research data and related material should be retained for a minimum of 10
 *  years after the study has been completed.
 */
public class MRCBasicRetentionPolicy implements RetentionPolicy {

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

        // Add ten years
        check = check.plusYears(10);

        return check.toLocalDate();
    }
}
