# Lexer & Scanner

### Course: Formal Languages & Finite Automata

### Author: Cobzari Ion

---

## Theory

Lexical analysis is the process of converting a sequence of characters into a sequence of **tokens**, which are meaningful units of a language. These tokens represent identifiers, keywords, symbols, operators, and literals that conform to the syntax of the language being analyzed. At the core of lexical analysis lies the use of **regular expressions**, a powerful tool for pattern matching and text processing.

The role of a lexical analyzer, or lexer, is to break down input text into tokens, similar to how words and punctuation are identified in natural language processing. Lexers are typically the first phase in a compiler or interpreter pipeline.

### Role of a Lexer for JSON

For this laboratory work, I implemented a lexer for **JSON (JavaScript Object Notation)**, a lightweight data interchange format. The lexer is responsible for recognizing JSON tokens such as braces, brackets, colons, commas, strings, numbers, booleans, and `null`. JSON is widely used in web development and APIs, making this lexer a practical tool for processing JSON data.

---

## Objectives

1. Understand what lexical analysis is.
2. Get familiar with the inner workings of a lexer/scanner/tokenizer.
3. Implement a sample lexer and demonstrate how it works.

---

## Implementation Description

The following steps were taken to implement the JSON lexer in Java. Below, I explain the key components of the lexer, including the token types, token class, and the logic for matching tokens.

---

### Token Types

The lexer defines the following token types using an `enum`:

```java
public enum TokenType {
    LEFT_BRACE,   // Matches '{'
    RIGHT_BRACE,  // Matches '}'
    LEFT_BRACKET, // Matches '['
    RIGHT_BRACKET,// Matches ']'
    COLON,        // Matches ':'
    COMMA,        // Matches ','
    STRING,       // Matches JSON strings (e.g., "name")
    NUMBER,       // Matches JSON numbers (e.g., 42, 3.14)
    BOOLEAN,      // Matches 'true' or 'false'
    NULL,         // Matches 'null'
    UNKNOWN       // Catches any unrecognized input
}
```

---

### Token Class

The `Token` class represents a token with its type, value, and position in the input:

```java

    public Token(TokenType type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

```

---

### Lexer Logic

The lexer processes the input string and generates tokens based on regex patterns. Below are the key methods:

#### 1. **`matchToken` Method**

This method matches a regex pattern and adds a token if a match is found.
How it works?
Create a matcher for the input substring starting from the current position.Check if the pattern matches at the current position
Add a new token to the list
Update the position to continue processing after the matched substring
```java
private boolean matchToken(List<Token> tokens, String regex, TokenType type) {
    
    Matcher matcher = Pattern.compile(regex).matcher(input.substring(position));
   if (matcher.lookingAt()) {
       tokens.add(new Token(type, matcher.group(), position));
        position += matcher.end();
        return true;
    }
    return false;
}
```

#### 2. **`matchString` Method**

This method matches JSON strings (e.g., `"name"`).

Regex to match JSON strings (supports escaped quotes)
Add the matched string as a token. Update the position
```java
private boolean matchString(List<Token> tokens) {
    Matcher matcher = Pattern.compile("\"(?:\\\\\"|[^\"])*?\"").matcher(input.substring(position));
    
    if (matcher.lookingAt()) {
        tokens.add(new Token(TokenType.STRING, matcher.group(), position));
        position += matcher.end();
        return true;
    }
    return false;
}
```

#### 3. **`matchNumber` Method**

This method matches JSON numbers (e.g., `42`, `3.14`):

```java
private boolean matchNumber(List<Token> tokens) {
    // Regex to match integers and floating-point numbers
    Matcher matcher = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(input.substring(position));
    
    if (matcher.lookingAt()) {
        // Add the matched number as a token
        tokens.add(new Token(TokenType.NUMBER, matcher.group(), position));
        
        // Update the position
        position += matcher.end();
        return true;
    }
    return false;
}
```

#### 4. **`matchBoolean` Method**

The `matchBoolean` method identifies and tokenizes boolean values (`true` or `false`) in the input JSON string. It uses a regex pattern (`"true|false"`) to match these values at the current position in the input. If a match is found, it creates a `Token` of type `BOOLEAN`, adds it to the token list, and updates the position to continue processing. This method ensures that boolean values are correctly recognized and tokenized during lexical analysis.
```java
private boolean matchBoolean(List<Token> tokens) {
    // Regex to match 'true' or 'false'
    Matcher matcher = Pattern.compile("true|false").matcher(input.substring(position));
    
    if (matcher.lookingAt()) {
        // Add the matched boolean as a token
        tokens.add(new Token(TokenType.BOOLEAN, matcher.group(), position));
        
        // Update the position
        position += matcher.end();
        return true;
    }
    return false;
}
```

