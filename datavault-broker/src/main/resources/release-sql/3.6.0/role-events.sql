ALTER TABLE `Events`
ADD COLUMN `assignee_id` varchar(36) DEFAULT NULL,
ADD CONSTRAINT `FK_hisyjr4nglx5lvhn4mirqnypp` FOREIGN KEY (`assignee_id`)
REFERENCES `Users`(`id`);
ALTER TABLE `Events`
ADD COLUMN `role_id` bigint(20) DEFAULT NULL,
ADD CONSTRAINT `FK_e0c78p3nf80x77tt6dh9m3sw8` FOREIGN KEY (`role_id`)
REFERENCES `Roles`(`id`);
ALTER TABLE `Events`
ADD COLUMN `school_id` varchar(180) DEFAULT NULL,
ADD CONSTRAINT `FK_o32xd6wvetciiqew3555rm7ml` FOREIGN KEY (`school_id`)
REFERENCES `Groups`(`id`);