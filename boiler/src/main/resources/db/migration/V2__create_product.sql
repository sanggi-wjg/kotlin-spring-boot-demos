CREATE TABLE `product`
(
    id             INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(64)    NOT NULL COMMENT '이름',
    price          DECIMAL(15, 0) NOT NULL COMMENT '가격 (원)',
    stock_quantity BIGINT         NOT NULL DEFAULT 0 COMMENT '재고 수량',

    created_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
    updated_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
    deleted_at     DATETIME(6)    NULL COMMENT '삭제일'

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '상품';
