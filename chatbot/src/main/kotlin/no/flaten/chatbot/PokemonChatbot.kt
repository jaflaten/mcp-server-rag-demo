package no.flaten.chatbot

import io.ktor.client.*
import kotlinx.coroutines.runBlocking

/**
 * Pokémon Expert Chatbot that combines Ollama LLM with RAG MCP server
 */
class PokemonChatbot(
    private val ollamaClient: OllamaClient,
    private val mcpClient: McpRagClient
) {
    private val conversationHistory = mutableListOf<Message>()
    
    private val systemPrompt = """
        You are a friendly and knowledgeable Pokémon expert assistant helping trainers.
        You have access to a knowledge base about Pokémon through a RAG (Retrieval Augmented Generation) system.
        
        When answering questions:
        1. Be friendly, encouraging, and enthusiastic about Pokémon
        2. Use the knowledge base information when available
        3. If you need to look up information, you'll receive it in the context
        4. Be concise but informative
        5. Help trainers understand evolution, types, and other Pokémon mechanics
        
        Your goal is to help trainers become better at understanding and training their Pokémon!
    """.trimIndent()

    fun chat(userMessage: String): String {
        // Add user message to history
        conversationHistory.add(Message("user", userMessage))
        
        // Check if we should query the knowledge base
        val ragContext = if (shouldQueryKnowledgeBase(userMessage)) {
            try {
                val ragResult = mcpClient.ragQuery(userMessage)
                "\n\n[Knowledge Base Information]:\n$ragResult"
            } catch (e: Exception) {
                "\n\n[Note: Could not retrieve information from knowledge base: ${e.message}]"
            }
        } else {
            ""
        }
        
        // Build prompt with conversation history and RAG context
        val prompt = buildPrompt(userMessage, ragContext)
        
        // Generate response using Ollama
        val response = runBlocking {
            ollamaClient.generate(
                prompt = prompt,
                system = systemPrompt,
                temperature = 0.7
            )
        }
        
        // Add assistant response to history
        conversationHistory.add(Message("assistant", response))
        
        // Keep only last 10 messages to avoid token limits
        if (conversationHistory.size > 10) {
            conversationHistory.removeAt(0)
            conversationHistory.removeAt(0)
        }
        
        return response
    }

    private fun shouldQueryKnowledgeBase(message: String): Boolean {
        // Simple heuristic: query if message contains question words or Pokémon-related terms
        val lowerMessage = message.lowercase()
        val questionWords = listOf("what", "which", "who", "where", "when", "how", "does", "is", "are", "can")
        val pokemonTerms = listOf("pokemon", "type", "evolve", "evolution", "move", "ability", "stat")
        
        return questionWords.any { lowerMessage.contains(it) } || 
               pokemonTerms.any { lowerMessage.contains(it) }
    }

    private fun buildPrompt(userMessage: String, ragContext: String): String {
        val historyContext = if (conversationHistory.size > 2) {
            "\n[Conversation History]:\n" + conversationHistory
                .takeLast(6) // Last 3 exchanges
                .joinToString("\n") { "${it.role}: ${it.content}" }
        } else {
            ""
        }
        
        return """
            $historyContext
            
            $ragContext
            
            User: $userMessage
            
            Assistant:
        """.trimIndent()
    }

    fun clearHistory() {
        conversationHistory.clear()
    }

    data class Message(
        val role: String,
        val content: String
    )
}
