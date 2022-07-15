package org.datavaultplatform.broker.scheduled;

public abstract class ScheduledUtils {

  public static final String SCHEDULE_1_AUDIT_DEPOSIT_NAME = "auditdeposit.schedule";

  public static final String SCHEDULE_2_ENCRYPTION_CHECK_NAME = "encryptioncheck.schedule";

  public static final String SCHEDULE_3_DELETE_NAME = "delete.schedule";

  public static final String SCHEDULE_4_REVIEW_NAME = "review.schedule";

  public static final String SCHEDULE_5_RETENTION_CHECK_NAME = "retentioncheck.schedule";

  public static final String SCHEDULE_1_AUDIT_DEPOSIT = "${" + SCHEDULE_1_AUDIT_DEPOSIT_NAME + "}";

  public static final String SCHEDULE_2_ENCRYPTION_CHECK = "${" + SCHEDULE_2_ENCRYPTION_CHECK_NAME + "}";

  public static final String SCHEDULE_3_DELETE = "${" + SCHEDULE_3_DELETE_NAME + "}";

  public static final String SCHEDULE_4_REVIEW = "${" + SCHEDULE_4_REVIEW_NAME + "}";

  public static final String SCHEDULE_5_RETENTION_CHECK = "${" + SCHEDULE_5_RETENTION_CHECK_NAME + "}";


}
