
# Chomsky Normal Form

### Course: Formal Languages & Finite Automata

### Author: Cobzari Ion

## Theory

One fundamental concept in the area of formal languages and finite automata is the Chomsky Normal Form (CNF), named after the linguist Noam Chomsky. CNF is a way of simplifying grammars to a standardized form, where every production rule fits a specific pattern. A grammar is said to be in CNF if all its production rules are of the form `A → BC` or `A → a`, where `A`, `B`, and `C` are non-terminal symbols, and `a` is a terminal symbol. The simplification to CNF is crucial as it allows for the implementation of efficient parsing algorithms.

The process of transforming an arbitrary context-free grammar into Chomsky Normal Form involves several systematic steps, each aimed at reducing the complexity of the grammar while preserving the language it generates. The typical steps include eliminating null productions (productions that generate the empty string), removing unit productions (productions where a non-terminal is mapped directly to another non-terminal), removing non-productive symbols (symbols that cannot be reached from the start symbol), and removing non-reachable symbols (symbols that cannot generate any terminal string). Lastly, we have to ensure that all remaining productions meet the CNF criteria. This normalization process is not merely a mechanical transformation but requires a deep understanding of the underlying structure of the grammar and the language it defines.

## Objectives

1. Learn about Chomsky Normal Form (CNF).
2. Get familiar with the approaches of normalizing a grammar.
3. Implement a method for normalizing an input grammar by the rules of CNF.
    1. The implementation needs to be encapsulated in a method with an appropriate signature (also ideally in an appropriate class/type).
    2. The implemented functionality needs executed and tested.
    3. A **BONUS point** will be given for the student who will have unit tests that validate the functionality of the project.
    4. Also, another **BONUS point** would be given if the student will make the aforementioned function to accept any grammar, not only the one from the student's variant.

## Implementation description

### Grammar Class

The provided Java code defines a class Grammar that encapsulates a context-free grammar and includes methods for transforming it into Chomsky Normal Form. The transformation process involves several steps, each targeting a specific type of production rule that does not conform to CNF.

The `Grammar` class is defined to encapsulate the components of a context-free grammar: non-terminals, terminals, production rules, and a start symbol. This encapsulation facilitates the operations and transformations required on the grammar, such as converting it to CNF.

```java
public class Grammar {
    private List<String> nonTerminals;
    private List<String> terminals;
    private Map<String, List<String>> rules;
    private String start;

    private static final String EPSILON = "ε"; // You can change it depending on your EPSILON symbol

    public Grammar(List<String> nonTerminals, List<String> terminals, 
                  Map<String, List<String>> rules, String start) {
        this.nonTerminals = new ArrayList<>(nonTerminals);
        this.terminals = new ArrayList<>(terminals);
        this.rules = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rules.entrySet()) {
            this.rules.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        this.start = start;
    }
}
```

The `printRules` method iterates through the grammar's production rules and prints them in a readable format. For each non-terminal symbol, it prints the symbol followed by an arrow (->), and then the corresponding production rules joined by the pipe symbol (|) to denote alternative productions in a grammar.

```java
public void printRules() {
    for (String nonTerminal : rules.keySet()) {
        System.out.println(nonTerminal + " -> " + String.join(" | ", rules.get(nonTerminal)));
    }
}
```

### Eliminating Epsilon Productions

The `eliminateEpsilonProductions` method in the `Grammar` class identifies and removes epsilon (ε) productions—rules that produce an empty string—while ensuring that the language of the grammar is preserved.

The method begins by identifying all nullable non-terminals, which are non-terminals that can directly or indirectly produce an empty string. It initializes a set nullable to keep track of such non-terminals. For each non-terminal, the method iterates through its production rules. If any production is equal to `EPSILON`, the non-terminal is added to the set.

```java
Set<String> nullable = new HashSet<>();

for (String nonTerminal : nonTerminals) {
    for (String production : rules.get(nonTerminal)) {
        if (production.equals(EPSILON)) {
            nullable.add(nonTerminal);
        }
    }
}
```

The provided code segment implements the process of identifying indirectly nullable non-terminals within a context-free grammar during the elimination of epsilon (ε) productions. It initializes a changes flag to true to control a while loop that continues until no further nullable symbols are found. For each non-terminal that is not yet marked as nullable, the code iterates through its production rules. For each production, it checks whether all symbols in the production are themselves nullable. If so, the non-terminal is added to the set of nullable symbols, and the changes flag is set to true, indicating that another iteration is necessary to capture potential new nullable dependencies. This iterative expansion ensures that any non-terminal which can eventually produce ε through a sequence of nullable productions is correctly marked, thus maintaining the correctness and completeness of the grammar transformation process.
```java
boolean changes = true;
while (changes) {
    changes = false;
    for (String nonTerminal : nonTerminals) {
        if (!nullable.contains(nonTerminal)) {
            for (String production : rules.get(nonTerminal)) {
                boolean allNullable = true;
                for (char symbol : production.toCharArray()) {
                    if (!nullable.contains(String.valueOf(symbol))) {
                        allNullable = false;
                        break;
                    }
                }
                if (allNullable) {
                    nullable.add(nonTerminal);
                    changes = true;
                    break;
                }
            }
        }
    }
}
```

