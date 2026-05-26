-- Product catalogue and order/product relationship.
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE order_product (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_product_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT
);

-- Read-path indexes for high-latency database access patterns.
CREATE INDEX idx_order_customer_id ON "order" (customer_id);
CREATE INDEX idx_order_product_order_id ON order_product (order_id);
CREATE INDEX idx_order_product_product_id ON order_product (product_id);

-- Supports case-insensitive substring search like /customer?q=ann.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_customer_name_trgm ON customer USING GIN (lower(name) gin_trgm_ops);

-- Small seed catalogue so existing sample orders can satisfy the new order-products requirement.
INSERT INTO product (id, description) VALUES
    (1, 'Laptop'),
    (2, 'Keyboard'),
    (3, 'Mouse'),
    (4, 'Monitor'),
    (5, 'USB-C Cable'),
    (6, 'Docking Station'),
    (7, 'Office Chair'),
    (8, 'Desk Lamp'),
    (9, 'Notebook'),
    (10, 'Backpack');

-- Attach one product to each existing sample order deterministically.
INSERT INTO order_product (order_id, product_id)
SELECT o.id, ((o.id - 1) % 10) + 1
FROM "order" o
ON CONFLICT DO NOTHING;

-- Existing seed data inserts explicit IDs, so align sequences before accepting POST requests.
SELECT setval(pg_get_serial_sequence('customer', 'id'), (SELECT COALESCE(MAX(id), 1) FROM customer));
SELECT setval(pg_get_serial_sequence('"order"', 'id'), (SELECT COALESCE(MAX(id), 1) FROM "order"));
SELECT setval(pg_get_serial_sequence('product', 'id'), (SELECT COALESCE(MAX(id), 1) FROM product));
