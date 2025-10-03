# Frontend Service - Comprehensive Documentation

## Overview

The Frontend Service is a comprehensive Spring Boot microservice that provides both traditional web UI and modern REST API endpoints for the Food Delivery System. It serves as the primary user interface layer, integrating with all backend microservices to deliver a complete user experience.

## Features

### 🎨 Modern Web Interface
- **Responsive Design**: Modern, mobile-first responsive web interface
- **Thymeleaf Templates**: Server-side rendering with dynamic content
- **Multiple UI Modes**: Both classic and modern UI implementations
- **Real-time Updates**: Dynamic content updates and status tracking

### 🔌 Comprehensive API Integration
- **Restaurant Service**: Menu browsing, restaurant management
- **Order Service**: Order creation, tracking, and management
- **Delivery Service**: Real-time delivery tracking
- **Payment Service**: Secure payment processing
- **Service Discovery**: Automatic service discovery via Eureka

### 🛡️ Production-Ready Features
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Logging**: Structured logging throughout the application
- **Health Checks**: Service health monitoring and reporting
- **CORS Support**: Cross-origin resource sharing configuration
- **Async Processing**: Non-blocking operations for better performance

## Architecture

### Service Layer
```
FrontendService
├── Restaurant Management
├── Order Management  
├── Delivery Tracking
├── Payment Processing
└── Health Monitoring
```

### Controller Layer
```
Controllers
├── ModernUIController (REST APIs + Modern UI)
├── WebController (Traditional Web Forms)
└── Configuration (CORS, Async, Web Config)
```

### Model Layer
```
Frontend Models
├── OrderForm (Order submission)
├── OrderItemForm (Order items)
└── PaymentForm (Payment processing)
```

## API Endpoints

### REST API Endpoints (`/api/*`)

#### Health & Information
- `GET /api/health` - Service health check
- `GET /api/info` - Service information and status

#### Restaurant Management
- `GET /api/restaurants` - Get all restaurants
- `GET /api/restaurants/{id}` - Get restaurant by ID
- `GET /api/restaurants/{id}/menu` - Get restaurant menu items

#### Order Management
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create new order
- `GET /api/orders/customer/{customerId}` - Get orders by customer
- `GET /api/orders/restaurant/{restaurantId}` - Get orders by restaurant

### Web UI Endpoints

#### Modern UI
- `GET /` - Home page (modern UI)
- `GET /restaurants` - Restaurant listing
- `GET /orders` - Order management
- `GET /admin` - Admin dashboard
- `GET /help` - Help and documentation

#### Classic UI
- `GET /classic` - Classic home page
- `GET /classic/restaurants` - Classic restaurant listing
- `GET /restaurant/{id}` - Restaurant details
- `GET /menu/{restaurantId}` - Restaurant menu
- `GET /track/{orderId}` - Order tracking
- `GET /checkout/{restaurantId}` - Checkout process

#### Admin Interface
- `GET /admin` - Admin dashboard
- `GET /admin/restaurants` - Restaurant management
- `GET /admin/orders` - Order management
- `GET /admin/analytics` - Analytics dashboard

## Configuration

### Application Properties
```yaml
server:
  port: 8085

spring:
  application:
    name: frontend-service
  
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Service URLs
```yaml
services:
  restaurant-service: http://restaurant-service
  order-service: http://order-service
  delivery-service: http://delivery-service
  payment-service: http://payment-service
```

## Dependencies

### Core Dependencies
- **Spring Boot Starter Web**: Web framework
- **Spring Boot Starter Thymeleaf**: Template engine
- **Spring Boot Starter WebFlux**: Reactive HTTP client
- **Spring Cloud Starter Netflix Eureka Client**: Service discovery
- **Common Library**: Shared models and utilities

### Build Configuration
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## Usage Examples

### Creating an Order via API
```bash
curl -X POST http://localhost:8085/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "123 Main St",
    "customerPhone": "+1234567890",
    "customerEmail": "user@example.com",
    "items": [
      {
        "menuItemId": 1,
        "itemName": "Burger",
        "quantity": 2,
        "price": 12.99
      }
    ]
  }'
