# 🍕 Food Delivery System - Complete Microservices with Web GUI

A comprehensive food delivery system built with **Spring Boot microservices architecture**, featuring a modern web-based GUI, service discovery, API gateway, and complete restaurant management functionality.

## 🎯 **FULLY FUNCTIONAL FEATURES**

### 🌟 **Restaurant Management System**
- **Complete CRUD Operations**: Add, edit, delete restaurants
- **Menu Management**: Add, edit, delete menu items for each restaurant
- **Admin Dashboard**: Full administrative interface
- **Customer Portal**: Browse restaurants and menus
- **Real-time Data**: All changes reflected immediately

### 🏗️ **Microservices Architecture**
- **Service Discovery**: Eureka-based service registry
- **API Gateway**: Centralized routing and load balancing
- **Individual Services**: Restaurant, Order, Payment, Delivery, Frontend
- **Inter-Service Communication**: REST APIs with WebClient
- **Database**: H2 in-memory database for development

## 🚀 **Quick Start**

### ⚡ **One-Command Setup**
```bash
./quick-start.sh
```
This will:
- Build all services
- Start services in proper order
- Wait for each service to be ready
- Add sample data (4 restaurants, 12 menu items)
- Display access URLs

### 🔧 **Manual Setup**
```bash
# 1. Build all services
mvn clean package -DskipTests

# 2. Start services in order
./start-services.sh

# 3. Add sample data
./add-sample-restaurants.sh
./add-sample-menus.sh
```

### 3. **Access the Application**
- 🎨 **Web Interface**: http://localhost:8085
- � **Admin Panel**: http://localhost:8085/admin/restaurants  
- � **Service Discovery**: http://localhost:8761
- 🌐 **API Gateway**: http://localhost:8080

## 📋 **Service Ports**

| Service | Port | Description | 
|---------|------|-------------|
| **Frontend GUI** | 8085 | Web Interface |
| **API Gateway** | 8080 | Central Routing |  
| **Service Discovery** | 8761 | Eureka Server |
| **Restaurant Service** | 8081 | Restaurant Management |
| **Order Service** | 8082 | Order Processing |
| **Delivery Service** | 8083 | Delivery Tracking |
| **Payment Service** | 8084 | Payment Processing |

## 🏗️ **Architecture Overview**

```
                    ┌─────────────────┐
                    │   Web Browser   │
                    │  (Port: 8085)   │
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐    ┌─────────────────┐
                    │ Frontend Service│────│ Service Registry│  
                    │  (Thymeleaf)    │    │  (Port: 8761)   │
                    └─────────┬───────┘    └─────────────────┘
                              │
                    ┌─────────▼───────┐
                    │   API Gateway   │
                    │  (Port: 8080)   │  
                    └─────────┬───────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
    ┌───────▼────────┐ ┌─────▼─────┐ ┌────▼────┐ ┌──────▼─────┐
    │ Restaurant     │ │   Order   │ │Delivery │ │  Payment   │
    │ Service        │ │  Service  │ │Service  │ │  Service   │
    │ (Port: 8081)   │ │(Port:8082)│ │(8083)   │ │ (Port:8084)│
    └────────────────┘ └───────────┘ └─────────┘ └────────────┘
```

### 4. **Order Service**
- **Port:** 8082
- **Purpose:** Handle customer orders, order status
- **Features:** Integration with Restaurant Service via Feign Client

### 5. **Delivery Service**
- **Port:** 8083
- **Purpose:** Manage delivery assignments and tracking

### 6. **Payment Service**
- **Port:** 8084
- **Purpose:** Process payments, refunds, payment history

## 🛠️ Tech Stack

- **Framework:** Spring Boot 3.1.0
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Database:** H2 (development), MySQL/PostgreSQL (production)
- **ORM:** Hibernate/JPA
- **Build Tool:** Maven
- **Java Version:** 17
- **Containerization:** Docker & Docker Compose
- **Inter-service Communication:** OpenFeign

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose (optional)

