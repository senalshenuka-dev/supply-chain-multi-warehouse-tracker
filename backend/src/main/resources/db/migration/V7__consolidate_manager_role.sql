-- V7: Migrate ROLE_WAREHOUSE_MANAGER, ROLE_PURCHASING_MANAGER, ROLE_SALES_OPERATOR → ROLE_MANAGER
-- The three separate manager roles are consolidated into a single unified ROLE_MANAGER

UPDATE users SET role = 'ROLE_MANAGER' WHERE role IN ('ROLE_WAREHOUSE_MANAGER', 'ROLE_PURCHASING_MANAGER', 'ROLE_SALES_OPERATOR');

-- Update the seed demo accounts to reflect the unified role
-- warehouse1, purchasing1, sales1, manager1 all become ROLE_MANAGER
INSERT INTO users (username, email, password, role)
VALUES
  ('manager1', 'manager@supplychain.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m168M.A4Z9e62', 'ROLE_MANAGER')
ON CONFLICT (username) DO UPDATE SET role = 'ROLE_MANAGER';
