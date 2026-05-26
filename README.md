# Store Application

The Store application keeps track of customers, orders, and products in a PostgreSQL database.

## Prerequisites

This service assumes PostgreSQL 16.2 is available with:

- host: `localhost`
- port: `5433`
- database: `store`
- username/password: `admin/admin`

Start PostgreSQL manually:

```shell
docker run -d \
  --name postgres \
  --restart always \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=store \
  -v postgres:/var/lib/postgresql/data \
  -p 5433:5432 \
  postgres:16.2 \
  postgres -c wal_level=logical
```

Or run the full stack with Docker Compose:

```shell
docker compose up --build
```

Compose starts PostgreSQL first, waits for `pg_isready` to report a healthy database, then starts the Spring Boot app with container-to-container datasource settings.
If host port `8080` is already in use, choose another host port:

```shell
APP_PORT=8081 docker compose up --build
```

## Running locally

```shell
./gradlew bootRun
```

The application uses Liquibase to migrate the schema and seed sample data.

## Running tests

```shell
./gradlew test
```

## API

The API is documented in `OpenAPI.yaml`.

### Health

```http
GET /health
```

### Customers

```http
GET /customer?page=0&size=50
GET /customer/{id}
GET /customer?q=john&page=0&size=50
POST /customer
```

Create customer request:

```json
{
  "name": "Jane Doe"
}
```

### Orders

```http
GET /order?page=0&size=50
GET /order/{id}
POST /order
```

Create order request:

```json
{
  "description": "Home office setup",
  "customerId": 1,
  "productIds": [1, 2, 4]
}
```

### Products

```http
GET /products?page=0&size=50
GET /products/{id}
POST /products
```

Create product request:

```json
{
  "description": "Mechanical Keyboard"
}
```

## Data model

- A customer has an ID, a name, and zero or more orders.
- An order has an ID, a description, one customer, and one or more products.
- A product has an ID, a description, and can appear in many orders.

The DTOs intentionally avoid circular JSON responses:

- `OrderDTO` contains a compact customer and compact products.
- `CustomerDTO` contains compact orders.
- `ProductDTO` contains product details and the IDs of orders that contain the product.

Collection GET endpoints return a page envelope with `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`. The default page size is 50 and the maximum accepted size is 100.

## Application structure

- `web.rest` contains HTTP resources and request validation.
- `web.rest.errors` translates exceptions into a consistent `ErrorResponse`.
- `service` defines use-case contracts; `service.impl` contains transactional implementation logic.
- `repository`, `entity`, `mapper`, and `dto` keep persistence, mapping, and API payload concerns separate.

## Performance notes

The application optimizes read endpoints for high database latency by:

- using service-layer read-only transactions;
- disabling Open Session in View;
- paginating collection endpoints before loading related rows;
- using fetch joins for DTO read paths;
- adding indexes for foreign keys and join-table lookups;
- adding a PostgreSQL trigram index for case-insensitive customer name substring search.

See `SOLUTION_ROADMAP.md` for the detailed solution rationale.
