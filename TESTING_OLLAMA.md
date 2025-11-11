# Testing Ollama Integration

## Quick Test

### 1. Check Current Behavior (Without Ollama)
```bash
./gradlew runRagPipeline
# Should show: "Using simple hash embeddings (Ollama not running)"
```

### 2. Install & Start Ollama
```bash
# Install (macOS)
brew install ollama

# Or download from https://ollama.com

# Start Ollama service
ollama serve
# Keep this running in terminal 1
```

### 3. Pull Embedding Model
```bash
# In terminal 2
ollama pull nomic-embed-text
# Downloads ~274MB model
```

### 4. Test Auto-Detection
```bash
# Should now show: "✓ Using Ollama embeddings (semantic, local, free!)"
./gradlew runRagPipeline
```

### 5. Test Semantic Retrieval
```bash
# This should now find Lapras correctly!
./gradlew runQuery -Pargs="What type is Lapras?"
```

## Expected Output

### Before Ollama:
```
ℹ️  Using simple hash embeddings (Ollama not running)
   Tip: Install Ollama for semantic embeddings:
   → https://ollama.com
   → ollama pull nomic-embed-text
```

### After Ollama:
```
✓ Using Ollama embeddings (semantic, local, free!)

[Step 3/4] Generating Embeddings
Using model: ollama:nomic-embed-text
✓ Generated embeddings for 70/70 chunks
```

### Query Results:
```
Query: "What type is Lapras?"
Retrieved: Lapras chunks! ✅
Similarity: 0.92, 0.88, 0.85 (meaningful scores)
```

## Troubleshooting

### Check if Ollama is Running
```bash
curl http://localhost:11434/api/tags
# Should return JSON with models list
```

### List Installed Models
```bash
ollama list
# Should show: nomic-embed-text
```

### Test Embedding API
```bash
curl http://localhost:11434/api/embeddings -d '{
  "model": "nomic-embed-text",
  "prompt": "test"
}'
# Should return embedding vector
```

## Model Options

```bash
# Recommended (balanced)
ollama pull nomic-embed-text    # 768 dims, 274MB

# Larger (best quality)
ollama pull mxbai-embed-large   # 1024 dims, 669MB

# Smaller (fastest)
ollama pull all-minilm          # 384 dims, 23MB
```
