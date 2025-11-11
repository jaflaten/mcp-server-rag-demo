# RAG Pipeline Implementation

A clean, modular Retrieval-Augmented Generation (RAG) pipeline with clearly defined steps.

## Overview

This RAG implementation follows a naive but clear architecture with four distinct steps:

```
Documents → Ingestion → Chunking → Embedding → Vector Storage
```

## Pipeline Steps

### Step 1: Document Ingestion
**File**: `DocumentIngestion.kt`

Reads files from the `documents/` folder and preserves structure:
- **Headings**: Markdown hierarchy (#, ##, ###)
- **Lists**: Bullet points and numbered lists
- **Structure**: Paragraphs, code blocks
- **Metadata**: Source path, title, file type, statistics

**Features**:
- Supports `.txt` and `.md` files
- Extracts title from content or filename
- Counts headings, lists, code blocks
- Preserves document structure

### Step 2: Document Chunking
**File**: `DocumentChunking.kt`

Splits documents into manageable chunks with overlapping context:
- **Chunk size**: 500 characters (configurable)
- **Overlap**: 100 characters (configurable)
- **Smart boundaries**: Breaks at sentence endings
- **Context preservation**: Includes relevant headings

**Features**:
- Sentence-aware splitting
- Word boundary detection
- Heading hierarchy tracking
- Chunk metadata (position, parent document)

### Step 3: Embedding Generation
**File**: `EmbeddingService.kt`

Creates vector embeddings using OpenAI's API:
- **Model**: `text-embedding-3-small`
- **Batch processing**: 20 chunks per batch
- **Rate limiting**: Built-in delays
- **Error handling**: Graceful fallbacks

**Features**:
- Async batch processing
- Progress reporting
- API error handling
- Configurable model selection

### Step 4: Vector Storage
**File**: `VectorStore.kt`

In-memory vector database with JSON persistence:
- **Storage**: `.json` file for simplicity
- **Search**: Cosine similarity
- **Operations**: Add, search, save, load

**Features**:
- Fast in-memory search
- Persistent storage to JSON
- Cosine similarity search
- Metadata tracking

## Data Models

**File**: `Models.kt`

Clean, serializable data structures:
- `Document` - Ingested document with metadata
- `DocumentChunk` - Chunk with embedding and metadata
- `VectorStoreEntry` - Serialized storage format

## Project Structure

```
src/main/kotlin/rag/
├── Models.kt              # Data structures
├── DocumentIngestion.kt   # Step 1: Read documents
├── DocumentChunking.kt    # Step 2: Create chunks
├── EmbeddingService.kt    # Step 3: Generate embeddings
├── VectorStore.kt         # Step 4: Store and search
├── RagPipeline.kt         # Orchestrator
└── RagPipelineTool.kt     # CLI tool

documents/                 # Place documents here
├── mcp_overview.md
├── kotlin_guide.md
└── rag_systems.md

vector_store.json          # Generated storage file
```

## Usage

### Prerequisites

1. **Optional: OpenAI API Key**: Get from https://platform.openai.com/api-keys (for production-quality embeddings)
2. **Documents**: Add `.txt` or `.md` files to `documents/` folder

### Running the Pipeline

**Option 1: Simple Embeddings (No API Key Required)**
```bash
# Run without API key - uses hash-based embeddings
./gradlew runRagPipeline
```

**Option 2: OpenAI Embeddings (Better Quality)**
```bash
# Set your API key
export OPENAI_API_KEY=sk-your-key-here

# Run the pipeline
./gradlew runRagPipeline

# Or in one line
OPENAI_API_KEY=sk-your-key ./gradlew runRagPipeline
```

### Testing with a Query

```bash
# Run pipeline and test a query
OPENAI_API_KEY=sk-your-key ./gradlew runRagPipeline --args="What is MCP?"
```

### Output

The pipeline creates `vector_store.json` with:
- All document chunks
- Vector embeddings
- Complete metadata
- Store statistics

Example output:
```
[Step 1/4] Document Ingestion
✓ Ingested: mcp_overview.md
✓ Ingested: kotlin_guide.md
✓ Ingested: rag_systems.md
✓ Ingested 3 documents

[Step 2/4] Document Chunking
✓ Chunked 'MCP Overview' into 12 chunks
✓ Chunked 'Kotlin Guide' into 10 chunks
✓ Chunked 'RAG Systems' into 11 chunks
✓ Created 33 chunks

[Step 3/4] Generating Embeddings
✓ Batch 1/2 complete
✓ Batch 2/2 complete
✓ Generated embeddings for 33/33 chunks

[Step 4/4] Storing in Vector Database
✓ Added 33 chunks to vector store
✓ Vector store saved to vector_store.json
```

## Configuration

### Chunk Settings

Edit in `RagPipeline.kt`:
```kotlin
val chunkSize = 500        // Characters per chunk
val chunkOverlap = 100     // Overlap between chunks
```

### Embedding Model

Edit in `EmbeddingService.kt`:
```kotlin
private val model: String = "text-embedding-3-small"
// Or: "text-embedding-3-large" for better quality
```

### Storage Location

```kotlin
val vectorStorePath = "vector_store.json"
// Or: "data/vectors.json" for custom location
```

## Integration with MCP Server

The RAG pipeline can be integrated with the MCP server to provide:
- Document search tools
- Context-aware responses
- Knowledge base queries

See `HelloWorldMcpServer.kt` for integration examples.

## Future Enhancements

This naive implementation can be upgraded with:

1. **Better Storage**: Replace JSON with proper vector DB
   - Pinecone
   - Weaviate
   - Qdrant
   - Milvus

2. **More File Types**: Add support for
   - PDF files
   - DOCX documents
   - HTML pages
   - JSON/YAML files

3. **Advanced Chunking**: Implement
   - Semantic chunking
   - Recursive splitting
   - Token-aware splitting

4. **Hybrid Search**: Combine
   - Vector similarity
   - Keyword search (BM25)
   - Reranking

5. **Metadata Filtering**: Query by
   - Document type
   - Date ranges
   - Source filters

## API Reference

### RagPipeline

```kotlin
val pipeline = RagPipeline(
    documentsPath = Paths.get("documents"),
    openAiApiKey = "your-api-key",
    vectorStorePath = "vector_store.json",
    chunkSize = 500,
    chunkOverlap = 100
)

// Run full pipeline
pipeline.runPipeline()

// Load existing store
pipeline.loadVectorStore()

// Query
val results = pipeline.query(
    queryText = "What is MCP?",
    topK = 5,
    minSimilarity = 0.7
)
```

### VectorStore

```kotlin
val store = VectorStore("vector_store.json")

// Add chunks
store.addChunks(chunks)

// Search
val results = store.search(
    queryEmbedding = embedding,
    topK = 5,
    minSimilarity = 0.7
)

// Persist
store.save()

// Load
store.load()
```

## Troubleshooting

### "OPENAI_API_KEY not set"
```bash
export OPENAI_API_KEY=sk-your-key-here
```

### "No documents found"
Add `.txt` or `.md` files to the `documents/` folder.

### "Failed to call OpenAI API"
- Check your API key is valid
- Verify you have API credits
- Check internet connection

### "Out of memory"
- Reduce chunk size
- Process fewer documents
- Increase JVM heap: `-Xmx2g`

## Performance

Current implementation handles:
- **Documents**: Hundreds
- **Chunks**: Thousands
- **Search**: Milliseconds (in-memory)

For larger scale, consider:
- Streaming document processing
- Batch embedding in parallel
- Dedicated vector database
- Distributed processing

## Security

- ✅ API keys via environment variables
- ✅ Input sanitization in chunking
- ✅ Error handling throughout
- ⚠️ Store API keys securely
- ⚠️ Don't commit `vector_store.json` with sensitive data

## License

Same as parent project (MIT).
