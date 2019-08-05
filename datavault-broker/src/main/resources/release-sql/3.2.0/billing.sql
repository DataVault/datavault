CREATE TABLE BillingInfo (
    id varchar(36) NOT NULL,
    amountBilled decimal(15,2) DEFAULT '0.00',
    amountToBeBilled decimal(15,2) DEFAULT '0.00',
    budgetCode bit(1) DEFAULT NULL,
    contactName text,  school text,
    specialComments text,
    subUnit text,
    version bigint(20) NOT NULL,
    vaultID varchar(36) NOT NULL,
    PRIMARY KEY (id),
    KEY FK_eb8b8ksy2fq52e8lxsr3gdku6 (vaultID),
    CONSTRAINT FK_eb8b8ksy2fq52e8lxsr3gdku6 FOREIGN KEY (vaultID) REFERENCES Vaults (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4