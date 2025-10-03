#!/bin/bash

# Order Service API Test Script
# Tests all the enhanced order service endpoints

BASE_URL="http://localhost:8082/api/orders"

echo "=== Order Service API Tests ==="
echo

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s -X GET "$BASE_URL/health" || echo "Service not running"
echo
echo

# Test 2: Create Order
echo "2. Creating a new order..."
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "123 Main St, City, State 12345",
    "specialInstructions": "Ring doorbell twice",
    "customerPhone": "+1234567890",
    "customerEmail": "customer@example.com",
    "deliveryFee": 3.50,
    "taxAmount": 2.40,
    "discountAmount": 0.00,
    "estimatedDeliveryTime": "2024-01-01T12:30:00",
    "items": [
      {
        "menuItemId": 101,
        "itemName": "Chicken Burger",
        "quantity": 2,
        "price": 12.99,
        "specialInstructions": "No onions"
      },
      {
        "menuItemId": 102,
        "itemName": "French Fries",
        "quantity": 1,
        "price": 4.99,
        "specialInstructions": "Extra crispy"
      }
    ]
  }')

echo "$ORDER_RESPONSE" | jq '.' 2>/dev/null || echo "$ORDER_RESPONSE"
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.id' 2>/dev/null)
echo "Created Order ID: $ORDER_ID"
echo

# Test 3: Get Order by ID
echo "3. Getting order by ID..."
curl -s -X GET "$BASE_URL/$ORDER_ID" | jq '.' 2>/dev/null || echo "Failed to get order"
echo
echo

# Test 4: Get All Orders
echo "4. Getting all orders..."
curl -s -X GET "$BASE_URL" | jq '.' 2>/dev/null || echo "Failed to get all orders"
echo
echo

# Test 5: Update Order Status
echo "5. Updating order status to CONFIRMED..."
curl -s -X PUT "$BASE_URL/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED",
    "estimatedDeliveryTime": "2024-01-01T13:00:00"
  }' | jq '.' 2>/dev/null || echo "Failed to update status"
echo
echo

# Test 6: Update Order Status to PREPARING
echo "6. Updating order status to PREPARING..."
curl -s -X PUT "$BASE_URL/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PREPARING"
  }' | jq '.' 2>/dev/null || echo "Failed to update status"
echo
echo

# Test 7: Get Orders by Customer ID
echo "7. Getting orders by customer ID (1)..."
curl -s -X GET "$BASE_URL/customer/1" | jq '.' 2>/dev/null || echo "Failed to get customer orders"
echo
echo

# Test 8: Get Orders by Restaurant ID
echo "8. Getting orders by restaurant ID (1)..."
curl -s -X GET "$BASE_URL/restaurant/1" | jq '.' 2>/dev/null || echo "Failed to get restaurant orders"
echo
echo

# Test 9: Get Orders by Status
echo "9. Getting orders by status (PREPARING)..."
curl -s -X GET "$BASE_URL/status/PREPARING" | jq '.' 2>/dev/null || echo "Failed to get orders by status"
echo
echo

# Test 10: Cancel Order (create another order first)
echo "10. Creating another order for cancellation test..."
CANCEL_ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 2,
    "restaurantId": 1,
    "deliveryAddress": "456 Oak Ave, City, State 12345",
    "specialInstructions": "Leave at door",
    "customerPhone": "+1987654321",
    "customerEmail": "customer2@example.com",
    "deliveryFee": 2.50,
    "taxAmount": 1.80,
    "discountAmount": 5.00,
    "estimatedDeliveryTime": "2024-01-01T14:00:00",
    "items": [
      {
        "menuItemId": 103,
        "itemName": "Pizza Margherita",
        "quantity": 1,
        "price": 18.99,
        "specialInstructions": "Extra cheese"
      }
    ]
  }')

CANCEL_ORDER_ID=$(echo "$CANCEL_ORDER_RESPONSE" | jq -r '.id' 2>/dev/null)
echo "Created Order ID for cancellation: $CANCEL_ORDER_ID"

echo "Cancelling order..."
curl -s -X PUT "$BASE_URL/$CANCEL_ORDER_ID/cancel?reason=Customer+requested+cancellation" | jq '.' 2>/dev/null || echo "Failed to cancel order"
echo
echo

# Test 11: Error Test - Try to update non-existent order
echo "11. Testing error handling - update non-existent order..."
curl -s -X PUT "$BASE_URL/99999/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }' || echo "Expected error for non-existent order"
echo
echo

# Test 12: Error Test - Invalid status transition
echo "12. Testing error handling - invalid status transition..."
curl -s -X PUT "$BASE_URL/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DELIVERED"
  }' || echo "Expected error for invalid status transition"
echo
echo

echo "=== Order Service API Tests Complete ==="