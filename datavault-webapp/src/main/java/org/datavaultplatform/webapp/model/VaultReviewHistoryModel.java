package org.datavaultplatform.webapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class VaultReviewHistoryModel {

    private List<VaultReviewViewModel> vaultReviewViewModels;
    
    public final void setVaultReviewViewModels(List<VaultReviewViewModel> vaultReviewViewModels) {
        this.vaultReviewViewModels = vaultReviewViewModels;
        if (this.vaultReviewViewModels == null) {
            this.vaultReviewViewModels = List.of();
        }
    }
}
