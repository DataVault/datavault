package org.datavaultplatform.common;

public interface PropNames {

  String ARCHIVE_DIGEST = "archiveDigest";
  String ARCHIVE_DIGEST_ALGORITHM = "archiveDigestAlgorithm";
  String ARCHIVE_ID = "archiveId";
  String ARCHIVE_SIZE = "archiveSize";
  String AUDIT = "audit";
  String AUDIT_ID = "auditId";
  String AWS_S3_BUCKET_NAME = "s3.bucketName";
  String AWS_S3_REGION = "s3.region";
  String AWS_ACCESS_KEY = "s3.awsAccessKey";
  String AWS_SECRET_KEY = "s3.awsSecretKey";
  String BAG_ID = "bagId";
  String CHUNK_ID = "id";
  String CHUNK_NUM = "chunkNum";
  String DEPOSIT_CHUNK = "depositChunk";
  String DEPOSIT_ID = "depositId";
  String DEPOSIT_CHUNKS_STORED = "depositChunksStored";
  /**
   * DepositCreationDate: Date Value in Basic ISO Format e.g. '20240131'
   * <p/>TODO - change to '2024-01-31'
   */
  String DEPOSIT_CREATION_DATE = "depositCreationDate";
  String DEPOSIT_METADATA = "depositMetadata";
  String EXTERNAL_METADATA = "externalMetadata";
  String HOST = "host";
  String IV = "iv";
  String LOCATION = "location";
  String NUM_OF_CHUNKS = "numOfChunks";
  String OCC_RETRY_TIME = "occRetryTime";
  String OCC_MAX_RETRIES = "occMaxRetries";
  String OCI_NAME_SPACE = "ociNameSpace";
  String OCI_BUCKET_NAME = "ociBucketName";
  String OPTIONS_DIR = "optionsDir";
  String PASSPHRASE = "passphrase";
  String PASSWORD = "password";
  String PORT = "port";
  String PRIVATE_KEY = "privateKey";
  String PUBLIC_KEY = "publicKey";
  String RETRIEVE_PATH = "retrievePath";
  String RETRIEVE_ID = "retrieveId";
  String ROOT_PATH = "rootPath";
  String STATUS = "status";
  String USER_ID = "userId";
  String VAULT_METADATA = "vaultMetadata";
  String TEMP_DIR = "tempDir";
  String TSM_MAX_RETRIES = "tsmMaxRetries";
  String TSM_RETRY_TIME = "tsmRetryTime";
  String USERNAME = "username";
  String MONITOR_SFTP = "monitorSftp";

  String TSM_REVERSE = "tsmReverse";
  String USER_FS_RETRY_MAX_ATTEMPTS = "userFsRetryMaxAttempts";
  String USER_FS_RETRY_DELAY_MS_1 = "userFsRetryDelayMs1";
  String USER_FS_RETRY_DELAY_MS_2 = "userFsRetryDelayMs2";
  String NON_RESTART_JOB_ID = "nonRestartJobId";
}
