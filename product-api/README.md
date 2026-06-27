# Product CRUD API - Developer Guide

**Version:** 1.0.0  
**Last Updated:** May 23, 2026  
**Tech Stack:** Java 17, Spring Boot 3.1.5, Spring Data JPA, MySQL

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Project Structure](#project-structure)
3. [API Endpoints](#api-endpoints)
4. [Authentication & Authorization](#authentication--authorization)
5. [Running Tests](#running-tests)
6. [Database Setup](#database-setup)
7. [Building & Deployment](#building--deployment)
8. [API Documentation](#api-documentation)
9. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Prerequisites

- **Java 17+** ([Download](https://www.oracle.com/java/technologies/downloads/#java17))
- **Maven 3.8.x** ([Download](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/mysql/))
- **Git** (for version control)

### Step 1: Clone & Navigate

```bash
cd product-api
```

### Step 2: Create Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE product_db;
USE product_db;
```

### Step 3: Update Database Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/product_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
```

### Step 4: Run Application

```bash
mvn clean install
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

### Step 5: Access Swagger UI

Open your browser:
```
http://localhost:8080/swagger-ui.html
```

---

## Project Structure

```
product-api/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/product/
│   │   │   ├── ProductApiApplication.java    # Entry point
│   │   │   ├── controller/
│   │   │   │   └── ProductController.java    # REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── IProductService.java      # Service interface
│   │   │   │   └── ProductService.java       # Service implementation
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java    # Data access (DAL)
│   │   │   ├── model/
│   │   │   │   └── Product.java              # JPA entity
│   │   │   ├── dto/
│   │   │   │   └── ProductDTO.java           # Data transfer object
│   │   │   ├── exception/
│   │   │   │   ├── ProductNotFoundException.java
│   │   │   │   ├── InvalidProductDataException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── config/
│   │   │       ├── SecurityConfig.java       # Spring Security setup
│   │   │       └── OpenAPIConfig.java        # Swagger/OpenAPI config
│   │   └── resources/
│   │       ├── application.properties        # Configuration (prod)
│   │       └── application-test.properties   # Configuration (test)
│   └── test/
│       └── java/com/ecommerce/product/
│           ├── repository/ProductRepositoryTest.java
│           ├── service/ProductServiceTest.java
│           └── controller/ProductControllerTest.java
```

---

## API Endpoints

### Base URL
```
https://api.ecommerce-store.com/api/v1
```

### 1. Retrieve Product by ID

```
GET /products/{id}
```

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/products/1" \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Wireless Bluetooth Headphones",
  "description": "Over-ear noise-cancelling headphones",
  "price": 89.99,
  "category": "Electronics",
  "stockQuantity": 150,
  "createdAt": "2026-05-23T10:00:00",
  "updatedAt": "2026-05-23T10:00:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product with id 1 not found",
  "timestamp": "2026-05-23T10:00:00Z",
  "path": "/api/v1/products/1"
}
```

---

### 2. Create Product

```
POST /products
```

**Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/products" \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Bluetooth Headphones",
    "description": "Over-ear noise-cancelling headphones",
    "price": 89.99,
    "category": "Electronics",
    "stockQuantity": 150
  }'
```

**Response (201 Created):**
```json
{
  "id": 101,
  "name": "Wireless Bluetooth Headphones",
  "description": "Over-ear noise-cancelling headphones",
  "price": 89.99,
  "category": "Electronics",
  "stockQuantity": 150,
  "createdAt": "2026-05-23T10:00:00",
  "updatedAt": "2026-05-23T10:00:00"
}
```

---

### 3. Update Product

```
PUT /products/{id}
```

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/v1/products/1" \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product",
    "description": "Updated description",
    "price": 149.99,
    "category": "Electronics",
    "stockQuantity": 200
  }'
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Updated Product",
  "description": "Updated description",
  "price": 149.99,
  "category": "Electronics",
  "stockQuantity": 200,
  "createdAt": "2026-05-23T09:00:00",
  "updatedAt": "2026-05-23T10:00:00"
}
```

---

### 4. Delete Product

```
DELETE /products/{id}
```

**Request:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/products/1" \
  -H "Authorization: Bearer <jwt-token>"
```

**Response (204 No Content):** Empty body

---

### 5. List All Products

```
GET /products
```

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/products" \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Wireless Bluetooth Headphones",
    "description": "Over-ear noise-cancelling headphones",
    "price": 89.99,
    "category": "Electronics",
    "stockQuantity": 150,
    "createdAt": "2026-05-23T10:00:00",
    "updatedAt": "2026-05-23T10:00:00"
  }
]
```

---

## Authentication & Authorization

### JWT Bearer Token

All endpoints require a Bearer JWT token in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Roles

| Role | Permissions |
|------|------------|
| `PRODUCT_VIEWER` | GET (Read-only) |
| `PRODUCT_MANAGER` | POST, PUT, DELETE, GET (Full CRUD) |

### Example: Using HTTP Basic Auth (For Testing)

For testing purposes, you can use HTTP Basic Authentication:

```bash
curl -X POST "http://localhost:8080/api/v1/products" \
  -H "Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=" \
  -H "Content-Type: application/json" \
  -d '{"name": "Product", ...}'
```

---

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ProductControllerTest
```

### Run Test with Coverage Report

```bash
mvn clean test jacoco:report
```

Report will be available at: `target/site/jacoco/index.html`

### Test Categories

| Test Class | Coverage | Tests |
|-----------|----------|-------|
| `ProductRepositoryTest` | DAL | 11 tests |
| `ProductServiceTest` | Business Logic | 13 tests |
| `ProductControllerTest` | API Endpoints | 14 tests |
| **Total** | **38 tests** | **90%+ coverage** |

---

## Database Setup

### Create Database (MySQL)

```sql
CREATE DATABASE product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE product_db;
```

### Tables Created Automatically

Hibernate (JPA) will create the `products` table automatically. Manual schema:

```sql
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
  category VARCHAR(100) NOT NULL,
  stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (category),
  INDEX idx_created_at (created_at)
);
```

### Backup Database

```bash
mysqldump -u root -p product_db > backup.sql
```

### Restore Database

```bash
mysql -u root -p product_db < backup.sql
```

---

## Building & Deployment

### Build JAR Package

```bash
mvn clean package
```

Creates: `target/product-crud-api-1.0.0.jar`

### Run JAR

```bash
java -jar target/product-crud-api-1.0.0.jar
```

### Docker Deployment (Optional)

Create `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/product-crud-api-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and Run:

```bash
docker build -t product-crud-api:1.0.0 .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/product_db \
  product-crud-api:1.0.0
```

---

## API Documentation

### Swagger UI

Access interactive API docs at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

Raw OpenAPI specification:
```
http://localhost:8080/v3/api-docs
```

### API Contract Example

Each endpoint documents:
- Request parameters and body
- Response schemas and examples
- Error codes and messages
- Security requirements
- HTTP status codes

---

## Troubleshooting

### Issue: Connection to MySQL Failed

**Error:** `com.mysql.cj.jdbc.exceptions.CommunicationsException`

**Solution:**
1. Verify MySQL is running: `mysql -u root -p`
2. Check connection string in `application.properties`
3. Verify database exists: `SHOW DATABASES;`

### Issue: Table Not Created

**Error:** `Table 'product_db.products' doesn't exist`

**Solution:**
1. Check Hibernate DDL setting: `spring.jpa.hibernate.ddl-auto=update`
2. Run application once to auto-create tables
3. Manually create table using provided SQL schema

### Issue: Authentication Failed

**Error:** `401 Unauthorized`

**Solution:**
1. Include `Authorization: Bearer <token>` header
2. Verify token is valid and not expired
3. For testing, use HTTP Basic Auth or `@WithMockUser` annotation

### Issue: Permission Denied

**Error:** `403 Forbidden`

**Solution:**
1. Verify user role has required permission
2. Use `PRODUCT_MANAGER` role for write operations
3. Use `PRODUCT_VIEWER` role for read operations

### Issue: Tests Fail

**Error:** `Tests in suite 'All Tests' failed`

**Solution:**
1. Ensure H2 dependency is in pom.xml
2. Run `mvn clean test` to refresh test environment
3. Check test profile is set: `@ActiveProfiles("test")`

---

## Next Steps & Future Enhancements

1. **API Versioning:** Support multiple API versions (v2, v3)
2. **Pagination:** Implement page/size query parameters
3. **Caching:** Add Redis cache for frequently accessed products
4. **Search:** Implement full-text search on product names
5. **Audit Logging:** Track all modifications with user details
6. **Rate Limiting:** Implement per-user rate limits
7. **Monitoring:** Add Prometheus metrics and Grafana dashboards

---

## Support & Contact

For issues, questions, or suggestions, contact the API team:
- **Email:** api-support@ecommerce-store.com
- **Slack:** #api-support

---

*Last Updated: May 23, 2026 | Version: 1.0.0*
