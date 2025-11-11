package no.flaten.rag

/**
 * Interface for LLM providers
 * Allows different implementations (OpenAI, Ollama, Claude, etc.)
 */
interface LLMProvider {
    suspend fun generateResponse(prompt: String, context: String): String
    fun getModelName(): String
    fun close()
}
