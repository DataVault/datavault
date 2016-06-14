package org.datavaultplatform.common.retentionpolicy.impl.uk;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stuart Lewis on 13/6/2016.
 *
 * See: http://www.wellcome.ac.uk/About-us/Policy/Policy-and-position-statements/WTD002753.htm
 *  Data generated in the course of research should be kept securely in paper or electronic format, as appropriate.
 *  The Trust considers a minimum of ten years to be an appropriate period, but research based on clinical samples or
 *  relating to public health might require longer storage to allow for long-term follow-up to occur.
 */
public class WTPHCRetentionPolicy implements RetentionPolicy {

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
        c.add(Calendar.YEAR, 20);
        check = c.getTime();

        return check;
    }
}
