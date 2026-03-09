package org.datavaultplatform.common.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/*
    In fancy terms this is a Transfer Object. In fact its just a convenience class to pass all the data
    relating to a VaultReview + DepositReviews.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "ReviewInfo")
@Data
@NoArgsConstructor
public class ReviewInfo {

    @Schema(description = "Universally Unique Identifier for the VaultReview")
    private String vaultReviewId;

    @Schema(description = "List of all deposits associated with a VaultReview")
    private List<String> depositIds;

    @Schema(description = "List of all depositReviews associated with a VaultReview")
    private List<String> depositReviewIds;

}
