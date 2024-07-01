DROP TABLE IF EXISTS `paused_deposit_state`;

CREATE TABLE `paused_deposit_state`
(
    `id`        VARCHAR(36) NOT NULL,
    `created`   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP NULL,
    `is_paused` TINYINT(1)   NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

DROP TABLE IF EXISTS `paused_retrieve_state`;

CREATE TABLE `paused_retrieve_state`
(
    `id`        VARCHAR(36) NOT NULL,
    `created`   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP NULL,
    `is_paused` TINYINT(1)   NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

