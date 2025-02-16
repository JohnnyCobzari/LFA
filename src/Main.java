import java.util.*;

class Grammar {
    private Set<Character> nonTerminals;
    private Set<Character> terminals;
    private Map<Character, List<String>> rules;
    private char startSymbol;

    public Grammar(Set<Character> nonTerminals, Set<Character> terminals, Map<Character, List<String>> rules, char startSymbol) {
        this.nonTerminals = nonTerminals;
        this.terminals = terminals;
        this.rules = rules;
        this.startSymbol = startSymbol;
    }

    public String generateString(char symbol, int length, int maxLength) {
        if (length > maxLength) return "";
        if (terminals.contains(symbol)) return String.valueOf(symbol);

        List<String> productions = rules.get(symbol);
        if (productions == null || productions.isEmpty()) return "";

        String chosenProduction = productions.get(new Random().nextInt(productions.size()));
        StringBuilder result = new StringBuilder();
        for (char ch : chosenProduction.toCharArray()) {
            result.append(generateString(ch, length + 1, maxLength));
        }
        return result.toString();
    }

    public List<String> generateStrings(int count) {
        List<String> generatedStrings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            generatedStrings.add(generateString(startSymbol, 0, 15));
        }
        return generatedStrings;
    }

    public FiniteAutomaton convertToFA() {
        Map<Character, Map<Character, Character>> transitions = new HashMap<>();
        Set<Character> states = new HashSet<>(nonTerminals);
        Set<Character> acceptStates = new HashSet<>();
        char initialState = startSymbol;
        char deadState = 'X';
        states.add(deadState);

        for (char nt : nonTerminals) {
            transitions.put(nt, new HashMap<>());
        }
        transitions.put(deadState, new HashMap<>());

        for (char nt : rules.keySet()) {
            for (String production : rules.get(nt)) {
                if (production.length() == 1) {
                    transitions.get(nt).put(production.charAt(0), deadState);
                    acceptStates.add(deadState);
                } else {
                    char terminal = production.charAt(0);
                    char nextState = production.charAt(1);
                    transitions.get(nt).put(terminal, nextState);
                }
            }
        }

        return new FiniteAutomaton(states, terminals, transitions, initialState, acceptStates);
    }
}

class FiniteAutomaton {
    private Set<Character> states;
    private Set<Character> alphabet;
    private Map<Character, Map<Character, Character>> transitions;
    private char initialState;
    private Set<Character> acceptStates;

    public FiniteAutomaton(Set<Character> states, Set<Character> alphabet, Map<Character, Map<Character, Character>> transitions, char initialState, Set<Character> acceptStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.initialState = initialState;
        this.acceptStates = acceptStates;
    }

    public boolean accepts(String input) {
        char currentState = initialState;
        for (char symbol : input.toCharArray()) {
            if (!alphabet.contains(symbol) || !transitions.containsKey(currentState) || !transitions.get(currentState).containsKey(symbol)) {
                return false;
            }
            currentState = transitions.get(currentState).get(symbol);
        }
        return acceptStates.contains(currentState);
    }
}

public class Main {
    public static void main(String[] args) {
        Set<Character> nonTerminals = new HashSet<>(Arrays.asList('S', 'D', 'E', 'J'));
        Set<Character> terminals = new HashSet<>(Arrays.asList('a', 'b', 'c', 'd', 'e'));
        Map<Character, List<String>> rules = new HashMap<>();
        rules.put('S', Arrays.asList("aD"));
        rules.put('D', Arrays.asList("dE", "bJ", "aE"));
        rules.put('J', Arrays.asList("cS"));
        rules.put('E', Arrays.asList("e", "aE"));

        Grammar grammar = new Grammar(nonTerminals, terminals, rules, 'S');
        List<String> generatedStrings = grammar.generateStrings(5);

        System.out.println("Generated Strings:");
        for (String str : generatedStrings) {
            System.out.println(str);
        }

        FiniteAutomaton fa = grammar.convertToFA();

        System.out.println("\nTesting :");
        List<String> testStrings = Arrays.asList("ade", "aaaaaaae", "aec", "abcade", "acd");
        for (String str : testStrings) {
            System.out.println("String: " + str + " - Accepted: " + fa.accepts(str));
        }
    }
}
