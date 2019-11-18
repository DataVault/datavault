-- add new column
-- nullable for now (as the existing data needs fixed first)

ALTER TABLE Datasets ADD crisId text;

-- fix existing entries
update Datasets set crisId = '96299082' where id = '098f8866-fa11-4ee8-bba0-77f2eb467de2';
update Datasets set crisId = '96497563' where id = '124e971e-16c2-4724-8be3-f3b8fbe0cfa0';
update Datasets set crisId = '69272279' where id = '138c6ecb-07b2-426f-81cc-69c197be6c2e';
update Datasets set crisId = '15143868' where id = '1bb95d84-4c9b-46f9-89a2-ff2cf629a24d';
update Datasets set crisId = '27569278' where id = '2716e831-287a-4815-a532-cad72fc8c3ed';
update Datasets set crisId = '118988138' where id = '28c2fe80-6bba-448b-ab88-e257964d7e1c';
update Datasets set crisId = '118987009' where id = '412d6606-fb90-485b-9f37-27f1c05e0383';
update Datasets set crisId = '96302395' where id = '473955c0-a38b-49a3-99e8-c3b7d0ee200b';
update Datasets set crisId = '81930562' where id = '499f2a33-ac66-4ad8-b1d4-03e7414a78fe';
update Datasets set crisId = '96313920' where id = '53f83878-bff6-4d53-87ef-f61320dde2e3';
update Datasets set crisId = '107668159' where id = '5acc1997-69be-496b-964b-f22ad06456d8';
update Datasets set crisId = '96291574' where id = '5c5c5100-4d40-45fa-8a8f-2611909a365e';
update Datasets set crisId = '118988334' where id = '5e53b5ad-35b1-4371-b879-275fa217912a';
update Datasets set crisId = '96498102' where id = '5f2ecd7e-11c1-4c59-be66-a6532c2d2fa0';
update Datasets set crisId = '96291812' where id = '6655e116-f95a-43ae-ab60-26c358b63101';
update Datasets set crisId = '96497902' where id = '6c6f0c5a-e89d-47a9-b279-a72059a72c9b';
update Datasets set crisId = '80058178' where id = '6f666eee-14ce-4de2-ae70-77eb42d63725';
update Datasets set crisId = '103714462' where id = '6fee4875-894f-4613-a861-59d5e46d8598';
update Datasets set crisId = '107532590' where id = '758c86a0-765c-495d-b776-f728c5008579';
update Datasets set crisId = '75726153' where id = '7d6ce345-9a6c-4330-9b16-9d21f2f550bc';
update Datasets set crisId = '111036721' where id = '84b585fc-57d2-4e5a-b3a3-694f70534a02';
update Datasets set crisId = '96285610' where id = '86072ae7-5d36-4c86-bdd2-ce53d007ae5e';
update Datasets set crisId = '82482794' where id = '8a96caae-db2a-42c5-9361-e366faa26e3f';
update Datasets set crisId = '112097106' where id = '9421e3b6-082a-44a4-8638-23e8922d18f1';
update Datasets set crisId = '27544982' where id = '9f7b49f8-e895-45dd-b982-4fcef0d4f426';
update Datasets set crisId = '33302243' where id = 'a2e7c6ae-0502-4a56-bddd-9325fbed7637';
update Datasets set crisId = '96314371' where id = 'a98bc747-c971-4c3d-b160-d4092fe6bf0c';
update Datasets set crisId = '96285220' where id = 'aa154c4b-0992-4592-95b2-b0734eeb15b0';
update Datasets set crisId = '26510986?' where id = 'ba4de74b-7d98-48c8-b587-c43f44dc37c9';
update Datasets set crisId = '66178816' where id = 'bcb94418-0ed3-4fd6-9881-3195fbe2deb9';
update Datasets set crisId = '105067103' where id = 'c19755a2-7306-439c-aaaa-0490136767e1';
update Datasets set crisId = '????????' where id = 'c4671ee6-239a-4b38-8601-8e2f95c10d0d';
update Datasets set crisId = '96291261' where id = 'c589ba9e-f1c4-4915-bc8e-931a82875c32';
update Datasets set crisId = '????????' where id = 'cb165c4b-e004-4ece-a622-8608877486df';
update Datasets set crisId = '????????' where id = 'ced97739-1bcc-44da-ba51-552889d08c85';
update Datasets set crisId = '????????' where id = 'd010a048-7ea6-41e2-8fc1-944301536dab';
update Datasets set crisId = '96285783' where id = 'd284fd6e-c01e-4348-985a-a216824968e5';
update Datasets set crisId = '96498191' where id = 'd9923091-bd5c-420b-8430-e9fdf95b84cb';
update Datasets set crisId = '103926175' where id = 'daa2a13f-4748-4d16-9e15-9a7f304618a2';
update Datasets set crisId = '118987950' where id = 'eaeb2941-9bc7-49ba-b407-9a4ff1837b97';
update Datasets set crisId = '107668235' where id = 'ec08c5d0-7251-40f7-865b-043c2d2dc32c';
update Datasets set crisId = '118988041' where id = 'f293e581-723a-464a-b5d5-56b207d46739';
update Datasets set crisId = '15143563' where id = 'fbce59b5-f9ed-42d7-9607-48aaae41cf29';
update Datasets set crisId = '21502727' where id = 'fd297498-7d0d-4d57-9040-769af9c65212';
-- update Datasets set crisId = '69272279' where id = '138c6ecb-07b2-426f-81cc-69c197be6c2e';


-- add non nullable constraint to new column
ALTER TABLE Datasets CHANGE crisId crisId text NOT NULL;