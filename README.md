# Ecommerce REST API — Spring Boot Microservices

A fully containerized ecommerce backend built with Spring Boot microservices,
featuring JWT authentication, asynchronous event-driven communication via
RabbitMQ, and a simulated payment gateway.

---

## Architecture Overview

```
Client
  └── API Gateway (port 8761)
        ├── Auth Service        - JWT authentication
        ├── User Service        - user profiles, addresses, payment cards
        ├── Product Service     - product catalog and stock management
        ├── Cart Service        - cart management and checkout
        ├── Order Service       - order lifecycle management
        └── Payment Service     - simulated payment processing
```

All client requests enter through the API Gateway, which validates the JWT
token, extracts claims (user ID, role), and forwards them as headers to
downstream services. Microservices communicate with each other via OpenFeign
(synchronous) and RabbitMQ (asynchronous).

---

## Services

### Auth Service

Handles user registration and authentication using JWT tokens.

- Register and login with email and password
- Issues short-lived access tokens (15 minutes) and long-lived refresh tokens (7
  days)
- Refresh token rotation - each use invalidates the old token
- On registration, calls User Service via OpenFeign to create the user profile

### User Service

Manages user profile information, delivery addresses, and saved payment cards.

- CRUD operations for user profiles
- Multiple delivery addresses per user with default address support
- Payment card tokenization - full card number is never stored, only the last
  four digits and a generated token
- Exposes an internal endpoint for Payment Service to retrieve card tokens

### Product Service

Manages the product catalog and tracks stock levels.

- Product and category management with full CRUD
- Dynamic product filtering and search (by category, price range, stock status,
  name)
- Pagination and sorting enabled with Spring Data Jpa
- Stock levels updated asynchronously when orders are confirmed
- Stock status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK) derived from stock quantity

### Cart Service

Manages the user's shopping cart and initiates the checkout flow.

- Automatically creates a cart on first access
- Add, update, and remove items - adding an existing product increments quantity
- Product price and name are snapshotted at the time of adding to cart
- Checkout triggers the order creation flow via RabbitMQ

### Order Service

Creates and manages orders throughout their lifecycle.

- Orders created from checkout events consumed from RabbitMQ
- Order lifecycle: PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED
- Failed payments result in CANCELLED status
- Publishes stock update events to Product Service on successful orders

### Payment Service

Processes payments using a simulated payment gateway.

- Consumes order events from RabbitMQ to initiate payment
- Retrieves card token from User Service via OpenFeign
- Simulates payment gateway validation (expiry, card type, amount)
- Publishes success or failure events back to Order Service

### API Gateway

Single entry point for all client requests.

- JWT validation using Spring Security OAuth2 Resource Server
- Extracts JWT claims and forwards as headers (`X-User-Id`, `X-User-Role`)
- Strips `Authorization` header before forwarding to downstream services

---

## Async Event Flow

```
POST /api/v1/cart/checkout
  └── Cart Service
        ├── validates cart is not empty
        ├── marks cart as CHECKED_OUT
        └── publishes CartCheckedOutEvent

Order Service (consumes OrderCreateMessage)
  ├── fetches address from User Service (OpenFeign)
  ├── creates Order (status PROCESSING)
  └── publishes PaymentConfirmMessage

Payment Service (consumes PaymentConfirmMessage)
  ├── fetches card token from User Service (OpenFeign)
  ├── simulates payment gateway
  └── publishes OrderConfirmationMessage or OrderCancellationMessage

Order Service (consumes OrderConfirmationMessage)
  ├── updates order status to CONFIRMED
  └── publishes ProductStockUpdateMessage

Product Service (consumes ProductStockUpdateMessage)
  └── deducts stock for each ordered item

Order Service (consumes OrderCancellationMessage)
  └── updates order status to CANCELLED
```

---

## Tech Stack

