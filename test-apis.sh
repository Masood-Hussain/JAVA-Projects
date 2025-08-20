#!/bin/bash

# Enhanced Food Delivery System - API Testing Script
# This script tests all the enhanced APIs including authentication, admin features, and more

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# API Base URLs
API_GATEWAY="http://localhost:8080"
USER_SERVICE="http://localhost:8086"
RESTAURANT_SERVICE="http://localhost:8081"
ORDER_SERVICE="http://localhost:8082"
FRONTEND_SERVICE="http://localhost:8085"

# Test variables
JWT_TOKEN=""
ADMIN_TOKEN=""
RESTAURANT_ID=""
MENU_ITEM_ID=""
ORDER_ID=""

echo -e "${BLUE}🧪 Testing Enhanced Food Delivery System APIs${NC}"
echo "=============================================="

# Function to print test results
print_test() {
    local test_name=$1
    local status=$2
    local response=$3
    
    case $status in
        "PASS")
            echo -e "${GREEN}✅ $test_name - PASSED${NC}"
            ;;
        "FAIL")
            echo -e "${RED}❌ $test_name - FAILED${NC}"
            if [ ! -z "$response" ]; then
                echo -e "${RED}   Response: $response${NC}"
            fi
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  $test_name${NC}"
            ;;
    esac
}

# Function to test API endpoint
test_api() {
    local method=$1
    local url=$2
    local headers=$3
    local data=$4
    local expected_status=$5
    
    local response
    local status_code
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" $headers "$url" 2>/dev/null || echo -e "\n000")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST $headers -d "$data" "$url" 2>/dev/null || echo -e "\n000")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT $headers -d "$data" "$url" 2>/dev/null || echo -e "\n000")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE $headers "$url" 2>/dev/null || echo -e "\n000")
    fi
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$status_code" = "$expected_status" ]; then
        return 0
    else
        echo "$response_body"
        return 1
    fi
}

# Test 1: Health Checks
echo -e "\n${YELLOW}Testing Service Health Checks...${NC}"

if test_api "GET" "$USER_SERVICE/actuator/health" "" "" "200"; then
    print_test "User Service Health" "PASS"
else
    print_test "User Service Health" "FAIL" "Service may not be running"
fi

if test_api "GET" "$RESTAURANT_SERVICE/actuator/health" "" "" "200"; then
    print_test "Restaurant Service Health" "PASS"
else
    print_test "Restaurant Service Health" "FAIL" "Service may not be running"
fi

# Test 2: User Registration
echo -e "\n${YELLOW}Testing User Management...${NC}"

register_data='{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User",
    "phone": "1234567890",
    "role": "CUSTOMER"
}'

if test_api "POST" "$USER_SERVICE/api/auth/register" "-H 'Content-Type: application/json'" "$register_data" "200"; then
    print_test "User Registration" "PASS"
else
    print_test "User Registration" "FAIL"
fi

# Test 3: User Login
login_data='{
    "username": "testuser",
    "password": "password123"
}'

response=$(curl -s -X POST -H "Content-Type: application/json" -d "$login_data" "$USER_SERVICE/api/auth/login" 2>/dev/null || echo "{}")
JWT_TOKEN=$(echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ ! -z "$JWT_TOKEN" ]; then
    print_test "User Login" "PASS"
    print_test "JWT Token Received" "INFO"
else
    print_test "User Login" "FAIL"
fi

# Test 4: Admin Login
admin_login_data='{
    "username": "admin",
    "password": "admin123"
}'

# Register admin user first
admin_register_data='{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "fullName": "Admin User",
    "role": "ADMIN"
}'

curl -s -X POST -H "Content-Type: application/json" -d "$admin_register_data" "$USER_SERVICE/api/auth/register" >/dev/null 2>&1

admin_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$admin_login_data" "$USER_SERVICE/api/auth/login" 2>/dev/null || echo "{}")
ADMIN_TOKEN=$(echo "$admin_response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ ! -z "$ADMIN_TOKEN" ]; then
    print_test "Admin Login" "PASS"
else
    print_test "Admin Login" "FAIL"
fi

# Test 5: Restaurant Management
echo -e "\n${YELLOW}Testing Restaurant Management...${NC}"

restaurant_data='{
    "name": "Test Pizza Palace",
    "address": "123 Test Street, Test City",
    "phone": "555-0123",
    "cuisine": "Italian",
    "description": "Best pizza in town!",
    "rating": 4.5,
    "isActive": true
}'

if test_api "POST" "$API_GATEWAY/api/restaurants" "-H 'Content-Type: application/json'" "$restaurant_data" "201"; then
    print_test "Restaurant Creation" "PASS"
    
    # Get restaurant ID from response
    response=$(curl -s -X POST -H "Content-Type: application/json" -d "$restaurant_data" "$API_GATEWAY/api/restaurants" 2>/dev/null)
    RESTAURANT_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    
    if [ ! -z "$RESTAURANT_ID" ]; then
        print_test "Restaurant ID Retrieved: $RESTAURANT_ID" "INFO"
    fi
else
    print_test "Restaurant Creation" "FAIL"
fi

