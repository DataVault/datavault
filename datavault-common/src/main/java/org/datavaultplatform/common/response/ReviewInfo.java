package org.datavaultplatform.common.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;
import lombok.Data;
import java.util.List;

/*
    In fancy terms this is a Transfer Object. In fact its just a convenience class to pass all the data
    relating to a VaultReview + DepositReviews.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "ReviewInfo")
@Data
@NoArgsConstructor
public class ReviewInfo {

    @ApiObjectField(description = "Universally Unique Identifier for the VaultReview", name="VaultReview Id")
    private String vaultReviewId;

    @ApiObjectField(description = "List of all deposits associated with a VaultReview", name="Deposit Ids")
    private List<String> depositIds;

    @ApiObjectField(description = "List of all depositReviews associated with a VaultReview", name="DepositReview Ids")
    private List<String> depositReviewIds;

}
