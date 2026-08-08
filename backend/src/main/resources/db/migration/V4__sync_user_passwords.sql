-- Flyway Migration V4: Update admin, manager1, clerk1 passwords to match admin2 BCrypt hash
UPDATE users SET password = (SELECT password FROM users WHERE username = 'admin2' LIMIT 1) WHERE username IN ('admin', 'manager1', 'clerk1');
