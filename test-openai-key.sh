#!/bin/bash
# Test OpenAI API key and connection

echo "Testing OpenAI API Key"
echo "======================"
echo ""

if [ -z "$OPENAI_API_KEY" ]; then
    echo "❌ OPENAI_API_KEY is not set"
    echo ""
    echo "Please set it:"
    echo "  export OPENAI_API_KEY=sk-your-key-here"
    exit 1
fi

echo "✓ API key is set (length: ${#OPENAI_API_KEY} chars)"
echo ""

# Check if key starts with sk-
if [[ $OPENAI_API_KEY == sk-* ]]; then
    echo "✓ Key format looks correct (starts with 'sk-')"
else
    echo "⚠️  Key doesn't start with 'sk-' - might be invalid"
fi
echo ""

# Test with curl
echo "Testing API connection with curl..."
echo ""

response=$(curl -s -w "\nHTTP_CODE:%{http_code}" https://api.openai.com/v1/embeddings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -d '{
    "input": "test",
    "model": "text-embedding-3-small"
  }')

http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
body=$(echo "$response" | sed '/HTTP_CODE:/d')

echo "HTTP Status Code: $http_code"
echo ""

if [ "$http_code" = "200" ]; then
    echo "✅ Success! API key is working correctly"
    echo ""
    echo "Response (first 200 chars):"
    echo "$body" | head -c 200
    echo "..."
elif [ "$http_code" = "401" ]; then
    echo "❌ Authentication failed (401 Unauthorized)"
    echo ""
    echo "Possible issues:"
    echo "  - API key is invalid or expired"
    echo "  - API key doesn't have correct permissions"
    echo ""
    echo "Error response:"
    echo "$body"
elif [ "$http_code" = "429" ]; then
    echo "⚠️  Rate limit exceeded (429)"
    echo ""
    echo "Your API key is valid but you've hit rate limits"
    echo ""
    echo "Error response:"
    echo "$body"
else
    echo "❌ API call failed with status $http_code"
    echo ""
    echo "Error response:"
    echo "$body"
fi

echo ""
echo "======================"
