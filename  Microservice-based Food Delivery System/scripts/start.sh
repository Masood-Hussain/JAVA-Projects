#!/bin/bash

echo "🚀 Starting Food Delivery Microservices System..."

# Function to check if port is available
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        echo "❌ Port $1 is already in use"
        return 1
    else
        return 0
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1

    echo "⏳ Waiting for $service_name to start on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:$port/actuator/health >/dev/null 2>&1 || curl -s http://localhost:$port >/dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        echo "   Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 3
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service_name failed to start within timeout"
    return 1
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Build all services
echo "🔨 Building all services..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

# Check required ports
echo "🔍 Checking required ports..."
ports=(8761 8080 8081 8082 8083 8084)
for port in "${ports[@]}"; do
    if ! check_port $port; then
        echo "Please stop the service using port $port and try again."
        exit 1
    fi
done

# Create logs directory
mkdir -p logs

# Start services in order
echo ""
echo "1️⃣ Starting Service Discovery (Eureka Server)..."
cd service-discovery
mvn spring-boot:run > ../logs/service-discovery.log 2>&1 &
SERVICE_DISCOVERY_PID=$!
cd ..

wait_for_service "Service Discovery" 8761

echo ""
echo "2️⃣ Starting API Gateway..."
cd api-gateway
mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
API_GATEWAY_PID=$!
cd ..

wait_for_service "API Gateway" 8080

echo ""
echo "3️⃣ Starting Restaurant Service..."
cd restaurant-service
mvn spring-boot:run > ../logs/restaurant-service.log 2>&1 &
RESTAURANT_PID=$!
cd ..

wait_for_service "Restaurant Service" 8081

echo ""
echo "4️⃣ Starting Order Service..."
cd order-service
mvn spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!
cd ..

wait_for_service "Order Service" 8082

echo ""
echo "5️⃣ Starting Delivery Service..."
cd delivery-service
mvn spring-boot:run > ../logs/delivery-service.log 2>&1 &
DELIVERY_PID=$!
cd ..

wait_for_service "Delivery Service" 8083

echo ""
echo "6️⃣ Starting Payment Service..."
cd payment-service
mvn spring-boot:run > ../logs/payment-service.log 2>&1 &
PAYMENT_PID=$!
cd ..

wait_for_service "Payment Service" 8084

echo ""
echo "7️⃣ Starting Frontend Service..."
cd frontend-service
mvn spring-boot:run > ../logs/frontend-service.log 2>&1 &
FRONTEND_PID=$!
cd ..

wait_for_service "Frontend Service" 8085

echo ""
echo "🎉 All services are now running!"
echo ""
echo "📊 Service URLs:"
echo "   🔍 Service Discovery: http://localhost:8761"
echo "   🌐 API Gateway: http://localhost:8080"
echo "   🎨 Frontend GUI: http://localhost:8085"
echo "   🍕 Restaurant Service: http://localhost:8081"
echo "   📦 Order Service: http://localhost:8082"
echo "   🚚 Delivery Service: http://localhost:8083"
echo "   💳 Payment Service: http://localhost:8084"
echo ""
echo "🔗 API Endpoints (via Gateway):"
echo "   GET  http://localhost:8080/api/restaurants"
echo "   POST http://localhost:8080/api/orders"
echo "   GET  http://localhost:8080/api/deliveries"
echo "   POST http://localhost:8080/api/payments"
echo ""
echo "📝 Logs are saved in the 'logs' directory"
echo "⏹️  To stop all services, run: ./stop-services.sh"

# Save PIDs for cleanup
echo $SERVICE_DISCOVERY_PID > logs/service-discovery.pid
echo $API_GATEWAY_PID > logs/api-gateway.pid
echo $RESTAURANT_PID > logs/restaurant-service.pid
echo $ORDER_PID > logs/order-service.pid
echo $DELIVERY_PID > logs/delivery-service.pid
echo $PAYMENT_PID > logs/payment-service.pid
echo $FRONTEND_PID > logs/frontend-service.pid

# Keep the script running
echo ""
echo "Press Ctrl+C to stop all services..."
trap 'echo "Stopping services..."; ./stop-services.sh; exit' INT
wait
