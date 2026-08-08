Supply Chain Multi-Warehouse Tracker

REST API for managing inventory, warehouses, stock transfers, purchasing, order
fulfillment, logistics, and demand analytics across multiple warehouse
locations.

## Features

- JWT authentication with role-based access control
- Warehouse and product management
- Paginated inventory lookup and low-stock alerts
- Barcode-based inventory scans
- Inter-warehouse stock transfers
- Purchase order creation, approval, and low-stock auto-generation
- Supplier portal order updates
- Sales order ingestion and status tracking
- Shipping-rate lookup and shipping manifest creation
- Demand forecasting and analytics summaries
- Administrative user management and audit logs

## Technology Stack

- Java 17
- Spring Boot 4.1
- Spring Web MVC
- Spring Security and JJWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven Wrapper
- Lombok

## Project Structure

```text
.
├── backend/
│   ├── src/main/java/       # Controllers, services, repositories, and domain models
│   ├── src/main/resources/  # Application configuration and Flyway migrations
│   └── src/test/            # Backend tests
└── database/
    └── schema.sql           # PostgreSQL schema and development seed data
```

## Prerequisites

- JDK 17 or newer
- PostgreSQL 14 or newer
- Git

Maven is not required globally; the repository includes Maven Wrapper scripts.

## Getting Started

### 1. Create the database

Create a PostgreSQL database named `supplychaindb`:

```bash
psql -U postgres -c "CREATE DATABASE supplychaindb;"
```

To load the development schema and sample data:

```bash
psql -U postgres -d supplychaindb -f database/schema.sql
```

The seed data includes sample users, warehouses, products, and inventory.

### 2. Configure the backend

Update `backend/src/main/resources/application.properties` for your local
PostgreSQL credentials. The default configuration uses:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/supplychaindb
spring.datasource.username=postgres
spring.datasource.password=your_password
server.port=8082
```

Set a unique, high-entropy `app.jwt.secret` through your local configuration
or environment-specific deployment settings. Do not use development secrets
in production.

### 3. Run the API

From the repository root:

```bash
cd backend
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8082`.

Flyway migrations in `backend/src/main/resources/db/migration` are applied by
the application at startup.

## Default Development Accounts

The database seed script creates these accounts. Each uses the development
password `password123`.

| Username | Role | Purpose |
| --- | --- | --- |
| `admin` | `ROLE_ADMIN` | User management and audit access |
| `manager1` | `ROLE_WAREHOUSE_MANAGER` | Warehouse operations and order workflows |
| `clerk1` | `ROLE_CLERK` | Inventory and low-stock visibility |

Change or remove these credentials before deploying outside a development
environment.

## API Overview

All endpoints are prefixed with `/api/v1`. Authentication endpoints are public;
other endpoints require a bearer token:

```http
Authorization: Bearer <jwt>
```

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /auth/login`, `POST /auth/register`, `GET /auth/me` |
| Inventory | `GET /inventory`, `GET /alerts/low-stock`, `POST /inventory/scan` |
| Warehouses and products | `GET /warehouses`, `GET /products` |
| Transfers | `GET/POST /transfers`, `GET /transfers/{id}`, `PATCH /transfers/{id}/status` |
| Purchase orders | `GET/POST /purchase-orders`, `GET /purchase-orders/{id}`, `POST /purchase-orders/auto-generate`, `PUT /purchase-orders/{id}/approve` |
| Supplier portal | `GET /supplier-portal/orders`, `PUT /supplier-portal/orders/{id}/status` |
| Sales orders | `GET /orders`, `POST /orders/ingest`, `PUT /orders/{id}/status` |
| Logistics | `GET /logistics/rates`, `POST /logistics/ship`, `GET /logistics/manifest/{orderId}` |
| Analytics | `GET /analytics/forecasting`, `GET /analytics/summary` |
| Administration | `/admin/users` and `/admin/audit-logs` endpoints for administrators |

Use the controller classes under
`backend/src/main/java/com/supplychainmultiwarehousetracker/controller` as the
authoritative source for request parameters and payloads.

## Development Commands

Run the test suite:

```bash
cd backend
./mvnw test
```

Build the application:

```bash
./mvnw clean package
```

On Windows, replace `./mvnw` with `.\mvnw.cmd`.

## Security Notes

- Keep database passwords and JWT secrets out of committed configuration.
- Replace all seeded passwords outside local development.
- Use HTTPS and restrict CORS origins for deployed environments.
- Grant users only the role required for their responsibilities.

## Contributing

1. Create a feature branch.
2. Make focused changes and add or update tests where appropriate.
3. Run `./mvnw test` from `backend/`.
4. Open a pull request describing the change and validation performed.
