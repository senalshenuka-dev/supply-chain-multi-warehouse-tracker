-- Flyway Migration V3: Update admin, manager1, clerk1 passwords to BCrypt hash for 'password123'
UPDATE users SET password = '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m168M.A4Z9e62' WHERE username IN ('admin', 'manager1', 'clerk1');
