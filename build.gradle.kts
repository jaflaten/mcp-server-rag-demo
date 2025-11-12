val kotlin_version: String by project
val logback_version: String by project
val mcpVersion = "0.7.7"

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("io.ktor.plugin") version "3.3.2"
}

group = "no.flaten"
version = "0.0.1"

application {
    mainClass = "no.flaten.HelloWorldMcpServerKt"
}

// Task to run HTTP/SSE version of MCP server
tasks.register<JavaExec>("runHttpServer") {
    group = "mcp"
    description = "Run the HTTP/JSON-RPC MCP server on port 8080"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("no.flaten.SimpleHttpMcpServerKt")
    standardInput = System.`in`
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-cio")
    implementation("io.ktor:ktor-server-sse")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.modelcontextprotocol:kotlin-sdk:${mcpVersion}")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
    
    // RAG Pipeline Dependencies
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

// Task to run RAG pipeline
tasks.register<JavaExec>("runRagPipeline") {
    group = "rag"
    description = "Runs the RAG pipeline to ingest documents and create vector store"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("no.flaten.rag.RagPipelineToolKt")
    standardInput = System.`in`
    
    // Pass arguments if provided
    if (project.hasProperty("args")) {
        args = (project.property("args") as String).split("\\s+".toRegex())
    }
}

// Task to query existing vector store (faster)
tasks.register<JavaExec>("runQuery") {
    group = "rag"
    description = "Query the RAG system using existing vector store"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("no.flaten.rag.RagQueryToolKt")
    standardInput = System.`in`
    
    if (project.hasProperty("args")) {
        args = (project.property("args") as String).split("\\s+".toRegex())
    }
}
