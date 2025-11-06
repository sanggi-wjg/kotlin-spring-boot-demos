USE ak_demo;

CREATE TABLE event
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    event_id   VARCHAR(100) NOT NULL UNIQUE COMMENT '이벤트 고유 ID (UUID)',
    message    VARCHAR(500) NOT NULL COMMENT '이벤트 메시지 내용',
    timestamp  DATETIME(6)  NOT NULL COMMENT '이벤트 발생 시각 (UTC)',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='이벤트 줍줍';

CREATE INDEX `idx_event_001` ON `event` (`event_id`);

CREATE TABLE dead_letter
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    letter     JSON        NOT NULL COMMENT '이벤트 메시지',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일'
);
