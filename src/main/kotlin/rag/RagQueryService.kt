package no.flaten.rag

/**
 * RAG Query Service
 * 
 * Handles the retrieval and generation steps:
 * 1. Generate query embedding
 * 2. Search vector store for similar chunks
 * 3. Format context from retrieved chunks
 * 4. Send to LLM for response generation
 */
class RagQueryService(
    private val vectorStore: VectorStore,
    private val embeddingService: EmbeddingService,
    private val llmProvider: LLMProvider
) {
    
    /**
     * Performs RAG query: retrieve + generate
     * 
     * @param query The user's question
     * @param topK Number of chunks to retrieve
     * @param minSimilarity Minimum similarity threshold
     * @return Generated response with sources
     */
    suspend fun query(
        query: String,
        topK: Int = 5,
        minSimilarity: Double = 0.0
    ): RagResponse {
        System.err.println("\n[RAG Query] \"$query\"")
        System.err.println("-" * 60)
        
        // Step 1: Generate query embedding
        System.err.println("Step 1: Generating query embedding...")
        val queryChunk = DocumentChunk(
            id = "query",
            content = query,
            embedding = null,
            metadata = ChunkMetadata(
                documentId = "query",
                chunkIndex = 0,
                totalChunks = 1,
                source = "query",
                title = "Query",
                startChar = 0,
                endChar = query.length
            )
        )
        
        val embeddedQuery = embeddingService.embedChunks(listOf(queryChunk))
        val queryEmbedding = embeddedQuery.firstOrNull()?.embedding
        
        if (queryEmbedding == null) {
            System.err.println("✗ Failed to generate query embedding")
            return RagResponse(
                query = query,
                answer = "Error: Failed to generate query embedding",
                sources = emptyList(),
                retrievedChunks = 0
            )
        }
        
        System.err.println("✓ Query embedding generated (${queryEmbedding.size} dimensions)")
        
        // Step 2: Search vector store
        System.err.println("\nStep 2: Searching vector store...")
        val results = vectorStore.search(queryEmbedding, topK, minSimilarity)
        System.err.println("✓ Found ${results.size} relevant chunks")
        
        if (results.isEmpty()) {
            return RagResponse(
                query = query,
                answer = "No relevant information found in the knowledge base.",
                sources = emptyList(),
                retrievedChunks = 0
            )
        }
        
        // Step 3: Format context
        System.err.println("\nStep 3: Formatting context...")
        val context = formatContext(results)
        val sources = results.map { result ->
            SourceReference(
                source = result.chunk.metadata.source,
                title = result.chunk.metadata.title,
                similarity = result.similarity,
                chunkId = result.chunk.id,
                excerpt = result.chunk.content.take(150) + "..."
            )
        }
        
        System.err.println("✓ Context formatted (${context.length} characters)")
        
        // Step 4: Generate response with LLM
        System.err.println("\nStep 4: Generating response with ${llmProvider.getModelName()}...")
        val answer = llmProvider.generateResponse(query, context)
        System.err.println("✓ Response generated")
        
        System.err.println("\n" + "=" * 60)
        
        return RagResponse(
            query = query,
            answer = answer,
            sources = sources,
            retrievedChunks = results.size
        )
    }
    
    /**
     * Formats retrieved chunks into context for LLM
     */
    private fun formatContext(results: List<SearchResult>): String {
        return buildString {
            results.forEachIndexed { index, result ->
                appendLine("--- Source ${index + 1} ---")
                appendLine("Title: ${result.chunk.metadata.title}")
                appendLine("Source: ${result.chunk.metadata.source}")
                appendLine("Similarity: ${String.format("%.3f", result.similarity)}")
                
                if (result.chunk.metadata.headings.isNotEmpty()) {
                    appendLine("Context: ${result.chunk.metadata.headings.joinToString(" > ")}")
                }
                
                appendLine()
                appendLine(result.chunk.content)
                appendLine()
            }
        }
    }
    
    /**
     * Prints a formatted response
     */
    fun printResponse(response: RagResponse) {
        println()
        println("=" * 80)
        println("Query: ${response.query}")
        println("=" * 80)
        println()
        println("Answer:")
        println("-" * 80)
        println(response.answer)
        println()
        println("=" * 80)
        println("Sources (${response.retrievedChunks} chunks retrieved):")
        println("-" * 80)
        
        response.sources.forEachIndexed { index, source ->
            println()
            println("[${index + 1}] ${source.title}")
            println("    Source: ${source.source}")
            println("    Similarity: ${String.format("%.3f", source.similarity)}")
            println("    Excerpt: ${source.excerpt}")
        }
        
        println()
        println("=" * 80)
    }
    
    /**
     * Closes resources
     */
    fun close() {
        llmProvider.close()
    }
}

/**
 * RAG response with answer and sources
 */
data class RagResponse(
    val query: String,
    val answer: String,
    val sources: List<SourceReference>,
    val retrievedChunks: Int
)

/**
 * Reference to a source document
 */
data class SourceReference(
    val source: String,
    val title: String,
    val similarity: Double,
    val chunkId: String,
    val excerpt: String
)

/**
 * Helper operator for string repetition
 */
private operator fun String.times(n: Int): String = this.repeat(n)
