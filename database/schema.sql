-- Drop existing database if exists
DROP DATABASE IF EXISTS fortis_atm;
CREATE DATABASE fortis_atm;
USE fortis_atm;

-- ============================================================================
-- TABLE 1: USERS
-- Stores user credentials and authentication data
-- ============================================================================
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
    status ENUM('ACTIVE', 'LOCKED', 'SUSPENDED') DEFAULT 'ACTIVE',
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_status (status)
);

-- ============================================================================
-- TABLE 2: ACCOUNTS
-- Bank account information
-- ============================================================================
CREATE TABLE accounts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    user_id INT NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT', 'SALARY') DEFAULT 'SAVINGS',
    balance DECIMAL(15, 2) DEFAULT 0.00,
    daily_limit DECIMAL(10, 2) DEFAULT 40000.00,
    daily_withdrawn DECIMAL(10, 2) DEFAULT 0.00,
    last_withdrawal_date DATE NULL,
    status ENUM('ACTIVE', 'FROZEN', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_account_number (account_number),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- ============================================================================
-- TABLE 3: TRANSACTIONS
-- All banking transactions
-- ============================================================================
CREATE TABLE transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    txn_id VARCHAR(50) UNIQUE NOT NULL,
    from_account_id INT NULL,
    to_account_id INT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'BALANCE_INQUIRY') NOT NULL,
    status ENUM('SUCCESS', 'FAILED', 'PENDING') DEFAULT 'PENDING',
    description VARCHAR(255) NULL,
    balance_after DECIMAL(15, 2) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    INDEX idx_txn_id (txn_id),
    INDEX idx_from_account (from_account_id),
    INDEX idx_to_account (to_account_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
);

-- ============================================================================
-- TABLE 4: ADMIN_LOGS
-- Audit trail for admin actions
-- ============================================================================
CREATE TABLE admin_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id INT NULL,
    details TEXT NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_admin_id (admin_id),
    INDEX idx_created_at (created_at)
);

-- ============================================================================
-- TABLE 5: ATM_CASH
-- ATM cash inventory management
-- ============================================================================
CREATE TABLE atm_cash (
    id INT PRIMARY KEY AUTO_INCREMENT,
    denomination INT NOT NULL,
    quantity INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by INT NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_denomination (denomination),
    INDEX idx_denomination (denomination)
);

-- ============================================================================
-- TABLE 6: FRAUD_ALERTS
-- Suspicious activity detection
-- ============================================================================
CREATE TABLE fraud_alerts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    alert_type ENUM('RAPID_WITHDRAWAL', 'HIGH_VALUE', 'UNUSUAL_PATTERN', 'MULTIPLE_FAILED_LOGIN') NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    details TEXT NULL,
    status ENUM('OPEN', 'INVESTIGATING', 'RESOLVED', 'FALSE_POSITIVE') DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    INDEX idx_account_id (account_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- ============================================================================
-- TABLE 7: SESSION_LOGS
-- Track user sessions
-- ============================================================================
CREATE TABLE session_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    session_id VARCHAR(100) UNIQUE NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    transactions_count INT DEFAULT 0,
    ip_address VARCHAR(45) NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id)
);

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Trigger 1: Auto-reset daily withdrawal limit
DELIMITER //
CREATE TRIGGER reset_daily_limit
BEFORE UPDATE ON accounts
FOR EACH ROW
BEGIN
    IF NEW.last_withdrawal_date IS NULL OR NEW.last_withdrawal_date < CURDATE() THEN
        SET NEW.daily_withdrawn = 0;
        SET NEW.last_withdrawal_date = CURDATE();
    END IF;
END//
DELIMITER ;

-- Trigger 2: Log high-value transactions as potential fraud
DELIMITER //
CREATE TRIGGER check_high_value_transaction
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    IF NEW.amount > 50000 AND NEW.transaction_type IN ('WITHDRAWAL', 'TRANSFER') THEN
        INSERT INTO fraud_alerts (account_id, alert_type, severity, details)
        VALUES (
            NEW.from_account_id,
            'HIGH_VALUE',
            'MEDIUM',
            CONCAT('High-value transaction: ₹', NEW.amount, ' - TXN: ', NEW.txn_id)
        );
    END IF;
END//
DELIMITER ;

-- Trigger 3: Detect rapid withdrawals (fraud detection)
DELIMITER //
CREATE TRIGGER detect_rapid_withdrawals
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    DECLARE recent_count INT;
    
    IF NEW.transaction_type = 'WITHDRAWAL' THEN
        SELECT COUNT(*) INTO recent_count
        FROM transactions
        WHERE from_account_id = NEW.from_account_id
        AND transaction_type = 'WITHDRAWAL'
        AND created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);
        
        IF recent_count >= 5 THEN
            INSERT INTO fraud_alerts (account_id, alert_type, severity, details)
            VALUES (
                NEW.from_account_id,
                'RAPID_WITHDRAWAL',
                'HIGH',
                CONCAT('Multiple withdrawals detected: ', recent_count, ' in last hour')
            );
        END IF;
    END IF;
