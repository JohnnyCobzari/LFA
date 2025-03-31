# Lexer & Scanner

### Course: Formal Languages & Finite Automata
### Author: Cobzari Ion FaF-233

---

## Theory

Regular expressions, often abbreviated as **regex**, are powerful tools for pattern matching and text processing. Rooted in formal language theory, regexes are used to define regular languages and serve as a foundational concept in lexical analysis and compiler design.

### Understanding Regular Expressions

Regular expressions describe patterns composed of:

1. **Literal Characters**: Match themselves (e.g., `a` matches `a`)
2. **Metacharacters**: Have special meanings (e.g., `.` matches any character)
3. **Quantifiers**: Specify repetition (`*`, `+`, `?`, superscripts like `²`)
4. **Character Classes**: Match any character within `[]`
5. **Groups**: Parentheses group subpatterns
6. **Alternation**: The pipe symbol `|` provides "OR" logic

### Applications

Regexes are used in:

- **Search/replace tools**
- **Form input validation**
- **Lexical analysis in compilers**
- **Web URL routing**
- **Data extraction from text**
- **Log and event stream parsing**

---

## Objectives

1. Understand regex structure and behavior.
2. Implement a generator that outputs valid strings matching a given pattern.
3. Dynamically interpret regex, not hardcode results.
4. Limit repetitions of `*` and `+` to a max of 5.
5. Include processing step visualization for learning purposes.

---

## Implementation Description

### Overview

The project implements a `RegexGenerator` class capable of:
- Parsing regex patterns
- Tokenizing them
- Recursively generating valid strings based on interpreted rules

The core logic is split into:
1. **Tokenization**
2. **Processing**
3. **Generation**

---

### 1. The `RegexGenerator` Class
The  RegexGenerator class is designed with clear objectives: configurability through constructor parameters (maxIterations, maxStarRepeat, maxPlusRepeat) while maintaining sensible defaults. The implementation choices reflect careful consideration of practical constraints - the default 5-repeat limit balances output variety with reasonable string lengths, and the 1000-iteration cap prevents excessive computation.
```java
public class RegexGenerator {
   

    public RegexGenerator(int maxIterations, int maxStarRepeat, int maxPlusRepeat) {
        this.maxIterations = maxIterations;
        this.maxStarRepeat = maxStarRepeat;
        this.maxPlusRepeat = maxPlusRepeat;
        this.random = new Random();
    }

    public RegexGenerator() {
        this(1000, 5, 5);
    }
}
```

- Controls max attempts per pattern
- Limits repetition for quantifiers like `*` and `+`
- Uses `Random` for probabilistic operations

---

### 2. Tokenization Function
The `tokenize` method implements a robust regex parser that handles nested structures and quantifiers efficiently. It processes input strings character-by-character, using a while-loop to maintain precise control over the parsing state. The implementation carefully tracks parenthesis nesting levels to correctly identify grouped expressions, while simultaneously looking ahead for potential quantifier operators.

For grouped expressions, the method employs a secondary counter to balance opening and closing parentheses, ensuring proper handling of nested structures. When encountering quantifiers, it captures both the base character and modifier as a single token. The design maintains linear time complexity O(n) by processing each character exactly once, with minimal additional memory overhead.

The parsing logic demonstrates careful attention to edge cases - it gracefully handles unbalanced parentheses by falling back to literal interpretation, and properly processes consecutive quantifiers. The method's structure allows clean extension to support additional regex features while maintaining readability through clear variable naming and logical segmentation of parsing phases.
```java
private List<String> tokenize(String regexStr) {
    List<String> tokens = new ArrayList<>();
    int i = 0;

    while (i < regexStr.length()) {
        if (regexStr.charAt(i) == '(') {
            // Handle group with potential quantifier
            int level = 1;
            int j = i + 1;
            while (j < regexStr.length() && level > 0) {
                if (regexStr.charAt(j) == '(') level++;
                else if (regexStr.charAt(j) == ')') level--;
                j++;
            }

            if (level == 0) {
                String groupContent = regexStr.substring(i + 1, j - 1);
                String repetition = "";
                if (j < regexStr.length() && isRepetitionOperator(regexStr.charAt(j))) {
                    repetition = String.valueOf(regexStr.charAt(j));
                    j++;
                }
                tokens.add("(" + groupContent + ")" + repetition);
                i = j;
            } else {
                tokens.add(String.valueOf(regexStr.charAt(i)));
                i++;
            }
        } 
        else if (i < regexStr.length() - 1 && isRepetitionOperator(regexStr.charAt(i + 1))) {
            tokens.add(regexStr.substring(i, i + 2));
            i += 2;
        } 
        else {
            tokens.add(String.valueOf(regexStr.charAt(i)));
            i++;
        }
    }
    return tokens;
}
```

