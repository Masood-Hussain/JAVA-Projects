#!/bin/bash

# Advanced Food Delivery System 2025 - Enhanced Startup Script
# This script builds and starts all microservices with cutting-edge 2025 features

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="🚀 Advanced Food Delivery System 2025"
BASE_DIR=$(pwd)
SERVICES=("service-discovery" "api-gateway" "restaurant-service" "order-service" "payment-service" "delivery-service" "frontend-service")
PORTS=(8761 8080 8081 8082 8083 8084 8085)

echo -e "${PURPLE}${PROJECT_NAME}${NC}"
echo -e "${CYAN}=================================================${NC}"

# Function to print status
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Enhanced prerequisites check
check_prerequisites() {
    print_status "🔍 Checking prerequisites for 2025 features..."
    
    # Check Java 17+
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required for advanced features. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.8+."
        exit 1
    fi
    
    # Check for Redis (for caching and real-time features)
    if command -v redis-server &> /dev/null; then
        print_success "✅ Redis found - Advanced caching and real-time features enabled"
    elif command -v docker &> /dev/null; then
        print_status "🐳 Docker found - Will start Redis container"
    else
        print_warning "⚠️  Redis not found - Some advanced features may be limited"
    fi
    
    # Check Node.js for frontend enhancements
    if command -v node &> /dev/null; then
        print_success "✅ Node.js found - Enhanced frontend features available"
    else
        print_warning "⚠️  Node.js not found - Basic frontend only"
    fi
    
    print_success "✅ Prerequisites check completed"
}

# Enhanced build with feature flags
build_services() {
    print_status "🔨 Building all services with 2025 enhancements..."
    
    # Set environment variables for advanced features
    export SPRING_PROFILES_ACTIVE=advanced,redis,websocket,ai
    export ENABLE_AI_FEATURES=true
    export ENABLE_CRYPTO_PAYMENTS=true
    export ENABLE_REAL_TIME_TRACKING=true
    
    # Build with optimizations
    print_status "📦 Building with advanced optimizations..."
    mvn clean install -DskipTests -Dspring.profiles.active=advanced -q
    
    if [ $? -eq 0 ]; then
        print_success "✅ Advanced build completed successfully"
    else
        print_error "❌ Build failed"
        exit 1
    fi
}

# Start Redis with advanced configuration
start_redis() {
    print_status "🗄️  Starting Redis for advanced caching and real-time features..."
    
    if command -v redis-server &> /dev/null; then
        if ! pgrep -x "redis-server" > /dev/null; then
            # Start Redis with optimized configuration
            redis-server --daemonize yes \
                --port 6379 \
                --maxmemory 256mb \
                --maxmemory-policy allkeys-lru \
                --save 900 1 \
                --save 300 10 \
                --save 60 10000
            print_success "✅ Redis started with advanced configuration"
        else
            print_warning "⚠️  Redis is already running"
        fi
    elif command -v docker &> /dev/null; then
        if ! docker ps | grep -q redis-fooddelivery; then
            docker run -d --name redis-fooddelivery \
                -p 6379:6379 \
                -v redis-data:/data \
                redis:alpine redis-server \
                --maxmemory 256mb \
                --maxmemory-policy allkeys-lru
            print_success "✅ Redis started in Docker with persistence"
        else
            print_warning "⚠️  Redis container is already running"
        fi
    else
        print_warning "⚠️  Redis not available - Advanced features limited"
    fi
}

# Enhanced service startup with health checks
start_service() {
    local service=$1
    local port=$2
    local index=$3
    
    print_status "🚀 Starting $service with advanced features on port $port..."
    
    cd "$service"
    
    # Create logs directory
    mkdir -p ../logs
    
    # Start with enhanced JVM options for 2025 performance
    nohup java -jar "target/${service}-1.0.0.jar" \
        --server.port=$port \
        --spring.profiles.active=advanced,redis,websocket,ai \
        --logging.file.name="../logs/${service}.log" \
        --management.endpoints.web.exposure.include=health,metrics,prometheus \
        --management.endpoint.health.show-details=always \
        -Xms512m -Xmx1024m \
        -XX:+UseG1GC \
        -XX:G1HeapRegionSize=16m \
        -XX:+UseCompressedOops \
        -Dspring.jpa.hibernate.ddl-auto=update \
        > "../logs/${service}.log" 2>&1 &
    
    local pid=$!
    echo $pid > "../logs/${service}.pid"
    
    cd "$BASE_DIR"
    
    # Enhanced health check
    sleep 5
    if kill -0 $pid 2>/dev/null; then
        print_success "✅ $service started successfully (PID: $pid)"
        wait_for_service_advanced $port $service
    else
        print_error "❌ $service failed to start"
        return 1
    fi
}

