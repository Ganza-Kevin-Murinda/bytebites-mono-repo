# 🍔 ByteBites Microservices Platform – Lab 6

## ✅ Overview

This project is the completed implementation of **Lab 6: A Secure Microservices Platform from Scratch** for ByteBites — an online food delivery startup. The goal was to build a **cloud-native microservices architecture** using **Spring Boot**, **Spring Cloud**, **JWT**, **OAuth2**, and **RabbitMQ**, with a focus on **security, scalability, and resilience**.

---

## 🧱 Microservices Architecture

The platform is built using the following independent services:

```bash
bytebites-platform/
├── discovery-server # Eureka service registry
├── config-server # Centralized external config
├── api-gateway # Entry point + JWT verification
├── auth-service # Login, registration, JWT generation
├── restaurant-service # CRUD for restaurants and menus
├── order-service # Place/view orders
├── notification-service # Listens to events and simulates notifications
```

---


---

## 🔐 Security Features

| Feature                  | Description |
|--------------------------|-------------|
| **JWT-based Authentication** | Login via `/auth/login` returns signed JWT |
| **Role-Based Access Control (RBAC)** | `@PreAuthorize` + JWT roles used to restrict actions |
| **OAuth2 Support** | Google OAuth2 login with automatic role mapping |
| **API Gateway Filters** | Validates JWT and forwards user info as headers |
| **Stateless Architecture** | No sessions; services rely solely on JWT |
| **Resource Ownership Checks** | Ensures users only access their own data |

---

## 🔄 Core Features Implemented

### 🧩 Infrastructure
- ✅ **Spring Cloud Config Server** – serves config from Git
- ✅ **Eureka Discovery Server** – registers all services
- ✅ **API Gateway** – routes requests, validates JWT, and secures entry

### 👥 Auth Service
- ✅ Registration with BCrypt password hashing
- ✅ JWT issuance and role embedding
- ✅ OAuth2 login with default `ROLE_CUSTOMER`

### 🍽️ Restaurant Service
- ✅ CRUD for restaurants & menus
- ✅ Owners can only manage their own restaurants
- ✅ All users can view restaurant listings

### 🛒 Order Service
- ✅ Place and view orders
- ✅ Orders associated with authenticated customers
- ✅ Restaurant owners can view incoming orders
- ✅ Publishes `OrderPlacedEvent` to RabbitMQ

### 🔔 Notification Service
- ✅ Listens to order events from RabbitMQ
- ✅ Simulates email/push notifications

---

## 🚀 Event-Driven Architecture

### ✅ Messaging with RabbitMQ:
- `order-service` publishes `OrderPlacedEvent`
- `notification-service` and `restaurant-service` listen to these events to process actions asynchronously

---

## 🛡️ Resilience

### ✅ Circuit Breakers (Resilience4j):
- All inter-service calls use **circuit breakers**
- Configured fallback methods ensure graceful degradation
- `/actuator/circuitbreakerevents` enabled for visibility

---

## ⚙️ Configuration Strategy

- All services load config from a Git-backed **Spring Cloud Config Server**
- Each service has its own `.properties` file in [bytebites-config-repo](https://github.com/your-username/bytebites-config-repo)
- Config includes DB settings, ports, and feature toggles

---

## 📦 Technologies Used

- **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1**
- **Spring Cloud Gateway**
- **Eureka Discovery**
- **RabbitMQ**
- **JWT & OAuth2**
- **Spring Security**
- **PostgreSQL**
- **Resilience4j**
- **H2 (test environments)**

---

## 🧪 How to Test

### 🔐 Auth Flow
```bash
POST /auth/login
# → returns JWT

Use JWT in Authorization header:
Authorization: Bearer <token>
```

---

## 🔁 API Gateway Routes
| Path                  | Target Service       |
| --------------------- | -------------------- |
| `/auth/**`            | `auth-service`       |
| `/api/restaurants/**` | `restaurant-service` |
| `/api/orders/**`      | `order-service`      |


---

## 🧪 Role Access Examples

| Endpoint                | Required Role           |
| ----------------------- | ----------------------- |
| POST `/api/orders`      | `ROLE_CUSTOMER`         |
| POST `/api/restaurants` | `ROLE_RESTAURANT_OWNER` |
| GET `/admin/users`      | `ROLE_ADMIN`            |


---

## 🛠️ Setup Instructions

1. Clone the monorepo:
    ```bash
   git clone https://github.com/Ganza-Kevin-Murinda/bytebites-mono-repo.git
    ```
2. Clone the config-repo(`private`):
    ```bash
   git clone https://github.com/Ganza-Kevin-Murinda/bytebites-config-repo.git
    ```
3. Set environment variables where required
4. Start RabbitMQ (via Docker or local install)
5. Run services in the following order:
    - discovery-server
    - config-server
    - auth-service
    - restaurant-service
    - order-service
    - notification-service
    - api-gateway
   
---

## 🔍 Observability
- actuator endpoints enabled for all services
- Use:
```bash
GET /actuator/circuitbreakerevents
GET /actuator/health
GET /actuator/metrics
```
---

### 👨‍💻 Author
> Ganza Kevin Murinda