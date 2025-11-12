# Pokémon Expert Chatbot - RAG-Powered MCP Integration

## Overview

This project demonstrates a complete AI chatbot system that combines:
- **Local LLM** (Ollama with llama3.2) for natural language generation
- **RAG (Retrieval Augmented Generation)** for knowledge-based responses
- **MCP (Model Context Protocol)** for tool integration
- **Web UI** for easy user interaction

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Pokémon Chatbot Web UI                    │
│                     (http://localhost:9000)                  │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────────┐
        │      PokemonChatbot (Kotlin)           │
        │  - Manages conversation history        │
        │  - Decides when to query knowledge base│
        │  - Formats prompts for LLM             │
        └──────────┬──────────────────┬──────────┘
                   │                  │
         ┌─────────▼──────┐    ┌─────▼──────────┐
         │ Ollama Client  │    │  MCP Client    │
         │  (llama3.2)    │    │  (JSON-RPC)    │
         └────────┬───────┘    └────────┬───────┘
                  │                     │
          ┌───────▼────────┐    ┌───────▼───────────┐
          │ Ollama Server  │    │  MCP RAG Server   │
          │  localhost     │    │   (JAR Process)   │
          │  :11434        │    └───────┬───────────┘
          └────────────────┘            │
                                ┌───────▼──────────┐
                                │  Vector Store    │
                                │ (embeddings +    │
                                │  Pokémon data)   │
                                └──────────────────┘
```

## Components

### 1. MCP RAG Server (Main Project)
Located in the root project, this server provides:
- **Tool**: `ragQuery` - Searches the vector database and returns relevant information
- **Vector Store**: In-memory database with Pokémon information
- **Embeddings**: Uses Ollama's embeddinggemma model for semantic search
- **Protocol**: JSON-RPC over stdio

### 2. Pokémon Chatbot (chatbot module)
A separate Kotlin module that acts as:
- **MCP Client**: Connects to the RAG server via stdio
- **LLM Client**: Integrates with Ollama for response generation
- **Web Server**: Ktor-based HTTP server with HTML/CSS/JS frontend
- **Conversation Manager**: Maintains context and history

### 3. Web Interface
A clean, modern single-page application featuring:
- Real-time chat interface
- Message history
- Automatic scrolling
- Loading indicators
- Responsive design with gradient background

## How It Works

1. **User sends a message** via the web interface
2. **Chatbot analyzes** the message to determine if it needs knowledge base access
3. **If needed**, chatbot calls MCP server's `ragQuery` tool:
   - Query is embedded using Ollama
   - Vector similarity search finds relevant chunks
   - Top 5 most relevant chunks are returned
4. **Chatbot constructs a prompt** combining:
   - System prompt (personality/instructions)
   - Conversation history (last 3 exchanges)
   - RAG context (if retrieved)
   - User's current message
5. **Ollama generates a response** based on the complete context
6. **Response is sent back** to the web UI and displayed

## Key Features

### Intelligent Query Detection
The chatbot automatically detects when to query the knowledge base based on:
- Question words (what, which, who, how, etc.)
- Pokémon-related terms (type, evolve, move, etc.)

### Context-Aware Responses
- Maintains conversation history (up to 5 exchanges)
- Combines retrieved information with conversational context
- Generates natural, friendly responses

### RAG Pipeline
The main project includes a complete RAG implementation:
- Document ingestion from text files
- Intelligent chunking with metadata
- Vector embeddings via Ollama (embeddinggemma)
- Cosine similarity search
- JSON-based vector store

### Local & Free
- No API keys required (uses local Ollama)
- No external services needed
- Complete privacy - all processing is local

## Running the System

### Prerequisites
1. Build the main MCP server:
   ```bash
   ./gradlew build
   ```

2. Ensure vector store exists:
   ```bash
   ./gradlew runRagPipeline
   ```

3. Start Ollama:
   ```bash
   ollama serve
   ollama pull llama3.2
   ollama pull embeddinggemma
   ```

### Start the Chatbot
```bash
# Default port 8080
./gradlew :chatbot:run

# Custom port
PORT=9000 ./gradlew :chatbot:run

# Custom Ollama model
OLLAMA_MODEL=mistral ./gradlew :chatbot:run
```

### Access the Web UI
Open your browser to http://localhost:9000 (or your configured port)

## Example Queries

- "What type is Lapras?"
- "Tell me about Gengar"
- "What ghost-type Pokémon do we have?"
- "How do Pokémon evolve?"
- "What are the different types?"

## Technical Details

### MCP Client Implementation
The `McpRagClient` uses direct JSON-RPC communication:
- Spawns the MCP server as a subprocess
- Communicates via stdin/stdout
- Sends initialize and tool call requests
- Parses JSON-RPC responses

### Ollama Integration
The `OllamaClient` makes HTTP requests to the local Ollama API:
- POST to `/api/generate` for text generation
- Supports system prompts and temperature control
- Non-streaming mode for simplicity

### Conversation Management
The `PokemonChatbot` class:
- Stores messages as `(role, content)` pairs
- Trims history to last 10 messages (5 exchanges)
- Builds prompts with history, RAG context, and system instructions
- Handles errors gracefully

## Performance

- **Initial connection**: ~1-2 seconds (MCP server startup)
- **RAG query**: ~2-5 seconds (embedding + vector search)
- **LLM generation**: ~5-15 seconds (depends on model and response length)
- **Total response time**: ~10-20 seconds

## Future Enhancements

Potential improvements:
- Streaming responses for faster perceived performance
- Multiple MCP servers for different data sources
- More sophisticated query routing
- Response caching
- Conversation persistence
- Multi-user support
- Voice input/output

## Files Structure

```
mcp-server-demo/
├── src/                          # Main MCP RAG server
│   └── main/kotlin/
│       ├── HelloWorldMcpServer.kt
│       └── rag/
│           ├── RagPipelineTool.kt
│           ├── OllamaEmbeddingProvider.kt
│           └── ...
├── chatbot/                      # Chatbot module
│   ├── build.gradle.kts
│   ├── README.md
│   └── src/main/kotlin/no/flaten/chatbot/
│       ├── PokemonChatbotServer.kt   # Main entry + web server
│       ├── PokemonChatbot.kt         # Core chatbot logic
│       ├── McpRagClient.kt           # MCP client
│       └── OllamaClient.kt           # Ollama client
├── documents/                    # Source documents for RAG
├── vector_store.json            # Embedded document chunks
└── build.gradle.kts             # Main build config
```

## Technologies Used

- **Kotlin** - Programming language
- **Ktor** - Web framework
- **MCP SDK** - Model Context Protocol
- **Ollama** - Local LLM runtime
- **Gradle** - Build system
- **Netty** - Async networking
- **Kotlinx Serialization** - JSON handling

## Summary

This project demonstrates a modern, local-first AI application architecture using:
- RAG for grounded, accurate responses
- MCP for modular tool integration
- Local LLMs for privacy and cost savings
- Clean web UI for user interaction

The chatbot provides knowledgeable, friendly assistance about Pokémon by combining the power of large language models with structured, retrieved information from a vector database.
