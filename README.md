# 🐼 Panda API — Open API Marketplace Platform

> **A production-grade, full-stack API marketplace built with a microservices architecture.**
> Developers can publish APIs, sign up, obtain keys, and invoke any interface with a single line of code via the built-in Spring Boot Starter SDK.

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-2.7-6DB33F?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Apache_Dubbo-3.3-1E8CBE?logo=apache&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Cloud_Gateway-2021-6DB33F?logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/Ant_Design_Pro-5.x-0170FE?logo=antdesign&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/Nacos-2.3-1E8CBE" />
  <img src="https://img.shields.io/badge/Deploy-Railway-0B0D0E?logo=railway&logoColor=white" />
</p>

---

## 🌐 Live Demo

👉 **[https://xiaohang-openapiplatform-production.up.railway.app](https://xiaohang-openapiplatform-production.up.railway.app)**


---

## 📌 Why this project?

Most "API platform" demo projects stop at a single monolithic Spring Boot app. **Panda API** is different — it's a **true microservices system** that solves three real engineering problems:

1. **How do you authenticate thousands of untrusted API callers without exposing credentials?**
   → Custom **AK/SK + HMAC signature** scheme enforced at the gateway layer, with replay-attack protection (nonce + 5-minute timestamp window).

2. **How do you give developers a frictionless integration experience?**
   → A published **Spring Boot Starter SDK** — users go from 20+ lines of manual HTTP + signing boilerplate to **a single method call**.

3. **How do you keep the codebase maintainable across 5 modules?**
   → **Dubbo RPC** over Nacos registry for inter-service calls, a shared `common` module for DTOs and service interfaces, and clear bounded contexts per submodule.

---

## 🛠️ Tech Stack

A deliberate, opinionated stack — every choice solves a specific problem.

### 🖥️ Frontend
| Tech | Version | Why it was chosen |
|---|---|---|
| **React** | 18 | Industry-standard component model, concurrent rendering, mature ecosystem |
| **Ant Design Pro** | 6.x | Out-of-the-box enterprise-grade dashboard layouts (sidebar, breadcrumbs, RBAC routes) |
| **UmiJS Max** | 4.x | Built-in routing, OpenAPI codegen, mock server, and dva state management |
| **TypeScript** | 5.x | End-to-end type safety, especially valuable when paired with auto-generated API clients |
| **ECharts + AntV** | 5.x | Rich, customizable charts for the analytics dashboard |
| **Umi Request** | — | Promise-based HTTP client with built-in interceptors for auth |

### ⚙️ Backend Core
| Tech | Version | Why it was chosen |
|---|---|---|
| **Java** | 17 (LTS) | Modern language features (records, pattern matching), long-term support |
| **Spring Boot** | 2.7 | Battle-tested foundation, massive ecosystem, ideal for microservices |
| **Spring Cloud Gateway** | 2021.x | Reactive, non-blocking gateway built on WebFlux — perfect for high-throughput auth filtering |
| **MyBatis Plus** | 3.5 | SQL-first ORM with code generation; far more flexible than JPA for complex queries |
| **Spring Session** | 2.7 | Externalize HTTP sessions to Redis — required for horizontal scaling |
| **Spring AOP** | 2.7 | Powers the `@AuthCheck` annotation for declarative RBAC |

### 🌐 Microservices Infrastructure
| Tech | Version | Why it was chosen |
|---|---|---|
| **Apache Dubbo** | 3.3 | High-performance RPC — significantly faster than REST for inter-service calls; native service discovery |
| **Nacos** | 2.3 | Single tool for both **service registry** and **dynamic config center** — replaces Eureka + Spring Cloud Config |
| **Spring Cloud Gateway** | 2021.x | Single entry point for signature auth, quota enforcement, and traffic routing |

### 💾 Data Layer
| Tech | Version | Why it was chosen |
|---|---|---|
| **MySQL** | 8.0 | Reliable RDBMS with strong consistency, JSON column support, and atomic counter updates |
| **Redis** | 7.x | Session store + future-proof for caching and rate limiting |
| **Aliyun OSS** | — | Object storage for user avatars and API icons; CDN-backed delivery |

### 🔐 Security
| Tech | Why it was chosen |
|---|---|
| **Custom HMAC (SHA-256)** | Industry-standard pattern for API authentication (used by AWS, Aliyun, Tencent Cloud) |
| **Nonce + Timestamp window** | Prevents replay attacks even if a request is intercepted |
| **Spring AOP `@AuthCheck`** | Declarative RBAC — no scattered `if (user.role != ADMIN)` checks |

### 📖 Developer Experience
| Tech | Why it was chosen |
|---|---|
| **Knife4j** | Enhanced Swagger UI with Chinese support, request/response examples, and offline export |
| **Swagger 3** | OpenAPI 3.0 spec generation — feeds the frontend's auto-codegen pipeline |
| **Spring Boot Starter pattern** | Auto-configuration for the SDK — users only need to set AK/SK in `application.yml` |
| **Lombok** | Eliminates getter/setter/builder boilerplate |
| **Hutool** | Pragmatic Java utility library — URL encoding, crypto, string manipulation |
| **Gson** | JSON serialization with better support for generic types than Jackson in some edge cases |

### 🐳 DevOps & Deployment
| Tech | Why it was chosen |
|---|---|
| **Maven (multi-module)** | Native support for the 5-module structure with shared dependency management |
| **Docker** | Per-service `Dockerfile` for reproducible builds; foundation for any orchestrator |
| **Docker Compose** | One-command local dev stack (Nacos + backend + gateway) |
| **Railway** | Zero-config cloud deployment with private networking between 7 linked services |

### 📊 Observability & Tooling
| Tech | Why it was chosen |
|---|---|
| **EasyExcel** | Streaming Excel export for admin reports — handles 100k+ rows without OOM |
| **JUnit 4 + Spring Boot Test** | Standard test stack with `@SpringBootTest` integration testing |

---

## 🏆 Engineering Highlights

| # | Highlight | Why it matters |
|---|---|---|
| 1 | **Microservices from day one** — 5 Maven modules, Dubbo RPC over Nacos | Demonstrates ability to design non-trivial distributed systems, not just a CRUD monolith |
| 2 | **Custom HMAC signature at the gateway** | Shows depth in API security — nonce, timestamp window, replay protection |
| 3 | **Published Spring Boot Starter SDK** | Proves understanding of library packaging, auto-configuration, and developer experience |
| 4 | **Gateway as a reactive WebFlux filter chain** | Non-blocking I/O, response body decoration, atomic Dubbo calls on post-processing |
| 5 | **Shared `common` module** for Dubbo interfaces + DTOs | Avoids code duplication across services — the textbook Dubbo pattern |
| 6 | **Auth via Spring AOP + custom annotations** (`@AuthCheck`) | Clean, declarative RBAC instead of scattered `if` statements |
| 7 | **Automated OpenAPI → TypeScript** code generation on the frontend | Removes manual API client drift between front and back end |
| 8 | **Deployed to Railway with 7 linked services** | End-to-end DevOps — not just local demos |

---


## 🏗️ System Architecture

```
                   ┌────────────────────────────────────────┐
                   │   React + Ant Design Pro (Frontend)    │
                   │   • User dashboard                     │
                   │   • Admin console                      │
                   │   • ECharts analytics                  │
                   └──────────────────┬─────────────────────┘
                                      │ HTTPS
                                      ▼
                   ┌────────────────────────────────────────┐
                   │   Spring Cloud Gateway (Port 8090)     │
                   │   ──────────────────────────────────   │
                   │   ✓ Signature verification (HMAC)      │
                   │   ✓ IP black/white list                │
                   │   ✓ Timestamp & nonce replay guard     │
                   │   ✓ Quota check (via Dubbo)            │
                   │   ✓ Traffic dyeing (X-Dye-Data)        │
                   │   ✓ Atomic call-count increment        │
                   └────┬────────────────────┬──────────────┘
                        │                    │
                 Dubbo RPC            Reactive route
                        │                    │
                        ▼                    ▼
        ┌─────────────────────────┐   ┌─────────────────────────┐
        │  Backend Service        │   │  Interface Service      │
        │  (Port 7529)            │   │  (Port 8123)            │
        │  ─────────────────────  │   │  ─────────────────────  │
        │  • User CRUD / auth     │   │  • Live API endpoints   │
        │  • API CRUD / publish   │   │  • Online debugger      │
        │  • Quota & stats        │   │  • Swagger docs         │
        │  • OSS file upload      │   │                         │
        └──────────┬──────────────┘   └─────────────────────────┘
                   │
       ┌───────────┼────────────┬──────────────┐
       ▼           ▼            ▼              ▼
   ┌───────┐  ┌───────┐   ┌───────────┐  ┌───────────┐
   │ MySQL │  │ Redis │   │   Nacos   │  │ Aliyun OSS│
   │  8.0  │  │Session│   │ Registry  │  │ (images)  │
   └───────┘  └───────┘   │ + Config  │  └───────────┘
                          └───────────┘
```

---

## ✨ Features at a Glance

### 👤 For Users
- Register / login with Spring Session (Redis-backed)
- Browse the API catalog, enable access per interface
- View personal call statistics and remaining quota
- **One-line SDK integration** — no HTTP boilerplate
- Download `accessKey` / `secretKey` pair from the dashboard

### 🛠️ For Administrators
- Full CRUD on API interfaces (publish / deprecate / edit)
- **Online API debugger** — send test requests directly from the UI
- **ECharts analytics dashboard** — top-invoked APIs, call trends, user distribution
- Role-Based Access Control via a custom `@AuthCheck` annotation + Spring AOP interceptor

### 🔐 HMAC-based Signature Authentication
Every API call is signed client-side and verified at the gateway. Tampering with **any byte** of the request invalidates the signature.

**Signature algorithm:**
```
signature = SHA256(body + secretKey)
```

Headers enforced by the gateway:
| Header | Purpose |
|---|---|
| `accessKey` | Identifies the caller |
| `nonce` | Random int — replay prevention |
| `timestamp` | Unix seconds — must be within 5 minutes |
| `sign` | SHA256 digest |
| `body` | URL-encoded request body |

### 📦 Zero-Config Client SDK

```java
// Traditional approach — 20+ lines of HTTP client + signing boilerplate 😩
// With the Panda API SDK — one line 😎

@Resource
private XiaohangApiClient xiaohangApiClient;

String result = xiaohangApiClient.getUsernameByPost(user);
```

The SDK is published as a **Spring Boot Starter** — drop it into your `pom.xml`, set your AK/SK in `application.yml`, and you're done. Signing, headers, and error handling are all handled transparently.

### 📊 Analytics Dashboard
Real-time pie / bar charts (ECharts + AntV) show the top-invoked APIs across the platform. Data flows through the gateway → Dubbo → `UserInterfaceInfoMapper` (atomic `UPDATE ... SET totalNum = totalNum + 1`).

### 📖 Auto-generated API Docs
Swagger + **Knife4j** produce interactive docs at `/doc.html`. The frontend uses the UmiJS OpenAPI plugin to **auto-generate TypeScript request functions** from the Swagger spec — zero manual API client code.

---

## 📁 Project Structure

```
Xiaohang-open_api_platform/
│
├── xiaohangapi-backend/              # ⚙️  Core backend (Spring Boot monolith)
│   ├── src/main/java/
│   │   ├── controller/               # REST endpoints (User, Interface, Analysis, File)
│   │   ├── service/ + mapper/        # Business logic + MyBatis Plus mappers
│   │   ├── aop/                      # AuthInterceptor, LogInterceptor
│   │   ├── annotation/               # @AuthCheck (RBAC)
│   │   ├── exception/                # Global exception handler + BusinessException
│   │   ├── provider/                 # Dubbo service implementations
│   │   └── config/                   # Knife4j, CORS, MyBatis, Aliyun OSS
│   ├── sql/                          # DDL scripts
│   ├── Dockerfile
│   └── docker-compose.yml            # Nacos + backend + gateway (local dev)
│
├── xiaohangapi-common/               # 📦 Shared DTOs + Dubbo service interfaces
│   ├── model/entity/                 # User, InterfaceInfo, UserInterfaceInfo
│   └── service/                      # Inner*Service (Dubbo contracts)
│
├── xiaohangapi-gateway/              # 🛡️  API Gateway (Spring Cloud Gateway)
│   └── CustomGlobalFilter.java       # ⭐ Signature auth + rate + stats filter
│
├── xiaohangapi-interface/            # 🎯 Live API implementations
│
├── xiaohangapi-client-sdk/           # 🎁 Spring Boot Starter (published SDK)
│
└── xiaohangapi-frontend/             # 🖥️  Ant Design Pro frontend
    ├── src/pages/                    # Login, InterfaceInfo, Admin, Analysis
    ├── src/components/               # Reusable UI components
    └── config/                       # UmiJS + proxy config
```

### Module responsibilities

| Module | Role | Talks to |
|---|---|---|
| `xiaohangapi-backend` | User mgmt, API CRUD, quota, statistics | MySQL, Redis, Nacos |
| `xiaohangapi-gateway` | Auth filter, routing, rate limiting | Dubbo (all services), Nacos |
| `xiaohangapi-interface` | Real API endpoints, online debugger | (stateless) |
| `xiaohangapi-client-sdk` | Signing + HTTP client, published as starter | Gateway |
| `xiaohangapi-common` | Shared DTOs + Dubbo service interfaces | — |

---

## 🚀 Quick Start (Local)

### Prerequisites
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8 / Redis 7
- Nacos 2.3 (standalone)

### 1. Spin up infrastructure

```bash
# Nacos
docker run -d --name nacos -p 8848:8848 \
  -e MODE=standalone \
  -e NACOS_AUTH_ENABLE=true \
  -e NACOS_AUTH_TOKEN=U2VjcmV0S2V5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA== \
  nacos/nacos-server:v2.3.2

# MySQL + Redis (or use your own)
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8
docker run -d --name redis -p 6379:6379 redis:7
```

### 2. Initialize the database

```bash
mysql -uroot -p < xiaohangapi-backend/sql/db.sql
```

### 3. Configure `application.yml`

Update MySQL, Redis, and Nacos addresses in:
- `xiaohangapi-backend/src/main/resources/application.yml`
- `xiaohangapi-gateway/src/main/resources/application.yml`
- `xiaohangapi-interface/src/main/resources/application.yml`

### 4. Start services (in 3 terminals)

```bash
# Terminal 1
cd xiaohangapi-backend && mvn spring-boot:run        # :7529

# Terminal 2
cd xiaohangapi-gateway && mvn spring-boot:run        # :8090

# Terminal 3
cd xiaohangapi-interface && mvn spring-boot:run      # :8123
```

### 5. Start the frontend

```bash
cd xiaohangapi-frontend
npm install
npm run start          # http://localhost:8000
```

### 6. Use the SDK in your own app

```xml
<dependency>
    <groupId>com.xiaohang</groupId>
    <artifactId>xiaohangapi-client-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

```yaml
xiaohang:
  client:
    access-key: ${YOUR_AK}
    secret-key: ${YOUR_SK}
```

```java
@Resource
private XiaohangApiClient client;

String result = client.getUsernameByPost(new User("world"));
```

---

## 🌐 Deployment — Railway

Panda API is fully deployed on **Railway** as a multi-service project:

| Railway Service | Role | Port |
|---|---|---|
| `backend` | Spring Boot core service | 7529 |
| `gateway` | Spring Cloud Gateway | 8090 |
| `interface` | API implementation service | 8123 |
| `nacos` | `nacos/nacos-server:v2.3.2` | 8848 |
| `redis` | Session store | 6379 |
| `mysql` | Persistent database | 3306 |
| `frontend` | Static build served via Nginx | 80 |

Each service is containerized via its own `Dockerfile` and linked through Railway's private network.

---


## 📄 License

MIT — free to use, fork, and learn from.

---

<p align="center">
  Built with ❤️ by Xiaohang Liu · <a href="mailto:your-email@example.com">Contact</a>
</p>
