# Kotlin Programming Guide

## Introduction
Kotlin is a modern, statically typed programming language that runs on the JVM and is fully interoperable with Java.

## Key Features

### Null Safety
Kotlin's type system distinguishes between nullable and non-nullable types:
- Use `?` for nullable types: `String?`
- Safe call operator: `variable?.method()`
- Elvis operator: `value ?: default`

### Coroutines
Kotlin provides first-class support for asynchronous programming:
- `suspend` functions for async operations
- Structured concurrency with coroutine scopes
- Flow for reactive streams

### Extension Functions
Add functionality to existing classes without inheritance:
```kotlin
fun String.isPalindrome(): Boolean {
    return this == this.reversed()
}
```

### Data Classes
Automatically generate equals, hashCode, toString, and copy:
```kotlin
data class User(val name: String, val age: Int)
```

## Common Patterns

### Singleton
```kotlin
object DatabaseConnection {
    fun connect() { }
}
```

### Sealed Classes
Represent restricted class hierarchies:
```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String) : Result<T>()
}
```

### Higher-Order Functions
Functions that take functions as parameters:
```kotlin
fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}
```

## Best Practices

1. **Prefer immutability**: Use `val` over `var`
2. **Use data classes**: For simple data holders
3. **Leverage extension functions**: Keep code clean and readable
4. **Handle nulls safely**: Avoid !! operator
5. **Use coroutines**: For async operations
