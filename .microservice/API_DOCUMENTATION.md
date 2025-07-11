# MRS SendEmail Backend API Documentation

## Tổng quan

**MRS SendEmail Backend** là một hệ thống API RESTful được thiết kế để gửi mã xác thực email với khả năng quản lý rate limiting, bảo mật API key và lưu trữ tạm thời. Hệ thống được xây dựng bằng Go với Gin framework, Redis cho caching và SMTP cho gửi email.

### Thông tin cơ bản
- **Base URL**: `http://localhost:8200` (hoặc domain của bạn)
- **Protocol**: HTTP/HTTPS
- **Data Format**: JSON
- **Authentication**: API Key via Header

## Cấu hình hệ thống

### Cài đặt và chạy

1. **Clone repository và cài đặt dependencies**:
```bash
git clone <repository-url>
cd MRS_SENDEMAIL_BE
go mod download
```

2. **Cấu hình environment variables**:
Sao chép file `config.example` thành `.env` và điều chỉnh các giá trị:

```env
# Server Configuration
SERVER_PORT=8200
SERVER_HOST=0.0.0.0

# Redis Configuration  
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=mypassword
REDIS_DB=0

# Gmail SMTP Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_FROM_NAME=Fix4Home System

# API Security - QUAN TRỌNG!
API_KEYS=your-secret-key-1,your-secret-key-2,your-secret-key-3

# Rate Limiting Configuration
RATE_LIMIT_EMAIL_PER_HOUR=5
RATE_LIMIT_IP_PER_HOUR=30

# Verification Code Configuration
CODE_EXPIRE_MINUTES=30
CODE_LENGTH=6

# Default System Name
DEFAULT_SYSTEM_NAME=Fix4Home
```

3. **Chạy hệ thống**:
```bash
# Development
go run cmd/server/main.go

# Production với Docker
docker-compose up -d
```

## Authentication - API Key

### Yêu cầu bảo mật

Tất cả các endpoint (trừ `/health`) đều yêu cầu API key hợp lệ thông qua header:

```http
x-api-key: your-secret-api-key
```

### Cấu hình API Keys

API keys được cấu hình trong environment variable `API_KEYS` (phân cách bằng dấu phẩy):


API_KEYS=fix4home_prod_123abc456def789

⚠️ **Bảo mật quan trọng**:
- Sử dụng API keys mạnh, không dễ đoán
- Mỗi hệ thống/client nên có API key riêng
- Thường xuyên rotate API keys
- Không commit API keys vào source code

## API Endpoints

### 1. Health Check

Kiểm tra trạng thái hoạt động của hệ thống và các dependencies.

**Endpoint**: `GET /health`  
**Authentication**: Không yêu cầu  
**Rate Limit**: Không giới hạn

#### Request:
```http
GET /health HTTP/1.1
Host: localhost:8200
```

#### Response Success:
```json
{
  "status": "healthy",
  "checks": {
    "redis": "healthy",
    "smtp": "healthy"
  }
}
```

#### Response Error:
```json
{
  "status": "unhealthy", 
  "checks": {
    "redis": "unhealthy: connection refused",
    "smtp": "healthy"
  }
}
```

### 2. Generate Verification Code

Tạo và gửi mã xác thực đến email người dùng.

**Endpoint**: `POST /generate`  
**Authentication**: API Key required  
**Rate Limit**: 5 email/hour per email, 30 requests/hour per IP

#### Request:
```http
POST /generate HTTP/1.1
Host: localhost:8200
Content-Type: application/json
x-api-key: your-secret-api-key

{
  "email": "user@example.com",
  "system": "MyApp",
  "customData": {
    "user_id": "12345",
    "action": "registration"
  }
}
```

#### Request Parameters:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | string | ✅ | Email địa chỉ để gửi mã xác thực |
| `system` | string | ❌ | Tên hệ thống (mặc định: Fix4Home) |
| `customData` | object | ❌ | Dữ liệu tùy chỉnh cho email template |

#### Response Success:
```json
{
  "success": true,
  "message": "Verification code sent successfully"
}
```

#### Response Errors:

**400 Bad Request - Invalid email:**
```json
{
  "error": "Bad Request",
  "message": "Key: 'GenerateRequest.Email' Error:Field validation for 'Email' failed on the 'email' tag"
}
```

**401 Unauthorized - Missing/Invalid API key:**
```json
{
  "error": "Unauthorized",
  "message": "API key is required"
}
```

**429 Too Many Requests - Rate limit exceeded:**
```json
{
  "error": "Rate Limit Exceeded",
  "message": "Too many requests"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal Server Error", 
  "message": "Failed to send verification email"
}
```

### 3. Verify Code

Xác thực mã xác thực đã gửi.

**Endpoint**: `POST /verify`  
**Authentication**: API Key required  
**Rate Limit**: 30 requests/hour per IP

#### Request:
```http
POST /verify HTTP/1.1
Host: localhost:8200
Content-Type: application/json
x-api-key: your-secret-api-key

{
  "email": "user@example.com",
  "code": "123456"
}
```