This code segment handles the reconstruction of the grammar's production rules after identifying all nullable non-terminals. It creates a new mapping newRules to store the updated set of productions for each non-terminal. For each non-terminal in the grammar, it iterates through its original production rules. If a production is not equal to EPSILON (the empty string), it calls the expandNullableProduction method to generate all possible expansions where nullable symbols are selectively omitted. These expansions are collected into newProds. To ensure uniqueness and avoid redundant productions, the list is converted into a HashSet and then back into a List before being stored in newRules. After processing all non-terminals, the grammar’s rules are updated to reference this newly constructed set of production rules. This ensures that all epsilon productions are effectively removed while preserving the original language generated by the grammar.

```java
Map<String, List<String>> newRules = new HashMap<>();
for (String nonTerminal : rules.keySet()) {
    List<String> newProds = new ArrayList<>();
    for (String production : rules.get(nonTerminal)) {
        if (!production.equals(EPSILON)) {
            newProds.addAll(expandNullableProduction(production, nullable));
        }
    }
    newRules.put(nonTerminal, new ArrayList<>(new HashSet<>(newProds)));
}
rules = newRules;
```

The expandNullableProduction method is designed to generate all possible expansions of a given production by selectively omitting nullable symbols. It begins by initializing a list expansions with an empty string, representing the base case for building combinations. For each symbol in the input production, the method checks if the symbol is nullable. If the symbol is nullable, two types of expansions are created for each existing partial expansion: one including the symbol and one excluding it. If the symbol is not nullable, it is simply appended to all current partial expansions. This iterative process systematically builds all possible sequences derived from the original production by optionally skipping nullable symbols. After processing all symbols, the method filters out any empty expansions (except the initial one) to ensure that only meaningful productions are returned. The resulting list of expanded productions enables the grammar to eliminate epsilon derivations while preserving all valid derivation paths.


```java
private List<String> expandNullableProduction(String production, Set<String> nullable) {
    List<String> expansions = new ArrayList<>();
    expansions.add("");

    for (char symbol : production.toCharArray()) {
        List<String> newExpansions = new ArrayList<>();
        if (nullable.contains(String.valueOf(symbol))) {
            for (String expansion : expansions) {
                newExpansions.add(expansion + symbol);
                newExpansions.add(expansion);
            }
        } else {
            for (String expansion : expansions) {
                newExpansions.add(expansion + symbol);
            }
        }
        expansions = newExpansions;
    }

    List<String> result = new ArrayList<>();
    for (String exp : expansions) {
        if (!exp.isEmpty()) {
            result.add(exp);
        }
    }
    return result;
}
```

### Eliminating Renaming Productions

The eliminateRenaming method systematically removes unit productions from the grammar. A unit production is a production of the form A → B, where both A and B are non-terminal symbols. The method operates iteratively, using a changes flag to track whether any modifications are made in an iteration. For each non-terminal, it first collects all unit productions into a temporary list. Then, for each unit production, it replaces the direct mapping to another non-terminal by copying all productions of the target non-terminal into the current non-terminal's production list. After copying, the original unit production is removed to prevent duplication. To maintain a clean production set, duplicates are eliminated by converting the list into a HashSet and then back to a List. Additionally, after processing all units, the method removes any remaining unit productions that could have been introduced indirectly. This loop continues until no further unit productions exist, ensuring that the grammar no longer has any direct non-terminal-to-non-terminal mappings, as required by Chomsky Normal Form.

```java
public void eliminateRenaming() {
    boolean changes = true;
    while (changes) {
        changes = false;
        for (String nonTerminal : new ArrayList<>(nonTerminals)) {
            List<String> unitProductions = new ArrayList<>();
            for (String production : rules.get(nonTerminal)) {
                if (nonTerminals.contains(production)) {
                    unitProductions.add(production);
                }
            }

            for (String unit : unitProductions) {
                List<String> unitProductionsRules = rules.get(unit);
                if (unitProductionsRules != null) {
                    rules.get(nonTerminal).addAll(unitProductionsRules);
                    rules.get(nonTerminal).remove(unit);
                    rules.put(nonTerminal, new ArrayList<>(new HashSet<>(rules.get(nonTerminal))));
                    changes = true;
                }
            }

            rules.get(nonTerminal).removeIf(p -> nonTerminals.contains(p));
        }
    }
}
```

### Eliminating Inaccessible Symbols

The eliminateInaccessibleSymbols method is responsible for detecting and removing non-terminals that cannot be reached from the start symbol of the grammar. It begins by initializing the accessible set with the start symbol and iteratively expands it by scanning productions. In each iteration, for every accessible non-terminal, the method examines its production rules and identifies new non-terminals that appear within these productions. If such a non-terminal has not been marked as accessible, it is added to the set, and the changes flag is triggered to continue the discovery process. This loop continues until no new accessible symbols are found. After all accessible non-terminals have been identified, the method updates the list of non-terminals to include only those reachable from the start symbol. It also reconstructs the grammar's production rules to retain only the rules corresponding to accessible non-terminals. This ensures that the grammar is simplified by discarding unreachable components without affecting the language generated by the grammar.

