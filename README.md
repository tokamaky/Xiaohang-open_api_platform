# 🐼 Panda API — Open API Marketplace Platform

> **A production-grade, full-stack API marketplace built on a distributed microservices architecture.**
> Developers can publish APIs, register, retrieve credentials, and invoke any endpoint with a single line of code through a custom-built Spring Boot client SDK.

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-2.7-6DB33F?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Cloud-2021-6DB33F?logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/API_Gateway-WebFlux-6DB33F?logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/RPC-Service_Mesh-1E8CBE" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/Deploy-Railway-0B0D0E?logo=railway&logoColor=white" />
</p>

---

## 🌐 Live Demo

👉 **[https://xiaohang-openapiplatform-production.up.railway.app](https://xiaohang-openapiplatform-production.up.railway.app)**

---

## 📌 Why this project?

Most "API platform" demo projects stop at a single monolithic Spring Boot app. **Panda API** is different — it's a **true distributed system** that solves three real engineering problems:

1. **How do you authenticate thousands of untrusted API callers without exposing credentials?**
   → Custom **AK/SK + HMAC signature** scheme enforced at the API gateway, with replay-attack protection (nonce + 5-minute timestamp window). Same pattern used by AWS SigV4, Stripe, and Shopify.

2. **How do you give developers a frictionless integration experience?**
   → A published **Spring Boot Starter SDK** — users go from 20+ lines of manual HTTP + signing boilerplate to **a single method call**.

3. **How do you keep the codebase maintainable across 5 services?**
   → Decoupled microservices with a **service registry** for discovery, a **shared contracts module** for type-safe inter-service communication, and clear bounded contexts per submodule.

---

## 🛠️ Tech Stack

A deliberate, opinionated stack — every choice solves a specific problem.

### 🖥️ Frontend
| Tech | Version | Why it was chosen |
|---|---|---|
| **React** | 18 | Industry-standard component model, concurrent rendering, mature ecosystem |
| **TypeScript** | 5.x | End-to-end type safety, especially valuable when paired with auto-generated API clients |
| **Ant Design Pro** | 6.x | Production-grade enterprise dashboard layouts (sidebar, breadcrumbs, RBAC routes) |
| **UmiJS Max** | 4.x | React framework with built-in routing, OpenAPI codegen, mock server, and state management |
| **ECharts + AntV** | 5.x | Rich, customizable charts for the analytics dashboard |

### ⚙️ Backend Core
| Tech | Version | Why it was chosen |
|---|---|---|
| **Java** | 17 (LTS) | Modern language features (records, sealed classes, pattern matching), long-term support |
| **Spring Boot** | 2.7 | Battle-tested foundation with massive ecosystem support |
| **Spring Cloud Gateway** | 2021.x | Reactive, non-blocking API gateway built on WebFlux — handles high-throughput auth filtering with backpressure |
| **MyBatis Plus** | 3.5 | SQL-first ORM with powerful query builders — more flexible than JPA for analytics-heavy workloads |
| **Spring Session** | 2.7 | Externalizes HTTP sessions to Redis — prerequisite for horizontal scaling |
| **Spring AOP** | 2.7 | Powers the `@AuthCheck` annotation for declarative role-based access control |

### 🌐 Distributed Systems
| Concern | Solution | Why it was chosen |
|---|---|---|
| **API Gateway** | Spring Cloud Gateway | Reactive single entry point — handles auth, routing, rate limiting in one place (think AWS API Gateway / Kong, but self-hosted) |
| **Service Registry** | Centralized registry server | Enables dynamic service discovery and client-side load balancing (analogous to Consul, Eureka, or etcd) |
| **Inter-service RPC** | Apache Dubbo over the registry | Strongly-typed RPC with built-in load balancing, retries, and timeouts (analogous to gRPC). Significantly lower overhead than REST for internal calls |
| **Config Management** | Centralized config server | Hot-reload of `application.yml` properties without restarts (analogous to Spring Cloud Config or HashiCorp Consul KV) |
| **Service Contracts** | Shared `common` Maven module | Type-safe DTOs and service interfaces — eliminates schema drift between services |

> *Implementation note: the registry and config server are powered by Nacos, and inter-service RPC by Apache Dubbo — both are standard, mature open-source projects that fill the same niche as their North American counterparts (Consul/Eureka, gRPC).*

### 💾 Data Layer
| Tech | Version | Why it was chosen |
|---|---|---|
| **MySQL** | 8.0 | Reliable RDBMS with strong consistency, JSON column support, and atomic counter updates |
| **Redis** | 7.x | Distributed session store, with headroom for caching and rate limiting |
| **S3-compatible Object Storage** | — | Externalized storage for user avatars and API icons; CDN-backed delivery |

### 🔐 Security
| Tech | Why it was chosen |
|---|---|
| **HMAC-SHA256 signing** | Industry-standard request signing pattern (used by AWS SigV4, Stripe, Shopify webhooks) |
| **Nonce + Timestamp window** | Prevents replay attacks even if a request is intercepted in transit |
| **Spring AOP `@AuthCheck`** | Declarative RBAC — no scattered `if (user.role != ADMIN)` checks polluting business logic |
| **Spring Session + Redis** | Stateless services, session state externalized — survives pod restarts |

### 📖 Developer Experience
| Tech | Why it was chosen |
|---|---|
| **OpenAPI 3 (Swagger)** | Auto-generated API documentation — feeds the frontend's auto-codegen pipeline |
| **Knife4j** | Enhanced Swagger UI with request/response examples and offline export |
| **Spring Boot Starter pattern** | Auto-configuration for the SDK — consumers only need to set credentials in `application.yml` |
| **Lombok** | Eliminates getter/setter/builder boilerplate |
| **OpenAPI → TypeScript codegen** | Frontend API client is auto-generated from the backend spec — eliminates contract drift |

### 🐳 DevOps & Deployment
| Tech | Why it was chosen |
|---|---|
| **Maven (multi-module)** | Native support for the 5-module structure with shared dependency management |
| **Docker** | Per-service `Dockerfile` for reproducible builds; foundation for any container orchestrator |
| **Docker Compose** | One-command local dev stack |
| **Railway** | Cloud deployment with private networking between linked services and zero-config TLS |

### 🧪 Testing & Tooling
| Tech | Why it was chosen |
|---|---|
| **JUnit 4 + Spring Boot Test** | Standard test stack with `@SpringBootTest` integration testing |
| **EasyExcel** | Streaming Excel export for admin reports — handles 100k+ rows without OOM |

---

## 🏆 Engineering Highlights

| # | Highlight | Why it matters |
|---|---|---|
| 1 | **Distributed microservices from day one** — 5 Maven modules with clear bounded contexts | Demonstrates the ability to design non-trivial distributed systems, not just CRUD monoliths |
| 2 | **Custom HMAC signature scheme at the API gateway** | Shows depth in API security — nonce, timestamp window, replay protection. Same pattern as AWS SigV4 |
| 3 | **Published Spring Boot Starter SDK** | Proves understanding of library packaging, auto-configuration, and developer experience design |
| 4 | **Reactive (non-blocking) API gateway** built on Spring WebFlux | Backpressure-aware filter chain, response body decoration, async post-processing |
| 5 | **Shared contracts module** for inter-service communication | Type-safe RPC interfaces and DTOs — catches breaking changes at compile time |
| 6 | **Declarative authorization** via Spring AOP + custom annotations (`@AuthCheck`) | Clean, cross-cutting RBAC — no `if/else` role checks scattered across controllers |
| 7 | **End-to-end type safety** — OpenAPI spec → auto-generated TypeScript client | Eliminates manual API client drift between frontend and backend |
| 8 | **Production deployment on Railway** with multiple linked services | Real DevOps pipeline — not just `localhost` demos |

---

## 🏗️ System Architecture

```
                   ┌────────────────────────────────────────┐
                   │   React + TypeScript (Frontend)        │
                   │   • User dashboard                     │
                   │   • Admin console                      │
                   │   • Real-time analytics                │
                   └──────────────────┬─────────────────────┘
                                      │ HTTPS
                                      ▼
                   ┌────────────────────────────────────────┐
                   │   API Gateway (Reactive WebFlux)       │
                   │   ──────────────────────────────────   │
                   │   ✓ HMAC signature verification        │
                   │   ✓ IP allow / deny lists              │
                   │   ✓ Replay protection (nonce + ts)     │
                   │   ✓ Quota enforcement                  │
                   │   ✓ Traffic shadowing (X-Dye-Data)     │
                   │   ✓ Atomic call-count metrics          │
                   └────┬────────────────────┬──────────────┘
                        │                    │
                Internal RPC          Reactive route
                        │                    │
                        ▼                    ▼
        ┌─────────────────────────┐   ┌─────────────────────────┐
        │  Backend Service        │   │  Interface Service      │
        │  ─────────────────────  │   │  ─────────────────────  │
        │  • User mgmt / auth     │   │  • Live API endpoints   │
        │  • API CRUD / publish   │   │  • Online debugger      │
        │  • Quota & metrics      │   │  • OpenAPI docs         │
        │  • File uploads         │   │                         │
        └──────────┬──────────────┘   └─────────────────────────┘
                   │
       ┌───────────┼───────────────┬──────────────────┐
       ▼           ▼               ▼                  ▼
   ┌───────┐  ┌─────────┐   ┌──────────────────┐  ┌────────────┐
   │ MySQL │  │  Redis  │   │ Service Registry │  │   Object   │
   │  8.0  │  │ Session │   │  + Config Server │  │  Storage   │
   │       │  │  store  │   │                  │  │   (CDN)    │
   └───────┘  └─────────┘   └──────────────────┘  └────────────┘
```

---

## ✨ Features at a Glance

### 👤 For Users
- Register / login with Redis-backed sessions
- Browse the API catalog, enable access per interface
- View personal call statistics and remaining quota
- **One-line SDK integration** — no HTTP boilerplate
- Self-service `accessKey` / `secretKey` retrieval from the dashboard

### 🛠️ For Administrators
- Full CRUD on API interfaces (publish / deprecate / edit)
- **Online API debugger** — send test requests directly from the UI
- **Real-time analytics dashboard** — top-invoked APIs, call trends, user distribution
- Role-based access control via a custom `@AuthCheck` annotation + Spring AOP interceptor

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
| `timestamp` | Unix seconds — must be within a 5-minute window |
| `sign` | SHA-256 digest |
| `body` | URL-encoded request body |

### 📦 Zero-Config Client SDK

```java
// Traditional approach — 20+ lines of HTTP client + signing boilerplate 😩
// With the Panda API SDK — one line 😎

@Resource
private XiaohangApiClient xiaohangApiClient;

String result = xiaohangApiClient.getUsernameByPost(user);
```

The SDK is published as a **Spring Boot Starter** — drop it into your `pom.xml`, set your AK/SK in `application.yml`, and you're done. Signing, headers, and error handling are all handled transparently via auto-configuration.

### 📊 Analytics Dashboard
Real-time pie / bar charts (ECharts + AntV) show the top-invoked APIs across the platform. Data flows: gateway → metrics RPC call → atomic SQL update (`UPDATE ... SET totalNum = totalNum + 1`) — race-condition free.

### 📖 Auto-generated API Docs
OpenAPI 3 + **Knife4j** produce interactive docs at `/doc.html`. The frontend uses the UmiJS OpenAPI plugin to **auto-generate TypeScript request functions** from the spec — zero manual API client code.

---

## 📁 Project Structure

```
panda-api-platform/
│
├── xiaohangapi-backend/              # ⚙️  Core backend service
│   ├── src/main/java/
│   │   ├── controller/               # REST endpoints (User, Interface, Analysis, File)
│   │   ├── service/ + mapper/        # Business logic + persistence
│   │   ├── aop/                      # AuthInterceptor, LogInterceptor
│   │   ├── annotation/               # @AuthCheck (RBAC)
│   │   ├── exception/                # Global exception handler
│   │   ├── provider/                 # Internal RPC service implementations
│   │   └── config/                   # OpenAPI, CORS, persistence, storage
│   ├── sql/                          # DDL scripts
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── xiaohangapi-common/               # 📦 Shared contracts module
│   ├── model/entity/                 # User, InterfaceInfo, UserInterfaceInfo
│   └── service/                      # Internal RPC interfaces (service contracts)
│
├── xiaohangapi-gateway/              # 🛡️  Reactive API Gateway
│   └── CustomGlobalFilter.java       # ⭐ Signature auth + rate limit + metrics filter
│
├── xiaohangapi-interface/            # 🎯 Live API implementations service
│
├── xiaohangapi-client-sdk/           # 🎁 Spring Boot Starter (published SDK)
│
└── xiaohangapi-frontend/             # 🖥️  React + TypeScript frontend
    ├── src/pages/                    # Login, InterfaceInfo, Admin, Analysis
    ├── src/components/               # Reusable UI components
    └── config/                       # Build + proxy config
```

### Module responsibilities

| Module | Role | Talks to |
|---|---|---|
| `xiaohangapi-backend` | User mgmt, API CRUD, quota, statistics | MySQL, Redis, registry |
| `xiaohangapi-gateway` | Auth filter, routing, rate limiting | Internal services via RPC |
| `xiaohangapi-interface` | Live API endpoints, online debugger | (stateless) |
| `xiaohangapi-client-sdk` | Request signing + HTTP client (published) | Gateway |
| `xiaohangapi-common` | Shared DTOs + RPC service contracts | — |

---

## 🚀 Quick Start (Local)

### Prerequisites
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8 / Redis 7
- Docker (for the registry + config server)

### 1. Spin up infrastructure

```bash
# Service registry + config server
docker run -d --name registry -p 8848:8848 \
  -e MODE=standalone \
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

Update MySQL, Redis, and registry addresses in:
- `xiaohangapi-backend/src/main/resources/application.yml`
- `xiaohangapi-gateway/src/main/resources/application.yml`
- `xiaohangapi-interface/src/main/resources/application.yml`

### 4. Start services (in 3 terminals)

```bash
# Terminal 1 — backend service
cd xiaohangapi-backend && mvn spring-boot:run        # :7529

# Terminal 2 — API gateway
cd xiaohangapi-gateway && mvn spring-boot:run        # :8090

# Terminal 3 — interface service
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

Panda API is deployed on **Railway** as a multi-service project:

| Railway Service | Role | Port |
|---|---|---|
| `backend` | Core Spring Boot service | 7529 |
| `gateway` | Reactive API gateway | 8090 |
| `interface` | API implementation service | 8123 |
| `registry` | Service registry + config server | 8848 |
| `redis` | Distributed session store | 6379 |
| `mysql` | Persistent database | 3306 |
| `frontend` | Static build served via Nginx | 80 |

Each service is containerized with its own `Dockerfile` and linked through Railway's private network.

---

## 📄 License

MIT — free to use, fork, and learn from.

---

<p align="center">
  Built with ❤️ by Xiaohang Ji · <a href="https://xiaohang-ji-profile.netlify.app/">Contact</a>
</p>
