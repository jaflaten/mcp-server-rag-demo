# How to Test Your Running MCP Server

Your server is running! Here are several ways to test it:

## 🚀 Method 1: Using NPX + MCP Inspector (EASIEST!)

This is the **recommended** way - similar to `mcp dev` for Python:

```bash
npx @modelcontextprotocol/inspector java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

**What happens:**
- Opens a web UI in your browser automatically
- Starts and connects to your server
- Provides a visual interface to test all tools
- Shows request/response JSON in real-time

**Or use the helper script:**
```bash
./test-with-npx.sh
```

## 🐍 Method 2: Using Python Test Client (Python-Style Testing)

Similar to how you'd test Python MCP servers:

```bash
python3 test-client.py build/libs/mcp-server-demo-0.0.1-all.jar
```

**What it does:**
- Runs 7 automated tests
- Shows colored output with emojis
- Tests both tools and sanitization
- Great for CI/CD pipelines

**Example output:**
```
📡 Test 1: Initialize connection
✅ Server initialized: hello-world-server
   Version: 1.0.0

🔧 Test 2: List available tools
✅ Found 2 tools:
   - hello: Returns a friendly greeting message...
   - echo: Echoes back the message you provide...

👋 Test 3: Call 'hello' tool (no name)
✅ Response: Hello, World! Welcome to the MCP Hello World server!
```

## 💻 Method 3: Manual JSON-RPC Testing

If your server is already running in another terminal:

### Step 1: Send JSON messages via stdin

In a new terminal:
```bash
# Initialize
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0.0"}}}' 

# List tools
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}'

# Call hello tool
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"hello","arguments":{}}}'
```

### Step 2: Check the output

Your running server should display responses in its terminal.

## 🎯 Method 4: Interactive Shell Script

For manual testing with a menu:

```bash
./interactive-test.sh build/libs/mcp-server-demo-0.0.1-all.jar
```

**Features:**
- Interactive menu
- Pre-built test cases
- Custom JSON-RPC input option
- Real-time responses

## 📋 Quick Test Commands

### Test the "hello" tool without a name:
```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"hello","arguments":{}}}' | java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

### Test the "hello" tool with a name:
```bash
echo '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"hello","arguments":{"name":"Alice"}}}' | java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

### Test the "echo" tool:
```bash
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"echo","arguments":{"message":"Test message"}}}' | java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

### Test input sanitization:
```bash
echo '{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"hello","arguments":{"name":"Alice\\nBob"}}}' | java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

## 🔍 What to Look For

When testing, verify:

1. **Tool Discovery**: Both "hello" and "echo" tools are listed
2. **Parameter Handling**: Optional name parameter works correctly
3. **Sanitization**: Control characters are removed
4. **Length Limits**: Long inputs are truncated
5. **Error Handling**: Invalid inputs return error messages

## 🎨 Using Claude Desktop

To use with Claude Desktop (or other MCP clients):

1. Build your server:
   ```bash
   ./gradlew build
   ```

2. Edit your Claude config (`~/Library/Application Support/Claude/claude_desktop_config.json`):
   ```json
   {
     "mcpServers": {
       "hello-world": {
         "command": "java",
         "args": [
           "-jar",
           "/absolute/path/to/mcp-server-demo/build/libs/mcp-server-demo-0.0.1-all.jar"
         ]
       }
     }
   }
   ```

3. Restart Claude Desktop

4. Test in Claude:
   ```
   "Can you greet me using the hello tool?"
   "Echo back the message: Hello from Claude!"
   ```

## 🐛 Troubleshooting

### Server doesn't respond?
- Make sure server is built: `./gradlew build`
- Check server is running without errors
- Verify JSON format (no trailing commas, proper escaping)

### NPX not found?
- Install Node.js: `brew install node` (macOS)
- Or download from: https://nodejs.org/

### Python script fails?
- Requires Python 3.6+
- Check: `python3 --version`

### Can't see responses?
- Run server with: `./gradlew run` to see stdout
- Or check the server's log output

## 📝 Example Test Session

Here's a complete test session:

```bash
# Terminal 1: Start server
./gradlew run

# Terminal 2: Test with Python client
python3 test-client.py build/libs/mcp-server-demo-0.0.1-all.jar

# Or use NPX inspector
npx @modelcontextprotocol/inspector java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

## 🎓 Learning More

- Check [TESTING.md](TESTING.md) for detailed JSON-RPC examples
- See [SANITIZATION_EXAMPLES.md](SANITIZATION_EXAMPLES.md) for security tests
- Read [SECURITY.md](SECURITY.md) for security considerations

---

**Happy Testing! 🎉**

If you have questions or issues, check the documentation or examine the test scripts for reference implementations.
