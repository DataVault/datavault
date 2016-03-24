package org.datavaultplatform.common.retentionpolicy.impl;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.PolicyStatus;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stuart Lewis on 30/10/2015.
 */
public class DefaultRetentionPolicy implements RetentionPolicy {

    public int run(Vault v) {
        Date now = new Date();
        Date check = v.getCreationTime();

        // Add five years
        Calendar c = Calendar.getInstance();
        c.setTime(check);
        c.add(Calendar.YEAR, 5);
        check = c.getTime();

        // Is it time for review?
        if (check.before(now)) {
            v.setPolicyStatus(PolicyStatus.REVIEW);
            return PolicyStatus.REVIEW;
        } else {
            v.setPolicyStatus(PolicyStatus.OK);
            return PolicyStatus.OK;
        }
    }
}
