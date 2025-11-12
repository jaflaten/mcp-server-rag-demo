package no.flaten.chatbot

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    // Configuration - determine correct path to main project JAR
    val workingDir = File(System.getProperty("user.dir"))
    val projectRoot = if (workingDir.name == "chatbot") workingDir.parentFile else workingDir
    val jarPath = File(projectRoot, "build/libs/mcp-server-demo-all.jar").absolutePath
    
    val ollamaModel = System.getenv("OLLAMA_MODEL") ?: "llama3.2"
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    
    println("================================================================================")
    println("Pokémon Expert Chatbot")
    println("================================================================================")
    println("MCP Server JAR: $jarPath")
    println("Ollama Model: $ollamaModel")
    println("Port: $port")
    println()
    
    // Verify JAR exists
    if (!File(jarPath).exists()) {
        println("❌ Error: MCP Server JAR not found at: $jarPath")
        println("Please build the main project first: ./gradlew build")
        return
    }
    
    // Create HTTP client
    val httpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
    }
    
    // Create components
    val ollamaClient = OllamaClient(httpClient, model = ollamaModel)
    val mcpClient = McpRagClient(jarPath)
    
    // Connect to MCP server
    println("Connecting to MCP RAG server...")
    try {
        mcpClient.connect()
        println("✓ Connected to MCP server")
    } catch (e: Exception) {
        println("❌ Failed to connect to MCP server: ${e.message}")
        e.printStackTrace()
        return
    }
    
    val chatbot = PokemonChatbot(ollamaClient, mcpClient)
    
    println()
    println("================================================================================")
    println("🚀 Server starting at http://localhost:$port")
    println("================================================================================")
    println()
    
    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        
        routing {
            // Home page
            get("/") {
                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title("Pokémon Expert Chatbot")
                        style {
                            unsafe {
                                raw("""
                                    * {
                                        margin: 0;
                                        padding: 0;
                                        box-sizing: border-box;
                                    }
                                    body {
                                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                        height: 100vh;
                                        display: flex;
                                        justify-content: center;
                                        align-items: center;
                                        padding: 20px;
                                    }
                                    .container {
                                        background: white;
                                        border-radius: 20px;
                                        box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                                        width: 100%;
                                        max-width: 800px;
                                        height: 90vh;
                                        max-height: 700px;
                                        display: flex;
                                        flex-direction: column;
                                        overflow: hidden;
                                    }
                                    .header {
                                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                        color: white;
                                        padding: 20px;
                                        text-align: center;
                                        border-radius: 20px 20px 0 0;
                                    }
                                    .header h1 {
                                        font-size: 24px;
                                        font-weight: 600;
                                        margin-bottom: 5px;
                                    }
                                    .header p {
                                        font-size: 14px;
                                        opacity: 0.9;
                                    }
                                    #messages {
                                        flex: 1;
                                        overflow-y: auto;
                                        padding: 20px;
                                        background: #f5f5f5;
                                    }
                                    .message {
                                        margin-bottom: 15px;
                                        animation: fadeIn 0.3s;
                                    }
                                    @keyframes fadeIn {
                                        from { opacity: 0; transform: translateY(10px); }
                                        to { opacity: 1; transform: translateY(0); }
                                    }
                                    .message.user {
                                        display: flex;
                                        justify-content: flex-end;
                                    }
                                    .message.assistant {
                                        display: flex;
                                        justify-content: flex-start;
                                    }
                                    .message-content {
                                        max-width: 70%;
                                        padding: 12px 16px;
                                        border-radius: 18px;
                                        word-wrap: break-word;
                                        white-space: pre-wrap;
                                    }
                                    .message.user .message-content {
                                        background: #667eea;
                                        color: white;
                                        border-bottom-right-radius: 4px;
                                    }
                                    .message.assistant .message-content {
                                        background: white;
                                        color: #333;
                                        border-bottom-left-radius: 4px;
                                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                                    }
                                    .input-area {
                                        padding: 20px;
                                        background: white;
                                        border-top: 1px solid #e0e0e0;
                                        display: flex;
                                        gap: 10px;
                                    }
                                    #userInput {
                                        flex: 1;
                                        padding: 12px 16px;
                                        border: 2px solid #e0e0e0;
                                        border-radius: 25px;
                                        font-size: 14px;
                                        outline: none;
                                        transition: border-color 0.3s;
                                    }
                                    #userInput:focus {
                                        border-color: #667eea;
                                    }
                                    button {
                                        padding: 12px 24px;
                                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                        color: white;
                                        border: none;
                                        border-radius: 25px;
                                        font-size: 14px;
                                        font-weight: 600;
                                        cursor: pointer;
                                        transition: transform 0.2s, box-shadow 0.2s;
                                    }
                                    button:hover {
                                        transform: translateY(-2px);
                                        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                                    }
                                    button:active {
                                        transform: translateY(0);
                                    }
                                    button:disabled {
                                        opacity: 0.5;
                                        cursor: not-allowed;
                                        transform: none;
                                    }
                                    .loading {
                                        display: inline-block;
                                        width: 12px;
                                        height: 12px;
                                        border: 2px solid #667eea;
                                        border-top-color: transparent;
                                        border-radius: 50%;
                                        animation: spin 0.6s linear infinite;
                                    }
                                    @keyframes spin {
                                        to { transform: rotate(360deg); }
                                    }
                                """.trimIndent())
                            }
                        }
                    }
                    body {
                        div(classes = "container") {
                            div(classes = "header") {
                                h1 { +"⚡ Pokémon Expert Chatbot" }
                                p { +"Your friendly AI assistant for all things Pokémon!" }
                            }
                            div {
                                id = "messages"
                            }
                            div(classes = "input-area") {
                                input {
                                    id = "userInput"
                                    type = InputType.text
                                    placeholder = "Ask me about Pokémon... (e.g., What type is Lapras?)"
                                    attributes["autocomplete"] = "off"
                                }
                                button {
                                    id = "sendBtn"
                                    +"Send"
                                }
                            }
                        }
                        script {
                            unsafe {
                                raw("""
                                    const messagesDiv = document.getElementById('messages');
                                    const userInput = document.getElementById('userInput');
                                    const sendBtn = document.getElementById('sendBtn');
                                    
                                    function addMessage(content, role) {
                                        const messageDiv = document.createElement('div');
                                        messageDiv.className = 'message ' + role;
                                        
                                        const contentDiv = document.createElement('div');
                                        contentDiv.className = 'message-content';
                                        contentDiv.textContent = content;
                                        
                                        messageDiv.appendChild(contentDiv);
                                        messagesDiv.appendChild(messageDiv);
                                        messagesDiv.scrollTop = messagesDiv.scrollHeight;
                                    }
                                    
                                    async function sendMessage() {
                                        const message = userInput.value.trim();
                                        if (!message) return;
                                        
                                        addMessage(message, 'user');
                                        userInput.value = '';
                                        sendBtn.disabled = true;
                                        sendBtn.innerHTML = '<span class="loading"></span>';
                                        
                                        try {
                                            const response = await fetch('/chat', {
                                                method: 'POST',
                                                headers: {
                                                    'Content-Type': 'application/json'
                                                },
                                                body: JSON.stringify({ message: message })
                                            });
                                            
                                            const data = await response.json();
                                            addMessage(data.response, 'assistant');
                                        } catch (error) {
                                            addMessage('Sorry, I encountered an error: ' + error.message, 'assistant');
                                        } finally {
                                            sendBtn.disabled = false;
                                            sendBtn.textContent = 'Send';
                                        }
                                    }
                                    
                                    sendBtn.addEventListener('click', sendMessage);
                                    userInput.addEventListener('keypress', (e) => {
                                        if (e.key === 'Enter') sendMessage();
                                    });
                                    
                                    // Welcome message
                                    addMessage("Hi! I'm your Pokémon Expert assistant! Ask me anything about Pokémon - types, evolutions, and more!", 'assistant');
                                """.trimIndent())
                            }
                        }
                    }
                }
            }
            
            // Chat API endpoint
            post("/chat") {
                val request = call.receive<ChatRequest>()
                
                try {
                    val response = runBlocking {
                        chatbot.chat(request.message)
                    }
                    call.respond(ChatResponse(response = response))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ChatResponse(response = "Sorry, I encountered an error: ${e.message}")
                    )
                }
            }
            
            // Clear history endpoint
            post("/clear") {
                chatbot.clearHistory()
                call.respond(HttpStatusCode.OK, mapOf("status" to "cleared"))
            }
        }
    }.start(wait = true)
}

@Serializable
data class ChatRequest(
    val message: String
)

@Serializable
data class ChatResponse(
    val response: String
)