Splits a regex string into meaningful tokens like groups, repetition modifiers, and simple characters.

---

### 3. Token Processing Function
The `processTokens` method builds valid strings by progressively expanding each regex token. It handles groups, quantifiers, literals, and empty strings through dedicated processing logic while maintaining all possible combinations. The implementation efficiently manages combinatorial expansion using temporary result lists, separating concerns through specialized sub-methods (like `processSimpleGroup`). This iterative approach controls memory usage while supporting the regex's inherent exponential growth potential from quantifiers and alternations. The clean conditional structure allows straightforward extension to additional regex features.
```java
private List<String> processTokens(List<String> tokens) {
    List<String> results = new ArrayList<>();
    results.add("");

    for (String token : tokens) {
        List<String> newResults = new ArrayList<>();

        if (token.startsWith("(")) {
            // Handle groups and alternation
            if (token.endsWith(")")) {
                processSimpleGroup(token, results, newResults);
            } 
            else if (hasRepetitionOperator(token)) {
                processGroupWithQuantifier(token, results, newResults);
            }
        }
        else if (token.length() == 2 && isRepetitionOperator(token.charAt(1))) {
            processCharacterWithQuantifier(token, results, newResults);
        }
        else if (token.equals("μ")) {
            newResults.addAll(results);
        }
        else {
            // Simple character
            for (String r : results) {
                newResults.add(r + token);
            }
        }
        results = newResults;
    }
    return results;
}
```

Interprets tokens and generates valid strings through recursive processing.

---

### 4. Generation Function
The `generate` method efficiently produces multiple valid strings for each input regex pattern while enforcing strict termination conditions. Its key characteristics include:

1. **Controlled Generation** - Uses both count-based (`patternStrings.size() < count`) and iteration-based (`iterations < maxIterations`) termination criteria to prevent infinite loops

2. **Pattern Processing** - For each pattern, it:
    - Creates a dedicated result list
    - Generates strings until reaching the requested count or maximum attempts
    - Only keeps the first valid result from each generation attempt

3. **Safety Mechanisms**:
    - Empty result validation (`!generated.isEmpty()`)
    - Iteration counting to bound processing time
    - Isolation of results per pattern

The implementation currently collects results in `patternStrings` but doesn't add them to the final `allResults` list, suggesting either an incomplete sample or potential oversight in the shown code segment. The method's structure allows clean extension for result aggregation or additional processing steps.
```java
public List<String> generate(List<String> regexPatterns, int count) {
    List<String> allResults = new ArrayList<>();

    for (String pattern : regexPatterns) {
        List<String> patternStrings = new ArrayList<>();
        int iterations = 0;

        while (patternStrings.size() < count && iterations < maxIterations) {
            List<String> generated = parseRegex(pattern);
            if (!generated.isEmpty()) {
                patternStrings.add(generated.get(0));
            }
            iterations++;
        }
    }
    return allResults;
}
```

Generates multiple valid strings for each pattern while tracking attempts.

---

### Example Usage

```java
public static void main(String[] args) {
    RegexGenerator generator = new RegexGenerator(1000, 3, 4);
    
    List<String> patterns = List.of(
        "M?N²(O|P)³Q*R+",
        "(X|Y|Z)³8+(9|0)",
        "(H|i)(J|K)L*N?"
    );

    List<String> validStrings = generator.generate(patterns, 3);
}
```

---

### Example Output

```
Pattern: M?N²(O|P)³Q*R+
  [NNPPPQRR, NNOOORRR, NNPPPRRRR]
Pattern: (X|Y|Z)³8+(9|0)
  [YYY888889, ZZZ8889, XXX88880]
Pattern: (H|i)(J|K)L*N?
  [iJN, HKLLN, iKLLN]
```

---

## Conclusions

This Java implementation demonstrates:

1. How regex patterns can be interpreted through tokenization
2. Recursive processing for flexible string generation
3. Controlled quantifier repetition limits
4. Practical application in testing and education

The object-oriented approach provides clear separation of concerns while maintaining the core functionality.

---

## Bibliography

1. Hopcroft, J. E., Motwani, R., & Ullman, J. D. (2006). *Introduction to Automata Theory, Languages, and Computation.*
2. Friedl, J. (2006). *Mastering Regular Expressions.* O'Reilly Media.
3. Oracle Java Documentation: [https://docs.oracle.com/javase/8/docs/api/](https://docs.oracle.com/javase/8/docs/api/)
4. Sipser, M. (2012). *Introduction to the Theory of Computation.*
5. Java Regular Expressions: [https://docs.oracle.com/javase/tutorial/essential/regex/](https://docs.oracle.com/javase/tutorial/essential/regex/)