package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.request.CreateVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ValidateService {
    private static final Logger logger = LoggerFactory.getLogger(ValidateService.class);

    public List<String> validate(CreateVault vault) {
        List<String> retVal = new ArrayList<>();
        retVal.addAll(this.validateFieldset1(vault));
        retVal.addAll(this.validateFieldset2(vault));
        retVal.addAll(this.validateFieldset3(vault));
        retVal.addAll(this.validateFieldset4(vault));
        retVal.addAll(this.validateFieldset5(vault));
        return retVal;
    }

    private List<String> validateFieldset1(CreateVault vault) {
        List<String> retVal = new ArrayList<>();

        // Field set 1 should have completed
        // Affirmation
        Boolean affirmed = vault.getAffirmed();
        if (affirmed == null || affirmed == false) {
            retVal.add("Affirmation");
        }

        return retVal;
    }

    private List<String> validateFieldset2(CreateVault vault) {
        List<String> retVal = new ArrayList<>();

        // Field set 2 should have completed
        //Billing type and it should have been one of our controlled options
        String type = vault.getBillingType();
        if (type == null || type.isEmpty()) {
            retVal.add("BillingType missing");
            return retVal;
        }

        if (!type.equals("NA") && !type.equals("GRANT_FUNDING") && !type.equals("BUDGET_CODE")  && !type.equals("SLICE")) {
            retVal.add("BillingType invalid (" + type + ")");
            return retVal;
        }

        //If type GRANT_FUNDING or BUDGET_CODE
        if (type.equals("GRANT_FUNDING") || type.equals("BUDGET_CODE")) {

            //Authoriser
            String authoriser = vault.getAuthoriser();
            if (authoriser == null || authoriser.isEmpty()) {
                retVal.add("Authoriser missing");
            }
            //School / Unit
            String schoolOrUnit = vault.getSchoolOrUnit();
            if (schoolOrUnit == null || schoolOrUnit.isEmpty()) {
                retVal.add("School / Unit missing");
            }
            //Subunit
            String subunit = vault.getSubunit();
            if (subunit == null || subunit.isEmpty()) {
                retVal.add("Subunit missing");
            }
            return retVal;
        }

        //If type SLICE
        if (type.equals("SLICE")) {
            //slice
            String slice = vault.getSliceID();
            if (slice == null || slice.isEmpty()) {
                retVal.add("Slice ID missing");
            }
        }


        return retVal;
    }

    private List<String> validateFieldset3(CreateVault vault) {
        List<String> retVal = new ArrayList<>();
//        Field set 3 should have completed
//        Name
        String name = vault.getName();
        if (name == null || name.isEmpty()) {
            retVal.add("Name missing");
        }
//        Description
        String description = vault.getDescription();
        if (description == null || description.isEmpty()) {
            retVal.add("Description missing");
        }
//        Retention policy
        String policy = vault.getPolicyID();
        if (policy == null || policy.isEmpty()) {
            retVal.add("Retention policy missing");
        }
//        School
        String school = vault.getGroupID();
        if (school == null || school.isEmpty()) {
            retVal.add("School missing");
        }
//        Review Date
        String reviewDate = vault.getReviewDate();
        if (reviewDate == null || reviewDate.isEmpty()) {
            retVal.add("Review Data missing");
        }

        return retVal;
    }

    private List<String> validateFieldset4(CreateVault vault) {
        List<String> retVal = new ArrayList<>();

//        Field set 4 should have completed

        return retVal;
    }

    private List<String> validateFieldset5(CreateVault vault) {
        List<String> retVal = new ArrayList<>();

//        Field set 5 should have completed
//        Pure link
        Boolean pureLink = vault.getPureLink();
        if (pureLink == null || pureLink == false) {
            retVal.add("Pure link not accepted");
        }
        return retVal;
    }
}
