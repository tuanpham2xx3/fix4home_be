# Fix4Home Docker Setup

This document describes how to run the Fix4Home backend with Docker.

## Prerequisites

- Docker and Docker Compose installed on your system

## Services

The Docker setup includes:

- **MySQL Database**: Runs on port 3307 (mapped from container's 3306)
- **Spring Boot Application**: Runs on port 8080

## Docker Commands

### Build and Start Services

```bash
# Build and start containers in detached mode
docker-compose up -d

# View logs of all services
docker-compose logs

# View logs of a specific service
docker-compose logs mysql
docker-compose logs app

# Follow logs in real time
docker-compose logs -f
```

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (database data)
docker-compose down -v
```

### Other Useful Commands

```bash
# Restart all services
docker-compose restart

# Restart a specific service
docker-compose restart app
docker-compose restart mysql

# View running containers
docker-compose ps

# Rebuild containers (after code changes)
docker-compose up -d --build
```

## Accessing the Application

- API: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html

## Database Access

- Host: localhost
- Port: 3307
- Username: root
- Password: root123
- Database Name: fix4home_db