package no.flaten.rag

import kotlinx.coroutines.runBlocking
import java.nio.file.Paths

/**
 * Standalone tool to run the RAG pipeline
 * 
 * Usage:
 *   OPENAI_API_KEY=your_key_here ./gradlew runRagPipeline
 */
fun main(args: Array<String>) {
    println("=" * 60)
    println("RAG Pipeline Tool")
    println("=" * 60)
    
    // Get API key from environment (optional)
    val apiKey = System.getenv("OPENAI_API_KEY")
    
    // Configure pipeline
    val documentsPath = Paths.get("documents")
    val vectorStorePath = "vector_store.json"
    val chunkSize = 500
    val chunkOverlap = 100
    
    // Auto-select best embedding provider
    val pipeline = runBlocking {
        RagPipeline.withBestAvailable(
            documentsPath = documentsPath,
            openAiApiKey = apiKey,
            vectorStorePath = vectorStorePath,
            chunkSize = chunkSize,
            chunkOverlap = chunkOverlap
        )
    }
    
    println("Configuration:")
    println("  Documents: $documentsPath")
    println("  Storage: $vectorStorePath")
    println("  Chunk size: $chunkSize chars")
    println("  Overlap: $chunkOverlap chars")
    println("")
    
    runBlocking {
        val success = pipeline.runPipeline()
        
        if (success) {
            println("\n✓ Pipeline completed successfully!")
            println("  Vector store saved to: $vectorStorePath")
            
            // Test query
            if (args.isNotEmpty()) {
                val query = args.joinToString(" ")
                println("\n" + "=" * 80)
                println("Testing RAG Query")
                println("=" * 80)
                
                // Choose LLM provider
                val llmProvider = if (apiKey.isNullOrBlank()) {
                    SimpleLLMProvider()
                } else {
                    OpenAILLMProvider(apiKey)
                }
                
                try {
                    val response = pipeline.query(
                        queryText = query,
                        topK = 3,
                        minSimilarity = 0.0,
                        llmProvider = llmProvider
                    )
                    
                    // Print formatted response
                    val queryService = RagQueryService(
                        vectorStore = pipeline.getVectorStore(),
                        embeddingService = EmbeddingServiceFactory.createSimple(),
                        llmProvider = SimpleLLMProvider()
                    )
                    queryService.printResponse(response)
                    
                } catch (e: Exception) {
                    System.err.println("✗ Query failed: ${e.message}")
                    e.printStackTrace()
                } finally {
                    llmProvider.close()
                }
            }
            
            kotlin.system.exitProcess(0)
        } else {
            System.err.println("\n✗ Pipeline failed")
            kotlin.system.exitProcess(1)
        }
    }
}

private operator fun String.times(n: Int): String = this.repeat(n)
