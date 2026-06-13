USE secondhand_fashion_marketplace;

SET FOREIGN_KEY_CHECKS = 0;

-- USERS (1 admin, 2 seller, 3 customer - 1 bị banned)
INSERT INTO users (id, email, password_hash, full_name, phone, role, auth_provider, is_active, created_at, updated_at, email_verified_at) VALUES
(1, 'admin@secondhand.local',   '{noop}password', 'Admin System',    '0900000000', 'ADMIN',    'EMAIL', 1, NOW(), NOW(), NOW()),
(2, 'seller1@secondhand.local', '{noop}password', 'Nguyễn Văn Bán',  '0911111111', 'SELLER',   'EMAIL', 1, NOW(), NOW(), NOW()),
(3, 'seller2@secondhand.local', '{noop}password', 'Trần Thị Shop',   '0922222222', 'SELLER',   'EMAIL', 1, NOW(), NOW(), NOW()),
(4, 'buyer1@secondhand.local',  '{noop}password', 'Lê Văn Mua',      '0933333333', 'CUSTOMER', 'EMAIL', 1, NOW(), NOW(), NOW()),
(5, 'buyer2@secondhand.local',  '{noop}password', 'Phạm Thị Khách',  '0944444444', 'CUSTOMER', 'EMAIL', 1, NOW(), NOW(), NOW()),
(6, 'buyer3@secondhand.local',  '{noop}password', 'Hoàng Minh Tú',   '0955555555', 'CUSTOMER', 'EMAIL', 0, NOW(), NOW(), NOW());

-- WALLETS
INSERT INTO wallets (id, user_id, balance, created_at, updated_at) VALUES
(1, 1, 0.00,       NOW(), NOW()),
(2, 2, 5000000.00, NOW(), NOW()),
(3, 3, 3200000.00, NOW(), NOW()),
(4, 4, 1500000.00, NOW(), NOW()),
(5, 5, 800000.00,  NOW(), NOW()),
(6, 6, 0.00,       NOW(), NOW());

-- CATEGORIES
INSERT INTO categories (id, name, slug, sort_order, is_active, parent_id) VALUES
(1, 'Áo',        'ao',        10, 1, NULL),
(2, 'Quần',      'quan',      20, 1, NULL),
(3, 'Áo khoác',  'ao-khoac',  30, 1, NULL),
(4, 'Váy & Đầm', 'vay-dam',   40, 1, NULL),
(5, 'Phụ kiện',  'phu-kien',  50, 1, NULL),
(6, 'Áo thun',   'ao-thun',   11, 1, 1),
(7, 'Áo sơ mi',  'ao-so-mi',  12, 1, 1),
(8, 'Quần jeans','quan-jeans', 21, 1, 2);

-- SHOPS
INSERT INTO shops (id, seller_id, name, slug, description, rating_avg, total_reviews, is_active, is_verified, created_at, updated_at) VALUES
(1, 2, 'Shop Vintage Nguyễn', 'shop-vintage-nguyen', 'Chuyên đồ vintage secondhand chất lượng cao', 4.70, 32, 1, 1, NOW(), NOW()),
(2, 3, 'Trần Thị Fashion',    'tran-thi-fashion',    'Thời trang nữ secondhand giá tốt',            4.50, 18, 1, 1, NOW(), NOW());

-- USER ADDRESSES
INSERT INTO user_addresses (id, user_id, full_name, phone, province, district, ward, address_detail, is_default, created_at) VALUES
(1, 4, 'Lê Văn Mua',     '0933333333', 'Hồ Chí Minh', 'Quận 1',    'Bến Nghé',   '12 Nguyễn Huệ', 1, NOW()),
(2, 5, 'Phạm Thị Khách', '0944444444', 'Hà Nội',      'Hoàn Kiếm', 'Hàng Bài',   '45 Hàng Bài',   1, NOW()),
(3, 6, 'Hoàng Minh Tú',  '0955555555', 'Đà Nẵng',     'Hải Châu',  'Hải Châu 1', '22 Trần Phú',   1, NOW());

