/*
  Schema updated to use BIGINT AUTO_INCREMENT for compatibility with legacy data.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE secondhand_fashion_marketplace;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(150) NOT NULL,
  `icon_url` text NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uq_categories_slug`(`slug`),
  CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NULL,
  `full_name` varchar(150) NOT NULL,
  `role` enum('CUSTOMER','SELLER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uq_users_email`(`email`)
) ENGINE = InnoDB;

-- ----------------------------
-- Table structure for shops
-- ----------------------------
DROP TABLE IF EXISTS `shops`;
CREATE TABLE `shops`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `seller_id` bigint NOT NULL,
  `name` varchar(150) NOT NULL,
  `slug` varchar(200) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uq_shops_seller`(`seller_id`),
  CONSTRAINT `fk_shops_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB;

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shop_id` bigint NOT NULL,
  `category_id` bigint NULL,
  `name` varchar(255) NOT NULL,
  `base_price` decimal(15, 2) NOT NULL,
  `stock_quantity` int NOT NULL DEFAULT 0,
  `condition` enum('NEW','LIKE_NEW','GOOD','FAIR') NOT NULL DEFAULT 'GOOD',
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_products_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB;

SET FOREIGN_KEY_CHECKS = 1;
