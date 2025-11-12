val kotlin_version: String by project
val logback_version: String by project
val ktor_version = "3.3.2"
val mcpVersion = "0.7.7"

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin") version "3.3.2"
}

group = "no.flaten"
version = "0.0.1"

application {
    mainClass = "no.flaten.chatbot.PokemonChatbotServerKt"
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    
    // MCP Client
    implementation("io.modelcontextprotocol:kotlin-sdk:${mcpVersion}")
    
    // HTTP Client for Ollama
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
