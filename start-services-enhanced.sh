#!/bin/bash

# Enhanced Food Delivery System - Service Startup Script
# This script starts all microservices with improved logging and monitoring

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
LOG_DIR="logs"
WAIT_TIME=30
HEALTH_CHECK_RETRIES=5

# Service configurations
declare -A services=(
    ["service-discovery"]="8761"
    ["api-gateway"]="8080"
    ["restaurant-service"]="8081"
    ["order-service"]="8082"
    ["delivery-service"]="8083"
    ["payment-service"]="8084"
    ["user-service"]="8086"
    ["frontend-service"]="8085"
)

# Create logs directory
mkdir -p $LOG_DIR

echo -e "${BLUE}🍕 Starting Enhanced Food Delivery System...${NC}"
echo "=================================="

# Function to print status
print_status() {
    local service=$1
    local status=$2
    local port=$3
    
    case $status in
        "starting")
            echo -e "${YELLOW}⏳ Starting $service on port $port...${NC}"
            ;;
        "started")
            echo -e "${GREEN}✅ $service started successfully on port $port${NC}"
            ;;
        "failed")
            echo -e "${RED}❌ Failed to start $service${NC}"
            ;;
        "healthy")
            echo -e "${GREEN}💚 $service is healthy${NC}"
            ;;
    esac
}

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1
    else
        return 0
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service=$1
    local port=$2
    local retries=$HEALTH_CHECK_RETRIES
    
    while [ $retries -gt 0 ]; do
        if curl -s http://localhost:$port/actuator/health >/dev/null 2>&1; then
            print_status $service "healthy" $port
            return 0
        fi
        sleep 2
        retries=$((retries-1))
    done
    
    echo -e "${YELLOW}⚠️  $service may not be fully ready yet${NC}"
    return 1
}

# Function to start a service
start_service() {
    local service=$1
    local port=$2
    
    print_status $service "starting" $port
    
    # Check if port is available
    if ! check_port $port; then
        echo -e "${YELLOW}⚠️  Port $port is already in use. Skipping $service.${NC}"
        return 1
    fi
    
    # Start the service
    cd $service
    
    # Build if needed
    if [ ! -f "target/$service-1.0.0.jar" ]; then
        echo -e "${YELLOW}🔨 Building $service...${NC}"
        mvn clean package -DskipTests -q
    fi
    
    # Start service in background
    nohup java -jar target/$service-1.0.0.jar \
        --spring.profiles.active=dev \
        --logging.file.name=../$LOG_DIR/$service.log \
        > ../$LOG_DIR/$service.out 2>&1 &
    
    local pid=$!
    echo $pid > ../$LOG_DIR/$service.pid
    
    cd ..
    
    # Wait a bit for service to start
    sleep 5
    
    # Check if process is still running
    if ps -p $pid > /dev/null; then
        print_status $service "started" $port
        return 0
    else
        print_status $service "failed" $port
        return 1
    fi
}

# Function to build all services
build_all() {
    echo -e "${BLUE}🔨 Building all services...${NC}"
    mvn clean install -DskipTests
    echo -e "${GREEN}✅ Build completed${NC}"
}

# Function to stop existing services
stop_existing() {
    echo -e "${YELLOW}🛑 Stopping existing services...${NC}"
    
    # Kill processes by PID files
    for service in "${!services[@]}"; do
        if [ -f "$LOG_DIR/$service.pid" ]; then
            local pid=$(cat $LOG_DIR/$service.pid)
            if ps -p $pid > /dev/null 2>&1; then
                kill $pid
                echo -e "${YELLOW}Stopped $service (PID: $pid)${NC}"
            fi
            rm -f $LOG_DIR/$service.pid
        fi
    done
    
    # Kill by port
    for port in "${services[@]}"; do
        local pid=$(lsof -ti :$port 2>/dev/null || true)
        if [ ! -z "$pid" ]; then
            kill $pid 2>/dev/null || true
            echo -e "${YELLOW}Killed process on port $port${NC}"
        fi
    done
    
    sleep 3
}

