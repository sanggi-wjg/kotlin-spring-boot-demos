USE ak_demo;

CREATE TABLE first_scenario_event
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    event_id   VARCHAR(100) NOT NULL UNIQUE COMMENT '이벤트 고유 ID (UUID)',
    message    VARCHAR(500) NOT NULL COMMENT '이벤트 메시지 내용',
    timestamp  DATETIME(6)  NOT NULL COMMENT '이벤트 발생 시각 (UTC)',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='첫번째 시나리오 이벤트 로그';


CREATE INDEX `idx_first_scenario_event_001` ON `first_scenario_event` (`event_id`);
