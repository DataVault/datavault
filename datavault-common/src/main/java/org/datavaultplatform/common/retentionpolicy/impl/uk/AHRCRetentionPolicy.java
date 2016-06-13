package org.datavaultplatform.common.retentionpolicy.impl.uk;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stuart Lewis on 13/6/2016.
 *
 * See: http://www.ahrc.ac.uk/documents/guides/research-funding-guide/   p.56, section 4.2.7.5
 *  The AHRC requires a minimum of three years after the end of project funding for both preservation and
 *  sustainability, but in many, if not most, cases a longer period will be appropriate. This should be decided on
 *  the basis of the significance of the outputs in the context of your project, their potential value to the larger
 *  research community, and the cost of developing them within the project award. Reviewers will need to be assured
 *  that the proposed period of preservation or sustainability represents value for money.
 */
public class AHRCRetentionPolicy implements RetentionPolicy {

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

        // Add three years
        Calendar c = Calendar.getInstance();
        c.setTime(check);
        c.add(Calendar.YEAR, 3);
        check = c.getTime();

        return check;
    }
}
