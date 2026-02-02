-- ============================================================================
-- FORTIS ATM SYSTEM - SAMPLE DATA
-- Test data for development and demonstration
-- ============================================================================

USE fortis_atm;

-- ============================================================================
-- INSERT USERS
-- Password hashing will be done by Java application
-- For testing: admin/1234, john_doe/5678, jane_smith/9012
-- ============================================================================

-- Admin user (PIN: 1234)
-- Hash generated using BCrypt for "1234"
INSERT INTO users (username, pin_hash, role, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVE');

-- Customer users
-- john_doe (PIN: 5678)
INSERT INTO users (username, pin_hash, role, status) VALUES
('john_doe', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CUSTOMER', 'ACTIVE');

-- jane_smith (PIN: 9012)
INSERT INTO users (username, pin_hash, role, status) VALUES
('jane_smith', '$2a$10$Xl0yhvzLIxp71rJkxmZjxeJmVHQvKppoLz6Y3YhKZXvP8xKvLhLGi', 'CUSTOMER', 'ACTIVE');

-- Additional test users
INSERT INTO users (username, pin_hash, role, status) VALUES
('alice_wonder', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CUSTOMER', 'ACTIVE'),
('bob_builder', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CUSTOMER', 'ACTIVE');

-- ============================================================================
-- INSERT ACCOUNTS
-- ============================================================================

-- Admin account
INSERT INTO accounts (account_number, user_id, account_type, balance, daily_limit, status) VALUES
('ACC1001', 1, 'SAVINGS', 100000.00, 50000.00, 'ACTIVE');

-- John Doe's accounts
INSERT INTO accounts (account_number, user_id, account_type, balance, daily_limit, status) VALUES
('ACC1002', 2, 'SAVINGS', 50000.00, 40000.00, 'ACTIVE'),
('ACC1003', 2, 'CURRENT', 25000.00, 40000.00, 'ACTIVE');

-- Jane Smith's account
INSERT INTO accounts (account_number, user_id, account_type, balance, daily_limit, status) VALUES
('ACC1004', 3, 'CURRENT', 75000.00, 40000.00, 'ACTIVE');

-- Alice Wonder's account
INSERT INTO accounts (account_number, user_id, account_type, balance, daily_limit, status) VALUES
('ACC1005', 4, 'SAVINGS', 120000.00, 40000.00, 'ACTIVE');

-- Bob Builder's account
INSERT INTO accounts (account_number, user_id, account_type, balance, daily_limit, status) VALUES
('ACC1006', 5, 'SALARY', 35000.00, 40000.00, 'ACTIVE');

-- ============================================================================
-- INSERT ATM CASH INVENTORY
-- ============================================================================

INSERT INTO atm_cash (denomination, quantity, updated_by) VALUES
(100, 500, 1),    -- ₹100 x 500 = ₹50,000
(200, 400, 1),    -- ₹200 x 400 = ₹80,000
(500, 600, 1),    -- ₹500 x 600 = ₹300,000
(2000, 200, 1);   -- ₹2000 x 200 = ₹400,000
-- Total ATM Cash: ₹830,000

-- ============================================================================
-- INSERT SAMPLE TRANSACTIONS (Historical Data)
-- ============================================================================

-- Deposits
INSERT INTO transactions (txn_id, to_account_id, amount, transaction_type, status, description, balance_after, created_at) VALUES
('TXN1704067200-ABC123', 2, 10000.00, 'DEPOSIT', 'SUCCESS', 'Cash deposit', 60000.00, '2024-01-01 10:30:00'),
('TXN1704153600-DEF456', 3, 15000.00, 'DEPOSIT', 'SUCCESS', 'Cheque deposit', 90000.00, '2024-01-02 14:20:00'),
('TXN1704240000-GHI789', 4, 5000.00, 'DEPOSIT', 'SUCCESS', 'Cash deposit', 80000.00, '2024-01-03 09:15:00');

-- Withdrawals
INSERT INTO transactions (txn_id, from_account_id, amount, transaction_type, status, description, balance_after, created_at) VALUES
('TXN1704326400-JKL012', 2, 5000.00, 'WITHDRAWAL', 'SUCCESS', 'ATM withdrawal', 55000.00, '2024-01-04 11:45:00'),
('TXN1704412800-MNO345', 4, 10000.00, 'WITHDRAWAL', 'SUCCESS', 'ATM withdrawal', 70000.00, '2024-01-05 16:30:00'),
('TXN1704499200-PQR678', 5, 8000.00, 'WITHDRAWAL', 'SUCCESS', 'ATM withdrawal', 112000.00, '2024-01-06 13:10:00');

-- Transfers
INSERT INTO transactions (txn_id, from_account_id, to_account_id, amount, transaction_type, status, description, balance_after, created_at) VALUES
('TXN1704585600-STU901', 2, 4, 15000.00, 'TRANSFER', 'SUCCESS', 'Payment to Jane', 40000.00, '2024-01-07 10:00:00'),
('TXN1704672000-VWX234', 4, 2, 5000.00, 'TRANSFER', 'SUCCESS', 'Refund', 80000.00, '2024-01-08 15:20:00'),
('TXN1704758400-YZA567', 5, 6, 20000.00, 'TRANSFER', 'SUCCESS', 'Salary transfer', 100000.00, '2024-01-09 09:30:00');

-- Failed transactions (for testing)
INSERT INTO transactions (txn_id, from_account_id, amount, transaction_type, status, description, created_at) VALUES
('TXN1704844800-BCD890', 2, 100000.00, 'WITHDRAWAL', 'FAILED', 'Insufficient funds', '2024-01-10 12:00:00'),
('TXN1704931200-EFG123', 3, 50000.00, 'WITHDRAWAL', 'FAILED', 'Daily limit exceeded', '2024-01-11 14:30:00');

-- Recent transactions (last 7 days)
INSERT INTO transactions (txn_id, from_account_id, amount, transaction_type, status, description, balance_after, created_at) VALUES
('TXN1735891200-HIJ456', 2, 3000.00, 'WITHDRAWAL', 'SUCCESS', 'ATM withdrawal', 47000.00, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('TXN1735977600-KLM789', 4, 2000.00, 'WITHDRAWAL', 'SUCCESS', 'ATM withdrawal', 78000.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('TXN1736064000-NOP012', 2, 5000.00, 'DEPOSIT', 'SUCCESS', 'Cash deposit', 52000.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
('TXN1736150400-QRS345', 5, 10000.00, 'WITHDRAWAL', 'SUCCESS', 'ATM withdrawal', 110000.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('TXN1736236800-TUV678', 2, 4, 8000.00, 'TRANSFER', 'SUCCESS', 'Payment', 44000.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('TXN1736323200-WXY901', 4, 3000.00, 'WITHDRAWAL', 'SUCCESS', 'ATM withdrawal', 75000.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Today's transactions
INSERT INTO transactions (txn_id, to_account_id, amount, transaction_type, status, description, balance_after, created_at) VALUES
('TXN1736409600-ZAB234', 2, 6000.00, 'DEPOSIT', 'SUCCESS', 'Cash deposit', 50000.00, NOW() - INTERVAL 3 HOUR);

-- ============================================================================
-- INSERT ADMIN LOGS
-- ============================================================================

INSERT INTO admin_logs (admin_id, action, target_type, target_id, details) VALUES
(1, 'ACCOUNT_CREATED', 'ACCOUNT', 2, 'Created savings account for john_doe'),
(1, 'ACCOUNT_CREATED', 'ACCOUNT', 3, 'Created current account for john_doe'),
(1, 'ACCOUNT_CREATED', 'ACCOUNT', 4, 'Created current account for jane_smith'),
(1, 'ATM_CASH_UPDATED', 'ATM_CASH', NULL, 'Updated ATM cash inventory'),
(1, 'USER_CREATED', 'USER', 2, 'Created user account for john_doe'),
(1, 'USER_CREATED', 'USER', 3, 'Created user account for jane_smith');

-- ============================================================================
-- INSERT SESSION LOGS (Sample)
-- ============================================================================

INSERT INTO session_logs (user_id, session_id, login_time, logout_time, transactions_count, ip_address) VALUES
(2, 'SESSION-1704067200-ABC', '2024-01-01 10:00:00', '2024-01-01 10:45:00', 3, '192.168.1.100'),
(3, 'SESSION-1704153600-DEF', '2024-01-02 14:00:00', '2024-01-02 14:35:00', 2, '192.168.1.101'),
(2, 'SESSION-1704326400-GHI', '2024-01-04 11:30:00', '2024-01-04 12:00:00', 1, '192.168.1.100'),
(4, 'SESSION-1704499200-JKL', '2024-01-06 13:00:00', '2024-01-06 13:25:00', 2, '192.168.1.102');

-- ============================================================================
-- INSERT FRAUD ALERTS (Sample)
-- ============================================================================

-- High-value transaction alert
INSERT INTO fraud_alerts (account_id, alert_type, severity, details, status) VALUES
(5, 'HIGH_VALUE', 'MEDIUM', 'High-value transfer: ₹20,000 - TXN: TXN1704758400-YZA567', 'RESOLVED');

-- Rapid withdrawal alert (simulated)
INSERT INTO fraud_alerts (account_id, alert_type, severity, details, status) VALUES
(2, 'RAPID_WITHDRAWAL', 'HIGH', 'Multiple withdrawals detected: 4 in last hour', 'FALSE_POSITIVE');

-- Unusual pattern
INSERT INTO fraud_alerts (account_id, alert_type, severity, details, status) VALUES
(4, 'UNUSUAL_PATTERN', 'LOW', 'Transaction at unusual time: 2:30 AM', 'OPEN');

-- ============================================================================
-- VERIFY DATA
-- ============================================================================

-- Show summary
SELECT 'Users Created' as Info, COUNT(*) as Count FROM users
UNION ALL
SELECT 'Accounts Created', COUNT(*) FROM accounts
UNION ALL
SELECT 'Transactions Recorded', COUNT(*) FROM transactions
UNION ALL
SELECT 'ATM Denominations', COUNT(*) FROM atm_cash
UNION ALL
SELECT 'Admin Logs', COUNT(*) FROM admin_logs
UNION ALL
SELECT 'Fraud Alerts', COUNT(*) FROM fraud_alerts;

-- Show total ATM cash
SELECT 
    'Total ATM Cash Available' as Info,
    CONCAT('₹', FORMAT(SUM(denomination * quantity), 2)) as Amount
FROM atm_cash;

-- Show account balances
SELECT 
    account_number,
    username,
    account_type,
    CONCAT('₹', FORMAT(balance, 2)) as balance,
    status
FROM accounts a
JOIN users u ON a.user_id = u.id
ORDER BY a.id;

-- ============================================================================
-- SAMPLE DATA INSERTED SUCCESSFULLY
-- ============================================================================

-- Quick Test Credentials:
-- =====================
-- Admin:
--   Username: admin
--   PIN: 1234
--   Account: ACC1001
--
-- Customer 1:
--   Username: john_doe
--   PIN: 5678
--   Accounts: ACC1002 (Savings), ACC1003 (Current)
--
-- Customer 2:
--   Username: jane_smith
--   PIN: 9012
--   Account: ACC1004 (Current)
-- =====================
