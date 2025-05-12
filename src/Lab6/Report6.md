# JSON Parser & Building an Abstract Syntax Tree

### Course: Formal Languages & Finite Automata

### Author: Cobzari Ion

## Theory

An **Abstract Syntax Tree (AST)** is a tree representation of the abstract syntactic structure of source code or data. Each node in the tree denotes a construct occurring in the input. Unlike a concrete syntax tree (or parse tree), an AST abstracts away certain syntactic details and focuses on the hierarchical and semantic structure.

### Purpose of AST

The AST serves multiple purposes in the parsing and compilation process:
- **Simplifies semantic analysis** by removing unnecessary syntactic elements like commas or parentheses.
- **Provides a foundation** for further processing stages such as interpretation, code generation, or transformation.
- **Improves readability and modularity** for tools such as linters, formatters, and transpilers.

### Characteristics of AST

- It is **hierarchical**, where each parent node represents a higher-level construct (e.g., a JSON object or array).
- **Leaves** of the tree typically represent literals or terminal values (e.g., strings, numbers).
- **Interior nodes** represent structural elements (e.g., object, array, key-value pair).
- **No redundant syntax** (like punctuation) is present, in contrast to parse trees.

JSON (JavaScript Object Notation) is a lightweight data interchange format with the following key components:
- Objects: Unordered collections of key-value pairs
- Arrays: Ordered lists of values
- Values: Strings, numbers, booleans, null, objects, or arrays

## Objectives

1. Implement a lexical analyzer (lexer) for JSON using regular expressions
2. Define token types for all JSON elements
3. Implement a parser that builds an AST from JSON tokens
4. Visualize the AST using Graphviz DOT format

## Implementation

### Lexical Analysis


* The TokenType enum defines all possible token categories that can be identified
during the lexical analysis of a JSON string. Each enum constant corresponds to a
specific syntactic element in JSON:

- LEFT_BRACE and RIGHT_BRACE represent the opening and closing of JSON objects `{}`.
- LEFT_BRACKET and RIGHT_BRACKET represent the opening and closing of JSON arrays `[]`.
- COLON and COMMA are structural delimiters used in key-value pairs and lists.
- STRING, NUMBER, BOOLEAN, and NULL represent the valid JSON value types.
- UNKNOWN_STRING is a fallback for strings that fail to match the expected format.

This classification enables the lexer to convert raw text into a stream of
meaningful tokens for the parser to process and build the AST.
 
```java
public enum TokenType {
    LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET, 
    COLON, COMMA, STRING, NUMBER, BOOLEAN, NULL, UNKNOWN_STRING
}
```

Attempts to match a JSON-formatted string token from the current position in the input.
This method uses a regular expression to identify valid JSON strings, which start and end
with double quotes and may contain escaped characters (e.g., `\"`). If a match is found
at the current position, a new Token of type STRING is added to the token list,
the input cursor (`position`) is advanced to the end of the matched string,
and the method returns true. Otherwise, it returns false, indicating no string match was found.


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
### Syntactic Analysis
The parser builds an AST with the following node types:

JsonObject: Represents JSON objects (key-value pairs)

JsonArray: Represents JSON arrays

JsonString: Represents string values

JsonNumber: Represents numeric values

JsonBoolean: Represents boolean values

JsonNull: Represents null values

Parses a JSON object from the token stream. This method assumes that the current token
is a LEFT_BRACE `{`, which marks the beginning of a JSON object. It then repeatedly parses
key-value pairs until it encounters a RIGHT_BRACE `}`. Each key must be a STRING token,
followed by a COLON `:`, and then a value parsed via `parseValue()`. The parsed key-value
pairs are added to the `JsonObject`'s `members` map. If additional pairs follow, the method
expects a COMMA `,` to separate them. Once all members are parsed, the method consumes
the closing RIGHT_BRACE and returns the constructed `JsonObject`.
Parsing methods:

```java
private JsonObject parseObject() {
    consume(JsonLexer.TokenType.LEFT_BRACE);
    JsonObject obj = new JsonObject();

    while (!check(JsonLexer.TokenType.RIGHT_BRACE)) {
        String key = consume(JsonLexer.TokenType.STRING).value.replaceAll("^\"|\"$", "");
        consume(JsonLexer.TokenType.COLON);
        JsonValue value = parseValue();
        obj.members.put(key, value);

        if (!check(JsonLexer.TokenType.RIGHT_BRACE)) {
            consume(JsonLexer.TokenType.COMMA);
        }
    }
    consume(JsonLexer.TokenType.RIGHT_BRACE);
    return obj;
}
```
AST Visualization
Recursively generates a Graphviz DOT representation of the JSON object and its children.
This method appends DOT syntax to the provided StringBuilder, representing nodes and edges
that form the structure of the Abstract Syntax Tree (AST) for a JSON object.
Each object is represented as a node labeled "Object", and each key in the object becomes
a child node labeled with the key string. An edge is created from the object node to the key node,
and then the associated value (a JsonValue) is recursively processed, building its subtree.
The `nodeId` array acts as a mutable counter to ensure unique node identifiers across recursive calls.
The optional `parentId` allows the method to connect the current object to its parent in the DOT tree.

The AST is visualized using Graphviz DOT format:

```java
void toDot(StringBuilder sb, String parentId, int[] nodeId) {
    String thisId = "node" + nodeId[0]++;
    sb.append(thisId).append(" [label=\"Object\"];\n");
    if (parentId != null) sb.append(parentId).append(" -> ").append(thisId).append(";\n");

    for (Map.Entry<String, JsonValue> entry : members.entrySet()) {
        String keyId = "node" + nodeId[0]++;
        sb.append(keyId).append(" [label=\"").append(entry.getKey()).append("\"];\n");
        sb.append(thisId).append(" -> ").append(keyId).append(";\n");
        entry.getValue().toDot(sb, keyId, nodeId);
    }
}
```
### Results
The implementation successfully:

Tokenizes JSON input using regular expressions

Parses tokens into an appropriate AST structure

Generates a DOT file for visualization

Example JSON input:

```json
{
  "person": {
    "name": "Alice",
    "age": 30,
    "contact": {
      "email": "alice@example.com",
      "phones": ["123-4567", "987-6543"]
    }
  }
}
```
Generated AST visualization:
```
digraph AST {
    node0 [label="Object"];
    node1 [label="person"];
    node0 -> node1;
    node2 [label="Object"];
    node1 -> node2;
    node3 [label="name"];
    node2 -> node3;
    node4 [label="String: Alice"];
    node3 -> node4;
    ...
}
```
### Conclusions
Through this laboratory work, we successfully implemented a complete JSON parser, consisting of both lexical and syntactic analysis components. The Abstract Syntax Tree (AST) accurately captures the hierarchical structure of JSON data, providing a clear and compact representation of its semantics. Additionally, the DOT-based visualization of the AST offers valuable insight into the parsing process and the underlying structure of the input. This project effectively demonstrates how theoretical concepts from formal language processing—such as tokenization, grammar rules, and tree-based representations—can be applied to real-world data formats like JSON. The full implementation is provided in the accompanying Java source files and serves as a practical example of building a custom parser from scratch.
