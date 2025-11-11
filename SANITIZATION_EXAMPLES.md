# Input Sanitization Examples

This document shows how the input sanitization protects your MCP server.

## Example 1: Control Character Removal

### Input
```json
{
  "name": "hello",
  "arguments": {
    "name": "Alice\nBob\tCharlie"
  }
}
```

### Before Sanitization
```
"Hello, Alice
Bob	Charlie! Welcome to the MCP Hello World server!"
```

### After Sanitization
```
"Hello, AliceBobCharlie! Welcome to the MCP Hello World server!"
```

**Protection**: Prevents log injection and output formatting issues.

---

## Example 2: Length Limiting

### Input
```json
{
  "name": "hello",
  "arguments": {
    "name": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
  }
}
```
(80 characters)

### Before Sanitization
```
"Hello, AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA! Welcome to the MCP Hello World server!"
```

### After Sanitization (50 char limit)
```
"Hello, AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA! Welcome to the MCP Hello World server!"
```

**Protection**: Prevents DoS attacks from excessive memory usage.

---

## Example 3: Whitespace Trimming

### Input
```json
{
  "name": "hello",
  "arguments": {
    "name": "   Alice   "
  }
}
```

### Before Sanitization
```
"Hello,    Alice   ! Welcome to the MCP Hello World server!"
```

### After Sanitization
```
"Hello, Alice! Welcome to the MCP Hello World server!"
```

**Protection**: Ensures clean, consistent output.

---

## Example 4: All Invalid Characters

### Input
```json
{
  "name": "hello",
  "arguments": {
    "name": "\n\r\t   "
  }
}
```

### Behavior
Since the input contains only invalid characters and whitespace:
1. Control characters removed: `""` (empty after trim)
2. Whitespace trimmed: `""` (empty)
3. Validation check: `isBlank()` returns `true`

### Output
```
"Hello, World! Welcome to the MCP Hello World server!"
```

**Protection**: Gracefully handles malicious input that's all control characters.

---

## Example 5: Safe Special Characters (Preserved)

### Input
```json
{
  "name": "hello",
  "arguments": {
    "name": "Alice O'Brien-Smith"
  }
}
```

### After Sanitization
```
"Hello, Alice O'Brien-Smith! Welcome to the MCP Hello World server!"
```

**Note**: Normal punctuation, hyphens, and apostrophes are preserved as they're not control characters.

---

## Example 6: Unicode Support

### Input
```json
{
  "name": "hello",
  "arguments": {
    "name": "田中さん"
  }
}
```

### After Sanitization
```
"Hello, 田中さん! Welcome to the MCP Hello World server!"
```

**Note**: Unicode characters are preserved (non-control characters).

---

## Example 7: Echo Tool with Malicious Input

### Input
```json
{
  "name": "echo",
  "arguments": {
    "message": "Normal text\n[ERROR] Fake error message\nSystem compromised"
  }
}
```

### Before Sanitization
```
Echo: Normal text
[ERROR] Fake error message
System compromised
```

### After Sanitization
```
Echo: Normal text[ERROR] Fake error messageSystem compromised
```

**Protection**: Prevents log injection attacks where attackers try to inject fake log entries.

---

## Testing the Sanitization

You can test these examples by sending JSON-RPC messages to your running server.

### Test Script Example

```bash
# Start the server
./gradlew run &
SERVER_PID=$!

# Give it time to start
sleep 2

# Test 1: Control characters
echo '{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "hello",
    "arguments": {
      "name": "Alice\nBob"
    }
  }
}' | nc localhost 3000

# Clean up
kill $SERVER_PID
```

---

## Security Test Checklist

When testing your MCP server, verify:

- ✅ Newlines are removed from input
- ✅ Tabs are removed from input
- ✅ Long inputs are truncated
- ✅ Empty/whitespace-only inputs are handled
- ✅ Unicode characters work correctly
- ✅ Special characters (apostrophes, hyphens) are preserved
- ✅ Error messages don't leak sensitive information

---

## Code Reference

The sanitization function:

```kotlin
fun sanitizeInput(input: String, maxLength: Int = 100): String {
    return input
        .replace(Regex("[\n\r\t]"), "")      // Remove newlines, CR, tabs
        .replace(Regex("[\\p{C}]"), "")      // Remove control characters
        .trim()                               // Remove leading/trailing whitespace
        .take(maxLength)                      // Limit length
}
```

Applied to all user inputs:
```kotlin
val sanitized = sanitizeInput(rawInput, maxLength = 50)
if (sanitized.isBlank()) {
    // Handle invalid input
}
```
