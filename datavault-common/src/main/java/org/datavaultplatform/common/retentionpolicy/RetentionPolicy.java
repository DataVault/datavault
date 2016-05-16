package org.datavaultplatform.common.retentionpolicy;

import org.datavaultplatform.common.model.Vault;

import java.util.Date;

public interface RetentionPolicy {

    /**
     * Execute the policy on a Vault
     */
    public int run(Vault v);

    /**
     * Get the current review date of the policy
     */
    public Date getReviewDate(Vault v);
}
