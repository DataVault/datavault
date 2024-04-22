DROP TABLE IF EXISTS paused_deposit_state;

CREATE TABLE paused_deposit_state
(
    id        VARCHAR(255) NOT NULL,
    created   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_paused BOOLEAN          NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

DROP TABLE IF EXISTS paused_retrieve_state;

CREATE TABLE paused_retrieve_state
(
    id        VARCHAR(255) NOT NULL,
    created   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_paused BOOLEAN          NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

