#!/bin/bash
# Interactive test script for MCP server

echo "MCP Server Interactive Tester"
echo "=============================="
echo ""
echo "This script will help you test the MCP server interactively."
echo "The server should be running in another terminal with: ./gradlew run"
echo ""

# Function to send a message and wait for response
send_message() {
    echo "$1"
    echo ""
}

# Check if server is provided as argument
if [ -z "$1" ]; then
    echo "Usage: $0 <path-to-jar-file>"
    echo "Example: $0 build/libs/mcp-server-demo-0.0.1-all.jar"
    exit 1
fi

SERVER_JAR="$1"

if [ ! -f "$SERVER_JAR" ]; then
    echo "Error: Server JAR not found at $SERVER_JAR"
    echo "Please build the project first: ./gradlew build"
    exit 1
fi

echo "Starting MCP server..."
echo ""

# Create a temporary file for communication
TEMP_INPUT=$(mktemp)
TEMP_OUTPUT=$(mktemp)

# Cleanup on exit
trap "rm -f $TEMP_INPUT $TEMP_OUTPUT; kill $SERVER_PID 2>/dev/null" EXIT

# Start server in background
java -jar "$SERVER_JAR" < "$TEMP_INPUT" > "$TEMP_OUTPUT" 2>&1 &
SERVER_PID=$!

sleep 2

# Check if server is running
if ! kill -0 $SERVER_PID 2>/dev/null; then
    echo "Error: Server failed to start"
    cat "$TEMP_OUTPUT"
    exit 1
fi

echo "Server started (PID: $SERVER_PID)"
echo ""

# Interactive menu
while true; do
    echo ""
    echo "Choose a test:"
    echo "1. Initialize connection"
    echo "2. List available tools"
    echo "3. Call 'hello' tool (no name)"
    echo "4. Call 'hello' tool (with name)"
    echo "5. Call 'echo' tool"
    echo "6. Test sanitization (control characters)"
    echo "7. Test sanitization (long input)"
    echo "8. Custom JSON-RPC message"
    echo "9. Exit"
    echo ""
    read -p "Enter choice (1-9): " choice

    case $choice in
        1)
            send_message '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0.0"}}}'
            ;;
        2)
            send_message '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}'
            ;;
        3)
            send_message '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"hello","arguments":{}}}'
            ;;
        4)
            read -p "Enter name: " name
            send_message "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"tools/call\",\"params\":{\"name\":\"hello\",\"arguments\":{\"name\":\"$name\"}}}"
            ;;
        5)
            read -p "Enter message to echo: " message
            send_message "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"tools/call\",\"params\":{\"name\":\"echo\",\"arguments\":{\"message\":\"$message\"}}}"
            ;;
        6)
            send_message '{"jsonrpc":"2.0","id":6,"method":"tools/call","params":{"name":"hello","arguments":{"name":"Alice\\nBob\\tCharlie"}}}'
            ;;
        7)
            send_message '{"jsonrpc":"2.0","id":7,"method":"tools/call","params":{"name":"hello","arguments":{"name":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}}}'
            ;;
        8)
            echo "Enter JSON-RPC message (press Ctrl+D when done):"
            read -d '' custom_msg
            send_message "$custom_msg"
            ;;
        9)
            echo "Exiting..."
            break
            ;;
        *)
            echo "Invalid choice"
            ;;
    esac
done
