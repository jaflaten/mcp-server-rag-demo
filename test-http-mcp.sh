#!/bin/bash

# HTTP MCP Server Test Script
# Tests all endpoints of the HTTP MCP server

SERVER="http://localhost:8080"
BOLD="\033[1m"
GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[0;33m"
NC="\033[0m" # No Color

echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BOLD}           HTTP MCP Server Test Suite${NC}"
echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
echo

# Test 1: Health Check
echo -e "${BLUE}Test 1: Health Check${NC}"
echo -e "${YELLOW}GET /health${NC}"
echo "-----------------------------------"
curl -s "${SERVER}/health"
echo -e "\n"

# Test 2: List Tools
echo -e "${BLUE}Test 2: List Available Tools${NC}"
echo -e "${YELLOW}GET /mcp/tools${NC}"
echo "-----------------------------------"
curl -s -X GET "${SERVER}/mcp/tools" | python3 -m json.tool
echo

# Test 3: Hello Tool (no name)
echo -e "${BLUE}Test 3: Hello Tool (no name)${NC}"
echo -e "${YELLOW}POST /mcp/call${NC}"
echo '{"tool":"hello","arguments":{}}' | python3 -m json.tool
echo "-----------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"hello","arguments":{}}' \
  "${SERVER}/mcp/call" | python3 -m json.tool
echo

# Test 4: Hello Tool (with name)
echo -e "${BLUE}Test 4: Hello Tool (with name)${NC}"
echo -e "${YELLOW}POST /mcp/call${NC}"
echo '{"tool":"hello","arguments":{"name":"Trainer"}}' | python3 -m json.tool
echo "-----------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"hello","arguments":{"name":"Trainer"}}' \
  "${SERVER}/mcp/call" | python3 -m json.tool
echo

# Test 5: RAG Query - Pokemon Type
echo -e "${BLUE}Test 5: RAG Query - Pokemon Type${NC}"
echo -e "${YELLOW}POST /mcp/call${NC}"
echo '{"tool":"rag_query","arguments":{"query":"What type is Lapras?","topK":3}}' | python3 -m json.tool
echo "-----------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"rag_query","arguments":{"query":"What type is Lapras?","topK":3}}' \
  "${SERVER}/mcp/call" | python3 -c "import sys, json; data = json.load(sys.stdin); print(data['content'][0]['text'][:500] + '...')"
echo -e "\n"

# Test 6: RAG Query - Evolution
echo -e "${BLUE}Test 6: RAG Query - Evolution${NC}"
echo -e "${YELLOW}POST /mcp/call${NC}"
echo '{"tool":"rag_query","arguments":{"query":"Which Pokemon can Mega Evolve?","topK":3}}' | python3 -m json.tool
echo "-----------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"rag_query","arguments":{"query":"Which Pokemon can Mega Evolve?","topK":3}}' \
  "${SERVER}/mcp/call" | python3 -c "import sys, json; data = json.load(sys.stdin); print(data['content'][0]['text'][:500] + '...')"
echo -e "\n"

# Test 7: RAG Query - Ghost Type
echo -e "${BLUE}Test 7: RAG Query - Ghost Type${NC}"
echo -e "${YELLOW}POST /mcp/call${NC}"
echo '{"tool":"rag_query","arguments":{"query":"What ghost type Pokemon do we have?","topK":3}}' | python3 -m json.tool
echo "-----------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"rag_query","arguments":{"query":"What ghost type Pokemon do we have?","topK":3}}' \
  "${SERVER}/mcp/call" | python3 -c "import sys, json; data = json.load(sys.stdin); print(data['content'][0]['text'][:500] + '...')"
echo -e "\n"

# Test 8: List Resources
echo -e "${BLUE}Test 8: List Resources${NC}"
echo -e "${YELLOW}GET /mcp/resources${NC}"
echo "-----------------------------------"
curl -s -X GET "${SERVER}/mcp/resources" | python3 -m json.tool
echo

# Test 9: Error Handling - Unknown Tool
echo -e "${BLUE}Test 9: Error Handling - Unknown Tool${NC}"
echo -e "${YELLOW}POST /mcp/call${NC}"
echo '{"tool":"nonexistent","arguments":{}}' | python3 -m json.tool
echo "-----------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"nonexistent","arguments":{}}' \
  "${SERVER}/mcp/call" | python3 -m json.tool
echo

# Test 10: Error Handling - Missing Required Argument
echo -e "${BLUE}Test 10: Error Handling - Missing Required Argument${NC}"
echo -e "${YELLOW}POST /mcp/call${NC}"
echo '{"tool":"rag_query","arguments":{}}' | python3 -m json.tool
echo "-----------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"rag_query","arguments":{}}' \
  "${SERVER}/mcp/call" | python3 -m json.tool
echo

echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}All tests completed!${NC}"
echo -e "${BOLD}═══════════════════════════════════════════════════════════════${NC}"
