#!/bin/bash

echo "🛑 Stopping Food Delivery Microservices System..."

# Function to stop service by PID
stop_service() {
    local service_name=$1
    local pid_file=$2
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "⏹️  Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 2
            if ps -p $pid > /dev/null 2>&1; then
                echo "   Force killing $service_name..."
                kill -9 $pid
            fi
        else
            echo "   $service_name was not running"
        fi
        rm -f "$pid_file"
    else
        echo "   No PID file found for $service_name"
    fi
}

# Create logs directory if it doesn't exist
mkdir -p logs

# Stop all services
stop_service "Frontend Service" "logs/frontend-service.pid"
stop_service "Payment Service" "logs/payment-service.pid"
stop_service "Delivery Service" "logs/delivery-service.pid"
stop_service "Order Service" "logs/order-service.pid"
stop_service "Restaurant Service" "logs/restaurant-service.pid"
stop_service "API Gateway" "logs/api-gateway.pid"
stop_service "Service Discovery" "logs/service-discovery.pid"

# Kill any remaining Spring Boot processes
echo "🧹 Cleaning up any remaining processes..."
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "food-delivery" 2>/dev/null || true

echo ""
echo "✅ All services stopped successfully!"
echo "📝 Logs are preserved in the 'logs' directory"
