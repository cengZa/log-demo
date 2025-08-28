DROP TABLE IF EXISTS orders;
CREATE TABLE orders(
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) UNIQUE NOT NULL,
  address  VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS op_log;
CREATE TABLE op_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant        VARCHAR(64)  NOT NULL,
  category      VARCHAR(64)  NULL,
  biz_no        VARCHAR(128) NOT NULL,
  content       VARCHAR(1024) NOT NULL,
  detail        JSON NULL,
  operator_id   VARCHAR(64)  NOT NULL,
  operator_name VARCHAR(128) NULL,
  success       TINYINT(1)   NOT NULL,
  error_msg     VARCHAR(512) NULL,
  request_id    VARCHAR(64)  NULL,
  trace_id      VARCHAR(64)  NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_biz      (tenant, biz_no, created_at),
  KEY idx_category (tenant, category, created_at),
  KEY idx_operator (tenant, operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
