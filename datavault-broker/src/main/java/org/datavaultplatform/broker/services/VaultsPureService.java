package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultsPureService {
    private static final Logger logger = LoggerFactory.getLogger(VaultsPureService.class);

    public Boolean hasProducedPureRecord(Vault vault) {
        return true;
    }

}
