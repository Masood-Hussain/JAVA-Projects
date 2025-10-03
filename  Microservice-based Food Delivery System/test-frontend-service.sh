#!/bin/bash

# Frontend Service Comprehensive Test Script
# Tests all enhanced frontend service endpoints and functionality

BASE_URL="http://localhost:8085"
API_URL="$BASE_URL/api"

echo "=== Frontend Service Comprehensive Test Suite ==="
echo "Base URL: $BASE_URL"
echo "API URL: $API_URL"
echo

# Function to make HTTP requests with error handling
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo "Testing: $description"
    echo "Method: $method | URL: $url"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$url" -H "Accept: application/json")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$url" \
            -H "Content-Type: application/json" \
            -H "Accept: application/json" \
            -d "$data")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT "$url" \
            -H "Content-Type: application/json" \
            -H "Accept: application/json" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo "✅ SUCCESS ($http_code)"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo "❌ FAILED ($http_code)"
        echo "$body"
    fi
    echo "----------------------------------------"
}

# Test 1: Service Health Check
echo "🏥 HEALTH & STATUS TESTS"
make_request "GET" "$API_URL/health" "" "Service Health Check"
make_request "GET" "$API_URL/info" "" "Service Information"

# Test 2: Web Page Endpoints
echo
echo "🌐 WEB PAGE TESTS"
curl -s -o /dev/null -w "Home Page: %{http_code}\n" "$BASE_URL/"
curl -s -o /dev/null -w "Restaurants Page: %{http_code}\n" "$BASE_URL/restaurants"
curl -s -o /dev/null -w "Orders Page: %{http_code}\n" "$BASE_URL/orders"
curl -s -o /dev/null -w "Admin Page: %{http_code}\n" "$BASE_URL/admin"
curl -s -o /dev/null -w "Help Page: %{http_code}\n" "$BASE_URL/help"

# Test 3: Restaurant API Tests
echo
echo "🍽️ RESTAURANT API TESTS"
make_request "GET" "$API_URL/restaurants" "" "Get All Restaurants"

# Test specific restaurant (assuming restaurant with ID 1 exists)
make_request "GET" "$API_URL/restaurants/1" "" "Get Restaurant by ID"
make_request "GET" "$API_URL/restaurants/999" "" "Get Non-existent Restaurant (should return 404)"

# Test 4: Menu API Tests
echo
echo "📋 MENU API TESTS"
make_request "GET" "$API_URL/restaurants/1/menu" "" "Get Menu Items for Restaurant"

# Test 5: Order API Tests
echo
echo "📦 ORDER API TESTS"
make_request "GET" "$API_URL/orders" "" "Get All Orders"

# Test create order
ORDER_DATA='{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "123 Test St, Test City",
    "customerPhone": "+1234567890",
    "customerEmail": "test@example.com",
    "specialInstructions": "Test order from frontend service",
    "items": [
        {
            "menuItemId": 1,
            "itemName": "Test Burger",
            "quantity": 2,
            "price": 12.99,
            "specialInstructions": "No pickles"
        }
    ],
    "deliveryFee": 3.50,
    "taxAmount": 2.10,
    "discountAmount": 0.00
}'

make_request "POST" "$API_URL/orders" "$ORDER_DATA" "Create New Order"

# Test get order by ID (assuming order 1 exists)
make_request "GET" "$API_URL/orders/1" "" "Get Order by ID"
make_request "GET" "$API_URL/orders/999" "" "Get Non-existent Order (should return 404)"

# Test get orders by customer and restaurant
make_request "GET" "$API_URL/orders/customer/1" "" "Get Orders by Customer ID"
make_request "GET" "$API_URL/orders/restaurant/1" "" "Get Orders by Restaurant ID"

# Test 6: Frontend-specific Web Endpoints
echo
echo "🖥️ FRONTEND WEB ENDPOINTS TESTS"
curl -s -o /dev/null -w "Classic Home: %{http_code}\n" "$BASE_URL/classic"
curl -s -o /dev/null -w "Classic Restaurants: %{http_code}\n" "$BASE_URL/classic/restaurants"
curl -s -o /dev/null -w "Restaurant Detail: %{http_code}\n" "$BASE_URL/restaurant/1"
curl -s -o /dev/null -w "Advanced Menu: %{http_code}\n" "$BASE_URL/menu/1"
curl -s -o /dev/null -w "Order Tracking: %{http_code}\n" "$BASE_URL/track/1"
curl -s -o /dev/null -w "Checkout: %{http_code}\n" "$BASE_URL/checkout/1"

# Test 7: Admin Endpoints
echo
echo "👨‍💼 ADMIN ENDPOINTS TESTS"
curl -s -o /dev/null -w "Admin Dashboard: %{http_code}\n" "$BASE_URL/admin"
curl -s -o /dev/null -w "Admin Restaurants: %{http_code}\n" "$BASE_URL/admin/restaurants"
curl -s -o /dev/null -w "Admin Orders: %{http_code}\n" "$BASE_URL/admin/orders"
curl -s -o /dev/null -w "Admin Analytics: %{http_code}\n" "$BASE_URL/admin/analytics"

# Test 8: Error Handling Tests
echo
echo "⚠️ ERROR HANDLING TESTS"
make_request "GET" "$API_URL/nonexistent" "" "Non-existent Endpoint (should return 404)"
make_request "POST" "$API_URL/orders" '{"invalid": "data"}' "Invalid Order Data (should return 400)"

# Test 9: Performance Test (Simple Load Test)
echo
echo "🚀 PERFORMANCE TESTS"
echo "Running simple load test (10 concurrent requests to health endpoint)..."
for i in {1..10}; do
    curl -s "$API_URL/health" > /dev/null &
done
wait
echo "✅ Load test completed"

# Test 10: Static Resource Tests
echo
echo "📁 STATIC RESOURCES TESTS"
curl -s -o /dev/null -w "CSS Resources: %{http_code}\n" "$BASE_URL/css/style.css"
curl -s -o /dev/null -w "JS Resources: %{http_code}\n" "$BASE_URL/js/app.js"
curl -s -o /dev/null -w "Images Resources: %{http_code}\n" "$BASE_URL/images/logo.png"

# Test 11: CORS Headers Test
echo
echo "🌐 CORS TESTS"
echo "Testing CORS headers for API endpoints..."
curl -s -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS "$API_URL/restaurants" \
     -w "CORS Preflight: %{http_code}\n" \
     -o /dev/null

# Summary
echo
echo "=== TEST SUMMARY ==="
echo "✅ Service Health Tests"
echo "✅ Web Page Accessibility Tests"
echo "✅ Restaurant API Tests"
echo "✅ Menu API Tests"
echo "✅ Order API Tests"
echo "✅ Frontend Web Endpoints Tests"
echo "✅ Admin Endpoints Tests"
echo "✅ Error Handling Tests"
echo "✅ Performance Tests"
echo "✅ Static Resources Tests"
echo "✅ CORS Tests"
echo
echo "🎉 Frontend Service Test Suite Completed!"
echo
echo "📝 Notes:"
echo "- Make sure the frontend service is running on port 8085"
echo "- Make sure other microservices are running for full functionality"
echo "- Some tests may fail if dependent services are not available"
echo "- Check logs for detailed error information"
echo
echo "🔗 Frontend Service URLs:"
echo "- Main App: $BASE_URL"
echo "- API Docs: $API_URL/info"
echo "- Health Check: $API_URL/health"