#!/bin/bash

# Test Menu Management APIs
BASE_URL="http://localhost:8082/api/restaurants"

echo "=== Testing Restaurant Service Menu Management ==="

# First, create a test restaurant
echo "1. Creating a test restaurant..."
RESTAURANT_RESPONSE=$(curl -s -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Restaurant",
    "address": "123 Test St, Test City",
    "phone": "+1234567890",
    "cuisine": "Italian",
    "description": "A test restaurant for menu management",
    "isActive": true
  }')

echo "Restaurant created: $RESTAURANT_RESPONSE"

# Extract restaurant ID (assuming the response contains an id field)
RESTAURANT_ID=$(echo $RESTAURANT_RESPONSE | jq -r '.id')

if [ "$RESTAURANT_ID" = "null" ] || [ -z "$RESTAURANT_ID" ]; then
    echo "Failed to create restaurant or extract ID. Using ID 1 for testing."
    RESTAURANT_ID=1
fi

echo "Using Restaurant ID: $RESTAURANT_ID"

# Test 1: Add a basic menu item
echo -e "\n2. Adding a basic menu item..."
curl -s -X POST "${BASE_URL}/${RESTAURANT_ID}/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Margherita Pizza",
    "description": "Classic pizza with tomato sauce, mozzarella, and basil",
    "price": 12.99,
    "category": "PIZZAS",
    "isAvailable": true
  }' | jq '.'

# Test 2: Add an advanced menu item with all features
echo -e "\n3. Adding an advanced menu item..."
curl -s -X POST "${BASE_URL}/${RESTAURANT_ID}/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vegan Buddha Bowl",
    "description": "Nutritious bowl with quinoa, roasted vegetables, and tahini dressing",
    "price": 15.99,
    "category": "MAIN_COURSE",
    "isAvailable": true,
    "imageUrl": "https://example.com/buddha-bowl.jpg",
    "ingredients": ["quinoa", "broccoli", "sweet potato", "chickpeas", "tahini"],
    "preparationTime": 20,
    "spiceLevel": "MILD",
    "calories": 450,
    "vegetarian": true,
    "vegan": true,
    "glutenFree": true,
    "halal": true,
    "keto": false,
    "allergens": ["sesame"],
    "featured": true,
    "promotionalOffer": "Healthy Choice - 10% Off"
  }' | jq '.'

# Test 3: Get all menu items for the restaurant
echo -e "\n4. Getting all menu items for restaurant $RESTAURANT_ID..."
curl -s -X GET "${BASE_URL}/${RESTAURANT_ID}/menu" | jq '.'

# Test 4: Search menu items by name
echo -e "\n5. Searching menu items by name (pizza)..."
curl -s -X GET "${BASE_URL}/${RESTAURANT_ID}/menu/search?name=pizza" | jq '.'

# Test 5: Get menu items by category
echo -e "\n6. Getting menu items by category (MAIN_COURSE)..."
curl -s -X GET "${BASE_URL}/${RESTAURANT_ID}/menu/category/MAIN_COURSE" | jq '.'

# Test 6: Get all restaurants
echo -e "\n7. Getting all restaurants..."
curl -s -X GET "${BASE_URL}" | jq '.'

echo -e "\n=== Test completed! ==="
echo "Note: Make sure the restaurant service is running on port 8082"
echo "Start the services with: ./start-services.sh"