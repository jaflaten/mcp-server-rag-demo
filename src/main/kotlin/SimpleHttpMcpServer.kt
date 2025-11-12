package no.flaten

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import no.flaten.rag.*
import java.io.File

/**
 * Simple HTTP/JSON-RPC MCP server
 * This exposes the MCP tools via HTTP POST endpoints
 */
fun main() {
    runSimpleHttpMcpServer()
}

fun runSimpleHttpMcpServer() {
    println("Starting HTTP MCP Server on http://0.0.0.0:8080")
    println("Available endpoints:")
    println("  GET  /health        - Health check")
    println("  POST /mcp/tools     - List available tools")
    println("  POST /mcp/call      - Call a tool")
    println("  GET  /mcp/resources - List resources")
    
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
        configureSimpleMcpRouting()
    }.start(wait = true)
}

fun Application.configureSimpleMcpRouting() {
    // Initialize RAG service
    val ragService = lazy {
        log.info("Initializing RAG service...")
        
        val vectorStorePath = "/Users/Jorn-Are.Klubben.Flaten/dev/solo/mcp-server-demo/vector_store.json"
        val vectorStoreFile = File(vectorStorePath)
        
        if (!vectorStoreFile.exists()) {
            throw Exception("Vector store not found at $vectorStorePath")
        }
        
        val vectorStore = VectorStore(vectorStorePath)
        if (!vectorStore.load()) {
            throw Exception("Failed to load vector store")
        }
        log.info("✓ Loaded ${vectorStore.size()} chunks")
        
        val embeddingProvider = OllamaEmbeddingProvider(
            model = "embeddinggemma:latest",
            baseUrl = "http://localhost:11434"
        )
        val embeddingService = EmbeddingService(embeddingProvider)
        val llmProvider = SimpleLLMProvider()
        
        RagQueryService(vectorStore, embeddingService, llmProvider).also {
            log.info("✓ RAG service ready")
        }
    }

    routing {
        // Health check
        get("/health") {
            call.respondText("OK - MCP Server Running", ContentType.Text.Plain)
        }

        // List available tools
        get("/mcp/tools") {
            val tools = listOf(
                mapOf(
                    "name" to "hello",
                    "description" to "Returns a friendly greeting",
                    "inputSchema" to mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "name" to mapOf(
                                "type" to "string",
                                "description" to "Name to greet (optional)"
                            )
                        )
                    )
                ),
                mapOf(
                    "name" to "rag_query",
                    "description" to "Query the RAG knowledge base",
                    "inputSchema" to mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "query" to mapOf(
                                "type" to "string",
                                "description" to "The question to search for"
                            ),
                            "topK" to mapOf(
                                "type" to "number",
                                "description" to "Number of results (default: 5)"
                            )
                        ),
                        "required" to listOf("query")
                    )
                )
            )
            call.respond(mapOf("tools" to tools))
        }

        // Call a tool
        post("/mcp/call") {
            try {
                val requestJson = call.receive<JsonObject>()
                
                val toolName = requestJson["tool"]?.jsonPrimitive?.content
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing 'tool' field"))
                
                val arguments = requestJson["arguments"]?.jsonObject ?: JsonObject(emptyMap())
                
                val result = when (toolName) {
                    "hello" -> {
                        val name = arguments["name"]?.jsonPrimitive?.content?.let {
                            sanitizeInput(it, 50)
                        }
                        val greeting = if (!name.isNullOrBlank()) {
                            "Hello, $name! Welcome to the HTTP MCP server!"
                        } else {
                            "Hello! Welcome to the HTTP MCP server!"
                        }
                        mapOf("content" to listOf(mapOf("type" to "text", "text" to greeting)))
                    }
                    
                    "rag_query" -> {
                        val query = arguments["query"]?.jsonPrimitive?.content
                            ?: return@post call.respond(HttpStatusCode.BadRequest, 
                                mapOf("error" to "Missing 'query' argument"))
                        
                        val topK = arguments["topK"]?.jsonPrimitive?.content?.toIntOrNull() ?: 5
                        val minSimilarity = arguments["minSimilarity"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                        
                        val response = runBlocking {
                            ragService.value.query(query, topK, minSimilarity)
                        }
                        
                        val formattedResponse = buildString {
                            appendLine("Query: ${response.query}")
                            appendLine()
                            appendLine("Answer: ${response.answer}")
                            appendLine()
                            appendLine("Sources (${response.retrievedChunks} chunks):")
                            response.sources.forEachIndexed { i, s ->
                                appendLine("  [${i+1}] ${s.title} (${String.format("%.3f", s.similarity)})")
                                appendLine("      ${s.excerpt}")
                            }
                        }
                        
                        mapOf("content" to listOf(mapOf("type" to "text", "text" to formattedResponse)))
                    }
                    
                    else -> return@post call.respond(HttpStatusCode.NotFound, 
                        mapOf("error" to "Tool '$toolName' not found"))
                }
                
                call.respond(result)
            } catch (e: Exception) {
                log.error("Error calling tool", e)
                call.respond(HttpStatusCode.InternalServerError, 
                    mapOf("error" to (e.message ?: "Unknown error")))
            }
        }

        // List resources
        get("/mcp/resources") {
            val resources = listOf(
                mapOf(
                    "uri" to "hello://server/info",
                    "name" to "Server Information",
                    "mimeType" to "text/plain"
                )
            )
            call.respond(mapOf("resources" to resources))
        }
    }
}
