# Fix4Home OpenAPI Documentation

## 📚 Overview

This directory contains the complete OpenAPI 3.0 specification for the Fix4Home API, along with interactive documentation tools.

## 🚀 Quick Start

### Option 1: View in Browser (Recommended)

Simply open `swagger-ui.html` in your web browser:

```bash
# Open directly in default browser
open docs/openapi/swagger-ui.html

# Or on Windows
start docs/openapi/swagger-ui.html

# Or on Linux
xdg-open docs/openapi/swagger-ui.html
```

### Option 2: Serve with Local Web Server

For the best experience, serve the files with a local web server:

#### Using Python
```bash
cd docs/openapi
python -m http.server 8000
# Then open http://localhost:8000/swagger-ui.html
```

#### Using Node.js (http-server)
```bash
cd docs/openapi
npx http-server -p 8000
# Then open http://localhost:8000/swagger-ui.html
```

#### Using PHP
```bash
cd docs/openapi
php -S localhost:8000
# Then open http://localhost:8000/swagger-ui.html
```

### Option 3: Online Swagger Editor

1. Go to [https://editor.swagger.io/](https://editor.swagger.io/)
2. File → Import file → Select `openapi.yaml`
3. Start editing and testing

### Option 4: VS Code Extension

Install the "OpenAPI (Swagger) Editor" extension and open `openapi.yaml`

## 📁 Files in this Directory

```
docs/openapi/
├── openapi.yaml           # OpenAPI 3.0 specification (YAML format)
├── openapi.json           # OpenAPI 3.0 specification (JSON format - auto-generated)
├── swagger-ui.html        # Interactive API documentation UI
├── README.md             # This file
└── postman-converter.js  # Script to convert OpenAPI to Postman
```

## 🎯 Features

### Interactive Documentation
- ✅ Try out API endpoints directly from the browser
- ✅ Automatic request/response examples
- ✅ Schema validation
- ✅ JWT authentication testing
- ✅ Real-time request/response inspection

### Complete API Coverage
- 🔐 **Authentication** - Register, login, logout
- 👥 **Customer Management** - Profiles, addresses
- 🛠️ **Technician Management** - Profiles, skills, availability
- 📋 **Service Management** - Service catalog
- 🎫 **Service Requests** - Full lifecycle management
- 📝 **Service Posts** - Customer posts and technician responses
- 💡 **Consultations** - Consultation proposals
- 💰 **Payments** - Payment processing
- ⭐ **Feedback** - Reviews and ratings
- 🔔 **Notifications** - User notifications
- 💬 **Chat** - Real-time messaging
- 🚨 **Complaints** - Complaint handling
- 👑 **Admin** - System administration
- 🧪 **Testing** - Health checks and testing

## 🔧 Using the API Documentation

### 1. Authentication Flow

Most endpoints require JWT authentication. Here's how to authenticate:

#### Step 1: Register a User
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "role": "CUSTOMER",
  "fullName": "Test User",
  "phone": "0123456789"
}
```

#### Step 2: Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

Response will include a JWT token:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": { ... }
  }
}
```

#### Step 3: Authorize in Swagger UI
1. Click the **"Authorize" 🔓** button at the top
2. Enter: `Bearer YOUR_TOKEN_HERE`
3. Click "Authorize"
4. Now you can test all authenticated endpoints!

### 2. Testing Endpoints

#### Using Swagger UI
1. Navigate to any endpoint
2. Click "Try it out"
3. Fill in the required parameters
4. Click "Execute"
5. View the response below

#### Using cURL
Copy the generated cURL command from Swagger UI or create your own:

```bash
curl -X GET "http://localhost:8100/api/v1/services" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

#### Using Postman
1. Import `openapi.yaml` or `openapi.json` into Postman
2. Or use the existing Postman collection in `docs/collections/`

## 📊 API Structure

### Base URLs
- **Development:** `http://localhost:8100/api/v1`
- **Staging:** `https://staging-api.fix4home.com/api/v1`
- **Production:** `https://api.fix4home.com/api/v1`

### Response Format
All responses follow this structure:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* response data */ },
  "timestamp": "2024-11-03T10:30:00Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-11-03T10:30:00Z"
}
```

### HTTP Status Codes
- `200 OK` - Successful operation
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

## 🔄 Converting to Other Formats

### Convert to Postman Collection

```bash
# Install converter
npm install -g openapi-to-postmanv2

# Convert
openapi2postmanv2 -s openapi.yaml -o postman-collection.json -p
```

### Convert to JSON
```bash
# Using yq
yq eval -o=json openapi.yaml > openapi.json

