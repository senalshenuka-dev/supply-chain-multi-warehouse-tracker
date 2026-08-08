-- Flyway Migration V6: Ensure all 5 RBAC demo accounts share the active BCrypt password hash
UPDATE users SET password = (SELECT password FROM users WHERE username = 'admin' LIMIT 1) WHERE username IN ('warehouse1', 'purchasing1', 'sales1', 'supplier1');
