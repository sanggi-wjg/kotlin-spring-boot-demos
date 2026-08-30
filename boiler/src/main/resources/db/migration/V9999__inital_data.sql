SET FOREIGN_KEY_CHECKS = 0;

truncate users;

INSERT INTO users (name, is_admin)
VALUES ('iam_admin', true),
       ('iam_noop', false);

SET FOREIGN_KEY_CHECKS = 1;
