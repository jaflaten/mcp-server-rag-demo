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
 * OpenAI embedding provider using text-embedding-3-small model
 */
class OpenAIEmbeddingProvider(
    private val apiKey: String,
    private val model: String = "text-embedding-3-small"
) : EmbeddingProvider {
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
    }
    
    override suspend fun generateEmbeddings(texts: List<String>): List<List<Double>> {
        val request = OpenAIEmbeddingRequest(
            input = texts,
            model = model
        )
        
        val response = try {
            val httpResponse = httpClient.post("https://api.openai.com/v1/embeddings") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            // Check if response is successful
            if (httpResponse.status.value !in 200..299) {
                val errorBody = httpResponse.body<String>()
                throw Exception("OpenAI API error (${httpResponse.status.value}): $errorBody")
            }
            
            httpResponse.body<OpenAIEmbeddingResponse>()
        } catch (e: Exception) {
            throw Exception("Failed to call OpenAI API: ${e.message}", e)
        }
        
        return response.data.map { it.embedding }
    }
    
    override fun getDimension(): Int = 1536  // text-embedding-3-small dimension
    
    override fun getModelName(): String = model
    
    override fun close() {
        httpClient.close()
    }
}

/**
 * OpenAI API Request
 */
@Serializable
private data class OpenAIEmbeddingRequest(
    val input: List<String>,
    val model: String
)

/**
 * OpenAI API Response
 */
@Serializable
private data class OpenAIEmbeddingResponse(
    val data: List<EmbeddingData>,
    val model: String,
    val usage: EmbeddingUsage
)

@Serializable
private data class EmbeddingData(
    val embedding: List<Double>,
    val index: Int,
    val `object`: String
)

@Serializable
private data class EmbeddingUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)
