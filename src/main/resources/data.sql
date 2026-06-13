-- Xóa dữ liệu cũ nếu có
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
TRUNCATE TABLE shops;
TRUNCATE TABLE categories;
TRUNCATE TABLE products;
SET FOREIGN_KEY_CHECKS = 1;

-- Chèn Admin (Mật khẩu: 123456)
INSERT INTO users (id, email, password_hash, full_name, role, auth_provider, is_active, created_at, updated_at) 
VALUES ('u1111111-1111-1111-1111-111111111111', 'admin@gmail.com', '$2a$10$Xl.S6.Yp7Y.FfG1z7GfGue.8UfG1z7GfGue.8UfG1z7GfGue', 'System Admin', 'ADMIN', 'EMAIL', 1, NOW(), NOW());

-- Chèn Shop
INSERT INTO shops (id, seller_id, name, slug, is_active, created_at, updated_at)
VALUES ('s1111111-1111-1111-1111-111111111111', 'u1111111-1111-1111-1111-111111111111', 'Admin Shop', 'admin-shop', 1, NOW(), NOW());

-- Chèn Category
INSERT INTO categories (id, name, slug, is_active, sort_order)
VALUES ('c1111111-1111-1111-1111-111111111111', 'Thời trang Nam', 'thoi-trang-nam', 1, 1);

-- Chèn Products
INSERT INTO products (id, shop_id, category_id, name, description, condition, base_price, stock_quantity, is_active, created_at, updated_at)
VALUES ('p1111111-1111-1111-1111-111111111111', 's1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111', 'Áo sơ mi Vintage', 'Áo sơ mi cổ điển chất liệu lụa', 'GOOD', 250000, 10, 1, NOW(), NOW());

INSERT INTO products (id, shop_id, category_id, name, description, condition, base_price, stock_quantity, is_active, created_at, updated_at)
VALUES ('p2222222-2222-2222-2222-222222222222', 's1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111', 'Quần Jean Levi\'s 501', 'Quần Jean cũ nhưng còn rất tốt', 'LIKE_NEW', 550000, 5, 1, NOW(), NOW());
