package org.datavaultplatform.common.util;

import org.datavaultplatform.common.response.VaultInfo;

public class PageDTOVaultInfo extends PageDTO<VaultInfo> {
    // Copy constructor
    public PageDTOVaultInfo(PageDTO<VaultInfo> page) {
        this.setContent(page.getContent());
        this.setTotalElements(page.getTotalElements());
        this.setTotalPages(page.getTotalPages());
    }
}
