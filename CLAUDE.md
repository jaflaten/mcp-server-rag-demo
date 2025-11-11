# Using Your MCP Server with Claude Desktop

## ✅ Configuration Complete!

Your Claude Desktop is now configured to use your Hello World MCP server!

## 📍 Configuration File Location

The config file has been created at:
```
~/Library/Application Support/Claude/claude_desktop_config.json
```

## 📄 Current Configuration

```json
{
  "mcpServers": {
    "hello-world": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/Jorn-Are.Klubben.Flaten/dev/solo/mcp-server-demo/build/libs/mcp-server-demo-all.jar"
      ]
    }
  }
}
```

## 🚀 Getting Started

### Step 1: Restart Claude Desktop

**Important:** You must completely restart Claude Desktop for the changes to take effect.

1. Quit Claude Desktop (Cmd+Q)
2. Reopen Claude Desktop

### Step 2: Verify Connection

After restarting, Claude should automatically connect to your MCP server. You can verify by:

1. Look for a small 🔌 or MCP indicator in Claude's interface
2. Try asking Claude to list available tools
3. Ask Claude to use one of your tools

### Step 3: Test the Tools

Try these prompts with Claude:

#### Test Tool: hello
```
Can you use the hello tool to greet me?
```

```
Use the hello tool to greet someone named Alice
```

#### Test Tool: echo
```
Can you echo back this message: "Testing MCP integration!"
```

#### Test Resources
```
Can you read the server info resource from the hello-world server?
```

```
Read the personalized greeting resource for Bob from hello://greetings/Bob
```

## 🎯 Example Conversations

### Example 1: Simple Greeting
**You:** "Use the hello tool to greet me"

**Claude:** "I'll use the hello tool to greet you!"
*[Claude calls the tool]*
"Hello, World! Welcome to the MCP Hello World server!"

### Example 2: Personalized Greeting
**You:** "Use the hello tool with my name, John"

**Claude:** "I'll greet you personally!"
*[Claude calls hello tool with name="John"]*
"Hello, John! Welcome to the MCP Hello World server!"

### Example 3: Echo
**You:** "Echo back: MCP is awesome!"

**Claude:** "I'll echo that for you!"
*[Claude calls echo tool]*
"Echo: MCP is awesome!"

### Example 4: Reading Resources
**You:** "What information is available in the server info resource?"

**Claude:** "Let me read that resource for you."
*[Claude reads hello://server/info]*
"The server is running version 1.0.0 and provides tools for hello and echo..."

## 🔧 Available Capabilities

Your Claude can now access:

### Tools (2)
- ✅ **hello** - Friendly greetings with optional name personalization
- ✅ **echo** - Echo messages back with sanitization

### Resources (2)
- ✅ **hello://server/info** - Static server information
- ✅ **hello://greetings/{name}** - Dynamic personalized greetings

## 🐛 Troubleshooting

### Claude doesn't see the MCP server

**Solution 1:** Rebuild the JAR
```bash
cd ~/dev/solo/mcp-server-demo
./gradlew clean build
```

**Solution 2:** Restart Claude Desktop
- Make sure you fully quit (Cmd+Q), not just close the window
- Reopen Claude Desktop

**Solution 3:** Check the logs
Claude Desktop logs are at:
```
~/Library/Logs/Claude/
```

Look for error messages related to MCP servers.

### "Command not found" error

**Check Java is installed:**
```bash
java -version
```

If not installed:
```bash
brew install openjdk@17
```

### JAR file not found

**Verify the JAR exists:**
```bash
ls -la ~/dev/solo/mcp-server-demo/build/libs/mcp-server-demo-all.jar
```

**If missing, rebuild:**
```bash
cd ~/dev/solo/mcp-server-demo
./gradlew shadowJar
```

### Server connects but tools don't work

**Test the server directly:**
```bash
npx @modelcontextprotocol/inspector java -jar build/libs/mcp-server-demo-all.jar
```

If the inspector works but Claude doesn't, try:
1. Restart Claude Desktop
2. Check Claude's logs for errors
3. Verify the config file syntax is correct

## 🎨 Advanced Usage

### Running Multiple MCP Servers

You can add more servers to the config:

```json
{
  "mcpServers": {
    "hello-world": {
      "command": "java",
      "args": ["-jar", "/path/to/mcp-server-demo-all.jar"]
    },
    "another-server": {
      "command": "python",
      "args": ["-m", "another_mcp_server"]
    }
  }
}
```

### Updating the Server

When you make changes to your server:

1. Rebuild the JAR:
   ```bash
   cd ~/dev/solo/mcp-server-demo
   ./gradlew clean build
   ```

2. Restart Claude Desktop

Your changes will be live!

## 💡 Tips for Using MCP with Claude

1. **Be Specific:** Tell Claude exactly which tool to use
   - "Use the hello tool" ✅
   - "Say hello" ⚠️ (Claude might just respond instead)

2. **Reference Resources by URI:**
   - "Read hello://server/info" ✅
   - "Show me the server info" ⚠️

3. **Test Tools Independently First:**
   - Use the MCP Inspector to verify tools work
   - Then try them with Claude

4. **Check for Updates:**
   - The MCP protocol evolves
   - Keep your Kotlin SDK updated

## 🎓 Learning Resources

- [MCP Documentation](https://modelcontextprotocol.io)
- [Claude Desktop MCP Guide](https://docs.anthropic.com/claude/docs/mcp)
- [Your Server's README](README.md)
- [Resources Guide](RESOURCES.md)

## 🎉 You're All Set!

Your MCP server is now integrated with Claude Desktop! Try asking Claude to use your tools and resources.

**Example prompt to get started:**
```
"Hi! I see you have access to a hello-world MCP server. 
Can you list what tools and resources are available, 
and then use the hello tool to greet me?"
```

Happy coding! 🚀
