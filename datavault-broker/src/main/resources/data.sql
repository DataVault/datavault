INSERT INTO Message (id, message, timestamp) values (UUID(), 'test-message', CURRENT_TIMESTAMP());

-- there should be a unique constraint on ip address and apiKey;
insert ignore into Clients (id, name, apiKey, ipAddress) values ('datavault-webapp', 'Datavault Webapp', 'datavault-webapp', '127.0.0.1');
insert ignore into Clients (id, name, apiKey, ipAddress) values ('datavault-webappIPV6', 'Datavault Webapp IPV6', 'datavault-webapp-ipv6', '0:0:0:0:0:0:0:1');

insert ignore into Users (id, firstname, lastname, password, email) values ('admin1', 'admin user 1', 'Test', 'password1', 'admin@test.com');
insert ignore into Users (id, firstname, lastname, password, email) values ('user1', 'user 1', 'Test', 'user1pass', 'user1@test.com');

insert ignore into Groups (id, name, enabled) values ('grp-lfcs','LFCS','Y');

insert into GroupOwners (group_id, user_id)
select 'grp-lfcs','admin1'
where not exists (select 1 from GroupOwners where group_id='grp-lfcs' and user_id='admin1');

-- create the admin user with 'IS Admin' role - all 31 permissions
insert ignore into Role_assignments (id,user_id, school_id, role_id) select 1,'admin1','grp-lfcs', max(id) from Roles where name='IS Admin';

-- create the non-admin user with 'DATA OWNER' role - just 6 data owner permissions
insert ignore into Role_assignments (id,user_id, school_id, role_id) select 2,'user1','grp-lfcs', max(id) from Roles where name='Data Owner';

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Multiple Sclerosis Society',37,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'https://www.mssociety.org.uk/ms-resources/grant-round-applicant-guidance',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 37);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'National Centre for the Replacement, Refinement and Reduction of Animal Research',38,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'Not stated',5,NULL,NULL,'http://www.nc3rs.org.uk/sites/default/files/documents/Funding/Handbook.pdf',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 38);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'National Institute for Health Research',39,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'https://www.nihr.ac.uk/funding-and-support/funding-for-research-studies/how-to-apply-for-funding/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 39);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'NERC',40,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'Not stated',5,NULL,NULL,'http://www.nerc.ac.uk/research/sites/data/policy/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 40);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Paul Mellon Centre for Studies in British Art',41,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'http://www.paul-mellon-centre.ac.uk/fellowships-and-grants/procedure',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 41);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Pet Plan Charitable Trust',42,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'?',5,NULL,NULL,NULL,STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 42);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Royal Academy of Engineering',43,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'https://www.raeng.org.uk/grants-and-prizes/support-for-research',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 43);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Royal Society',44,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'not stated',5,NULL,NULL,'https://royalsociety.org/grants-schemes-awards/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 44);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Royal Society of Chemistry',45,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'http://www.rsc.org/awards-funding/funding',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 45);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Royal Society of Edinburgh',46,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'https://www.rse.org.uk/funding-awards/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 46);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Scottish Funding Council',47,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'http://www.sfc.ac.uk/funding/university-funding/university-funding-research/university-research-funding.aspx',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 47);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Scottish Government',48,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'http://www.gov.scot/topics/research',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 48);


INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Scottish Institute for Policing Research',49,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'http://www.sipr.ac.uk/research/index.php',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 49);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Society for Endocrinology',50,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'https://www.endocrinology.org/grants-and-awards/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 50);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Society for Reproduction and Fertility',51,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'http://srf-reproduction.org/grants-awards/grants/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 51);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'STFC',52,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'10 years from the end of the project',5,NULL,NULL,'https://www.stfc.ac.uk/funding/research-grants/data-management-plan/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 52);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Tenovus - Scotland',53,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'https://tenovus-scotland.org.uk/for-researchers/',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 53);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'UK-India Eduation and Research Initiative',54,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'http://www.ukieri.org/call-for-research-applications-2017-18.html',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 54);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'University of Edinburgh (applicable to unfunded or self-funded research)',55,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,NULL,NULL,'',STR_TO_DATE('13/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 55);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Wellcome Trust Basic',56,'org.datavaultplatform.common.retentionpolicy.impl.uk.WTBasicRetentionPolicy',TRUE,'Not stated',5,NULL,NULL,'https://wellcome.ac.uk/funding/managing-grant/policy-data-software-materials-management-and-sharing',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 56);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Wellcome Trust Population Health / Clinical',57,'org.datavaultplatform.common.retentionpolicy.impl.uk.WTPHCRetentionPolicy',TRUE,'Not stated',5,NULL,NULL,'https://wellcome.ac.uk/funding/managing-grant/policy-data-software-materials-management-and-sharing',STR_TO_DATE('20/02/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 57);

INSERT ignore INTO RetentionPolicies(name,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'NHS Retention Policy',58,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,STR_TO_DATE('03/12/2018', '%d/%m/%Y'),NULL,'https://www.hra.nhs.uk/planning-and-improving-research/policies-standards-legislation/',STR_TO_DATE('03/12/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 58);

INSERT ignore INTO RetentionPolicies(name,description,sort,engine,extendUponRetrieval,minDataRetentionPeriod,minRetentionPeriod,inEffectDate,endDate,url,dataGuidanceReviewed)
select 'Edinburgh Imaging Retention Policy','Policy of UoE''s Edinburgh Imaging (part of Edinburgh Medical School)',59,'org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy',TRUE,'N/A',5,STR_TO_DATE('03/12/2018', '%d/%m/%Y'),NULL,'',STR_TO_DATE('03/12/2018', '%d/%m/%Y')
where not exists (select 1 from RetentionPolicies where sort = 59);

-- Java Code will Add this 'IS Admin' role if it's missing but we need it here (before Java Runs) so we can add a role_assignment which refers to it
INSERT INTO Roles (id, description, assignedUserCount, name, role_status, role_type)
SELECT * FROM (SELECT
                   1 as id,
                   'An admin of the whole system, with full permissions over the system.' as description,
                   0 as assignedUserCount,
                   'IS Admin' as name,
                   '0' as role_status,
                   'ADMIN' as role_type) AS tmp
WHERE NOT EXISTS (
        SELECT name FROM Roles WHERE name = 'IS Admin'
    ) LIMIT 1;

-- Java Code will Add this 'Data Owner' role if it's missing but we need it here (before Java Runs) so we can add a role_assignment which refers to it
INSERT INTO Roles (id, description, assignedUserCount, name, role_status, role_type)
SELECT * FROM (SELECT
                   2 as id,
                   'An admin of a specific vault, with full permissions over that vault.' as description,
                   0 as assignedUserCount,
                   'Data Owner' as name,
                   '0' as role_status,
                   'ADMIN' as role_type) AS tmp
WHERE NOT EXISTS (
        SELECT name FROM Roles WHERE name = 'Data Owner'
    ) LIMIT 1;

insert ignore INTO Roles (id, description, assignedUserCount, name, role_status, role_type) VALUES (98, 'School Role 98', 0, 'SchoolRole98', "0", 'SCHOOL');
insert ignore INTO Roles (id, description, assignedUserCount, name, role_status, role_type) VALUES (99, 'School Role 99', 0, 'SchoolRole99', "1", 'SCHOOL');

insert ignore into Role_assignments (id,user_id,school_id,role_id) select 1,'admin1', 'grp-lfcs', R.id from Roles R where R.name = 'IS Admin';
insert ignore into Role_assignments (id,user_id,school_id,role_id) select 2,'user1',  'grp-lfcs', R.id from Roles R where R.name = 'Data Owner';

-- this prevents means new Role Ids added by Java (uses hibernate_sequence) clashing with Role Ids added by this script
update hibernate_sequence set next_val = 100 where next_val < 100;
