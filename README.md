# MCP Server Demo - Hello World

A simple "Hello World" Model Context Protocol (MCP) server implementation using the [Kotlin MCP SDK](https://github.com/modelcontextprotocol/kotlin-sdk).

## What is MCP?

The Model Context Protocol (MCP) is a standardized protocol that allows applications to provide context for Large Language Models (LLMs) in a consistent way. It separates the concerns of providing context from the actual LLM interaction, enabling better modularity and reusability.

## Project Overview

This project demonstrates:

### MCP Server Features
- **2 Tools**: hello (greeting), echo (message repeating)
- **2 Resources**: Server info, dynamic greetings
- **Input Sanitization**: Protection against injection attacks

### RAG Pipeline (NEW!)
A complete Retrieval-Augmented Generation system with:
- **Document Ingestion**: Read and parse Markdown/Text files
- **Smart Chunking**: Sentence-aware splitting with overlap
- **Vector Embeddings**: OpenAI text-embedding-3-small
- **Vector Storage**: In-memory database with JSON persistence

See [RAG_README.md](RAG_README.md) for complete RAG documentation.

## Prerequisites

- Java 17 or higher
- Gradle (included via wrapper)

## Building the Project

```bash
./gradlew clean build
```

## Running the Server

You can run the MCP server in two modes:

### 1. Stdio Mode (for Claude Desktop, MCP Inspector)

The server uses STDIO transport for communication with MCP clients:

```bash
./gradlew run
```

Or run the compiled JAR:

```bash
java -jar build/libs/mcp-server-demo-0.0.1-all.jar
```

### 2. HTTP Mode (for Web Applications, APIs)

Run as an HTTP server on port 8080:

```bash
./gradlew runHttpServer
```

Then access via HTTP:
```bash
# Health check
curl http://localhost:8080/health

# Call a tool
curl -X POST -H "Content-Type: application/json" \
  -d '{"tool":"rag_query","arguments":{"query":"What type is Lapras?"}}' \
  http://localhost:8080/mcp/call
```

See [HTTP_MCP_SERVER.md](HTTP_MCP_SERVER.md) for complete HTTP API documentation.

## Available Tools

### 1. hello

Greets the user with an optional personalized message.

**Parameters:**
- `name` (optional): The name of the person to greet

**Examples:**
- Without name: Returns "Hello, World! Welcome to the MCP Hello World server!"
- With name: Returns "Hello, [name]! Welcome to the MCP Hello World server!"

### 2. echo

Echoes back the message you provide.

**Parameters:**
- `message` (required): The message to echo back

**Example:**
- Input: "Test message"
- Output: "Echo: Test message"

## Available Resources

### 1. Server Information

Static resource providing information about the server.

**URI:** `hello://server/info`  
**Type:** text/plain  
**Content:** Server version, capabilities, and description

### 2. Personalized Greeting

Dynamic resource that generates personalized greeting messages.

**URI Pattern:** `hello://greetings/{name}`  
**Type:** text/plain  
**Example URIs:**
- `hello://greetings/Alice` - Greeting for Alice
- `hello://greetings/Bob` - Greeting for Bob

**Content:** Personalized greeting with timestamp

## Project Structure

```
.
├── build.gradle.kts              # Gradle build configuration
├── src/
│   └── main/
│       └── kotlin/
│           └── HelloWorldMcpServer.kt  # Main MCP server implementation
└── README.md                      # This file
```

## Key Components

### Server Setup

The server is configured with:
- **Name**: hello-world-server
- **Version**: 1.0.0
- **Capabilities**: 
  - Tools with list change notification support
  - Resources (static and dynamic)
- **Transport**: STDIO (Standard Input/Output)
- **Security**: Input sanitization and validation (see [SECURITY.md](SECURITY.md))

### Code Highlights

```kotlin
// Creating the server
val server = Server(
    serverInfo = Implementation(
        name = "hello-world-server",
        version = "1.0.0"
    ),
    options = ServerOptions(
        capabilities = ServerCapabilities(
            tools = ServerCapabilities.Tools(listChanged = true)
        )
    )
) {
    "A simple Hello World MCP server demonstrating tool capabilities"
}

// Adding a tool
server.addTool(
    name = "hello",
    description = "Returns a friendly greeting message...",
    inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("name") {
                put("type", "string")
                put("description", "The name of the person to greet (optional)")
            }
        },
        required = emptyList()
    )
) { request ->
    // Tool implementation
}
```

## Dependencies

- **Kotlin MCP SDK** (0.7.7): Core MCP protocol implementation
- **Ktor**: HTTP client and server framework
- **Kotlinx IO**: Modern IO primitives for Kotlin
- **Kotlinx Serialization**: JSON handling

## Security

This server implements input sanitization to protect against common vulnerabilities:
- Control character filtering (newlines, tabs, etc.)
- Length limits to prevent DoS attacks
- Whitespace trimming
- Post-sanitization validation

For detailed information, see [SECURITY.md](SECURITY.md).

## Testing with an MCP Client

To test this server, you'll need an MCP client. You can:

1. Use the [official MCP Inspector](https://github.com/modelcontextprotocol/inspector)
2. Build a custom client using the MCP Kotlin SDK
3. Integrate with LLM applications that support MCP (like Claude Desktop)

## Further Resources

- [MCP Official Documentation](https://modelcontextprotocol.io)
- [Kotlin MCP SDK Repository](https://github.com/modelcontextprotocol/kotlin-sdk)
- [MCP Specification](https://spec.modelcontextprotocol.io)

## License

This project is open source and available under the MIT License.

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                               | Description                                                 |
|----------------------------------------------------|-------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing-default) | Allows to define structured routes and associated handlers. |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
|-----------------------------------------|----------------------------------------------------------------------|
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

