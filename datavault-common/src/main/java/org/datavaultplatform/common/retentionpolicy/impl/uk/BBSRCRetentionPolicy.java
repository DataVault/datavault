package org.datavaultplatform.common.retentionpolicy.impl.uk;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Stuart Lewis on 6/6/2016.
 *
 * See: http://www.bbsrc.ac.uk/documents/data-sharing-policy-pdf/
 *  In line with the BBSRC Statement on Safeguarding Good Scientific Practice, data should also be retained for a
 *  period of ten years after completion of a research project.
 */
public class BBSRCRetentionPolicy implements RetentionPolicy {

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