# Test 6: Menu Item Management
echo -e "\n${YELLOW}Testing Menu Item Management...${NC}"

if [ ! -z "$RESTAURANT_ID" ]; then
    menu_item_data='{
        "name": "Margherita Pizza",
        "description": "Classic tomato and mozzarella pizza",
        "price": 299.99,
        "category": "PIZZAS",
        "isAvailable": true
    }'
    
    if test_api "POST" "$API_GATEWAY/api/restaurants/$RESTAURANT_ID/menu" "-H 'Content-Type: application/json'" "$menu_item_data" "201"; then
        print_test "Menu Item Creation" "PASS"
    else
        print_test "Menu Item Creation" "FAIL"
    fi
    
    if test_api "GET" "$API_GATEWAY/api/restaurants/$RESTAURANT_ID/menu" "" "" "200"; then
        print_test "Menu Item Retrieval" "PASS"
    else
        print_test "Menu Item Retrieval" "FAIL"
    fi
else
    print_test "Menu Item Tests" "FAIL" "No restaurant ID available"
fi

# Test 7: Order Management
echo -e "\n${YELLOW}Testing Order Management...${NC}"

order_data='{
    "customerName": "Test Customer",
    "customerPhone": "1234567890",
    "deliveryAddress": "456 Test Avenue",
    "restaurantId": '$RESTAURANT_ID',
    "items": [
        {
            "menuItemId": 1,
            "quantity": 2,
            "price": 299.99
        }
    ],
    "totalAmount": 599.98,
    "status": "PENDING"
}'

if [ ! -z "$RESTAURANT_ID" ]; then
    if test_api "POST" "$API_GATEWAY/api/orders" "-H 'Content-Type: application/json'" "$order_data" "201"; then
        print_test "Order Creation" "PASS"
    else
        print_test "Order Creation" "FAIL"
    fi
else
    print_test "Order Creation" "FAIL" "No restaurant ID available"
fi

# Test 8: Admin Dashboard APIs
echo -e "\n${YELLOW}Testing Admin Dashboard APIs...${NC}"

if test_api "GET" "$FRONTEND_SERVICE/admin/api/dashboard-stats" "" "" "200"; then
    print_test "Dashboard Statistics" "PASS"
else
    print_test "Dashboard Statistics" "FAIL"
fi

if test_api "GET" "$FRONTEND_SERVICE/admin/api/restaurants" "" "" "200"; then
    print_test "Admin Restaurant List" "PASS"
else
    print_test "Admin Restaurant List" "FAIL"
fi

if test_api "GET" "$FRONTEND_SERVICE/admin/api/orders" "" "" "200"; then
    print_test "Admin Order List" "PASS"
else
    print_test "Admin Order List" "FAIL"
fi

# Test 9: Search Functionality
echo -e "\n${YELLOW}Testing Search Functionality...${NC}"

if test_api "GET" "$FRONTEND_SERVICE/api/search?query=pizza" "" "" "200"; then
    print_test "Restaurant Search" "PASS"
else
    print_test "Restaurant Search" "FAIL"
fi

# Test 10: Service Health Monitoring
echo -e "\n${YELLOW}Testing Service Health Monitoring...${NC}"

services=("restaurant" "order" "delivery" "payment")
for service in "${services[@]}"; do
    if test_api "GET" "$FRONTEND_SERVICE/admin/api/health/$service" "" "" "200"; then
        print_test "Health Check - $service" "PASS"
    else
        print_test "Health Check - $service" "FAIL"
    fi
done

# Test 11: Frontend Pages
echo -e "\n${YELLOW}Testing Frontend Pages...${NC}"

if test_api "GET" "$FRONTEND_SERVICE/" "" "" "200"; then
    print_test "Home Page" "PASS"
else
    print_test "Home Page" "FAIL"
fi

if test_api "GET" "$FRONTEND_SERVICE/admin" "" "" "200"; then
    print_test "Admin Dashboard Page" "PASS"
else
    print_test "Admin Dashboard Page" "FAIL"
fi

if test_api "GET" "$FRONTEND_SERVICE/restaurants" "" "" "200"; then
    print_test "Restaurants Page" "PASS"
else
    print_test "Restaurants Page" "FAIL"
fi

# Summary
echo -e "\n${BLUE}🎯 Test Summary${NC}"
echo "==============="
echo -e "${GREEN}✅ Enhanced Food Delivery System API testing completed${NC}"
echo -e "${BLUE}📊 Check the results above for any failed tests${NC}"
echo -e "${YELLOW}💡 If tests fail, ensure all services are running properly${NC}"
echo ""
echo -e "${BLUE}🔗 Quick Access URLs:${NC}"
echo -e "${GREEN}   Frontend: http://localhost:8085${NC}"
echo -e "${GREEN}   Admin Panel: http://localhost:8085/admin${NC}"
echo -e "${GREEN}   API Gateway: http://localhost:8080${NC}"
echo -e "${GREEN}   Service Discovery: http://localhost:8761${NC}"

# Test cleanup
echo -e "\n${YELLOW}🧹 Cleaning up test data...${NC}"
# Add cleanup logic here if needed

echo -e "${GREEN}✨ Testing completed!${NC}"
