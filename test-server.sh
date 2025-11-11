#!/bin/bash
# Simple script to test the MCP server by sending JSON-RPC messages

echo "Testing MCP Hello World Server"
echo "================================"
echo ""

# Test 1: Initialize
echo "Test 1: Initialize connection"
echo '{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {},
    "clientInfo": {
      "name": "test-client",
      "version": "1.0.0"
    }
  }
}'
echo ""
echo "---"
echo ""

# Test 2: List tools
echo "Test 2: List available tools"
echo '{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list",
  "params": {}
}'
echo ""
echo "---"
echo ""

# Test 3: Call hello without name
echo "Test 3: Call 'hello' tool without name"
echo '{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "tools/call",
  "params": {
    "name": "hello",
    "arguments": {}
  }
}'
echo ""
echo "---"
echo ""

# Test 4: Call hello with name
echo "Test 4: Call 'hello' tool with name"
echo '{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "tools/call",
  "params": {
    "name": "hello",
    "arguments": {
      "name": "Alice"
    }
  }
}'
echo ""
echo "---"
echo ""

# Test 5: Call echo
echo "Test 5: Call 'echo' tool"
echo '{
  "jsonrpc": "2.0",
  "id": 5,
  "method": "tools/call",
  "params": {
    "name": "echo",
    "arguments": {
      "message": "Hello from test script!"
    }
  }
}'
echo ""
echo "---"
echo ""

# Test 6: Test sanitization - control characters
echo "Test 6: Test sanitization (control characters)"
echo '{
  "jsonrpc": "2.0",
  "id": 6,
  "method": "tools/call",
  "params": {
    "name": "hello",
    "arguments": {
      "name": "Alice\\nBob\\tCharlie"
    }
  }
}'
echo ""
echo "---"
echo ""

# Test 7: Test sanitization - long input
echo "Test 7: Test sanitization (length limit)"
echo '{
  "jsonrpc": "2.0",
  "id": 7,
  "method": "tools/call",
  "params": {
    "name": "hello",
    "arguments": {
      "name": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    }
  }
}'
echo ""
