-- add new column
-- nullable for now (as the existing data needs fixed first)

ALTER TABLE Vaults
ADD snapshot longtext;

-- fix existing entries



-- add non nullable constraint to new column
ALTER TABLE Vaults
CHANGE snapshot snapshot longtext NOT NULL;

