package no.flaten.rag

import kotlinx.serialization.Serializable

/**
 * Represents a document with its metadata
 */
@Serializable
data class Document(
    val content: String,
    val metadata: DocumentMetadata
)

/**
 * Metadata for a document
 */
@Serializable
data class DocumentMetadata(
    val source: String,          // File path or source identifier
    val title: String,            // Document title (from filename or content)
    val page: Int? = null,        // Page number (if applicable)
    val totalPages: Int? = null,  // Total pages (if applicable)
    val fileType: String,         // e.g., "txt", "md", "pdf"
    val structure: Map<String, String> = emptyMap() // Additional structural info
)

/**
 * Represents a chunk of a document with metadata
 */
@Serializable
data class DocumentChunk(
    val id: String,               // Unique identifier for this chunk
    val content: String,          // The chunk text
    val embedding: List<Double>? = null,  // Vector embedding (null before embedding step)
    val metadata: ChunkMetadata
)

/**
 * Metadata for a document chunk
 */
@Serializable
data class ChunkMetadata(
    val documentId: String,       // Reference to parent document
    val chunkIndex: Int,          // Position in the document
    val totalChunks: Int,         // Total number of chunks in document
    val source: String,           // Original source file
    val title: String,            // Document title
    val headings: List<String> = emptyList(), // Contextual headings
    val startChar: Int,           // Character position in original document
    val endChar: Int              // End character position
)

/**
 * Vector store entry for serialization
 */
@Serializable
data class VectorStoreEntry(
    val chunks: List<DocumentChunk>,
    val metadata: VectorStoreMetadata
)

/**
 * Metadata about the vector store
 */
@Serializable
data class VectorStoreMetadata(
    val version: String = "1.0",
    val createdAt: String,
    val embeddingModel: String,
    val chunkSize: Int,
    val totalChunks: Int,
    val sources: List<String>
)
