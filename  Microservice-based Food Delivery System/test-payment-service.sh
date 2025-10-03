#!/bin/bash

# Test Payment Service APIs
BASE_URL="http://localhost:8083/api/payments"

echo "=== Testing Payment Service ==="

# Test 1: Health check
echo "1. Health check..."
curl -s -X GET "${BASE_URL}/health" | jq '.'

# Test 2: Process a basic payment (legacy endpoint)
echo -e "\n2. Processing basic payment..."
BASIC_PAYMENT=$(curl -s -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1001,
    "amount": 29.99,
    "paymentMethod": "credit_card"
  }')

echo $BASIC_PAYMENT | jq '.'

# Extract payment ID
PAYMENT_ID=$(echo $BASIC_PAYMENT | jq -r '.id')

# Test 3: Get all payments
echo -e "\n3. Getting all payments..."
curl -s -X GET "${BASE_URL}" | jq '.'

# Test 4: Get payment by order ID
echo -e "\n4. Getting payment by order ID..."
curl -s -X GET "${BASE_URL}/order/1001" | jq '.'

# Test 5: Update payment status
echo -e "\n5. Updating payment status..."
curl -s -X PUT "${BASE_URL}/${PAYMENT_ID}/status?status=COMPLETED" | jq '.'

# Test 6: Get payments by status
echo -e "\n6. Getting payments by status..."
curl -s -X GET "${BASE_URL}/status/COMPLETED" | jq '.'

echo -e "\n=== Testing Enhanced Endpoints ==="

# Test 7: Process enhanced payment
echo -e "\n7. Processing enhanced payment..."
curl -s -X POST "${BASE_URL}/process" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1002,
    "amount": 45.50,
    "paymentMethod": "paypal",
    "currency": "USD",
    "description": "Premium meal order",
    "customerEmail": "customer@example.com",
    "customerName": "John Doe",
    "customerPhone": "+1234567890",
    "billingAddress": "123 Main St",
    "billingCity": "New York",
    "billingState": "NY",
    "billingZip": "10001",
    "billingCountry": "USA",
    "metadata": {
      "source": "mobile_app",
      "campaign": "summer_special"
    }
  }' | jq '.'

# Test 8: Get enhanced payments list
echo -e "\n8. Getting enhanced payments list..."
curl -s -X GET "${BASE_URL}/enhanced" | jq '.'

# Test 9: Test different payment methods
echo -e "\n9. Testing different payment methods..."

echo "  - Testing Apple Pay..."
curl -s -X POST "${BASE_URL}/process" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1003,
    "amount": 15.75,
    "paymentMethod": "apple_pay",
    "customerEmail": "apple@example.com"
  }' | jq '.status'

echo "  - Testing Google Pay..."
curl -s -X POST "${BASE_URL}/process" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1004,
    "amount": 22.00,
    "paymentMethod": "google_pay",
    "customerEmail": "google@example.com"
  }' | jq '.status'

echo "  - Testing Bank Transfer..."
curl -s -X POST "${BASE_URL}/process" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1005,
    "amount": 100.00,
    "paymentMethod": "bank_transfer",
    "customerEmail": "bank@example.com"
  }' | jq '.status'

# Test 10: Test invalid payment (should fail)
echo -e "\n10. Testing invalid payment (negative amount)..."
curl -s -X POST "${BASE_URL}/process" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1006,
    "amount": -10.00,
    "paymentMethod": "credit_card"
  }' | jq '.'

# Test 11: Test unsupported payment method
echo -e "\n11. Testing unsupported payment method..."
curl -s -X POST "${BASE_URL}/process" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1007,
    "amount": 25.00,
    "paymentMethod": "cryptocurrency"
  }' | jq '.'

echo -e "\n=== Payment Service Test Completed! ==="
echo "Note: Make sure the payment service is running on port 8083"
echo "Start the services with: ./start-services.sh"