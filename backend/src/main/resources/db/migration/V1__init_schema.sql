-- Flyway Migration V1: Initial Schema & Seed Data

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CLERK',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS warehouses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    min_threshold INT NOT NULL DEFAULT 10,
    unit_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS warehouse_inventory (
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (warehouse_id, product_id),
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stock_transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_number VARCHAR(50) NOT NULL UNIQUE,
    source_warehouse_id BIGINT NOT NULL,
    target_warehouse_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    initiated_by_user_id BIGINT NOT NULL,
    dispatched_by_user_id BIGINT,
    received_by_user_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_source FOREIGN KEY (source_warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_transfer_target FOREIGN KEY (target_warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_transfer_initiator FOREIGN KEY (initiated_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_transfer_dispatcher FOREIGN KEY (dispatched_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_transfer_receiver FOREIGN KEY (received_by_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS transfer_items (
    id BIGSERIAL PRIMARY KEY,
    stock_transfer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    requested_quantity INT NOT NULL CHECK (requested_quantity > 0),
    transferred_quantity INT CHECK (transferred_quantity >= 0),
    CONSTRAINT fk_item_transfer FOREIGN KEY (stock_transfer_id) REFERENCES stock_transfers (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(50) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed Initial Data
INSERT INTO users (username, email, password, role) VALUES 
('admin', 'admin@supplychain.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1e.5fS8/6Yp3eX9P.Hw.R3.V7aD6/uC', 'ROLE_ADMIN'),
('manager1', 'manager@supplychain.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1e.5fS8/6Yp3eX9P.Hw.R3.V7aD6/uC', 'ROLE_WAREHOUSE_MANAGER'),
('clerk1', 'clerk@supplychain.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1e.5fS8/6Yp3eX9P.Hw.R3.V7aD6/uC', 'ROLE_CLERK')
ON CONFLICT (username) DO NOTHING;

INSERT INTO warehouses (code, name, location) VALUES
('WH-NORTH', 'North Logistics Hub', 'Building A, Industrial Park, Chicago IL'),
('WH-SOUTH', 'South Regional Hub', 'Zone B, Commerce Way, Dallas TX'),
('WH-WEST', 'Pacific Coast Fulfillment', 'Dock 4, Logistics Blvd, Los Angeles CA')
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (sku, name, description, min_threshold, unit_price) VALUES
('PROD-CPU-001', 'High Performance Processor i9', '16-Core 5.2GHz Server Processor', 15, 499.99),
('PROD-RAM-002', '32GB DDR5 Server RAM', 'ECC Registered 4800MHz Memory Module', 30, 149.50),
('PROD-SSD-003', '2TB NVMe PCIe 4.0 SSD', 'High Endurance Enterprise Solid State Drive', 20, 199.00),
('PROD-GPU-004', 'AI Workstation GPU 24GB', 'High Compute Graphics Accelerator', 5, 1299.00),
('PROD-PSU-005', '850W Platinum Modular PSU', '80 Plus Platinum Power Supply Unit', 25, 139.99)
ON CONFLICT (sku) DO NOTHING;

INSERT INTO warehouse_inventory (warehouse_id, product_id, quantity, reserved_quantity) VALUES
(1, 1, 100, 0),
(1, 2, 25, 0),
(1, 3, 80, 0),
(1, 4, 12, 0),
(1, 5, 45, 0),
(2, 1, 40, 0),
(2, 2, 150, 0),
(2, 3, 10, 0),
(2, 4, 3, 0),
(2, 5, 60, 0),
(3, 1, 5, 0),
(3, 2, 80, 0),
(3, 3, 120, 0),
(3, 4, 8, 0),
(3, 5, 18, 0)
ON CONFLICT (warehouse_id, product_id) DO NOTHING;
