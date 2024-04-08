CREATE TABLE paused_state
(
    id        VARCHAR(255) NOT NULL,
    created   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_paused BOOLEAN          NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


