package no.flaten.rag

/**
 * Step 3: Embedding Generation
 * 
 * Orchestrates embedding generation using pluggable providers
 */
class EmbeddingService(
    private val provider: EmbeddingProvider
) {
    
    /**
     * Generates embeddings for a list of document chunks
     * 
     * @param chunks The chunks to embed
     * @return Chunks with embeddings populated
     */
    suspend fun embedChunks(chunks: List<DocumentChunk>): List<DocumentChunk> {
        val embeddings = mutableListOf<DocumentChunk>()
        
        // Process in batches to avoid memory issues
        val batchSize = 20
        val batches = chunks.chunked(batchSize)
        
        System.err.println("Generating embeddings for ${chunks.size} chunks in ${batches.size} batches...")
        System.err.println("Using model: ${provider.getModelName()}")
        
        batches.forEachIndexed { batchIndex, batch ->
            try {
                val embeddedBatch = embedBatch(batch)
                embeddings.addAll(embeddedBatch)
                System.err.println("✓ Batch ${batchIndex + 1}/${batches.size} complete")
                
                // Small delay between batches
                if (batchIndex < batches.size - 1) {
                    kotlinx.coroutines.delay(100)
                }
            } catch (e: Exception) {
                System.err.println("✗ Failed to embed batch ${batchIndex + 1}: ${e.message}")
                // Add chunks without embeddings so we don't lose data
                embeddings.addAll(batch)
            }
        }
        
        return embeddings
    }
    
    /**
     * Embeds a batch of chunks
     */
    private suspend fun embedBatch(chunks: List<DocumentChunk>): List<DocumentChunk> {
        // Extract texts to embed
        val texts = chunks.map { it.content }
        
        // Get embeddings from provider
        val embeddings = provider.generateEmbeddings(texts)
        
        // Match embeddings back to chunks
        return chunks.mapIndexed { index, chunk ->
            val embedding = embeddings.getOrNull(index)
            chunk.copy(embedding = embedding)
        }
    }
    
    /**
     * Cleans up resources
     */
    fun close() {
        provider.close()
    }
    
    /**
     * Gets the embedding dimension
     */
    fun getDimension(): Int = provider.getDimension()
    
    /**
     * Gets the model name
     */
    fun getModelName(): String = provider.getModelName()
}

/**
 * Factory methods for creating embedding services
 */
object EmbeddingServiceFactory {
    
    /**
     * Creates a service using OpenAI
     */
    fun createOpenAI(apiKey: String, model: String = "text-embedding-3-small"): EmbeddingService {
        return EmbeddingService(OpenAIEmbeddingProvider(apiKey, model))
    }
    
    /**
     * Creates a service using simple hash-based embeddings (for testing)
     */
    fun createSimple(dimension: Int = 384): EmbeddingService {
        return EmbeddingService(SimpleEmbeddingProvider(dimension))
    }
    
    /**
     * Creates a service using Ollama (local, semantic, free!)
     */
    fun createOllama(model: String = "embeddinggemma:latest"): EmbeddingService {
        return EmbeddingService(OllamaEmbeddingProvider(model))
    }
    
    /**
     * Auto-select best available embedding provider
     */
    suspend fun createBestAvailable(openAiApiKey: String? = null): EmbeddingService {
        // Priority: Ollama > Simple (OpenAI disabled for testing)
        
        if (OllamaEmbeddingProvider.isAvailable()) {
            System.err.println("✓ Using Ollama embeddings (embeddinggemma, semantic, local, free!)")
            return createOllama()
        }
        
        System.err.println("ℹ️  Using simple hash embeddings (Ollama not running)")
        System.err.println("   Tip: Start Ollama for semantic embeddings:")
        System.err.println("   → ollama serve")
        System.err.println("   → ollama pull all-minilm")
        return createSimple()
    }
}
