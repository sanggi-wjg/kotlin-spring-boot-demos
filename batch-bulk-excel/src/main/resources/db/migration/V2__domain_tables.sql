CREATE TABLE users
(
    id   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE excel_request
(
    id                     VARCHAR(36) NOT NULL PRIMARY KEY,
    excel_request_type     VARCHAR(50) NOT NULL,
    status                 VARCHAR(20) NOT NULL,
    params                 JSON,
    input_file_url         VARCHAR(512),
    result_file_url        VARCHAR(512),
    error_report_url       VARCHAR(512),
    result_summary         JSON,
    batch_job_execution_id BIGINT,
    started_at             DATETIME(6),
    finished_at            DATETIME(6),

    created_at             DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at             DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_status_created (status, created_at)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE user_mileage
(
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    balance    BIGINT      NOT NULL DEFAULT 0,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT uk_user UNIQUE (user_id),
    CONSTRAINT fk_mileage_user FOREIGN KEY (user_id) REFERENCES users (id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE user_mileage_history
(
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_mileage_id BIGINT      NOT NULL,
    amount          BIGINT      NOT NULL,
    reason          VARCHAR(255),
    job_id          VARCHAR(36),

    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_mileage (user_mileage_id),
    CONSTRAINT fk_hist_mileage FOREIGN KEY (user_mileage_id) REFERENCES user_mileage (id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE orders
(
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    total_price BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL,

    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE order_item
(
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT       NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity     INT          NOT NULL,
    price        BIGINT       NOT NULL,

    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_order (order_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