END//
DELIMITER ;

-- ============================================================================
-- STORED PROCEDURES
-- ============================================================================

-- Procedure 1: Withdraw Money
DELIMITER //
CREATE PROCEDURE sp_withdraw(
    IN p_account_id INT,
    IN p_amount DECIMAL(15,2),
    IN p_description VARCHAR(255),
    OUT p_txn_id VARCHAR(50),
    OUT p_status VARCHAR(20),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_balance DECIMAL(15,2);
    DECLARE v_daily_withdrawn DECIMAL(15,2);
    DECLARE v_daily_limit DECIMAL(15,2);
    DECLARE v_account_status VARCHAR(20);
    DECLARE v_new_balance DECIMAL(15,2);
    
    START TRANSACTION;
    
    SELECT balance, daily_withdrawn, daily_limit, status
    INTO v_balance, v_daily_withdrawn, v_daily_limit, v_account_status
    FROM accounts
    WHERE id = p_account_id
    FOR UPDATE;
    -- Check account status
    IF v_account_status != 'ACTIVE' THEN
        SET p_status = 'FAILED';
        SET p_message = 'Account is not active';
        ROLLBACK;
    -- Check sufficient balance
    ELSEIF v_balance < p_amount THEN
        SET p_status = 'FAILED';
        SET p_message = 'Insufficient funds';
        ROLLBACK;
    -- Check daily limit
    ELSEIF (v_daily_withdrawn + p_amount) > v_daily_limit THEN
        SET p_status = 'FAILED';
        SET p_message = CONCAT('Daily limit exceeded. Remaining: ₹', (v_daily_limit - v_daily_withdrawn));
        ROLLBACK;
    ELSE
        -- Generate transaction ID
        SET p_txn_id = CONCAT('TXN', UNIX_TIMESTAMP(), '-', SUBSTRING(MD5(RAND()), 1, 8));
        
        -- Calculate new balance
        SET v_new_balance = v_balance - p_amount;
        
        -- Update account
        UPDATE accounts
        SET balance = v_new_balance,
            daily_withdrawn = daily_withdrawn + p_amount,
            last_withdrawal_date = CURDATE()
        WHERE id = p_account_id;
        
        -- Insert transaction
        INSERT INTO transactions (txn_id, from_account_id, amount, transaction_type, status, description, balance_after)
        VALUES (p_txn_id, p_account_id, p_amount, 'WITHDRAWAL', 'SUCCESS', p_description, v_new_balance);
        
        SET p_status = 'SUCCESS';
        SET p_message = CONCAT('Withdrawal successful. New balance: ₹', v_new_balance);
        
        COMMIT;
    END IF;
END//
DELIMITER ;

-- Procedure 2: Deposit Money
DELIMITER //
CREATE PROCEDURE sp_deposit(
    IN p_account_id INT,
    IN p_amount DECIMAL(15,2),
    IN p_description VARCHAR(255),
    OUT p_txn_id VARCHAR(50),
    OUT p_status VARCHAR(20),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_balance DECIMAL(15,2);
    DECLARE v_account_status VARCHAR(20);
    DECLARE v_new_balance DECIMAL(15,2);
    
    START TRANSACTION;
    
    SELECT balance, status INTO v_balance, v_account_status
    FROM accounts
    WHERE id = p_account_id
    FOR UPDATE;
    
    IF v_account_status != 'ACTIVE' THEN
        SET p_status = 'FAILED';
        SET p_message = 'Account is not active';
        ROLLBACK;
    ELSE
        SET p_txn_id = CONCAT('TXN', UNIX_TIMESTAMP(), '-', SUBSTRING(MD5(RAND()), 1, 8));
        SET v_new_balance = v_balance + p_amount;
        
        UPDATE accounts SET balance = v_new_balance WHERE id = p_account_id;
        
        INSERT INTO transactions (txn_id, to_account_id, amount, transaction_type, status, description, balance_after)
        VALUES (p_txn_id, p_account_id, p_amount, 'DEPOSIT', 'SUCCESS', p_description, v_new_balance);
        
        SET p_status = 'SUCCESS';
        SET p_message = CONCAT('Deposit successful. New balance: ₹', v_new_balance);
        
        COMMIT;
    END IF;
END//
DELIMITER ;

-- Procedure 3: Transfer Money
DELIMITER //
CREATE PROCEDURE sp_transfer(
    IN p_from_account_id INT,
    IN p_to_account_id INT,
    IN p_amount DECIMAL(15,2),
    IN p_description VARCHAR(255),
    OUT p_txn_id VARCHAR(50),
    OUT p_status VARCHAR(20),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_from_balance DECIMAL(15,2);
    DECLARE v_to_balance DECIMAL(15,2);
    DECLARE v_from_status VARCHAR(20);
    DECLARE v_to_status VARCHAR(20);
    
    START TRANSACTION;
    
    SELECT balance, status INTO v_from_balance, v_from_status
    FROM accounts WHERE id = p_from_account_id FOR UPDATE;
    
    SELECT balance, status INTO v_to_balance, v_to_status
    FROM accounts WHERE id = p_to_account_id FOR UPDATE;
    
    IF v_from_status != 'ACTIVE' OR v_to_status != 'ACTIVE' THEN
        SET p_status = 'FAILED';
        SET p_message = 'One or both accounts are not active';
        ROLLBACK;
    ELSEIF v_from_balance < p_amount THEN
        SET p_status = 'FAILED';
        SET p_message = 'Insufficient funds';
        ROLLBACK;
    ELSE
        SET p_txn_id = CONCAT('TXN', UNIX_TIMESTAMP(), '-', SUBSTRING(MD5(RAND()), 1, 8));
        
        UPDATE accounts SET balance = balance - p_amount WHERE id = p_from_account_id;
        UPDATE accounts SET balance = balance + p_amount WHERE id = p_to_account_id;
        
        INSERT INTO transactions (txn_id, from_account_id, to_account_id, amount, transaction_type, status, description, balance_after)
        VALUES (p_txn_id, p_from_account_id, p_to_account_id, p_amount, 'TRANSFER', 'SUCCESS', p_description, v_from_balance - p_amount);
        
        SET p_status = 'SUCCESS';
        SET p_message = 'Transfer successful';
        
        COMMIT;
    END IF;
END//
DELIMITER ;

-- Procedure 4: Get Account Summary
DELIMITER //
CREATE PROCEDURE sp_get_account_summary(IN p_account_id INT)
BEGIN
    SELECT 
        a.account_number,
        a.account_type,
        a.balance,
        a.daily_limit,
        a.daily_withdrawn,
        a.status,
        u.username,
        u.role,
        COUNT(t.id) as total_transactions,
        COALESCE(SUM(CASE WHEN t.transaction_type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) as total_deposits,
        COALESCE(SUM(CASE WHEN t.transaction_type = 'WITHDRAWAL' THEN t.amount ELSE 0 END), 0) as total_withdrawals
    FROM accounts a
    JOIN users u ON a.user_id = u.id
    LEFT JOIN transactions t ON (t.from_account_id = a.id OR t.to_account_id = a.id)
    WHERE a.id = p_account_id
    GROUP BY a.id;
END//
DELIMITER ;

-- Procedure 5: Lock Account After Failed Attempts
DELIMITER //
CREATE PROCEDURE sp_lock_account_after_failed_login(IN p_username VARCHAR(50))
BEGIN
    UPDATE users
    SET status = 'LOCKED',
        locked_until = DATE_ADD(NOW(), INTERVAL 30 MINUTE)
    WHERE username = p_username;
END//
DELIMITER ;

-- ============================================================================
-- VIEWS
-- ============================================================================

-- View 1: Account Overview
CREATE VIEW vw_account_overview AS
SELECT 
    a.id,
    a.account_number,
    u.username,
    a.account_type,
    a.balance,
    a.status,
    a.created_at
FROM accounts a
JOIN users u ON a.user_id = u.id;

-- View 2: Recent Transactions
CREATE VIEW vw_recent_transactions AS
SELECT 
    t.txn_id,
    t.transaction_type,
    t.amount,
    t.status,
    t.created_at,
    fa.account_number as from_account,
    ta.account_number as to_account
FROM transactions t
LEFT JOIN accounts fa ON t.from_account_id = fa.id
LEFT JOIN accounts ta ON t.to_account_id = ta.id
ORDER BY t.created_at DESC
LIMIT 100;

-- View 3: Fraud Alert Dashboard
CREATE VIEW vw_fraud_dashboard AS
SELECT 
    f.id,
    a.account_number,
    u.username,
    f.alert_type,
    f.severity,
    f.status,
    f.details,
    f.created_at
FROM fraud_alerts f
JOIN accounts a ON f.account_id = a.id
JOIN users u ON a.user_id = u.id
WHERE f.status = 'OPEN'
ORDER BY f.severity DESC, f.created_at DESC;

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Additional composite indexes
CREATE INDEX idx_txn_date_type ON transactions(created_at, transaction_type);
CREATE INDEX idx_account_user_status ON accounts(user_id, status);
CREATE INDEX idx_fraud_account_status ON fraud_alerts(account_id, status);

-- ============================================================================
-- DATABASE SCHEMA CREATED SUCCESSFULLY
-- ============================================================================
