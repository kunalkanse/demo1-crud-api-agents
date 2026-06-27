# Docker Setup Guide for Product CRUD API

This guide explains how to build, run, and manage the Product CRUD API using Docker.

## 📋 Prerequisites

- **Docker** 20.10+ installed ([Install Docker](https://docs.docker.com/get-docker/))
- **Docker Compose** 2.0+ installed ([Install Docker Compose](https://docs.docker.com/compose/install/))
- **Git** (for cloning the repository)

Verify installation:
```bash
docker --version
docker-compose --version
```

---

## 🚀 Quick Start with Docker Compose

The easiest way to run the complete stack (API + MySQL database):

### 1. Start the Application
```bash
cd product-api
docker-compose up --build
```

**Expected Output:**
```
Creating product-api-mysql ... done
Creating product-api-app ... done
Attaching to product-api-mysql, product-api-app
product-api-mysql | 2025-05-26 10:00:00 0 [Note] Server socket created on IP: '::'.
product-api-app | 2025-05-26 10:00:10,123 INFO  [main] Started ProductApiApplication in 8.432 seconds
```

### 2. Access the API
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

### 3. Stop the Application
```bash
docker-compose down
```

---

## 🔧 Build Docker Image Manually

If you prefer building the image separately:

### Build the Image
```bash
cd product-api
docker build -t product-crud-api:1.0 .
```

### Run the Container
```bash
docker run -d \
  --name product-api \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/product_db \
  -e SPRING_DATASOURCE_USERNAME=product_user \
  -e SPRING_DATASOURCE_PASSWORD=product_password \
  product-crud-api:1.0
```

---

## 🗄️ Docker Compose File Breakdown

### MySQL Service
```yaml
mysql-db:
  image: mysql:8.0
  ports:
    - "3306:3306"
  environment:
    MYSQL_ROOT_PASSWORD: rootpassword
    MYSQL_DATABASE: product_db
    MYSQL_USER: product_user
    MYSQL_PASSWORD: product_password
```

**Access MySQL from host:**
```bash
mysql -h 127.0.0.1 -u product_user -p product_password product_db
```

### Spring Boot Application Service
```yaml
product-api:
  build: .
  ports:
    - "8080:8080"
  depends_on:
    mysql-db:
      condition: service_healthy
```

**Features:**
- ✅ Auto-builds from Dockerfile
- ✅ Waits for MySQL to be healthy before starting
- ✅ Exposes port 8080
- ✅ Auto-restarts on failure

---

## 📝 Environment Variables

Override default environment variables when running containers:

```bash
docker run -d \
  --name product-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/product_db \
  product-crud-api:1.0
```

**Common Variables:**
| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | docker | Spring profile (dev, test, production) |
| `JAVA_OPTS` | -Xmx512m -Xms256m | JVM memory settings |
| `SPRING_DATASOURCE_URL` | jdbc:mysql://mysql-db:3306/product_db | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | product_user | Database user |
| `SPRING_DATASOURCE_PASSWORD` | product_password | Database password |

---

## 📊 Common Docker Commands

### View Running Containers
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs product-api

# Follow logs in real-time
docker-compose logs -f product-api
```

### Execute Commands Inside Container
```bash
# Open shell in API container
docker-compose exec product-api sh

# Check database connectivity
docker-compose exec product-api curl http://localhost:8080/actuator/health
```

### Stop Individual Services
```bash
docker-compose stop product-api
docker-compose stop mysql-db
```

### Restart Services
```bash
docker-compose restart product-api
```

### Remove All Containers and Volumes
```bash
docker-compose down -v
```

---

## 🏗️ Dockerfile Explanation

The Dockerfile uses a **multi-stage build** for efficiency:

### Stage 1: Builder
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
# Builds the JAR file using Maven
RUN mvn clean package -DskipTests
```
- Uses Maven 3.9.6 with Java 17
- Compiles and packages the application
- Result: `app.jar` file

### Stage 2: Runtime
```dockerfile
FROM eclipse-temurin:17.0.11_9-jdk-alpine
# Runs the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
```
- Uses lightweight Alpine Linux base image
- Only includes JDK (no Maven or build tools)
- Creates non-root user for security
- **Result: ~300MB image** (vs ~800MB+ if built without multi-stage)

### Security Features
- ✅ Non-root user (`appuser`)
- ✅ Health checks enabled
- ✅ Read-only file system support

---

## 📈 Performance Tips

### 1. Layer Caching
Docker caches Dockerfile layers. To speed up builds:
- Change `pom.xml` rarely (it's cached)
- Put frequently-changed files (source code) later in Dockerfile
- Dependencies are downloaded once and cached

### 2. Memory Configuration
Adjust JVM heap size based on container limits:
```bash
docker run -e JAVA_OPTS="-Xmx1g -Xms512m" product-crud-api:1.0
```

### 3. Multi-Stage Build Benefits
Current setup uses multi-stage build:
- Final image size: **~300MB** (only runtime JDK)
- Without multi-stage: **~800MB+** (includes Maven and build tools)
- **Savings: 60-70% image size reduction**

---

## 🐛 Troubleshooting

### 1. Container Won't Start
```bash
# Check logs
docker-compose logs product-api

# Common issue: Port 8080 already in use
# Solution: Change port mapping
docker run -p 8081:8080 product-crud-api:1.0
```

### 2. Database Connection Error
```bash
# Check MySQL is running
docker-compose ps

# Check database logs
docker-compose logs mysql-db

# Verify connectivity from API container
docker-compose exec product-api mysql -h mysql-db -u product_user -p
```

### 3. Build Fails with "Out of Memory"
```bash
# Increase Docker's memory limit in Docker Desktop settings
# Or use build args to reduce memory usage
docker build --build-arg MAVEN_OPTS="-Xmx512m" .
```

### 4. Health Check Failing
```bash
# Check if application is responding
curl http://localhost:8080/actuator/health

# View detailed health info
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

---

## 🔐 Security Best Practices

### 1. Don't Use Default Passwords
```yaml
# ❌ Bad (in docker-compose.yml)
MYSQL_PASSWORD: product_password

# ✅ Good (use secrets or environment file)
MYSQL_PASSWORD: ${DB_PASSWORD}
```

Create `.env` file:
```
DB_PASSWORD=your-secure-password-here
MYSQL_ROOT_PASSWORD=your-root-password
```

Run with environment file:
```bash
docker-compose --env-file .env up
```

### 2. Use Private Docker Registry
```bash
# Tag image for private registry
docker tag product-crud-api:1.0 my-registry.com/product-api:1.0

# Push to registry
docker push my-registry.com/product-api:1.0
```

### 3. Scan Image for Vulnerabilities
```bash
# Using Trivy (free open-source scanner)
trivy image product-crud-api:1.0
```

### 4. Keep Base Images Updated
```bash
# Pull latest base images
docker pull eclipse-temurin:17.0.11_9-jdk-alpine
docker pull mysql:8.0

# Rebuild with latest base images
docker-compose build --pull
```

---

## 📦 Production Deployment

### Deploy to Docker Swarm
```bash
docker stack deploy -c docker-compose.yml product-api-stack
```

### Deploy to Kubernetes
Convert Docker Compose to Kubernetes:
```bash
# Using Kompose tool
kompose convert -f docker-compose.yml
kubectl apply -f product-api-service.yaml
```

### Use Docker Registry (e.g., DockerHub)
```bash
# Login to DockerHub
docker login

# Tag image
docker tag product-crud-api:1.0 your-username/product-crud-api:1.0

# Push to DockerHub
docker push your-username/product-crud-api:1.0
```

---

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topical/spring-boot-docker/)
- [Best Practices for Java Applications in Docker](https://www.docker.com/blog/containerized-java/)

---

## ✅ Verification Checklist

After running `docker-compose up`:

- [ ] Both containers are running: `docker-compose ps`
- [ ] API responds to health check: `curl http://localhost:8080/actuator/health`
- [ ] Swagger UI loads: http://localhost:8080/swagger-ui.html
- [ ] MySQL is accessible: `docker-compose exec mysql-db mysql -u product_user -p`
- [ ] Logs show no errors: `docker-compose logs`

---

**Questions?** Refer to Docker documentation or check application logs for detailed error messages.
