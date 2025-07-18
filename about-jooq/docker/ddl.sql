CREATE DATABASE IF NOT EXISTS jooq;
USE jooq;

DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `product`;

CREATE TABLE `product`
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)   NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `order`
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    order_status VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at   TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `order_item`
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    order_id   INT            NOT NULL,
    product_id INT            NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order` (id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
