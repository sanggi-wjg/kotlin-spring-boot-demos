CREATE TABLE `order`
(
    id                     BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    status                 VARCHAR(64)    NOT NULL COMMENT '상태',

    amount                 DECIMAL(15, 0) NOT NULL COMMENT '총 금액',
    coupon_discount_amount DECIMAL(15, 0) NOT NULL COMMENT '쿠폰 할인 금액'

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '주문';

CREATE TABLE `order_item`
(
    id                     BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id             INT            NOT NULL COMMENT '상품 FK',

    amount                 DECIMAL(15, 0) NOT NULL COMMENT '상품별 금액',
    coupon_discount_amount DECIMAL(15, 0) NOT NULL COMMENT '상품별 쿠폰 할인 금액',

    CONSTRAINT `fk_order_item_001` FOREIGN KEY (product_id) REFERENCES `product` (id) ON DELETE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '주문 상품';
