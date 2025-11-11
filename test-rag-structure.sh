#!/bin/bash
# Quick test of the RAG pipeline without OpenAI API

set -e

echo "==========================================================="
echo "RAG Pipeline Structure Test (No API Key Required)"
echo "==========================================================="
echo ""

# Check if documents exist
if [ ! -d "documents" ]; then
    echo "✗ Documents directory not found"
    exit 1
fi

doc_count=$(find documents -name "*.md" -o -name "*.txt" | wc -l | tr -d ' ')
echo "✓ Found documents directory with $doc_count files"

# Check if source files exist
echo ""
echo "Checking RAG pipeline components:"
for file in "Models.kt" "DocumentIngestion.kt" "DocumentChunking.kt" "EmbeddingService.kt" "VectorStore.kt" "RagPipeline.kt"; do
    if [ -f "src/main/kotlin/rag/$file" ]; then
        echo "  ✓ $file"
    else
        echo "  ✗ $file MISSING"
        exit 1
    fi
done

echo ""
echo "✓ All RAG pipeline components present"
echo ""
echo "To run the full pipeline with embeddings:"
echo "  export OPENAI_API_KEY=your_key_here"
echo "  ./gradlew runRagPipeline"
echo ""
echo "Or test with a query:"
echo "  OPENAI_API_KEY=your_key ./gradlew runRagPipeline -Pargs=\"What is MCP?\""
echo ""
