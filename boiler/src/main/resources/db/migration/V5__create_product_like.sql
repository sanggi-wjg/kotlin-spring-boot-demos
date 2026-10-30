CREATE TABLE `product_like`
(
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id INT         NOT NULL COMMENT '상품 FK',
    user_id    INT         NOT NULL COMMENT '유저 FK',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',

    UNIQUE KEY `uq_product_like_001` (`product_id`, `user_id`),

    CONSTRAINT `fk_product_like_001` FOREIGN KEY (product_id) REFERENCES `product` (id) ON DELETE RESTRICT,
    CONSTRAINT `fk_product_like_002` FOREIGN KEY (user_id) REFERENCES `users` (id) ON DELETE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '쿠폰';
