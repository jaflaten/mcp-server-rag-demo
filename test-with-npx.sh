#!/bin/bash
# Test MCP server using npx @modelcontextprotocol/inspector

echo "Testing MCP Server with MCP Inspector"
echo "======================================"
echo ""

# Check if the JAR exists
JAR_FILE="build/libs/mcp-server-demo-0.0.1-all.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Building the project..."
    ./gradlew build
    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
fi

echo "JAR file found: $JAR_FILE"
echo ""
echo "Starting MCP Inspector..."
echo ""
echo "The inspector will open in your browser."
echo "Connect to your server using:"
echo "  Command: java"
echo "  Args: -jar $PWD/$JAR_FILE"
echo ""
echo "Or if you have the server already running, use STDIO connection."
echo ""

# Try to use npx to run the inspector
if command -v npx &> /dev/null; then
    echo "Launching MCP Inspector with npx..."
    npx @modelcontextprotocol/inspector java -jar "$PWD/$JAR_FILE"
else
    echo "Error: npx not found. Please install Node.js first."
    echo ""
    echo "To install Node.js:"
    echo "  - macOS: brew install node"
    echo "  - Or download from: https://nodejs.org/"
    echo ""
    echo "After installing Node.js, run this script again."
    exit 1
fi
