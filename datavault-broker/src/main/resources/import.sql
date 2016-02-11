insert into Policies (id, name, engine, description, sort) values ('UNIVERSITY', 'Default University Policy', 'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy', 'Default University policy that flags vaults for review after five years.', 1);
insert into Policies (id, name, engine, description, sort) values ('EPSRC', 'EPSRC retention policy', 'org.datavaultplatform.common.retentionpolicy.impl.EPSRCRetentionPolicy', 'Vaults are due for review ten years after the date of the last restore event or the data the last deposit was made, whichever is the latter.', 2);
insert into Policies (id, name, engine, description, sort) values ('5MIN', '5 minute test policy', 'org.datavaultplatform.common.retentionpolicy.impl.FiveMinuteRetentionPolicy', 'Test policy that flags for review any vault over 5 minutes old.', 3);

insert into Users (id, name) values ('USER1', 'Test user 1');
insert into Users (id, name) values ('USER2', 'Test user 2');
insert into Users (id, name) values ('USER3', 'Test user 3');
insert into Users (id, name) values ('USER4', 'Test user 4');
insert into Users (id, name) values ('USER5', 'Test user 5');
insert into Users (id, name) values ('USER6', 'Test user 6');
insert into Users (id, name) values ('USER7', 'Test user 7');
insert into Users (id, name) values ('USER8', 'Test user 8');
insert into Users (id, name) values ('USER9', 'Test user 9');
insert into Users (id, name) values ('USER10', 'Test user 10');
insert into Users (id, name) values ('USER11', 'Test user 11');
insert into Users (id, name) values ('USER12', 'Test user 12');
insert into Users (id, name) values ('USER13', 'Test user 13');
insert into Users (id, name) values ('USER14', 'Test user 14');
insert into Users (id, name) values ('USER15', 'Test user 15');
insert into Users (id, name) values ('USER16', 'Test user 16');
insert into Users (id, name) values ('USER17', 'Test user 17');
insert into Users (id, name) values ('USER18', 'Test user 18');
insert into Users (id, name) values ('USER19', 'Test user 19');
insert into Users (id, name) values ('USER20', 'Test user 20');
insert into Users (id, name) values ('USER21', 'Test user 21');
insert into Users (id, name) values ('USER22', 'Test user 22');
insert into Users (id, name) values ('USER23', 'Test user 23');
insert into Users (id, name) values ('USER24', 'Test user 24');
insert into Users (id, name) values ('USER25', 'Test user 25');
insert into Users (id, name) values ('USER26', 'Test user 26');
insert into Users (id, name) values ('USER27', 'Test user 27');
insert into Users (id, name) values ('USER28', 'Test user 28');
insert into Users (id, name) values ('USER29', 'Test user 29');
insert into Users (id, name) values ('USER30', 'Test user 30');

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
