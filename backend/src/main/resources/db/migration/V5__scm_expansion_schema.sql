-- Flyway Migration V5: SME SCM Expansion Schema (Vendors, POs, Sales Orders, Shipping & Barcodes)

ALTER TABLE products ADD COLUMN IF NOT EXISTS barcode VARCHAR(100);

UPDATE products SET barcode = '8801234567890' WHERE sku = 'PROD-CPU-001';
UPDATE products SET barcode = '8801234567891' WHERE sku = 'PROD-RAM-002';
UPDATE products SET barcode = '8801234567892' WHERE sku = 'PROD-SSD-003';
UPDATE products SET barcode = '8801234567893' WHERE sku = 'PROD-GPU-004';
UPDATE products SET barcode = '8801234567894' WHERE sku = 'PROD-PSU-005';

CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    contact_email VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    lead_time_days INT NOT NULL DEFAULT 5,
    fulfillment_accuracy NUMERIC(5, 2) DEFAULT 98.50,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(12, 2) DEFAULT 0.00,
    expected_delivery_date TIMESTAMP WITHOUT TIME ZONE,
    notes TEXT,
    created_by_user_id BIGINT REFERENCES users(id),
    approved_by_user_id BIGINT REFERENCES users(id),
    dispatch_doc_url TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    requested_quantity INT NOT NULL,
    received_quantity INT DEFAULT 0,
    unit_price NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS sales_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    channel_source VARCHAR(50) NOT NULL DEFAULT 'DIRECT',
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(12, 2) DEFAULT 0.00,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales_order_items (
    id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS shipping_manifests (
    id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT UNIQUE NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    carrier VARCHAR(50) NOT NULL,
    tracking_number VARCHAR(100) NOT NULL,
    shipping_cost NUMERIC(10, 2) NOT NULL,
    label_pdf_url TEXT,
    shipped_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed Demo Vendors
INSERT INTO vendors (code, name, contact_email, phone, lead_time_days, fulfillment_accuracy) VALUES
('VEND-TECH-01', 'Silicon Microelectronics Corp', 'orders@siliconmicro.com', '+1-800-555-0199', 4, 99.20),
('VEND-COMP-02', 'Global Logistics Components', 'sales@glcomponents.com', '+1-800-555-0288', 6, 96.50)
ON CONFLICT (code) DO NOTHING;

-- Seed Accounts for 5 RBAC Roles (Password hash = 'password123')
INSERT INTO users (username, email, password, role) VALUES
('warehouse1', 'warehouse@supplychain.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m168M.A4Z9e62', 'ROLE_WAREHOUSE_MANAGER'),
('purchasing1', 'purchasing@supplychain.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m168M.A4Z9e62', 'ROLE_PURCHASING_MANAGER'),
('sales1', 'sales@supplychain.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m168M.A4Z9e62', 'ROLE_SALES_OPERATOR'),
('supplier1', 'supplier@supplychain.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m168M.A4Z9e62', 'ROLE_SUPPLIER')
ON CONFLICT (username) DO NOTHING;

-- Seed Demo Purchase Orders
INSERT INTO purchase_orders (po_number, vendor_id, warehouse_id, status, total_amount, expected_delivery_date, notes) VALUES
('PO-2026-001', 1, 1, 'PENDING_APPROVAL', 4999.90, CURRENT_TIMESTAMP + INTERVAL '5 days', 'Urgent CPU restocking for Q3 demand'),
('PO-2026-002', 2, 2, 'SENT', 2990.00, CURRENT_TIMESTAMP + INTERVAL '3 days', 'Routine RAM and SSD buffer stock')
ON CONFLICT (po_number) DO NOTHING;

INSERT INTO purchase_order_items (purchase_order_id, product_id, requested_quantity, received_quantity, unit_price) VALUES
(1, 1, 10, 0, 499.99),
(2, 2, 20, 0, 149.50)
ON CONFLICT DO NOTHING;

-- Seed Demo Sales Orders
INSERT INTO sales_orders (order_number, channel_source, customer_name, customer_email, warehouse_id, status, total_amount) VALUES
('ORD-SHOPIFY-801', 'SHOPIFY', 'TechCorp Enterprises', 'buyer@techcorp.io', 1, 'PENDING', 999.98),
('ORD-AMAZON-902', 'AMAZON', 'Acme Systems', 'procurement@acme.org', 2, 'ALLOCATED', 1495.00)
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO sales_order_items (sales_order_id, product_id, quantity, unit_price) VALUES
(1, 1, 2, 499.99),
(2, 2, 10, 149.50)
ON CONFLICT DO NOTHING;
