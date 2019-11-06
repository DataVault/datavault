ALTER TABLE Roles ADD COLUMN role_status text;

ALTER TABLE Roles CHANGE role_status role_status text NOT NULL;