## 🏃‍♂️ Running the Application

### Option 1: Using Maven (Local Development)

1. **Start Service Discovery first:**
   ```bash
   cd service-discovery
   mvn spring-boot:run
   ```

2. **Start API Gateway:**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

3. **Start individual services:**
   ```bash
   # Terminal 1
   cd restaurant-service
   mvn spring-boot:run
   
   # Terminal 2
   cd order-service
   mvn spring-boot:run
   
   # Terminal 3
   cd delivery-service
   mvn spring-boot:run
   
   # Terminal 4
   cd payment-service
   mvn spring-boot:run
   ```

### Option 2: Using Docker Compose (Recommended)

1. **Build and start all services:**
   ```bash
   docker-compose up --build
   ```

2. **Start in detached mode:**
   ```bash
   docker-compose up -d --build
   ```

3. **Stop all services:**
   ```bash
   docker-compose down
   ```

## 🔧 Service Health Check

After starting services, verify they are running:

1. **Eureka Dashboard:** http://localhost:8761
2. **API Gateway:** http://localhost:8080/actuator/health
3. **Restaurant Service:** http://localhost:8081/actuator/health

## 🌐 API Testing

### Via API Gateway (Recommended):
```bash
# Get all restaurants
curl http://localhost:8080/api/restaurants

# Create a restaurant
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace",
    "address": "123 Main St",
    "phone": "555-0123",
    "cuisine": "Italian",
    "rating": 4.5
  }'

# Search restaurants
curl http://localhost:8080/api/restaurants/search?name=Pizza
```

### Direct Service Access:
```bash
# Direct access to restaurant service
curl http://localhost:8081/api/restaurants
```

## 📊 Database Access

Each service uses H2 in-memory database for development:

- **Restaurant Service:** http://localhost:8081/h2-console
- **Order Service:** http://localhost:8082/h2-console
- **Delivery Service:** http://localhost:8083/h2-console
- **Payment Service:** http://localhost:8084/h2-console

**Connection Details:**
- **JDBC URL:** `jdbc:h2:mem:{servicename}db`
- **Username:** `sa`
- **Password:** (empty)

## 🚀 Cloud Deployment

### AWS Deployment (Example)

1. **Build Docker images:**
   ```bash
   docker build -t food-delivery/service-discovery ./service-discovery
   docker build -t food-delivery/api-gateway ./api-gateway
   docker build -t food-delivery/restaurant-service ./restaurant-service
   # ... repeat for other services
   ```

2. **Push to AWS ECR:**
   ```bash
   aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin {account-id}.dkr.ecr.us-west-2.amazonaws.com
   docker tag food-delivery/restaurant-service:latest {account-id}.dkr.ecr.us-west-2.amazonaws.com/food-delivery/restaurant-service:latest
   docker push {account-id}.dkr.ecr.us-west-2.amazonaws.com/food-delivery/restaurant-service:latest
   ```

3. **Deploy using ECS/EKS or Elastic Beanstalk**

## 🔒 Production Configuration

For production deployment:

1. **Replace H2 with persistent database:**
   - Update `application-prod.properties`
   - Use RDS MySQL/PostgreSQL

2. **Add security:**
   - Spring Security
   - JWT authentication
   - API rate limiting

3. **Add monitoring:**
   - Spring Boot Actuator
   - Micrometer + Prometheus
   - ELK Stack for logging

4. **Configure external service discovery:**
   - AWS Service Discovery
   - Consul
   - Kubernetes Service Discovery

## 📁 Project Structure

```
food-delivery-system/
├── common-lib/           # Shared DTOs and utilities
├── service-discovery/    # Eureka Server
├── api-gateway/         # Spring Cloud Gateway
├── restaurant-service/  # Restaurant management
├── order-service/       # Order processing
├── delivery-service/    # Delivery tracking
├── payment-service/     # Payment processing
├── docker-compose.yml   # Container orchestration
└── README.md           # This file
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 📞 Support

For support, please open an issue or contact the development team.
