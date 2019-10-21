CREATE TABLE `Permissions` (
                                                                       `id` varchar(36) NOT NULL,
                                                                       `label` text NOT NULL,
                                                                       `permission` text NOT NULL,
                                                                       `type` text NOT NULL,
                                                                       PRIMARY KEY (`id`)
                                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Roles` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT,
                         `assignedUserCount` int(11) NOT NULL,
                         `description` text,
                         `name` text NOT NULL,
                         `role_type` text NOT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Role_assignments` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                    `role_id` bigint(20) NOT NULL,
                                    `school_id` varchar(180) DEFAULT NULL,
                                    `user_id` varchar(36) NOT NULL,
                                    `vault_id` varchar(36) DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `UK_inpjm7k16ph1lrue5o38yiact` (`role_id`,`user_id`,`school_id`,`vault_id`),
                                    KEY `FK_mtrvvfub5q70r6jkcw5gggg0k` (`school_id`),
                                    KEY `FK_2j93l0yx740l9r9hjeinwia1f` (`user_id`),
                                    KEY `FK_by9fc9iswmf9l5q7c243gdprl` (`vault_id`),
                                    CONSTRAINT `FK_2j93l0yx740l9r9hjeinwia1f` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`),
                                    CONSTRAINT `FK_by9fc9iswmf9l5q7c243gdprl` FOREIGN KEY (`vault_id`) REFERENCES `Vaults` (`id`),
                                    CONSTRAINT `FK_mtrvvfub5q70r6jkcw5gggg0k` FOREIGN KEY (`school_id`) REFERENCES `Groups` (`id`),
                                    CONSTRAINT `FK_spyctngrr3ou9iyr5ld3h28fs` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Role_permissions` (
                                    `role_id` bigint(20) NOT NULL,
                                    `permission_id` varchar(36) NOT NULL,
                                    KEY `FK_ack8r2th4oy2wqlf3ljutfg6s` (`permission_id`),
                                    KEY `FK_nrwjc3i7g5d4udpcpe6llcuc5` (`role_id`),
                                    CONSTRAINT `FK_ack8r2th4oy2wqlf3ljutfg6s` FOREIGN KEY (`permission_id`) REFERENCES `Permissions` (`id`),
                                    CONSTRAINT `FK_nrwjc3i7g5d4udpcpe6llcuc5` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;