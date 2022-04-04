-- MySQL dump 10.14  Distrib 5.5.68-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: datavault
-- ------------------------------------------------------
-- Server version	5.5.68-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ArchiveStores`
--

DROP TABLE IF EXISTS `ArchiveStores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ArchiveStores` (
  `id` varchar(36) NOT NULL,
  `label` text,
  `properties` longblob,
  `retrieveEnabled` bit(1) NOT NULL,
  `storageClass` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Archives`
--

DROP TABLE IF EXISTS `Archives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Archives` (
  `id` varchar(36) NOT NULL,
  `archiveId` text,
  `creationTime` datetime DEFAULT NULL,
  `archiveStore_id` varchar(36) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_48rbysqgkw69ges9wr7ep51nl` (`archiveStore_id`),
  KEY `FK_jwlxx65xk33xxjkacb0o34uhs` (`deposit_id`),
  CONSTRAINT `FK_48rbysqgkw69ges9wr7ep51nl` FOREIGN KEY (`archiveStore_id`) REFERENCES `ArchiveStores` (`id`),
  CONSTRAINT `FK_jwlxx65xk33xxjkacb0o34uhs` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AuditChunkStatus`
--

DROP TABLE IF EXISTS `AuditChunkStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AuditChunkStatus` (
  `id` varchar(36) NOT NULL,
  `note` text,
  `status` text,
  `timestamp` datetime DEFAULT NULL,
  `completeTime` datetime DEFAULT NULL,
  `archiveId` text,
  `location` varchar(36) DEFAULT NULL,
  `audit_id` varchar(36) DEFAULT NULL,
  `depositChunk_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_auditedChunks_audit_id` (`audit_id`),
  KEY `fk_auditedChunks_depositChunk_id` (`depositChunk_id`),
  CONSTRAINT `fk_auditedChunks_audit_id` FOREIGN KEY (`audit_id`) REFERENCES `Audits` (`id`),
  CONSTRAINT `fk_auditedChunks_depositChunk_id` FOREIGN KEY (`depositChunk_id`) REFERENCES `DepositChunks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Audits`
--

DROP TABLE IF EXISTS `Audits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Audits` (
  `id` varchar(36) NOT NULL,
  `note` text,
  `status` text,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BillingInfo`
--

DROP TABLE IF EXISTS `BillingInfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BillingInfo` (
  `id` varchar(36) NOT NULL,
  `amountBilled` decimal(15,2) DEFAULT '0.00',
  `amountToBeBilled` decimal(15,2) DEFAULT '0.00',
  `budgetCode` bit(1) DEFAULT NULL,
  `contactName` text,
  `school` text,
  `specialComments` text,
  `subUnit` text,
  `version` bigint(20) NOT NULL,
  `vaultID` varchar(36) NOT NULL,
  `billingType` text NOT NULL,
  `sliceID` text,
  `projectTitle` text,
  PRIMARY KEY (`id`),
  KEY `FK_eb8b8ksy2fq52e8lxsr3gdku6` (`vaultID`),
  CONSTRAINT `FK_eb8b8ksy2fq52e8lxsr3gdku6` FOREIGN KEY (`vaultID`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Clients`
--

DROP TABLE IF EXISTS `Clients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Clients` (
  `id` varchar(180) NOT NULL,
  `apiKey` text,
  `ipAddress` text,
  `name` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DataCreators`
--

DROP TABLE IF EXISTS `DataCreators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DataCreators` (
  `id` varchar(36) NOT NULL,
  `name` text NOT NULL,
  `version` bigint(20) NOT NULL,
  `vault_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DataManagers`
--

DROP TABLE IF EXISTS `DataManagers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DataManagers` (
  `id` varchar(36) NOT NULL,
  `uun` text,
  `vault_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_fhxyt33vbww6lxhrnjglxxxah` (`vault_id`),
  CONSTRAINT `FK_fhxyt33vbww6lxhrnjglxxxah` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Datasets`
--

DROP TABLE IF EXISTS `Datasets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Datasets` (
  `id` varchar(180) NOT NULL,
  `name` text NOT NULL,
  `crisId` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DepositChunks`
--

DROP TABLE IF EXISTS `DepositChunks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DepositChunks` (
  `id` varchar(36) NOT NULL,
  `archiveDigest` text,
  `archiveDigestAlgorithm` text,
  `chunkNum` int(11) DEFAULT NULL,
  `ecnArchiveDigest` text,
  `encIV` blob,
  `deposit_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_lugcfbjhi7bnmp27hy5x2jfk4` (`deposit_id`),
  CONSTRAINT `FK_lugcfbjhi7bnmp27hy5x2jfk4` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DepositPaths`
--

DROP TABLE IF EXISTS `DepositPaths`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DepositPaths` (
  `id` varchar(36) NOT NULL,
  `filePath` text,
  `pathType` int(11) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_s882kpueeque5q76vc4a8mym3` (`deposit_id`),
  CONSTRAINT `FK_s882kpueeque5q76vc4a8mym3` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DepositReviews`
--

DROP TABLE IF EXISTS `DepositReviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DepositReviews` (
  `id` varchar(36) NOT NULL,
  `actionedDate` datetime DEFAULT NULL,
  `comment` text,
  `creationTime` datetime(3) NOT NULL,
  `toBeDeleted` bit(1) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  `vaultReview_id` varchar(36) DEFAULT NULL,
  `deleteStatus` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_b7hilkosopp7ey1nlt5wsktlg` (`deposit_id`),
  KEY `FK_ce59sny0jfo3yc4hcp3kfy4r5` (`vaultReview_id`),
  CONSTRAINT `FK_b7hilkosopp7ey1nlt5wsktlg` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`),
  CONSTRAINT `FK_ce59sny0jfo3yc4hcp3kfy4r5` FOREIGN KEY (`vaultReview_id`) REFERENCES `VaultReviews` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Deposits`
--

DROP TABLE IF EXISTS `Deposits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Deposits` (
  `id` varchar(36) NOT NULL,
  `archiveDigest` text,
  `archiveDigestAlgorithm` text,
  `archiveSize` text,
  `bagId` text,
  `creationTime` datetime DEFAULT NULL,
  `depositSize` bigint(20) NOT NULL,
  `description` text,
  `encArchiveDigest` text,
  `encIV` blob,
  `fileOrigin` text,
  `filePath` text,
  `hasPersonalData` bit(1) NOT NULL,
  `name` text NOT NULL,
  `numOfChunks` int(11) DEFAULT '0',
  `personalDataStatement` text,
  `shortFilePath` text,
  `status` int(11) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `vault_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_52ut21706aajhepgtp1e21pmt` (`user_id`),
  KEY `FK_6gdh2hj6uya6lwir6pdq7lsy5` (`vault_id`),
  CONSTRAINT `FK_52ut21706aajhepgtp1e21pmt` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_6gdh2hj6uya6lwir6pdq7lsy5` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Events`
--

DROP TABLE IF EXISTS `Events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Events` (
  `eventType` varchar(31) NOT NULL,
  `id` varchar(36) NOT NULL,
  `agent` varchar(255) DEFAULT NULL,
  `agentType` varchar(255) DEFAULT NULL,
  `eventClass` varchar(255) DEFAULT NULL,
  `message` longtext,
  `remoteAddress` varchar(255) DEFAULT NULL,
  `retrieveId` varchar(255) DEFAULT NULL,
  `sequence` int(11) NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `archiveIds` tinyblob,
  `archiveSize` bigint(20) DEFAULT NULL,
  `chunksDigest` longblob,
  `digestAlgorithm` varchar(255) DEFAULT NULL,
  `digest` varchar(255) DEFAULT NULL,
  `aesMode` varchar(255) DEFAULT NULL,
  `chunkIVs` longblob,
  `encChunkDigests` longblob,
  `encTarDigest` varchar(255) DEFAULT NULL,
  `tarIV` tinyblob,
  `bytes` bigint(20) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  `job_id` varchar(36) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `vault_id` varchar(36) DEFAULT NULL,
  `audit_id` varchar(36) DEFAULT NULL,
  `archive_id` varchar(36) DEFAULT NULL,
  `location` varchar(36) DEFAULT NULL,
  `chunk_id` varchar(36) DEFAULT NULL,
  `assignee_id` varchar(36) DEFAULT NULL,
  `role_id` bigint(20) DEFAULT NULL,
  `school_id` varchar(180) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_v9fox13af61yovcqo4bv09at` (`deposit_id`),
  KEY `FK_of5s89qsikm2ly9r58qjl0ggp` (`job_id`),
  KEY `FK_95eow86t5qtv0skulhp5oor7d` (`user_id`),
  KEY `FK_tff26y6db47efd6da7x2d2v26` (`vault_id`),
  KEY `fk_audit_id` (`audit_id`),
  KEY `fk_archive_id` (`archive_id`),
  KEY `fk_chunk_id` (`chunk_id`),
  KEY `FK_hisyjr4nglx5lvhn4mirqnypp` (`assignee_id`),
  KEY `FK_e0c78p3nf80x77tt6dh9m3sw8` (`role_id`),
  KEY `FK_o32xd6wvetciiqew3555rm7ml` (`school_id`),
  CONSTRAINT `FK_95eow86t5qtv0skulhp5oor7d` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `fk_archive_id` FOREIGN KEY (`archive_id`) REFERENCES `Archives` (`id`),
  CONSTRAINT `fk_audit_id` FOREIGN KEY (`audit_id`) REFERENCES `Audits` (`id`),
  CONSTRAINT `fk_chunk_id` FOREIGN KEY (`chunk_id`) REFERENCES `DepositChunks` (`id`),
  CONSTRAINT `FK_e0c78p3nf80x77tt6dh9m3sw8` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`),
  CONSTRAINT `FK_hisyjr4nglx5lvhn4mirqnypp` FOREIGN KEY (`assignee_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_o32xd6wvetciiqew3555rm7ml` FOREIGN KEY (`school_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK_of5s89qsikm2ly9r58qjl0ggp` FOREIGN KEY (`job_id`) REFERENCES `Jobs` (`id`),
  CONSTRAINT `FK_tff26y6db47efd6da7x2d2v26` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`),
  CONSTRAINT `FK_v9fox13af61yovcqo4bv09at` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FileStores`
--

DROP TABLE IF EXISTS `FileStores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FileStores` (
  `id` varchar(36) NOT NULL,
  `label` text,
  `properties` longblob,
  `storageClass` text,
  `user_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_33t06dc48905kadiuikwqeaiw` (`user_id`),
  CONSTRAINT `FK_33t06dc48905kadiuikwqeaiw` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupOwners`
--

DROP TABLE IF EXISTS `GroupOwners`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupOwners` (
  `group_id` varchar(180) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  KEY `FK_iev9q48rimcl3vhau7xoyemsw` (`user_id`),
  KEY `FK_bd2529ss121bedic3n5rcggpr` (`group_id`),
  CONSTRAINT `FK_bd2529ss121bedic3n5rcggpr` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK_iev9q48rimcl3vhau7xoyemsw` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Groups`
--

DROP TABLE IF EXISTS `Groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Groups` (
  `id` varchar(180) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Jobs`
--

DROP TABLE IF EXISTS `Jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Jobs` (
  `id` varchar(36) NOT NULL,
  `error` bit(1) NOT NULL,
  `errorMessage` text,
  `progress` bigint(20) NOT NULL,
  `progressMax` bigint(20) NOT NULL,
  `progressMessage` text,
  `state` int(11) DEFAULT NULL,
  `states` tinyblob,
  `taskClass` text,
  `timestamp` datetime DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_on2dpccq3xl46u7wbdgphwy7k` (`deposit_id`),
  CONSTRAINT `FK_on2dpccq3xl46u7wbdgphwy7k` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PendingDataCreators`
--

DROP TABLE IF EXISTS `PendingDataCreators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PendingDataCreators` (
  `id` varchar(36) NOT NULL,
  `name` text NOT NULL,
  `version` bigint(20) NOT NULL,
  `pendingVault_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1oncea34ucfe9u54oi2mmj3qw` (`pendingVault_id`),
  CONSTRAINT `FK1oncea34ucfe9u54oi2mmj3qw` FOREIGN KEY (`pendingVault_id`) REFERENCES `PendingVaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PendingVaults`
--

DROP TABLE IF EXISTS `PendingVaults`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PendingVaults` (
  `id` varchar(36) NOT NULL,
  `affirmed` bit(1) NOT NULL,
  `billingType` text,
  `creationTime` datetime DEFAULT NULL,
  `description` text,
  `estimate` text,
  `grantEndDate` date DEFAULT NULL,
  `name` text,
  `notes` text,
  `reviewDate` date DEFAULT NULL,
  `sliceID` text,
  `subunit` text,
  `authoriser` text,
  `schoolOrUnit` text,
  `projectTitle` text,
  `version` bigint(20) NOT NULL,
  `group_id` varchar(180) DEFAULT NULL,
  `retentionPolicy_id` int(11) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `contact` text,
  `pureLink` bit(1) NOT NULL DEFAULT b'0',
  `confirmed` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `FK1oqxrihj71uwiynk4m8u0n0vj` (`group_id`),
  KEY `FKtqmx003465315spj99suwf1ta` (`retentionPolicy_id`),
  KEY `FKjj6cuk6m88qn7th5df64ui3cw` (`user_id`),
  CONSTRAINT `FK1oqxrihj71uwiynk4m8u0n0vj` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FKjj6cuk6m88qn7th5df64ui3cw` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FKtqmx003465315spj99suwf1ta` FOREIGN KEY (`retentionPolicy_id`) REFERENCES `RetentionPolicies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Permissions`
--

DROP TABLE IF EXISTS `Permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Permissions` (
  `id` varchar(36) NOT NULL,
  `label` text NOT NULL,
  `permission` text NOT NULL,
  `type` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RetentionPolicies`
--

DROP TABLE IF EXISTS `RetentionPolicies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RetentionPolicies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dataGuidanceReviewed` date DEFAULT NULL,
  `description` text,
  `endDate` date DEFAULT NULL,
  `engine` text NOT NULL,
  `inEffectDate` date DEFAULT NULL,
  `minDataRetentionPeriod` text,
  `name` text NOT NULL,
  `sort` int(11) NOT NULL,
  `url` text,
  `extendUponRetrieval` tinyint(1) DEFAULT NULL,
  `minRetentionPeriod` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Retrieves`
--

DROP TABLE IF EXISTS `Retrieves`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Retrieves` (
  `id` varchar(36) NOT NULL,
  `hasExternalRecipients` bit(1) NOT NULL,
  `note` text,
  `retrievePath` text,
  `status` text,
  `timestamp` datetime DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_snimg6slb5wgj6fffqt7d1ewp` (`deposit_id`),
  KEY `FK_bprbu40mo7lofejxligkhotlc` (`user_id`),
  CONSTRAINT `FK_bprbu40mo7lofejxligkhotlc` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_snimg6slb5wgj6fffqt7d1ewp` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Role_assignments`
--

DROP TABLE IF EXISTS `Role_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Role_assignments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `school_id` varchar(180) DEFAULT NULL,
  `user_id` varchar(36) NOT NULL,
  `vault_id` varchar(36) DEFAULT NULL,
  `pending_vault_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kqx1luv7kal490959aop4pj75` (`role_id`,`user_id`,`school_id`),
  UNIQUE KEY `UK_myhycfy43e3socqtp24jx1x71` (`role_id`,`user_id`,`vault_id`),
  UNIQUE KEY `UK_Pending_Vault_ID` (`role_id`,`user_id`,`pending_vault_id`),
  KEY `FK_mtrvvfub5q70r6jkcw5gggg0k` (`school_id`),
  KEY `FK_2j93l0yx740l9r9hjeinwia1f` (`user_id`),
  KEY `FK_by9fc9iswmf9l5q7c243gdprl` (`vault_id`),
  KEY `FK_Pending_Vault_ID` (`pending_vault_id`),
  CONSTRAINT `FK_2j93l0yx740l9r9hjeinwia1f` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_by9fc9iswmf9l5q7c243gdprl` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`),
  CONSTRAINT `FK_mtrvvfub5q70r6jkcw5gggg0k` FOREIGN KEY (`school_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK_Pending_Vault_ID` FOREIGN KEY (`pending_vault_id`) REFERENCES `PendingVaults` (`id`),
  CONSTRAINT `FK_spyctngrr3ou9iyr5ld3h28fs` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2087 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Role_permissions`
--

DROP TABLE IF EXISTS `Role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Role_permissions` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` varchar(36) NOT NULL,
  KEY `FK_ack8r2th4oy2wqlf3ljutfg6s` (`permission_id`),
  KEY `FK_nrwjc3i7g5d4udpcpe6llcuc5` (`role_id`),
  CONSTRAINT `FK_ack8r2th4oy2wqlf3ljutfg6s` FOREIGN KEY (`permission_id`) REFERENCES `Permissions` (`id`),
  CONSTRAINT `FK_nrwjc3i7g5d4udpcpe6llcuc5` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Roles`
--

DROP TABLE IF EXISTS `Roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignedUserCount` int(11) NOT NULL,
  `description` text,
  `name` text NOT NULL,
  `role_type` text NOT NULL,
  `role_status` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Users` (
  `id` varchar(36) NOT NULL,
  `admin` bit(1) NOT NULL DEFAULT b'0',
  `email` text,
  `firstname` text NOT NULL,
  `lastname` text NOT NULL,
  `password` text,
  `properties` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `VaultReviews`
--

DROP TABLE IF EXISTS `VaultReviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `VaultReviews` (
  `id` varchar(36) NOT NULL,
  `actionedDate` datetime DEFAULT NULL,
  `comment` text,
  `creationTime` datetime(3) NOT NULL,
  `newReviewDate` date DEFAULT NULL,
  `vault_id` varchar(36) DEFAULT NULL,
  `oldReviewDate` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_7mnsjibebjylwhoui5othfnus` (`vault_id`),
  CONSTRAINT `FK_7mnsjibebjylwhoui5othfnus` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Vaults`
--

DROP TABLE IF EXISTS `Vaults`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Vaults` (
  `id` varchar(36) NOT NULL,
  `creationTime` datetime DEFAULT NULL,
  `description` text,
  `grantEndDate` date DEFAULT NULL,
  `name` text NOT NULL,
  `retentionPolicyExpiry` datetime DEFAULT NULL,
  `retentionPolicyLastChecked` datetime DEFAULT NULL,
  `retentionPolicyStatus` int(11) NOT NULL,
  `reviewDate` date NOT NULL,
  `vaultSize` bigint(20) NOT NULL,
  `version` bigint(20) NOT NULL,
  `dataset_id` varchar(180) DEFAULT NULL,
  `group_id` varchar(180) DEFAULT NULL,
  `retentionPolicy_id` int(11) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `projectId` varchar(180) DEFAULT NULL,
  `snapshot` longtext,
  `affirmed` bit(1) NOT NULL DEFAULT b'0',
  `estimate` text,
  `notes` text,
  `pureLink` bit(1) NOT NULL DEFAULT b'0',
  `contact` text,
  PRIMARY KEY (`id`),
  KEY `FK_s57eqq03teo6ii3oohwkeejtq` (`dataset_id`),
  KEY `FK_pwp5m1fuxas7twj2kcy3c7uvg` (`group_id`),
  KEY `FK_2hfbxakun43pw5g37831dfa35` (`retentionPolicy_id`),
  KEY `FK_qt1tqtq2358vqlvei1488qci8` (`user_id`),
  CONSTRAINT `FK_2hfbxakun43pw5g37831dfa35` FOREIGN KEY (`retentionPolicy_id`) REFERENCES `RetentionPolicies` (`id`),
  CONSTRAINT `FK_pwp5m1fuxas7twj2kcy3c7uvg` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK_qt1tqtq2358vqlvei1488qci8` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_s57eqq03teo6ii3oohwkeejtq` FOREIGN KEY (`dataset_id`) REFERENCES `Datasets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-03-30 16:28:25
