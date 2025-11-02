package com.fix4home.fix4home.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller that provides endpoints for Swagger UI and OpenAPI documentation
 * without requiring authentication
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SwaggerController {

    private final ResourceLoader resourceLoader;
    
    /**
     * Note: The /swagger endpoint has been removed since we are using Swagger UI on port 3001 instead
     */
    /**
     * Creates a manual endpoint for OpenAPI JSON that doesn't require authentication.
     * This is a fallback in case the standard endpoint doesn't work.
     */
    @GetMapping(value = "/api-docs-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> getApiDocs() {
        return ResponseEntity.ok("{\n" +
                "  \"openapi\": \"3.0.1\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"Fix4Home API Documentation\",\n" +
                "    \"description\": \"RESTful API documentation for Fix4Home home services platform\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  },\n" +
                "  \"servers\": [\n" +
                "    {\n" +
                "      \"url\": \"http://localhost:8080\",\n" +
                "      \"description\": \"Local development server\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"paths\": {\n" +
                "    \"/api/v1/auth/login\": {\n" +
                "      \"post\": {\n" +
                "        \"tags\": [\"Authentication\"],\n" +
                "        \"summary\": \"Login user\",\n" +
                "        \"requestBody\": {\n" +
                "          \"content\": {\n" +
                "            \"application/json\": {\n" +
                "              \"schema\": {\n" +
                "                \"type\": \"object\",\n" +
                "                \"properties\": {\n" +
                "                  \"usernameOrEmail\": {\"type\": \"string\"},\n" +
                "                  \"password\": {\"type\": \"string\"}\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"Successful login\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"type\": \"object\",\n" +
                "                  \"properties\": {\n" +
                "                    \"success\": {\"type\": \"boolean\"},\n" +
                "                    \"message\": {\"type\": \"string\"},\n" +
                "                    \"data\": {\n" +
                "                      \"type\": \"object\",\n" +
                "                      \"properties\": {\n" +
                "                        \"accessToken\": {\"type\": \"string\"},\n" +
                "                        \"tokenType\": {\"type\": \"string\"}\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"/api/v1/auth/register\": {\n" +
                "      \"post\": {\n" +
                "        \"tags\": [\"Authentication\"],\n" +
                "        \"summary\": \"Register new user\",\n" +
                "        \"requestBody\": {\n" +
                "          \"content\": {\n" +
                "            \"application/json\": {\n" +
                "              \"schema\": {\n" +
                "                \"type\": \"object\",\n" +
                "                \"properties\": {\n" +
                "                  \"username\": {\"type\": \"string\"},\n" +
                "                  \"email\": {\"type\": \"string\"},\n" +
                "                  \"password\": {\"type\": \"string\"},\n" +
                "                  \"phoneNumber\": {\"type\": \"string\"},\n" +
                "                  \"role\": {\"type\": \"string\", \"enum\": [\"CUSTOMER\", \"TECHNICIAN\", \"ADMIN\"]},\n" +
                "                  \"fullName\": {\"type\": \"string\"}\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"User registered successfully\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"/api/v1/test/health\": {\n" +
                "      \"get\": {\n" +
                "        \"tags\": [\"Test\"],\n" +
                "        \"summary\": \"Health check endpoint\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"Service is up and running\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"/api/v1/test/customer\": {\n" +
                "      \"get\": {\n" +
                "        \"tags\": [\"Test\"],\n" +
                "        \"summary\": \"Protected customer endpoint\",\n" +
                "        \"security\": [{\"bearerAuth\": []}],\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"Accessible with CUSTOMER role\"\n" +
                "          },\n" +
                "          \"401\": {\n" +
                "            \"description\": \"Unauthorized\"\n" +
                "          },\n" +
                "          \"403\": {\n" +
                "            \"description\": \"Forbidden\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"/api/v1/test/technician\": {\n" +
                "      \"get\": {\n" +
                "        \"tags\": [\"Test\"],\n" +
                "        \"summary\": \"Protected technician endpoint\",\n" +
                "        \"security\": [{\"bearerAuth\": []}],\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"Accessible with TECHNICIAN role\"\n" +
                "          },\n" +
                "          \"401\": {\n" +
                "            \"description\": \"Unauthorized\"\n" +
                "          },\n" +
                "          \"403\": {\n" +
                "            \"description\": \"Forbidden\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"components\": {\n" +
                "    \"securitySchemes\": {\n" +
                "      \"bearerAuth\": {\n" +
                "        \"type\": \"http\",\n" +
                "        \"scheme\": \"bearer\",\n" +
                "        \"bearerFormat\": \"JWT\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n");
    }
}
