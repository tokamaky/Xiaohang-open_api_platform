# Backend For API Platform

<!-- ## How to Debug APIs Online

You can access and debug APIs online by visiting [**http://localhost:7529/api/doc.html**](http://localhost:7529/api/doc.html). -->

## Overview
The backend is the core of the API open platform, responsible for managing API services, user access control, data storage, and analytics. It ensures secure and efficient communication between users and APIs through robust backend functionalities.

### Functionality
- Spring Boot 2.7.0 
- Spring MVC  
- MySQL Driver  
- MyBatis Plus  
- Spring Session Redis for distributed login  
- Spring AOP  
- Apache Commons Lang3 utility classes  
- Lombok annotations  
- Swagger + Knife4j for API documentation  
- Spring Boot debugging tools and project processors  
- Global request and response interceptors (for logging)  
- Global exception handler  
- Custom error codes  
- Encapsulated generic response class  


### Features
1. **API Management**:
   - Create, update, delete, and publish APIs.
   - Analyze API usage and statistics.
   - Enforce API access control and rate limiting.

2. **Authentication & Security**:
   - Signature-based API authentication to prevent malicious access.
   - Assign unique Access Key and Secret Key for secure API calls.

3. **Performance Optimization**:
   - High-performance communication between microservices using Dubbo.
   - Caching with Redis and search indexing with Elasticsearch for faster operations.

4. **Administrative Controls**:
   - Provide tools for debugging and testing APIs.
   - Monitor user activity and API performance in real-time.

### Technology Stack
- **Core Framework**: Spring Boot
- **Microservices**: Spring Cloud Gateway, Dubbo
- **Data Layer**: MyBatis-Plus, Redis, Elasticsearch
- **Documentation**: Swagger, Knife4j

### Modules
1. **xiaohangapi-backend**: Core API management and analytics module.
2. **xiaohangapi-gateway**: Unified entry point for API calls, handling routing, rate limiting, and traffic control.
3. **xiaohangapi-common**: Shared utilities for logging, model definitions, and validation.
