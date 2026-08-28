CREATE TABLE `coupon_scheme`
(
    id                  INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    discount_type       VARCHAR(64)    NOT NULL COMMENT '할인 유형 (AMOUNT, RATE)',
    discount_amount     DECIMAL(15, 0) NULL COMMENT '정액 할인액 (원, AMOUNT 시 필수)',
    discount_rate       DECIMAL(5, 2)  NULL COMMENT '정률 할인률 % (RATE 시 필수, 0.01 ~ 100.00)',
    max_discount_amount DECIMAL(15, 0) NULL COMMENT '최대 할인 금액 (원, 정률 할인에서 사용)',
    min_order_amount    DECIMAL(15, 0) NOT NULL DEFAULT 0 COMMENT '최소 주문 금액 (원)',

    using_started_at    DATETIME(6)    NOT NULL COMMENT '사용 시작일',
    using_expired_at    DATETIME(6)    NOT NULL COMMENT '사용 만료일',
    max_issue_count     INT            NOT NULL COMMENT '최대 발급 수량',
    current_issue_count INT            NOT NULL DEFAULT 0 COMMENT '현재 발급 수량'

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '쿠폰스킴';


CREATE TABLE `coupon`
(
    id               BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    coupon_scheme_id INT         NOT NULL COMMENT '쿠폰 스킴 FK',
    user_id          INT         NULL COMMENT '유저 FK',
    started_at       DATETIME(6) NOT NULL COMMENT '사용 시작일',
    expired_at       DATETIME(6) NOT NULL COMMENT '사용 만료일',
    used_at          DATETIME(6) NULL COMMENT '사용일',
    order_id         BIGINT      NULL COMMENT '사용 주문 FK',

    UNIQUE KEY `uq_coupon_001` (`coupon_scheme_id`, `user_id`),

    CONSTRAINT `fk_coupon_001` FOREIGN KEY (coupon_scheme_id) REFERENCES `coupon_scheme` (id) ON DELETE RESTRICT,
    CONSTRAINT `fk_coupon_002` FOREIGN KEY (user_id) REFERENCES `users` (id) ON DELETE RESTRICT,
    CONSTRAINT `fk_coupon_003` FOREIGN KEY (order_id) REFERENCES `order` (id) ON DELETE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '쿠폰';
