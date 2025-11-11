package no.flaten.rag

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText

/**
 * Step 1: Document Ingestion
 * 
 * Reads files from a directory and converts them to Document objects
 * while preserving structure (headings, lists) and adding metadata.
 */
class DocumentIngestion(
    private val documentsPath: Path
) {
    
    /**
     * Ingests all documents from the configured directory
     * 
     * @return List of documents with metadata
     */
    fun ingestDocuments(): List<Document> {
        val documents = mutableListOf<Document>()
        
        val directory = documentsPath.toFile()
        if (!directory.exists() || !directory.isDirectory) {
            System.err.println("Documents directory not found: $documentsPath")
            return emptyList()
        }
        
        directory.listFiles()?.forEach { file ->
            if (file.isFile && isSupportedFileType(file)) {
                try {
                    val document = ingestDocument(file)
                    documents.add(document)
                    System.err.println("✓ Ingested: ${file.name}")
                } catch (e: Exception) {
                    System.err.println("✗ Failed to ingest ${file.name}: ${e.message}")
                }
            }
        }
        
        return documents
    }
    
    /**
     * Ingests a single document file
     */
    private fun ingestDocument(file: File): Document {
        val content = file.readText()
        val fileType = file.extension.lowercase()
        
        // Extract title from content or use filename
        val title = extractTitle(content, file.nameWithoutExtension)
        
        // Preserve structure based on file type
        val structuredContent = when (fileType) {
            "md", "markdown" -> preserveMarkdownStructure(content)
            "txt" -> preserveTextStructure(content)
            else -> content
        }
        
        val metadata = DocumentMetadata(
            source = file.path,
            title = title,
            fileType = fileType,
            structure = extractStructure(content, fileType)
        )
        
        return Document(
            content = structuredContent,
            metadata = metadata
        )
    }
    
    /**
     * Checks if file type is supported
     */
    private fun isSupportedFileType(file: File): Boolean {
        val supportedExtensions = setOf("txt", "md", "markdown")
        return file.extension.lowercase() in supportedExtensions
    }
    
    /**
     * Extracts title from content or uses fallback
     */
    private fun extractTitle(content: String, fallbackTitle: String): String {
        // Try to extract first heading from markdown
        val lines = content.lines()
        for (line in lines) {
            if (line.startsWith("# ")) {
                return line.removePrefix("# ").trim()
            }
        }
        
        // Use first non-empty line if it's short enough
        val firstLine = lines.firstOrNull { it.isNotBlank() }
        if (firstLine != null && firstLine.length < 100) {
            return firstLine.trim()
        }
        
        return fallbackTitle
    }
    
    /**
     * Preserves markdown structure (headings, lists, code blocks)
     */
    private fun preserveMarkdownStructure(content: String): String {
        // Keep markdown as-is since it already has structure
        return content
    }
    
    /**
     * Preserves text structure (paragraphs, spacing)
     */
    private fun preserveTextStructure(content: String): String {
        // Normalize line endings and preserve paragraph structure
        return content
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }
    
    /**
     * Extracts structural information from the document
     */
    private fun extractStructure(content: String, fileType: String): Map<String, String> {
        val structure = mutableMapOf<String, String>()
        
        when (fileType) {
            "md", "markdown" -> {
                val headings = extractHeadings(content)
                if (headings.isNotEmpty()) {
                    structure["headings"] = headings.joinToString(" > ")
                }
                
                val lists = countLists(content)
                if (lists > 0) {
                    structure["lists"] = lists.toString()
                }
                
                val codeBlocks = countCodeBlocks(content)
                if (codeBlocks > 0) {
                    structure["codeBlocks"] = codeBlocks.toString()
                }
            }
        }
        
        // Add basic stats
        structure["lines"] = content.lines().size.toString()
        structure["characters"] = content.length.toString()
        structure["words"] = content.split("\\s+".toRegex()).size.toString()
        
        return structure
    }
    
    /**
     * Extracts all headings from markdown content
     */
    private fun extractHeadings(content: String): List<String> {
        return content.lines()
            .filter { it.trim().startsWith("#") }
            .map { it.replace(Regex("^#+\\s*"), "").trim() }
    }
    
    /**
     * Counts list items in markdown
     */
    private fun countLists(content: String): Int {
        return content.lines().count { line ->
            val trimmed = line.trim()
            trimmed.startsWith("- ") || 
            trimmed.startsWith("* ") || 
            trimmed.matches(Regex("^\\d+\\.\\s.*"))
        }
    }
    
    /**
     * Counts code blocks in markdown
     */
    private fun countCodeBlocks(content: String): Int {
        return content.split("```").size - 1
    }
}
