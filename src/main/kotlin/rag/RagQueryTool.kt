package no.flaten.rag

import kotlinx.coroutines.runBlocking
import java.nio.file.Paths

/**
 * Query-only tool - loads existing vector store and queries it
 * No ingestion, chunking, or embedding generation needed
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Usage: ./gradlew runQuery -Pargs=\"Your query here\"")
        kotlin.system.exitProcess(1)
    }
    
    val query = args.joinToString(" ")
    val vectorStorePath = "vector_store.json"
    
    // Check if vector store exists
    if (!java.io.File(vectorStorePath).exists()) {
        System.err.println("❌ Vector store not found: $vectorStorePath")
        System.err.println("")
        System.err.println("Please run the pipeline first:")
        System.err.println("  ./gradlew runRagPipeline")
        kotlin.system.exitProcess(1)
    }
    
    println("=" * 80)
    println("RAG Query Tool")
    println("=" * 80)
    println()
    println("Vector store: $vectorStorePath")
    
    // Get API key - but don't use for LLM (no quota)
    val apiKey = System.getenv("OPENAI_API_KEY")
    
    // Execute everything in a single coroutine scope
    runBlocking {
        // Create services - auto-select best available
        val embeddingService = EmbeddingServiceFactory.createBestAvailable(apiKey)
        
        // Force SimpleLLM since we have no OpenAI quota
        val llmProvider = SimpleLLMProvider()
        println("LLM: SimpleLLMProvider (formatted output)")
        
        println()
        
        // Load vector store
        val vectorStore = VectorStore(vectorStorePath)
        vectorStore.load()
        println("✓ Loaded ${vectorStore.size()} chunks from vector store")
        println()
        
        // Create query service
        val queryService = RagQueryService(
            vectorStore = vectorStore,
            embeddingService = embeddingService,
            llmProvider = llmProvider
        )
        
        // Execute query
        try {
            val response = queryService.query(
                query = query,
                topK = 5,
                minSimilarity = 0.0
            )
            
            queryService.printResponse(response)
            
        } catch (e: Exception) {
            System.err.println("✗ Query failed: ${e.message}")
            e.printStackTrace()
            kotlin.system.exitProcess(1)
        } finally {
            queryService.close()
        }
    }
}

private operator fun String.times(n: Int): String = this.repeat(n)
