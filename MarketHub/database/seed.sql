-- =====================================================================
-- MarketHub E-Commerce Platform - Comprehensive Seed Data
-- Demo Credentials & Realistic Marketplace Data
-- =====================================================================

USE markethub_db;

-- ---------------------------------------------------------------------
-- 1. SEED USERS
-- Password for all demo accounts: 'password123'
-- Hash is SHA-256 with salt representation used by PasswordHasher.java
-- ---------------------------------------------------------------------
INSERT INTO users (user_id, username, email, password_hash, full_name, role, phone, address, is_active) VALUES
(1, 'admin', 'admin@markethub.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Platform Administrator', 'ADMIN', '+1-800-555-0100', '100 Silicon Way, Tech City, CA', TRUE),
(2, 'apextech', 'seller@apextech.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Apex Tech Solutions', 'SELLER', '+1-800-555-0201', '450 Innovation Blvd, Austin, TX', TRUE),
(3, 'nordicstyle', 'seller@nordic.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Nordic Living & Co.', 'SELLER', '+1-800-555-0202', '72 Design District, Seattle, WA', TRUE),
(4, 'janebuyer', 'jane@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Jane Doe', 'BUYER', '+1-800-555-0301', '742 Evergreen Terrace, Springfield, OR', TRUE),
(5, 'johnbuyer', 'john@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'John Smith', 'BUYER', '+1-800-555-0302', '124 Conch Street, Bikini Bottom, FL', TRUE);

-- ---------------------------------------------------------------------
-- 2. SEED CATEGORIES
-- ---------------------------------------------------------------------
INSERT INTO categories (category_id, name, slug, description, image_url) VALUES
(1, 'Electronics', 'electronics', 'High performance audio, computing devices, cameras, and accessories', 'https://picsum.photos/seed/elec1/600/400'),
(2, 'Home & Living', 'home-living', 'Minimalist furniture, smart ambient lighting, and artisan decor', 'https://picsum.photos/seed/home1/600/400'),
(3, 'Apparel & Fashion', 'apparel-fashion', 'Sustainable fabrics, everyday essentials, and premium activewear', 'https://picsum.photos/seed/fashion1/600/400'),
(4, 'Books & Media', 'books-media', 'Architectural monographs, design philosophy, and technical literature', 'https://picsum.photos/seed/books1/600/400'),
(5, 'Fitness & Wellness', 'fitness-wellness', 'Ergonomic fitness gear, recovery equipment, and tracking tools', 'https://picsum.photos/seed/fit1/600/400');

-- ---------------------------------------------------------------------
-- 3. SEED PRODUCTS
-- ---------------------------------------------------------------------
INSERT INTO products (product_id, seller_id, category_id, name, slug, description, price, discount_percent, stock_quantity, image_url, rating, review_count, is_approved, is_active) VALUES
(1, 2, 1, 'ProNoise Wireless Studio Headphones', 'pronoise-wireless-headphones', 'Engineered with 40mm beryllium drivers, active hybrid noise cancellation, and 45-hour playback on a single USB-C charge.', 299.99, 15, 42, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80', 4.85, 128, TRUE, TRUE),
(2, 2, 1, 'Mechanical Studio 75% Keyboard', 'mechanical-studio-keyboard', 'CNC anodized aluminum chassis, hot-swappable gasket mount switches, custom dampening foam, and Bluetooth 5.2 connectivity.', 189.50, 10, 24, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80', 4.90, 84, TRUE, TRUE),
(3, 2, 1, 'UltraView 4K Creator Monitor 27"', 'ultraview-4k-creator-monitor', 'IPS Black panel covering 98% DCI-P3 color gamut, hardware calibration, 90W USB-C power delivery, and ultra-thin bezels.', 649.00, 0, 15, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&auto=format&fit=crop&q=80', 4.75, 42, TRUE, TRUE),
(4, 2, 1, 'Precision Ergonomic Wireless Mouse', 'precision-ergonomic-mouse', 'Multi-device pairing, magspeed electromagnetic scroll wheel, 8000 DPI Darkfield tracking, and silent tactile switches.', 89.99, 5, 60, 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=800&auto=format&fit=crop&q=80', 4.70, 95, TRUE, TRUE),
(5, 3, 2, 'Nordic Oak Standing Desk 60x30', 'nordic-oak-standing-desk', 'Solid white oak desktop with dual synchronized motors, memory presets, anti-collision sensor, and integrated cable tray.', 749.00, 12, 10, 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800&auto=format&fit=crop&q=80', 4.95, 36, TRUE, TRUE),
(6, 3, 2, 'Ambient LED Desk Task Light', 'ambient-led-desk-light', 'High CRI >95 glare-free illumination, rotary aluminum dimmer dial, auto daylight tracking sensor, and wireless charging base.', 129.00, 0, 35, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&auto=format&fit=crop&q=80', 4.60, 52, TRUE, TRUE),
(7, 3, 2, 'Ceramic Minimalist Pour-Over Set', 'ceramic-pour-over-set', 'Hand-glazed ceramic dripper with double-walled thermal glass carafe and precision stainless steel reusable mesh filter.', 58.00, 0, 50, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&auto=format&fit=crop&q=80', 4.80, 68, TRUE, TRUE),
(8, 3, 3, 'Merino Wool Daily Crew Sweater', 'merino-wool-crew-sweater', '100% extra-fine 19.5 micron Australian Merino wool. Naturally odor-resistant, breathable, and temperature-regulating.', 145.00, 20, 18, 'https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=800&auto=format&fit=crop&q=80', 4.65, 41, TRUE, TRUE),
(9, 2, 5, 'Recovery Percussion Massage Gun', 'recovery-percussion-gun', 'Quiet brushless motor providing 3200 RPM percussion depth, 5 interchangeable therapy heads, and lightweight carry case.', 179.99, 15, 8, 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=800&auto=format&fit=crop&q=80', 4.80, 77, TRUE, TRUE),
(10, 3, 4, 'Principles of Clean Code & Architecture', 'principles-clean-code-book', 'Comprehensive guide to architectural separation of concerns, SOLID design patterns, and sustainable maintainability.', 49.99, 0, 85, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=80', 4.95, 110, TRUE, TRUE);

-- ---------------------------------------------------------------------
-- 4. SEED CARTS & CART ITEMS
-- ---------------------------------------------------------------------
INSERT INTO cart (cart_id, user_id) VALUES
(1, 4),
(2, 5);

INSERT INTO cart_items (cart_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 254.99),
(1, 4, 1, 85.49),
(2, 2, 1, 170.55);

-- ---------------------------------------------------------------------
-- 5. SEED ORDERS & ORDER ITEMS
-- ---------------------------------------------------------------------
INSERT INTO orders (order_id, order_number, buyer_id, total_amount, shipping_address, payment_method, order_status, payment_status, created_at) VALUES
(1, 'ORD-2026-8819', 4, 444.49, '742 Evergreen Terrace, Springfield, OR 97477', 'CREDIT_CARD', 'DELIVERED', 'PAID', '2026-09-15 14:22:00'),
(2, 'ORD-2026-9204', 4, 749.00, '742 Evergreen Terrace, Springfield, OR 97477', 'CREDIT_CARD', 'SHIPPED', 'PAID', '2026-10-02 10:15:30'),
(3, 'ORD-2026-9481', 5, 299.99, '124 Conch Street, Bikini Bottom, FL 33040', 'PAYPAL', 'PROCESSING', 'PAID', '2026-10-06 18:40:12');

INSERT INTO order_items (order_id, product_id, seller_id, product_name, product_image_url, quantity, unit_price, subtotal_price) VALUES
(1, 1, 2, 'ProNoise Wireless Studio Headphones', 'https://picsum.photos/seed/headphones/600/600', 1, 254.99, 254.99),
(1, 2, 2, 'Mechanical Studio 75% Keyboard', 'https://picsum.photos/seed/keyboard/600/600', 1, 189.50, 189.50),
(2, 5, 3, 'Nordic Oak Standing Desk 60x30', 'https://picsum.photos/seed/desk/600/600', 1, 749.00, 749.00),
(3, 1, 2, 'ProNoise Wireless Studio Headphones', 'https://picsum.photos/seed/headphones/600/600', 1, 299.99, 299.99);

-- ---------------------------------------------------------------------
-- 6. SEED REVIEWS
-- ---------------------------------------------------------------------
INSERT INTO reviews (review_id, product_id, user_id, rating, comment, created_at) VALUES
(1, 1, 4, 5, 'The soundstage on these headphones is extraordinarily crisp. Beryllium drivers make classical and electronic music shine.', '2026-09-20 16:30:00'),
(2, 1, 5, 5, 'Best battery life of any over-ear ANC headset I have tested. Super comfortable memory foam pads.', '2026-09-22 09:12:00'),
(3, 2, 4, 5, 'The CNC chassis feels solid as a brick and the gasket mount delivers that deep creamy thock sound profile.', '2026-09-25 11:45:00'),
(4, 5, 4, 5, 'Assembly took 25 minutes. Motors are silent and solid oak finish is stunning in natural lighting.', '2026-10-05 13:00:00');

-- ---------------------------------------------------------------------
-- 7. SEED WISHLIST
-- ---------------------------------------------------------------------
INSERT INTO wishlist (wishlist_id, user_id, product_id, added_at) VALUES
(1, 4, 3, '2026-09-18 10:00:00'),
(2, 4, 5, '2026-09-28 14:10:00'),
(3, 5, 4, '2026-10-04 16:22:00');
