-- add new column
-- nullable for now (as the existing data needs fixed first)

ALTER TABLE Vaults ADD snapshot longtext;

-- fix existing entries
update Vaults set snapshot = '<dataset>Missing due to bug placeholder so not null</dataset>';


-- add non nullable constraint to new column
ALTER TABLE Vaults CHANGE snapshot snapshot longtext NOT NULL;