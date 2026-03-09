-- =========================================================

-- Schema for Account Service API (H2)

DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    account_id UUID PRIMARY KEY,
    customer_id VARCHAR(10) NOT NULL,
    account_number VARCHAR(14) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance DECIMAL(19,2) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Sugerencias de configuración Spring (application.yml):
-- spring.jpa.hibernate.ddl-auto=none
-- spring.sql.init.mode=always
-- spring.h2.console.enabled=true
