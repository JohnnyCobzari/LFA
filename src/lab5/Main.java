package lab5;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> nonTerminals = Arrays.asList("S", "A", "B");
        List<String> terminals = Arrays.asList("a", "b");

        Map<String, List<String>> rules = new HashMap<>();
        rules.put("S", Arrays.asList("aB", "baA"));
        rules.put("A", Arrays.asList("bAaB", "aS", "a"));
        rules.put("B", Arrays.asList("A", "BS","ε"));


        Grammar grammar = new Grammar(nonTerminals, terminals, rules, "S");

        System.out.println("Original grammar:");
        grammar.printRules();

        System.out.println("\nConverting to CNF:");
        grammar.toCNF(true);

        // NO TEST EXECUTION HERE!
    }
}
