# MCP Server: Stdio to HTTP Conversion

## Overview

This document explains how the MCP server was converted from stdio-based communication to HTTP-based communication, enabling it to be deployed as a web service.

## Why HTTP Transport?

### Stdio Limitations
- **Local only**: Can only be used by processes on the same machine
- **Single client**: One client at a time
- **No caching**: Difficult to implement HTTP caching strategies
- **Hard to debug**: Can't use standard HTTP tools like curl, Postman
- **No scaling**: Can't load balance or horizontally scale

### HTTP Benefits
- **Remote access**: Accessible from anywhere on the network
- **Multiple clients**: Handle concurrent requests
- **Standard tooling**: Use curl, Postman, browser for testing
- **Easy deployment**: Deploy to cloud, containers, Kubernetes
- **Familiar**: REST APIs are well-understood
- **Scalable**: Load balancers, CDNs, caching layers

## Architecture Comparison

### Stdio Architecture
```
┌─────────────┐
│Claude Desktop│
│     or      │
│MCP Inspector │
└──────┬──────┘
       │ stdin/stdout
       │ (JSON-RPC)
       ▼
┌──────────────┐
│  MCP Server  │
│   (stdio)    │
└──────────────┘
```

### HTTP Architecture
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Browser    │     │   Mobile    │     │  Desktop    │
│    App      │     │     App     │     │     App     │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       └───────────────────┴───────────────────┘
                           │
                    HTTP/JSON (REST)
                           │
                           ▼
                  ┌──────────────────┐
                  │   HTTP Server    │
                  │   (Port 8080)    │
                  │                  │
                  │  MCP Tools API   │
                  └──────────────────┘
```

## Implementation Details

### 1. Transport Layer

**Stdio Version** (`HelloWorldMcpServer.kt`):
```kotlin
val transport = StdioServerTransport(
    System.`in`.asInput(),
    System.out.asSink().buffered()
)
```

**HTTP Version** (`SimpleHttpMcpServer.kt`):
```kotlin
embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    configureMcpRouting()
}.start(wait = true)
```

### 2. API Design

The HTTP version exposes MCP functionality through REST endpoints:

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/health` | GET | Health check |
| `/mcp/tools` | GET | List available tools |
| `/mcp/call` | POST | Execute a tool |
| `/mcp/resources` | GET | List available resources |

### 3. Request/Response Format

**Tool Call Request:**
```json
{
  "tool": "rag_query",
  "arguments": {
    "query": "What type is Lapras?",
    "topK": 5
  }
}
```

**Tool Call Response:**
```json
{
  "content": [
    {
      "type": "text",
      "text": "Query: What type is Lapras?\n\nAnswer: ..."
    }
  ]
}
```

### 4. Error Handling

HTTP version uses standard HTTP status codes:
- `200 OK` - Successful request
- `400 Bad Request` - Invalid request format
- `404 Not Found` - Tool not found
- `500 Internal Server Error` - Server error

### 5. Dependencies Added

In `build.gradle.kts`:
```kotlin
implementation("io.ktor:ktor-server-netty")
implementation("io.ktor:ktor-server-content-negotiation")
```

## Code Structure

### Shared Components
Both stdio and HTTP versions share:
- RAG pipeline (`rag/` package)
- Input sanitization (`sanitizeInput()`)
- Tool definitions and logic
- Resource definitions

### HTTP-Specific Code

**SimpleHttpMcpServer.kt**:
- `runSimpleHttpMcpServer()` - Main entry point
- `configureSimpleMcpRouting()` - Route configuration
- REST endpoint handlers
- JSON request/response mapping

## Usage Examples

### Stdio Version
```bash
# Run server
./gradlew run

# Test with MCP Inspector
npx @modelcontextprotocol/inspector java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

### HTTP Version
```bash
# Run server
./gradlew runHttpServer

# Test with curl
curl -X POST -H "Content-Type: application/json" \
  -d '{"tool":"hello","arguments":{"name":"World"}}' \
  http://localhost:8080/mcp/call

# Test with Python
import requests
response = requests.post(
    'http://localhost:8080/mcp/call',
    json={"tool": "rag_query", "arguments": {"query": "What type is Lapras?"}}
)
print(response.json())
```

## Testing

### Stdio Version
- MCP Inspector (interactive)
- Python test client with MCP SDK
- Claude Desktop integration

### HTTP Version
- curl commands
- HTTP test script (`test-http-mcp.sh`)
- Postman/Insomnia
- Web browsers
- Any HTTP client library

## Deployment Options

### Stdio Version
- Local process only
- Managed by parent process (Claude Desktop)
- Requires MCP-compatible client

### HTTP Version
- **Docker**: Containerize and deploy
- **Kubernetes**: Scale horizontally
- **Cloud Run**: Serverless deployment
- **AWS Lambda**: With API Gateway
- **Heroku/Railway**: Simple PaaS deployment
- **Traditional servers**: nginx/Apache reverse proxy

## Performance Considerations

### Stdio
- ✅ Low latency (local process)
- ✅ No network overhead
- ❌ Single client limitation
- ❌ No connection pooling

### HTTP
- ✅ Concurrent requests
- ✅ Connection pooling
- ✅ Horizontal scaling
- ❌ Network latency
- ❌ HTTP overhead

## Security Considerations

### Stdio
- ✅ Process isolation
- ✅ Local only (no network exposure)
- ❌ No authentication mechanism

### HTTP
- ❌ Exposed to network
- ✅ Can add authentication (JWT, API keys)
- ✅ Can use HTTPS/TLS
- ✅ Can implement rate limiting
- ✅ Can add request validation

## Future Enhancements

### Planned Features
1. **SSE Support**: Server-Sent Events for streaming responses
2. **WebSocket**: Real-time bidirectional communication  
3. **Authentication**: JWT tokens, API keys
4. **Rate Limiting**: Protect against abuse
5. **CORS Support**: Enable browser-based clients
6. **OpenAPI Spec**: Auto-generated documentation
7. **Metrics**: Prometheus/Grafana monitoring
8. **Caching**: Redis for vector store caching

## Conversion Checklist

When converting an MCP server from stdio to HTTP:

- [x] Create HTTP server with Ktor/Netty
- [x] Add content negotiation for JSON
- [x] Design REST API endpoints
- [x] Implement tool listing endpoint
- [x] Implement tool calling endpoint
- [x] Implement resource listing endpoint
- [x] Add health check endpoint
- [x] Handle errors with HTTP status codes
- [x] Add request validation
- [x] Create API documentation
- [x] Write test scripts
- [ ] Add authentication (future)
- [ ] Add rate limiting (future)
- [ ] Add CORS support (future)
- [ ] Create OpenAPI spec (future)

## Conclusion

The HTTP conversion makes the MCP server:
1. **More accessible** - Standard REST API
2. **More scalable** - Horizontal scaling possible
3. **Easier to test** - Standard HTTP tooling
4. **Production-ready** - Can deploy to cloud
5. **Flexible** - Supports multiple clients

Both versions serve different purposes:
- **Stdio**: Best for local tools, Claude Desktop integration
- **HTTP**: Best for web apps, APIs, production deployments

Choose based on your use case!
