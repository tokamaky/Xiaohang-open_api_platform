# XiaoHang Open API Platform

> by [程序员鱼皮知识星球](https://xiaohang.icu)

A full-stack API Marketplace Platform where developers can publish, discover, and consume APIs. Built with a microservices architecture featuring API Gateway routing, distributed rate limiting, and SDK-based client authentication.

## Architecture Overview

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   Client    │ ───► │   API Gateway    │ ───► │  Backend API    │
│   (SDK)     │      │  (Spring Cloud   │      │  (Spring Boot)  │
│             │      │   Gateway)       │      │                 │
└─────────────┘      └────────┬─────────┘      └────────┬────────┘
                              │                          │
                              │ Dubbo                    │ Dubbo
                              ▼                          ▼
                     ┌─────────────────┐        ┌─────────────────┐
                     │  Interface      │        │     MySQL       │
                     │  Service        │        │     + Redis     │
                     │  (Mock APIs)    │        │                 │
                     └─────────────────┘        └─────────────────┘
```

## Technology Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot 2.7.0, Spring MVC, Spring AOP |
| **ORM** | MyBatis-Plus 3.5.1 |
| **Gateway** | Spring Cloud Gateway 2021.0.5 |
| **RPC** | Apache Dubbo 3.3.0 (Triple Protocol) |
| **Cache** | Redis (Session + Distributed Rate Limiting) |
| **Database** | MySQL 8.0 |
| **API Docs** | Knife4j 3.0.3 |
| **Client SDK** | Java SDK with SHA256 signature |
| **Frontend** | React + Umi.js + Ant Design Pro + TypeScript |
| **Container** | Docker Compose |

## Project Modules

```
xiaohangapi-backend/
├── xiaohangapi-backend/         # Main business API service (port 7529)
├── xiaohangapi-gateway/         # API Gateway (port 8090)
├── xiaohangapi-interface/       # Mock interface provider (port 8123)
├── xiaohangapi-common/          # Shared entities, DTOs, service interfaces
├── xiaohangapi-client-sdk/      # Java SDK for API consumers
```

## Core Features

### 1. Distributed Rate Limiting (Gateway)

Three-dimensional rate limiting powered by Redis + Lua sliding window algorithm.

```
Request → Signature Verification → Rate Limit Check → Route to Interface
```

**Dimensions:**
- **Global limit** (1000 req/min) - protects the entire platform
- **Per-user limit** (100 req/min per accessKey) - prevents resource monopolization
- **Per-interface limit** (50 req/min per path) - prevents individual API overload

**Implementation:**
- Redis sorted sets (ZSET) with sliding window algorithm
- Atomic Lua script ensures accuracy under high concurrency
- Configurable limits via `application.yml`
- Returns HTTP 429 when rate limit is triggered
- Fails open (allows requests) if Redis is unavailable

```yaml
# Configuration in xiaohangapi-gateway/src/main/resources/application.yml
rate-limit:
  user:
    max-requests: 100
    window-seconds: 60
  interface:
    max-requests: 50
    window-seconds: 60
  global:
    max-requests: 1000
    window-seconds: 60
```

Key files:
- `xiaohangapi-gateway/src/main/java/com/xiaohang/xiaohangapigateway/utils/RateLimiter.java` - Sliding window algorithm
- `xiaohangapi-gateway/src/main/java/com/xiaohang/xiaohangapigateway/config/RateLimitConfig.java` - Configuration binding
- `xiaohangapi-gateway/src/main/java/com/xiaohang/xiaohangapigateway/CustomGlobalFilter.java` - Filter integration

### 2. API Gateway

- **Signature Authentication**: SHA256(body + secretKey) signature verification
- **Replay Attack Prevention**: 5-minute timestamp validation
- **Traffic Dying**: Internal header (`X-Dye-Data: nero`) marks requests for internal routing
- **Response Interception**: Captures responses and increments call counts asynchronously
- **Dynamic Routing**: Routes all requests to the interface service

### 3. Client SDK

Java SDK makes API consumption as simple as a local method call:

```java
XiaohangApiClient client = new XiaohangApiClient(accessKey, secretKey);
client.setGatewayHost("https://gateway-production-xxx.up.railway.app");

String result = client.invokeInterface(params, "/api/rand.text", "GET");
```

Features:
- Automatic header generation (accessKey, nonce, timestamp, sign)
- SHA256 signature generation
- HTTP request/response handling
- Error handling and response parsing

### 4. API Key Authentication

Every user receives a pair of credentials on registration:

- **accessKey**: Public identifier (stored in request headers)
- **secretKey**: Private signing key (never transmitted over network)

Signature flow:
1. Client: `sign = SHA256(body + "." + secretKey)`
2. Client sends: `accessKey, timestamp, nonce, sign, method`
3. Gateway verifies: re-generates signature with user's secretKey and compares

### 5. Service-to-Service Communication (Dubbo)

Internal services communicate via Apache Dubbo with the Triple protocol (gRPC-compatible):

| Caller | Callee | Purpose |
|--------|--------|---------|
| Gateway | Backend (InnerUserService) | Fetch user by accessKey |
| Gateway | Backend (InnerInterfaceInfoService) | Verify interface exists |
| Gateway | Backend (InnerUserInterfaceInfoService) | Check quota & increment calls |
| Interface | Backend (Dubbo Provider) | Invoke business logic |

### 6. Traffic Dying (Blue-Green Routing)

Gateway injects `X-Dye-Data: nero` header into forwarded requests. The Interface service uses an interceptor (`DyeDataInterceptor`) to only allow dyed traffic through, preventing direct access to internal services.

## Getting Started

### Prerequisites

- JDK 8+ (for backend)
- JDK 17+ (for gateway and interface modules)
- MySQL 8.0
- Redis
- Node.js 16+ (for frontend)
- Maven 3.6+

### Backend Setup

```bash
cd xiaohangapi-backend

# Build all modules
mvn clean install -DskipTests

# Run backend (requires MySQL and Redis)
cd xiaohangapi-backend
mvn spring-boot:run

# Run gateway (port 8090)
cd xiaohangapi-gateway
mvn spring-boot:run

# Run interface service (port 8123)
cd xiaohangapi-interface
mvn spring-boot:run
```

### Environment Variables

```bash
# Backend
MYSQLHOST=localhost
MYSQLPORT=3306
MYSQLDATABASE=xiaohangapi
MYSQLUSER=root
MYSQLPASSWORD=your_password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=
GATEWAY_HOST=https://gateway-production-xxx.up.railway.app

# Gateway
DUBBO_BACKEND_URL=dubbo://localhost:20880
GATEWAY_INTERFACE_URL=https://interface-production-xxx.up.railway.app
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

### Frontend Setup

```bash
cd xiaohangapi-frontend
npm install
npm start
```

### Docker Deployment

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop
docker-compose down
```

## API Endpoints

### User Management
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/user/register` | - | User registration |
| POST | `/api/user/login` | - | User login |
| GET | `/api/user/get/login` | User | Get current user |
| POST | `/api/user/update/my` | User | Update profile |

### Interface Management
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/interfaceInfo/add` | User | Create interface |
| POST | `/api/interfaceInfo/online` | Admin | Publish interface |
| POST | `/api/interfaceInfo/offline` | Admin | Take offline |
| POST | `/api/interfaceInfo/invoke` | User | Test invoke |

### Gateway (External)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| ANY | `/**` | Signed | Route to interface service |

## Rate Limiting Response

When rate limit is exceeded, the gateway returns:

```json
{
  "code": 42900,
  "data": null,
  "message": "Rate limit exceeded, please try again later"
}
```

HTTP Status: `429 Too Many Requests`

Headers:
- `Retry-After`: seconds until the rate limit window resets

## Database Schema

```sql
-- User with API credentials
user (id, userAccount, userPassword, userName, userAvatar, userRole,
      accessKey, secretKey, createTime, updateTime, isDelete)

-- API interface definition
interface_info (id, name, description, url, host, requestParams,
                requestHeader, responseHeader, status, method,
                userId, createTime, updateTime, isDelete)

-- User-interface subscription & call tracking
user_interface_info (id, userId, interfaceInfoId, totalNum, leftNum,
                     status, createTime, updateTime, isDelete)
```

## Template Features

Beyond the API platform, the backend template includes:

- Spring Boot 2.7.0
- Spring MVC + MyBatis-Plus
- Spring Session with Redis (distributed login)
- Spring AOP for request logging and auth checks
- Swagger + Knife4j API documentation (`/api/doc.html`)
- Global exception handler
- Custom error codes (`ErrorCode` enum)
- Standardized responses (`BaseResponse<T>`)
- Soft delete support (`@TableLogic`)
- Lombok annotations
- Alibaba EasyExcel for Excel import/export
- Aliyun OSS + Tencent COS file upload

## Interview Highlights

When presenting this project in interviews, emphasize:

1. **Microservices Architecture**: Gateway + Backend + Interface separation
2. **Distributed Rate Limiting**: Redis Lua sliding window algorithm, three dimensions
3. **Security**: accessKey/secretKey + SHA256 signature + timestamp replay prevention
4. **SDK Design**: How to design a developer-friendly client library
5. **Traffic Management**: Traffic dying, header propagation
6. **Error Handling**: Global exception handler with proper HTTP status codes
