-- ============================================================
-- MIGRATION: Add GHN address fields & product dimensions
-- ============================================================
-- GHN API Token : db44e853-cc14-11ef-b1ed-769685acafa5
-- GHN Shop ID   : 2509459
-- Date          : 2026-06-22
-- ============================================================

USE secondhand_fashion_marketplace;

-- ============================================================
-- BƯỚC 1: Thêm cột địa chỉ GHN vào bảng shops
--         (province_id, district_id, ward_code, address_detail)
-- ============================================================

ALTER TABLE `shops`
    ADD COLUMN `province_id`    INT          NULL DEFAULT NULL COMMENT 'GHN Province ID',
    ADD COLUMN `province_name`  VARCHAR(100) NULL DEFAULT NULL COMMENT 'Tên tỉnh/thành',
    ADD COLUMN `district_id`    INT          NULL DEFAULT NULL COMMENT 'GHN District ID',
    ADD COLUMN `district_name`  VARCHAR(100) NULL DEFAULT NULL COMMENT 'Tên quận/huyện',
    ADD COLUMN `ward_code`      VARCHAR(20)  NULL DEFAULT NULL COMMENT 'GHN Ward Code',
    ADD COLUMN `ward_name`      VARCHAR(100) NULL DEFAULT NULL COMMENT 'Tên phường/xã',
    ADD COLUMN `address_detail` TEXT         NULL DEFAULT NULL COMMENT 'Địa chỉ chi tiết (số nhà, tên đường)';

-- ============================================================
-- BƯỚC 2: Thêm cột địa chỉ GHN vào bảng user_addresses
--         (GHN province_id, district_id, ward_code)
--         Bảng user_addresses đã có province, district, ward (text)
--         Thêm thêm các cột ID/code của GHN để tích hợp
-- ============================================================

ALTER TABLE `user_addresses`
    ADD COLUMN `province_id`   INT         NULL DEFAULT NULL COMMENT 'GHN Province ID',
    ADD COLUMN `district_id`   INT         NULL DEFAULT NULL COMMENT 'GHN District ID',
    ADD COLUMN `ward_code`     VARCHAR(20) NULL DEFAULT NULL COMMENT 'GHN Ward Code';

-- ============================================================
-- BƯỚC 3: Thêm cột kích thước mặc định vào bảng products
--         weight(g), length(cm), width(cm), height(cm)
-- ============================================================

ALTER TABLE `products`
    ADD COLUMN `weight`    INT NOT NULL DEFAULT 500  COMMENT 'Khối lượng (gram), mặc định 500g',
    ADD COLUMN `length`    INT NOT NULL DEFAULT 20   COMMENT 'Chiều dài (cm), mặc định 20cm',
    ADD COLUMN `width`     INT NOT NULL DEFAULT 15   COMMENT 'Chiều rộng (cm), mặc định 15cm',
    ADD COLUMN `height`    INT NOT NULL DEFAULT 5    COMMENT 'Chiều cao (cm), mặc định 5cm';

-- ============================================================
-- BƯỚC 4: Populate địa chỉ GHN cho shops hiện tại (random)
-- Danh sách địa chỉ hợp lệ theo GHN:
--
-- TP. Hồ Chí Minh (province_id=202):
--   Quận 1 (district_id=1442): ward_code=20014 (Phường Bến Thành)
--   Quận 3 (district_id=1444): ward_code=20301 (Phường 1)
--   Quận Bình Thạnh (district_id=1462): ward_code=21601 (Phường 1)
--
-- Hà Nội (province_id=201):
--   Quận Hoàn Kiếm (district_id=1489): ward_code=1A0207 (Phường Hàng Bồ)
--   Quận Cầu Giấy (district_id=1485): ward_code=1A0601 (Phường Dịch Vọng)
--
-- Đà Nẵng (province_id=203):
--   Quận Hải Châu (district_id=1526): ward_code=20210 (Phường Bình Thuận)
-- ============================================================

-- Tạo stored procedure để assign địa chỉ random cho shops
DROP PROCEDURE IF EXISTS assign_shop_addresses;

DELIMITER $$

