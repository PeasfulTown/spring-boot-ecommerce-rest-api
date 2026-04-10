-- Categories
INSERT INTO category (id, name, description) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Electronics', 'Electronic devices and accessories'),
('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Clothing', 'Apparel and fashion items'),
('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Home & Kitchen', 'Home appliances and kitchen essentials'),
('d4e5f6a7-b8c9-0123-defa-234567890123', 'Books', 'Physical and digital books'),
('e5f6a7b8-c9d0-1234-efab-345678901234', 'Sports & Outdoors', 'Sports equipment and outdoor gear');

-- Products
INSERT INTO product (id, category_id, name, description, price, active_status, stock_status) VALUES
-- Electronics
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'iPhone 15', 'Apple iPhone 15 128GB', 999.99, 'ACTIVE', 'IN_STOCK'),
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Samsung Galaxy S24', 'Samsung Galaxy S24 256GB', 899.99, 'ACTIVE', 'IN_STOCK'),
('b8c9d0e1-f2a3-4567-bcde-678901234567', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Sony WH-1000XM5', 'Noise cancelling wireless headphones', 349.99, 'ACTIVE', 'LOW_STOCK'),
('18a2d3e4-f5a6-7890-bcde-f12345678902', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'iPad Pro 12.9', 'Apple iPad Pro 12.9 inch 256GB', 1099.99, 'ACTIVE', 'IN_STOCK'),

-- Clothing
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Levi''s 501 Jeans', 'Classic straight fit jeans', 59.99, 'ACTIVE', 'IN_STOCK'),
('d0e1f2a3-b4c5-6789-defa-890123456789', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Nike Air Max 270', 'Men''s running shoes size 10', 149.99, 'ACTIVE', 'IN_STOCK'),
('e1f2a3b4-c5d6-7890-efab-901234567890', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Adidas Ultraboost 23', 'Women''s training shoes', 189.99, 'ACTIVE', 'LOW_STOCK'),
('28b3c4d5-e6f7-8901-cdef-012345678903', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Uniqlo Fleece Jacket', 'Lightweight fleece jacket', 49.99, 'ACTIVE', 'IN_STOCK'),

-- Home & Kitchen
('f2a3b4c5-d6e7-8901-fabc-012345678901', 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'Instant Pot Duo 7-in-1', 'Multi-use pressure cooker 6 quart', 99.99, 'ACTIVE', 'IN_STOCK'),
('a3b4c5d6-e7f8-9012-abcd-123456789012', 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'Dyson V15 Detect', 'Cordless vacuum cleaner', 699.99, 'ACTIVE', 'OUT_OF_STOCK'),
('38c4d5e6-f7a8-9012-defa-123456789014', 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'Nespresso Vertuo Pop', 'Coffee and espresso machine', 119.99, 'ACTIVE', 'IN_STOCK'),
('48d5e6f7-a8b9-0123-efab-234567890125', 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'KitchenAid Stand Mixer', 'Tilt-head stand mixer 4.5 quart', 449.99, 'INACTIVE', 'OUT_OF_STOCK'),

-- Books
('b4c5d6e7-f8a9-0123-bcde-234567890123', 'd4e5f6a7-b8c9-0123-defa-234567890123', 'Clean Code', 'A handbook of agile software craftsmanship by Robert C. Martin', 34.99, 'ACTIVE', 'IN_STOCK'),
('c5d6e7f8-a9b0-1234-cdef-345678901234', 'd4e5f6a7-b8c9-0123-defa-234567890123', 'The Pragmatic Programmer', '20th anniversary edition by David Thomas', 49.99, 'ACTIVE', 'IN_STOCK'),
('58e6f7a8-b9c0-1234-fabc-345678901236', 'd4e5f6a7-b8c9-0123-defa-234567890123', 'System Design Interview', 'An insider''s guide by Alex Xu', 39.99, 'ACTIVE', 'LOW_STOCK'),
('68f7a8b9-c0d1-2345-abcd-456789012347', 'd4e5f6a7-b8c9-0123-defa-234567890123', 'Designing Data-Intensive Applications', 'By Martin Kleppmann', 54.99, 'ACTIVE', 'IN_STOCK'),

-- Sports & Outdoors
('d6e7f8a9-b0c1-2345-defa-456789012345', 'e5f6a7b8-c9d0-1234-efab-345678901234', 'Yoga Mat Pro', 'Non-slip exercise mat 6mm thick', 45.99, 'ACTIVE', 'IN_STOCK'),
('e7f8a9b0-c1d2-3456-efab-567890123456', 'e5f6a7b8-c9d0-1234-efab-345678901234', 'Kettlebell 20kg', 'Cast iron kettlebell', 79.99, 'INACTIVE', 'OUT_OF_STOCK'),
('78a8b9c0-d1e2-3456-bcde-567890123458', 'e5f6a7b8-c9d0-1234-efab-345678901234', 'Resistance Bands Set', 'Set of 5 resistance bands', 29.99, 'ACTIVE', 'IN_STOCK'),
('88b9c0d1-e2f3-4567-cdef-678901234569', 'e5f6a7b8-c9d0-1234-efab-345678901234', 'Hydro Flask 32oz', 'Insulated stainless steel water bottle', 44.99, 'ACTIVE', 'IN_STOCK');

-- Product Image URLs
INSERT INTO product_image_urls (product_id, image_url) VALUES
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'https://images.example.com/iphone15-front.jpg'),
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'https://images.example.com/iphone15-back.jpg'),
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'https://images.example.com/galaxy-s24-front.jpg'),
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'https://images.example.com/galaxy-s24-back.jpg'),
('b8c9d0e1-f2a3-4567-bcde-678901234567', 'https://images.example.com/sony-wh1000xm5.jpg'),
('18a2d3e4-f5a6-7890-bcde-f12345678902', 'https://images.example.com/ipad-pro-front.jpg'),
('18a2d3e4-f5a6-7890-bcde-f12345678902', 'https://images.example.com/ipad-pro-back.jpg'),
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'https://images.example.com/levis-501-blue.jpg'),
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'https://images.example.com/levis-501-black.jpg'),
('d0e1f2a3-b4c5-6789-defa-890123456789', 'https://images.example.com/nike-airmax270.jpg'),
('e1f2a3b4-c5d6-7890-efab-901234567890', 'https://images.example.com/adidas-ultraboost23.jpg'),
('28b3c4d5-e6f7-8901-cdef-012345678903', 'https://images.example.com/uniqlo-fleece-jacket.jpg'),
('f2a3b4c5-d6e7-8901-fabc-012345678901', 'https://images.example.com/instantpot-duo.jpg'),
('a3b4c5d6-e7f8-9012-abcd-123456789012', 'https://images.example.com/dyson-v15.jpg'),
('38c4d5e6-f7a8-9012-defa-123456789014', 'https://images.example.com/nespresso-vertuo.jpg'),
('48d5e6f7-a8b9-0123-efab-234567890125', 'https://images.example.com/kitchenaid-mixer.jpg'),
('b4c5d6e7-f8a9-0123-bcde-234567890123', 'https://images.example.com/clean-code-book.jpg'),
('c5d6e7f8-a9b0-1234-cdef-345678901234', 'https://images.example.com/pragmatic-programmer.jpg'),
('58e6f7a8-b9c0-1234-fabc-345678901236', 'https://images.example.com/system-design-interview.jpg'),
('68f7a8b9-c0d1-2345-abcd-456789012347', 'https://images.example.com/ddia-book.jpg'),
('d6e7f8a9-b0c1-2345-defa-456789012345', 'https://images.example.com/yoga-mat-pro.jpg'),
('e7f8a9b0-c1d2-3456-efab-567890123456', 'https://images.example.com/kettlebell-20kg.jpg'),
('78a8b9c0-d1e2-3456-bcde-567890123458', 'https://images.example.com/resistance-bands.jpg'),
('88b9c0d1-e2f3-4567-cdef-678901234569', 'https://images.example.com/hydro-flask-32oz.jpg');
