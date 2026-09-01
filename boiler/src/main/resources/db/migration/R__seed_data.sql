INSERT INTO users (id, name, is_admin)
VALUES (1, 'iam_admin', true),
       (2, 'iam_noop', false)
ON DUPLICATE KEY UPDATE name     = VALUES(name),
                        is_admin = VALUES(is_admin);