```java
public void eliminateInaccessibleSymbols() {
    Set<String> accessible = new HashSet<>();
    accessible.add(start);

    boolean changes = true;
    while (changes) {
        changes = false;
        Set<String> newAccessible = new HashSet<>(accessible);
        for (String nonTerminal : accessible) {
            List<String> productions = rules.get(nonTerminal);
            if (productions != null) {
                for (String production : productions) {
                    for (char symbol : production.toCharArray()) {
                        if (nonTerminals.contains(String.valueOf(symbol)) 
                            && !accessible.contains(String.valueOf(symbol))) {
                            newAccessible.add(String.valueOf(symbol));
                            changes = true;
                        }
                    }
                }
            }
        }
        accessible = newAccessible;
    }

    List<String> updatedNonTerminals = new ArrayList<>();
    for (String nt : nonTerminals) {
        if (accessible.contains(nt)) {
            updatedNonTerminals.add(nt);
        }
    }
    nonTerminals = updatedNonTerminals;

    Map<String, List<String>> newRules = new HashMap<>();
    for (String nt : accessible) {
        newRules.put(nt, rules.get(nt));
    }
    rules = newRules;
}
```

### Eliminating Non-Productive Symbols

The eliminateNonProductiveSymbols method identifies and removes non-terminals that cannot derive any terminal string, thus simplifying the grammar without altering the language it generates. It initializes the productive set with the start symbol and iteratively expands it by scanning through the grammar’s production rules. For each non-terminal, if all symbols within a production are either terminals or previously marked as productive non-terminals, the non-terminal itself is considered productive and added to the set. This process repeats until no new productive symbols are discovered. Once identified, the grammar’s list of non-terminals is updated to include only productive symbols. Additionally, production rules are filtered: only those productions whose symbols are entirely productive or terminal are retained. This ensures that the resulting grammar consists exclusively of symbols that contribute to generating terminal strings, thereby eliminating redundant or non-contributing parts of the grammar.

```java
public void eliminateNonProductiveSymbols() {
    Set<String> productive = new HashSet<>();
    productive.add(start);

    boolean changes = true;
    while (changes) {
        changes = false;
        for (String nonTerminal : nonTerminals) {
            if (!productive.contains(nonTerminal)) {
                for (String production : rules.get(nonTerminal)) {
                    boolean allProductive = true;
                    for (char symbol : production.toCharArray()) {
                        if (!terminals.contains(String.valueOf(symbol)) 
                            && !productive.contains(String.valueOf(symbol))) {
                            allProductive = false;
                            break;
                        }
                    }
                    if (allProductive) {
                        productive.add(nonTerminal);
                        changes = true;
                        break;
                    }
                }
            }
        }
    }

    List<String> updatedNonTerminals = new ArrayList<>();
    for (String nt : nonTerminals) {
        if (productive.contains(nt)) {
            updatedNonTerminals.add(nt);
        }
    }
    nonTerminals = updatedNonTerminals;

    Map<String, List<String>> newRules = new HashMap<>();
    for (String nt : productive) {
        List<String> filteredProductions = new ArrayList<>();
        for (String production : rules.get(nt)) {
            boolean valid = true;
            for (char symbol : production.toCharArray()) {
                if (!terminals.contains(String.valueOf(symbol)) 
                    && !productive.contains(String.valueOf(symbol))) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                filteredProductions.add(production);
            }
        }
        newRules.put(nt, filteredProductions);
    }
    rules = newRules;
}
```

### Converting to Chomsky Normal Form

The `isCNF` method checks if the grammar is already in Chomsky Normal Form.

```java
public boolean isCNF() {
    for (String nonTerminal : rules.keySet()) {
        for (String production : rules.get(nonTerminal)) {
            if (production.length() == 0 || production.length() > 2) {
                return false;
            }
            if (production.length() == 1 && !terminals.contains(production)) {
                return false;
            }
            if (production.length() == 2) {
                for (char symbol : production.toCharArray()) {
                    if (terminals.contains(String.valueOf(symbol))) {
                        return false;
                    }
                }
            }
        }
    }
    return true;
}
```


## Conclusions / Results

In conclusion, this report has explored the process of transforming context-free grammars into Chomsky Normal Form using Java. The `Grammar` class serves as a framework for encapsulating grammars and applying transformations to adhere to CNF's requirements. Each step of the transformation process—eliminating epsilon productions, renaming productions, inaccessible symbols, and non-productive symbols—has been implemented to ensure the grammar's language is preserved while simplifying its structure. The final conversion to CNF ensures that each production is either of the form `A → BC` or `A → a`, making the grammar suitable for efficient parsing algorithms.