# Function to show service status
show_status() {
    echo -e "\n${BLUE}📊 Service Status:${NC}"
    echo "=================="
    
    for service in "${!services[@]}"; do
        local port=${services[$service]}
        if curl -s http://localhost:$port/actuator/health >/dev/null 2>&1; then
            echo -e "${GREEN}✅ $service - http://localhost:$port${NC}"
        else
            echo -e "${RED}❌ $service - Port $port${NC}"
        fi
    done
}

# Function to show useful URLs
show_urls() {
    echo -e "\n${BLUE}🔗 Access URLs:${NC}"
    echo "==============="
    echo -e "${GREEN}🌐 Frontend Application: http://localhost:8085${NC}"
    echo -e "${GREEN}🎛️  Admin Dashboard: http://localhost:8085/admin${NC}"
    echo -e "${GREEN}🔍 Service Discovery: http://localhost:8761${NC}"
    echo -e "${GREEN}🚪 API Gateway: http://localhost:8080${NC}"
    echo -e "${GREEN}👤 User Service: http://localhost:8086${NC}"
    echo ""
    echo -e "${BLUE}📚 API Documentation:${NC}"
    echo -e "${GREEN}📖 Restaurant API: http://localhost:8081/swagger-ui.html${NC}"
    echo -e "${GREEN}📖 Order API: http://localhost:8082/swagger-ui.html${NC}"
    echo -e "${GREEN}📖 User API: http://localhost:8086/swagger-ui.html${NC}"
}

# Function to monitor logs
monitor_logs() {
    echo -e "\n${BLUE}📋 Monitoring logs... (Press Ctrl+C to exit)${NC}"
    tail -f $LOG_DIR/*.log 2>/dev/null || echo "No log files found yet."
}

# Main execution
main() {
    case "${1:-start}" in
        "build")
            build_all
            ;;
        "stop")
            stop_existing
            ;;
        "restart")
            stop_existing
            sleep 2
            main "start"
            ;;
        "status")
            show_status
            ;;
        "logs")
            monitor_logs
            ;;
        "start"|"")
            # Build all services first
            build_all
            
            # Stop any existing services
            stop_existing
            
            # Start services in order
            local failed_services=()
            
            # Start services in dependency order
            start_service "service-discovery" "8761" || failed_services+=("service-discovery")
            
            if [ ${#failed_services[@]} -eq 0 ]; then
                echo -e "${YELLOW}⏳ Waiting for Service Discovery to be ready...${NC}"
                sleep $WAIT_TIME
                wait_for_service "service-discovery" "8761"
            fi
            
            # Start other services
            start_service "user-service" "8086" || failed_services+=("user-service")
            start_service "restaurant-service" "8081" || failed_services+=("restaurant-service")
            start_service "order-service" "8082" || failed_services+=("order-service")
            start_service "delivery-service" "8083" || failed_services+=("delivery-service")
            start_service "payment-service" "8084" || failed_services+=("payment-service")
            start_service "api-gateway" "8080" || failed_services+=("api-gateway")
            start_service "frontend-service" "8085" || failed_services+=("frontend-service")
            
            # Wait for all services to be ready
            echo -e "${YELLOW}⏳ Waiting for all services to be ready...${NC}"
            sleep 15
            
            # Show final status
            show_status
            show_urls
            
            if [ ${#failed_services[@]} -gt 0 ]; then
                echo -e "\n${RED}⚠️  Some services failed to start: ${failed_services[*]}${NC}"
                echo -e "${YELLOW}Check logs in $LOG_DIR/ for details${NC}"
                exit 1
            else
                echo -e "\n${GREEN}🎉 All services started successfully!${NC}"
                echo -e "${BLUE}💡 Use '$0 logs' to monitor logs${NC}"
                echo -e "${BLUE}💡 Use '$0 stop' to stop all services${NC}"
            fi
            ;;
        *)
            echo "Usage: $0 {start|stop|restart|build|status|logs}"
            echo ""
            echo "Commands:"
            echo "  start    - Start all services (default)"
            echo "  stop     - Stop all services"
            echo "  restart  - Restart all services"
            echo "  build    - Build all services"
            echo "  status   - Show service status"
            echo "  logs     - Monitor service logs"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
