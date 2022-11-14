package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import org.datavaultplatform.broker.controllers.ReviewsController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ReviewsControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  ReviewsController controller;

  @Test
  void testGetDepositReview() {

    when(controller.getDepositReview(USER_ID_1, "deposit-review-123")).thenReturn(
        AuthTestData.DEPOSIT_REVIEW_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaultreviews/depositreviews/{depositReviewId}", "deposit-review-123"),
        AuthTestData.DEPOSIT_REVIEW_1);

    verify(controller).getDepositReview(USER_ID_1, "deposit-review-123");
  }

  @Test
  void testGetDepositReviews() {

    when(controller.getDepositReviews(USER_ID_1, "vault-review-1234")).thenReturn(
        Arrays.asList(AuthTestData.DEPOSIT_REVIEW_1, AuthTestData.DEPOSIT_REVIEW_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaultreviews/{vaultReviewId}/depositreviews", "vault-review-1234"),
        Arrays.asList(AuthTestData.DEPOSIT_REVIEW_1, AuthTestData.DEPOSIT_REVIEW_2));

    verify(controller).getDepositReviews(USER_ID_1, "vault-review-1234");
  }

  @Test
  void testGetVaultReview() {

    when(controller.getVaultReview(USER_ID_1, "vault-review-12345")).thenReturn(
        AuthTestData.VAULT_REVIEW_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/vaultreviews/{vaultReviewId}", "vault-review-12345"),
        AuthTestData.VAULT_REVIEW_1);

    verify(controller).getVaultReview(USER_ID_1, "vault-review-12345");
  }

  @Test
  void testGetVaultReviews() throws Exception {
    when(controller.getVaultReviews(USER_ID_1, "vault-id-123")).thenReturn(
        Arrays.asList(AuthTestData.REVIEW_INFO_1, AuthTestData.REVIEW_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/{vaultid}/vaultreviews", "vault-id-123"),
        Arrays.asList(AuthTestData.REVIEW_INFO_1, AuthTestData.REVIEW_INFO_2));

    verify(controller).getVaultReviews(USER_ID_1, "vault-id-123");
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }

}