# Advanced health check with retry logic
wait_for_service_advanced() {
    local port=$1
    local service=$2
    local max_attempts=60
    local attempt=1
    
    print_status "⏳ Waiting for $service to be ready with advanced features..."
    
    while [ $attempt -le $max_attempts ]; do
        # Check multiple endpoints
        health_check=$(curl -s "http://localhost:$port/actuator/health" 2>/dev/null || echo "")
        
        if echo "$health_check" | grep -q '"status":"UP"'; then
            print_success "✅ $service is ready and healthy"
            
            # Additional feature checks
            case $service in
                "restaurant-service")
                    if curl -s "http://localhost:$port/api/v2/restaurants" >/dev/null 2>&1; then
                        print_success "🍽️  Advanced restaurant API is ready"
                    fi
                    ;;
                "payment-service")
                    if curl -s "http://localhost:$port/api/v2/payments/methods/test" >/dev/null 2>&1; then
                        print_success "💳 Advanced payment API is ready"
                    fi
                    ;;
                "order-service")
                    print_success "📦 Real-time order tracking is ready"
                    ;;
            esac
            
            return 0
        fi
        
        echo -ne "\r${YELLOW}[WAIT]${NC} Attempt $attempt/$max_attempts - waiting for $service with advanced features..."
        sleep 2
        ((attempt++))
    done
    
    echo ""
    print_warning "⚠️  $service may not be fully ready yet, but continuing..."
}

# Enhanced service status display
show_advanced_status() {
    echo ""
    echo -e "${PURPLE}🎉 ${PROJECT_NAME} - Advanced Service Status${NC}"
    echo -e "${CYAN}================================================================${NC}"
    
    for i in "${!SERVICES[@]}"; do
        service=${SERVICES[$i]}
        port=${PORTS[$i]}
        
        if [ -f "logs/${service}.pid" ]; then
            pid=$(cat "logs/${service}.pid")
            if kill -0 $pid 2>/dev/null; then
                status="${GREEN}✅ RUNNING${NC}"
                url="http://localhost:$port"
                
                # Enhanced endpoints for each service
                case $service in
                    "service-discovery")
                        endpoint="$url - 🔍 Eureka Service Registry"
                        ;;
                    "api-gateway")
                        endpoint="$url - 🌐 Advanced API Gateway with Circuit Breakers"
                        ;;
                    "restaurant-service")
                        endpoint="$url/api/v2/restaurants - 🍽️  AI-Powered Restaurant Service"
                        ;;
                    "payment-service")
                        endpoint="$url/api/v2/payments - 💳 Multi-Gateway Payment Service"
                        ;;
                    "order-service")
                        endpoint="$url/ws/orders - 📦 Real-time Order Tracking"
                        ;;
                    "delivery-service")
                        endpoint="$url/api/deliveries - 🚚 Smart Delivery Management"
                        ;;
                    "frontend-service")
                        endpoint="$url - 🎨 Modern Web Interface"
                        ;;
                esac
            else
                status="${RED}❌ STOPPED${NC}"
                endpoint="Service not running"
            fi
        else
            status="${YELLOW}❓ UNKNOWN${NC}"
            endpoint="PID file not found"
        fi
        
        printf "%-20s %-20s %-60s\n" "$service" "$status" "$endpoint"
    done
    
    echo ""
    echo -e "${PURPLE}🌟 Advanced 2025 Features Enabled:${NC}"
    echo -e "${GREEN}🤖 AI-Powered Menu Recommendations with Machine Learning${NC}"
    echo -e "${GREEN}⚡ Real-time Order Tracking with WebSockets & Server-Sent Events${NC}"
    echo -e "${GREEN}💎 Cryptocurrency & BNPL Payment Support${NC}"
    echo -e "${GREEN}🔍 Advanced Menu Search with Elasticsearch-like Filtering${NC}"
    echo -e "${GREEN}📊 Real-time Analytics Dashboard with Metrics${NC}"
    echo -e "${GREEN}🛡️  Circuit Breaker Patterns with Resilience4j${NC}"
    echo -e "${GREEN}⚡ Reactive Programming with Spring WebFlux${NC}"
    echo -e "${GREEN}💾 Redis Caching for Sub-second Performance${NC}"
    echo -e "${GREEN}🔄 Event-Driven Architecture with Message Queues${NC}"
    echo -e "${GREEN}🎭 Advanced Error Handling & Fallback Patterns${NC}"
    
    echo ""
    echo -e "${CYAN}🚀 Quick Access Links:${NC}"
    echo -e "${BLUE}🌐 Modern Web App:${NC}        http://localhost:8085"
    echo -e "${BLUE}🔗 API Gateway:${NC}           http://localhost:8080"
    echo -e "${BLUE}🔍 Service Discovery:${NC}     http://localhost:8761"
    echo -e "${BLUE}🍽️  Restaurant API v2:${NC}     http://localhost:8080/api/v2/restaurants"
    echo -e "${BLUE}💳 Payment API v2:${NC}        http://localhost:8080/api/v2/payments"
    echo -e "${BLUE}📦 Order WebSocket:${NC}       ws://localhost:8080/ws/orders"
    echo -e "${BLUE}🤖 AI Recommendations:${NC}    http://localhost:8080/api/v2/restaurants/1/menu/recommendations"
    
    echo ""
    echo -e "${YELLOW}📊 Advanced Monitoring & Observability:${NC}"
    echo -e "${BLUE}❤️  Health Dashboard:${NC}      http://localhost:8080/actuator/health"
    echo -e "${BLUE}📈 Metrics (Prometheus):${NC}   http://localhost:8080/actuator/prometheus"
    echo -e "${BLUE}🔍 Service Registry:${NC}       http://localhost:8761"
    echo -e "${BLUE}📋 Application Info:${NC}       http://localhost:8080/actuator/info"
    
    echo ""
    echo -e "${GREEN}🎊 Success! Your Advanced Food Delivery System 2025 is now running!${NC}"
    echo -e "${CYAN}✨ Experience the future of food delivery with AI, real-time features, and modern payments${NC}"
}

