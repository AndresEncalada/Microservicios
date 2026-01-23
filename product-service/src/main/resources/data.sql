-- data.sql
INSERT INTO products VALUES (1, 'Laptop Gamer HP', 1200.50, 10) ON CONFLICT (id) DO NOTHING;
INSERT INTO products VALUES (2, 'Mouse Inalámbrico', 25.00, 50) ON CONFLICT (id) DO NOTHING;
INSERT INTO products VALUES (3, 'Teclado Mecánico', 85.99, 20) ON CONFLICT (id) DO NOTHING;
INSERT INTO products VALUES (4, 'Monitor 24 pulgadas', 180.00, 15) ON CONFLICT (id) DO NOTHING;
INSERT INTO products VALUES (5, 'Silla Ergonómica', 250.00, 5) ON CONFLICT (id) DO NOTHING;