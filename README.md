# Secondhand Fashion Marketplace Backend

Backend Spring Boot cho sàn thương mại bán thời trang cũ.

## Công nghệ chính
- Spring Boot
- Spring Web (REST API)
- Spring Data JPA
- MySQL
- Validation
- Spring Security + JWT
- Lombok
- Swagger / OpenAPI
- MapStruct
- Actuator

## Cấu trúc dự án

### Package Layout
```
src/main/java/com/be/
├── config/          # Spring configuration beans
├── common/          # Shared utilities, constants, enums, responses
├── exception/       # Exception handling
├── entity/          # JPA entities (17 entities)
├── repository/      # Spring Data repositories (17 repos)
├── service/         # Business logic layer
├── controller/      # REST API endpoints
├── dto/             # Data transfer objects
├── security/        # Authentication & JWT
├── spec/            # JPA Specifications
├── auth/            # Authentication feature
├── user/            # User management feature
├── product/         # Product catalog feature
├── category/        # Category management feature
├── order/           # Order management feature
└── file/            # File upload feature
```

### Database Entities

16 main entities mapped from MySQL schema:
- **User** - Users (customer, seller, admin)
- **Wallet** - E-wallet for each user
- **WalletTransaction** - Transaction history
- **Shop** - Seller shops
- **Category** - Product categories (hierarchical)
- **Product** - Products catalog
- **ProductImage, ProductAttribute, ProductTag** - Product details
- **Order** - Orders
- **OrderItem** - Order line items
- **OrderStatusLog** - Order status history
- **Review, ReviewImage** - Customer reviews
- **Comment** - Product comments (nested)
- **UserAddress** - Shipping addresses
- **RevenueSnapshot** - Daily revenue stats

For complete entity mapping details, see [ENTITY_MAPPING.md](ENTITY_MAPPING.md)

## Enum Types

- `UserRole` - CUSTOMER, SELLER, ADMIN
- `AuthProvider` - EMAIL, GOOGLE, FACEBOOK
- `ProductCondition` - NEW, LIKE_NEW, GOOD, FAIR
- `OrderStatus` - PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, REFUNDED
- `PaymentMethod` - WALLET, COD, BANK_TRANSFER
- `PaymentStatus` - UNPAID, PAID, REFUNDED
- `TransactionType` - TOPUP, PAYMENT, REFUND, WITHDRAWAL

## Cấu trúc thư mục đề xuất
```text
src/main/java/com/be/
├── config/
├── common/
├── exception/
├── entity/
├── repository/
├── service/
├── controller/
├── dto/
├── security/
└── spec/
```

## Setup hướng dẫn

### 1. Database Setup

Tạo database MySQL 9.1+:
```sql
CREATE DATABASE IF NOT EXISTS secondhand_fashion_marketplace
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Hoặc import full schema từ file SQL (nếu có).

### 2. Environment Variables

```bash
# .env hoặc system env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/secondhand_fashion_marketplace?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=dev
```

### 3. Build & Run

```powershell
# Build
.\mvnw.cmd clean install

# Run (dev profile)
$env:SPRING_PROFILES_ACTIVE='dev'; .\mvnw.cmd spring-boot:run

# Run tests
.\mvnw.cmd test
```

### 4. Swagger Documentation

Sau khi start app:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

### 5. Actuator Health Check

- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics

## Profile môi trường
- `dev`: phát triển
- `prod`: production
- `test`: kiểm thử

## Chạy ứng dụng
```powershell
./mvnw spring-boot:run
```

Nếu dùng profile khác:
```powershell
$env:SPRING_PROFILES_ACTIVE='dev'; ./mvnw spring-boot:run
```

## Swagger
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api-docs`