-- PRODUCTS (7 active, 1 inactive)
INSERT INTO products (id, shop_id, category_id, name, description, brand, origin_country, product_condition, base_price, sale_price, stock_quantity, rating_avg, total_reviews, is_active, created_at, updated_at) VALUES
(1, 1, 3, 'Áo blazer linen vintage',    'Áo blazer linen đã qua sử dụng, form rộng.',         'Vintage Select', 'Japan',   'GOOD',     520000, 410000, 7,  4.60, 8,  1, NOW(), NOW()),
(2, 1, 8, 'Quần jeans ống đứng washed', 'Quần jeans ống đứng màu xanh washed, còn tốt.',      'Levis',          'USA',     'LIKE_NEW', 680000, 590000, 5,  4.80, 12, 1, NOW(), NOW()),
(3, 1, 3, 'Áo khoác bomber đen',        'Bomber đen có túi trong, bo tay hơi xước nhẹ.',       'Uniqlo',         'Vietnam', 'FAIR',     450000, 320000, 3,  4.20, 5,  1, NOW(), NOW()),
(4, 1, 5, 'Túi tote canvas kem',        'Túi tote canvas dày dặn, phù hợp dùng hàng ngày.',   'Local Brand',    'Vietnam', 'NEW',      260000, 220000, 12, 4.90, 15, 1, NOW(), NOW()),
(5, 2, 6, 'Áo thun basic trắng',        'Áo thun cotton 100%, form regular, màu trắng tinh.', 'H&M',            'Vietnam', 'LIKE_NEW', 180000, 150000, 8,  4.50, 6,  1, NOW(), NOW()),
(6, 2, 7, 'Áo sơ mi kẻ caro vintage',   'Áo sơ mi kẻ caro flannel, màu đỏ trắng, size M.',    'Zara',           'Spain',   'GOOD',     350000, 280000, 4,  4.30, 9,  1, NOW(), NOW()),
(7, 2, 4, 'Váy midi hoa nhí',           'Váy midi vải lụa họa tiết hoa nhí, dài qua gối.',    'No Brand',       'Korea',   'LIKE_NEW', 420000, 360000, 6,  4.70, 11, 1, NOW(), NOW()),
(8, 1, 5, 'Mũ bucket caro (ngừng bán)', 'Sản phẩm inactive để test admin.',                   'No Brand',       'Korea',   'GOOD',     180000, 150000, 0,  0.00, 0,  0, NOW(), NOW());

