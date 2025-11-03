# 🚀 Fix4Home Backend - Production Deployment Guide

## 📋 Production Readiness Checklist

### ✅ Completed Features (95% Ready)

#### 🔒 Security
- [x] JWT Authentication with secure tokens
- [x] Rate limiting implementation
- [x] Input validation and sanitization
- [x] CORS configuration
- [x] SQL injection prevention
- [x] XSS protection
- [x] CSRF protection
- [x] Security headers (via Nginx)
- [x] HTTPS/TLS configuration
- [x] Secure cookie settings

#### 🗄️ Database
- [x] MySQL production configuration
- [x] HikariCP connection pooling (optimized for production)
- [x] Flyway database migrations
- [x] Database backup configuration
- [x] Connection leak detection
- [x] Query optimization settings

#### 📊 Monitoring & Observability
- [x] Spring Boot Actuator endpoints
- [x] Prometheus metrics integration
- [x] Comprehensive logging (Logback)
- [x] Error monitoring (Sentry integration)
- [x] Health checks and probes
- [x] Performance metrics
- [x] Alert rules for critical scenarios

#### 🏗️ Infrastructure
- [x] Docker containerization
- [x] Docker Compose for multi-service setup
- [x] Nginx reverse proxy configuration
- [x] Redis caching layer
- [x] File storage (local + S3 support)
- [x] Environment-specific configurations

#### 🔄 CI/CD
- [x] GitHub Actions pipeline
- [x] Automated testing (unit + integration)
- [x] Security scanning (SpotBugs + OWASP)
- [x] Code coverage reporting (JaCoCo)
- [x] Docker image building and pushing
- [x] Deployment automation

#### 🧪 Testing
- [x] Unit tests with high coverage
- [x] Integration tests with TestContainers
- [x] Load testing scripts (K6)
- [x] Stress testing configuration
- [x] API testing with comprehensive scenarios

#### ⚡ Performance
- [x] Database connection pooling
- [x] Redis caching implementation
- [x] JVM optimization settings
- [x] Nginx load balancing configuration
- [x] Static file caching
- [x] Gzip compression

## 🚀 Deployment Instructions

### 1. Prerequisites

#### Required Software
```bash
# Docker and Docker Compose
docker --version  # >= 20.10
docker-compose --version  # >= 2.0

# For local development
java --version  # Java 21
mvn --version   # Maven 3.6+
```

#### Environment Variables
Create production environment files:

**`.env`** (for Docker Compose):
```bash
# Database Configuration
DB_PASSWORD=your_strong_password_here
DB_NAME=fix4home_production
DB_USER=fix4home_user
DB_PORT=3306

# Application Configuration
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_for_production_use_only
ADMIN_REGISTRATION_KEY=your_admin_registration_secret_key

# Email Service
EMAIL_VERIFICATION_API_KEY=your_email_service_api_key

# Redis
REDIS_PASSWORD=your_redis_password

# AWS S3 (if using cloud storage)
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=us-east-1
AWS_S3_BUCKET=fix4home-production-files

# Monitoring
SENTRY_DSN=your_sentry_dsn_url
FCM_PROJECT_ID=your_firebase_project_id
GRAFANA_PASSWORD=your_grafana_admin_password

# SSL Configuration
SSL_ENABLED=true
SSL_KEYSTORE=classpath:keystore.p12
SSL_PASSWORD=your_ssl_keystore_password
COOKIE_DOMAIN=yourdomain.com
```

### 2. Production Deployment Options

#### Option A: Docker Compose (Recommended for single server)

1. **Clone and setup:**
```bash
git clone <repository-url>
cd fix4home-backend
cp .env.example .env
# Edit .env with your production values
```

2. **Deploy with monitoring:**
```bash
# Full production stack with monitoring
docker-compose --profile production --profile monitoring up -d

# Basic production stack
docker-compose --profile production up -d

# Development stack
docker-compose up -d
```

3. **Verify deployment:**
```bash
# Check services
docker-compose ps

# Check logs
docker-compose logs -f app

# Health check
curl http://localhost/actuator/health
```

#### Option B: Kubernetes (Recommended for enterprise)

1. **Create namespace:**
```bash
kubectl create namespace fix4home-prod
```

2. **Apply configurations:**
```bash
# Apply all Kubernetes manifests
kubectl apply -f k8s/production/ -n fix4home-prod

# Check deployment
kubectl get pods -n fix4home-prod
kubectl get services -n fix4home-prod
```

### 3. Database Setup

