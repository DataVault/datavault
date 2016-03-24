package org.datavaultplatform.common.retentionpolicy.impl;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.PolicyStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stuart Lewis on 29/10/2015.
 */
public class EPSRCRetentionPolicy implements RetentionPolicy {

    public int run(Vault v) {
        Date now = new Date();
        Date check;

        // Get all the retrieve events
        ArrayList<Retrieve> retrieves = new ArrayList();
        for (Deposit d : v.getDeposits()) {
            retrieves.addAll(d.getRetrieves());
        }

        // Have their been any retrieves?
        if (!retrieves.isEmpty()) {
            check = retrieves.get(0).getTimestamp();
            for (Retrieve r : retrieves) {
                if (r.getTimestamp().after(check)) {
                    check = r.getTimestamp();
                }
            }
        }
        // No retrieves, so use date of last deposit
        else {
            if (v.getDeposits().isEmpty()) {
                // No deposits, so return an error
                v.setPolicyStatus(PolicyStatus.ERROR);
                return PolicyStatus.ERROR;
            } else {
                check = v.getDeposits().get(v.getDeposits().size() - 1).getCreationTime();
            }
        }

        // Add ten years
        Calendar c = Calendar.getInstance();
        c.setTime(check);
        c.add(Calendar.YEAR, 10);
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
