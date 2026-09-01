CREATE TABLE `order`
(
    id                     BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                INT            NOT NULL COMMENT '주문자 FK',
    status                 VARCHAR(64)    NOT NULL COMMENT '상태',
    amount                 DECIMAL(15, 0) NOT NULL COMMENT '총 금액',
    coupon_discount_amount DECIMAL(15, 0) NOT NULL COMMENT '쿠폰 할인 금액',

    created_at             DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
    updated_at             DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
    deleted_at             DATETIME(6)    NULL COMMENT '삭제일',

    CONSTRAINT `ck_order_001` CHECK ( amount >= 0 ),
    CONSTRAINT `ck_order_002` CHECK ( coupon_discount_amount >= 0 ),

    CONSTRAINT `fk_order_001` FOREIGN KEY (user_id) REFERENCES `users` (id) ON DELETE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '주문';

CREATE TABLE `order_item`
(
    id                     BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id               BIGINT         NOT NULL COMMENT '주문 FK',
    product_id             INT            NOT NULL COMMENT '상품 FK',
    quantity               BIGINT         NOT NULL COMMENT '주문 수량',
    amount                 DECIMAL(15, 0) NOT NULL COMMENT '상품별 금액',
    coupon_discount_amount DECIMAL(15, 0) NOT NULL COMMENT '상품별 쿠폰 할인 금액',

    created_at             DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
    updated_at             DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
    deleted_at             DATETIME(6)    NULL COMMENT '삭제일',

    CONSTRAINT `ck_order_item_001` CHECK ( amount >= 0 ),
    CONSTRAINT `ck_order_item_002` CHECK ( coupon_discount_amount >= 0 ),
    CONSTRAINT `ck_order_item_003` CHECK ( quantity > 0 ),

    CONSTRAINT `fk_order_item_001` FOREIGN KEY (product_id) REFERENCES `product` (id) ON DELETE RESTRICT,
    CONSTRAINT `fk_order_item_002` FOREIGN KEY (order_id) REFERENCES `order` (id) ON DELETE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '주문 상품';