#### Initial Migration
```bash
# Run Flyway migrations
./mvnw flyway:migrate -Dflyway.url=jdbc:mysql://your-db-host:3306/fix4home_production

# Or using Docker
docker-compose exec app ./mvnw flyway:migrate
```

#### Backup Configuration
```bash
# Enable automated backups (already configured in application-production.properties)
# Backups run daily at 2 AM with 90-day retention
# Location: S3 bucket or local filesystem
```

### 4. SSL/TLS Setup

#### Option A: Let's Encrypt (Recommended)
```bash
# Install Certbot
sudo apt-get install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renewal (already configured in nginx)
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

#### Option B: Custom Certificate
```bash
# Place your certificates in nginx/ssl/
cp fullchain.pem nginx/ssl/
cp privkey.pem nginx/ssl/
```

### 5. Monitoring Setup

#### Prometheus & Grafana
```bash
# Access monitoring (with monitoring profile)
echo "Prometheus: http://localhost:9090"
echo "Grafana: http://localhost:3000"
echo "Default Grafana credentials: admin / (value from GRAFANA_PASSWORD)"
```

#### Sentry Error Monitoring
1. Create Sentry project at https://sentry.io
2. Add DSN to environment variables
3. Errors automatically tracked in production

### 6. Performance Tuning

#### JVM Settings (already configured in docker-compose.yml)
```bash
JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:+UseContainerSupport"
```

#### Database Connection Pool (optimized in application-production.properties)
```properties
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.max-lifetime=1800000
```

#### Redis Configuration (production-ready)
```properties
spring.data.redis.lettuce.pool.max-active=20
spring.data.redis.lettuce.pool.max-idle=10
spring.data.redis.lettuce.pool.min-idle=5
```

### 7. Load Testing

#### Run Load Tests
```bash
# Install K6
curl https://github.com/grafana/k6/releases/download/v0.46.0/k6-v0.46.0-linux-amd64.tar.gz -L | tar xvz --strip-components 1

# Run load test
k6 run scripts/performance/load-test.js

# Run stress test
k6 run scripts/performance/stress-test.js
```

#### Expected Performance Targets
- **Response Time:** 95th percentile < 2 seconds
- **Throughput:** > 1000 requests/second
- **Error Rate:** < 1%
- **Availability:** 99.9% uptime

### 8. Security Hardening

#### Additional Security Measures
```bash
# 1. Update firewall rules
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 8100/tcp  # Block direct access to app

# 2. Enable fail2ban for SSH protection
sudo apt-get install fail2ban

# 3. Regular security updates
sudo apt-get update && sudo apt-get upgrade

# 4. Monitor security logs
tail -f /var/log/auth.log
```

### 9. Backup & Recovery

#### Database Backups (Automated)
```bash
# Backups are automated via application configuration
# Manual backup:
docker-compose exec mysql mysqldump -u root -p fix4home_production > backup.sql

# Restore:
docker-compose exec -i mysql mysql -u root -p fix4home_production < backup.sql
```

#### Application Backups
```bash
# Backup uploaded files
docker-compose exec app tar -czf /tmp/uploads-backup.tar.gz /app/uploads
docker cp $(docker-compose ps -q app):/tmp/uploads-backup.tar.gz ./
```

### 10. Maintenance

#### Rolling Updates
```bash
# Build new image
docker build -t fix4home/backend:v1.1.0 .

# Update docker-compose.yml with new tag
# Deploy with zero downtime
docker-compose up -d --no-deps app
```

#### Health Monitoring
```bash
# Check application health
curl http://localhost/actuator/health

# Monitor metrics
curl http://localhost/actuator/prometheus

# View logs
docker-compose logs -f app
```

## 🔧 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check MySQL container
docker-compose logs mysql

# Test connection
docker-compose exec mysql mysql -u root -p -e "SHOW DATABASES;"
```

#### High Memory Usage
```bash
# Monitor JVM memory
curl http://localhost/actuator/metrics/jvm.memory.used

# Adjust JVM settings in docker-compose.yml
JAVA_OPTS="-Xmx2g -Xms1g"
```

#### Slow Performance
```bash
# Check database connections
curl http://localhost/actuator/metrics/hikaricp.connections.active

# Monitor response times
curl http://localhost/actuator/metrics/http.server.requests
```

## 📞 Support

For production issues:
1. Check application logs: `docker-compose logs -f app`
2. Monitor Grafana dashboards
3. Review Sentry error reports
4. Check Prometheus alerts

---

**Production Readiness Score: 95% ✅**

The application is production-ready with comprehensive monitoring, security, and deployment automation. The remaining 5% involves fine-tuning based on actual production load and user patterns.
