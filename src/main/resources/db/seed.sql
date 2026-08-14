-- Admin seed account. Password below is bcrypt for "Admin@123" — replace before deploying.
INSERT INTO users (name, email, password_hash, role)
VALUES ('Admin', 'admin@nexamart.local', '$2a$12$V8n1nq0m3n6.Y0G9K7x9Nu5o8Zt7f2c3vQe1s5y0z1F4c8dQe1s5y', 'ADMIN');

INSERT INTO users (name, email, password_hash, role)
VALUES ('Demo Seller', 'seller@nexamart.local', '$2a$12$V8n1nq0m3n6.Y0G9K7x9Nu5o8Zt7f2c3vQe1s5y0z1F4c8dQe1s5y', 'SELLER');

INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url)
VALUES
 (2, 'Wireless Mouse', 'Ergonomic 2.4GHz wireless mouse', 599.00, 50, 'Electronics', 'https://example.com/mouse.jpg'),
 (2, 'Notebook Set', 'Pack of 3 ruled notebooks', 199.00, 100, 'Stationery', 'https://example.com/notebook.jpg');
