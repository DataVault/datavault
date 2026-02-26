package org.datavaultplatform.common.retentionpolicy.impl.uk;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Created by Stuart Lewis on 29/10/2015.
 */
public class EPSRCRetentionPolicy implements RetentionPolicy {

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
        LocalDateTime check;

        // Get all the retrieve events
        ArrayList<Retrieve> retrieves = new ArrayList<>();
        for (Deposit d : v.getDeposits()) {
            retrieves.addAll(d.getRetrieves());
        }

        // Have their been any retrieves?
        if (!retrieves.isEmpty()) {
            check = retrieves.get(0).getTimestamp();
            for (Retrieve r : retrieves) {
                if (r.getTimestamp().isAfter(check)) {
                    check = r.getTimestamp();
                }
            }
        }
        // No retrieves, so use date of last deposit
        else {
            if (v.getDeposits().isEmpty()) {
                // No deposits, so use vault creation date
                check = v.getCreationTime();
            } else {
                check = v.getDeposits().get(v.getDeposits().size() - 1).getCreationTime();
            }
        }

        // Add ten years
        check = check.plusYears(10);

        return check.toLocalDate();
    }
}
