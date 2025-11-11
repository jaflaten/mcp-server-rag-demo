package no.flaten

import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import no.flaten.rag.*
import java.io.File

/**
 * A simple "Hello World" MCP server that provides a greeting tool.
 */
fun main() {
    runHelloWorldMcpServer()
}

/**
 * Sanitizes user input to prevent injection attacks and ensure safe output.
 * - Removes control characters (newlines, tabs, etc.)
 * - Limits length to prevent DoS
 * - Trims whitespace
 */
fun sanitizeInput(input: String, maxLength: Int = 100): String {
    return input
        .replace(Regex("[\n\r\t]"), "") // Remove newlines, carriage returns, tabs
        .replace(Regex("[\\p{C}]"), "") // Remove other control characters
        .trim()
        .take(maxLength) // Limit length
}

fun runHelloWorldMcpServer() {
    // Initialize RAG components (lazy initialization)
    var ragQueryService: RagQueryService? = null
    
    fun getRagService(): RagQueryService {
        if (ragQueryService == null) {
            System.err.println("Initializing RAG service...")
            
            // Load vector store
            val vectorStore = VectorStore("vector_store.json")
            if (!vectorStore.load()) {
                throw Exception("Failed to load vector store. Please run the ingestion pipeline first.")
            }
            System.err.println("✓ Loaded ${vectorStore.size()} chunks from vector store")
            
            // Initialize embedding service (using Ollama)
            val embeddingProvider = OllamaEmbeddingProvider(
                model = "embeddinggemma:latest",
                baseUrl = "http://localhost:11434"
            )
            val embeddingService = EmbeddingService(embeddingProvider)
            
            // Initialize LLM provider (using simple provider since OpenAI quota exceeded)
            val llmProvider = SimpleLLMProvider()
            
            // Create RAG query service
            ragQueryService = RagQueryService(vectorStore, embeddingService, llmProvider)
            System.err.println("✓ RAG service initialized")
        }
        return ragQueryService!!
    }
    
    // Create the MCP Server instance
    val server = Server(
        serverInfo = Implementation(
            name = "hello-world-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                resources = ServerCapabilities.Resources(
                    subscribe = false,
                    listChanged = false
                )
            )
        )
    ) {
        "A RAG-powered MCP server with semantic search and knowledge retrieval capabilities"
    }

    // Add a simple "hello" tool that greets the user
    server.addTool(
        name = "hello",
        description = "Returns a friendly greeting message. You can optionally provide a name to personalize the greeting.",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("name") {
                    put("type", "string")
                    put("description", "The name of the person to greet (optional, max 50 characters)")
                    put("maxLength", 50)
                }
            },
            required = emptyList()
        )
    ) { request ->
        // Extract and sanitize the name parameter if provided
        val rawName = request.arguments["name"]?.jsonPrimitive?.content
        val name = rawName?.let { sanitizeInput(it, maxLength = 50) }
        
        // Validate that sanitized name is not empty (in case it was all control characters)
        val greeting = if (!name.isNullOrBlank()) {
            "Hello, $name! Welcome to the MCP Hello World server!"
        } else {
            "Hello, World! Welcome to the MCP Hello World server!"
        }
        
        // Return the greeting as a text content
        CallToolResult(
            content = listOf(TextContent(greeting))
        )
    }

    // Add an "echo" tool that echoes back the provided message
    server.addTool(
        name = "echo",
        description = "Echoes back the message you provide. This demonstrates basic parameter handling.",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("message") {
                    put("type", "string")
                    put("description", "The message to echo back (max 500 characters)")
                    put("maxLength", 500)
                }
            },
            required = listOf("message")
        )
    ) { request ->
        val rawMessage = request.arguments["message"]?.jsonPrimitive?.content
        
        if (rawMessage.isNullOrBlank()) {
            CallToolResult(
                content = listOf(TextContent("Error: The 'message' parameter is required and cannot be empty."))
            )
        } else {
            // Sanitize the message before echoing
            val sanitizedMessage = sanitizeInput(rawMessage, maxLength = 500)
            
            if (sanitizedMessage.isBlank()) {
                CallToolResult(
                    content = listOf(TextContent("Error: The message contains only invalid characters."))
                )
            } else {
                CallToolResult(
                    content = listOf(TextContent("Echo: $sanitizedMessage"))
                )
            }
        }
    }

    // Add a static resource - server information
    server.addResource(
        uri = "hello://server/info",
        name = "Server Information",
        description = "Information about this MCP server",
        mimeType = "text/plain"
    ) {
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = "hello://server/info",
                    mimeType = "text/plain",
                    text = """
                        MCP Hello World Server
                        ======================
                        Version: 1.0.0
                        
                        This is a demonstration MCP server written in Kotlin.
                        
                        Capabilities:
                        - Tools: hello, echo
                        - Resources: Server info, dynamic greeting
                        - Input sanitization for security
                        
                        Created as a learning example for the Model Context Protocol.
                    """.trimIndent()
                )
            )
        )
    }

    // Add a dynamic resource - personalized greeting
    server.addResource(
        uri = "hello://greetings/{name}",
        name = "Personalized Greeting",
        description = "Get a personalized greeting message (use {name} placeholder in URI)",
        mimeType = "text/plain"
    ) { request ->
        // Extract the name from the URI path
        val uriPath = request.uri.substringAfter("hello://greetings/")
        val rawName = if (uriPath.isNotBlank()) uriPath else "World"
        val sanitizedName = sanitizeInput(rawName, maxLength = 50)
        
        val greeting = if (sanitizedName.isNotBlank()) {
            """
                Hello, $sanitizedName!
                
                This is a dynamic resource generated just for you.
                
                Current timestamp: ${java.time.Instant.now()}
                Resource URI: ${request.uri}
                
                Have a wonderful day!
            """.trimIndent()
        } else {
            "Error: Invalid name provided"
        }
        
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = request.uri,
                    mimeType = "text/plain",
                    text = greeting
                )
            )
        )
    }

    // Add RAG query tool
    server.addTool(
        name = "rag_query",
        description = "Query the RAG knowledge base. Retrieves relevant information from documents and provides contextual answers. The system uses semantic search to find the most relevant content.",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("query") {
                    put("type", "string")
                    put("description", "The question or query to search for in the knowledge base")
                }
                putJsonObject("topK") {
                    put("type", "number")
                    put("description", "Number of relevant chunks to retrieve (default: 5)")
                    put("default", 5)
                }
                putJsonObject("minSimilarity") {
                    put("type", "number")
                    put("description", "Minimum similarity threshold 0.0-1.0 (default: 0.0)")
                    put("default", 0.0)
                }
            },
            required = listOf("query")
        )
    ) { request ->
        try {
            val query = request.arguments["query"]?.jsonPrimitive?.content
            if (query.isNullOrBlank()) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Error: The 'query' parameter is required and cannot be empty."))
                )
            }
            
            val topK = request.arguments["topK"]?.jsonPrimitive?.content?.toIntOrNull() ?: 5
            val minSimilarity = request.arguments["minSimilarity"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            
            // Get RAG service and perform query
            val ragService = getRagService()
            val response = runBlocking {
                ragService.query(query, topK, minSimilarity)
            }
            
            // Format response
            val formattedResponse = buildString {
                appendLine("Query: ${response.query}")
                appendLine()
                appendLine("Answer:")
                appendLine(response.answer)
                appendLine()
                appendLine("=" .repeat(60))
                appendLine("Sources (${response.retrievedChunks} chunks retrieved):")
                appendLine("-".repeat(60))
                
                response.sources.forEachIndexed { index, source ->
                    appendLine()
                    appendLine("[${index + 1}] ${source.title}")
                    appendLine("    Source: ${source.source}")
                    appendLine("    Similarity: ${String.format("%.3f", source.similarity)}")
                    appendLine("    Excerpt: ${source.excerpt}")
                }
            }
            
            CallToolResult(
                content = listOf(TextContent(formattedResponse))
            )
        } catch (e: Exception) {
            System.err.println("Error in rag_query: ${e.message}")
            e.printStackTrace()
            CallToolResult(
                content = listOf(TextContent("Error performing RAG query: ${e.message}"))
            )
        }
    }

    // Create stdio transport for communication
    val transport = StdioServerTransport(
        System.`in`.asInput(),
        System.out.asSink().buffered()
    )

    // Start the server
    runBlocking {
        val session = server.createSession(transport)
        val done = Job()
        session.onClose {
            done.complete()
        }
        done.join()
    }
}