```

### Getting Restaurant Information
```bash
curl http://localhost:8085/api/restaurants/1
```

### Health Check
```bash
curl http://localhost:8085/api/health
```

## Error Handling

### API Error Responses
```json
{
  "error": "Restaurant not found",
  "message": "Restaurant with ID 999 does not exist",
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404
}
```

### Web Error Pages
- Custom error pages for 404, 500, and other HTTP errors
- User-friendly error messages
- Automatic redirect to safe pages

## Testing

### Running Tests
```bash
# Run the comprehensive test suite
./test-frontend-service.sh

# Test specific endpoints
curl http://localhost:8085/api/health
curl http://localhost:8085/api/restaurants
```

### Test Coverage
- ✅ REST API endpoints
- ✅ Web page accessibility
- ✅ Error handling
- ✅ CORS configuration
- ✅ Service integration
- ✅ Performance testing

## Deployment

### Local Development
```bash
# Build the service
mvn clean package

# Run the service
java -jar target/frontend-service-1.0.0.jar

# Or use Maven
mvn spring-boot:run
```

### Docker Deployment
```bash
# Build Docker image
docker build -t frontend-service .

# Run container
docker run -p 8085:8085 frontend-service
```

### Production Deployment
1. Configure production service URLs
2. Set up proper logging levels
3. Configure health checks
4. Set up monitoring and alerting

## Monitoring & Observability

### Health Checks
- Service health endpoint: `/api/health`
- Dependency health checks included
- Automatic failover handling

### Logging
- Structured logging with SLF4J
- Request/response logging
- Error tracking and reporting
- Performance metrics

### Metrics
- Service response times
- Error rates
- Request volumes
- Service dependency status

## Security

### CORS Configuration
- Configurable allowed origins
- Secure headers handling
- API endpoint protection

### Input Validation
- Form data validation
- API request validation
- XSS protection via Thymeleaf

## Performance Optimization

### Async Processing
- Non-blocking HTTP clients
- Async service calls
- Thread pool configuration

### Caching
- Template caching
- Static resource caching
- Service response caching

### Resource Management
- Connection pooling
- Timeout configuration
- Resource cleanup

## Troubleshooting

### Common Issues

1. **Service Discovery Issues**
   - Check Eureka server status
   - Verify service registration
   - Check network connectivity

2. **API Integration Problems**
   - Verify service endpoints
   - Check service health
   - Review timeout configurations

3. **Web UI Issues**
   - Check template paths
   - Verify static resources
   - Review CORS settings

### Debug Mode
```yaml
logging:
  level:
    com.fooddelivery.frontend: DEBUG
    org.springframework.web: DEBUG
```

## Contributing

### Development Guidelines
1. Follow Spring Boot best practices
2. Add comprehensive logging
3. Include error handling
4. Write unit tests
5. Update documentation

### Code Structure
```
src/main/java/com/fooddelivery/frontend/
├── FrontendServiceApplication.java
├── service/
│   └── FrontendService.java
├── controller/
│   ├── ModernUIController.java
│   └── WebController.java
├── config/
│   ├── WebConfig.java
│   └── AsyncConfig.java
└── model/
    ├── OrderForm.java
    ├── OrderItemForm.java
    └── PaymentForm.java
```

## Future Enhancements

### Planned Features
- [ ] Real-time notifications
- [ ] Progressive Web App (PWA) support
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] Enhanced mobile experience

### Technical Improvements
- [ ] GraphQL API support
- [ ] Enhanced caching strategies
- [ ] Microservice circuit breakers
- [ ] Advanced monitoring integration
- [ ] Performance optimization

## Support

For technical support or questions:
- Check application logs: `logs/frontend-service.log`
- Review health endpoints: `/api/health`
- Run test suite: `./test-frontend-service.sh`
- Check service discovery: Eureka dashboard

---

**Frontend Service** - Version 1.0.0  
*Comprehensive web interface and API gateway for the Food Delivery System*