| Category              | Technology                                    |
| --------------------- | --------------------------------------------- |
| Framework             | Spring Boot 3.5.11                            |
| API Gateway           | Spring Cloud Gateway                          |
| Service Communication | OpenFeign (sync), RabbitMQ (async)            |
| Authentication        | JWT (JJWT), Spring Security                   |
| Database              | MySQL 8                                       |
| Database Migrations   | Flyway                                        |
| ORM                   | Spring Data JPA / Hibernate                   |
| API Documentation     | SpringDoc OpenAPI (Swagger UI)                |
| Containerization      | Docker, Docker Compose                        |
| Testing               | JUnit 5, Testcontainers, WireMock, Awaitility |
| Build Tool            | Maven / Gradle                                |

---

## Prerequisites

- Docker and Docker Compose
- Java 17+
- Maven

---

## Getting Started

**1. Clone the repository:**

```bash
git clone https://github.com/peasfultown/spring-boot-ecommerce-rest-api
cd spring-boot-ecommerce-rest-api
```

**2. Create your `.env` file from the example:**

```bash
cp .env.example .env
```

Fill in your custom env or use the default sample:

```env
ACTIVE_PROFILE=prod
DB_USERNAME=user
DB_PASSWORD=password
DB_ROOT_PASSWORD=root

SERVICES_INTERNAL_SECRET=sosecretmuchwow
JWT_SECRET=asecretlongenoughforhmacsha256orsomethingsomething

RABBITMQ_PORT=5672
RABBITMQ_DEFAULT_EXCHANGE=main
RABBITMQ_USERNAME=user
RABBITMQ_PASSWORD=password
RABBITMQ_VHOST=/
```

**4. Build and start all services:**

```bash
docker compose up --build
```

**5. Access the API:**

- API Gateway: `http://localhost:8761`
- RabbitMQ Management UI: `http://localhost:15672`

---

## API Documentation

**IN PROGRESS**
**IN PROGRESS**
**IN PROGRESS**

Each service exposes a Swagger UI when running:

| Service         | Swagger UI                              |
| --------------- | --------------------------------------- |
| Auth Service    | `http://localhost:8080/swagger-ui.html` |
| User Service    | `http://localhost:8081/swagger-ui.html` |
| Product Service | `http://localhost:8082/swagger-ui.html` |
| Cart Service    | `http://localhost:8083/swagger-ui.html` |
| Order Service   | `http://localhost:8084/swagger-ui.html` |
| Payment Service | `http://localhost:8085/swagger-ui.html` |

All endpoints are also accessible through the API Gateway at `http://localhost:8761/swagger-ui.html`.

---

## Authentication

Register a new account:

```bash
curl -X POST http://localhost:8761/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123", "firstName":
  "First", "lastName": "Last", "phone": "12457890"}'
```

Login:

```bash
curl -X POST http://localhost:8761/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'
```

Use the returned `accessToken` as a Bearer token on all subsequent requests:

```bash
curl http://localhost:8761/api/v1/cart \
  -H "Authorization: Bearer <accessToken>"
```

---

## Test Payment Cards

The simulated payment gateway recognizes the following test card numbers:

| Card Number         | Result             |
| ------------------- | ------------------ |
| 4242 4242 4242 4242 | Payment succeeds   |
| 4000 0000 0000 0002 | Card declined      |
| 4000 0000 0000 9995 | Insufficient funds |
| 4000 0000 0000 0127 | Incorrect CVV      |

---

## Running Tests

Run tests for a specific service, remember to set environment variables:

```bash
cd auth-service
./mvnw test
```

Integration tests use Testcontainers and require Docker to be running. Tests
spin up real MySQL and RabbitMQ containers automatically.

---

## Project Structure

```
ecommerce/
├── api-gateway/
├── auth-service/
├── docker/
├── user-service/
├── product-service/
├── cart-service/
├── order-service/
├── payment-service/
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Service-to-Service Security

Internal service calls are authenticated using a shared secret header
(`X-Internal-Service-Secret`). This prevents external clients from calling
internal endpoints directly. In production, services are deployed in a private
VPC subnet so only the API Gateway is publicly accessible.

---

## Future Improvements

- AWS deployment
- Replace simulated payment gateway with Stripe integration
- Add notification service for order confirmation and payment emails
- Elasticsearch integration for advanced product search
- AWS Parameter Store for secrets management in production
