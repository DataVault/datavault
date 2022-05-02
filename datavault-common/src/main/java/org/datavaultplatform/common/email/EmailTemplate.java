package org.datavaultplatform.common.email;

/*
 * @see org.datavaultplatform.broker.email.VelocityConfigTest
 */
public abstract class EmailTemplate {

  public static final String AUDIT_CHUNK_ERROR = "audit-chunk-error.vm";
  public static final String GROUP_ADMIN_DEPOSIT_COMPLETE = "group-admin-deposit-complete.vm";
  public static final String GROUP_ADMIN_DEPOSIT_ERROR = "group-admin-deposit-error.vm";
  public static final String GROUP_ADMIN_DEPOSIT_START = "group-admin-deposit-start.vm";
  public static final String GROUP_ADMIN_MISSING_IV = "group-admin-missing-iv.vm";
  public static final String GROUP_ADMIN_RETRIEVE_COMPLETE = "group-admin-retrieve-complete.vm";
  public static final String GROUP_ADMIN_RETRIEVE_START = "group-admin-retrieve-start.vm";
  public static final String GROUP_ADMIN_VAULT_CREATE = "group-admin-vault-create.vm";
  public static final String NEW_ROLE_ASSIGNMENT = "new-role-assignment.vm";
  public static final String REVIEW_DUE_DATA_MANAGER = "review-due-data-manager.vm";
  public static final String REVIEW_DUE_OWNER = "review-due-owner.vm";
  public static final String REVIEW_DUE_SUPPORT = "review-due-support.vm";
  public static final String TRANSFER_VAULT_OWNERSHIP = "transfer-vault-ownership.vm";
  public static final String UPDATE_ROLE_ASSIGNMENT = "update-role-assignment.vm";
  public static final String USER_DEPOSIT_COMPLETE = "user-deposit-complete.vm";
  public static final String USER_DEPOSIT_ERROR = "user-deposit-error.vm";
  public static final String USER_DEPOSIT_START = "user-deposit-start.vm";
  public static final String USER_RETRIEVE_COMPLETE = "user-retrieve-complete.vm";
  public static final String USER_RETRIEVE_START = "user-retrieve-start.vm";
  public static final String USER_VAULT_CREATE = "user-vault-create.vm";
}
