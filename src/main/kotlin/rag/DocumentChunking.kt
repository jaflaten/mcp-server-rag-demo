package no.flaten.rag

import java.util.UUID

/**
 * Step 2: Document Chunking
 * 
 * Splits documents into smaller chunks with overlapping context
 * and preserves metadata and structure information.
 */
class DocumentChunking(
    private val chunkSize: Int = 500,        // Characters per chunk
    private val overlapSize: Int = 100       // Overlap between chunks
) {
    
    /**
     * Chunks a list of documents
     * 
     * @param documents The documents to chunk
     * @return List of document chunks with metadata
     */
    fun chunkDocuments(documents: List<Document>): List<DocumentChunk> {
        val allChunks = mutableListOf<DocumentChunk>()
        
        documents.forEach { document ->
            val chunks = chunkDocument(document)
            allChunks.addAll(chunks)
            System.err.println("✓ Chunked '${document.metadata.title}' into ${chunks.size} chunks")
        }
        
        return allChunks
    }
    
    /**
     * Chunks a single document
     */
    private fun chunkDocument(document: Document): List<DocumentChunk> {
        val chunks = mutableListOf<DocumentChunk>()
        val documentId = UUID.randomUUID().toString()
        
        // Extract headings for context
        val headings = extractHeadingsHierarchy(document.content)
        
        // Create chunks with overlap
        val chunkTexts = createChunksWithOverlap(document.content)
        
        chunkTexts.forEachIndexed { index, (chunkText, startChar, endChar) ->
            // Find relevant headings for this chunk
            val relevantHeadings = findRelevantHeadings(
                document.content,
                headings,
                startChar
            )
            
            val chunk = DocumentChunk(
                id = UUID.randomUUID().toString(),
                content = chunkText,
                embedding = null, // Will be filled in embedding step
                metadata = ChunkMetadata(
                    documentId = documentId,
                    chunkIndex = index,
                    totalChunks = chunkTexts.size,
                    source = document.metadata.source,
                    title = document.metadata.title,
                    headings = relevantHeadings,
                    startChar = startChar,
                    endChar = endChar
                )
            )
            
            chunks.add(chunk)
        }
        
        return chunks
    }
    
    /**
     * Creates text chunks with overlapping context
     * Returns list of (text, startChar, endChar)
     */
    private fun createChunksWithOverlap(text: String): List<Triple<String, Int, Int>> {
        val chunks = mutableListOf<Triple<String, Int, Int>>()
        
        var startIndex = 0
        
        while (startIndex < text.length) {
            // Calculate end index
            var endIndex = minOf(startIndex + chunkSize, text.length)
            
            // Try to break at sentence boundary if not at end
            if (endIndex < text.length) {
                endIndex = findSentenceBoundary(text, endIndex)
            }
            
            // Extract chunk
            val chunkText = text.substring(startIndex, endIndex).trim()
            
            if (chunkText.isNotEmpty()) {
                chunks.add(Triple(chunkText, startIndex, endIndex))
            }
            
            // Move to next chunk with overlap
            startIndex = endIndex - overlapSize
            
            // Ensure we don't go backwards
            if (startIndex <= chunks.lastOrNull()?.second ?: -1) {
                startIndex = endIndex
            }
        }
        
        return chunks
    }
    
    /**
     * Finds a good sentence boundary near the target index
     */
    private fun findSentenceBoundary(text: String, targetIndex: Int): Int {
        // Look for sentence endings within a window
        val windowSize = 100
        val startSearch = maxOf(0, targetIndex - windowSize)
        val endSearch = minOf(text.length, targetIndex + windowSize)
        
        val searchText = text.substring(startSearch, endSearch)
        
        // Look for sentence endings: . ! ? followed by space or newline
        val sentenceEndings = listOf(". ", ".\n", "! ", "!\n", "? ", "?\n")
        
        var bestBoundary = targetIndex
        var bestDistance = Int.MAX_VALUE
        
        for (ending in sentenceEndings) {
            var pos = searchText.indexOf(ending)
            while (pos != -1) {
                val actualPos = startSearch + pos + ending.length
                val distance = kotlin.math.abs(actualPos - targetIndex)
                
                if (distance < bestDistance && actualPos <= endSearch) {
                    bestDistance = distance
                    bestBoundary = actualPos
                }
                
                pos = searchText.indexOf(ending, pos + 1)
            }
        }
        
        // If we found a good boundary, use it
        if (bestDistance < windowSize) {
            return bestBoundary
        }
        
        // Otherwise, look for word boundary
        return findWordBoundary(text, targetIndex)
    }
    
    /**
     * Finds a word boundary (space) near the target index
     */
    private fun findWordBoundary(text: String, targetIndex: Int): Int {
        // Look ahead for space
        for (i in targetIndex until minOf(targetIndex + 50, text.length)) {
            if (text[i].isWhitespace()) {
                return i
            }
        }
        
        // Look behind for space
        for (i in targetIndex downTo maxOf(0, targetIndex - 50)) {
            if (text[i].isWhitespace()) {
                return i
            }
        }
        
        // Give up and use target index
        return targetIndex
    }
    
    /**
     * Extracts heading hierarchy from markdown content
     */
    private fun extractHeadingsHierarchy(content: String): List<Pair<Int, String>> {
        val headings = mutableListOf<Pair<Int, String>>()
        
        content.lines().forEachIndexed { lineIndex, line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("#")) {
                val level = trimmed.takeWhile { it == '#' }.length
                val heading = trimmed.removePrefix("#".repeat(level)).trim()
                
                if (heading.isNotEmpty()) {
                    headings.add(lineIndex to heading)
                }
            }
        }
        
        return headings
    }
    
    /**
     * Finds relevant headings for a chunk based on its position
     */
    private fun findRelevantHeadings(
        content: String,
        headings: List<Pair<Int, String>>,
        chunkStartChar: Int
    ): List<String> {
        // Calculate line number from character position
        val chunkStartLine = content.substring(0, chunkStartChar).count { it == '\n' }
        
        // Find all headings before this chunk
        val relevantHeadings = headings
            .filter { (lineIndex, _) -> lineIndex < chunkStartLine }
            .map { (_, heading) -> heading }
            .takeLast(3) // Keep last 3 levels of context
        
        return relevantHeadings
    }
}
