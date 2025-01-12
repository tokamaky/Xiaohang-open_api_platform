# Frontend for API Platform

## Overview
The frontend provides a user-friendly interface for developers and administrators to manage APIs, analyze usage, and interact with API documentation. It focuses on a seamless and efficient user experience.

### Features
1. **User Dashboard**:
   - Register and log in securely.
   - Enable or disable API access permissions.
   - View API call history and usage statistics.

2. **Administrator Panel**:
   - Manage APIs: Add, edit, publish, or remove APIs.
   - Monitor and debug APIs with real-time data.
   - Configure access control and rate limits.

3. **Interactive Documentation**:
   - Auto-generated API documentation with Swagger and Knife4j.
   - Real-time API testing and debugging tools.
   - Enhanced parameter editing with a JSON editor.

### Technology Stack
- **Frontend Framework**: React, Umi
- **UI Components**: Ant Design Pro
- **API Communication**: Umi Request (Axios wrapper)

### Highlights
1. **Developer-Friendly Tools**:
   - Auto-generated documentation ensures clear communication of API capabilities.
   - Integrated debugging tools to streamline API testing.

2. **Responsive UI**:
   - Built with Ant Design Pro for a modern and interactive user experience.
   - Optimized for both desktop and mobile platforms.

3. **Performance and Security**:
   - Secure API calls with JWT-based authentication.
   - Optimized API requests using a customized Axios wrapper.


## Environment Prepare

Install `node_modules`:

```bash
npm install
```


## Provided Scripts

Ant Design Pro provides some useful script to help you quick start and build with web project, code style check and test.

Scripts provided in `package.json`. It's safe to modify or add additional script:

### Start project

```bash
npm start
```

### Build project

```bash
npm run build
```
