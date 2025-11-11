# RAG Pipeline Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                       RAG PIPELINE                               │
│                                                                  │
│  ┌──────────┐   ┌──────────┐   ┌───────────┐   ┌─────────────┐ │
│  │  Step 1  │ → │  Step 2  │ → │  Step 3   │ → │   Step 4    │ │
│  │          │   │          │   │           │   │             │ │
│  │ Document │   │ Document │   │ Embedding │   │   Vector    │ │
│  │Ingestion │   │ Chunking │   │Generation │   │   Storage   │ │
│  └──────────┘   └──────────┘   └───────────┘   └─────────────┘ │
│       │              │               │                 │         │
│       ↓              ↓               ↓                 ↓         │
│   Documents     Chunks with     Embeddings      vector_store    │
│   with          metadata        via OpenAI          .json       │
│   metadata                                                       │
└─────────────────────────────────────────────────────────────────┘
```

## Step-by-Step Flow

### Step 1: Document Ingestion
```
documents/                    Document Objects
  ├── mcp_overview.md    →    ┌──────────────────┐
  ├── kotlin_guide.md    →    │ content: "..."   │
  └── rag_systems.md     →    │ metadata:        │
                               │  - source        │
                               │  - title         │
                               │  - fileType      │
                               │  - structure     │
                               └──────────────────┘
```

**Key Features:**
- ✓ Preserves headings (#, ##, ###)
- ✓ Detects lists (-, *, 1.)
- ✓ Counts code blocks
- ✓ Extracts title
- ✓ Adds metadata

### Step 2: Document Chunking
```
Document (1500 chars)
  │
  ├─→ Chunk 1 [0-500]        overlap →  ┌──────────┐
  │                                      │ "text..."│
  ├─→ Chunk 2 [400-900]      overlap →  │ metadata │
  │                                      │  - index │
  └─→ Chunk 3 [800-1300]     overlap →  │  - source│
                                          │  - headings
                                          └──────────┘
```

**Key Features:**
- ✓ Configurable chunk size (default: 500 chars)
- ✓ Overlap for context (default: 100 chars)
- ✓ Sentence boundary detection
- ✓ Preserves heading context
- ✓ Tracks position in document

### Step 3: Embedding Generation
```
Text Chunks                  OpenAI API                  Vectors
┌─────────────┐             ┌──────────┐             ┌──────────┐
│"MCP is a    │  ────────→  │ text-    │  ────────→  │[0.123,   │
│ protocol..." │             │embedding │             │ 0.456,   │
└─────────────┘             │-3-small  │             │ ...]     │
                            └──────────┘             └──────────┘
                               1536 dimensions
```

**Key Features:**
- ✓ Batch processing (20 chunks/batch)
- ✓ Rate limiting protection
- ✓ Progress reporting
- ✓ Error handling
- ✓ 1536-dimensional vectors

### Step 4: Vector Storage
```
Chunks with Embeddings              vector_store.json
┌────────────────────┐             ┌─────────────────────┐
│ Chunk 1 + [vector] │             │ {                   │
│ Chunk 2 + [vector] │  ────────→  │   "chunks": [...],  │
│ Chunk 3 + [vector] │             │   "metadata": {...} │
│ ...                │             │ }                   │
└────────────────────┘             └─────────────────────┘

In-Memory Index                    Similarity Search
┌────────────────┐                ┌──────────────────┐
│ ID → Chunk     │                │ Query Vector     │
│ ID → Vector    │   Cosine →     │      ↓           │
│ Metadata       │   Similarity   │ Top K Results    │
└────────────────┘                └──────────────────┘
```

**Key Features:**
- ✓ In-memory for fast search
- ✓ JSON persistence
- ✓ Cosine similarity
- ✓ Configurable top-K
- ✓ Similarity thresholds

## Data Flow Example

```
Input Document: "MCP Overview"
│
├─ Ingestion
│  └─ Output: Document{content="MCP is...", metadata={title="MCP Overview"}}
│
├─ Chunking  
│  ├─ Chunk 1: "MCP is a protocol..." [0-500]
│  ├─ Chunk 2: "...standardized protocol..." [400-900]  
│  └─ Chunk 3: "...connecting AI assistants..." [800-1300]
│
├─ Embedding
│  ├─ Chunk 1 → [0.123, 0.456, ..., 0.789] (1536 dims)
│  ├─ Chunk 2 → [0.234, 0.567, ..., 0.890] (1536 dims)
│  └─ Chunk 3 → [0.345, 0.678, ..., 0.901] (1536 dims)
│
└─ Storage
   └─ vector_store.json: 3 chunks with embeddings saved