#### Request Parameters:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | string | ✅ | Email địa chỉ đã nhận mã xác thực |
| `code` | string | ✅ | Mã xác thực 6 chữ số |

#### Response Success:
```json
{
  "success": true,
  "message": "Verification successful"
}
```

#### Response Errors:

**400 Bad Request - Invalid/Expired code:**
```json
{
  "error": "Invalid or Expired Code",
  "message": "Verification code not found or has expired"
}
```

**400 Bad Request - Wrong code:**
```json
{
  "error": "Invalid Code", 
  "message": "The verification code provided is incorrect"
}
```

## Rate Limiting

Hệ thống áp dụng rate limiting để ngăn chặn spam và abuse:

### Giới hạn theo Email
- **5 email/hour** mỗi địa chỉ email
- Reset mỗi giờ
- Áp dụng cho endpoint `/generate`

### Giới hạn theo IP
- **30 requests/hour** mỗi IP address  
- Reset mỗi giờ
- Áp dụng cho tất cả endpoints có authentication

### Cấu hình Rate Limiting
```env
RATE_LIMIT_EMAIL_PER_HOUR=5
RATE_LIMIT_IP_PER_HOUR=30
```

## Code Examples

### cURL Examples

#### Health Check:
```bash
curl -X GET http://localhost:8200/health
```

#### Generate Code:
```bash
curl -X POST http://localhost:8200/generate \
  -H "Content-Type: application/json" \
  -H "x-api-key: your-secret-api-key" \
  -d '{
    "email": "user@example.com",
    "system": "MyWebApp",
    "customData": {
      "user_id": "12345"
    }
  }'
```

#### Verify Code:
```bash
curl -X POST http://localhost:8200/verify \
  -H "Content-Type: application/json" \
  -H "x-api-key: your-secret-api-key" \
  -d '{
    "email": "user@example.com", 
    "code": "123456"
  }'
```

### JavaScript/Node.js Example

```javascript
const axios = require('axios');

const API_BASE_URL = 'http://localhost:8200';
const API_KEY = 'your-secret-api-key';

class EmailVerificationClient {
  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': API_KEY
      }
    });
  }

  async healthCheck() {
    try {
      const response = await axios.get(`${API_BASE_URL}/health`);
      return response.data;
    } catch (error) {
      throw new Error(`Health check failed: ${error.response?.data?.message || error.message}`);
    }
  }

  async generateCode(email, system = 'MyApp', customData = {}) {
    try {
      const response = await this.client.post('/generate', {
        email,
        system,
        customData
      });
      return response.data;
    } catch (error) {
      throw new Error(`Generate code failed: ${error.response?.data?.message || error.message}`);
    }
  }

  async verifyCode(email, code) {
    try {
      const response = await this.client.post('/verify', {
        email,
        code
      });
      return response.data;
    } catch (error) {
      throw new Error(`Verify code failed: ${error.response?.data?.message || error.message}`);
    }
  }
}

// Usage example
const emailClient = new EmailVerificationClient();

async function example() {
  try {
    // Check health
    const health = await emailClient.healthCheck();
    console.log('Health:', health);

    // Generate verification code
    const result = await emailClient.generateCode(
      'user@example.com',
      'MyWebApp',
      { user_id: '12345', action: 'registration' }
    );
    console.log('Code sent:', result);

    // Verify code (user inputs the code they received)
    const verification = await emailClient.verifyCode('user@example.com', '123456');
    console.log('Verification:', verification);
    
  } catch (error) {
    console.error('Error:', error.message);
  }
}
```

### Python Example

```python
import requests
import json

class EmailVerificationClient:
    def __init__(self, base_url='http://localhost:8200', api_key='your-secret-api-key'):
        self.base_url = base_url
        self.headers = {
            'Content-Type': 'application/json',
            'x-api-key': api_key
        }
    
    def health_check(self):
        """Check system health"""
        response = requests.get(f'{self.base_url}/health')
        return response.json()
    
    def generate_code(self, email, system='MyApp', custom_data=None):
        """Generate and send verification code"""
        payload = {
            'email': email,
            'system': system
        }
        if custom_data:
            payload['customData'] = custom_data
            
        response = requests.post(
            f'{self.base_url}/generate',
            headers=self.headers,
            json=payload
        )
        
        if response.status_code != 200:
            raise Exception(f"Generate failed: {response.json().get('message', 'Unknown error')}")
            
        return response.json()
    
    def verify_code(self, email, code):
        """Verify the code"""
        payload = {
            'email': email,
            'code': code
        }
        
        response = requests.post(
            f'{self.base_url}/verify',
            headers=self.headers,
            json=payload
        )
        
        if response.status_code != 200:
            raise Exception(f"Verify failed: {response.json().get('message', 'Unknown error')}")
            
        return response.json()

# Usage example
if __name__ == '__main__':
    client = EmailVerificationClient()
    
    try:
        # Check health
        health = client.health_check()
        print('Health:', health)
        
        # Generate code
        result = client.generate_code(
            'user@example.com',
            'MyPythonApp',
            {'user_id': '12345', 'action': 'login'}
        )
        print('Code sent:', result)
        
        # Verify code (user inputs the received code)
        verification = client.verify_code('user@example.com', '123456')
        print('Verification:', verification)
        
    except Exception as e:
        print(f'Error: {e}')
```

