# Docker Deployment Guide

This guide explains how to deploy the MCP RAG Server using Docker.

## Prerequisites

- Docker installed and running
- Docker Compose (optional, for easier deployment)
- Ollama running on host machine (or use Docker Compose Ollama service)

## Quick Start with Docker Compose

The easiest way to run the MCP server:

```bash
# Build and start the server
docker-compose up -d

# View logs
docker-compose logs -f mcp-server

# Stop the server
docker-compose down
```

The server will be available at `http://localhost:8080`

## Building the Docker Image

```bash
# Build the image
docker build -t mcp-rag-server .

# Or with a specific tag
docker build -t mcp-rag-server:latest .
```

## Running the Container

### Basic Run

```bash
docker run -d \
  --name mcp-rag-server \
  -p 8080:8080 \
  mcp-rag-server
```

### With Ollama on Host

```bash
docker run -d \
  --name mcp-rag-server \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  -e OLLAMA_URL=http://host.docker.internal:11434 \
  mcp-rag-server
```

### With Volume Persistence

```bash
docker run -d \
  --name mcp-rag-server \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  -e OLLAMA_URL=http://host.docker.internal:11434 \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/vector_store.json:/app/data/vector_store.json:ro \
  mcp-rag-server
```

### With OpenAI API Key

```bash
docker run -d \
  --name mcp-rag-server \
  -p 8080:8080 \
  -e OPENAI_API_KEY=your-api-key-here \
  mcp-rag-server
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OPENAI_API_KEY` | "" | OpenAI API key (optional) |
| `OLLAMA_URL` | http://host.docker.internal:11434 | Ollama API endpoint |
| `VECTOR_STORE_PATH` | /app/data/vector_store.json | Path to vector store file |
| `DOCUMENTS_PATH` | /app/documents | Path to documents directory |

## Volume Mounts

- `/app/data` - Persistent storage for vector store
- `/app/documents` - Document directory for RAG pipeline (read-only)

## Health Check

The container includes a health check endpoint:

```bash
# Check health
curl http://localhost:8080/health

# Or using Docker
docker inspect --format='{{.State.Health.Status}}' mcp-rag-server
```

## Testing the Server

```bash
# List available tools
curl -X POST http://localhost:8080/mcp/tools

# Call the greeting tool
curl -X POST http://localhost:8080/mcp/call \
  -H "Content-Type: application/json" \
  -d '{
    "tool": "getPersonalizedGreeting",
    "arguments": {"name": "Trainer"}
  }'

# Call the RAG query tool
curl -X POST http://localhost:8080/mcp/call \
  -H "Content-Type: application/json" \
  -d '{
    "tool": "ragQuery",
    "arguments": {"query": "What type is Pikachu?"}
  }'
```

## Logs

View container logs:

```bash
# Follow logs
docker logs -f mcp-rag-server

# Last 100 lines
docker logs --tail 100 mcp-rag-server
```

## Stopping and Removing

```bash
# Stop the container
docker stop mcp-rag-server

# Remove the container
docker rm mcp-rag-server

# Remove the image
docker rmi mcp-rag-server
```

## Deploying to Cloud

### Build for Multi-Platform

```bash
docker buildx build --platform linux/amd64,linux/arm64 -t mcp-rag-server:latest .
```

### Push to Registry

```bash
# Tag for registry
docker tag mcp-rag-server:latest your-registry/mcp-rag-server:latest

# Push
docker push your-registry/mcp-rag-server:latest
```

## Troubleshooting

### Cannot Connect to Ollama

Make sure:
1. Ollama is running on host: `ollama list`
2. Container can access host: `--add-host=host.docker.internal:host-gateway`
3. Correct OLLAMA_URL: `http://host.docker.internal:11434`

### Vector Store Not Found

The vector store needs to be created first. You can:
1. Run the ingestion pipeline before building the image
2. Mount an existing vector_store.json file
3. Create it inside the container

### Port Already in Use

If port 8080 is already in use, map to a different port:
```bash
docker run -p 9090:8080 mcp-rag-server
```

## Production Considerations

1. **Use a reverse proxy** (nginx, Traefik) for SSL/TLS
2. **Set resource limits** using `--memory` and `--cpus`
3. **Use Docker secrets** for API keys instead of environment variables
4. **Enable logging drivers** for centralized logging
5. **Use health checks** for orchestration (Kubernetes, Docker Swarm)
6. **Persist vector store** using volumes or external storage
