# 🐳 Fix4Home Docker Setup

## 🚀 Quick Start

### 1. Setup Environment

#### Root .env (for Docker Compose)
```bash
# Copy template to .env
cp environment.template .env

# Edit .env - mainly for database and application configs
# Note: SMTP credentials are configured in microservice .env (see below)
```

#### Email Service .env (for microservice)
```bash
# Email service uses its own .env file
# Make sure .microservice/MRS_SENDEMAIL_BE/.env exists and has:
# - SMTP_USERNAME and SMTP_PASSWORD (Gmail credentials)
# - API_KEYS (must include the key from root .env EMAIL_VERIFICATION_API_KEY)

# The .env file is automatically mounted into the container
```

**Important:** 
- Root `.env` contains configs for MySQL, Redis, and Spring Boot app
- Microservice `.env` contains SMTP credentials and API keys
- `EMAIL_VERIFICATION_API_KEY` in root `.env` must match one of the API keys in microservice `.env`

### 2. Start All Services
```bash
docker-compose up -d
```

### 3. Access Services
| Service | URL | Description |
|---------|-----|-------------|
| **Main API** | http://localhost:8100 | Spring Boot backend |
| **Email Service** | http://localhost:8200 | Go microservice |
| **Documentation** | http://localhost:8000 | OpenAPI docs |
| **MySQL** | localhost:3307 | Database |
| **Redis** | localhost:6379 | Cache |

### 4. Documentation Views
| Viewer | URL |
|--------|-----|
| **Main Hub** | http://localhost:8000/index.html |
| **Swagger UI** | http://localhost:8000/swagger-ui.html |
| **ReDoc** | http://localhost:8000/redoc.html |
| **RapiDoc** | http://localhost:8000/rapidoc.html |

## 🔧 Management Commands

```bash
# View all services status
docker-compose ps

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f email-service
docker-compose logs -f docs

# Restart services
docker-compose restart

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose build --no-cache
docker-compose up -d
```

## 🩺 Health Checks

```bash
# API Health
curl http://localhost:8100/actuator/health

# Email Service Health
curl http://localhost:8200/health

# Documentation (should return HTML)
curl http://localhost:8000/index.html
```

## ⚙️ Gmail Setup

1. Go to https://myaccount.google.com/
2. Security > 2-Step Verification > App passwords
3. Create password for Fix4Home
4. Update `.microservice/MRS_SENDEMAIL_BE/.env` file:
   ```env
   SMTP_USERNAME=your-email@gmail.com
   SMTP_PASSWORD=your-app-password
   ```
   
   **Note:** Gmail credentials are configured in microservice `.env` file, not root `.env`

## 🐛 Troubleshooting

### Port conflicts
```bash
# Check what's using ports
netstat -tulpn | grep :8100
netstat -tulpn | grep :8200
netstat -tulpn | grep :8000

# Kill processes if needed
sudo kill -9 <PID>
```

### Service not starting
```bash
# Check logs for errors
docker-compose logs email-service
docker-compose logs docs

# Rebuild specific service
docker-compose build email-service
docker-compose up -d email-service
```

### Database connection issues
```bash
# Restart database
docker-compose restart mysql

# Check database logs
docker-compose logs mysql
```
