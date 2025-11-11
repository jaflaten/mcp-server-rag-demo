package no.flaten.rag

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import kotlin.math.sqrt

/**
 * Step 4: In-Memory Vector Store
 * 
 * Stores document chunks with their embeddings in memory
 * and provides similarity search capabilities.
 * Can be persisted to/loaded from JSON file.
 */
class VectorStore(
    private val storagePath: String = "vector_store.json"
) {
    private val chunks = mutableListOf<DocumentChunk>()
    private var metadata: VectorStoreMetadata? = null
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Adds chunks to the vector store
     */
    fun addChunks(newChunks: List<DocumentChunk>) {
        chunks.addAll(newChunks)
        System.err.println("✓ Added ${newChunks.size} chunks to vector store (total: ${chunks.size})")
    }
    
    /**
     * Searches for similar chunks using cosine similarity
     * 
     * @param queryEmbedding The query vector
     * @param topK Number of results to return
     * @param minSimilarity Minimum similarity threshold (0.0 to 1.0)
     * @return List of chunks with similarity scores
     */
    fun search(
        queryEmbedding: List<Double>,
        topK: Int = 5,
        minSimilarity: Double = 0.0
    ): List<SearchResult> {
        if (chunks.isEmpty()) {
            return emptyList()
        }
        
        // Calculate similarities
        val results = chunks
            .filter { it.embedding != null }
            .map { chunk ->
                val similarity = cosineSimilarity(queryEmbedding, chunk.embedding!!)
                SearchResult(chunk, similarity)
            }
            .filter { it.similarity >= minSimilarity }
            .sortedByDescending { it.similarity }
            .take(topK)
        
        return results
    }
    
    /**
     * Calculates cosine similarity between two vectors
     */
    private fun cosineSimilarity(a: List<Double>, b: List<Double>): Double {
        require(a.size == b.size) { "Vectors must have the same dimension" }
        
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        
        normA = sqrt(normA)
        normB = sqrt(normB)
        
        return if (normA == 0.0 || normB == 0.0) {
            0.0
        } else {
            dotProduct / (normA * normB)
        }
    }
    
    /**
     * Saves the vector store to a JSON file
     */
    fun save(
        embeddingModel: String = "text-embedding-3-small",
        chunkSize: Int = 500
    ) {
        val sources = chunks.map { it.metadata.source }.distinct()
        
        val storeMetadata = VectorStoreMetadata(
            createdAt = Instant.now().toString(),
            embeddingModel = embeddingModel,
            chunkSize = chunkSize,
            totalChunks = chunks.size,
            sources = sources
        )
        
        val entry = VectorStoreEntry(
            chunks = chunks,
            metadata = storeMetadata
        )
        
        try {
            val jsonString = json.encodeToString(entry)
            File(storagePath).writeText(jsonString)
            System.err.println("✓ Vector store saved to $storagePath (${chunks.size} chunks)")
        } catch (e: Exception) {
            System.err.println("✗ Failed to save vector store: ${e.message}")
            throw e
        }
    }
    
    /**
     * Loads the vector store from a JSON file
     */
    fun load(): Boolean {
        val file = File(storagePath)
        
        if (!file.exists()) {
            System.err.println("Vector store file not found: $storagePath")
            return false
        }
        
        return try {
            val jsonString = file.readText()
            val entry = json.decodeFromString<VectorStoreEntry>(jsonString)
            
            chunks.clear()
            chunks.addAll(entry.chunks)
            metadata = entry.metadata
            
            System.err.println("✓ Loaded vector store from $storagePath (${chunks.size} chunks)")
            true
        } catch (e: Exception) {
            System.err.println("✗ Failed to load vector store: ${e.message}")
            false
        }
    }
    
    /**
     * Gets the number of chunks in the store
     */
    fun size(): Int = chunks.size
    
    /**
     * Checks if the store is empty
     */
    fun isEmpty(): Boolean = chunks.isEmpty()
    
    /**
     * Gets all chunks (for debugging/inspection)
     */
    fun getAllChunks(): List<DocumentChunk> = chunks.toList()
    
    /**
     * Clears all data from the store
     */
    fun clear() {
        chunks.clear()
        metadata = null
        System.err.println("✓ Vector store cleared")
    }
    
    /**
     * Gets store metadata
     */
    fun getMetadata(): VectorStoreMetadata? = metadata
}

/**
 * Search result with similarity score
 */
data class SearchResult(
    val chunk: DocumentChunk,
    val similarity: Double
)
