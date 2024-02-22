package org.datavaultplatform.common.retentionpolicy.impl;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stuart Lewis on 30/10/2015.
 */
public class FiveMinuteRetentionPolicy implements RetentionPolicy {

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
        Date check = v.getCreationTime();

        // Add five minutes
        Calendar c = Calendar.getInstance();
        c.setTime(check);
        c.add(Calendar.MINUTE, 5);
        check = c.getTime();

        return check;
    }
}
