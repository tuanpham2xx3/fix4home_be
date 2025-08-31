#!/bin/bash
# Fix4Home API Testing with cURL

echo "=== Fix4Home API Testing with cURL ==="

BASE_URL="http://localhost:8080"

echo -e "\n1. Health Check"
curl -X GET "$BASE_URL/api/v1/test/health" \
  -H "Accept: application/json" | jq '.'

echo -e "\n2. Database Check"
curl -X GET "$BASE_URL/api/v1/test/database" \
  -H "Accept: application/json" | jq '.'

echo -e "\n3. Customer Registration"
curl -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "curl_customer",
    "password": "password123",
    "email": "curl.customer@example.com",
    "phoneNumber": "0999888777",
    "role": "CUSTOMER",
    "fullName": "Curl Test Customer"
  }' | jq '.'

echo -e "\n4. Customer Login"
CUSTOMER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "curl_customer",
    "password": "password123"
  }')

echo "$CUSTOMER_RESPONSE" | jq '.'

# Extract token
CUSTOMER_TOKEN=$(echo "$CUSTOMER_RESPONSE" | jq -r '.data.accessToken')

echo -e "\n5. Protected Customer Endpoint"
curl -X GET "$BASE_URL/api/v1/test/customer" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Accept: application/json" | jq '.'

echo -e "\n6. Cross-Role Access Test (should fail)"
curl -X GET "$BASE_URL/api/v1/test/technician" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Accept: application/json" || echo "✅ Access denied as expected"

echo -e "\n7. Technician Registration"
curl -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "curl_technician",
    "password": "password123",
    "email": "curl.technician@example.com",
    "phoneNumber": "0888777666",
    "role": "TECHNICIAN",
    "fullName": "Curl Test Technician",
    "skills": "Plumbing, Electrical",
    "experience": "3 years experience"
  }' | jq '.'

echo -e "\n8. Technician Login"
TECH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "curl_technician",
    "password": "password123"
  }')

echo "$TECH_RESPONSE" | jq '.'

# Extract technician token
TECH_TOKEN=$(echo "$TECH_RESPONSE" | jq -r '.data.accessToken')

echo -e "\n9. Protected Technician Endpoint"
curl -X GET "$BASE_URL/api/v1/test/technician" \
  -H "Authorization: Bearer $TECH_TOKEN" \
  -H "Accept: application/json" | jq '.'

echo -e "\n10. Error Testing - Invalid Login"
curl -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "nonexistent",
    "password": "wrongpassword"
  }' || echo "✅ Invalid login rejected as expected"

echo -e "\n11. Error Testing - Unauthorized Access"
curl -X GET "$BASE_URL/api/v1/test/customer" \
  -H "Accept: application/json" || echo "✅ Unauthorized access blocked as expected"

echo -e "\n=== cURL Testing Complete ===" 