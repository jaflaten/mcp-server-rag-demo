#!/bin/bash
# Example queries to test the RAG pipeline with Pokemon dataset

echo "=========================================================="
echo "RAG Pipeline - Pokemon Query Examples"
echo "=========================================================="
echo ""
echo "Make sure to set your OpenAI API key first:"
echo "  export OPENAI_API_KEY=your_key_here"
echo ""
echo "=========================================================="
echo ""

# Check if API key is set
if [ -z "$OPENAI_API_KEY" ]; then
    echo "⚠️  OPENAI_API_KEY not set. These are example commands only."
    echo ""
fi

echo "Example 1: Simple Type Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"What type is Pikachu?\""
echo ""

echo "Example 2: Evolution Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"How does Eevee evolve?\""
echo ""

echo "Example 3: Ability Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"Which Pokemon can sense aura?\""
echo ""

echo "Example 4: Trading Evolution Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"Which Pokemon evolve by trading?\""
echo ""

echo "Example 5: Mega Evolution Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"Tell me about Charizard Mega Evolution\""
echo ""

echo "Example 6: Comparison Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"Compare Alakazam and Machamp\""
echo ""

echo "Example 7: Specific Feature Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"What Pokemon can fly?\""
echo ""

echo "Example 8: Legendary Pokemon Query"
echo "Command:"
echo "  ./gradlew runRagPipeline -Pargs=\"Tell me about Mewtwo\""
echo ""

echo "=========================================================="
echo ""
echo "To run any query:"
echo "  OPENAI_API_KEY=your_key ./gradlew runRagPipeline -Pargs=\"YOUR QUERY HERE\""
echo ""
echo "To just build the vector store (without query):"
echo "  OPENAI_API_KEY=your_key ./gradlew runRagPipeline"
echo ""
