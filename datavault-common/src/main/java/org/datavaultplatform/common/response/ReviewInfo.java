package org.datavaultplatform.common.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class ReviewInfo {

    @ApiObjectField(description = "Universally Unique Identifier for the VaultReview", name="VaultReview Id")
    private String vaultReviewId;

    @ApiObjectField(description = "List of all deposits associated with a VaultReview", name="Deposit Ids")
    private List<String> depositIds;

    @ApiObjectField(description = "List of all depositReviews associated with a VaultReview", name="DepositReview Ids")
    private List<String> depositReviewIds;



    public String getVaultReviewId() {
        return vaultReviewId;
    }

    public void setVaultReviewId(String vaultReviewId) {
        this.vaultReviewId = vaultReviewId;
    }

    public List<String> getDepositIds() {
        return depositIds;
    }

    public void setDepositIds(List<String> depositIds) {
        this.depositIds = depositIds;
    }

    public List<String> getDepositReviewIds() {
        return depositReviewIds;
    }

    public void setDepositReviewIds(List<String> depositReviewIds) {
        this.depositReviewIds = depositReviewIds;
    }
}
