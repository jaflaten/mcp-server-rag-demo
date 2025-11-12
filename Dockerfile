# Multi-stage build for MCP RAG Server
# Stage 1: Build the application
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy gradle files first for better layer caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Download dependencies (cached if build files don't change)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the fat JAR
RUN gradle buildFatJar --no-daemon

# Stage 2: Runtime image
FROM amazoncorretto:21-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the fat JAR from builder stage
COPY --from=builder /app/build/libs/mcp-server-demo-all.jar ./app.jar

# Copy documents directory for RAG pipeline
COPY documents ./documents

# Copy vector store if it exists (optional)
RUN if [ -f vector_store.json ]; then cp vector_store.json ./vector_store.json; fi


# Create a volume for the vector store to persist data
VOLUME ["/app/data"]

# Expose the HTTP server port
EXPOSE 8080

# Environment variables for configuration
ENV OPENAI_API_KEY=""
ENV OLLAMA_URL="http://host.docker.internal:11434"
ENV VECTOR_STORE_PATH="/app/data/vector_store.json"
ENV DOCUMENTS_PATH="/app/documents"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the HTTP MCP server
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["no.flaten.SimpleHttpMcpServerKt"]
