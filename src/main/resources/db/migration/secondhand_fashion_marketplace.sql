/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 90100 (9.1.0)
 Source Host           : localhost:3306
 Source Schema         : secondhand_fashion_marketplace

 Target Server Type    : MySQL
 Target Server Version : 90100 (9.1.0)
 File Encoding         : 65001

 Date: 10/05/2026 09:13:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS secondhand_fashion_marketplace;
CREATE DATABASE secondhand_fashion_marketplace
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE secondhand_fashion_marketplace;

-- ============================================================
-- DROP TABLES (thứ tự ngược dependency: bảng con trước)
-- ============================================================
DROP TABLE IF EXISTS `review_images`;
DROP TABLE IF EXISTS `product_tags`;
DROP TABLE IF EXISTS `product_images`;
DROP TABLE IF EXISTS `product_attributes`;
DROP TABLE IF EXISTS `order_status_logs`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `wallet_transactions`;
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `revenue_snapshots`;
DROP TABLE IF EXISTS `campaign_products`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `coupons`;
DROP TABLE IF EXISTS `campaigns`;
DROP TABLE IF EXISTS `wallets`;
DROP TABLE IF EXISTS `user_addresses`;
DROP TABLE IF EXISTS `shops`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;

-- ============================================================
-- CREATE TABLES
-- ============================================================

-- secondhand_fashion_marketplace.users definition

