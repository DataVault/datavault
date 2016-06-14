package org.datavaultplatform.common.retentionpolicy.impl.uk;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stuart Lewis on 13/6/2016.
 *
 * See: http://www.mrc.ac.uk/research/research-policy-ethics/good-research-practice/guidelines-and-standards/
 *  Basic research: If no restrictions apply, deposit primary/raw data and related material in an appropriate repository
 *  and/or publication should be considered. Research data and related material should be retained for a minimum of 10
 *  years after the study has been completed.
 */
public class MRCBasicRetentionPolicy implements RetentionPolicy {

    public int run(Vault v) {
        Date now = new Date();
        Date check = getReviewDate(v);

        // Is it time for review?
        if (check.before(now)) {
            v.setRetentionPolicyStatus(RetentionPolicyStatus.REVIEW);
            return RetentionPolicyStatus.REVIEW;
        } else {
            v.setRetentionPolicyStatus(RetentionPolicyStatus.OK);
            return RetentionPolicyStatus.OK;
        }
    }

    public Date getReviewDate(Vault v) {
        // Work from the date of last deposit
        Date check = v.getCreationTime();
        if (!v.getDeposits().isEmpty()) {
            check = v.getDeposits().get(v.getDeposits().size() - 1).getCreationTime();
        }

        // Add ten years
        Calendar c = Calendar.getInstance();
        c.setTime(check);
        c.add(Calendar.YEAR, 10);
        check = c.getTime();

        return check;
    }
}
