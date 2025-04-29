package lab5;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> nonTerminals = Arrays.asList("S", "A", "B", "C");
        List<String> terminals = Arrays.asList("a", "d");

        Map<String, List<String>> rules = new HashMap<>();
        rules.put("S", Arrays.asList("dB", "A"));
        rules.put("A", Arrays.asList("d", "dS", "aAdAB"));
        rules.put("B", Arrays.asList("a", "aS","A","ε"));
        rules.put("C", Arrays.asList("Aa"));

        Grammar grammar = new Grammar(nonTerminals, terminals, rules, "S");

        System.out.println("Original grammar:");
        grammar.printRules();

        System.out.println("\nConverting to CNF:");
        grammar.toCNF(true);

        // NO TEST EXECUTION HERE!
    }
}
