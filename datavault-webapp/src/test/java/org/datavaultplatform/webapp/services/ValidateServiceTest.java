package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.request.CreateVault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;

class ValidateServiceTest {

    Clock testClock;
    ValidateService validateService;
    LocalDate today;

    @BeforeEach
    void setup() {
        testClock = Clock.fixed(Instant.parse("2024-04-05T15:23:57Z"), ZoneOffset.UTC);
        validateService = new ValidateService(testClock);
        today = LocalDate.now(testClock);
    }

    @Test
    void testGetDefaultReviewDate() {
        Date defaultReviewDate = validateService.getDefaultReviewDate();
        LocalDate defaultReviewLocalDate = convertDateToLocalDate(defaultReviewDate);

        LocalDate todayPlusThreeYears = today.plusYears(3);
        assertThat(defaultReviewLocalDate).isEqualTo(todayPlusThreeYears);

    }

    @Test
    void testGetMaxReviewDate() {
        Date defaultReviewDate = validateService.getMaxReviewDate();
        LocalDate defaultReviewLocalDate = convertDateToLocalDate(defaultReviewDate);

        LocalDate todayPlusThirtyYears = today.plusYears(30);
        assertThat(defaultReviewLocalDate).isEqualTo(todayPlusThirtyYears);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1})
    void testGetMaxReviewDate(int years) {
        Date defaultReviewDate = validateService.getDefaultReviewDate(years);
        LocalDate defaultReviewLocalDate = convertDateToLocalDate(defaultReviewDate);

        LocalDate expectedLocalDate = today.plusYears(years);
        assertThat(defaultReviewLocalDate).isEqualTo(expectedLocalDate);
    }

    private LocalDate convertDateToLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }

    @Nested
    class WithSpyTests {

        ValidateService validateService;

        @BeforeEach
        void setup() {
            validateService = Mockito.spy(new ValidateService(testClock));
        }

        @Test
        void testValidateTopLevel() {
            CreateVault vault = new CreateVault();
            String userID = "test-user-id";
            Mockito.doReturn(Arrays.asList("1", "11")).when(validateService).validateFieldset1(vault);
            Mockito.doReturn(Arrays.asList("2", "22")).when(validateService).validateFieldset2(vault);
            Mockito.doReturn(Arrays.asList("3", "33")).when(validateService).validateFieldset3(vault);
            Mockito.doReturn(Arrays.asList("4", "44")).when(validateService).validateFieldset4(vault, userID);
            Mockito.doReturn(Arrays.asList("5", "55")).when(validateService).validateFieldset5(vault);

            List<String> result = validateService.validate(vault, userID);
            assertThat(result).containsExactly("1", "11", "2", "22", "3", "33", "4", "44", "5", "55");
            InOrder inOrder = inOrder(validateService);
            inOrder.verify(validateService).validate(vault, userID);
            inOrder.verify(validateService).validateFieldset1(vault);
            inOrder.verify(validateService).validateFieldset2(vault);
            inOrder.verify(validateService).validateFieldset3(vault);
            inOrder.verify(validateService).validateFieldset4(vault, userID);
            inOrder.verify(validateService).validateFieldset5(vault);
        }

        @Nested
        class Fieldset1Tests {

            @Test
            void testInvalidFieldSet1() {
                checkFieldSet1(false, Arrays.asList("Affirmation"));
                checkFieldSet1(null, Arrays.asList("Affirmation"));
            }

            @Test
            void testValidFieldSet1() {
                checkFieldSet1(true, Collections.emptyList());
            }

            void checkFieldSet1(Boolean affirmed, List<String> expectedResult) {
                CreateVault vault = new CreateVault();
                vault.setAffirmed(affirmed);
                List<String> result = validateService.validateFieldset1(vault);
                assertThat(result).isEqualTo(expectedResult);
            }
        }

        @Nested
        class Fieldset2Tests {

            @Test
            void testFieldSet2billingType_NA() {
                CreateVault vault = new CreateVault();
                checkFieldset2(vault, Arrays.asList("BillingType missing"));

                vault.setBillingType("bob");
                checkFieldset2(vault, Arrays.asList("BillingType invalid (bob)"));

                vault.setBillingType("NA");
                checkFieldset2(vault, Collections.emptyList());
            }

            @Test
            void testFieldSet2billingType_GrantFunding() {
                CreateVault vault = new CreateVault();

                vault.setBillingType("GRANT_FUNDING");
                checkFieldset2(vault, Arrays.asList("Authoriser missing",
                        "School / Unit missing",
                        "Subunit missing",
                        "Project Title missing",
                        "Review Date missing"));
                vault.setGrantSubunit("grantSubunit");
                checkFieldset2(vault, Arrays.asList("Authoriser missing",
                        "School / Unit missing",
                        "Project Title missing",
                        "Review Date missing"));
                checkFieldset2(vault, Arrays.asList("Authoriser missing",
                        "School / Unit missing",
                        "Project Title missing",
                        "Review Date missing"));
                vault.setProjectTitle("project-title");
                checkFieldset2(vault, Arrays.asList("Authoriser missing",
                        "School / Unit missing",
                        "Review Date missing"));
                vault.setReviewDate(new Date());
                checkFieldset2(vault, Arrays.asList("Authoriser missing",
                        "School / Unit missing"));
                vault.setGrantAuthoriser("grant-authoriser");
                checkFieldset2(vault, Arrays.asList("School / Unit missing"));
                vault.setGrantSchoolOrUnit("grant-school");
                checkFieldset2(vault, Collections.EMPTY_LIST);

            }

            @Test
            void testFieldSet2billingType_BudgetCode() {
                CreateVault vault = new CreateVault();

                vault.setBillingType("BUDGET_CODE");
                checkFieldset2(vault, Arrays.asList("Authoriser missing", "School / Unit missing", "Subunit missing"));
                vault.setBudgetAuthoriser("budget-authoriser");
                checkFieldset2(vault, Arrays.asList("School / Unit missing", "Subunit missing"));
                vault.setBudgetSchoolOrUnit("budget-school");
                checkFieldset2(vault, Arrays.asList("Subunit missing"));
                vault.setBudgetSubunit("budget-subunit");
                checkFieldset2(vault, Collections.EMPTY_LIST);
            }

            @Test
            void testFieldSet2billingType_Slice() {
                CreateVault vault = new CreateVault();

                vault.setBillingType("SLICE");
                checkFieldset2(vault, Collections.EMPTY_LIST);
            }

            @Test
            void testFieldSet2billingType_Feewaiver() {
                CreateVault vault = new CreateVault();

                vault.setBillingType("FEEWAIVER");
                checkFieldset2(vault, Collections.EMPTY_LIST);
            }

            @Test
            void testFieldSet2billingType_WillPay() {
                CreateVault vault = new CreateVault();

                vault.setBillingType("WILL_PAY");
                checkFieldset2(vault, Arrays.asList("Payment authoriser is missing."));
                vault.setBudgetAuthoriser("budget-authorizer");
                checkFieldset2(vault, Collections.EMPTY_LIST);
            }

            @Test
            void testFieldSet2billingType_BuyNewSlice() {
                CreateVault vault = new CreateVault();

                vault.setBillingType("BUY_NEW_SLICE");
                checkFieldset2(vault, Collections.EMPTY_LIST);
            }

            @Test
            void testFieldSet2billingType_FundingNoOrDoNotKnow() {
                CreateVault vault = new CreateVault();

                vault.setBillingType("FUNDING_NO_OR_DO_NOT_KNOW");
                checkFieldset2(vault, Collections.EMPTY_LIST);
            }

            void checkFieldset2(CreateVault vault, List<String> expectedResult) {
                List<String> result = validateService.validateFieldset2(vault);
                assertThat(result).isEqualTo(expectedResult);
            }
        }

        @Nested
        class Fieldset3Tests {
            CreateVault vault = new CreateVault();

            @BeforeEach
            void setup() {
                vault = new CreateVault();
                vault.setName("name");
                vault.setDescription("description");
                vault.setPolicyInfo("policyInfo");
                vault.setGroupID("groupId");
                vault.setReviewDate(new Date(ZonedDateTime.now().plusYears(4).toInstant().toEpochMilli()));

            }

            @Test
            void testFieldset3() {
                checkFieldset3(vault, Collections.EMPTY_LIST);
            }

            @ParameterizedTest
            @ValueSource(strings = "")
            @NullSource
            void testFieldset3_Name(String name) {
                vault.setName(name);
                checkFieldset3(vault, Arrays.asList("Name missing"));
            }

            @ParameterizedTest
            @ValueSource(strings = "")
            @NullSource
            void testFieldset3_Description(String description) {
                vault.setDescription(description);
                checkFieldset3(vault, Arrays.asList("Description missing"));
            }

            @ParameterizedTest
            @ValueSource(strings = "")
            @NullSource
            void testFieldset3_PolicyInfo(String policyInfo) {
                vault.setPolicyInfo(policyInfo);
                checkFieldset3(vault, Arrays.asList("Retention policy missing"));
            }

            @ParameterizedTest
            @ValueSource(strings = "")
            @NullSource
            void testFieldset3_GroupId(String groupId) {
                vault.setGroupID(groupId);
                checkFieldset3(vault, Arrays.asList("School missing"));
            }

            @Test
            void testFieldset3_ReviewDateMissing() {
                vault.setReviewDate(null);
                checkFieldset3(vault, Arrays.asList("Review Date missing"));
            }

            @Test
            void testFieldset3_ReviewDateTooSoon() {
                vault.setReviewDate(new Date(ZonedDateTime.now().plusYears(1).toInstant().toEpochMilli()));
                checkFieldset3(vault, Arrays.asList("Review Date for selected policy is required to be at least 3 years"));
            }

            @Test
            void testFieldset3_ReviewDateTooLate() {
                vault.setReviewDate(new Date(ZonedDateTime.now().plusYears(31).toInstant().toEpochMilli()));
                checkFieldset3(vault, Arrays.asList("Review Date for selected policy is required to be less than 30 years in the future"));
            }

            void checkFieldset3(CreateVault vault, List<String> expectedResult) {
                List<String> result = validateService.validateFieldset3(vault);
                assertThat(result).isEqualTo(expectedResult);
            }
        }

        @Nested
        class FieldSet4Tests {

            @ParameterizedTest
            @CsvSource(value = {
                    "null,   user1",
                    "owner1, null",
                    "owner1, user1"}, nullValues = "null")
            void testUserCannotBeBothDepositorAndNdm(String owner, String userId) {
                CreateVault vault = new CreateVault();
                vault.setVaultOwner(owner);
                vault.setNominatedDataManagers(Arrays.asList("a", "b", "c"));
                vault.setDepositors(Arrays.asList("d", "b", "f"));
                checkFieldset4(vault, userId, Arrays.asList("A user cannot have the role of both a NDM and a depositor"));
            }

            @ParameterizedTest
            @CsvSource(value = {
                    "null,   owner1",
                    "owner1, null",
                    "owner1, user1"}, nullValues = "null")
            void testUserCannotBeBothOwnerAndNdm(String owner, String userId) {
                CreateVault vault = new CreateVault();
                vault.setVaultOwner(owner);
                vault.setNominatedDataManagers(Arrays.asList("a", "owner1", "c"));
                checkFieldset4(vault, userId, Arrays.asList("A user cannot have the role of both a NDM and an owner"));
            }

            @ParameterizedTest
            @CsvSource(value = {
                    "null,   owner1",
                    "owner1, null",
                    "owner1, user1"}, nullValues = "null")
            void testUserCannotBeBothOwnerAndDepositor(String owner, String userId) {
                CreateVault vault = new CreateVault();
                vault.setVaultOwner(owner);
                vault.setDepositors(Arrays.asList("a", "owner1", "c"));
                checkFieldset4(vault, userId, Arrays.asList("A user cannot have the role of both a depositor and an owner"));
            }

            @ParameterizedTest
            @CsvSource(value = {
                    "null,   owner1",
                    "owner1, null",
                    "owner1, user1"}, nullValues = "null")
            void testValid(String owner, String userId) {
                CreateVault vault = new CreateVault();
                vault.setVaultOwner(owner);
                vault.setNominatedDataManagers(Arrays.asList("a", "b", "c"));
                vault.setDepositors(Arrays.asList("d", "e", "f"));
                checkFieldset4(vault, userId, Collections.emptyList());
            }

            void checkFieldset4(CreateVault vault, String userId, List<String> expectedResult) {
                List<String> result = validateService.validateFieldset4(vault, userId);
                assertThat(result).isEqualTo(expectedResult);
            }
        }

        @Nested
        class FieldSet5Tests {

            @Test
            void testValid() {
                CreateVault vault = new CreateVault();
                vault.setPureLink(true);
                checkFieldset5(vault, Collections.emptyList());
            }

            @ParameterizedTest
            @ValueSource(booleans = false)
            @NullSource
            void testInValid(Boolean pureLink) {
                CreateVault vault = new CreateVault();
                vault.setPureLink(pureLink);
                checkFieldset5(vault, Arrays.asList("Pure link not accepted"));
            }

            void checkFieldset5(CreateVault vault, List<String> expectedResult) {
                List<String> result = validateService.validateFieldset5(vault);
                assertThat(result).isEqualTo(expectedResult);
            }

        }
    }
}