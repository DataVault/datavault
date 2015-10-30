package org.datavaultplatform.common.retentionpolicy;

import org.datavaultplatform.common.model.Vault;

public interface RetentionPolicy {

    /**
     * Execute the policy on a Vault
     */
    public int run(Vault v);
}
