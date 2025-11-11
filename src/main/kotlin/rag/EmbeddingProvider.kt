package no.flaten.rag

/**
 * Interface for embedding generation
 * Allows different implementations (OpenAI, local models, etc.)
 */
interface EmbeddingProvider {
    suspend fun generateEmbeddings(texts: List<String>): List<List<Double>>
    fun getDimension(): Int
    fun getModelName(): String
    fun close()
}