CREATE TABLE `users` (
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email_verified_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_url` text COLLATE utf8mb4_unicode_ci,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `auth_provider` enum('EMAIL','FACEBOOK','GOOGLE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','CUSTOMER','SELLER') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_provider` (`auth_provider`,`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.categories definition

CREATE TABLE `categories` (
  `is_active` bit(1) NOT NULL,
  `sort_order` int NOT NULL,
  `id` bigint NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon_url` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_categories_slug` (`slug`),
  KEY `idx_categories_parent` (`parent_id`),
  CONSTRAINT `FKsaok720gsu4u2wrgbk10b5n8d` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.shops definition

CREATE TABLE `shops` (
  `is_active` bit(1) NOT NULL,
  `is_verified` bit(1) NOT NULL,
  `rating_avg` decimal(3,2) NOT NULL,
  `total_reviews` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_url` text COLLATE utf8mb4_unicode_ci,
  `banner_url` text COLLATE utf8mb4_unicode_ci,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_shops_seller` (`seller_id`),
  UNIQUE KEY `uq_shops_slug` (`slug`),
  CONSTRAINT `FKqu7p50yuiaukogi05ibair0ru` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.coupons definition

CREATE TABLE `coupons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `discount_type` enum('PERCENTAGE', 'FIXED_AMOUNT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_value` decimal(15,2) NOT NULL,
  `min_order_value` decimal(15,2) DEFAULT '0.00',
  `max_discount_amount` decimal(15,2) DEFAULT NULL,
  `usage_limit` int DEFAULT NULL,
  `used_count` int DEFAULT 0,
  `start_date` datetime(6) NOT NULL,
  `end_date` datetime(6) NOT NULL,
  `created_by` enum('ADMIN', 'SELLER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `shop_id` bigint DEFAULT NULL,
  `is_active` bit(1) NOT NULL DEFAULT b'1',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_coupons_code` (`code`),
  KEY `idx_coupons_code` (`code`),
  KEY `idx_coupons_shop` (`shop_id`),
  CONSTRAINT `fk_coupons_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.campaigns definition

CREATE TABLE `campaigns` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `banner_url` text COLLATE utf8mb4_unicode_ci,
  `start_date` datetime(6) NOT NULL,
  `end_date` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL DEFAULT b'1',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_campaigns_dates` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.user_addresses definition

CREATE TABLE `user_addresses` (
  `is_default` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `district` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ward` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address_detail` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_addresses_user` (`user_id`),
  CONSTRAINT `FKn2fisxyyu3l9wlch3ve2nocgp` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.wallets definition

CREATE TABLE `wallets` (
  `balance` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wallets_user` (`user_id`),
  CONSTRAINT `FKc1foyisidw7wqqrkamafuwn4e` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.orders definition

CREATE TABLE `orders` (
  `shipping_fee` decimal(15,2) NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  `discount_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `coupon_id` bigint DEFAULT NULL,
  `promotion_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `customer_id` bigint NOT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `shipping_address_id` bigint DEFAULT NULL,
  `shop_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `order_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cancel_reason` text COLLATE utf8mb4_unicode_ci,
  `payment_method` enum('BANK_TRANSFER','COD','WALLET') COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` enum('PAID','REFUNDED','UNPAID') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CANCELLED','DONE','PENDING','SHIPPING', 'CONFIRMED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_orders_code` (`order_code`),
  KEY `idx_orders_customer` (`customer_id`),
  KEY `idx_orders_shop` (`shop_id`),
  KEY `idx_orders_status` (`status`),
  KEY `idx_orders_created` (`created_at`),
  KEY `idx_orders_coupon` (`coupon_id`),
  KEY `idx_orders_promotion` (`promotion_id`),
  KEY `FKq2dfcmpxmg3lqseeacd48f12k` (`shipping_address_id`),
  CONSTRAINT `FK21gttsw5evi5bbsvleui69d7r` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
  CONSTRAINT `FKq2dfcmpxmg3lqseeacd48f12k` FOREIGN KEY (`shipping_address_id`) REFERENCES `user_addresses` (`id`),
  CONSTRAINT `FKsjfs85qf6vmcurlx43cnc16gy` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_orders_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`),
  CONSTRAINT `fk_orders_promotion` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.products definition

CREATE TABLE `products` (
  `base_price` decimal(15,2) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `rating_avg` decimal(3,2) NOT NULL,
  `sale_price` decimal(15,2) DEFAULT NULL,
  `stock_quantity` int NOT NULL,
  `total_reviews` int NOT NULL,
  `category_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `shop_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origin_country` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_condition` enum('FAIR','GOOD','LIKE_NEW','NEW') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_products_shop` (`shop_id`),
  KEY `idx_products_category` (`category_id`),
  KEY `idx_products_condition` (`product_condition`),
  KEY `idx_products_price` (`sale_price`,`base_price`),
  KEY `idx_products_created` (`created_at`),
  CONSTRAINT `FK7kp8sbhxboponhx3lxqtmkcoj` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.campaign_products definition

CREATE TABLE `campaign_products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campaign_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `campaign_price` decimal(15,2) NOT NULL,
  `status` enum('PENDING', 'APPROVED', 'REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_campaign_product` (`campaign_id`, `product_id`),
  KEY `idx_campaign_products_campaign` (`campaign_id`),
  KEY `idx_campaign_products_product` (`product_id`),
  CONSTRAINT `fk_campaign_products_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`id`),
  CONSTRAINT `fk_campaign_products_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.revenue_snapshots definition

CREATE TABLE `revenue_snapshots` (
  `net_revenue` decimal(15,2) NOT NULL,
  `platform_fee` decimal(15,2) NOT NULL,
  `snapshot_date` date NOT NULL,
  `total_items_sold` int NOT NULL,
  `total_orders` int NOT NULL,
  `total_revenue` decimal(15,2) NOT NULL,
  `id` bigint NOT NULL,
  `shop_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_revenue_shop_date` (`shop_id`,`snapshot_date`),
  KEY `idx_revenue_date` (`snapshot_date`),
  CONSTRAINT `FKajmf8lr4ft1dj4oarxii92ofx` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.reviews definition

CREATE TABLE `reviews` (
  `is_visible` bit(1) NOT NULL,
  `rating` tinyint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `customer_id` bigint NOT NULL,
  `id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_reviews_order_product` (`order_id`,`product_id`),
  KEY `idx_reviews_product` (`product_id`),
  KEY `idx_reviews_customer` (`customer_id`),
  CONSTRAINT `FKkquncb1glvrldaui8v52xfd5q` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpl51cejpw4gy5swfar8br9ngi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKqwgq1lxgahsxdspnwqfac6sv6` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.wallet_transactions definition

CREATE TABLE `wallet_transactions` (
  `amount` decimal(15,2) NOT NULL,
  `balance_after` decimal(15,2) NOT NULL,
  `balance_before` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `wallet_id` bigint NOT NULL,
  `reference_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `tx_type` enum('PAYMENT','REFUND','TOPUP','WITHDRAWAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wallet_tx_wallet` (`wallet_id`),
  KEY `idx_wallet_tx_order` (`order_id`),
  CONSTRAINT `FK8seu7b87ifqi09ghhssusmb0x` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`),
  CONSTRAINT `FKetfi6nehbld4k4ma3oshxpp8j` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.comments definition

CREATE TABLE `comments` (
  `is_visible` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comments_product` (`product_id`),
  KEY `idx_comments_parent` (`parent_id`),
  KEY `idx_comments_user` (`user_id`),
  CONSTRAINT `FK6uv0qku8gsu6x1r2jkrtqwjtn` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKlri30okf66phtcgbe5pok7cc0` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.order_items definition

CREATE TABLE `order_items` (
  `quantity` int NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  `discount_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `unit_price` decimal(15,2) NOT NULL,
  `id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_items_order` (`order_id`),
  KEY `idx_order_items_product` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.order_status_logs definition

CREATE TABLE `order_status_logs` (
  `changed_by` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `status` enum('CANCELLED','DONE','PENDING','SHIPPING') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_logs_order` (`order_id`),
  KEY `fk_order_logs_user` (`changed_by`),
  CONSTRAINT `FKfs3dssq3pkgrwr97m6kr8xvl` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpoehv8fptppd81oysnw7l44by` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.product_attributes definition

CREATE TABLE `product_attributes` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `attr_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `attr_value` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product_attributes_product` (`product_id`),
  CONSTRAINT `FKcex46yvx4g18b2pn09p79h1mc` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.product_images definition

CREATE TABLE `product_images` (
  `is_primary` bit(1) NOT NULL,
  `sort_order` int NOT NULL,
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `url` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product_images_product` (`product_id`),
  CONSTRAINT `FKqnq71xsohugpqwf3c9gxmsuy` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.product_tags definition

CREATE TABLE `product_tags` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `tag` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product_tags_product` (`product_id`),
  KEY `idx_product_tags_tag` (`tag`),
  CONSTRAINT `FK5rk6s19k3risy7q7wqdr41uss` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- secondhand_fashion_marketplace.review_images definition

CREATE TABLE `review_images` (
  `id` bigint NOT NULL,
  `review_id` bigint NOT NULL,
  `url` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_review_images_review` (`review_id`),
  CONSTRAINT `FK3aayo5bjciyemf3bvvt987hkr` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;