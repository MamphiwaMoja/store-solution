# Store Application Solution Roadmap

## 1. Understand the current state

The original application had two resources: customers and orders. Controllers accessed repositories directly and mapped JPA entities to DTOs. The database schema contained `customer` and `order`, with Liquibase loading a large sample dataset.

Key risks found:

- `GET /order` and `GET /customer` could trigger many lazy-loading database calls while DTOs are being mapped.
- API controllers accepted JPA entities directly as request bodies.
- Existing seed data inserted explicit IDs but did not reset PostgreSQL identity sequences.
- The OpenAPI file already hinted at products, but the data model and endpoint did not exist.
- There was no CI or Dockerized application delivery pipeline.

## 2. API delivery plan

### Required endpoints

- `GET /order?page={page}&size={size}` — return a bounded page of orders with customer summary and products.
- `GET /order/{id}` — return one order by ID.
- `POST /order` — create an order for an existing customer and one or more existing products.
- `GET /customer?page={page}&size={size}` — return a bounded page of customers.
- `GET /customer?q={query}&page={page}&size={size}` — return a bounded page of customers whose names contain the query substring, case-insensitively.
- `POST /customer` — create a customer.
- `GET /products?page={page}&size={size}` — return a bounded page of products with the order IDs containing each product.
- `GET /products/{id}` — return one product with the order IDs containing it.
- `POST /products` — create a product.

## 3. Data model plan

A many-to-many relationship was added between orders and products:

- One order contains one or more products.
- One product can appear in zero or more orders.
- Join table: `order_product(order_id, product_id)`.

The application keeps DTOs intentionally non-circular:

- `OrderDTO` contains a compact customer object and compact product objects.
- `CustomerDTO` contains compact order objects.
- `ProductDTO` contains `orderIds` instead of full order objects.

## 4. Performance plan

The highest-impact production fix is reducing database round trips on read endpoints.

Implemented improvements:

- Read service methods use `@Transactional(readOnly = true)`.
- Collection endpoints use bounded pagination with a default page size of 50 and a maximum page size of 100.
- Paginated read paths first page IDs, then fetch the selected rows and relationships, avoiding in-memory pagination over collection fetch joins.
- Read queries use fetch joins to load exactly the relationships needed by DTO mapping.
- `spring.jpa.open-in-view=false` prevents hidden lazy database access during serialization.
- `hibernate.default_batch_fetch_size=50` provides a safer fallback for remaining lazy access.
- Indexes were added on foreign-key join paths:
  - `order.customer_id`
  - `order_product.order_id`
  - `order_product.product_id`
- A PostgreSQL trigram GIN index was added on `lower(customer.name)` to support case-insensitive substring search.

## 5. Production-readiness plan

Implemented changes:

- Service layer added for transaction boundaries and business rules.
- Request DTOs added so controllers no longer accept JPA entities directly.
- Validation added for required fields and non-empty product lists.
- Centralized error handling added for validation and not-found responses.
- Liquibase migration added for products, order-product mapping, indexes, seed products, and sequence alignment.
- OpenAPI file updated to match the implemented API.
- Dockerfile added for image builds.
- `compose.yaml` added for local app + PostgreSQL runtime.
- GitHub Actions CI added for tests, JAR build, and Docker image build.

## 6. Assumptions made

Because the assignment is intentionally vague, these assumptions were made:

- Customer search uses `q` as the query parameter: `GET /customer?q=john`.
- Search is case-insensitive substring matching against the full name.
- New orders must include at least one product ID.
- Existing sample orders were backfilled with one deterministic seed product each so the database remains consistent with the new model.
- A product can be created before being assigned to an order.
- `GET /products` returns products with `orderIds`, not full nested order objects, to avoid circular responses and large payloads.
