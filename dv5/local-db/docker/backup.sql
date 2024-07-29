-- MySQL dump 10.13  Distrib 5.7.38, for Linux (x86_64)
--
-- Host: localhost    Database: test
-- ------------------------------------------------------
-- Server version	5.7.38

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ArchiveStores`
--

LOCK TABLES `ArchiveStores` WRITE;
/*!40000 ALTER TABLE `ArchiveStores` DISABLE KEYS */;
INSERT INTO `ArchiveStores` VALUES ('033184c7-9739-437e-ad6d-4137cce3275b','Default archive store (TSM)',0xACED0005737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C77080000001000000001740008726F6F74506174687400162F746D702F646174617661756C742F6172636869766578,0x01,'org.datavaultplatform.common.storage.impl.TivoliStorageManager'),('2f48bb08-846f-4480-bd59-f9b131fe6586','Cloud archive store',0xACED0005737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C77080000001000000001740008726F6F74506174687400162F746D702F646174617661756C742F6172636869766578,0x00,'org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic'),('e27f7cba-2785-4a95-9110-7af5a5f2b54a','LocalFileSystem',0xACED0005737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C77080000001000000001740008726F6F745061746874000D2F746D702F61732F6C6F63616C78,0x01,'org.datavaultplatform.common.storage.impl.LocalFileSystem');
/*!40000 ALTER TABLE `ArchiveStores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Archives`
--

DROP TABLE IF EXISTS `Archives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Archives` (
  `id` varchar(36) NOT NULL,
  `archiveId` text,
  `creationTime` datetime(6) DEFAULT NULL,
  `archiveStore_id` varchar(36) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr9pxy8mt0ywinv810al60hxfq` (`archiveStore_id`),
  KEY `FK2ghsu2t3fo7i6mtift5kwj17v` (`deposit_id`),
  CONSTRAINT `FK2ghsu2t3fo7i6mtift5kwj17v` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`),
  CONSTRAINT `FKr9pxy8mt0ywinv810al60hxfq` FOREIGN KEY (`archiveStore_id`) REFERENCES `ArchiveStores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Archives`
--

LOCK TABLES `Archives` WRITE;
/*!40000 ALTER TABLE `Archives` DISABLE KEYS */;
/*!40000 ALTER TABLE `Archives` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AuditChunkStatus`
--