CREATE PROCEDURE assign_shop_addresses()
BEGIN
    DECLARE done         INT DEFAULT 0;
    DECLARE v_shop_id    CHAR(36);
    DECLARE v_rand       INT;

    DECLARE cur CURSOR FOR SELECT id FROM shops;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;

    shop_loop: LOOP
        FETCH cur INTO v_shop_id;
        IF done = 1 THEN
            LEAVE shop_loop;
        END IF;

        -- Random 0..5 → 6 địa chỉ mẫu
        SET v_rand = FLOOR(RAND() * 6);

        CASE v_rand
            WHEN 0 THEN
                UPDATE shops SET
                    province_id    = 202,
                    province_name  = 'TP. Hồ Chí Minh',
                    district_id    = 1442,
                    district_name  = 'Quận 1',
                    ward_code      = '20014',
                    ward_name      = 'Phường Bến Thành',
                    address_detail = CONCAT(FLOOR(RAND()*200+1), ' Lê Lợi, Phường Bến Thành')
                WHERE id = v_shop_id;

            WHEN 1 THEN
                UPDATE shops SET
                    province_id    = 202,
                    province_name  = 'TP. Hồ Chí Minh',
                    district_id    = 1444,
                    district_name  = 'Quận 3',
                    ward_code      = '20303',
                    ward_name      = 'Phường 3',
                    address_detail = CONCAT(FLOOR(RAND()*150+1), ' Nguyễn Đình Chiểu, Phường 3')
                WHERE id = v_shop_id;

            WHEN 2 THEN
                UPDATE shops SET
                    province_id    = 202,
                    province_name  = 'TP. Hồ Chí Minh',
                    district_id    = 1462,
                    district_name  = 'Quận Bình Thạnh',
                    ward_code      = '21612',
                    ward_name      = 'Phường 17',
                    address_detail = CONCAT(FLOOR(RAND()*300+1), ' Xô Viết Nghệ Tĩnh, Phường 17')
                WHERE id = v_shop_id;

            WHEN 3 THEN
                UPDATE shops SET
                    province_id    = 201,
                    province_name  = 'Hà Nội',
                    district_id    = 1489,
                    district_name  = 'Quận Hoàn Kiếm',
                    ward_code      = '1A0207',
                    ward_name      = 'Phường Hàng Bồ',
                    address_detail = CONCAT(FLOOR(RAND()*50+1), ' Hàng Bồ, Phường Hàng Bồ')
                WHERE id = v_shop_id;

            WHEN 4 THEN
                UPDATE shops SET
                    province_id    = 201,
                    province_name  = 'Hà Nội',
                    district_id    = 1485,
                    district_name  = 'Quận Cầu Giấy',
                    ward_code      = '1A0601',
                    ward_name      = 'Phường Dịch Vọng',
                    address_detail = CONCAT(FLOOR(RAND()*200+1), ' Xuân Thủy, Phường Dịch Vọng')
                WHERE id = v_shop_id;

            WHEN 5 THEN
                UPDATE shops SET
                    province_id    = 203,
                    province_name  = 'Đà Nẵng',
                    district_id    = 1526,
                    district_name  = 'Quận Hải Châu',
                    ward_code      = '550504',
                    ward_name      = 'Phường Bình Thuận',
                    address_detail = CONCAT(FLOOR(RAND()*100+1), ' Trần Phú, Phường Bình Thuận')
                WHERE id = v_shop_id;
        END CASE;

    END LOOP;

    CLOSE cur;
END$$

DELIMITER ;

-- Chạy procedure
CALL assign_shop_addresses();
DROP PROCEDURE IF EXISTS assign_shop_addresses;

-- ============================================================
-- BƯỚC 5: Populate địa chỉ GHN cho user_addresses hiện tại
-- ============================================================

DROP PROCEDURE IF EXISTS assign_user_addresses;

DELIMITER $$

