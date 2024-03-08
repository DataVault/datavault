package org.datavaultplatform.webapp.services;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ValidateService {

  public List<String> validate(CreateVault vault, String userID) {
    List<String> retVal = new ArrayList<>();
    retVal.addAll(this.validateFieldset1(vault));
    retVal.addAll(this.validateFieldset2(vault));
    retVal.addAll(this.validateFieldset3(vault));
    retVal.addAll(this.validateFieldset4(vault, userID));
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
    // Billing type and it should have been one of our controlled options
    String type = vault.getBillingType();
    if (type == null || type.isEmpty()) {
      retVal.add("BillingType missing");
      return retVal;
    }

    if (
      !type.equals("NA") &&
      !type.equals("GRANT_FUNDING") &&
      !type.equals("BUDGET_CODE") &&
      !type.equals("SLICE") &&
      !type.equals("FEEWAIVER") &&
      !type.equals("WILL_PAY") &&
      !type.equals("BUY_NEW_SLICE") &&
      !type.equals("FUNDING_NO_OR_DO_NOT_KNOW")
    ) {
      retVal.add("BillingType invalid (" + type + ")");
      return retVal;
    }

    // If type GRANT_FUNDING or BUDGET_CODE
    if (type.equals("GRANT_FUNDING")) {
      return this.validateGrantBillingType(vault);
    }

    if (type.equals("BUDGET_CODE")) {
      return this.validateBudgetBillingType(vault);
    }

    // If type SLICE
    if (type.equals("SLICE")) {
      // No longer reequire Slice ID. So do nothing.
    }

    if (type.equals("FEEWAIVER")) {
      // Currently no further checks
    }

    if (type.equals("WILL_PAY")) {
      String authoriser = vault.getBudgetAuthoriser();
      if (authoriser == null || authoriser.trim().isEmpty()) {
        retVal.add("Payment authoriser is missing.");
      }
    }

    if (type.equals("BUY_NEW_SLICE")) {
      // Currently no further checks
    }

    if (type.equals("FUNDING_NO_OR_DO_NOT_KNOW")) {
      // Currently no further checks
    }

    return retVal;
  }

  private List<String> validateBudgetBillingType(CreateVault vault) {
    List<String> retVal = new ArrayList<>();

    // Authoriser
    String authoriser = vault.getBudgetAuthoriser();
    if (authoriser == null || authoriser.isEmpty()) {
      retVal.add("Authoriser missing");
    }
    // School / Unit
    String schoolOrUnit = vault.getBudgetSchoolOrUnit();
    if (schoolOrUnit == null || schoolOrUnit.isEmpty()) {
      retVal.add("School / Unit missing");
    }
    // Subunit
    String subunit = vault.getBudgetSubunit();
    if (subunit == null || subunit.isEmpty()) {
      retVal.add("Subunit missing");
    }
    return retVal;
  }

  private List<String> validateGrantBillingType(CreateVault vault) {
    List<String> retVal = new ArrayList<>();

    String authoriser = vault.getGrantAuthoriser();
    if (authoriser == null || authoriser.isEmpty()) {
      retVal.add("Authoriser missing");
    }
    // School / Unit
    String schoolOrUnit = vault.getGrantSchoolOrUnit();
    if (schoolOrUnit == null || schoolOrUnit.isEmpty()) {
      retVal.add("School / Unit missing");
    }
    // Subunit
    String subunit = vault.getGrantSubunit();
    if (subunit == null || subunit.isEmpty()) {
      retVal.add("Subunit missing");
    }
    // Project Title
    String projectTitle = vault.getProjectTitle();
    if (projectTitle == null || projectTitle.isEmpty()) {
      retVal.add("Project Title missing");
    }
    // Grant end date
    Date grantEndDate = vault.getReviewDate();
    if (grantEndDate == null) {
      retVal.add("Review Date missing");
    }
    return retVal;
  }

  private List<String> validateFieldset3(CreateVault vault) {
    List<String> retVal = new ArrayList<>();
    // Field set 3 should have completed
    // Name
    String name = vault.getName();
    if (name == null || name.isEmpty()) {
      retVal.add("Name missing");
    }
    // Description
    String description = vault.getDescription();
    if (description == null || description.isEmpty()) {
      retVal.add("Description missing");
    }
    // Retention policy
    String policy = vault.getPolicyInfo();
    if (policy == null || policy.isEmpty()) {
      retVal.add("Retention policy missing");
    }
    // School
    String school = vault.getGroupID();
    if (school == null || school.isEmpty()) {
      retVal.add("School missing");
    }
    // Review Date
    Date reviewDate = vault.getReviewDate();
    if (reviewDate == null) {
      retVal.add("Review Date missing");
    } else {
      // if review date is less than 3 years from today
      int yearsToAdd = 3;
      if (DateTimeUtils.isBefore(reviewDate,this.getDefaultReviewDate(yearsToAdd))) {
        retVal.add("Review Date for selected policy is required to be at least " + yearsToAdd + " years");
      }

      if (DateTimeUtils.isBefore(reviewDate,this.getMaxReviewDate())) {
        retVal.add("Review Date for selected policy is required to be less than 30 years in the future");
      }
    }

    return retVal;
  }

  private List<String> validateFieldset4(CreateVault vault, String userID) {
    List<String> retVal = new ArrayList<>();

    // Field set 4 should have only one role per uuid
    String owner = vault.getVaultOwner() != null &&
      !vault.getVaultOwner().isEmpty()
      ? vault.getVaultOwner()
      : userID;
    List<String> ownerList = new ArrayList<>();
    ownerList.add(owner);
    List<String> ndms = vault.getNominatedDataManagers();
    List<String> deps = vault.getDepositors();
    boolean match1 = ndms
      .stream()
      .filter(x -> !Objects.equals(x, ""))
      .anyMatch(deps::contains);
    boolean match2 = ndms
      .stream()
      .filter(x -> !Objects.equals(x, ""))
      .anyMatch(ownerList::contains);
    boolean match3 = deps
      .stream()
      .filter(x -> !Objects.equals(x, ""))
      .anyMatch(ownerList::contains);

    if (match1) {
      retVal.add("A user cannot have the role of both a NDM and a depositor");
    }

    if (match2) {
      retVal.add("A user cannot have the role of both a NDM and an owner");
    }

    if (match3) {
      retVal.add(
        "A user cannot have the role of both a depositor and an owner"
      );
    }

    return retVal;
  }

  private List<String> validateFieldset5(CreateVault vault) {
    List<String> retVal = new ArrayList<>();

    // Field set 5 should have completed
    // Pure link
    Boolean pureLink = vault.getPureLink();
    if (pureLink == null || pureLink == false) {
      retVal.add("Pure link not accepted");
    }
    return retVal;
  }

  public Date getDefaultReviewDate(int addedYears) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    cal.add(Calendar.YEAR, addedYears);
    Date todayPlusXYears = cal.getTime();
    return todayPlusXYears;
  }

  public Date getDefaultReviewDate() {
    return this.getDefaultReviewDate(3);
  }

  public Date getMaxReviewDate() {
    return this.getDefaultReviewDate(30);
  }
}
