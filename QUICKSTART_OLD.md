# Quick Start Guide

Get your MCP Hello World server up and running in just a few steps!

## Installation

1. Clone or navigate to the project directory:
```bash
cd mcp-server-demo
```

2. Build the project:
```bash
./gradlew build
```

## Running the Server

### Option 1: Using Gradle
```bash
./gradlew run
```

### Option 2: Using the JAR directly
```bash
java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

## What's Inside?

Your MCP server includes:

### 🔧 Tools (2)
- **hello**: A friendly greeting tool that can personalize messages
- **echo**: A simple echo tool that repeats your message back

### 📚 Resources (2)
- **Server Info** (`hello://server/info`): Static information about the server
- **Personalized Greeting** (`hello://greetings/{name}`): Dynamic greetings with timestamps

## Next Steps

1. **Test the Server**: See [TESTING.md](TESTING.md) for detailed testing instructions

2. **Add More Tools**: Edit `src/main/kotlin/HelloWorldMcpServer.kt` to add your own tools

3. **Connect to LLM Clients**: Integrate with Claude Desktop or other MCP-compatible clients

## Example Tool Usage

### Using "hello" tool:
```json
{
  "name": "hello",
  "arguments": {
    "name": "World"
  }
}
```
**Response**: "Hello, World! Welcome to the MCP Hello World server!"

### Using "echo" tool:
```json
{
  "name": "echo",
  "arguments": {
    "message": "Testing 1-2-3"
  }
}
```
**Response**: "Echo: Testing 1-2-3"

## Architecture

```
┌──────────────────────┐
│   LLM Client         │
│  (e.g., Claude)      │
└──────────┬───────────┘
           │
           │ JSON-RPC over STDIO
           │
┌──────────▼───────────┐
│   MCP Server         │
│  (HelloWorldServer)  │
├──────────────────────┤
│  Tools:              │
│  • hello             │
│  • echo              │
└──────────────────────┘
```

## Troubleshooting

**Build fails?**
- Ensure you have Java 17 or higher: `java -version`
- Run `./gradlew clean build`

**Server doesn't start?**
- Check that port/stdio isn't already in use
- Verify the JAR was built: `ls -la build/libs/`

**Tools not working?**
- Ensure JSON-RPC messages are properly formatted
- Check server logs for error messages

## Resources

- [Full README](README.md) - Complete documentation
- [Testing Guide](TESTING.md) - How to test your server
- [Resources Guide](RESOURCES.md) - Understanding MCP resources
- [MCP Documentation](https://modelcontextprotocol.io) - Official MCP docs
- [Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk) - SDK repository

Happy coding! 🚀