CREATE PROCEDURE assign_user_addresses()
BEGIN
    DECLARE done          INT DEFAULT 0;
    DECLARE v_addr_id     CHAR(36);
    DECLARE v_rand        INT;

    DECLARE cur CURSOR FOR SELECT id FROM user_addresses;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;

    addr_loop: LOOP
        FETCH cur INTO v_addr_id;
        IF done = 1 THEN
            LEAVE addr_loop;
        END IF;

        SET v_rand = FLOOR(RAND() * 8);

        CASE v_rand
            WHEN 0 THEN
                UPDATE user_addresses SET
                    province_id  = 202,
                    district_id  = 1442,
                    ward_code    = '20014',
                    province     = 'TP. Hồ Chí Minh',
                    district     = 'Quận 1',
                    ward         = 'Phường Bến Thành',
                    address_detail = CONCAT(FLOOR(RAND()*200+1), ' Nguyễn Huệ, Phường Bến Thành')
                WHERE id = v_addr_id;

            WHEN 1 THEN
                UPDATE user_addresses SET
                    province_id  = 202,
                    district_id  = 1442,
                    ward_code    = '20012',
                    province     = 'TP. Hồ Chí Minh',
                    district     = 'Quận 1',
                    ward         = 'Phường Cầu Kho',
                    address_detail = CONCAT(FLOOR(RAND()*100+1), ' Calmette, Phường Cầu Kho')
                WHERE id = v_addr_id;

            WHEN 2 THEN
                UPDATE user_addresses SET
                    province_id  = 202,
                    district_id  = 1444,
                    ward_code    = '20310',
                    province     = 'TP. Hồ Chí Minh',
                    district     = 'Quận 3',
                    ward         = 'Phường 10',
                    address_detail = CONCAT(FLOOR(RAND()*200+1), ' Võ Thị Sáu, Phường 10')
                WHERE id = v_addr_id;

            WHEN 3 THEN
                UPDATE user_addresses SET
                    province_id  = 202,
                    district_id  = 1462,
                    ward_code    = '21604',
                    province     = 'TP. Hồ Chí Minh',
                    district     = 'Quận Bình Thạnh',
                    ward         = 'Phường 5',
                    address_detail = CONCAT(FLOOR(RAND()*300+1), ' Đinh Bộ Lĩnh, Phường 5')
                WHERE id = v_addr_id;

            WHEN 4 THEN
                UPDATE user_addresses SET
                    province_id  = 202,
                    district_id  = 1462,
                    ward_code    = '21611',
                    province     = 'TP. Hồ Chí Minh',
                    district     = 'Quận Bình Thạnh',
                    ward         = 'Phường 15',
                    address_detail = CONCAT(FLOOR(RAND()*200+1), ' Nơ Trang Long, Phường 15')
                WHERE id = v_addr_id;

            WHEN 5 THEN
                UPDATE user_addresses SET
                    province_id  = 201,
                    district_id  = 1489,
                    ward_code    = '1A0211',
                    province     = 'Hà Nội',
                    district     = 'Quận Hoàn Kiếm',
                    ward         = 'Phường Hàng Gai',
                    address_detail = CONCAT(FLOOR(RAND()*50+1), ' Hàng Gai, Phường Hàng Gai')
                WHERE id = v_addr_id;

            WHEN 6 THEN
                UPDATE user_addresses SET
                    province_id  = 201,
                    district_id  = 1485,
                    ward_code    = '1A0603',
                    province     = 'Hà Nội',
                    district     = 'Quận Cầu Giấy',
                    ward         = 'Phường Mai Dịch',
                    address_detail = CONCAT(FLOOR(RAND()*100+1), ' Trần Thái Tông, Phường Mai Dịch')
                WHERE id = v_addr_id;

            WHEN 7 THEN
                UPDATE user_addresses SET
                    province_id  = 203,
                    district_id  = 1526,
                    ward_code    = '550504',
                    province     = 'Đà Nẵng',
                    district     = 'Quận Hải Châu',
                    ward         = 'Phường Bình Thuận',
                    address_detail = CONCAT(FLOOR(RAND()*150+1), ' Nguyễn Văn Linh, Phường Bình Thuận')
                WHERE id = v_addr_id;
        END CASE;

    END LOOP;

    CLOSE cur;
END$$

DELIMITER ;

CALL assign_user_addresses();
DROP PROCEDURE IF EXISTS assign_user_addresses;

-- ============================================================
-- BƯỚC 6: Nếu chưa có record nào trong user_addresses,
--         tạo địa chỉ mặc định cho tất cả users hiện tại
-- ============================================================

-- Tạo user_addresses cho users chưa có địa chỉ nào
INSERT INTO user_addresses (id, user_id, full_name, phone, province, district, ward, address_detail,
                             province_id, district_id, ward_code, is_default, created_at)
SELECT
    UUID(),
    u.id,
    u.full_name,
    COALESCE(u.phone, CONCAT('09', LPAD(FLOOR(RAND()*100000000), 8, '0'))),
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'TP. Hồ Chí Minh'
        WHEN 1 THEN 'Hà Nội'
        ELSE 'Đà Nẵng'
    END,
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'Quận 1'
        WHEN 1 THEN 'Quận Hoàn Kiếm'
        ELSE 'Quận Hải Châu'
    END,
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'Phường Bến Thành'
        WHEN 1 THEN 'Phường Hàng Bồ'
        ELSE 'Phường Bình Thuận'
    END,
    CONCAT(FLOOR(RAND()*200+1), ' Lê Thánh Tôn'),
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 202
        WHEN 1 THEN 201
        ELSE 203
    END,
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 1442
        WHEN 1 THEN 1489
        ELSE 1526
    END,
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN '20014'
        WHEN 1 THEN '1A0207'
        ELSE '550504'
    END,
    1,
    NOW()
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM user_addresses ua WHERE ua.user_id = u.id
);

-- ============================================================
-- BƯỚC 7: Xác nhận thay đổi
-- ============================================================

-- Kiểm tra shops đã có địa chỉ
SELECT 
    id, name, province_name, district_name, ward_name, address_detail
FROM shops
LIMIT 10;

-- Kiểm tra user_addresses đã có GHN IDs
SELECT 
    ua.id, u.full_name, ua.province, ua.district, ua.ward,
    ua.province_id, ua.district_id, ua.ward_code,
    ua.address_detail
FROM user_addresses ua
JOIN users u ON u.id = ua.user_id
LIMIT 10;

-- Kiểm tra products có dimensions
SELECT id, name, weight, length, width, height FROM products LIMIT 5;

-- Summary
SELECT 
    'shops'          AS tbl, COUNT(*) AS total, SUM(province_id IS NOT NULL) AS has_address FROM shops
UNION ALL
SELECT 
    'user_addresses' AS tbl, COUNT(*) AS total, SUM(province_id IS NOT NULL) AS has_address FROM user_addresses
UNION ALL
SELECT 
    'products'       AS tbl, COUNT(*) AS total, SUM(weight = 500) AS default_weight FROM products;
