# Pokemon Dataset for RAG Testing

This dataset contains 20 text files about popular Pokemon, perfect for testing the RAG pipeline.

## Dataset Contents

### Pokemon Files (20)
1. **alakazam.txt** - Psychic-type with IQ over 5,000
2. **blaziken.txt** - Fire/Fighting-type with powerful kicks
3. **bulbasaur.txt** - Grass/Poison starter Pokemon
4. **charizard.txt** - Fire/Flying dragon-like Pokemon
5. **dragonite.txt** - Dragon/Flying-type with kind nature
6. **eevee.txt** - Normal-type with 8 evolutions
7. **garchomp.txt** - Dragon/Ground land shark
8. **gengar.txt** - Ghost/Poison shadow lurker
9. **greninja.txt** - Water/Dark ninja Pokemon
10. **gyarados.txt** - Water/Flying evolved from Magikarp
11. **lapras.txt** - Water/Ice gentle transport Pokemon
12. **lucario.txt** - Fighting/Steel aura Pokemon
13. **machamp.txt** - Fighting-type with four arms
14. **mewtwo.txt** - Legendary Psychic-type
15. **pikachu.txt** - Electric-type mascot Pokemon
16. **scizor.txt** - Bug/Steel evolved from Scyther
17. **snorlax.txt** - Normal-type sleeping giant
18. **squirtle.txt** - Water-type turtle starter
19. **tyranitar.txt** - Rock/Dark destructive Pokemon
20. **umbreon.txt** - Dark-type Eevee evolution

### Technical Writing Files (3)
- **kotlin_guide.md** - Kotlin programming guide
- **mcp_overview.md** - Model Context Protocol overview
- **rag_systems.md** - RAG implementation guide

## Statistics

- **Total files**: 23 (20 Pokemon + 3 technical)
- **Pokemon files**: ~450-520 bytes each
- **Total size**: ~12KB
- **Format**: Plain text (.txt and .md)

## Content Structure

Each Pokemon file contains:
- Type information (e.g., Electric, Fire/Flying)
- Physical description and abilities
- Evolution information
- Special characteristics
- Mega Evolution details (if applicable)
- Behavioral traits

## Example Queries for RAG Testing

### Type-Based Queries
- "Which Pokemon can use fire attacks?"
- "Show me all Psychic-type Pokemon"
- "What Pokemon have dual typing?"

### Evolution Queries
- "How does Eevee evolve?"
- "Which Pokemon evolve through trading?"
- "What Pokemon can Mega Evolve?"

### Ability Queries
- "Which Pokemon can sense aura?"
- "What Pokemon can fly?"
- "Which Pokemon is the strongest?"

### Comparison Queries
- "Compare Pikachu and Mewtwo"
- "What's the difference between Charizard's Mega Evolutions?"
- "Which is better: Alakazam or Machamp?"

### Specific Information
- "What is Snorlax's diet?"
- "How fast can Dragonite fly?"
- "What is Gengar's behavior?"

## Usage with RAG Pipeline

```bash
# 1. Set your OpenAI API key
export OPENAI_API_KEY=your_key_here

# 2. Run the pipeline to ingest all documents
./gradlew runRagPipeline

# 3. Test with Pokemon queries
OPENAI_API_KEY=key ./gradlew runRagPipeline -Pargs="What type is Pikachu?"
OPENAI_API_KEY=key ./gradlew runRagPipeline -Pargs="Which Pokemon can Mega Evolve?"
OPENAI_API_KEY=key ./gradlew runRagPipeline -Pargs="Tell me about Eevee evolutions"
```

## Expected RAG Results

### Example 1: "What is Pikachu?"
**Expected chunks**: 
- pikachu.txt (high similarity)
- eevee.txt (mentions evolution, medium similarity)

### Example 2: "Which Pokemon evolve by trading?"
**Expected chunks**:
- alakazam.txt (Kadabra → Alakazam via trade)
- gengar.txt (Haunter → Gengar via trade)
- machamp.txt (Machoke → Machamp via trade)
- scizor.txt (Scyther → Scizor via Metal Coat trade)

### Example 3: "Tell me about Dragon type Pokemon"
**Expected chunks**:
- dragonite.txt (Dragon/Flying)
- garchomp.txt (Dragon/Ground)
- charizard.txt (mentions Mega Charizard X becomes Fire/Dragon)

## Chunking Expectations

With default settings (500 char chunks, 100 overlap):
- Each Pokemon file will be split into 1-2 chunks
- Total expected chunks: ~30-35 chunks
- Overlap ensures context preservation
- Headings and structure preserved

## Dataset Benefits

1. **Diverse Content**: Mix of types, abilities, evolutions
2. **Clear Topics**: Each file focuses on one Pokemon
3. **Structured Info**: Consistent format across files
4. **Cross-References**: Pokemon mention others (evolutions, comparisons)
5. **Test Complexity**: From simple ("What is X?") to complex ("Compare X and Y")

## Extending the Dataset

To add more Pokemon:
```bash
cat > documents/new_pokemon.txt << 'EOF'
[Pokemon Name] is a [Type]-type Pokemon...
[Description of abilities and characteristics]
[Evolution information]
[Special features]
EOF
```

Then re-run the pipeline to re-index.

## Clean Up

To remove Pokemon files and keep only technical docs:
```bash
rm documents/*.txt
```

To remove everything:
```bash
rm documents/*
```

---

**Ready to test your RAG pipeline with Pokemon!** 🎮⚡🔥💧🌿
