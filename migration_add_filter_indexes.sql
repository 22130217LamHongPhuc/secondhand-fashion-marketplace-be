-- Products: shop + date range filter
CREATE INDEX idx_products_shop_created ON products(shop_id, created_at);

-- Products: shop + price filter (salePrice fallback basePrice)
CREATE INDEX idx_products_shop_saleprice ON products(shop_id, sale_price, base_price);

-- Orders: shop + date range
CREATE INDEX idx_orders_shop_created ON orders(shop_id, created_at);

-- Orders: shop + status + date range (most common query)
CREATE INDEX idx_orders_shop_status_created ON orders(shop_id, status, created_at);

-- Orders: shop + subtotal
CREATE INDEX idx_orders_shop_subtotal ON orders(shop_id, subtotal);

-- Promotions: shop + date range
CREATE INDEX idx_promotions_shop_created ON promotions(shop_id, created_at);

-- Promotions: shop + discount_value
CREATE INDEX idx_promotions_shop_discountvalue ON promotions(shop_id, discount_value);

-- Promotions: shop + keyword search (code + name)
CREATE INDEX idx_promotions_shop_code_name ON promotions(shop_id, code, name);
