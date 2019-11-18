-- add new column
-- nullable for now (as the existing data needs fixed first)

ALTER TABLE Datasets ADD crisId text;

-- fix existing entries
update Datasets set crisId = '69272279' where id = '138c6ecb-07b2-426f-81cc-69c197be6c2e';
update Datasets set crisId = '15143868' where id = '1bb95d84-4c9b-46f9-89a2-ff2cf629a24d';
update Datasets set crisId = '????????' where id = '1e3466b5-6cad-4433-984e-7ea700655cac';
update Datasets set crisId = '27569278' where id = '2716e831-287a-4815-a532-cad72fc8c3ed';
update Datasets set crisId = '????????' where id = '28add805-3244-4276-90b8-03a3cf5c758d';
update Datasets set crisId = '15144245' where id = '28c8468d-c916-47fe-913b-db2f6b285b23';
update Datasets set crisId = '????????' where id = '44a93be0-4d49-45d1-8c47-006e7e7da29e';
update Datasets set crisId = '112428063' where id = '60911ec6-2c83-4d17-942a-e43362c24fc4';
update Datasets set crisId = '????????' where id = '886bac53-cd95-4623-8480-17758cc92012';
update Datasets set crisId = '????????' where id = 'a8cf56bd-3336-411a-a5b0-947bb4b91d50';
update Datasets set crisId = '70298749' where id = 'b7db46ea-57ca-4559-8252-d9f38e85cc77';
update Datasets set crisId = '26510986' where id = 'ba4de74b-7d98-48c8-b587-c43f44dc37c9';
update Datasets set crisId = '66178816' where id = 'bcb94418-0ed3-4fd6-9881-3195fbe2deb9';
update Datasets set crisId = '????????' where id = 'c4671ee6-239a-4b38-8601-8e2f95c10d0d';
update Datasets set crisId = '????????' where id = 'cb165c4b-e004-4ece-a622-8608877486df';
update Datasets set crisId = '????????' where id = 'ced97739-1bcc-44da-ba51-552889d08c85';
update Datasets set crisId = '????????' where id = 'd010a048-7ea6-41e2-8fc1-944301536dab';
update Datasets set crisId = '107426960' where id = 'd969c58a-9d8a-4f34-ac7a-1c992b228658';
update Datasets set crisId = '112427677' where id = 'e47216dd-c2c2-49d2-92ae-a0535dcf27ad';
update Datasets set crisId = '107427002' where id = 'f23467cb-434b-4fad-8666-db07cc404bed';
update Datasets set crisId = '15143563' where id = 'fbce59b5-f9ed-42d7-9607-48aaae41cf29';
update Datasets set crisId = '21502727' where id = 'fd297498-7d0d-4d57-9040-769af9c65212';

-- add non nullable constraint to new column
ALTER TABLE Datasets CHANGE crisId crisId text NOT NULL;