# Panda API — Open API Marketplace

> A full-stack, distributed API marketplace where developers can publish APIs, manage credentials, and consume any endpoint with a single line of Java via a published Spring Boot Starter SDK.

[![Java](https://img.shields.io/badge/Java-17_(LTS)-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud_Gateway-2021-6DB33F?logo=spring)](https://spring.io/projects/spring-cloud-gateway)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Deployed on Railway](https://img.shields.io/badge/Deployed-Railway-0B0D0E?logo=railway)](https://railway.app)

**🌐 [Live Demo](https://xiaohang-openapiplatform-production.up.railway.app)** &nbsp;·&nbsp; **📖 [API Docs](https://xiaohang-openapiplatform-production.up.railway.app/doc.html)**

---

## TL;DR

A microservices-based API marketplace deployed across **7 linked services on Railway**, demonstrating production patterns I'd previously only read about: a reactive API gateway, HMAC request signing, RPC-based service-to-service communication, and a published client SDK that reduces integration code from ~30 lines to 1.

**Built to learn:** distributed-system design, API security at scale, library packaging, and end-to-end DevOps.

---

## Table of Contents

- [What it does](#what-it-does)
- [Architecture](#architecture)
- [Key engineering decisions](#key-engineering-decisions)
- [Security: HMAC request signing](#security-hmac-request-signing)
- [The SDK: from 30 lines to 1](#the-sdk-from-30-lines-to-1)
- [Tech stack](#tech-stack)
- [Tradeoffs and what I'd change](#tradeoffs-and-what-id-change)
- [What I learned](#what-i-learned)
- [Local development](#local-development)
- [Deployment](#deployment)

---

## What it does

Two audiences, one platform:

**API consumers** sign up, generate an `accessKey` / `secretKey` pair, browse the public API catalog, and integrate any endpoint into their own Spring Boot apps with a single annotated method call.

**API providers** (admins) publish new endpoints through a web console with an interactive request debugger, set per-user quotas, monitor invocation analytics, and deprecate old versions.

Every call flows through a central gateway that authenticates the signature, enforces quotas, and records metrics — none of this lives in the business services themselves.

---

## Architecture

```
                  ┌────────────────────────────────────────┐
                  │   React 18 + Ant Design Pro            │
                  │   User dashboard · Admin console       │
                  └──────────────────┬─────────────────────┘
                                     │ HTTPS
                                     ▼
                  ┌────────────────────────────────────────┐
                  │   Spring Cloud Gateway (reactive)      │
                  │   • HMAC signature verification        │
                  │   • Replay protection (nonce + window) │
                  │   • Quota enforcement (RPC call)       │
                  │   • Atomic invocation counter          │
                  │   • Request/response logging           │
                  └────┬────────────────────┬──────────────┘
                       │ RPC                │ Reactive routing
                       ▼                    ▼
       ┌─────────────────────────┐   ┌─────────────────────────┐
       │  Backend Service        │   │  Interface Service      │
       │  • User & API CRUD      │   │  • Live API endpoints   │
       │  • Quota management     │   │  • Online debugger      │
       │  • Statistics           │   │  • Swagger docs         │
       └──────────┬──────────────┘   └─────────────────────────┘
                  │
       ┌──────────┼─────────────┬──────────────┐
       ▼          ▼             ▼              ▼
   ┌───────┐ ┌─────────┐  ┌────────────┐  ┌────────────┐
   │ MySQL │ │  Redis  │  │  Service   │  │   Object   │
   │  8.0  │ │ Session │  │  Registry  │  │  Storage   │
   └───────┘ └─────────┘  │  + Config  │  └────────────┘
                          └────────────┘
```

**Why a gateway?** Every authentication, rate-limit, and metrics decision lives in one place. Business services stay focused on business logic and never see an unauthenticated request.

**Why RPC for internal calls?** The gateway needs to fetch the user's `secretKey` and check their quota *before* the request reaches the target service. REST round-trips would add latency to every external call. RPC over a service registry gives sub-millisecond internal lookups.

---

## Key engineering decisions

### 1. Reactive gateway, blocking services

The gateway is built on Spring Cloud Gateway (WebFlux-based, non-blocking). The backend services are traditional Spring MVC. **This is intentional**: the gateway is I/O-bound (lots of small auth + quota checks across many concurrent connections), while the backend is logic-heavy and benefits from the simpler programming model of MVC.

### 2. Shared `common` module for service contracts

DTOs and RPC interfaces live in a separate Maven module that all services depend on. This means:
- **Compile-time safety** across service boundaries — rename a field in the User DTO and every service that reads it fails to build
- **Single source of truth** for entity definitions — no drifting copies of `UserVO` across 5 modules

### 3. Declarative authorization via Spring AOP

Instead of scattering `if (currentUser.getRole() != ADMIN)` checks across controllers, I built a custom `@AuthCheck(mustRole = "admin")` annotation backed by an AOP aspect:

```java
@AuthCheck(mustRole = "admin")
@PostMapping("/interface/publish")
public BaseResponse<Boolean> publishInterface(@RequestBody InterfacePublishRequest req) {
    return ResultUtils.success(interfaceService.publish(req));
}
```

The aspect resolves the user from the session, checks the role, and either proceeds or throws a `BusinessException(40100, "Forbidden")`. Authorization logic exists in **exactly one file**.

### 4. Auto-generated TypeScript clients from OpenAPI

The frontend doesn't hand-write API call functions. A UmiJS plugin reads the backend's Swagger 3 spec at build time and generates typed `request*` functions. Add a new endpoint on the backend → run `npm run openapi` → use it on the frontend with full TypeScript autocomplete. **Zero manual API client maintenance.**

---

## Security: HMAC request signing

Every API call from the SDK to the gateway carries five headers:

| Header | Purpose |
|---|---|
| `accessKey` | Identifies the caller (public, like a username) |
| `nonce` | Random value — prevents replay of intercepted requests |
| `timestamp` | Unix seconds — request rejected if outside a 5-minute window |
| `body` | URL-encoded request payload (included in the signed string) |
| `sign` | `SHA256(accessKey + nonce + timestamp + body + secretKey)` |

The gateway:
1. Looks up the user's `secretKey` from the backend service via RPC
2. Recomputes the signature with the same algorithm
3. Compares against the `sign` header — mismatch → 401
4. Checks `nonce` hasn't been seen recently and `timestamp` is within window → otherwise 401
5. Calls the backend via RPC to check the user's remaining quota → 403 if exhausted
6. Routes the request, then atomically increments the user's invocation count on success

This is the same pattern used by AWS, Aliyun, and Tencent Cloud. The `secretKey` never travels over the wire — only its derived signature does.

---

## The SDK: from 30 lines to 1

**Before** (what consumers would have to write without the SDK):

```java
// Build headers, hash with secretKey, encode body, set timeout, parse response...
HttpHeaders headers = new HttpHeaders();
String nonce = String.valueOf(new Random().nextInt());
String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
String body = URLEncoder.encode(JSON.toJSONString(payload), StandardCharsets.UTF_8);
String sign = DigestUtils.sha256Hex(accessKey + nonce + timestamp + body + secretKey);
headers.set("accessKey", accessKey);
headers.set("nonce", nonce);
headers.set("timestamp", timestamp);
headers.set("body", body);
headers.set("sign", sign);
// ... and then the actual HTTP call, error handling, response parsing
```

**After** (with the SDK):

```java
@Resource
private XiaohangApiClient client;

String result = client.getUsernameByPost(new User("world"));
```

The SDK is published as a **Spring Boot Starter** with auto-configuration:

```yaml
# application.yml
xiaohang:
  client:
    access-key: ${YOUR_AK}
    secret-key: ${YOUR_SK}
```

Spring Boot's auto-config picks up the properties, instantiates the `XiaohangApiClient` bean, wires in the signing interceptor, and exposes it for `@Resource` injection. Consumers never see HTTP, signing, or header construction.

This was the most rewarding piece of the project to build — it forced me to understand `META-INF/spring.factories`, the `@ConditionalOnProperty` family, and how starter packaging actually works under the hood.

---

## Tech stack

### Backend
- **Java 17** (LTS) — records, pattern matching, sealed classes
- **Spring Boot 2.7** — REST APIs, dependency injection, validation
- **Spring Cloud Gateway 2021.x** — reactive WebFlux gateway
- **Spring AOP** — `@AuthCheck` declarative authorization
- **MyBatis Plus 3.5** — SQL-first ORM with code generation
- **Spring Session** — Redis-backed HTTP sessions for horizontal scaling

### Microservices infrastructure
- **Apache Dubbo 3.3** — high-performance RPC (analogous to gRPC; chosen here because of its tight integration with the Spring ecosystem)
- **Nacos 2.3** — service registry + dynamic configuration center (analogous to Consul + Spring Cloud Config combined)

> If you're more familiar with the Netflix/HashiCorp stack: Dubbo ≈ gRPC, Nacos ≈ Consul + Vault for config. The patterns are identical; the implementations differ.

### Data
- **MySQL 8.0** — relational store with strong consistency
- **Redis 7** — session store, replay-protection nonce cache
- **Object Storage (S3-compatible)** — user avatars, API icons

### Frontend
- **React 18** + **TypeScript 5**
- **Ant Design Pro 6** — enterprise dashboard layout, RBAC routes, breadcrumbs
- **UmiJS Max 4** — routing, build, OpenAPI codegen
- **ECharts** — analytics dashboard

### DevOps
- **Maven multi-module** — 5-module project structure
- **Docker** (multi-stage builds) — one Dockerfile per service
- **Railway** — multi-service deployment with private internal networking
- **Knife4j + Swagger 3** — interactive API documentation

---

## Tradeoffs and what I'd change

I want to be honest about the limits of the current implementation:

**1. Single-tenant SDK signing.** The signing logic assumes one `accessKey` per app instance. For a real SaaS product, the SDK would need to support multi-tenant key rotation and per-request key selection.

**2. Nonce store is in-memory on the gateway.** This means nonce uniqueness only holds within a single gateway instance. A horizontally scaled gateway would need Redis-backed nonce storage to prevent cross-instance replay.

**3. No circuit breakers.** If the backend RPC service goes down, the gateway will keep trying every request until timeout, cascading the failure. Adding Resilience4j (or Sentinel) would be the next step.

**4. Quota check is eventually consistent.** Because the invocation counter is incremented after the request succeeds, a burst of concurrent requests can briefly exceed the configured quota. For a paid API, this would need to be replaced with a token-bucket pre-check.

**5. Dubbo + Nacos is uncommon outside the Chinese tech ecosystem.** If I rebuilt this for a North American team, I'd use **gRPC + Consul** (or just plain Spring Cloud OpenFeign + Eureka) for better team familiarity, even though the architectural patterns are identical.

---

## What I learned

This was my first project where I had to think about **where state lives**. In a monolith, "the database" is the answer to every state question. In a distributed system, you have to ask: does this user's quota live in the gateway's memory, in Redis, or in MySQL? What's the latency cost of each choice? What happens when the cache and the database disagree?

Specific concrete things I now understand that I didn't before:

- **Why reactive matters at the edge.** A blocking gateway thread per connection caps you at a few thousand concurrent requests. WebFlux on the same hardware handles tens of thousands.
- **Why service contracts matter.** When the User DTO changed from `Long id` to `String id`, the shared `common` module made every consumer fail to compile in CI. Without it, I would have shipped broken code to production.
- **Why auto-configuration is non-trivial.** My first SDK draft required users to manually `@Import` a config class. Reading how `spring-boot-starter-data-jpa` actually works was a small revelation — Spring's "magic" is just `META-INF/spring.factories` plus `@ConditionalOnXxx`.
- **Why `docker-compose.yml` saves your sanity.** The first week I was running Nacos, MySQL, Redis, and three Spring Boot apps by hand in 6 terminals. Docker Compose was a 30-line investment that paid back daily.

---

## Local development

### Prerequisites
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8 / Redis 7
- Nacos 2.3 (or use Docker Compose below)

### Quick start with Docker Compose

```bash
docker compose -f xiaohangapi-backend/docker-compose.yml up -d
```

This brings up Nacos, MySQL, and Redis in one command.

### Initialize the database

```bash
mysql -uroot -p < xiaohangapi-backend/sql/db.sql
```

### Run services

```bash
# Terminal 1 — Backend (port 7529)
cd xiaohangapi-backend && mvn spring-boot:run

# Terminal 2 — Gateway (port 8090)
cd xiaohangapi-gateway && mvn spring-boot:run

# Terminal 3 — Interface service (port 8123)
cd xiaohangapi-interface && mvn spring-boot:run

# Terminal 4 — Frontend (port 8000)
cd xiaohangapi-frontend && npm install && npm run start
```

Open `http://localhost:8000`.

### Use the SDK in your own project

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

## Deployment

Deployed on Railway as 7 linked services with private internal networking:

| Service | Role |
|---|---|
| `frontend` | Static React build served via Nginx |
| `gateway` | Spring Cloud Gateway (entry point) |
| `backend` | Core service: users, APIs, quotas |
| `interface` | Public API implementations |
| `nacos` | Service registry + config center |
| `mysql` | Persistent data |
| `redis` | Session store |

Each service has its own `Dockerfile` and is built via Railway's Nixpacks. Inter-service calls use Railway's private networking (`*.railway.internal`); only the gateway and frontend are exposed publicly.

---

## Project structure

```
panda-api/
├── xiaohangapi-common/         Shared DTOs + RPC service interfaces
├── xiaohangapi-backend/        Core service (users, APIs, quotas)
├── xiaohangapi-gateway/        Spring Cloud Gateway (auth, routing, metrics)
├── xiaohangapi-interface/      Public API implementations
├── xiaohangapi-client-sdk/     Published Spring Boot Starter
└── xiaohangapi-frontend/       React + Ant Design Pro
```

---

## License

MIT — free to use, fork, and learn from.

---

<sub>Built by Xiaohang Ji · [GitHub](https://github.com/tokamaky) · [LinkedIn](https://www.linkedin.com/in/xiaohang-ji)</sub>
