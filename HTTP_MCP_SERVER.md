# HTTP MCP Server

This project now includes an HTTP-based MCP server that exposes the RAG-powered tools via RESTful HTTP endpoints.

## Overview

The HTTP MCP server provides a simple HTTP/JSON-RPC interface to the MCP tools, making them accessible from any HTTP client or web application. This is an alternative to the stdio-based MCP server and enables deployment as a web service.

## Architecture

```
┌─────────────┐      HTTP/JSON      ┌──────────────────┐
│   Client    │ ◄──────────────────► │   HTTP Server    │
│ (curl, web) │                      │   (Port 8080)    │
└─────────────┘                      └──────────────────┘
                                              │
                                              ▼
                                     ┌──────────────────┐
                                     │   MCP Tools      │
                                     │  - hello         │
                                     │  - rag_query     │
                                     └──────────────────┘
                                              │
                                              ▼
                                     ┌──────────────────┐
                                     │  RAG Pipeline    │
                                     │  - Vector Store  │
                                     │  - Ollama        │
                                     └──────────────────┘
```

## Running the Server

### Prerequisites

1. Build the vector store first:
```bash
./gradlew runRagPipeline
```

2. Make sure Ollama is running with the embedding model:
```bash
ollama pull embeddinggemma:latest
ollama serve  # Should run on http://localhost:11434
```

### Start the HTTP Server

```bash
./gradlew runHttpServer
```

The server will start on `http://0.0.0.0:8080`

## API Endpoints

### Health Check
```bash
GET /health
```

Returns: `OK - MCP Server Running`

### List Available Tools
```bash
GET /mcp/tools
```

Returns JSON with all available tools and their schemas.

Example response:
```json
{
  "tools": [
    {
      "name": "hello",
      "description": "Returns a friendly greeting",
      "inputSchema": { ... }
    },
    {
      "name": "rag_query",
      "description": "Query the RAG knowledge base",
      "inputSchema": { ... }
    }
  ]
}
```

### Call a Tool
```bash
POST /mcp/call
Content-Type: application/json

{
  "tool": "tool_name",
  "arguments": {
    "param1": "value1",
    "param2": "value2"
  }
}
```

Returns: Tool execution result with content

### List Resources
```bash
GET /mcp/resources
```

Returns JSON with available resources.

## Usage Examples

### Hello Tool

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"hello","arguments":{"name":"World"}}' \
  http://localhost:8080/mcp/call
```

Response:
```json
{
  "content": [
    {
      "type": "text",
      "text": "Hello, World! Welcome to the HTTP MCP server!"
    }
  ]
}
```

### RAG Query Tool

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"tool":"rag_query","arguments":{"query":"What type is Lapras?","topK":5}}' \
  http://localhost:8080/mcp/call
```

Response:
```json
{
  "content": [
    {
      "type": "text",
      "text": "Query: What type is Lapras?\n\nAnswer: ...\n\nSources (5 chunks):\n..."
    }
  ]
}
```

### RAG Query with Python

```python
import requests
import json

url = "http://localhost:8080/mcp/call"
payload = {
    "tool": "rag_query",
    "arguments": {
        "query": "Which Pokemon can Mega Evolve?",
        "topK": 5,
        "minSimilarity": 0.3
    }
}

response = requests.post(url, json=payload)
result = response.json()
print(result["content"][0]["text"])
```

### RAG Query with JavaScript

```javascript
fetch('http://localhost:8080/mcp/call', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    tool: 'rag_query',
    arguments: {
      query: 'What Pokemon are Ghost type?',
      topK: 5
    }
  })
})
.then(response => response.json())
.then(data => console.log(data.content[0].text));
```

## Deployment Considerations

### Running in Production

1. **Use a proper application server**: Consider deploying with a reverse proxy (nginx, Apache)
2. **Add authentication**: Implement API keys or OAuth
3. **Add rate limiting**: Protect against abuse
4. **Use HTTPS**: Enable TLS for secure communication
5. **Monitor**: Add logging and metrics
6. **Scale**: Consider load balancing for high traffic

### Environment Variables

Set these before running:
- `OLLAMA_BASE_URL` - Ollama API endpoint (default: http://localhost:11434)
- `VECTOR_STORE_PATH` - Path to vector store file (default: ./vector_store.json)

### Docker Deployment (Future Enhancement)

```dockerfile
FROM gradle:jdk21 AS build
COPY . /app
WORKDIR /app
RUN ./gradlew shadowJar

FROM openjdk:21-slim
COPY --from=build /app/build/libs/*-all.jar /app/server.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/server.jar"]
```

## Differences from Stdio MCP Server

| Feature | Stdio Server | HTTP Server |
|---------|-------------|-------------|
| Transport | stdin/stdout | HTTP/JSON |
| Use Case | Local CLI tools, Claude Desktop | Web apps, APIs, remote access |
| Discovery | MCP protocol | REST API |
| Authentication | Process-based | API keys (future) |
| Scalability | Single process | Horizontal scaling |
| Debugging | Harder | Easy with curl/Postman |

## Testing

Test the server with the provided curl commands or use tools like:
- **Postman** - GUI for API testing
- **HTTPie** - User-friendly curl alternative
- **Insomnia** - REST client
- **Thunder Client** - VS Code extension

## Troubleshooting

### Server won't start
- Check if port 8080 is already in use: `lsof -ti:8080`
- Kill existing process: `kill -9 $(lsof -ti:8080)`

### RAG queries fail
- Ensure vector store exists: `ls -la vector_store.json`
- Verify Ollama is running: `curl http://localhost:11434/api/tags`
- Check Ollama has the model: `ollama list | grep embeddinggemma`

### Empty responses
- Check server logs in the terminal
- Verify request format matches examples
- Ensure Content-Type header is set

## Future Enhancements

1. **SSE Support**: Add Server-Sent Events for streaming responses
2. **WebSocket**: Real-time bidirectional communication
3. **GraphQL**: Alternative query interface
4. **OpenAPI Spec**: Auto-generated API documentation
5. **Client SDKs**: Auto-generated clients for popular languages
6. **Authentication**: JWT, OAuth2, API keys
7. **Monitoring**: Prometheus metrics, distributed tracing
