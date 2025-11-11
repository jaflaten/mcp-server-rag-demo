package no.flaten.rag

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * OpenAI LLM provider using GPT models
 */
class OpenAILLMProvider(
    private val apiKey: String,
    private val model: String = "gpt-4o-mini"
) : LLMProvider {
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
    }
    
    override suspend fun generateResponse(prompt: String, context: String): String {
        val systemPrompt = """
            You are a helpful assistant that answers questions based on the provided context.
            Use only the information from the context to answer the question.
            If the context doesn't contain enough information, say so.
            Be concise and accurate.
        """.trimIndent()
        
        val userPrompt = """
            Context:
            $context
            
            Question: $prompt
            
            Answer:
        """.trimIndent()
        
        val request = OpenAIChatRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(role = "user", content = userPrompt)
            ),
            temperature = 0.7,
            maxTokens = 500
        )
        
        return try {
            val httpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (httpResponse.status.value !in 200..299) {
                val errorBody = httpResponse.body<String>()
                throw Exception("OpenAI API error (${httpResponse.status.value}): $errorBody")
            }
            
            val response = httpResponse.body<OpenAIChatResponse>()
            response.choices.firstOrNull()?.message?.content 
                ?: "No response generated"
                
        } catch (e: Exception) {
            throw Exception("Failed to call OpenAI API: ${e.message}", e)
        }
    }
    
    override fun getModelName(): String = model
    
    override fun close() {
        httpClient.close()
    }
}

@Serializable
private data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens") val maxTokens: Int? = null
)

@Serializable
private data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
private data class OpenAIChatResponse(
    val choices: List<Choice>,
    val usage: ChatUsage
)

@Serializable
private data class Choice(
    val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String
)

@Serializable
private data class ChatUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)
