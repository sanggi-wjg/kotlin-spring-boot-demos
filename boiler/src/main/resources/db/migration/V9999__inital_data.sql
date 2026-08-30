truncate users;

INSERT INTO users (name, is_admin)
VALUES ('iam_admin', true),
       ('iam_noop', false);
