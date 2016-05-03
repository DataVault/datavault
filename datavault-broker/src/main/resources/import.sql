insert into RetentionPolicies (id, name, engine, description, sort) values ('UNIVERSITY', 'Default University Policy', 'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy', 'Default University policy that flags vaults for review after five years.', 1);
insert into RetentionPolicies (id, name, engine, description, sort) values ('EPSRC', 'EPSRC retention policy', 'org.datavaultplatform.common.retentionpolicy.impl.EPSRCRetentionPolicy', 'Vaults are due for review ten years after the date of the last retrieve event or the data the last deposit was made, whichever is the latter.', 2);
insert into RetentionPolicies (id, name, engine, description, sort) values ('5MIN', '5 minute test policy', 'org.datavaultplatform.common.retentionpolicy.impl.FiveMinuteRetentionPolicy', 'Test policy that flags for review any vault over 5 minutes old.', 3);

insert into Users (id, name, password, admin) values ('USER1', 'Test user 1', 'password1', FALSE);
insert into Users (id, name, password, admin) values ('USER2', 'Test user 2', 'password2', FALSE);
insert into Users (id, name, password, admin) values ('USER3', 'Test user 3', 'password3', FALSE);
insert into Users (id, name, password, admin) values ('USER4', 'Test user 4', 'password4', FALSE);
insert into Users (id, name, password, admin) values ('USER5', 'Test user 5', 'password5', FALSE);
insert into Users (id, name, password, admin) values ('USER6', 'Test user 6', 'password6', FALSE);
insert into Users (id, name, password, admin) values ('USER7', 'Test user 7', 'password7', FALSE);
insert into Users (id, name, password, admin) values ('USER8', 'Test user 8', 'password8', FALSE);
insert into Users (id, name, password, admin) values ('USER9', 'Test user 9', 'password9', FALSE);
insert into Users (id, name, password, admin) values ('USER10', 'Test user 10', 'password10', FALSE);
insert into Users (id, name, password, admin) values ('USER11', 'Test user 11', 'password11', FALSE);
insert into Users (id, name, password, admin) values ('USER12', 'Test user 12', 'password12', FALSE);
insert into Users (id, name, password, admin) values ('USER13', 'Test user 13', 'password13', FALSE);
insert into Users (id, name, password, admin) values ('USER14', 'Test user 14', 'password14', FALSE);
insert into Users (id, name, password, admin) values ('USER15', 'Test user 15', 'password15', FALSE);
insert into Users (id, name, password, admin) values ('USER16', 'Test user 16', 'password16', FALSE);
insert into Users (id, name, password, admin) values ('USER17', 'Test user 17', 'password17', FALSE);
insert into Users (id, name, password, admin) values ('USER18', 'Test user 18', 'password18', FALSE);
insert into Users (id, name, password, admin) values ('USER19', 'Test user 19', 'password19', FALSE);
insert into Users (id, name, password, admin) values ('USER20', 'Test user 20', 'password20', FALSE);
insert into Users (id, name, password, admin) values ('USER21', 'Test user 21', 'password21', FALSE);
insert into Users (id, name, password, admin) values ('USER22', 'Test user 22', 'password22', FALSE);
insert into Users (id, name, password, admin) values ('USER23', 'Test user 23', 'password23', FALSE);
insert into Users (id, name, password, admin) values ('USER24', 'Test user 24', 'password24', FALSE);
insert into Users (id, name, password, admin) values ('USER25', 'Test user 25', 'password25', FALSE);
insert into Users (id, name, password, admin) values ('USER26', 'Test user 26', 'password26', FALSE);
insert into Users (id, name, password, admin) values ('USER27', 'Test user 27', 'password27', FALSE);
insert into Users (id, name, password, admin) values ('USER28', 'Test user 28', 'password28', FALSE);
insert into Users (id, name, password, admin) values ('USER29', 'Test user 29', 'password29', FALSE);
insert into Users (id, name, password, admin) values ('USER30', 'Test user 30', 'password30', FALSE);

insert into Users (id, name, password, admin) values ('ADMIN1', 'Test admin user 1', 'password1', TRUE);

insert into Groups (id, name) values ('CHSS', 'Humanities and Social Science');
insert into Groups (id, name) values ('CSE', 'Science and Engineering');
insert into Groups (id, name) values ('CMVM', 'Medicine and Veterinary Medicine');

insert into GroupOwners (group_id, user_id) values ('CHSS', 'USER1');
insert into GroupOwners (group_id, user_id) values ('CSE', 'USER1');
insert into GroupOwners (group_id, user_id) values ('CMVM', 'USER1');
insert into GroupOwners (group_id, user_id) values ('CHSS', 'USER2');
insert into GroupOwners (group_id, user_id) values ('CSE', 'USER3');
insert into GroupOwners (group_id, user_id) values ('CMVM', 'USER4');

insert into Clients (id, name, apiKey, ipAddress) values ('datavault-webapp', 'Datavault Webapp', 'datavault-webapp', '127.0.0.1');
