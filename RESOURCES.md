# MCP Resources Guide

## What are MCP Resources?

Resources in MCP are data sources that can be read by clients. Unlike tools (which perform actions), resources provide information. Think of them as files or data endpoints that clients can access.

## Resources in This Server

### 1. Static Resource: Server Information

**URI:** `hello://server/info`

**Description:** Provides static information about the server, including version, capabilities, and description.

**Type:** text/plain

**Example Usage:**

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "resources/read",
  "params": {
    "uri": "hello://server/info"
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "contents": [
      {
        "uri": "hello://server/info",
        "mimeType": "text/plain",
        "text": "MCP Hello World Server\n======================\nVersion: 1.0.0\n..."
      }
    ]
  }
}
```

---

### 2. Dynamic Resource: Personalized Greetings

**URI Pattern:** `hello://greetings/{name}`

**Description:** Generates a personalized greeting message with timestamp. The `{name}` placeholder is replaced with the actual name in the URI.

**Type:** text/plain

**Examples:**

#### For Alice
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "resources/read",
  "params": {
    "uri": "hello://greetings/Alice"
  }
}
```

#### For Bob
```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "resources/read",
  "params": {
    "uri": "hello://greetings/Bob"
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "contents": [
      {
        "uri": "hello://greetings/Alice",
        "mimeType": "text/plain",
        "text": "Hello, Alice!\n\nThis is a dynamic resource...\n\nCurrent timestamp: 2024-11-11T12:00:00Z\n..."
      }
    ]
  }
}
```

---

## Listing All Resources

To discover available resources:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "resources/list",
  "params": {}
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "resources": [
      {
        "uri": "hello://server/info",
        "name": "Server Information",
        "description": "Information about this MCP server",
        "mimeType": "text/plain"
      },
      {
        "uri": "hello://greetings/{name}",
        "name": "Personalized Greeting",
        "description": "Get a personalized greeting message (use {name} placeholder in URI)",
        "mimeType": "text/plain"
      }
    ]
  }
}
```

---

## Static vs Dynamic Resources

### Static Resources
- Content doesn't change between reads
- Example: Server configuration, documentation
- URI: Fixed (`hello://server/info`)

### Dynamic Resources
- Content changes based on parameters or time
- Example: Personalized greetings, current data
- URI: Template with placeholders (`hello://greetings/{name}`)

---

## Input Sanitization

Just like tools, resource URIs are sanitized to prevent security issues:

```kotlin
// Extracting name from URI
val rawName = uriPath.substringAfter("hello://greetings/")
val sanitizedName = sanitizeInput(rawName, maxLength = 50)
```

**Security measures:**
- Control characters removed
- Length limited to 50 characters
- Whitespace trimmed

---

## Testing Resources

### Using the Python Test Client

```bash
python3 test-client.py build/libs/mcp-server-demo-all.jar
```

This automatically tests all resources (tests 8-10).

### Using MCP Inspector

```bash
npx @modelcontextprotocol/inspector java -jar build/libs/mcp-server-demo-all.jar
```

In the inspector UI:
1. Click "Resources" tab
2. See list of available resources
3. Click on a resource to read it
4. For dynamic resources, edit the URI to test different values

### Manual Testing

```bash
# List resources
echo '{"jsonrpc":"2.0","id":1,"method":"resources/list","params":{}}' | java -jar build/libs/mcp-server-demo-all.jar

# Read server info
echo '{"jsonrpc":"2.0","id":2,"method":"resources/read","params":{"uri":"hello://server/info"}}' | java -jar build/libs/mcp-server-demo-all.jar

# Read greeting for Alice
echo '{"jsonrpc":"2.0","id":3,"method":"resources/read","params":{"uri":"hello://greetings/Alice"}}' | java -jar build/libs/mcp-server-demo-all.jar
```

---

## Use Cases for Resources

Resources are ideal for:

1. **Configuration Data**: Server settings, API endpoints
2. **Documentation**: Help text, API docs, examples
3. **Status Information**: Current state, health checks
4. **Data Access**: Database records, file contents
5. **Generated Content**: Reports, summaries, formatted data

---

## Code Example

Adding a new resource:

```kotlin
server.addResource(
    uri = "hello://example/{id}",
    name = "Example Resource",
    description = "An example resource with dynamic ID",
    mimeType = "text/plain"
) { request ->
    val id = request.uri.substringAfter("hello://example/")
    val sanitizedId = sanitizeInput(id, maxLength = 20)
    
    ReadResourceResult(
        contents = listOf(
            TextResourceContents(
                uri = request.uri,
                mimeType = "text/plain",
                text = "Resource ID: $sanitizedId"
            )
        )
    )
}
```

---

## Best Practices

1. **Use descriptive URIs**: Make it clear what the resource provides
2. **Include MIME types**: Help clients understand content format
3. **Sanitize inputs**: Always validate URI parameters
4. **Handle errors gracefully**: Return meaningful error messages
5. **Keep it simple**: Resources should be fast to read
6. **Document parameters**: Explain URI template placeholders

---

## Related Documentation

- [README.md](README.md) - Project overview
- [TESTING.md](TESTING.md) - Testing examples
- [SECURITY.md](SECURITY.md) - Security considerations
- [HOW_TO_TEST.md](HOW_TO_TEST.md) - Quick testing guide
