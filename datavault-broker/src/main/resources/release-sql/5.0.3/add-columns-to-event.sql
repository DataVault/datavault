alter table `Events` ROW_FORMAT=DYNAMIC;
alter table `Events` ADD `chunkNumber` INT NULL, ADD `archive_store_id` varchar(256) NULL;


