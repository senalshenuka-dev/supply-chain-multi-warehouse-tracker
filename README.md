# 🏭 Supply Chain Multi-Warehouse Tracker

<div align="center">

A full-stack, enterprise-grade supply chain management system for real-time inventory tracking, inter-warehouse stock transfers, purchase & sales order management, and logistics — secured with JWT-based Role-Based Access Control.

[![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21.2-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

</div>

A full-stack enterprise supply chain management system for tracking inventory, stock transfers, purchase orders, sales orders, and logistics across multiple warehouses in real time.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#️-tech-stack)
- [Architecture](#️-architecture)
- [Data Model](#️-data-model)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Database Setup](#1-database-setup)
  - [Backend Setup](#2-backend-setup)
  - [Frontend Setup](#3-frontend-setup)
- [Default Credentials](#-default-credentials)
- [API Reference](#-api-reference)
- [Role-Based Access Control](#-role-based-access-control)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🧭 Overview

The **Supply Chain Multi-Warehouse Tracker** is a comprehensive, production-ready platform designed to give organizations full visibility and control over their multi-site inventory operations. It supports real-time inventory tracking, inter-warehouse stock transfers, supplier management, purchase/sales order fulfillment, demand forecasting analytics, and a complete audit trail — all secured with JWT-based Role-Based Access Control (RBAC).

---

## ✨ Features

| Module | Description |
|---|---|
| 📦 **Inventory Ledger** | Paginated, filterable inventory view per warehouse; filter by SKU, sort by any column |
| 🔄 **Stock Transfer Pipeline** | Initiate, dispatch, and receive inter-warehouse transfers with a tracked status pipeline (`REQUESTED → DISPATCHED → RECEIVED`) |
| 🚨 **Low-Stock Alerts** | Automatic alerts triggered when product quantity falls below the product-defined minimum threshold |
| 🔍 **Barcode Scanning** | Inventory lookup and quantity update via barcode scan endpoint |
| 🛒 **Purchase Orders** | Create and manage purchase orders with line items against registered vendors |
| 🤝 **Supplier Portal** | Dedicated interface for vendor-facing purchase order acknowledgment and status updates |
| 📬 **Order Fulfillment** | End-to-end sales order creation, processing, and fulfillment tracking |
| 🚚 **Shipping Manifests** | Generate and manage shipping manifests with carrier, tracking number, and label URL |
| 📊 **Forecasting & Analytics** | Demand forecasting dashboard and inventory trend analysis |
| 🔐 **Admin Panel** | Full user management (create, update, delete, assign roles) and paginated audit log viewer |
| 📝 **Audit Logs** | Immutable, timestamped record of all significant system actions |

---

## 🛠️ Tech Stack

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 4.1.0 | Application framework |
| Spring Web MVC | (managed) | REST API layer |
| Spring Security | (managed) | Authentication & authorization |
| Spring Data JPA | (managed) | ORM / data access layer |
| Spring Validation | (managed) | Request body validation (`@Valid`) |
| Flyway | (managed) | Database migration management |
| PostgreSQL | 14+ | Relational database |
| JJWT | 0.11.5 | JWT token generation & validation |
| Lombok | (optional) | Boilerplate reduction |
| Maven | (wrapper) | Build & dependency management |

### Frontend

| Technology | Version | Purpose |
|---|---|---|
| Angular | ~21.2.0 | Single-page application framework |
| Angular Material | ~21.2.0 | UI component library |
| Angular CDK | ~21.2.0 | Component Dev Kit |
| RxJS | ~7.8.0 | Reactive programming & HTTP streams |
| TypeScript | ~5.9.2 | Type-safe JavaScript |
| Vitest | ^4.0.8 | Unit testing |
| Prettier | ^3.8.1 | Code formatting |

---

## 🏗️ Architecture

```
supply-chain-multi-warehouse-tracker/
├── backend/          # Spring Boot REST API (port 8080)
├── frontend/         # Angular SPA (port 4200)
└── database/         # PostgreSQL schema & seed data
```

The backend follows a **layered monolithic** architecture:

```
[ Angular SPA (Material UI) ]
          ↕  HTTP / REST (JSON)   — Auth via Bearer JWT
[ Spring Boot REST Controllers ]
          ↕
[ Service Layer  (business logic, audit logging) ]
          ↕
[ Repository Layer  (Spring Data JPA) ]
          ↕
[ PostgreSQL Database ]
```

**Security filter chain:**
```
Inbound Request
    → JwtAuthenticationFilter  (validate & parse Bearer token)
    → Spring Security FilterChain
    → @PreAuthorize / method-level checks
    → Controller
```

All endpoints under `/api/v1/**` (except `/api/v1/auth/**`) require a valid `Authorization: Bearer <JWT>` header.
Method-level security (`@EnableMethodSecurity`) enforces fine-grained role restrictions per endpoint.

---

## 🗄️ Data Model

| Table | Key Columns | Notes |
|---|---|---|
| `users` | `id`, `username`, `email`, `password` (BCrypt), `role` | Roles: `ROLE_ADMIN`, `ROLE_WAREHOUSE_MANAGER`, `ROLE_CLERK` |
| `warehouses` | `id`, `code`, `name`, `location` | Unique warehouse code (e.g. `WH-NORTH`) |
| `products` | `id`, `sku`, `name`, `min_threshold`, `unit_price` | Low-stock threshold per product |
| `warehouse_inventory` | `(warehouse_id, product_id)` PK, `quantity`, `reserved_quantity` | Composite key; `quantity >= 0` enforced by DB constraint |
| `stock_transfers` | `id`, `transfer_number`, `source/target_warehouse_id`, `status` | Status: `REQUESTED → DISPATCHED → RECEIVED` |
| `transfer_items` | `id`, `stock_transfer_id`, `product_id`, `requested_qty`, `transferred_qty` | Line items per transfer |
| `purchase_orders` | `id`, `vendor_id`, `status`, `total_amount` | Linked to `vendors` table |
| `sales_orders` | `id`, `status`, `customer_name`, `total_amount` | Order fulfillment lifecycle |
| `shipping_manifests` | `id`, `sales_order_id`, `carrier`, `tracking_number`, `label_pdf_url` | One-to-one with `sales_orders` |
| `audit_logs` | `id`, `entity_name`, `entity_id`, `action`, `performed_by`, `timestamp` | Immutable append-only record |

---

## 🚀 Getting Started

### Prerequisites

Ensure the following are installed:

| Tool | Version | Link |
|---|---|---|
| Java (JDK) | 17+ | [Adoptium](https://adoptium.net/) |
| Maven | 3.8+ *(or use the included `mvnw` wrapper)* | [Maven](https://maven.apache.org/) |
| Node.js | 20+ | [nodejs.org](https://nodejs.org/) |
| npm | 11+ | Bundled with Node.js |
| PostgreSQL | 14+ | [postgresql.org](https://www.postgresql.org/download/) |
| Angular CLI | 21+ | `npm install -g @angular/cli` |

---

### 1. Database Setup

Create the database and apply the full schema + seed data:

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE supplychaindb;"

# Apply schema and seed data
psql -U postgres -d supplychaindb -f database/schema.sql
```

The seed script provisions:
- **3 warehouses** — North Logistics Hub (Chicago), South Regional Hub (Dallas), Pacific Coast Fulfillment (Los Angeles)
- **5 products** (CPUs, RAM, SSDs, GPUs, PSUs) with per-product low-stock thresholds
- **Initial inventory levels** across all warehouses (including intentional low-stock triggers for demonstration)
- **3 default user accounts** (see [Default Credentials](#-default-credentials))

> Flyway migrations also run automatically on backend startup, keeping the schema in sync across environments.

---

### 2. Backend Setup

```bash
cd backend
```

Configure your database connection and JWT secret in `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/supplychaindb
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# JWT — use a secure random string of at least 256 bits
app.jwt.secret=your-very-long-and-secure-secret-key-at-least-256-bits
app.jwt.expiration-ms=86400000
```

Start the backend:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The REST API will be available at **`http://localhost:8080`**.

---

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
# or equivalently: ng serve
```

The Angular application will be available at **`http://localhost:4200`**.

> The Angular dev server is pre-configured with an API proxy to `http://localhost:8080`. No additional CORS configuration is required for local development.

---

## 🔑 Default Credentials

The database seed script creates the following accounts. All default passwords are `password123`.

| Username | Email | Role | Access Level |
|---|---|---|---|
| `admin` | admin@supplychain.com | `ROLE_ADMIN` | Full access — user management, audit logs, all modules |
| `manager1` | manager@supplychain.com | `ROLE_WAREHOUSE_MANAGER` | Inventory, transfers, orders, analytics |
| `clerk1` | clerk@supplychain.com | `ROLE_CLERK` | Read-only inventory and alerts |

> ⚠️ **Important:** Change all default passwords immediately before deploying to a production environment.

---

## 📡 API Reference

All endpoints are prefixed with `/api/v1`. Authentication endpoints are public; all others require an `Authorization: Bearer <JWT>` header.

### 🔓 Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Authenticate and receive a JWT token |
| `POST` | `/api/v1/auth/register` | Register a new user account |
| `GET` | `/api/v1/auth/me` | Get the currently authenticated user's profile |

### 📦 Inventory

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/inventory` | List paginated inventory; filter by `warehouseId`, `sku` |
| `GET` | `/api/v1/alerts/low-stock` | Get all products currently below their minimum threshold |
| `POST` | `/api/v1/inventory/scan` | Look up or update inventory via barcode scan |

### 🔄 Stock Transfers

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/transfers` | Initiate a new inter-warehouse stock transfer |
| `GET` | `/api/v1/transfers` | List paginated transfers; filter by `status`, `warehouseId` |
| `GET` | `/api/v1/transfers/{id}` | Get full transfer details by ID |
| `PATCH` | `/api/v1/transfers/{id}/status` | Advance transfer status (`REQUESTED → DISPATCHED → RECEIVED`) |

### 🏢 Warehouses

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/warehouses` | List all warehouses |
| `POST` | `/api/v1/warehouses` | Create a new warehouse |

### 🛒 Purchase Orders

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/purchase-orders` | List all purchase orders |
| `POST` | `/api/v1/purchase-orders` | Create a new purchase order |
| `PATCH` | `/api/v1/purchase-orders/{id}/status` | Update purchase order status |

### 🤝 Supplier Portal

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/supplier-portal/orders` | List purchase orders visible to a vendor |
| `PATCH` | `/api/v1/supplier-portal/orders/{id}/acknowledge` | Vendor acknowledges a purchase order |

### 📬 Sales Orders & Logistics

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/orders` | List all sales orders |
| `POST` | `/api/v1/orders` | Create a new sales order |
| `GET` | `/api/v1/logistics/manifests` | List all shipping manifests |
| `POST` | `/api/v1/logistics/manifests` | Create a shipping manifest for a fulfilled order |

### 📊 Analytics

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/analytics/demand` | Get demand forecasting data |

### 🔐 Admin (`ROLE_ADMIN` only)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/admin/audit-logs` | Paginated audit log viewer |
| `GET` | `/api/v1/admin/users` | List all system users |
| `POST` | `/api/v1/admin/users` | Create a new user account |
| `PUT` | `/api/v1/admin/users/{id}` | Update user account details |
| `DELETE` | `/api/v1/admin/users/{id}` | Delete a user account |
| `PUT` | `/api/v1/admin/users/{id}/role` | Change a user's role |

---

## 🔐 Role-Based Access Control

The system implements three roles with escalating privileges:

```
ROLE_ADMIN
  └── Full system access
      ├── User management (create, update, delete, assign roles)
      ├── Audit log access
      └── All warehouse manager permissions

ROLE_WAREHOUSE_MANAGER
  └── Operational access
      ├── Initiate & manage stock transfers
      ├── Manage purchase orders & order fulfillment
      ├── View & update inventory
      └── Access analytics & forecasting

ROLE_CLERK
  └── Read-only access
      ├── View inventory ledger
      └── View low-stock alerts
```

---

## 📁 Project Structure

```
supply-chain-multi-warehouse-tracker/
│
├── backend/
│   └── src/main/java/com/supplychainmultiwarehousetracker/
│       ├── BackendApplication.java
│       ├── controller/
│       │   ├── AdminController.java           # User management & audit logs
│       │   ├── AnalyticsController.java        # Demand forecasting data
│       │   ├── AuthController.java             # Login, register, /me
│       │   ├── GlobalExceptionHandler.java     # Centralized error responses
│       │   ├── InventoryController.java        # Ledger & barcode scan
│       │   ├── LogisticsController.java        # Shipping manifests
│       │   ├── OrderController.java            # Sales orders
│       │   ├── PurchaseOrderController.java    # Purchase orders
│       │   ├── SupplierPortalController.java   # Vendor-facing endpoints
│       │   ├── TransferController.java         # Stock transfer pipeline
│       │   └── WarehouseController.java        # Warehouse CRUD
│       ├── domain/
│       │   ├── model/                          # JPA entities
│       │   │   ├── AuditLog.java
│       │   │   ├── Product.java
│       │   │   ├── PurchaseOrder.java / PurchaseOrderItem.java
│       │   │   ├── Role.java / TransferStatus.java  (enums)
│       │   │   ├── SalesOrder.java / SalesOrderItem.java
│       │   │   ├── ShippingManifest.java
│       │   │   ├── StockTransfer.java / TransferItem.java
│       │   │   ├── User.java / Vendor.java
│       │   │   └── Warehouse.java / WarehouseInventory.java
│       │   └── repository/                     # Spring Data JPA repositories
│       ├── dto/                                # Request & response DTOs
│       ├── security/                           # JWT filter, token provider, SecurityConfig
│       └── service/                            # Business logic services
│
├── frontend/
│   └── src/app/
│       ├── app.routes.ts                       # Route definitions with authGuard
│       ├── core/
│       │   ├── guards/                         # authGuard (JWT presence check)
│       │   ├── interceptors/                   # HTTP auth interceptor
│       │   └── services/                       # Auth, notification, shared services
│       ├── features/
│       │   ├── admin/                          # Admin audit log UI
│       │   ├── alerts/                         # Low-stock alerts dashboard
│       │   ├── analytics/                      # Forecasting & analytics UI
│       │   ├── auth/                           # Login page
│       │   ├── inventory/                      # Inventory ledger component
│       │   ├── orders/                         # Order fulfillment component
│       │   ├── purchasing/                     # Purchase orders component
│       │   ├── supplier/                       # Supplier portal component
│       │   └── transfers/                      # Stock transfer pipeline component
│       └── shared/                             # Shared components, pipes, utilities
│
└── database/
    └── schema.sql                              # Full PostgreSQL schema + seed data
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Commit your changes (follow [Conventional Commits](https://www.conventionalcommits.org/)):
   ```bash
   git commit -m "feat: add stock reservation on transfer initiation"
   ```
4. Push to your branch:
   ```bash
   git push origin feature/your-feature-name
   ```
5. Open a Pull Request

**Code style:**
- **Backend:** Follow standard Java conventions; Lombok is already configured.
- **Frontend:** Run `prettier --write .` from the `frontend/` directory before committing.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
