CREATE TABLE users
(
    id       INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL COMMENT '이름',
    is_admin BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '관리자 여부'

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '유저';

INSERT INTO users (name, is_admin)
VALUES ('iam_admin', TRUE),
       ('iam_user', FALSE),
       ('iam_noop', FALSE),
       ('iam_test', FALSE);
