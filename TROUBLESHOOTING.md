# Common MCP Server Issues and Fixes

## Issue: "Unexpected non-whitespace character after JSON"

### Symptoms
```
Error from MCP server: SyntaxError: Unexpected non-whitespace character after JSON at position 4
```

### Cause
The MCP server is writing non-JSON data (like logs) to STDOUT, which interferes with JSON-RPC communication.

### Why This Happens
When using STDIO transport, the MCP protocol uses:
- **STDOUT**: For JSON-RPC messages ONLY
- **STDERR**: For logs, debug output, etc.

If logging is configured to use STDOUT, it corrupts the JSON-RPC stream.

### The Fix

Update `src/main/resources/logback.xml` to use STDERR:

```xml
<configuration>
    <!-- Disable logback's own status messages -->
    <statusListener class="ch.qos.logback.core.status.NopStatusListener" />
    
    <!-- Log to STDERR (not STDOUT) -->
    <appender name="STDERR" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.err</target>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Root logger: ERROR level, output to STDERR -->
    <root level="ERROR">
        <appender-ref ref="STDERR"/>
    </root>
</configuration>
```

**Key changes:**
1. `<target>System.err</target>` - Redirects to STDERR
2. `<statusListener class="ch.qos.logback.core.status.NopStatusListener" />` - Silences logback's init messages
3. `level="ERROR"` - Reduces log verbosity

### After the Fix

Rebuild the server:
```bash
cd ~/dev/solo/mcp-server-demo
./gradlew clean build
```

Restart Claude Desktop:
```bash
# Quit completely (Cmd+Q)
# Reopen Claude Desktop
```

### Testing the Fix

Test that only JSON is output to STDOUT:
```bash
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0.0"}}}' | java -jar build/libs/mcp-server-demo-all.jar 2>/dev/null
```

You should see only JSON output, nothing else.

---

## Other Common Issues

### Issue: Server doesn't start

**Check Java version:**
```bash
java -version
```

Need Java 17+. Install with:
```bash
brew install openjdk@17
```

### Issue: JAR not found

**Rebuild:**
```bash
cd ~/dev/solo/mcp-server-demo
./gradlew clean shadowJar
```

**Verify:**
```bash
ls -la build/libs/mcp-server-demo-all.jar
```

### Issue: Claude doesn't see the server

**Check config file:**
```bash
cat ~/Library/Application\ Support/Claude/claude_desktop_config.json
```

**Verify path is correct:**
```json
{
  "mcpServers": {
    "hello-world": {
      "command": "java",
      "args": ["-jar", "/full/absolute/path/to/mcp-server-demo-all.jar"]
    }
  }
}
```

**Restart Claude:**
- Must fully quit (Cmd+Q)
- Not just close window

### Issue: Tools work in inspector but not in Claude

**Test with inspector first:**
```bash
npx @modelcontextprotocol/inspector java -jar build/libs/mcp-server-demo-all.jar
```

If inspector works but Claude doesn't:
1. Check Claude logs: `~/Library/Logs/Claude/`
2. Verify config file syntax
3. Try removing and re-adding the server config

---

## Best Practices for STDIO MCP Servers

### 1. Never Write to STDOUT (except JSON-RPC)
```kotlin
// ❌ BAD
println("Debug message")  // Goes to STDOUT!

// ✅ GOOD
System.err.println("Debug message")  // Goes to STDERR
```

### 2. Configure Logging Properly
- Use STDERR for all logs
- Set log level to ERROR or WARN
- Disable framework initialization messages

### 3. Test Output Cleanliness
```bash
# Should output ONLY JSON
echo '{"jsonrpc":"2.0","id":1,"method":"initialize",...}' | java -jar server.jar 2>/dev/null
```

### 4. Use Log Files for Development
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/mcp-server.log</file>
    <encoder>
        <pattern>%d{HH:mm:ss.SSS} %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

Then you can tail the log file:
```bash
tail -f logs/mcp-server.log
```

---

## Debugging Tips

### Enable Verbose Logging (temporarily)

Change logback.xml:
```xml
<root level="DEBUG">
    <appender-ref ref="STDERR"/>
</root>
```

Watch STDERR output in Claude logs.

### Test JSON-RPC Messages Manually

```bash
# Initialize
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0.0"}}}' | java -jar build/libs/mcp-server-demo-all.jar

# List tools
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' | java -jar build/libs/mcp-server-demo-all.jar

# Call tool
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"hello","arguments":{}}}' | java -jar build/libs/mcp-server-demo-all.jar
```

### Check Process Communication

```bash
# Start server
java -jar build/libs/mcp-server-demo-all.jar &
SERVER_PID=$!

# Check it's running
ps -p $SERVER_PID

# Kill when done
kill $SERVER_PID
```

---

## Summary

The most common issue with MCP STDIO servers is accidentally writing to STDOUT. Always:

1. ✅ Configure logging to use STDERR
2. ✅ Silence framework initialization messages  
3. ✅ Test that STDOUT contains only JSON
4. ✅ Use log files for development debugging

After fixing logging, rebuild and restart Claude Desktop!
