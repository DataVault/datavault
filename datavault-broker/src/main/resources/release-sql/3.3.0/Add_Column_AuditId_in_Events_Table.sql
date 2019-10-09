CREATE TABLE `Audits` (
  `id` varchar(36) NOT NULL,
  `note` text,
  `status` text,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
ALTER TABLE Events
ADD COLUMN audit_id varchar(36);
ALTER TABLE Events
ADD CONSTRAINT fk_audit_id FOREIGN KEY (audit_id) REFERENCES Audits (id);

ALTER TABLE Events
ADD COLUMN archive_id varchar(36);
ALTER TABLE Events
ADD CONSTRAINT fk_archive_id FOREIGN KEY (archive_id) REFERENCES Archives (id);

ALTER TABLE Events
ADD COLUMN location varchar(36);

ALTER TABLE Events
ADD COLUMN chunk_id varchar(36);
ALTER TABLE Events
ADD CONSTRAINT fk_chunk_id FOREIGN KEY (chunk_id) REFERENCES DepositChunks (id);

CREATE TABLE `AuditChunkStatus` (
  `id` varchar(36) NOT NULL,
  `note` text,
  `status` text,
  `timestamp` datetime DEFAULT NULL,
  `completeTime` datetime DEFAULT NULL,
  `archiveId` text,
  `location` varchar(36),
  `audit_id` varchar(36),
  `depositChunk_id` varchar(36),
  PRIMARY KEY (`id`),
  KEY `fk_auditedChunks_audit_id` (`audit_id`),
  KEY `fk_auditedChunks_depositChunk_id` (`depositChunk_id`),
  CONSTRAINT `fk_auditedChunks_audit_id` FOREIGN KEY (`audit_id`) REFERENCES `Audits` (`id`),
  CONSTRAINT `fk_auditedChunks_depositChunk_id` FOREIGN KEY (`depositChunk_id`) REFERENCES `DepositChunks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

