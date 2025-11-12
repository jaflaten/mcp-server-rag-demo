package no.flaten.chatbot

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client for Ollama LLM API
 */
class OllamaClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://localhost:11434",
    private val model: String = "llama3.2"
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    suspend fun generate(
        prompt: String,
        system: String? = null,
        temperature: Double = 0.7
    ): String {
        val request = OllamaGenerateRequest(
            model = model,
            prompt = prompt,
            system = system,
            stream = true,  // Use streaming to handle responses properly
            options = OllamaOptions(temperature = temperature)
        )

        val response: HttpResponse = httpClient.post("$baseUrl/api/generate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Ollama API error (${response.status.value}): ${response.bodyAsText()}")
        }

        // Handle streaming response - collect all chunks
        val fullText = StringBuilder()
        val responseText = response.bodyAsText()
        
        // Parse each line as a separate JSON object
        responseText.lines().forEach { line ->
            if (line.isNotBlank()) {
                try {
                    val chunk = json.decodeFromString<OllamaGenerateResponse>(line)
                    fullText.append(chunk.response)
                } catch (e: Exception) {
                    // Skip malformed lines
                }
            }
        }
        
        return fullText.toString()
    }

    @Serializable
    data class OllamaGenerateRequest(
        val model: String,
        val prompt: String,
        val system: String? = null,
        val stream: Boolean = false,
        val options: OllamaOptions? = null
    )

    @Serializable
    data class OllamaOptions(
        val temperature: Double
    )

    @Serializable
    data class OllamaGenerateResponse(
        val model: String,
        val response: String,
        val done: Boolean
    )
}
