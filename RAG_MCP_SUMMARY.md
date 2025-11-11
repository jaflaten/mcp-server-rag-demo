# RAG-Powered MCP Server Summary

## Overview
This MCP (Model Context Protocol) server implements a complete RAG (Retrieval-Augmented Generation) pipeline that allows LLMs to query a local knowledge base about Pokemon.

## Architecture

### MCP Server Tools
The server exposes two main tools:

1. **greeting** - A simple hello world tool that greets users by name
2. **rag_query** - A RAG-powered search tool that retrieves relevant information from the vector database

### RAG Pipeline Components

#### 1. Ingestion Pipeline (`RagIngestionPipeline.kt`)
- **Document Loading**: Reads `.txt` files from the `documents/` folder
- **Structure Preservation**: Maintains file structure including headings and lists
- **Metadata Tracking**: Adds source title, filename, and page information
- **Chunking Strategy**: Uses semantic chunking with 500 character chunks and 100 character overlap
- **Embedding Generation**: Creates vector embeddings using Ollama's `embeddinggemma:latest` model (384 dimensions)
- **Storage**: Saves embeddings and metadata to `vector_store.json` for fast retrieval

#### 2. Query Pipeline (Integrated in MCP Server)
When a user calls the `rag_query` tool:

1. **Query Embedding**: Converts the user's question into a vector embedding using the same Ollama model
2. **Similarity Search**: Compares query embedding against all stored document embeddings using cosine similarity
3. **Top-K Retrieval**: Returns the 5 most relevant text chunks
4. **Context Formatting**: Formats retrieved chunks with metadata (source, page) for the LLM
5. **Response Generation**: Sends context to Ollama's LLM (llama3.2:latest) to generate a natural language answer
6. **Source Attribution**: Returns both the answer and the source chunks used

## Technical Details

### Embedding Model
- **Engine**: Ollama (local, free, no API keys required)
- **Model**: embeddinggemma:latest
- **Dimensions**: 384
- **Type**: Semantic embeddings (understands meaning, not just keywords)

### LLM Model
- **Engine**: Ollama (local, free, no API keys required)
- **Model**: llama3.2:latest
- **Purpose**: Generates natural language answers based on retrieved context

### Vector Store
- **Format**: JSON file (`vector_store.json`)
- **Storage**: In-memory during runtime, persisted to disk
- **Contents**: Text chunks, embeddings, metadata (source, page, title)
- **Size**: Currently 70 chunks from Pokemon documents

## Usage Flow

### With Claude Desktop
1. User asks Claude: "What type is Lapras?"
2. Claude invokes the MCP server's `rag_query` tool with the query
3. Server generates query embedding → searches vector store → retrieves relevant chunks
4. Server sends chunks to Ollama LLM for answer generation
5. Claude receives the answer and source information
6. Claude presents the answer to the user with proper attribution

### Standalone Query
```bash
./gradlew runQuery -Pargs="What type is Lapras?"
```

## Key Features

✓ **No API Keys Required**: Uses local Ollama models for embeddings and LLM  
✓ **Semantic Search**: Understands meaning, not just keyword matching  
✓ **Source Attribution**: Always shows which documents were used  
✓ **Fast Retrieval**: In-memory vector store with cosine similarity  
✓ **Metadata Rich**: Tracks document sources, pages, and titles  
✓ **MCP Integration**: Seamlessly works with Claude Desktop and other MCP clients  

## Example Interaction

**User Query**: "What type is Lapras?"

**Pipeline Execution**:
1. Query embedding generated (384d vector)
2. Vector store searched (70 chunks)
3. Top 5 relevant chunks retrieved
4. Context formatted with metadata
5. LLM generates answer using context

**Response**:
```
Lapras is a Water/Ice type Pokemon.

Sources:
- documents/lapras.txt (Page 1)
- documents/water_pokemon.txt (Page 2)
```

## Files Overview

- `src/main/kotlin/no/flaten/McpServer.kt` - MCP server with rag_query tool
- `src/main/kotlin/no/flaten/rag/RagIngestionPipeline.kt` - Document ingestion and embedding
- `src/main/kotlin/no/flaten/rag/RagQueryTool.kt` - Standalone query tool
- `src/main/kotlin/no/flaten/rag/OllamaEmbeddingProvider.kt` - Ollama embedding integration
- `src/main/kotlin/no/flaten/rag/OllamaLLMProvider.kt` - Ollama LLM integration
- `src/main/kotlin/no/flaten/rag/VectorStore.kt` - Vector storage and similarity search
- `documents/*.txt` - Knowledge base (Pokemon information)
- `vector_store.json` - Persisted vector embeddings

## Future Enhancements

Possible improvements:
- Add more sophisticated chunking strategies (markdown-aware, sentence-based)
- Implement re-ranking of retrieved chunks
- Add support for multiple document formats (PDF, Markdown, HTML)
- Implement hybrid search (keyword + semantic)
- Add caching for frequently asked questions
- Support for streaming responses