-- PRODUCT IMAGES
INSERT INTO product_images (id, product_id, url, sort_order, is_primary) VALUES
(1,  1, 'https://images.unsplash.com/photo-1594938298603-c8148c4b4357?w=800', 0, 1),
(2,  2, 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=800',    0, 1),
(3,  3, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800',    0, 1),
(4,  4, 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=800',    0, 1),
(5,  5, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800', 0, 1),
(6,  6, 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800', 0, 1),
(7,  7, 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=800', 0, 1),
(8,  8, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800', 0, 1);

-- PRODUCT ATTRIBUTES
INSERT INTO product_attributes (id, product_id, attr_key, attr_value) VALUES
(1, 1, 'size', 'M'), (2, 1, 'color', 'Beige'),
(3, 2, 'size', '30'), (4, 2, 'color', 'Blue'),
(5, 3, 'size', 'L'), (6, 3, 'defect', 'Xước nhẹ bo tay'),
(7, 4, 'color', 'Cream'), (8, 5, 'size', 'M'),
(9, 6, 'size', 'M'), (10, 7, 'size', 'S/M');

-- PRODUCT TAGS
INSERT INTO product_tags (id, product_id, tag) VALUES
(1, 1, 'blazer'), (2, 1, 'vintage'),
(3, 2, 'jeans'),  (4, 2, 'denim'),
(5, 3, 'bomber'), (6, 3, 'streetwear'),
(7, 4, 'tote'),   (8, 4, 'canvas'),
(9, 5, 'basic'),  (10, 6, 'caro'),
(11, 7, 'midi'),  (12, 7, 'floral');

-- ORDERS (đủ 5 trạng thái)
INSERT INTO orders (id, customer_id, shop_id, order_code, shipping_address_id, subtotal, shipping_fee, status, payment_method, payment_status, cancel_reason, paid_at, delivered_at, created_at, updated_at) VALUES
(1, 4, 1, 'ORD-2026-0001', 1, 410000, 30000, 'PENDING',   'COD',           'UNPAID',   NULL,                        NULL,  NULL,  NOW(), NOW()),
(2, 4, 1, 'ORD-2026-0002', 1, 590000, 30000, 'CONFIRMED', 'WALLET',        'PAID',     NULL,                        NOW(), NULL,  NOW(), NOW()),
(3, 5, 2, 'ORD-2026-0003', 2, 280000, 25000, 'SHIPPING',  'BANK_TRANSFER', 'PAID',     NULL,                        NOW(), NULL,  NOW(), NOW()),
(4, 4, 2, 'ORD-2026-0004', 1, 360000, 25000, 'DONE',      'WALLET',        'PAID',     NULL,                        NOW(), NOW(), NOW(), NOW()),
(5, 5, 1, 'ORD-2026-0005', 2, 220000, 30000, 'CANCELLED', 'WALLET',        'REFUNDED', 'Khách đổi ý sau khi đặt',  NOW(), NULL,  NOW(), NOW()),
(6, 4, 1, 'ORD-2026-0006', 1, 320000, 30000, 'PENDING',   'COD',           'UNPAID',   NULL,                        NULL,  NULL,  NOW(), NOW()),
(7, 5, 2, 'ORD-2026-0007', 2, 150000, 25000, 'CONFIRMED', 'COD',           'UNPAID',   NULL,                        NULL,  NULL,  NOW(), NOW());

-- ORDER ITEMS
INSERT INTO order_items (id, order_id, product_id, product_name, unit_price, quantity, subtotal) VALUES
(1, 1, 1, 'Áo blazer linen vintage',    410000, 1, 410000),
(2, 2, 2, 'Quần jeans ống đứng washed', 590000, 1, 590000),
(3, 3, 6, 'Áo sơ mi kẻ caro vintage',   280000, 1, 280000),
(4, 4, 7, 'Váy midi hoa nhí',           360000, 1, 360000),
(5, 5, 4, 'Túi tote canvas kem',        220000, 1, 220000),
(6, 6, 3, 'Áo khoác bomber đen',        320000, 1, 320000),
(7, 7, 5, 'Áo thun basic trắng',        150000, 1, 150000);

-- ORDER STATUS LOGS
INSERT INTO order_status_logs (id, order_id, status, note, changed_by, created_at) VALUES
(1, 1, 'PENDING',   'Đơn hàng mới tạo',      4, NOW()),
(2, 2, 'PENDING',   'Đơn hàng mới tạo',      4, NOW()),
(3, 2, 'CONFIRMED', 'Người bán xác nhận',     2, NOW()),
(4, 3, 'PENDING',   'Đơn hàng mới tạo',      5, NOW()),
(5, 3, 'SHIPPING',  'Đang giao hàng',         3, NOW()),
(6, 4, 'DONE',      'Giao hàng thành công',   3, NOW()),
(7, 5, 'CANCELLED', 'Khách hủy đơn',          4, NOW()),
(8, 6, 'PENDING',   'Đơn hàng mới tạo',      4, NOW()),
(9, 7, 'PENDING',   'Đơn hàng mới tạo',      5, NOW());

SET FOREIGN_KEY_CHECKS = 1;