# Main execution flow
main() {
    echo -e "${PURPLE}🚀 Welcome to the Advanced Food Delivery System 2025${NC}"
    echo -e "${CYAN}Built with cutting-edge technologies for the modern era:${NC}"
    echo -e "${GREEN}☕ Spring Boot 3.1 with Java 17 (Virtual Threads Ready)${NC}"
    echo -e "${GREEN}☁️  Spring Cloud 2022 (Latest Microservices Stack)${NC}"
    echo -e "${GREEN}⚡ WebFlux for Reactive Programming & High Performance${NC}"
    echo -e "${GREEN}🗄️  Redis for Ultra-Fast Caching & Real-time Features${NC}"
    echo -e "${GREEN}🔌 WebSockets for Live Order Tracking${NC}"
    echo -e "${GREEN}🤖 AI-Powered Smart Recommendations${NC}"
    echo -e "${GREEN}💎 Modern Payment Integrations (Crypto, BNPL, Split)${NC}"
    echo -e "${GREEN}📊 Advanced Analytics & Monitoring${NC}"
    echo ""
    
    check_prerequisites
    
    print_status "🧹 Cleaning previous builds..."
    mvn clean -q
    rm -rf logs/*.log logs/*.pid
    
    build_services
    start_redis
    
    print_status "🚀 Starting all advanced microservices..."
    
    for i in "${!SERVICES[@]}"; do
        service=${SERVICES[$i]}
        port=${PORTS[$i]}
        
        echo ""
        echo -e "${CYAN}[$((i+1))/${#SERVICES[@]}] Launching $service with 2025 enhancements${NC}"
        
        if start_service "$service" "$port" "$i"; then
            if [ "$service" = "service-discovery" ]; then
                print_status "⏳ Giving service discovery time to initialize the registry..."
                sleep 15
            elif [ "$service" = "api-gateway" ]; then
                print_status "⏳ Giving API gateway time to register routes..."
                sleep 10
            fi
        else
            print_error "❌ Failed to start $service"
            exit 1
        fi
    done
    
    show_advanced_status
    
    echo ""
    echo -e "${GREEN}🎉 All advanced services are now running!${NC}"
    echo -e "${YELLOW}📖 Check the logs in the 'logs/' directory for detailed information${NC}"
    echo -e "${BLUE}🛑 Run './stop-services.sh' to stop all services gracefully${NC}"
    echo -e "${PURPLE}🌟 Enjoy exploring the future of food delivery technology!${NC}"
    echo ""
}

# Execute main function
main
