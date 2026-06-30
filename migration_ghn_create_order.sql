ALTER TABLE orders ADD COLUMN ghn_order_code VARCHAR(50) NULL;
ALTER TABLE orders ADD COLUMN expected_delivery_time DATETIME NULL;
ALTER TABLE orders ADD COLUMN ghn_total_fee DECIMAL(15,2) NULL;
