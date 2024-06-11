alter table `Deposits` ROW_FORMAT=DYNAMIC;
alter table `Deposits` ADD `non_restart_job_id` VARCHAR(256) DEFAULT NULL;
