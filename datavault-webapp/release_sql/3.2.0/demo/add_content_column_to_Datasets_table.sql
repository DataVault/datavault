-- add new column
-- nullable for now (as the existing data needs fixed first)

ALTER TABLE Datasets
ADD Content longtext;

-- fix existing entries
update Datasets set content = '<dataset>Missing due to bug placeholder so not null</dataset>';


-- add non nullable constraint to new column
ALTER TABLE Datasets
CHANGE Contents Contents longtext NOT NULL;