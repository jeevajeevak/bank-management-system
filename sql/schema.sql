-- ============================================================
--  Bank Management System - MySQL Schema
--  Run this file first before launching the application
-- ============================================================

CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

-- ── Customers ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    customer_id   INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    phone         VARCHAR(15)  NOT NULL,
    address       TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Accounts ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS accounts (
    account_id     INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(12) UNIQUE NOT NULL,
    customer_id    INT NOT NULL,
    account_type   ENUM('SAVINGS','CURRENT','FIXED_DEPOSIT') NOT NULL,
    balance        DECIMAL(15,2) DEFAULT 0.00,
    status         ENUM('ACTIVE','FROZEN','CLOSED') DEFAULT 'ACTIVE',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ── Transactions ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    account_id       INT NOT NULL,
    type             ENUM('DEPOSIT','WITHDRAWAL','TRANSFER_IN','TRANSFER_OUT') NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    description      VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- ── Loans ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS loans (
    loan_id         INT AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT NOT NULL,
    loan_type       ENUM('HOME','CAR','PERSONAL','EDUCATION') NOT NULL,
    principal       DECIMAL(15,2) NOT NULL,
    interest_rate   DECIMAL(5,2)  NOT NULL,   -- annual %
    duration_months INT NOT NULL,
    emi             DECIMAL(15,2) NOT NULL,
    amount_paid     DECIMAL(15,2) DEFAULT 0.00,
    status          ENUM('PENDING','ACTIVE','CLOSED','REJECTED') DEFAULT 'PENDING',
    start_date      DATE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ── Seed demo data ───────────────────────────────────────────
INSERT IGNORE INTO customers (full_name, email, phone, address) VALUES
  ('Alice Johnson', 'alice@example.com', '9876543210', '12 MG Road, Bangalore'),
  ('Bob Smith',     'bob@example.com',   '9123456780', '45 Park Street, Mumbai');

INSERT IGNORE INTO accounts (account_number, customer_id, account_type, balance) VALUES
  ('ACC000000001', 1, 'SAVINGS', 50000.00),
  ('ACC000000002', 2, 'CURRENT', 120000.00);
