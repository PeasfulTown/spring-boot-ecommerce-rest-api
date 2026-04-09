-- Categories
INSERT INTO category (id, name, description) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Electronics', 'Electronic devices and accessories'),
('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Clothing', 'Apparel and fashion items'),
('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Home & Kitchen', 'Home appliances and kitchen essentials'),
('d4e5f6a7-b8c9-0123-defa-234567890123', 'Books', 'Physical and digital books'),
('e5f6a7b8-c9d0-1234-efab-345678901234', 'Sports & Outdoors', 'Sports equipment and outdoor gear');

-- Products
INSERT INTO product (id, name, description, price, active_status, stock_status) VALUES
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'iPhone 15', 'Apple iPhone 15 128GB', 999.99, 'ACTIVE', 'IN_STOCK'),
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'Samsung Galaxy S24', 'Samsung Galaxy S24 256GB', 899.99, 'ACTIVE', 'IN_STOCK'),
('b8c9d0e1-f2a3-4567-bcde-678901234567', 'Sony WH-1000XM5', 'Noise cancelling wireless headphones', 349.99, 'ACTIVE', 'LOW_STOCK'),
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'Levi''s 501 Jeans', 'Classic straight fit jeans', 59.99, 'ACTIVE', 'IN_STOCK'),
('d0e1f2a3-b4c5-6789-defa-890123456789', 'Nike Air Max 270', 'Men''s running shoes size 10', 149.99, 'ACTIVE', 'IN_STOCK'),
('e1f2a3b4-c5d6-7890-efab-901234567890', 'Adidas Ultraboost 23', 'Women''s training shoes', 189.99, 'ACTIVE', 'LOW_STOCK'),
('f2a3b4c5-d6e7-8901-fabc-012345678901', 'Instant Pot Duo 7-in-1', 'Multi-use pressure cooker 6 quart', 99.99, 'ACTIVE', 'IN_STOCK'),
('a3b4c5d6-e7f8-9012-abcd-123456789012', 'Dyson V15 Detect', 'Cordless vacuum cleaner', 699.99, 'ACTIVE', 'OUT_OF_STOCK'),
('b4c5d6e7-f8a9-0123-bcde-234567890123', 'Clean Code', 'A handbook of agile software craftsmanship by Robert C. Martin', 34.99, 'ACTIVE', 'IN_STOCK'),
('c5d6e7f8-a9b0-1234-cdef-345678901234', 'The Pragmatic Programmer', '20th anniversary edition by David Thomas', 49.99, 'ACTIVE', 'IN_STOCK'),
('d6e7f8a9-b0c1-2345-defa-456789012345', 'Yoga Mat Pro', 'Non-slip exercise mat 6mm thick', 45.99, 'ACTIVE', 'IN_STOCK'),
('e7f8a9b0-c1d2-3456-efab-567890123456', 'Kettlebell 20kg', 'Cast iron kettlebell', 79.99, 'INACTIVE', 'OUT_OF_STOCK');

-- Product Categories
INSERT INTO product_category (product_id, category_id) VALUES
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'), -- iPhone 15 → Electronics
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'), -- Galaxy S24 → Electronics
('b8c9d0e1-f2a3-4567-bcde-678901234567', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'), -- Sony WH → Electronics
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'b2c3d4e5-f6a7-8901-bcde-f12345678901'), -- Levi's → Clothing
('d0e1f2a3-b4c5-6789-defa-890123456789', 'b2c3d4e5-f6a7-8901-bcde-f12345678901'), -- Nike → Clothing
('e1f2a3b4-c5d6-7890-efab-901234567890', 'b2c3d4e5-f6a7-8901-bcde-f12345678901'), -- Adidas → Clothing
('f2a3b4c5-d6e7-8901-fabc-012345678901', 'c3d4e5f6-a7b8-9012-cdef-123456789012'), -- Instant Pot → Home & Kitchen
('a3b4c5d6-e7f8-9012-abcd-123456789012', 'c3d4e5f6-a7b8-9012-cdef-123456789012'), -- Dyson → Home & Kitchen
('b4c5d6e7-f8a9-0123-bcde-234567890123', 'd4e5f6a7-b8c9-0123-defa-234567890123'), -- Clean Code → Books
('c5d6e7f8-a9b0-1234-cdef-345678901234', 'd4e5f6a7-b8c9-0123-defa-234567890123'), -- Pragmatic → Books
('d6e7f8a9-b0c1-2345-defa-456789012345', 'e5f6a7b8-c9d0-1234-efab-345678901234'), -- Yoga Mat → Sports
('e7f8a9b0-c1d2-3456-efab-567890123456', 'e5f6a7b8-c9d0-1234-efab-345678901234'), -- Kettlebell → Sports
-- a few products in multiple categories
('d0e1f2a3-b4c5-6789-defa-890123456789', 'e5f6a7b8-c9d0-1234-efab-345678901234'), -- Nike → Sports too
('e1f2a3b4-c5d6-7890-efab-901234567890', 'e5f6a7b8-c9d0-1234-efab-345678901234'); -- Adidas → Sports too

-- Product Image URLs
INSERT INTO product_image_urls (product_id, image_url) VALUES
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'https://images.example.com/iphone15-front.jpg'),
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'https://images.example.com/iphone15-back.jpg'),
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'https://images.example.com/galaxy-s24-front.jpg'),
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'https://images.example.com/galaxy-s24-back.jpg'),
('b8c9d0e1-f2a3-4567-bcde-678901234567', 'https://images.example.com/sony-wh1000xm5.jpg'),
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'https://images.example.com/levis-501-blue.jpg'),
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'https://images.example.com/levis-501-black.jpg'),
('d0e1f2a3-b4c5-6789-defa-890123456789', 'https://images.example.com/nike-airmax270.jpg'),
('e1f2a3b4-c5d6-7890-efab-901234567890', 'https://images.example.com/adidas-ultraboost23.jpg'),
('f2a3b4c5-d6e7-8901-fabc-012345678901', 'https://images.example.com/instantpot-duo.jpg'),
('a3b4c5d6-e7f8-9012-abcd-123456789012', 'https://images.example.com/dyson-v15.jpg'),
('b4c5d6e7-f8a9-0123-bcde-234567890123', 'https://images.example.com/clean-code-book.jpg'),
('c5d6e7f8-a9b0-1234-cdef-345678901234', 'https://images.example.com/pragmatic-programmer.jpg'),
('d6e7f8a9-b0c1-2345-defa-456789012345', 'https://images.example.com/yoga-mat-pro.jpg'),
('e7f8a9b0-c1d2-3456-efab-567890123456', 'https://images.example.com/kettlebell-20kg.jpg');