package no.flaten.rag

/**
 * Simple in-memory embedding provider for testing without external dependencies
 * 
 * This creates deterministic "fake" embeddings based on text content.
 * Useful for:
 * - Testing the RAG pipeline structure
 * - Demos without API keys
 * - Development when API is unavailable
 * 
 * NOT suitable for production - embeddings won't capture semantic meaning!
 */
class SimpleEmbeddingProvider(
    private val dimension: Int = 384  // Smaller dimension for testing
) : EmbeddingProvider {
    
    override suspend fun generateEmbeddings(texts: List<String>): List<List<Double>> {
        return texts.map { text -> generateEmbedding(text) }
    }
    
    override fun getDimension(): Int = dimension
    
    override fun getModelName(): String = "simple-hash-embedding"
    
    override fun close() {
        // No resources to close
    }
    
    /**
     * Generates a deterministic embedding from text using a simple hash-based approach
     */
    private fun generateEmbedding(text: String): List<Double> {
        val normalized = text.lowercase().trim()
        val embedding = DoubleArray(dimension)
        
        // Use multiple hash functions to fill the embedding vector
        for (i in 0 until dimension) {
            val seed = i * 37 // Prime number for better distribution
            val hash = hashString(normalized, seed)
            
            // Normalize to [-1, 1] range
            embedding[i] = (hash % 2000 - 1000) / 1000.0
        }
        
        // Normalize the vector to unit length (like real embeddings)
        val magnitude = kotlin.math.sqrt(embedding.sumOf { it * it })
        if (magnitude > 0) {
            for (i in embedding.indices) {
                embedding[i] /= magnitude
            }
        }
        
        return embedding.toList()
    }
    
    /**
     * Simple hash function with seed for spreading values across dimensions
     */
    private fun hashString(text: String, seed: Int): Int {
        var hash = seed
        for (char in text) {
            hash = hash * 31 + char.code
            hash = hash xor (hash ushr 16)
        }
        return hash
    }
}
