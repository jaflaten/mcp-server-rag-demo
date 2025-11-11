package no.flaten.rag

import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.nio.file.Paths

/**
 * RAG Pipeline Orchestrator
 * 
 * Coordinates the entire RAG pipeline from ingestion to storage:
 * 1. Document Ingestion - Read and parse documents
 * 2. Chunking - Split documents into manageable pieces
 * 3. Embedding - Generate vector embeddings
 * 4. Storage - Store in vector database
 */
class RagPipeline(
    private val documentsPath: Path,
    private val embeddingService: EmbeddingService,
    private val vectorStorePath: String = "vector_store.json",
    private val chunkSize: Int = 500,
    private val chunkOverlap: Int = 100
) {
    
    private val ingestion = DocumentIngestion(documentsPath)
    private val chunking = DocumentChunking(chunkSize, chunkOverlap)
    private val vectorStore = VectorStore(vectorStorePath)
    
    companion object {
        /**
         * Creates a pipeline with OpenAI embeddings
         */
        fun withOpenAI(
            documentsPath: Path,
            apiKey: String,
            vectorStorePath: String = "vector_store.json",
            chunkSize: Int = 500,
            chunkOverlap: Int = 100
        ): RagPipeline {
            val embeddingService = EmbeddingServiceFactory.createOpenAI(apiKey)
            return RagPipeline(documentsPath, embeddingService, vectorStorePath, chunkSize, chunkOverlap)
        }
        
        /**
         * Creates a pipeline with Ollama embeddings (local, semantic, free!)
         */
        fun withOllama(
            documentsPath: Path,
            model: String = "all-minilm",
            vectorStorePath: String = "vector_store.json",
            chunkSize: Int = 500,
            chunkOverlap: Int = 100
        ): RagPipeline {
            val embeddingService = EmbeddingServiceFactory.createOllama(model)
            return RagPipeline(documentsPath, embeddingService, vectorStorePath, chunkSize, chunkOverlap)
        }
        
        /**
         * Creates a pipeline with simple hash-based embeddings (no API key needed)
         */
        fun withSimpleEmbeddings(
            documentsPath: Path,
            vectorStorePath: String = "vector_store.json",
            chunkSize: Int = 500,
            chunkOverlap: Int = 100
        ): RagPipeline {
            val embeddingService = EmbeddingServiceFactory.createSimple()
            return RagPipeline(documentsPath, embeddingService, vectorStorePath, chunkSize, chunkOverlap)
        }
        
        /**
         * Auto-selects best available embedding provider
         */
        suspend fun withBestAvailable(
            documentsPath: Path,
            openAiApiKey: String? = null,
            vectorStorePath: String = "vector_store.json",
            chunkSize: Int = 500,
            chunkOverlap: Int = 100
        ): RagPipeline {
            val embeddingService = EmbeddingServiceFactory.createBestAvailable(openAiApiKey)
            return RagPipeline(documentsPath, embeddingService, vectorStorePath, chunkSize, chunkOverlap)
        }
    }
    
    /**
     * Runs the complete RAG pipeline
     * 
     * @return Success status
     */
    fun runPipeline(): Boolean {
        return try {
            System.err.println("=" * 60)
            System.err.println("Starting RAG Pipeline")
            System.err.println("=" * 60)
            
            // Step 1: Ingestion
            System.err.println("\n[Step 1/4] Document Ingestion")
            System.err.println("-" * 60)
            val documents = ingestion.ingestDocuments()
            
            if (documents.isEmpty()) {
                System.err.println("✗ No documents found. Add files to: $documentsPath")
                return false
            }
            
            System.err.println("✓ Ingested ${documents.size} documents")
            
            // Step 2: Chunking
            System.err.println("\n[Step 2/4] Document Chunking")
            System.err.println("-" * 60)
            val chunks = chunking.chunkDocuments(documents)
            System.err.println("✓ Created ${chunks.size} chunks")
            
            // Step 3: Embedding
            System.err.println("\n[Step 3/4] Generating Embeddings")
            System.err.println("-" * 60)
            val embeddedChunks = runBlocking {
                embeddingService.embedChunks(chunks)
            }
            
            val successfulEmbeddings = embeddedChunks.count { it.embedding != null }
            System.err.println("✓ Generated embeddings for $successfulEmbeddings/${chunks.size} chunks")
            
            // Step 4: Storage
            System.err.println("\n[Step 4/4] Storing in Vector Database")
            System.err.println("-" * 60)
            vectorStore.addChunks(embeddedChunks)
            vectorStore.save(
                embeddingModel = embeddingService.getModelName(),
                chunkSize = chunkSize
            )
            
            System.err.println("\n" + "=" * 60)
            System.err.println("Pipeline Complete!")
            System.err.println("=" * 60)
            System.err.println("Documents processed: ${documents.size}")
            System.err.println("Chunks created: ${chunks.size}")
            System.err.println("Embeddings generated: $successfulEmbeddings")
            System.err.println("Storage: $vectorStorePath")
            System.err.println("=" * 60)
            
            true
        } catch (e: Exception) {
            System.err.println("\n✗ Pipeline failed: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            embeddingService.close()
        }
    }
    
    /**
     * Loads an existing vector store
     */
    fun loadVectorStore(): Boolean {
        return vectorStore.load()
    }
    
    /**
     * Gets the vector store for querying
     */
    fun getVectorStore(): VectorStore = vectorStore
    
    /**
     * Queries the vector store with a text query and generates an answer
     */
    suspend fun query(
        queryText: String,
        topK: Int = 5,
        minSimilarity: Double = 0.0,
        llmProvider: LLMProvider? = null
    ): RagResponse {
        val queryService = RagQueryService(
            vectorStore = vectorStore,
            embeddingService = embeddingService,
            llmProvider = llmProvider ?: SimpleLLMProvider()
        )
        
        return try {
            queryService.query(queryText, topK, minSimilarity)
        } finally {
            if (llmProvider != null) {
                queryService.close()
            }
        }
    }
    
    /**
     * Legacy method for backwards compatibility - returns search results
     */
    suspend fun search(
        queryText: String,
        topK: Int = 5,
        minSimilarity: Double = 0.7
    ): List<SearchResult> {
        // Generate embedding for query
        val queryChunk = DocumentChunk(
            id = "query",
            content = queryText,
            embedding = null,
            metadata = ChunkMetadata(
                documentId = "query",
                chunkIndex = 0,
                totalChunks = 1,
                source = "query",
                title = "Query",
                startChar = 0,
                endChar = queryText.length
            )
        )
        
        val embeddedQuery = embeddingService.embedChunks(listOf(queryChunk))
        val queryEmbedding = embeddedQuery.firstOrNull()?.embedding
        
        if (queryEmbedding == null) {
            System.err.println("✗ Failed to generate query embedding")
            return emptyList()
        }
        
        // Search vector store
        return vectorStore.search(queryEmbedding, topK, minSimilarity)
    }
}

/**
 * Helper operator for string repetition
 */
private operator fun String.times(n: Int): String = this.repeat(n)