```

## Query Flow

```
User Query: "What is MCP?"
     │
     ├─ Generate Query Embedding
     │  └─ OpenAI API → [0.111, 0.222, ..., 0.333]
     │
     ├─ Search Vector Store
     │  └─ Cosine Similarity with all chunks
     │
     ├─ Rank Results
     │  ├─ Chunk 5: similarity 0.92 ★★★★★
     │  ├─ Chunk 1: similarity 0.87 ★★★★☆
     │  └─ Chunk 9: similarity 0.81 ★★★★☆
     │
     └─ Return Top K Results
        └─ User sees most relevant chunks
```

## File Structure

```
mcp-server-demo/
├── src/main/kotlin/rag/
│   ├── Models.kt               # Data structures
│   ├── DocumentIngestion.kt    # Step 1 implementation
│   ├── DocumentChunking.kt     # Step 2 implementation
│   ├── EmbeddingService.kt     # Step 3 implementation
│   ├── VectorStore.kt          # Step 4 implementation
│   ├── RagPipeline.kt          # Orchestrator
│   └── RagPipelineTool.kt      # CLI tool
│
├── documents/                  # Input documents
│   ├── mcp_overview.md
│   ├── kotlin_guide.md
│   └── rag_systems.md
│
├── vector_store.json           # Generated output
├── RAG_README.md              # Documentation
└── test-rag-structure.sh      # Test script
```

## Configuration

```kotlin
// In RagPipeline constructor
val pipeline = RagPipeline(
    documentsPath = Paths.get("documents"),    // Where to read docs
    openAiApiKey = "sk-...",                   // Your API key
    vectorStorePath = "vector_store.json",     // Where to save
    chunkSize = 500,                           // Characters per chunk
    chunkOverlap = 100                         // Overlap size
)
```

## Performance Characteristics

```
Operation            Time         Memory      Scalability
─────────────────────────────────────────────────────────
Ingestion           Fast         Low         Hundreds of files
Chunking            Fast         Low         Thousands of chunks
Embedding           Slow*        Low         Limited by API rate
Search              Very Fast    Medium      Thousands of vectors
Storage (Save)      Fast         Low         MB-sized files
Storage (Load)      Fast         Medium      MB-sized files

* Depends on OpenAI API response time and rate limits
```

## Dependencies

```kotlin
// Core
kotlinx-serialization      # JSON serialization
kotlinx-coroutines        # Async operations

// HTTP Client
ktor-client-core          # HTTP client
ktor-client-cio           # CIO engine
ktor-client-content-negotiation  # JSON handling

// OpenAI API
text-embedding-3-small    # 1536 dimensions
```

## Future Enhancements

```
Current (Naive RAG)          →    Enhanced RAG
─────────────────────────────────────────────────────
In-memory vector store      →    Pinecone/Weaviate
Simple cosine similarity    →    Hybrid search (vector + BM25)
Fixed chunk size            →    Semantic chunking
Single embedding model      →    Multiple models / reranking
JSON persistence            →    Distributed database
Batch processing            →    Parallel processing
                                 Streaming ingestion
                                 Incremental updates
```

## Quick Commands

```bash
# Test structure
./test-rag-structure.sh

# Run pipeline
export OPENAI_API_KEY=your-key
./gradlew runRagPipeline

# Run with query
OPENAI_API_KEY=key ./gradlew runRagPipeline -Pargs="What is Kotlin?"

# Check output
cat vector_store.json | jq '.metadata'
```

## Success Metrics

```
✓ Documents Ingested:     3
✓ Chunks Created:         33
✓ Embeddings Generated:   33/33
✓ Storage:               vector_store.json (2.1 MB)
✓ Search Time:           <10ms for top-5 results
```
