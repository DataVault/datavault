package org.datavaultplatform.webapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Used in VaultsController
@Slf4j
@Data
@NoArgsConstructor
public class VaultReviewViewModel {

    public static final Comparator<VaultReviewViewModel> BY_CREATION_TIME =
            Comparator.nullsFirst(Comparator.comparing(
                    VaultReviewViewModel::getCreationTime,
                    Comparator.nullsFirst(Comparator.naturalOrder())));

    private String vaultReviewId;
    private LocalDate nextReviewDate;
    private String comment;

    private LocalDateTime actionedDate;
    private LocalDate oldReviewDate;
    private LocalDateTime creationTime;

    private List<DepositReviewViewModel> depositReviewViewModels;
    
    public void setDepositReviewViewModels(List<DepositReviewViewModel> depositReviewViewModels) {
        this.depositReviewViewModels = depositReviewViewModels;
        if (depositReviewViewModels == null) {
            this.depositReviewViewModels = new ArrayList<>();
        }
    }
}

