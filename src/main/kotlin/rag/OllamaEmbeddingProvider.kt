package no.flaten.rag

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Ollama embedding provider using local models
 * Supports models like embeddinggemma, nomic-embed-text, mxbai-embed-large, all-minilm, etc.
 * 
 * Install Ollama: https://ollama.com
 * Pull model: ollama pull embeddinggemma:latest
 */
class OllamaEmbeddingProvider(
    private val model: String = "embeddinggemma:latest",
    private val baseUrl: String = "http://localhost:11434"
) : EmbeddingProvider {
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
        // Long timeout for local inference
        engine {
            requestTimeout = 60_000
        }
    }
    
    override suspend fun generateEmbeddings(texts: List<String>): List<List<Double>> {
        return texts.map { text ->
            generateSingleEmbedding(text)
        }
    }
    
    private suspend fun generateSingleEmbedding(text: String): List<Double> {
        val request = OllamaEmbeddingRequest(
            model = model,
            prompt = text
        )
        
        return try {
            val httpResponse = httpClient.post("$baseUrl/api/embeddings") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (httpResponse.status.value !in 200..299) {
                val errorBody = httpResponse.bodyAsText()
                throw Exception("Ollama API error (${httpResponse.status.value}): $errorBody")
            }
            
            val response = httpResponse.body<OllamaEmbeddingResponse>()
            response.embedding
            
        } catch (e: Exception) {
            throw Exception("Failed to call Ollama API: ${e.message}\n" +
                "Make sure Ollama is running and model '$model' is installed:\n" +
                "  ollama pull $model", e)
        }
    }
    
    override fun getDimension(): Int {
        return when (model) {
            "embeddinggemma:latest", "embeddinggemma" -> 768
            "all-minilm" -> 384
            "nomic-embed-text" -> 768
            "mxbai-embed-large" -> 1024
            else -> 768  // Default for most modern models
        }
    }
    
    override fun getModelName(): String = "ollama:$model"
    
    override fun close() {
        httpClient.close()
    }
    
    companion object {
        /**
         * Check if Ollama is available
         */
        suspend fun isAvailable(baseUrl: String = "http://localhost:11434"): Boolean {
            return try {
                val client = HttpClient(CIO)
                val response = client.get("$baseUrl/api/tags")
                client.close()
                response.status.value in 200..299
            } catch (e: Exception) {
                false
            }
        }
    }
}

@Serializable
private data class OllamaEmbeddingRequest(
    val model: String,
    val prompt: String
)

@Serializable
private data class OllamaEmbeddingResponse(
    val embedding: List<Double>
)