### PHP Example

```php
<?php

class EmailVerificationClient {
    private $baseUrl;
    private $apiKey;
    
    public function __construct($baseUrl = 'http://localhost:8200', $apiKey = 'your-secret-api-key') {
        $this->baseUrl = rtrim($baseUrl, '/');
        $this->apiKey = $apiKey;
    }
    
    private function makeRequest($method, $endpoint, $data = null) {
        $url = $this->baseUrl . $endpoint;
        $headers = [
            'Content-Type: application/json',
            'x-api-key: ' . $this->apiKey
        ];
        
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        
        if ($method === 'POST' && $data) {
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        }
        
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);
        
        $decoded = json_decode($response, true);
        
        if ($httpCode !== 200) {
            throw new Exception("Request failed: " . ($decoded['message'] ?? 'Unknown error'));
        }
        
        return $decoded;
    }
    
    public function healthCheck() {
        return $this->makeRequest('GET', '/health');
    }
    
    public function generateCode($email, $system = 'MyApp', $customData = []) {
        $data = [
            'email' => $email,
            'system' => $system
        ];
        
        if (!empty($customData)) {
            $data['customData'] = $customData;
        }
        
        return $this->makeRequest('POST', '/generate', $data);
    }
    
    public function verifyCode($email, $code) {
        $data = [
            'email' => $email,
            'code' => $code
        ];
        
        return $this->makeRequest('POST', '/verify', $data);
    }
}

// Usage example
try {
    $client = new EmailVerificationClient();
    
    // Check health
    $health = $client->healthCheck();
    echo "Health: " . json_encode($health) . "\n";
    
    // Generate code
    $result = $client->generateCode(
        'user@example.com',
        'MyPHPApp',
        ['user_id' => '12345', 'action' => 'password_reset']
    );
    echo "Code sent: " . json_encode($result) . "\n";
    
    // Verify code
    $verification = $client->verifyCode('user@example.com', '123456');
    echo "Verification: " . json_encode($verification) . "\n";
    
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
?>
```

## Error Handling

Hệ thống sử dụng HTTP status codes chuẩn:

| Status Code | Description |
|-------------|-------------|
| 200 | OK - Request thành công |
| 400 | Bad Request - Dữ liệu đầu vào không hợp lệ |
| 401 | Unauthorized - API key thiếu hoặc không hợp lệ |
| 429 | Too Many Requests - Vượt quá rate limit |
| 500 | Internal Server Error - Lỗi hệ thống |
| 503 | Service Unavailable - Dịch vụ không khả dụng |

### Error Response Format

Tất cả lỗi đều trả về format JSON chuẩn:

```json
{
  "error": "Error Type",
  "message": "Detailed error description"
}
```

## Best Practices

### 1. Security
- ✅ Luôn sử dụng HTTPS trong production
- ✅ Bảo mật API keys, không hardcode trong client-side code
- ✅ Implement proper API key rotation
- ✅ Monitor và log các hoạt động bất thường

### 2. Rate Limiting
- ✅ Implement exponential backoff khi gặp 429 errors
- ✅ Cache verification codes ở client nếu cần thiết
- ✅ Thông báo rõ ràng với user về giới hạn

### 3. Error Handling
- ✅ Luôn kiểm tra HTTP status codes
- ✅ Implement retry logic cho network errors
- ✅ Provide meaningful error messages cho users
- ✅ Log errors cho debugging

### 4. Performance
- ✅ Sử dụng connection pooling
- ✅ Implement timeout cho HTTP requests
- ✅ Monitor response times và availability

## Troubleshooting

### Common Issues

**1. 401 Unauthorized**
- Kiểm tra API key có được set đúng header `x-api-key`
- Xác nhận API key có trong list `API_KEYS`

**2. 429 Rate Limit Exceeded**
- Kiểm tra số lượng requests đã gửi trong 1 giờ
- Implement retry với exponential backoff

**3. 500 Internal Server Error**
- Kiểm tra logs của server
- Verify Redis và SMTP connections
- Check `/health` endpoint

**4. Email không được gửi**
- Verify SMTP configuration
- Check spam/junk folders
- Confirm email address format

### Health Check Monitoring

Sử dụng endpoint `/health` để monitor:
```bash
# Simple health check
curl -f http://localhost:8200/health

# Advanced monitoring with jq
curl -s http://localhost:8200/health | jq '.status'
```

## Contact & Support

- **Repository**: [Link to your repository]
- **Issues**: [Link to issues page]  
- **Documentation**: Tài liệu này
- **Email**: [your-support-email]

---

**Version**: 1.0  
**Last Updated**: $(date)  
**Maintained by**: Fix4Home Development Team 