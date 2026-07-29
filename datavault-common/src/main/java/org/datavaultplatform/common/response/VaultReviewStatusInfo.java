package org.datavaultplatform.common.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.datavaultplatform.common.model.VaultReview;

@Data
@NoArgsConstructor
public class VaultReviewStatusInfo {

    private VaultReview latestSubmittedReview;
    private VaultReview underwayReview;

}
