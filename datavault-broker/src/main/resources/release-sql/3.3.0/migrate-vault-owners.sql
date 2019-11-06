SET @dataOwnerId = (SELECT id FROM Roles WHERE name='Data Owner');
INSERT INTO Role_assignments (role_id, user_id, vault_id)
SELECT @dataOwnerId, user_id, id FROM Vaults;