#### 5. **`matchNull` Method**


The `matchNull` method identifies and tokenizes the `null` value in the input JSON string. It uses a regex pattern (`"null"`) to match the literal `null` at the current position in the input. If a match is found, it creates a `Token` of type `NULL`, adds it to the token list, and updates the position to continue processing. This method ensures that `null` values are correctly recognized and tokenized during lexical analysis.
```java
private boolean matchNull(List<Token> tokens) {
    // Regex to match 'null'
    Matcher matcher = Pattern.compile("null").matcher(input.substring(position));
    
    if (matcher.lookingAt()) {
        // Add the matched null as a token
        tokens.add(new Token(TokenType.NULL, matcher.group(), position));
        
        // Update the position
        position += matcher.end();
        return true;
    }
    return false;
}
```

---

### Tokenization Examples

#### Input:
```json
{ "name": "Alice", "age": 25, "isStudent": true, "scores": [95, 82, 88], "address": null }
```

#### Output:
```
Token(LEFT_BRACE, {, 0)
Token(STRING, "name", 2)
Token(COLON, :, 7)
Token(STRING, "Alice", 9)
Token(COMMA, ,, 15)
Token(STRING, "age", 17)
Token(COLON, :, 21)
Token(NUMBER, 25, 23)
Token(COMMA, ,, 25)
Token(STRING, "isStudent", 27)
Token(COLON, :, 36)
Token(BOOLEAN, true, 38)
Token(COMMA, ,, 42)
Token(STRING, "scores", 44)
Token(COLON, :, 50)
Token(LEFT_BRACKET, [, 52)
Token(NUMBER, 95, 53)
Token(COMMA, ,, 55)
Token(NUMBER, 82, 57)
Token(COMMA, ,, 59)
Token(NUMBER, 88, 61)
Token(RIGHT_BRACKET, ], 63)
Token(COMMA, ,, 64)
Token(STRING, "address", 66)
Token(COLON, :, 74)
Token(NULL, null, 76)
Token(RIGHT_BRACE, }, 80)
```

---

### Error Handling

### Short Paragraph Description

The `matchUnknownString` method identifies and tokenizes any unrecognized string enclosed in double quotes in the input JSON string. It uses a regex pattern (`"\"[^\"]+\""`) to match strings that do not conform to other specific token rules. If a match is found, it creates a `Token` of type `UNKNOWN_STRING`, adds it to the token list, and updates the position to continue processing. This method ensures that unrecognized strings are captured and handled gracefully during lexical analysis.
```java
private boolean matchUnknownString(List<Token> tokens) {
    // Regex to match any unrecognized string
    Matcher matcher = Pattern.compile("\"[^\"]+\"").matcher(input.substring(position));
    
    if (matcher.lookingAt()) {
        // Add the unknown string as a token
        tokens.add(new Token(TokenType.UNKNOWN_STRING, matcher.group(), position));
        
        // Update the position
        position += matcher.end();
        return true;
    }
    return false;
}
```

---

## Conclusions / Results

The JSON lexer successfully tokenizes JSON input into meaningful tokens, demonstrating the practical application of lexical analysis. By leveraging regex patterns, the lexer efficiently identifies JSON structures such as objects, arrays, strings, numbers, booleans, and `null`. This implementation provides a solid foundation for building more advanced tools, such as JSON parsers or validators.

### Key Takeaways:
1. **Regex is Powerful**: Regular expressions are essential for defining token patterns in a lexer. They allow for concise and efficient matching of complex structures.
2. **Modular Design**: The lexer is designed with modular methods (`matchToken`, `matchString`, etc.), making it easy to extend or modify.
3. **Error Handling**: The lexer gracefully handles unknown characters and invalid input, ensuring robust performance.
4. **Practical Application**: This lexer can be integrated into larger systems, such as JSON parsers or data processing pipelines.


## Bibliography

1. [Java Regular Expressions (Oracle Docs)](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)
2. [JSON Specification](https://www.json.org/json-en.html)

--- 

