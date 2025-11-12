package no.flaten.chatbot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * MCP Client wrapper for connecting to the RAG MCP server
 * Uses stdio communication with JSON-RPC protocol
 */
class McpRagClient(
    private val jarPath: String
) {
    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var requestId = 0
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    fun connect() {
        val javaHome = System.getProperty("java.home")
        val javaCmd = "$javaHome/bin/java"
        
        val processBuilder = ProcessBuilder(javaCmd, "-jar", jarPath)
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT) // Show stderr
        
        process = processBuilder.start()
        writer = BufferedWriter(OutputStreamWriter(process!!.outputStream))
        reader = BufferedReader(InputStreamReader(process!!.inputStream))
        
        // Initialize the connection
        val initRequest = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", ++requestId)
            put("method", "initialize")
            putJsonObject("params") {
                put("protocolVersion", "2024-11-05")
                putJsonObject("capabilities") {}
                putJsonObject("clientInfo") {
                    put("name", "pokemon-chatbot")
                    put("version", "1.0.0")
                }
            }
        }
        
        sendRequest(initRequest)
        val initResponse = readResponse()
        
        if (initResponse["error"] != null) {
            throw Exception("Failed to initialize: ${initResponse["error"]}")
        }
    }

    fun ragQuery(query: String): String {
        val request = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", ++requestId)
            put("method", "tools/call")
            putJsonObject("params") {
                put("name", "rag_query")
                putJsonObject("arguments") {
                    put("query", query)
                }
            }
        }
        
        sendRequest(request)
        val response = readResponse()
        
        // Check for error
        if (response["error"] != null) {
            throw Exception("MCP Error: ${response["error"]}")
        }
        
        // Extract text from result
        val result = response["result"]?.jsonObject
        val content = result?.get("content")?.jsonArray
        val firstContent = content?.firstOrNull()?.jsonObject
        return firstContent?.get("text")?.jsonPrimitive?.content ?: "No results found"
    }

    private fun sendRequest(request: JsonObject) {
        val writer = writer ?: throw IllegalStateException("Client not connected")
        val requestJson = json.encodeToString(JsonObject.serializer(), request)
        writer.write(requestJson)
        writer.newLine()
        writer.flush()
    }

    private fun readResponse(): JsonObject {
        val reader = reader ?: throw IllegalStateException("Client not connected")
        val line = reader.readLine() ?: throw Exception("No response from server")
        return json.parseToJsonElement(line).jsonObject
    }

    fun close() {
        try {
            writer?.close()
            reader?.close()
            process?.destroy()
            process?.waitFor()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        process = null
        writer = null
        reader = null
    }
}