DROP TABLE IF EXISTS `AuditChunkStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AuditChunkStatus` (
  `id` varchar(36) NOT NULL,
  `archiveId` text,
  `completeTime` datetime(6) DEFAULT NULL,
  `location` text,
  `note` text,
  `status` text,
  `timestamp` datetime(6) DEFAULT NULL,
  `audit_id` varchar(36) DEFAULT NULL,
  `depositChunk_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKed9j1e85mb2pq2plvvd4wa1f9` (`audit_id`),
  KEY `FK9l48qtim467ttq5s12f3ac5jl` (`depositChunk_id`),
  CONSTRAINT `FK9l48qtim467ttq5s12f3ac5jl` FOREIGN KEY (`depositChunk_id`) REFERENCES `DepositChunks` (`id`),
  CONSTRAINT `FKed9j1e85mb2pq2plvvd4wa1f9` FOREIGN KEY (`audit_id`) REFERENCES `Audits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AuditChunkStatus`
--

LOCK TABLES `AuditChunkStatus` WRITE;
/*!40000 ALTER TABLE `AuditChunkStatus` DISABLE KEYS */;
/*!40000 ALTER TABLE `AuditChunkStatus` ENABLE KEYS */;
UNLOCK TABLES;

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
  `timestamp` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Audits`
--

LOCK TABLES `Audits` WRITE;
/*!40000 ALTER TABLE `Audits` DISABLE KEYS */;
/*!40000 ALTER TABLE `Audits` ENABLE KEYS */;
UNLOCK TABLES;

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
  `billingType` text NOT NULL,
  `budgetCode` bit(1) DEFAULT NULL,
  `contactName` text,
  `projectTitle` text,
  `school` text,
  `sliceID` text,
  `specialComments` text,
  `subUnit` text,
  `version` bigint(20) NOT NULL,
  `vaultID` varchar(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr0tkmejeiiaen4hp401ag2nl9` (`vaultID`),
  CONSTRAINT `FKr0tkmejeiiaen4hp401ag2nl9` FOREIGN KEY (`vaultID`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `BillingInfo`
--

LOCK TABLES `BillingInfo` WRITE;
/*!40000 ALTER TABLE `BillingInfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `BillingInfo` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Clients`
--

LOCK TABLES `Clients` WRITE;
/*!40000 ALTER TABLE `Clients` DISABLE KEYS */;
INSERT INTO `Clients` VALUES ('datavault-webapp','datavault-webapp','127.0.0.1','Datavault Webapp'),('datavault-webappIPV6','datavault-webapp-ipv6','0:0:0:0:0:0:0:1','Datavault Webapp IPV6');
/*!40000 ALTER TABLE `Clients` ENABLE KEYS */;
UNLOCK TABLES;

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
  PRIMARY KEY (`id`),
  KEY `FK8mevnnr770r4r6h1ivu6i4oag` (`vault_id`),
  CONSTRAINT `FK8mevnnr770r4r6h1ivu6i4oag` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DataCreators`
--

LOCK TABLES `DataCreators` WRITE;
/*!40000 ALTER TABLE `DataCreators` DISABLE KEYS */;
/*!40000 ALTER TABLE `DataCreators` ENABLE KEYS */;
UNLOCK TABLES;

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
  KEY `FKocptfa3sjt5lfj0oxplqadguj` (`vault_id`),
  CONSTRAINT `FKocptfa3sjt5lfj0oxplqadguj` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DataManagers`
--

LOCK TABLES `DataManagers` WRITE;
/*!40000 ALTER TABLE `DataManagers` DISABLE KEYS */;
/*!40000 ALTER TABLE `DataManagers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Datasets`
--

DROP TABLE IF EXISTS `Datasets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Datasets` (
  `id` varchar(180) NOT NULL,
  `crisId` text NOT NULL,
  `name` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Datasets`
--

LOCK TABLES `Datasets` WRITE;
/*!40000 ALTER TABLE `Datasets` DISABLE KEYS */;
/*!40000 ALTER TABLE `Datasets` ENABLE KEYS */;
UNLOCK TABLES;

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
  KEY `FKndguin7fe90s63leldgw4psl5` (`deposit_id`),
  CONSTRAINT `FKndguin7fe90s63leldgw4psl5` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DepositChunks`
--

LOCK TABLES `DepositChunks` WRITE;
/*!40000 ALTER TABLE `DepositChunks` DISABLE KEYS */;
/*!40000 ALTER TABLE `DepositChunks` ENABLE KEYS */;
UNLOCK TABLES;

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
  KEY `FK2ytvilfcexh114l55es7lt61g` (`deposit_id`),
  CONSTRAINT `FK2ytvilfcexh114l55es7lt61g` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DepositPaths`
--

LOCK TABLES `DepositPaths` WRITE;
/*!40000 ALTER TABLE `DepositPaths` DISABLE KEYS */;
/*!40000 ALTER TABLE `DepositPaths` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DepositReviews`
--

DROP TABLE IF EXISTS `DepositReviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DepositReviews` (
  `id` varchar(36) NOT NULL,
  `actionedDate` datetime(6) DEFAULT NULL,
  `comment` text,
  `creationTime` datetime(6) NOT NULL,
  `deleteStatus` int(11) NOT NULL,
  `toBeDeleted` bit(1) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  `vaultReview_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdebpqavie4gma63sgd07yq8b2` (`deposit_id`),
  KEY `FKbm52snvf4627yxqt7ssonns3n` (`vaultReview_id`),
  CONSTRAINT `FKbm52snvf4627yxqt7ssonns3n` FOREIGN KEY (`vaultReview_id`) REFERENCES `VaultReviews` (`id`),
  CONSTRAINT `FKdebpqavie4gma63sgd07yq8b2` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DepositReviews`
--

LOCK TABLES `DepositReviews` WRITE;
/*!40000 ALTER TABLE `DepositReviews` DISABLE KEYS */;
/*!40000 ALTER TABLE `DepositReviews` ENABLE KEYS */;
UNLOCK TABLES;

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
  `creationTime` datetime(6) DEFAULT NULL,
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
  KEY `FKtilt0dpjks2dc7ljj2ykax3qf` (`user_id`),
  KEY `FKnj1jt1eofaxt0hyxw15exg8j2` (`vault_id`),
  CONSTRAINT `FKnj1jt1eofaxt0hyxw15exg8j2` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`),
  CONSTRAINT `FKtilt0dpjks2dc7ljj2ykax3qf` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Deposits`
--

LOCK TABLES `Deposits` WRITE;
/*!40000 ALTER TABLE `Deposits` DISABLE KEYS */;
/*!40000 ALTER TABLE `Deposits` ENABLE KEYS */;
UNLOCK TABLES;

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
  `location` varchar(255) DEFAULT NULL,
  `message` longtext,
  `remoteAddress` varchar(255) DEFAULT NULL,
  `retrieveId` varchar(255) DEFAULT NULL,
  `sequence` int(11) NOT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `archiveIds` tinyblob,
  `archiveSize` bigint(20) DEFAULT NULL,
  `digest` varchar(255) DEFAULT NULL,
  `digestAlgorithm` varchar(255) DEFAULT NULL,
  `chunksDigest` longblob,
  `bytes` bigint(20) DEFAULT NULL,
  `aesMode` varchar(255) DEFAULT NULL,
  `chunkIVs` longblob,
  `encChunkDigests` longblob,
  `encTarDigest` varchar(255) DEFAULT NULL,
  `tarIV` tinyblob,
  `archive_id` varchar(36) DEFAULT NULL,
  `assignee_id` varchar(36) DEFAULT NULL,
  `audit_id` varchar(36) DEFAULT NULL,
  `chunk_id` varchar(36) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  `job_id` varchar(36) DEFAULT NULL,
  `role_id` bigint(20) DEFAULT NULL,
  `school_id` varchar(180) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `vault_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKep67tag4fwv17nl3i491kfqfo` (`archive_id`),
  KEY `FKlnlglhyryrdqkfc3br82ymtjr` (`assignee_id`),
  KEY `FKkhk65sfbl9qqy7sgh233v9ob3` (`audit_id`),
  KEY `FKl16ud7orxt2ty8yu5qv77ed8w` (`chunk_id`),
  KEY `FKa9d801t407b5lw0bbp0x1x0m7` (`deposit_id`),
  KEY `FKov95katp9baujla6quxm3bhji` (`job_id`),
  KEY `FKtnuy3bx42sq3uocvd21b0ip2s` (`role_id`),
  KEY `FK29shnpa9v19n5icghndxwqm61` (`school_id`),
  KEY `FKfgapxagq63wrno5s0tk89ehjh` (`user_id`),
  KEY `FKefgds4egcvd1vhqvu52pi0ec4` (`vault_id`),
  CONSTRAINT `FK29shnpa9v19n5icghndxwqm61` FOREIGN KEY (`school_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FKa9d801t407b5lw0bbp0x1x0m7` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`),
  CONSTRAINT `FKefgds4egcvd1vhqvu52pi0ec4` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`),
  CONSTRAINT `FKep67tag4fwv17nl3i491kfqfo` FOREIGN KEY (`archive_id`) REFERENCES `Archives` (`id`),
  CONSTRAINT `FKfgapxagq63wrno5s0tk89ehjh` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FKkhk65sfbl9qqy7sgh233v9ob3` FOREIGN KEY (`audit_id`) REFERENCES `Audits` (`id`),
  CONSTRAINT `FKl16ud7orxt2ty8yu5qv77ed8w` FOREIGN KEY (`chunk_id`) REFERENCES `DepositChunks` (`id`),
  CONSTRAINT `FKlnlglhyryrdqkfc3br82ymtjr` FOREIGN KEY (`assignee_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FKov95katp9baujla6quxm3bhji` FOREIGN KEY (`job_id`) REFERENCES `Jobs` (`id`),
  CONSTRAINT `FKtnuy3bx42sq3uocvd21b0ip2s` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Events`
--

LOCK TABLES `Events` WRITE;
/*!40000 ALTER TABLE `Events` DISABLE KEYS */;
/*!40000 ALTER TABLE `Events` ENABLE KEYS */;
UNLOCK TABLES;

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
  KEY `FKk034r68jynocc5i1psu2nd0mw` (`user_id`),
  CONSTRAINT `FKk034r68jynocc5i1psu2nd0mw` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FileStores`
--

LOCK TABLES `FileStores` WRITE;
/*!40000 ALTER TABLE `FileStores` DISABLE KEYS */;
/*!40000 ALTER TABLE `FileStores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `GroupOwners`
--

DROP TABLE IF EXISTS `GroupOwners`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupOwners` (
  `group_id` varchar(180) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  KEY `FK7h53nqg2uubdh72ksnsslr3po` (`user_id`),
  KEY `FK1fe5hswfe8lxy9c4eyb509gf8` (`group_id`),
  CONSTRAINT `FK1fe5hswfe8lxy9c4eyb509gf8` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK7h53nqg2uubdh72ksnsslr3po` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `GroupOwners`
--

LOCK TABLES `GroupOwners` WRITE;
/*!40000 ALTER TABLE `GroupOwners` DISABLE KEYS */;
INSERT INTO `GroupOwners` VALUES ('grp-lfcs','admin1');
/*!40000 ALTER TABLE `GroupOwners` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Groups`
--

LOCK TABLES `Groups` WRITE;
/*!40000 ALTER TABLE `Groups` DISABLE KEYS */;
INSERT INTO `Groups` VALUES ('grp-lfcs',0x01,'LFCS');
/*!40000 ALTER TABLE `Groups` ENABLE KEYS */;
UNLOCK TABLES;

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
  `timestamp` datetime(6) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8aac5kl8f7tches8b03oe1ncl` (`deposit_id`),
  CONSTRAINT `FK8aac5kl8f7tches8b03oe1ncl` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Jobs`
--

LOCK TABLES `Jobs` WRITE;
/*!40000 ALTER TABLE `Jobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `Jobs` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PendingDataCreators`
--

LOCK TABLES `PendingDataCreators` WRITE;
/*!40000 ALTER TABLE `PendingDataCreators` DISABLE KEYS */;
/*!40000 ALTER TABLE `PendingDataCreators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PendingVaults`
--

DROP TABLE IF EXISTS `PendingVaults`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PendingVaults` (
  `id` varchar(36) NOT NULL,
  `affirmed` bit(1) NOT NULL,
  `authoriser` text,
  `billingType` text,
  `confirmed` bit(1) NOT NULL,
  `contact` text NOT NULL,
  `creationTime` datetime(6) DEFAULT NULL,
  `description` text,
  `estimate` text,
  `grantEndDate` date DEFAULT NULL,
  `name` text,
  `notes` text,
  `projectTitle` text,
  `pureLink` bit(1) NOT NULL,
  `reviewDate` date DEFAULT NULL,
  `schoolOrUnit` text,
  `sliceID` text,
  `subunit` text,
  `version` bigint(20) NOT NULL,
  `group_id` varchar(180) DEFAULT NULL,
  `retentionPolicy_id` int(11) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1oqxrihj71uwiynk4m8u0n0vj` (`group_id`),
  KEY `FKtqmx003465315spj99suwf1ta` (`retentionPolicy_id`),
  KEY `FKjj6cuk6m88qn7th5df64ui3cw` (`user_id`),
  CONSTRAINT `FK1oqxrihj71uwiynk4m8u0n0vj` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FKjj6cuk6m88qn7th5df64ui3cw` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FKtqmx003465315spj99suwf1ta` FOREIGN KEY (`retentionPolicy_id`) REFERENCES `RetentionPolicies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PendingVaults`
--

LOCK TABLES `PendingVaults` WRITE;
/*!40000 ALTER TABLE `PendingVaults` DISABLE KEYS */;
/*!40000 ALTER TABLE `PendingVaults` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Permissions`
--

LOCK TABLES `Permissions` WRITE;
/*!40000 ALTER TABLE `Permissions` DISABLE KEYS */;
INSERT INTO `Permissions` VALUES ('ASSIGN_SCHOOL_VAULT_ROLES','Assign school vault roles','ASSIGN_SCHOOL_VAULT_ROLES','SCHOOL'),('ASSIGN_VAULT_ROLES','Assign vault roles','ASSIGN_VAULT_ROLES','VAULT'),('CAN_MANAGE_ARCHIVE_STORES','Manage archive stores','CAN_MANAGE_ARCHIVE_STORES','ADMIN'),('CAN_MANAGE_BILLING_DETAILS','Manage billing details','CAN_MANAGE_BILLING_DETAILS','ADMIN'),('CAN_MANAGE_DEPOSITS','Administer school deposits','CAN_MANAGE_DEPOSITS','SCHOOL'),('CAN_MANAGE_PENDING_VAULTS','Administer pending vaults','CAN_MANAGE_PENDING_VAULTS','ADMIN'),('CAN_MANAGE_RETENTION_POLICIES','Manage retention policies','CAN_MANAGE_RETENTION_POLICIES','ADMIN'),('CAN_MANAGE_REVIEWS','Manage Reviews','CAN_MANAGE_REVIEWS','ADMIN'),('CAN_MANAGE_ROLES','Manage roles','CAN_MANAGE_ROLES','ADMIN'),('CAN_MANAGE_SCHOOL_ROLE_ASSIGNMENTS','Manage school role assignments','CAN_MANAGE_SCHOOL_ROLE_ASSIGNMENTS','ADMIN'),('CAN_MANAGE_VAULTS','Administer school vaults','CAN_MANAGE_VAULTS','SCHOOL'),('CAN_ORPHAN_SCHOOL_VAULTS','Orphan school vaults','CAN_ORPHAN_SCHOOL_VAULTS','SCHOOL'),('CAN_RETRIEVE_DATA','Retrieve data','CAN_RETRIEVE_DATA','SCHOOL'),('CAN_TRANSFER_VAULT_OWNERSHIP','Transfer vault ownership','CAN_TRANSFER_VAULT_OWNERSHIP','VAULT'),('CAN_VIEW_EVENTS','View events','CAN_VIEW_EVENTS','ADMIN'),('CAN_VIEW_IN_PROGRESS','Admin view transfers','CAN_VIEW_IN_PROGRESS','SCHOOL'),('CAN_VIEW_QUEUES','Admin view school queues','CAN_VIEW_QUEUES','SCHOOL'),('CAN_VIEW_RETRIEVES','Admin view school retrievals','CAN_VIEW_RETRIEVES','SCHOOL'),('CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS','Admin view school role assignments','CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS','SCHOOL'),('CAN_VIEW_VAULTS_SIZE','Admin view vault size','CAN_VIEW_VAULTS_SIZE','SCHOOL'),('DELETE_SCHOOL_VAULT_DEPOSITS','Delete school vault deposits','DELETE_SCHOOL_VAULT_DEPOSITS','SCHOOL'),('EDIT_SCHOOL_VAULT_METADATA','Edit school vault metadata','EDIT_SCHOOL_VAULT_METADATA','SCHOOL'),('MANAGE_SCHOOL_VAULT_DEPOSITS','Manage school transfers','MANAGE_SCHOOL_VAULT_DEPOSITS','SCHOOL'),('TRANSFER_SCHOOL_VAULT_OWNERSHIP','Transfer school vault ownership','TRANSFER_SCHOOL_VAULT_OWNERSHIP','SCHOOL'),('VIEW_DEPOSITS_AND_RETRIEVES','View deposits and retrieves','VIEW_DEPOSITS_AND_RETRIEVES','VAULT'),('VIEW_SCHOOL_VAULT_HISTORY','View school vault history','VIEW_SCHOOL_VAULT_HISTORY','SCHOOL'),('VIEW_SCHOOL_VAULT_METADATA','View school vault metadata','VIEW_SCHOOL_VAULT_METADATA','SCHOOL'),('VIEW_SCHOOL_VAULT_ROLES','View school vault roles','VIEW_SCHOOL_VAULT_ROLES','SCHOOL'),('VIEW_VAULT_HISTORY','View vault history','VIEW_VAULT_HISTORY','VAULT'),('VIEW_VAULT_METADATA','View vault metadata','VIEW_VAULT_METADATA','VAULT'),('VIEW_VAULT_ROLES','View vault roles','VIEW_VAULT_ROLES','VAULT');
/*!40000 ALTER TABLE `Permissions` ENABLE KEYS */;
UNLOCK TABLES;

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
  `extendUponRetrieval` bit(1) DEFAULT NULL,
  `inEffectDate` date DEFAULT NULL,
  `minDataRetentionPeriod` text,
  `minRetentionPeriod` int(11) NOT NULL,
  `name` text NOT NULL,
  `sort` int(11) NOT NULL,
  `url` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RetentionPolicies`
--

LOCK TABLES `RetentionPolicies` WRITE;
/*!40000 ALTER TABLE `RetentionPolicies` DISABLE KEYS */;
INSERT INTO `RetentionPolicies` VALUES (1,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Multiple Sclerosis Society',37,'https://www.mssociety.org.uk/ms-resources/grant-round-applicant-guidance'),(2,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'Not stated',5,'National Centre for the Replacement, Refinement and Reduction of Animal Research',38,'http://www.nc3rs.org.uk/sites/default/files/documents/Funding/Handbook.pdf'),(3,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'National Institute for Health Research',39,'https://www.nihr.ac.uk/funding-and-support/funding-for-research-studies/how-to-apply-for-funding/'),(4,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'Not stated',5,'NERC',40,'http://www.nerc.ac.uk/research/sites/data/policy/'),(5,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Paul Mellon Centre for Studies in British Art',41,'http://www.paul-mellon-centre.ac.uk/fellowships-and-grants/procedure'),(6,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'?',5,'Pet Plan Charitable Trust',42,NULL),(7,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Royal Academy of Engineering',43,'https://www.raeng.org.uk/grants-and-prizes/support-for-research'),(8,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'not stated',5,'Royal Society',44,'https://royalsociety.org/grants-schemes-awards/'),(9,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Royal Society of Chemistry',45,'http://www.rsc.org/awards-funding/funding'),(10,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Royal Society of Edinburgh',46,'https://www.rse.org.uk/funding-awards/'),(11,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Scottish Funding Council',47,'http://www.sfc.ac.uk/funding/university-funding/university-funding-research/university-research-funding.aspx'),(12,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Scottish Government',48,'http://www.gov.scot/topics/research'),(13,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Scottish Institute for Policing Research',49,'http://www.sipr.ac.uk/research/index.php'),(14,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Society for Endocrinology',50,'https://www.endocrinology.org/grants-and-awards/'),(15,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Society for Reproduction and Fertility',51,'http://srf-reproduction.org/grants-awards/grants/'),(16,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'10 years from the end of the project',5,'STFC',52,'https://www.stfc.ac.uk/funding/research-grants/data-management-plan/'),(17,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'Tenovus - Scotland',53,'https://tenovus-scotland.org.uk/for-researchers/'),(18,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'UK-India Eduation and Research Initiative',54,'http://www.ukieri.org/call-for-research-applications-2017-18.html'),(19,'2018-02-13',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,NULL,'N/A',5,'University of Edinburgh (applicable to unfunded or self-funded research)',55,''),(20,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.uk.WTBasicRetentionPolicy',0x01,NULL,'Not stated',5,'Wellcome Trust Basic',56,'https://wellcome.ac.uk/funding/managing-grant/policy-data-software-materials-management-and-sharing'),(21,'2018-02-20',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.uk.WTPHCRetentionPolicy',0x01,NULL,'Not stated',5,'Wellcome Trust Population Health / Clinical',57,'https://wellcome.ac.uk/funding/managing-grant/policy-data-software-materials-management-and-sharing'),(22,'2018-12-03',NULL,NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,'2018-12-03','N/A',5,'NHS Retention Policy',58,'https://www.hra.nhs.uk/planning-and-improving-research/policies-standards-legislation/'),(23,'2018-12-03','Policy of UoE\'s Edinburgh Imaging (part of Edinburgh Medical School)',NULL,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',0x01,'2018-12-03','N/A',5,'Edinburgh Imaging Retention Policy',59,'');
/*!40000 ALTER TABLE `RetentionPolicies` ENABLE KEYS */;
UNLOCK TABLES;

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
  `timestamp` datetime(6) DEFAULT NULL,
  `deposit_id` varchar(36) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl3oxmju8k8onaakf35ufdc9i2` (`deposit_id`),
  KEY `FKma62emkptkrk72v1rdj8ubwr4` (`user_id`),
  CONSTRAINT `FKl3oxmju8k8onaakf35ufdc9i2` FOREIGN KEY (`deposit_id`) REFERENCES `Deposits` (`id`),
  CONSTRAINT `FKma62emkptkrk72v1rdj8ubwr4` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Retrieves`
--

LOCK TABLES `Retrieves` WRITE;
/*!40000 ALTER TABLE `Retrieves` DISABLE KEYS */;
/*!40000 ALTER TABLE `Retrieves` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Role_assignments`
--

DROP TABLE IF EXISTS `Role_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Role_assignments` (
  `id` bigint(20) NOT NULL,
  `pending_vault_id` varchar(255) DEFAULT NULL,
  `school_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `vault_id` varchar(255) DEFAULT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkqx1luv7kal490959aop4pj75` (`role_id`,`user_id`,`school_id`),
  UNIQUE KEY `UKmyhycfy43e3socqtp24jx1x71` (`role_id`,`user_id`,`vault_id`),
  UNIQUE KEY `UKg2acjll67qg8362houjkgans5` (`role_id`,`user_id`,`pending_vault_id`),
  CONSTRAINT `FK7qiveo6i47tn9oemylkia6joo` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Role_assignments`
--

LOCK TABLES `Role_assignments` WRITE;
/*!40000 ALTER TABLE `Role_assignments` DISABLE KEYS */;
INSERT INTO `Role_assignments` VALUES (1,NULL,'grp-lfcs','admin1',NULL,1),(2,NULL,'grp-lfcs','user1',NULL,2);
/*!40000 ALTER TABLE `Role_assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Role_permissions`
--

DROP TABLE IF EXISTS `Role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Role_permissions` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` varchar(36) NOT NULL,
  KEY `FK29bs3ppvbassust2crr5v0emu` (`permission_id`),
  KEY `FKbjjjjidtwd336fgsi5dvwqnh1` (`role_id`),
  CONSTRAINT `FK29bs3ppvbassust2crr5v0emu` FOREIGN KEY (`permission_id`) REFERENCES `Permissions` (`id`),
  CONSTRAINT `FKbjjjjidtwd336fgsi5dvwqnh1` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Role_permissions`
--

LOCK TABLES `Role_permissions` WRITE;
/*!40000 ALTER TABLE `Role_permissions` DISABLE KEYS */;
INSERT INTO `Role_permissions` VALUES (1,'ASSIGN_SCHOOL_VAULT_ROLES'),(1,'ASSIGN_VAULT_ROLES'),(1,'CAN_MANAGE_ARCHIVE_STORES'),(1,'CAN_MANAGE_BILLING_DETAILS'),(1,'CAN_MANAGE_DEPOSITS'),(1,'CAN_MANAGE_PENDING_VAULTS'),(1,'CAN_MANAGE_RETENTION_POLICIES'),(1,'CAN_MANAGE_REVIEWS'),(1,'CAN_MANAGE_ROLES'),(1,'CAN_MANAGE_SCHOOL_ROLE_ASSIGNMENTS'),(1,'CAN_MANAGE_VAULTS'),(1,'CAN_ORPHAN_SCHOOL_VAULTS'),(1,'CAN_RETRIEVE_DATA'),(1,'CAN_TRANSFER_VAULT_OWNERSHIP'),(1,'CAN_VIEW_EVENTS'),(1,'CAN_VIEW_IN_PROGRESS'),(1,'CAN_VIEW_QUEUES'),(1,'CAN_VIEW_RETRIEVES'),(1,'CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS'),(1,'CAN_VIEW_VAULTS_SIZE'),(1,'DELETE_SCHOOL_VAULT_DEPOSITS'),(1,'EDIT_SCHOOL_VAULT_METADATA'),(1,'MANAGE_SCHOOL_VAULT_DEPOSITS'),(1,'TRANSFER_SCHOOL_VAULT_OWNERSHIP'),(1,'VIEW_DEPOSITS_AND_RETRIEVES'),(1,'VIEW_SCHOOL_VAULT_HISTORY'),(1,'VIEW_SCHOOL_VAULT_METADATA'),(1,'VIEW_SCHOOL_VAULT_ROLES'),(1,'VIEW_VAULT_HISTORY'),(1,'VIEW_VAULT_METADATA'),(1,'VIEW_VAULT_ROLES'),(2,'ASSIGN_VAULT_ROLES'),(2,'CAN_TRANSFER_VAULT_OWNERSHIP'),(2,'VIEW_DEPOSITS_AND_RETRIEVES'),(2,'VIEW_VAULT_HISTORY'),(2,'VIEW_VAULT_METADATA'),(2,'VIEW_VAULT_ROLES'),(102,'ASSIGN_VAULT_ROLES'),(102,'CAN_TRANSFER_VAULT_OWNERSHIP'),(102,'VIEW_DEPOSITS_AND_RETRIEVES'),(102,'VIEW_VAULT_HISTORY'),(102,'VIEW_VAULT_METADATA'),(102,'VIEW_VAULT_ROLES');
/*!40000 ALTER TABLE `Role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Roles`
--

DROP TABLE IF EXISTS `Roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Roles` (
  `id` bigint(20) NOT NULL,
  `assignedUserCount` int(11) NOT NULL,
  `description` text,
  `name` text NOT NULL,
  `role_status` text NOT NULL,
  `role_type` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Roles`
--

LOCK TABLES `Roles` WRITE;
/*!40000 ALTER TABLE `Roles` DISABLE KEYS */;
INSERT INTO `Roles` VALUES (1,0,'An admin of the whole system, with full permissions over the system.','IS Admin','0','ADMIN'),(2,0,'An admin of a specific vault, with full permissions over that vault.','Data Owner','0','ADMIN'),(98,0,'School Role 98','SchoolRole98','0','SCHOOL'),(99,0,'School Role 99','SchoolRole99','1','SCHOOL'),(100,0,'Acting on behalf of the Data Owner, may view the vault, deposit data and retrieve any deposit in the vault.','Depositor','2','VAULT'),(101,0,'Acting on behalf of the Data Owner, may view the vault, edit metadata fields, deposit data and retrieve  any deposit in the vault. Can assign other users to the Depositor role. ','Nominated Data Manager','1','VAULT'),(102,0,'The creator of the vault pending acceptance to full vault status','Vault Creator','1','ADMIN');
/*!40000 ALTER TABLE `Roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Users` (
  `id` varchar(36) NOT NULL,
  `email` text,
  `firstname` text NOT NULL,
  `lastname` text NOT NULL,
  `password` text,
  `properties` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Users`
--

LOCK TABLES `Users` WRITE;
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` VALUES ('admin1','admin@test.com','admin user 1','Test','password1',NULL),('user1','user1@test.com','user 1','Test','user1pass',NULL);
/*!40000 ALTER TABLE `Users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `VaultReviews`
--

DROP TABLE IF EXISTS `VaultReviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `VaultReviews` (
  `id` varchar(36) NOT NULL,
  `actionedDate` datetime(6) DEFAULT NULL,
  `comment` text,
  `creationTime` datetime(6) NOT NULL,
  `newReviewDate` date DEFAULT NULL,
  `oldReviewDate` date DEFAULT NULL,
  `vault_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo1mg4q2at7c7bqggenipxyl35` (`vault_id`),
  CONSTRAINT `FKo1mg4q2at7c7bqggenipxyl35` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `VaultReviews`
--

LOCK TABLES `VaultReviews` WRITE;
/*!40000 ALTER TABLE `VaultReviews` DISABLE KEYS */;
/*!40000 ALTER TABLE `VaultReviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Vaults`
--

DROP TABLE IF EXISTS `Vaults`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Vaults` (
  `id` varchar(36) NOT NULL,
  `affirmed` bit(1) NOT NULL,
  `contact` text NOT NULL,
  `creationTime` datetime(6) DEFAULT NULL,
  `description` text,
  `estimate` text,
  `grantEndDate` date DEFAULT NULL,
  `name` text NOT NULL,
  `notes` text,
  `projectId` varchar(255) DEFAULT NULL,
  `pureLink` bit(1) NOT NULL,
  `retentionPolicyExpiry` datetime(6) DEFAULT NULL,
  `retentionPolicyLastChecked` datetime(6) DEFAULT NULL,
  `retentionPolicyStatus` int(11) NOT NULL,
  `reviewDate` date NOT NULL,
  `snapshot` longtext,
  `vaultSize` bigint(20) NOT NULL,
  `version` bigint(20) NOT NULL,
  `dataset_id` varchar(180) DEFAULT NULL,
  `group_id` varchar(180) DEFAULT NULL,
  `retentionPolicy_id` int(11) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2kkd4nhtim6h32ayv8bbntr10` (`dataset_id`),
  KEY `FK2h0lbbyrjkskm1g02xwe3s69g` (`group_id`),
  KEY `FKl46pt0r8q1u403ns982985o58` (`retentionPolicy_id`),
  KEY `FK13r9ixgxy4f1meg24xlgmmpaf` (`user_id`),
  CONSTRAINT `FK13r9ixgxy4f1meg24xlgmmpaf` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK2h0lbbyrjkskm1g02xwe3s69g` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK2kkd4nhtim6h32ayv8bbntr10` FOREIGN KEY (`dataset_id`) REFERENCES `Datasets` (`id`),
  CONSTRAINT `FKl46pt0r8q1u403ns982985o58` FOREIGN KEY (`retentionPolicy_id`) REFERENCES `RetentionPolicies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Vaults`
--

LOCK TABLES `Vaults` WRITE;
/*!40000 ALTER TABLE `Vaults` DISABLE KEYS */;
/*!40000 ALTER TABLE `Vaults` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `paused_deposit_state`;

CREATE TABLE `paused_deposit_state`
(
    `id`        VARCHAR(255) NOT NULL,
    `created`   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP NULL,
    `is_paused` TINYINT(1)   NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

DROP TABLE IF EXISTS `paused_retrieve_state`;

CREATE TABLE `paused_retrieve_state`
(
    `id`        VARCHAR(255) NOT NULL,
    `created`   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP NULL,
    `is_paused` TINYINT(1)   NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

delete from `paused_deposit_state`;
INSERT INTO `paused_deposit_state`  VALUES ('101', '2001-07-22 12:12:12.123456', true);
INSERT INTO `paused_deposit_state`  VALUES ('102', '2002-07-22 12:12:12.123456', false);
INSERT INTO `paused_deposit_state`  VALUES ('103', '2003-07-22 12:12:12.123456', true);
INSERT INTO `paused_deposit_state`  VALUES ('104', '2004-07-22 12:12:12.123456', false);

delete from `paused_retrieve_state`;
INSERT INTO `paused_retrieve_state`  VALUES ('201', '2001-07-22 12:12:12.123456', true);
INSERT INTO `paused_retrieve_state`  VALUES ('202', '2002-07-22 12:12:12.123456', false);
INSERT INTO `paused_retrieve_state`  VALUES ('203', '2003-07-22 12:12:12.123456', true);
INSERT INTO `paused_retrieve_state`  VALUES ('204', '2004-07-22 12:12:12.123456', false);

--
-- 5.0.1
--
ALTER TABLE BillingInfo add column paymentDetails TEXT;
ALTER TABLE PendingVaults add column sliceQueryChoice TEXT;
ALTER TABLE PendingVaults add column fundingQueryChoice TEXT;
ALTER TABLE PendingVaults add column feewaiverQueryChoice TEXT;
ALTER TABLE PendingVaults add column paymentDetails TEXT;

--
-- 5.0.3 Deposit/Restart improvements
--

alter table `Deposits` ROW_FORMAT=DYNAMIC;
alter table `Deposits` ADD `non_restart_job_id` VARCHAR(256) DEFAULT NULL;

alter table `Events` ROW_FORMAT=DYNAMIC;
alter table `Events` ADD `chunkNumber` INT NULL, ADD `archive_store_id` varchar(256) NULL;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (103);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-08-16 15:33:41
