insert into RetentionPolicies (id, name, engine, description, sort) values ('UNIVERSITY', 'Default University Policy', 'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy', 'Default University policy that flags vaults for review after 5 years.', 1);
insert into RetentionPolicies (id, name, engine, description, sort) values ('AHRC', 'AHRC', 'org.datavaultplatform.common.retentionpolicy.impl.uk.AHRCRetentionPolicy', 'Vaults are due for review 3 years after the date the last deposit was made.', 2);
insert into RetentionPolicies (id, name, engine, description, sort) values ('BBSRC', 'BBSRC', 'org.datavaultplatform.common.retentionpolicy.impl.uk.BBSRCRetentionPolicy', 'Vaults are due for review 10 years after the date the last deposit was made.', 3);
insert into RetentionPolicies (id, name, engine, description, sort) values ('CRUK', 'Cancer Research UK', 'org.datavaultplatform.common.retentionpolicy.impl.uk.CancerResearchRetentionPolicy', 'Vaults are due for review 5 years after the date the last deposit was made.', 4);
insert into RetentionPolicies (id, name, engine, description, sort) values ('EPSRC', 'EPSRC', 'org.datavaultplatform.common.retentionpolicy.impl.uk.EPSRCRetentionPolicy', 'Vaults are due for review 10 years after the date of the last retrieve event or the data the last deposit was made, whichever is the latter.', 5);
insert into RetentionPolicies (id, name, engine, description, sort) values ('H2020', 'Horizon 2020', 'org.datavaultplatform.common.retentionpolicy.impl.eu.H2020RetentionPolicy', 'Vaults are due for review 10 years after the date the last deposit was made.', 6);
insert into RetentionPolicies (id, name, engine, description, sort) values ('MRC Basic', 'MRC Basic', 'org.datavaultplatform.common.retentionpolicy.impl.uk.MRCBasicRetentionPolicy', 'Vaults are due for review 10 years after the date the last deposit was made.', 7);
insert into RetentionPolicies (id, name, engine, description, sort) values ('MRC PHC', 'MRC Population Health / Clinical', 'org.datavaultplatform.common.retentionpolicy.impl.uk.MRCPHCRetentionPolicy', 'Vaults are due for review 20 years after the date the last deposit was made.', 8);
insert into RetentionPolicies (id, name, engine, description, sort) values ('WT Basic', 'Wellcome Trust Basic', 'org.datavaultplatform.common.retentionpolicy.impl.uk.WTBasicRetentionPolicy', 'Vaults are due for review 10 years after the date the last deposit was made.', 9);
insert into RetentionPolicies (id, name, engine, description, sort) values ('WT PHC', 'Wellcome Trust Population Health / Clinical', 'org.datavaultplatform.common.retentionpolicy.impl.uk.WTPHCRetentionPolicy', 'Vaults are due for review 20 years after the date the last deposit was made.', 10);
insert into RetentionPolicies (id, name, engine, description, sort) values ('5MIN', '5 minute test policy', 'org.datavaultplatform.common.retentionpolicy.impl.FiveMinuteRetentionPolicy', 'Test policy that flags for review any vault over 5 minutes old.', 11);

insert into Users (id, firstname, lastname, password, admin) values ('user1', 'user 1', 'Test', 'password1', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user2', 'user 2', 'Test', 'password2', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user3', 'user 3', 'Test', 'password3', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user4', 'user 4', 'Test', 'password4', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user5', 'user 5', 'Test', 'password5', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user6', 'user 6', 'Test', 'password6', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user7', 'user 7', 'Test', 'password7', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user8', 'user 8', 'Test', 'password8', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user9', 'user 9', 'Test', 'password9', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user10', 'user 10', 'Test', 'password10', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user11', 'user 11', 'Test', 'password11', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user12', 'user 12', 'Test', 'password12', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user13', 'user 13', 'Test', 'password13', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user14', 'user 14', 'Test', 'password14', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user15', 'user 15', 'Test', 'password15', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user16', 'user 16', 'Test', 'password16', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user17', 'user 17', 'Test', 'password17', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user18', 'user 18', 'Test', 'password18', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user19', 'user 19', 'Test', 'password19', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user20', 'user 20', 'Test', 'password20', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user21', 'user 21', 'Test', 'password21', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user22', 'user 22', 'Test', 'password22', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user23', 'user 23', 'Test', 'password23', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user24', 'user 24', 'Test', 'password24', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user25', 'user 25', 'Test', 'password25', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user26', 'user 26', 'Test', 'password26', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user27', 'user 27', 'Test', 'password27', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user28', 'user 28', 'Test', 'password28', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user29', 'user 29', 'Test', 'password29', FALSE);
insert into Users (id, firstname, lastname, password, admin) values ('user30', 'user 30', 'Test', 'password30', FALSE);

insert into Users (id, firstname, lastname, password, admin) values ('admin1', 'admin user 1', 'Test', 'password1', TRUE);

insert into Groups (id, name, enabled) values ('CHSS', 'Humanities and Social Science', 1);
insert into Groups (id, name, enabled) values ('CSE', 'Science and Engineering', 1);
insert into Groups (id, name, enabled) values ('CMVM', 'Medicine and Veterinary Medicine', 1);

insert into GroupOwners (group_id, user_id) values ('CHSS', 'user1');
insert into GroupOwners (group_id, user_id) values ('CSE', 'user1');
insert into GroupOwners (group_id, user_id) values ('CMVM', 'user1');
insert into GroupOwners (group_id, user_id) values ('CHSS', 'user2');
insert into GroupOwners (group_id, user_id) values ('CSE', 'user3');
insert into GroupOwners (group_id, user_id) values ('CMVM', 'user4');

insert into Clients (id, name, apiKey, ipAddress) values ('datavault-webapp', 'Datavault Webapp', 'datavault-webapp', '127.0.0.1');
