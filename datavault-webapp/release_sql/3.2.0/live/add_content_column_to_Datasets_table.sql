-- add new column
-- nullable for now (as the existing data needs fixed first)

ALTER TABLE Datasets
ADD Content longtext;

-- fix existing entries
update Datasets set content = '' where id = '6f666eee-14ce-4de2-ae70-77eb42d63725';
update Datasets set content = '' where id = '138c6ecb-07b2-426f-81cc-69c197be6c2e';
update Datasets set content = '' where id = 'bcb94418-0ed3-4fd6-9881-3195fbe2deb9';
update Datasets set content = '' where id = 'fbce59b5-f9ed-42d7-9607-48aaae41cf29';
update Datasets set content = '' where id = '8a96caae-db2a-42c5-9361-e366faa26e3f';
update Datasets set content = '' where id = '2716e831-287a-4815-a532-cad72fc8c3ed';
update Datasets set content = '' where id = '499f2a33-ac66-4ad8-b1d4-03e7414a78fe';
update Datasets set content = '' where id = '7d6ce345-9a6c-4330-9b16-9d21f2f550bc';
update Datasets set content = '' where id = 'fbce59b5-f9ed-42d7-9607-48aaae41cf29';


-- add non nullable constraint to new column
ALTER TABLE Datasets
CHANGE Contents Contents longtext NOT NULL;

