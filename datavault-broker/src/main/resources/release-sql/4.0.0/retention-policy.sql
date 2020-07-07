ALTER TABLE RetentionPolicies ADD extendUponRetrieval BOOLEAN DEFAULT NULL;
ALTER TABLE RetentionPolicies ADD minRetentionPeriod int(11) NOT NULL;

update RetentionPolicies set extendUponRetrieval = false;