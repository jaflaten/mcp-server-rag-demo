package no.flaten.rag

/**
 * Simple mock LLM for testing without API
 * Returns a formatted response with retrieved chunks
 */
class SimpleLLMProvider : LLMProvider {
    
    override suspend fun generateResponse(prompt: String, context: String): String {
        return buildString {
            appendLine("Based on the retrieved context, here's what I found:")
            appendLine()
            appendLine("Query: $prompt")
            appendLine()
            appendLine("Relevant Information:")
            appendLine(context)
            appendLine()
            appendLine("(Note: Using SimpleLLMProvider - set OPENAI_API_KEY for AI-generated responses)")
        }
    }
    
    override fun getModelName(): String = "simple-mock-llm"
    
    override fun close() {
        // No resources to close
    }
}
