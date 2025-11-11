# Security Considerations

This document outlines the security measures implemented in the MCP Hello World server.

## Input Sanitization

All user inputs are sanitized using the `sanitizeInput()` function to protect against common security vulnerabilities.

### What Gets Sanitized

1. **Control Characters**
   - Newlines (`\n`)
   - Carriage returns (`\r`)
   - Tabs (`\t`)
   - Other Unicode control characters

2. **Length Limits**
   - `name` parameter: Maximum 50 characters
   - `message` parameter: Maximum 500 characters

3. **Whitespace**
   - Leading and trailing whitespace is trimmed

### Protection Against

#### 1. Injection Attacks
By removing control characters and limiting input length, we prevent:
- Command injection attempts
- Log injection (newlines in log files)
- Format string attacks

**Example Attack Prevention:**
```kotlin
// Malicious input:
"Alice\n[ERROR] System compromised"

// After sanitization:
"Alice[ERROR] System compromised"  // Newline removed
```

#### 2. Denial of Service (DoS)
Length limits prevent:
- Memory exhaustion from extremely long inputs
- Processing overhead from large strings

**Example:**
```kotlin
// Malicious input: 1MB string
val hugeInput = "A".repeat(1_000_000)

// After sanitization: 50 characters
"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
```

#### 3. Output Formatting Issues
Removing control characters ensures:
- Consistent output formatting
- No broken JSON responses
- Clean log entries

### Implementation

```kotlin
/**
 * Sanitizes user input to prevent injection attacks and ensure safe output.
 * - Removes control characters (newlines, tabs, etc.)
 * - Limits length to prevent DoS
 * - Trims whitespace
 */
fun sanitizeInput(input: String, maxLength: Int = 100): String {
    return input
        .replace(Regex("[\n\r\t]"), "") // Remove newlines, carriage returns, tabs
        .replace(Regex("[\\p{C}]"), "") // Remove other control characters
        .trim()
        .take(maxLength) // Limit length
}
```

## Parameter Validation

### JSON Schema Constraints

Tools define input schemas with built-in constraints:

```kotlin
inputSchema = Tool.Input(
    properties = buildJsonObject {
        putJsonObject("name") {
            put("type", "string")
            put("description", "The name of the person to greet (optional, max 50 characters)")
            put("maxLength", 50)  // Schema-level validation
        }
    },
    required = emptyList()
)
```

### Runtime Validation

Additional checks are performed at runtime:

1. **Null/Empty Checks**
   ```kotlin
   if (rawMessage.isNullOrBlank()) {
       // Return error
   }
   ```

2. **Post-Sanitization Validation**
   ```kotlin
   if (sanitizedMessage.isBlank()) {
       // Input was all invalid characters
       return error
   }
   ```

## Transport Security

### STDIO Transport

The server uses STDIO (Standard Input/Output) transport:
- **Local Communication**: No network exposure by default
- **Process Isolation**: Runs in isolated process space
- **No Authentication Required**: Relies on OS-level process permissions

### Considerations

For production deployments:
- Use secure transports (HTTPS, WSS) for network communication
- Implement authentication and authorization
- Use TLS for encrypted connections
- Add rate limiting for API calls

## Best Practices

### For Developers Extending This Server

1. **Always Sanitize User Input**
   ```kotlin
   val sanitized = sanitizeInput(rawInput, maxLength = appropriateLimit)
   ```

2. **Define Maximum Lengths**
   - Consider memory constraints
   - Balance usability with security
   - Document limits in tool descriptions

3. **Validate After Sanitization**
   ```kotlin
   if (sanitized.isBlank()) {
       // Handle case where input was all invalid characters
   }
   ```

4. **Avoid Direct String Interpolation**
   ```kotlin
   // ✅ Good: Using sanitized input
   "Hello, ${sanitizeInput(name)}!"
   
   // ❌ Bad: Using raw input
   "Hello, $rawName!"
   ```

5. **Log Sanitized Inputs**
   ```kotlin
   // ✅ Good
   logger.info("Processed name: ${sanitizeInput(name)}")
   
   // ❌ Bad: Could inject malicious log entries
   logger.info("Processed name: $name")
   ```

## Testing Security

### Test Cases to Consider

1. **Control Characters**
   ```
   Input: "Alice\nBob\tCharlie"
   Expected: "AliceBobCharlie"
   ```

2. **Excessive Length**
   ```
   Input: "A".repeat(1000)
   Expected: "A".repeat(50)  // Truncated to maxLength
   ```

3. **Only Whitespace**
   ```
   Input: "   \n\t   "
   Expected: "" (blank after sanitization)
   ```

4. **Special Characters (Safe)**
   ```
   Input: "Alice O'Brien"
   Expected: "Alice O'Brien" (preserved)
   ```

5. **Unicode Characters**
   ```
   Input: "你好 World"
   Expected: "你好 World" (preserved)
   ```

## Additional Security Measures to Consider

For production environments, consider implementing:

1. **Rate Limiting**
   - Limit requests per client per time window
   - Prevent abuse and DoS attacks

2. **Authentication & Authorization**
   - Verify client identity
   - Control access to specific tools

3. **Input Content Filtering**
   - Profanity filters
   - PII detection and redaction
   - Sensitive data scanning

4. **Audit Logging**
   - Log all tool invocations
   - Include timestamps and client info
   - Monitor for suspicious patterns

5. **Resource Limits**
   - Memory usage caps
   - CPU time limits
   - Maximum concurrent connections

## Reporting Security Issues

If you discover a security vulnerability:
1. Do not open a public issue
2. Contact the maintainers privately
3. Provide detailed reproduction steps
4. Allow time for a fix before public disclosure

## References

- [OWASP Input Validation Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html)
- [CWE-20: Improper Input Validation](https://cwe.mitre.org/data/definitions/20.html)
- [MCP Security Best Practices](https://modelcontextprotocol.io/docs/security)
