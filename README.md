# 🐼 Panda API — Open API Platform

> A production-grade API marketplace built with React + Spring Boot + Dubbo + Spring Cloud Gateway

**🌐 Live Demo:** [xiaohang-openapiplatform-production.up.railway.app](https://xiaohang-openapiplatform-production.up.railway.app)

---

## 📌 Project Overview

Panda API is a full-stack open API platform designed to provide developers with efficient, secure, and user-friendly API services. Through modular design and modern technology integration, the platform supports API management, user permission control, usage analytics, and more.

- **Admins** can publish, manage, and deprecate APIs, and visualize call statistics via interactive charts
- **Users** can browse APIs, enable access permissions, test them online, and integrate via a lightweight client SDK with a single line of code

---

## 🏗️ Architecture

```
Frontend (React + Ant Design Pro)
        │
        ▼
  Spring Cloud Gateway  ──────────────────────────────────────────┐
  (Signature Auth · Access Control · Rate Limiting · Traffic Dye) │
        │                                                          │
        ▼  Dubbo RPC                                               ▼
  Backend Service          ◄──────────────────►   Interface Service
  (User · API CRUD                               (Live API endpoints
   Permissions · Stats)                           + Online Debugger)
        │
        ├── MySQL  (Persistent data)
        ├── Redis  (Session store)
        └── Nacos  (Service registry + config center)
```

---

## ✨ Key Features

### 👤 User Features
- User registration, login, and profile management
- Enable or disable API call permissions per interface
- View personal API usage statistics

### 🛠️ Administrator Features
- Full CRUD management for API interfaces
- Publish, deprecate, or debug APIs
- Visualize top-called APIs via pie charts (ECharts)

### 🔐 API Signature Authentication
Each user receives a unique `accessKey` / `secretKey` pair. Every API call is signed using a custom HMAC algorithm — ensuring security, traceability, and preventing malicious use.

### 📦 Lightweight Client SDK
Developers can call any platform API with **a single line of code** via the published Spring Boot Starter SDK — no manual HTTP or signing logic required.

```java
// Traditional approach: ~20 lines of HTTP + signing boilerplate
// With Panda API SDK:
String result = xiaohangApiClient.getUsernameByPost(user);
```

### 🌐 API Gateway
Spring Cloud Gateway provides a unified entry point handling:
- **Signature verification** — reject unsigned or tampered requests
- **Access control** — check quota before forwarding
- **Traffic labeling** — dye headers for distributed tracing
- **Call counting** — atomically update stats via Dubbo RPC

### 📊 Analytics Dashboard
Admins can view a real-time visualization of the most-invoked APIs, powered by ECharts/AntV.

### 📖 Auto-Generated API Docs
Integrated Swagger + Knife4j automatically generates interactive API documentation. Frontend uses plugins to auto-generate request code from OpenAPI specs, reducing front-end/back-end collaboration overhead.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Ant Design Pro, UmiJS, Umi Request, TypeScript |
| Backend | Spring Boot 2.7, MyBatis Plus, Spring Session |
| Microservices | Apache Dubbo 3.3, Nacos, Spring Cloud Gateway |
| Database | MySQL 8, Redis |
| Auth | Custom API Signature (AccessKey / SecretKey + HMAC) |
| Docs | Swagger, Knife4j |
| SDK | Spring Boot Starter (Client SDK) |
| Deploy | Docker, Railway |

---

## 📁 Project Structure

```
Xiaohang-open_api_platform/
├── xiaohangapi-backend/           # Core backend (Spring Boot)
│   ├── xiaohangapi-common/        # Shared models, interfaces, utilities
│   ├── xiaohangapi-gateway/       # API Gateway (Spring Cloud Gateway + Dubbo consumer)
│   ├── xiaohangapi-interface/     # Live API implementations + online debugger
│   └── xiaohangapi-client-sdk/   # Spring Boot Starter SDK for developers
└── xiaohangapi-frontend/          # Frontend (React + Ant Design Pro)
```

### Module Responsibilities

| Module | Responsibility |
|---|---|
| `xiaohangapi-backend` | User management, API CRUD, permissions, call statistics |
| `xiaohangapi-gateway` | Routing, signature auth, access control, call counting |
| `xiaohangapi-interface` | Actual API endpoints, interactive docs |
| `xiaohangapi-client-sdk` | Standardized SDK with signing built-in |
| `xiaohangapi-common` | Shared models, constants, Dubbo service interfaces |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8
- Redis
- Nacos (standalone)

### Backend

```bash
# 1. Start Nacos
docker run -d -p 8848:8848 \
  -e MODE=standalone \
  -e NACOS_AUTH_ENABLE=true \
  -e NACOS_AUTH_TOKEN=U2VjcmV0S2V5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA== \
  nacos/nacos-server:v2.3.2

# 2. Configure src/main/resources/application.yml
#    (MySQL, Redis, Nacos addresses)

# 3. Start backend, gateway, and interface services
cd xiaohangapi-backend && mvn spring-boot:run
cd xiaohangapi-gateway && mvn spring-boot:run
cd xiaohangapi-interface && mvn spring-boot:run
```

### Frontend

```bash
cd xiaohangapi-frontend
npm install
npm start
```

---

## 🌐 Deployment (Railway)

This project is fully deployed on [Railway](https://railway.app):

| Service | Role |
|---|---|
| `backend` | Core Spring Boot application (port 7529) |
| `Gateway` | Spring Cloud Gateway (port 8090) |
| `interface` | API implementation service (port 8123) |
| `nacos` | Service registry (`nacos/nacos-server:v2.3.2`) |
| `Redis` | Session storage |
| `MySQL` | Persistent database |

---

## 🏆 Highlights

1. **Modular Design** — Clear separation of concerns across 5 submodules, ensuring maintainability and scalability
2. **Security First** — HMAC-based API signature authentication with per-user AK/SK, preventing unauthorized and malicious calls
3. **Developer-Friendly SDK** — Spring Boot Starter with zero-config signing, designed to avoid dependency conflicts
4. **High-Performance RPC** — Dubbo replaces REST for inter-service communication, reducing overhead and eliminating code duplication via shared common module
5. **Comprehensive Tooling** — Swagger + Knife4j auto-docs, ECharts analytics, online API debugger, and frontend code generation

---

## 📄 License

MIT
