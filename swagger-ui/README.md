# Fix4Home Swagger UI

This is a standalone Swagger UI for testing the Fix4Home backend API. It runs on port 3001 and connects to the backend API running on port 8080.

## Prerequisites

- Node.js (v14 or higher)
- npm (comes with Node.js)
- Fix4Home backend running on port 8080

## Setup and Running

1. Navigate to the swagger-ui directory:
   ```
   cd swagger-ui
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Start the Swagger UI server:
   ```
   npm start
   ```
   
   Alternatively, you can directly run:
   ```
   node server.js
   ```

4. Open your browser and navigate to:
   ```
   http://localhost:3001
   ```

## Features

- Interactive API documentation
- Test API endpoints directly from the UI
- Authentication support with JWT tokens
- Customizable API URL (default is http://localhost:8080/api-docs)

## Usage for Frontend Developers

1. Make sure the Fix4Home backend is running on port 8080
2. Launch the Swagger UI server using the instructions above
3. Use the UI to explore and test API endpoints
4. For authenticated endpoints:
   - First use the `/api/v1/auth/login` or `/api/v1/auth/register` endpoints to get a token
   - Click the "Authorize" button at the top right and enter your token
   - Format: `Bearer your_token_here`
   - All subsequent requests will include the authentication token

## Troubleshooting

- If you see CORS errors, make sure the backend has proper CORS configuration
- If the API docs don't load, verify the backend is running and the API URL is correct
- For authorization issues, check that your token is valid and properly formatted with "Bearer " prefix

### API Documentation Not Available

If you see the "API Documentation Not Available" warning, check:

1. The backend server is running at http://localhost:8080
2. The `OpenApiConfig.java` configuration class is properly configured:
   ```java
   @Configuration
   public class OpenApiConfig {
       @Bean
       public OpenAPI customOpenAPI() {
           // Configuration for Swagger/OpenAPI documentation
       }
   }
   ```
3. The SpringDoc properties are correctly set in `application.properties`:
   ```properties
   # API Documentation
   springdoc.api-docs.path=/api-docs
   springdoc.swagger-ui.path=/swagger-ui.html
   ```
4. Verify the SpringDoc dependency is in the project's `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.5.0</version>
   </dependency>
   ```
   
5. Restart both the backend server and the Swagger UI server to ensure changes take effect