# Or using online tools
# https://www.convertjson.com/yaml-to-json.htm
```

### Generate Client SDKs

Using OpenAPI Generator:

```bash
# Install
npm install -g @openapitools/openapi-generator-cli

# Generate TypeScript/JavaScript client
openapi-generator-cli generate \
  -i openapi.yaml \
  -g typescript-axios \
  -o ./clients/typescript

# Generate Java client
openapi-generator-cli generate \
  -i openapi.yaml \
  -g java \
  -o ./clients/java

# Generate Python client
openapi-generator-cli generate \
  -i openapi.yaml \
  -g python \
  -o ./clients/python
```

Supported languages: TypeScript, JavaScript, Java, Python, Go, PHP, Ruby, C#, Swift, Kotlin, and 50+ more!

## 🛠️ Development

### Validating the OpenAPI Spec

```bash
# Using Swagger CLI
npm install -g @apidevtools/swagger-cli
swagger-cli validate openapi.yaml

# Using Spectral (advanced linting)
npm install -g @stoplight/spectral-cli
spectral lint openapi.yaml
```

### Editing the Spec

1. **VS Code** - Use "OpenAPI (Swagger) Editor" extension
2. **Online Editor** - [https://editor.swagger.io/](https://editor.swagger.io/)
3. **Stoplight Studio** - [https://stoplight.io/studio](https://stoplight.io/studio)

### Auto-generating from Code

If you're using Spring Boot (Java):

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

Then access auto-generated docs at:
- Swagger UI: `http://localhost:8100/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8100/v3/api-docs`
- OpenAPI YAML: `http://localhost:8100/v3/api-docs.yaml`

## 📖 Additional Resources

### Official Documentation
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
- [OpenAPI Generator](https://openapi-generator.tech/)

### Tutorials
- [OpenAPI 3.0 Tutorial](https://support.smartbear.com/swaggerhub/docs/tutorials/openapi-3-tutorial.html)
- [Writing OpenAPI Specs](https://apihandyman.io/writing-openapi-swagger-specification-tutorial-part-1-introduction/)

### Tools
- [Swagger Editor](https://editor.swagger.io/) - Online editor
- [Postman](https://www.postman.com/) - API testing
- [Insomnia](https://insomnia.rest/) - API client
- [Stoplight Studio](https://stoplight.io/studio) - API design

## 🤝 Integration with Frontend

### For React/Vue/Angular

```bash
# Generate TypeScript client
npm install --save-dev @openapitools/openapi-generator-cli

# Add to package.json scripts
{
  "scripts": {
    "generate-api": "openapi-generator-cli generate -i ../backend/docs/openapi/openapi.yaml -g typescript-axios -o ./src/api"
  }
}

# Run generation
npm run generate-api
```

Then use in your code:

```typescript
import { Configuration, AuthenticationApi, CustomerManagementApi } from './api';

const config = new Configuration({
  basePath: 'http://localhost:8100/api/v1',
  accessToken: 'your-jwt-token'
});

const authApi = new AuthenticationApi(config);
const customerApi = new CustomerManagementApi(config);

// Login
const loginResponse = await authApi.login({
  email: 'test@example.com',
  password: 'password123'
});

// Get profile
const profile = await customerApi.getCustomerProfile();
```

### For Mobile Apps (iOS/Android)

Generate Swift or Kotlin clients:

```bash
# Swift (iOS)
openapi-generator-cli generate \
  -i openapi.yaml \
  -g swift5 \
  -o ./mobile/ios/api

# Kotlin (Android)
openapi-generator-cli generate \
  -i openapi.yaml \
  -g kotlin \
  -o ./mobile/android/api
```

## 🐛 Troubleshooting

### CORS Issues
If you're testing from a different origin, make sure CORS is enabled in your backend:

```yaml
# application.yml
spring:
  web:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
```

### Authentication Errors
- Make sure to include `Bearer ` prefix before the token
- Check token expiration
- Verify the token is for the correct environment

### Swagger UI Not Loading
- Ensure you're serving the files with a web server (not file://)
- Check browser console for errors
- Verify `openapi.yaml` path in `swagger-ui.html`

## 📝 License

This API documentation is part of the Fix4Home project.

## 📞 Support

For issues or questions:
- Check the [API Guide](../api/README_API.md)
- Review [Postman Collection](../collections/)
- Contact the development team

---

**Last Updated:** November 3, 2024
**API Version:** v1.0.0
**OpenAPI Version:** 3.0.3

