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
 *  Population health and clinical studies: The retention period for primary/raw data and related material from
 *  population health or clinical studies will be informed by the relevant regulatory framework, the legal requirements
 *  outlined in guidance from the MHRA and any additional requirements identified by ethics committees or professional
 *  codes. For clinical research undertaken in MRC research units and institutes, the MRC expects research data relating
 *  to such studies to be retained for 20 years after the study has been completed to allow an appropriate follow-up
 *  period. Studies which propose retention periods beyond 20 years must include valid justification, for example,
 *  research data relating to longitudinal studies will often be retained indefinitely and archived and managed
 *  accordingly.
 */
public class MRCPHCRetentionPolicy implements RetentionPolicy {

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
