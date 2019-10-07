INSERT INTO Role_assignments(role_id,user_id)
SELECT
    (SELECT id FROM Roles WHERE name = 'IS Admin'),
    u.id
FROM Users u
LEFT OUTER JOIN Role_assignments ra
    on u.id = ra.user_id
LEFT OUTER JOIN Roles r
    on ra.role_id = r.id
    and r.name = 'IS Admin'
WHERE
    u.admin = 1
and ra.id IS NULL;