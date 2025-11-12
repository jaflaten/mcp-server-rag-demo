# Pokémon Expert Chatbot

A friendly AI chatbot that helps Pokémon trainers using RAG (Retrieval Augmented Generation) with MCP integration.

## Architecture

This chatbot combines:
- **Ollama LLM** - Local language model for natural conversation
- **MCP Client** - Connects to the RAG MCP server for knowledge retrieval
- **Web UI** - Simple, beautiful interface for interaction

## Prerequisites

1. **Main MCP Server Built**: The main project must be built first
   ```bash
   cd ..
   ./gradlew build
   ```

2. **Ollama Running**: Ollama must be installed and running with a model
   ```bash
   ollama pull llama3.2
   ollama serve
   ```

3. **Vector Store**: The RAG pipeline must have been run to create the vector store
   ```bash
   cd ..
   ./gradlew runRagPipeline
   ```

## Running the Chatbot

From the project root:
```bash
./gradlew :chatbot:run
```

Or from the chatbot directory:
```bash
cd chatbot
../gradlew run
```

The server will start at http://localhost:8080

## Configuration

Environment variables:
- `OLLAMA_MODEL` - Ollama model to use (default: `llama3.2`)
- `PORT` - Server port (default: `8080`)

Example:
```bash
OLLAMA_MODEL=mistral PORT=3000 ./gradlew :chatbot:run
```

## Features

- 💬 Natural conversation with context awareness
- 🔍 Automatic knowledge base queries when needed
- 📚 Retrieval-augmented responses using the RAG system
- 🎨 Clean, modern web interface
- 📝 Conversation history (last 5 exchanges)

## How It Works

1. User asks a question in the web interface
2. Chatbot determines if it needs to query the knowledge base
3. If needed, queries the MCP RAG server using the `ragQuery` tool
4. Combines retrieved information with conversation context
5. Generates a friendly, informative response using Ollama
6. Displays the response in the web UI

## Example Queries

- "What type is Lapras?"
- "Tell me about ghost-type Pokémon"
- "How do I evolve my Pokémon?"
- "What are the different Pokémon types?"

## Troubleshooting

**MCP Server JAR not found**
- Build the main project: `cd .. && ./gradlew build`

**Ollama connection error**
- Ensure Ollama is running: `ollama serve`
- Check the model is available: `ollama list`

**Vector store not found**
- Run the ingestion pipeline: `cd .. && ./gradlew runRagPipeline`

**No relevant results**
- Ensure the vector store has data about your query topic
- Try rephrasing your question
